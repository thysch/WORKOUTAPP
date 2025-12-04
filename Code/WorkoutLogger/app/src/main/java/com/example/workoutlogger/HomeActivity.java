package com.example.workoutlogger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.WorkoutLog;
import com.example.workoutlogger.data.WorkoutPlan;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    private AppDatabase db;
    private CalendarView calendarView;
    private TextView recommendationText;
    private RecyclerView dailyWorkoutsRecyclerView;
    private DailyWorkoutAdapter dailyWorkoutAdapter;
    private int mCurrentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int colorRes = sharedPreferences.getInt("selected_theme_color", R.color.colorPrimary);
        mCurrentTheme = getThemeResId(colorRes);
        setTheme(mCurrentTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = AppDatabase.getDatabase(getApplicationContext());
        calendarView = findViewById(R.id.calendar_view);
        recommendationText = findViewById(R.id.recommendation_text);
        dailyWorkoutsRecyclerView = findViewById(R.id.daily_workouts_recycler_view);
        dailyWorkoutsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dailyWorkoutAdapter = new DailyWorkoutAdapter(new ArrayList<>());
        dailyWorkoutsRecyclerView.setAdapter(dailyWorkoutAdapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_workouts) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }
            return false;
        });

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            loadWorkoutsForDay(year, month, dayOfMonth);
        });

        setWorkoutRecommendation();
        // Load workouts for today initially
        Calendar today = Calendar.getInstance();
        loadWorkoutsForDay(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
    }

    private void loadWorkoutsForDay(int year, int month, int dayOfMonth) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            long startDate = calendar.getTimeInMillis();
            calendar.set(year, month, dayOfMonth, 23, 59, 59);
            long endDate = calendar.getTimeInMillis();

            List<WorkoutLog> workoutLogs = db.workoutLogDao().getWorkoutLogsByDateRange(startDate, endDate);

            runOnUiThread(() -> {
                dailyWorkoutAdapter.updateData(workoutLogs);
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

    private void setWorkoutRecommendation() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<WorkoutPlan> allWorkoutPlans = db.workoutPlanDao().getAllWorkoutPlans();
            if (allWorkoutPlans.isEmpty()) {
                runOnUiThread(() -> {
                    recommendationText.setText("No workouts to recommend. Add a workout plan first!");
                });
                return;
            }

            List<String> workoutNames = new ArrayList<>();
            for (WorkoutPlan plan : allWorkoutPlans) {
                workoutNames.add(plan.name);
            }

            List<WorkoutLog> workoutLogs = db.workoutLogDao().getAllWorkoutLogs();
            Map<String, Long> lastPerformed = new HashMap<>();

            for (String workout : workoutNames) {
                lastPerformed.put(workout, Long.MAX_VALUE);
            }

            for (WorkoutLog log : workoutLogs) {
                if (workoutNames.contains(log.workoutName) && lastPerformed.get(log.workoutName) == Long.MAX_VALUE) {
                    lastPerformed.put(log.workoutName, log.date);
                }
            }

            String recommendation = workoutNames.get(0);
            long oldestDate = 0;

            for (Map.Entry<String, Long> entry : lastPerformed.entrySet()) {
                if (entry.getValue() == Long.MAX_VALUE) { // This workout was never done
                    recommendation = entry.getKey();
                    break;
                }
                if (entry.getValue() < oldestDate || oldestDate == 0) {
                    oldestDate = entry.getValue();
                    recommendation = entry.getKey();
                }
            }

            final String finalRecommendation = recommendation;
            runOnUiThread(() -> {
                recommendationText.setText("Recommended Workout: " + finalRecommendation);
            });
        });
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
