package com.example.workoutlogger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.workoutlogger.data.AppDatabase;

public class ProfileActivity extends AppCompatActivity {

    private AppDatabase db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = AppDatabase.getDatabase(getApplicationContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        TextView myStatsButton = findViewById(R.id.button_my_stats);
        myStatsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyStatsActivity.class);
            startActivity(intent);
        });

        TextView settingsButton = findViewById(R.id.button_settings);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        updateWorkoutStats();
        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        String name = sharedPreferences.getString("user_name", "Tim S");
        String imageUriString = sharedPreferences.getString("profile_image_uri", null);

        TextView nameTextView = findViewById(R.id.profile_name);
        nameTextView.setText(name);

        if (imageUriString != null) {
            ImageView profileImageView = findViewById(R.id.profile_avatar);
            profileImageView.setImageURI(Uri.parse(imageUriString));
        }
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
