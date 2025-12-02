package com.example.workoutlogger;

import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.WorkoutPlan;
import com.example.workoutlogger.data.WorkoutSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StartWorkoutActivity extends AppCompatActivity implements StartWorkoutSetAdapter.OnVolumeChangedListener {

    private AppDatabase db;
    private long workoutPlanId;
    private Chronometer uptimeChronometer;
    private TextView totalVolumeTextView;
    private RecyclerView exercisesRecyclerView;
    private StartWorkoutExerciseAdapter adapter;

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

        uptimeChronometer.setBase(SystemClock.elapsedRealtime());
        uptimeChronometer.start();

        loadWorkoutData();
    }

    private void loadWorkoutData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            WorkoutPlan workoutPlan = db.workoutPlanDao().getWorkoutPlanById(workoutPlanId);
            if (workoutPlan != null) {
                List<Exercise> exercises = new ArrayList<>();
                if (workoutPlan.exerciseIds != null && !workoutPlan.exerciseIds.isEmpty()) {
                    List<Long> exerciseIds = Arrays.stream(workoutPlan.exerciseIds.split(","))
                                                   .map(String::trim)
                                                   .map(Long::parseLong)
                                                   .collect(Collectors.toList());

                    for (Long id : exerciseIds) {
                        Exercise exercise = db.exerciseDao().getExerciseById(id);
                        if (exercise != null) {
                            exercises.add(exercise);
                        }
                    }
                }

                List<WorkoutSet> sets = db.workoutSetDao().getSetsForWorkoutPlan(workoutPlanId);

                runOnUiThread(() -> {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(workoutPlan.name);
                    }
                    adapter = new StartWorkoutExerciseAdapter(exercises, this);
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

    private void calculateTotalVolume() {
        float totalVolume = 0;
        if (adapter != null) {
            Map<Long, List<WorkoutSet>> setsByExercise = adapter.getSetsByExercise();
            for (List<WorkoutSet> sets : setsByExercise.values()) {
                for (WorkoutSet set : sets) {
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
        totalVolumeTextView.setText("Total Volume: " + totalVolume + " lbs");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
