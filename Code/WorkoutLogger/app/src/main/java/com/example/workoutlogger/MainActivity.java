package com.example.workoutlogger;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.WorkoutPlan;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

        FloatingActionButton newWorkoutButton = findViewById(R.id.new_workout_button);
        newWorkoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddWorkoutActivity.class);
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

        // Initial data seeding if necessary
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (db.exerciseDao().getAllExercises().isEmpty()) {
                seedData();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkouts();
    }

    private void loadWorkouts() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<WorkoutPlan> workoutPlans = db.workoutPlanDao().getAllWorkoutPlans();
            List<Exercise> allExercises = db.exerciseDao().getAllExercises();

            runOnUiThread(() -> {
                if (adapter == null) {
                    adapter = new WorkoutAdapter(workoutPlans, allExercises);
                    workoutList.setAdapter(adapter);
                } else {
                    adapter.updateData(workoutPlans, allExercises);
                }
            });
        });
    }

    private void seedData() {
        // This runs on a background thread
        Exercise ex1 = new Exercise(); ex1.name = "Bench Press"; ex1.type = "WEIGHT"; ex1.tags = "Chest,Triceps,Shoulders,Push"; long id1 = db.exerciseDao().insert(ex1);
        Exercise ex2 = new Exercise(); ex2.name = "Overhead Press"; ex2.type = "WEIGHT"; ex2.tags = "Shoulders,Triceps,Push"; long id2 = db.exerciseDao().insert(ex2);
        Exercise ex3 = new Exercise(); ex3.name = "Tricep Pushdown"; ex3.type = "WEIGHT"; ex3.tags = "Triceps,Push,Arms"; long id3 = db.exerciseDao().insert(ex3);
        Exercise ex4 = new Exercise(); ex4.name = "Pull Ups"; ex4.type = "WEIGHT"; ex4.tags = "Back,Biceps,Pull"; long id4 = db.exerciseDao().insert(ex4);
        Exercise ex5 = new Exercise(); ex5.name = "Bent Over Rows"; ex5.type = "WEIGHT"; ex5.tags = "Back,Biceps,Pull"; long id5 = db.exerciseDao().insert(ex5);
        Exercise ex6 = new Exercise(); ex6.name = "Bicep Curls"; ex6.type = "WEIGHT"; ex6.tags = "Biceps,Arms,Pull"; long id6 = db.exerciseDao().insert(ex6);
        Exercise ex7 = new Exercise(); ex7.name = "Squats"; ex7.type = "WEIGHT"; ex7.tags = "Legs,Quads,Glutes"; long id7 = db.exerciseDao().insert(ex7);
        Exercise ex8 = new Exercise(); ex8.name = "Deadlifts"; ex8.type = "WEIGHT"; ex8.tags = "Legs,Back,Hamstrings,Glutes"; long id8 = db.exerciseDao().insert(ex8);
        Exercise ex9 = new Exercise(); ex9.name = "Leg Press"; ex9.type = "WEIGHT"; ex9.tags = "Legs,Quads,Glutes,Calves"; long id9 = db.exerciseDao().insert(ex9);
        Exercise ex10 = new Exercise(); ex10.name = "Running"; ex10.type = "CARDIO"; ex10.tags = "Cardio"; long id10 = db.exerciseDao().insert(ex10);
        Exercise ex11 = new Exercise(); ex11.name = "Cycling"; ex11.type = "CARDIO"; ex11.tags = "Cardio,Legs"; long id11 = db.exerciseDao().insert(ex11);
        Exercise ex12 = new Exercise(); ex12.name = "Jump Rope"; ex12.type = "CARDIO"; ex12.tags = "Cardio,Calves"; long id12 = db.exerciseDao().insert(ex12);

        WorkoutPlan plan1 = new WorkoutPlan(); plan1.name = "Push Day"; plan1.exerciseIds = id1 + "," + id2 + "," + id3; db.workoutPlanDao().insert(plan1);
        WorkoutPlan plan2 = new WorkoutPlan(); plan2.name = "Pull Day"; plan2.exerciseIds = id4 + "," + id5 + "," + id6; db.workoutPlanDao().insert(plan2);
        WorkoutPlan plan3 = new WorkoutPlan(); plan3.name = "Leg Day"; plan3.exerciseIds = id7 + "," + id8 + "," + id9; db.workoutPlanDao().insert(plan3);
        WorkoutPlan plan4 = new WorkoutPlan(); plan4.name = "Cardio Day"; plan4.exerciseIds = id10 + "," + id11 + "," + id12; db.workoutPlanDao().insert(plan4);
    }
}
