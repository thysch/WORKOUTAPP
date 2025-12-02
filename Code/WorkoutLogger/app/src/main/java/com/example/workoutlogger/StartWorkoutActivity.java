package com.example.workoutlogger;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.WorkoutLog;
import com.example.workoutlogger.data.WorkoutPlan;
import com.example.workoutlogger.data.Set;
import com.example.workoutlogger.data.WorkoutSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StartWorkoutActivity extends AppCompatActivity implements StartWorkoutSetAdapter.OnVolumeChangedListener, StartWorkoutExerciseAdapter.OnWorkoutCompleteListener {

    private AppDatabase db;
    private long workoutPlanId;
    private Chronometer uptimeChronometer;
    private TextView totalVolumeTextView;
    private RecyclerView exercisesRecyclerView;
    private StartWorkoutExerciseAdapter adapter;
    private List<Exercise> exercises;
    private float totalVolume = 0;

    private final ActivityResultLauncher<Intent> selectExercisesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<Long> newExerciseIds = (ArrayList<Long>) result.getData().getSerializableExtra("SELECTED_EXERCISES");
                    if (newExerciseIds != null && !newExerciseIds.isEmpty()) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            List<Exercise> newExercises = db.exerciseDao().getExercisesByIds(newExerciseIds);
                            runOnUiThread(() -> {
                                exercises.addAll(newExercises);
                                adapter.notifyDataSetChanged();
                            });
                        });
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_workout);

        db = AppDatabase.getDatabase(getApplicationContext());
        workoutPlanId = getIntent().getLongExtra("WORKOUT_PLAN_ID", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        uptimeChronometer = findViewById(R.id.uptime_chronometer);
        totalVolumeTextView = findViewById(R.id.total_volume_text_view);
        exercisesRecyclerView = findViewById(R.id.exercises_recycler_view);
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton addExerciseFab = findViewById(R.id.add_exercise_fab);
        addExerciseFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectExerciseActivity.class);
            selectExercisesLauncher.launch(intent);
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                if (exercises != null) {
                    Collections.swap(exercises, fromPosition, toPosition);
                    adapter.notifyItemMoved(fromPosition, toPosition);
                }
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });

        itemTouchHelper.attachToRecyclerView(exercisesRecyclerView);

        uptimeChronometer.setBase(SystemClock.elapsedRealtime());
        uptimeChronometer.start();

        loadWorkoutData();
    }

    private void loadWorkoutData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            WorkoutPlan workoutPlan = db.workoutPlanDao().getWorkoutPlanById(workoutPlanId);
            if (workoutPlan != null) {
                exercises = new ArrayList<>();
                if (workoutPlan.exerciseIds != null && !workoutPlan.exerciseIds.isEmpty()) {
                    List<Long> exerciseIds = Arrays.stream(workoutPlan.exerciseIds.split(","))
                                                   .map(String::trim)
                                                   .map(Long::parseLong)
                                                   .collect(Collectors.toList());

                    exercises.addAll(db.exerciseDao().getExercisesByIds(exerciseIds));
                }

                List<WorkoutSet> sets = db.workoutSetDao().getSetsForWorkoutPlan(workoutPlanId);

                runOnUiThread(() -> {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(workoutPlan.name);
                    }
                    adapter = new StartWorkoutExerciseAdapter(exercises, this, this);
                    exercisesRecyclerView.setAdapter(adapter);
                    adapter.populateSets(sets);
                    adapter.notifyDataSetChanged();
                    calculateTotalVolume();
                });
            }
        });
    }

    @Override
    public void onVolumeChanged() {
        calculateTotalVolume();
    }

    @Override
    public void onWorkoutComplete() {
        Map<Long, List<WorkoutSet>> setsByExercise = adapter.getSetsByExercise();
        for (List<WorkoutSet> sets : setsByExercise.values()) {
            for (WorkoutSet set : sets) {
                if (!set.isCompleted) {
                    return; // Not all sets are completed
                }
            }
        }

        // If we reach here, all sets are completed
        saveWorkoutAndFinish();
    }

    private void saveWorkoutAndFinish() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long workoutLogId = saveWorkout();
            runOnUiThread(() -> {
                Intent intent = new Intent(this, WorkoutCompleteActivity.class);
                intent.putExtra(WorkoutCompleteActivity.EXTRA_WORKOUT_LOG_ID, workoutLogId);
                intent.putExtra(WorkoutCompleteActivity.EXTRA_USER_NAME, "Tim S"); // Replace with actual user name
                startActivity(intent);
                finish();
            });
        });
    }

    private void calculateTotalVolume() {
        totalVolume = 0;
        if (adapter != null) {
            Map<Long, List<WorkoutSet>> setsByExercise = adapter.getSetsByExercise();
            for (List<WorkoutSet> sets : setsByExercise.values()) {
                for (WorkoutSet set : sets) {
                    if (set.isCompleted) {
                        try {
                            int reps = Integer.parseInt(set.plannedReps);
                            float weight = set.weight;
                            totalVolume += reps * weight;
                        } catch (NumberFormatException e) {
                            // Ignore if reps is not a valid number
                        }
                    }
                }
            }
        }
        totalVolumeTextView.setText("Total Volume: " + totalVolume + " lbs");
    }

    private long saveWorkout() {
        WorkoutLog workoutLog = new WorkoutLog();
        workoutLog.planId = workoutPlanId;
        workoutLog.date = new Date().getTime();
        workoutLog.duration = SystemClock.elapsedRealtime() - uptimeChronometer.getBase();
        workoutLog.totalVolume = totalVolume;
        workoutLog.workoutName = getSupportActionBar().getTitle().toString();

        long workoutLogId = db.workoutLogDao().insert(workoutLog);

        List<Set> setsToSave = new ArrayList<>();
        Map<Long, List<WorkoutSet>> setsByExercise = adapter.getSetsByExercise();

        for (Exercise exercise : exercises) {
            List<WorkoutSet> completedSets = setsByExercise.get(exercise.uid);
            if (completedSets == null) continue;

            for (int i = 0; i < completedSets.size(); i++) {
                WorkoutSet completedSet = completedSets.get(i);
                if (completedSet.isCompleted) {
                    Set setToSave = new Set();
                    setToSave.workoutLogId = workoutLogId;
                    setToSave.exerciseId = exercise.uid;
                    setToSave.setNumber = i + 1;
                    if ("CARDIO".equals(exercise.type)) {
                        setToSave.duration = completedSet.duration;
                        setToSave.distance = completedSet.distance;
                    } else {
                        setToSave.reps = Integer.parseInt(completedSet.plannedReps);
                        setToSave.weight = completedSet.weight;
                    }
                    setsToSave.add(setToSave);
                }
            }
        }

        db.setDao().insertAll(setsToSave.toArray(new Set[0]));
        return workoutLogId;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Discard workout?")
                .setMessage("Do you want to save this workout session?")
                .setPositiveButton("Save", (dialog, which) -> saveWorkoutAndFinish())
                .setNegativeButton("Discard", (dialog, which) -> finish())
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
