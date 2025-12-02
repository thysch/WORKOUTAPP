package com.example.workoutlogger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
