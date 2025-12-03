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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
