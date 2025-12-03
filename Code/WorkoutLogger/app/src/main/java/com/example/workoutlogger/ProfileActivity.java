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
import com.bumptech.glide.Glide;
import com.example.workoutlogger.data.AppDatabase;

public class ProfileActivity extends AppCompatActivity {

    private AppDatabase db;
    private SharedPreferences sharedPreferences;
    private int mCurrentTheme;
    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int colorRes = sharedPreferences.getInt("selected_theme_color", R.color.colorPrimary);
        mCurrentTheme = getThemeResId(colorRes);
        setTheme(mCurrentTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = AppDatabase.getDatabase(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        profileImageView = findViewById(R.id.profile_avatar);

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int colorRes = sharedPreferences.getInt("selected_theme_color", R.color.colorPrimary);
        if (mCurrentTheme != getThemeResId(colorRes)) {
            recreate();
            return;
        }
        // Reload profile to reflect changes from SettingsActivity
        loadProfile();
    }

    private void loadProfile() {
        String name = sharedPreferences.getString("user_name", "Tim S");
        String imageUriString = sharedPreferences.getString("profile_image_uri", null);

        TextView nameTextView = findViewById(R.id.profile_name);
        nameTextView.setText(name);

        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .placeholder(R.drawable.ic_default_profile)
                .error(R.drawable.ic_default_profile) // In case of error (e.g., file deleted)
                .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.ic_default_profile);
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
