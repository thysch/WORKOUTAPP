package com.example.workoutlogger;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.WorkoutPlan;

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

        loadWorkouts();
    }

    private void loadWorkouts() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Seed data if the database is empty
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
        Exercise ex1 = new Exercise(); ex1.name = "Bench Press"; ex1.type = "WEIGHT"; db.exerciseDao().insert(ex1);
        Exercise ex2 = new Exercise(); ex2.name = "Overhead Press"; ex2.type = "WEIGHT"; db.exerciseDao().insert(ex2);
        Exercise ex3 = new Exercise(); ex3.name = "Tricep Pushdown"; ex3.type = "WEIGHT"; db.exerciseDao().insert(ex3);
        Exercise ex4 = new Exercise(); ex4.name = "Pull Ups"; ex4.type = "WEIGHT"; db.exerciseDao().insert(ex4);
        Exercise ex5 = new Exercise(); ex5.name = "Bent Over Rows"; ex5.type = "WEIGHT"; db.exerciseDao().insert(ex5);
        Exercise ex6 = new Exercise(); ex6.name = "Bicep Curls"; ex6.type = "WEIGHT"; db.exerciseDao().insert(ex6);
        Exercise ex7 = new Exercise(); ex7.name = "Squats"; ex7.type = "WEIGHT"; db.exerciseDao().insert(ex7);
        Exercise ex8 = new Exercise(); ex8.name = "Deadlifts"; ex8.type = "WEIGHT"; db.exerciseDao().insert(ex8);
        Exercise ex9 = new Exercise(); ex9.name = "Leg Press"; ex9.type = "WEIGHT"; db.exerciseDao().insert(ex9);
        Exercise ex10 = new Exercise(); ex10.name = "Running"; ex10.type = "CARDIO"; db.exerciseDao().insert(ex10);
        Exercise ex11 = new Exercise(); ex11.name = "Cycling"; ex11.type = "CARDIO"; db.exerciseDao().insert(ex11);
        Exercise ex12 = new Exercise(); ex12.name = "Jump Rope"; ex12.type = "CARDIO"; db.exerciseDao().insert(ex12);

        WorkoutPlan plan1 = new WorkoutPlan(); plan1.name = "Push Day"; plan1.exerciseIds = "1, 2, 3"; db.workoutPlanDao().insert(plan1);
        WorkoutPlan plan2 = new WorkoutPlan(); plan2.name = "Pull Day"; plan2.exerciseIds = "4, 5, 6"; db.workoutPlanDao().insert(plan2);
        WorkoutPlan plan3 = new WorkoutPlan(); plan3.name = "Leg Day"; plan3.exerciseIds = "7, 8, 9"; db.workoutPlanDao().insert(plan3);
        WorkoutPlan plan4 = new WorkoutPlan(); plan4.name = "Cardio Day"; plan4.exerciseIds = "10, 11, 12"; db.workoutPlanDao().insert(plan4);
    }
}
