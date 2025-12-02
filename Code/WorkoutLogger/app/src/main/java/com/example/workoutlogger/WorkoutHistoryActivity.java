package com.example.workoutlogger;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.WorkoutLog;

import java.util.List;

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
            runOnUiThread(() -> {
                adapter = new WorkoutHistoryAdapter(workoutLogs);
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
