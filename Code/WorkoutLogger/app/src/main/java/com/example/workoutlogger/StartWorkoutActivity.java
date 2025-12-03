package com.example.workoutlogger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
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
    private List<Exercise> exercises = new ArrayList<>();
    private float totalVolume = 0;
    private EditText workoutNameEditText;
    private int mCurrentTheme;

    private final ActivityResultLauncher<Intent> selectExercisesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    long newExerciseId = result.getData().getLongExtra("exerciseId", -1);
                    if (newExerciseId != -1) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            Exercise newExercise = db.exerciseDao().getExerciseById(newExerciseId);
                            if (newExercise != null) {
                                runOnUiThread(() -> {
                                    exercises.add(newExercise);
                                    adapter.notifyItemInserted(exercises.size() - 1);
                                });
                            }
                        });
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int colorRes = sharedPreferences.getInt("selected_theme_color", R.color.colorPrimary);
        mCurrentTheme = getThemeResId(colorRes);
        setTheme(mCurrentTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_workout);

        db = AppDatabase.getDatabase(getApplicationContext());
        workoutPlanId = getIntent().getLongExtra("WORKOUT_PLAN_ID", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        workoutNameEditText = findViewById(R.id.workout_name_edit_text);
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

        adapter = new StartWorkoutExerciseAdapter(exercises, this, this, this::getUncheckedSetsCount);
        exercisesRecyclerView.setAdapter(adapter);

        if (workoutPlanId == -1) {
            // This is an empty workout
            getSupportActionBar().setTitle("New Workout");
            workoutNameEditText.setVisibility(View.VISIBLE);
            adapter.populateSets(new ArrayList<>()); // Initialize with empty sets
        } else {
            // This is a planned workout
            loadWorkoutData();
        }
    }

    private void loadWorkoutData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            WorkoutPlan workoutPlan = db.workoutPlanDao().getWorkoutPlanById(workoutPlanId);
            if (workoutPlan != null) {
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
                    adapter.populateSets(sets);
                    adapter.notifyDataSetChanged();
                    calculateTotalVolume();
                });
            }
        });
    }

    private int getUncheckedSetsCount() {
        int count = 0;
        if (adapter != null) {
            for (List<WorkoutSet> sets : adapter.getSetsByExercise().values()) {
                for (WorkoutSet set : sets) {
                    if (!set.isCompleted) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int colorRes = sharedPreferences.getInt("selected_theme_color", R.color.colorPrimary);
        if (mCurrentTheme != getThemeResId(colorRes)) {
            recreate();
        }
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
        String workoutName;
        if (workoutPlanId == -1) {
            workoutName = workoutNameEditText.getText().toString().trim();
            if (TextUtils.isEmpty(workoutName)) {
                runOnUiThread(() -> Toast.makeText(this, "Please enter a workout name", Toast.LENGTH_SHORT).show());
                return;
            }
        } else {
            workoutName = getSupportActionBar().getTitle().toString();
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            long workoutLogId = saveWorkout(workoutName);
            runOnUiThread(() -> {
                Intent intent = new Intent(this, WorkoutCompleteActivity.class);
                intent.putExtra(WorkoutCompleteActivity.EXTRA_WORKOUT_LOG_ID, workoutLogId);
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

    private long saveWorkout(String workoutName) {
        WorkoutLog workoutLog = new WorkoutLog();
        if (workoutPlanId != -1) {
            workoutLog.planId = workoutPlanId;
        }
        workoutLog.date = new Date().getTime();
        workoutLog.duration = SystemClock.elapsedRealtime() - uptimeChronometer.getBase();
        workoutLog.totalVolume = totalVolume;
        workoutLog.workoutName = workoutName;

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

        if (!setsToSave.isEmpty()) {
            db.setDao().insertAll(setsToSave.toArray(new Set[0]));
        }
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

    private int getThemeResId(int colorRes) {
        if (colorRes == R.color.theme_red) return R.style.Theme_WorkoutLogger_Red;
        if (colorRes == R.color.theme_pink) return R.style.Theme_WorkoutLogger_Pink;
        if (colorRes == R.color.theme_purple) return R.style.Theme_WorkoutLogger_Purple;
        if (colorRes == R.color.theme_deep_purple) return R.style.Theme_WorkoutLogger_DeepPurple;
        if (colorRes == R.color.theme_indigo) return R.style.Theme_WorkoutLogger_Indigo;
        if (colorRes == R.color.theme_blue) return R.style.Theme_WorkoutLogger_Blue;
        if (colorRes == R.color.theme_teal) return R.style.Theme_WorkoutLogger_Teal;
        if (colorRes == R.color.theme_green) return R.style.Theme_WorkoutLogger_Green;
        if (colorRes == R.color.theme_orange) return R.style.Theme_WorkoutLogger_Orange;
        if (colorRes == R.color.theme_brown) return R.style.Theme_WorkoutLogger_Brown;
        return R.style.Theme_WorkoutLogger;
    }
}
