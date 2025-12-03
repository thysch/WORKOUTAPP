package com.example.workoutlogger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.Set;

import java.util.List;
import java.util.Locale;

public class MyStatsActivity extends AppCompatActivity {

    private AppDatabase db;
    private TextView totalWorkoutsTextView;
    private TextView totalWeightLiftedTextView;
    private int mCurrentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int colorRes = sharedPreferences.getInt("selected_theme_color", R.color.colorPrimary);
        mCurrentTheme = getThemeResId(colorRes);
        setTheme(mCurrentTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_stats);

        db = AppDatabase.getDatabase(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Stats");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        totalWorkoutsTextView = findViewById(R.id.total_workouts_text_view);
        totalWeightLiftedTextView = findViewById(R.id.total_weight_lifted_text_view);

        loadStats();
    }

    private void loadStats() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int totalWorkouts = db.workoutLogDao().getWorkoutLogCount();
            List<Set> allSets = db.setDao().getAllSets();

            float totalWeight = 0;
            for (Set set : allSets) {
                if (set.weight > 0 && set.reps > 0) { // Assuming weight-based sets
                    totalWeight += set.weight * set.reps;
                }
            }

            final float finalTotalWeight = totalWeight;

            runOnUiThread(() -> {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String weightUnit = sharedPreferences.getString("weight_unit", "lbs");

                totalWorkoutsTextView.setText(String.valueOf(totalWorkouts));

                float displayWeight = finalTotalWeight;
                if ("kg".equals(weightUnit)) {
                    displayWeight *= 0.453592f;
                }

                totalWeightLiftedTextView.setText(String.format(Locale.getDefault(), "%.1f %s", displayWeight, weightUnit));
            });
        });
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
