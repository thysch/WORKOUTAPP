package com.example.workoutlogger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.workoutlogger.data.AppDatabase;

public class ProfileActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = AppDatabase.getDatabase(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView workoutHistoryButton = findViewById(R.id.button_workout_history);
        workoutHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, WorkoutHistoryActivity.class);
            startActivity(intent);
        });

        updateWorkoutStats();
    }

    private void updateWorkoutStats() {
        TextView statsTextView = findViewById(R.id.profile_stats);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int workoutCount = db.workoutLogDao().getWorkoutLogCount();
            runOnUiThread(() -> {
                statsTextView.setText(workoutCount + " Workouts Completed");
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
