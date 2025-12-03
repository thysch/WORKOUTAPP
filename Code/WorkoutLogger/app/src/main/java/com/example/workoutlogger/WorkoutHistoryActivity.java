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
    private int mCurrentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int colorRes = sharedPreferences.getInt("selected_theme_color", R.color.colorPrimary);
        mCurrentTheme = getThemeResId(colorRes);
        setTheme(mCurrentTheme);

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
