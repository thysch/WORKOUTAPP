package com.example.workoutlogger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.Set;
import com.example.workoutlogger.data.WorkoutLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutHistoryActivity extends AppCompatActivity {

    private AppDatabase db;
    private RecyclerView workoutHistoryRecyclerView;
    private WorkoutHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        db = AppDatabase.getDatabase(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Workout History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        workoutHistoryRecyclerView = findViewById(R.id.workout_history_recycler_view);
        workoutHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadWorkoutHistory();
    }

    private void loadWorkoutHistory() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<WorkoutLog> workoutLogs = db.workoutLogDao().getAllWorkoutLogs();
            List<Set> allSets = db.setDao().getAllSets();
            List<Exercise> allExercises = db.exerciseDao().getAllExercises(); // Assuming you have this method

            Map<Long, List<Set>> setsByWorkoutLogId = new HashMap<>();
            for (Set set : allSets) {
                if (!setsByWorkoutLogId.containsKey(set.workoutLogId)) {
                    setsByWorkoutLogId.put(set.workoutLogId, new java.util.ArrayList<>());
                }
                setsByWorkoutLogId.get(set.workoutLogId).add(set);
            }

            Map<Long, Exercise> exerciseById = new HashMap<>();
            for (Exercise exercise : allExercises) {
                exerciseById.put(exercise.uid, exercise);
            }

            runOnUiThread(() -> {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String weightUnit = sharedPreferences.getString("weight_unit", "lbs");
                String distanceUnit = sharedPreferences.getString("distance_unit", "mi");

                adapter = new WorkoutHistoryAdapter(workoutLogs, setsByWorkoutLogId, exerciseById, weightUnit, distanceUnit);
                workoutHistoryRecyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
