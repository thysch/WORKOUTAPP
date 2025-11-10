package com.example.workoutlogger;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.WorkoutPlan;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView workoutList;
    private AppDatabase db;
    private WorkoutAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(getApplicationContext());

        workoutList = findViewById(R.id.workout_list);
        workoutList.setLayoutManager(new LinearLayoutManager(this));

        Button newWorkoutButton = findViewById(R.id.new_workout_button);
        newWorkoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddWorkoutActivity.class);
            startActivity(intent);
        });


        // Temporarily add this button to your activity_main.xml to test
        Button testShareButton = findViewById(R.id.test_share_button);
        testShareButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ShareWorkoutActivity.class);
            startActivity(intent);
        });



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_home) {
                // Already on the main activity, so do nothing or refresh
                return true;
            } else if (itemId == R.id.nav_workouts) {
                // This could be a different view or activity for all workouts.
                // For now, we'll just stay here.
                return true;
            }
            return false;
        });

        loadWorkouts();
    }

    private void loadWorkouts() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (db.exerciseDao().getAllExercises().isEmpty()) {
                seedData();
            }

            List<WorkoutPlan> workoutPlans = db.workoutPlanDao().getAllWorkoutPlans();
            List<Exercise> allExercises = db.exerciseDao().getAllExercises();

            runOnUiThread(() -> {
                adapter = new WorkoutAdapter(workoutPlans, allExercises);
                workoutList.setAdapter(adapter);
            });
        });
    }

    private void seedData() {
        // ... (seed data method remains the same)
    }
}
