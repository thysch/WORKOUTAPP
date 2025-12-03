package com.example.workoutlogger;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import androidx.annotation.NonNull;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.WorkoutPlan;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView workoutList;
    private AppDatabase db;
    private WorkoutAdapter adapter;
    private List<WorkoutPlan> workoutPlans;
    private int mCurrentTheme;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int colorRes = sharedPreferences.getInt("selected_theme_color", R.color.colorPrimary);
        mCurrentTheme = getThemeResId(colorRes);
        setTheme(mCurrentTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(getApplicationContext());

        workoutList = findViewById(R.id.workout_list);
        workoutList.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                Collections.swap(workoutPlans, fromPosition, toPosition);
                adapter.notifyItemMoved(fromPosition, toPosition);
                updateWorkoutOrder();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });

        itemTouchHelper.attachToRecyclerView(workoutList);

        Button startEmptyWorkoutButton = findViewById(R.id.start_empty_workout_button);
        startEmptyWorkoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StartWorkoutActivity.class);
            startActivity(intent);
        });

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

        // Initial data loading and seeding
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (db.exerciseDao().getAllExercises().isEmpty()) {
                seedData();
            }
            // Always load data after checking for seeding
            runOnUiThread(this::loadWorkouts);
        });
    }

    private void updateWorkoutOrder() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            for (int i = 0; i < workoutPlans.size(); i++) {
                workoutPlans.get(i).displayOrder = i + 1;
            }
            db.workoutPlanDao().updateAll(workoutPlans);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // We call loadWorkouts here as well to refresh the data when the user
        // navigates back to this screen after adding/editing a workout.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int colorRes = sharedPreferences.getInt("selected_theme_color", R.color.colorPrimary);
        if (mCurrentTheme != getThemeResId(colorRes)) {
            recreate();
            return;
        }
        loadWorkouts();
    }



    private void loadWorkouts() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            workoutPlans = db.workoutPlanDao().getAllWorkoutPlans();
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
        Exercise ex4 = new Exercise(); ex4.name = "Pull Ups"; ex4.type = "WEIGHT"; ex4.tags = "Back,Biceps,Pull,Bodyweight"; long id4 = db.exerciseDao().insert(ex4);
        Exercise ex5 = new Exercise(); ex5.name = "Bent Over Rows"; ex5.type = "WEIGHT"; ex5.tags = "Back,Biceps,Pull"; long id5 = db.exerciseDao().insert(ex5);
        Exercise ex6 = new Exercise(); ex6.name = "Bicep Curls"; ex6.type = "WEIGHT"; ex6.tags = "Biceps,Arms,Pull"; long id6 = db.exerciseDao().insert(ex6);
        Exercise ex7 = new Exercise(); ex7.name = "Squats"; ex7.type = "WEIGHT"; ex7.tags = "Legs,Quads,Glutes,Bodyweight"; long id7 = db.exerciseDao().insert(ex7);
        Exercise ex8 = new Exercise(); ex8.name = "Deadlifts"; ex8.type = "WEIGHT"; ex8.tags = "Legs,Back,Hamstrings,Glutes"; long id8 = db.exerciseDao().insert(ex8);
        Exercise ex9 = new Exercise(); ex9.name = "Leg Press"; ex9.type = "WEIGHT"; ex9.tags = "Legs,Quads,Glutes,Calves"; long id9 = db.exerciseDao().insert(ex9);
        Exercise ex10 = new Exercise(); ex10.name = "Running"; ex10.type = "CARDIO"; ex10.tags = "Cardio"; long id10 = db.exerciseDao().insert(ex10);
        Exercise ex11 = new Exercise(); ex11.name = "Cycling"; ex11.type = "CARDIO"; ex11.tags = "Cardio,Legs"; long id11 = db.exerciseDao().insert(ex11);
        Exercise ex12 = new Exercise(); ex12.name = "Jump Rope"; ex12.type = "CARDIO"; ex12.tags = "Cardio,Calves"; long id12 = db.exerciseDao().insert(ex12);
        Exercise ex13 = new Exercise(); ex13.name = "Incline Bench Press"; ex13.type = "WEIGHT"; ex13.tags = "Chest,Triceps,Shoulders,Push,Barbell"; long id13 = db.exerciseDao().insert(ex13);
        Exercise ex14 = new Exercise(); ex14.name = "Decline Bench Press"; ex14.type = "WEIGHT"; ex14.tags = "Chest,Triceps,Shoulders,Push,Barbell"; long id14 = db.exerciseDao().insert(ex14);
        Exercise ex15 = new Exercise(); ex15.name = "Dumbbell Bench Press"; ex15.type = "WEIGHT"; ex15.tags = "Chest,Triceps,Shoulders,Push,Dumbbell"; long id15 = db.exerciseDao().insert(ex15);
        Exercise ex16 = new Exercise(); ex16.name = "Dumbbell Flyes"; ex16.type = "WEIGHT"; ex16.tags = "Chest,Push,Dumbbell"; long id16 = db.exerciseDao().insert(ex16);
        Exercise ex17 = new Exercise(); ex17.name = "Push Ups"; ex17.type = "WEIGHT"; ex17.tags = "Chest,Triceps,Shoulders,Push,Bodyweight"; long id17 = db.exerciseDao().insert(ex17);
        Exercise ex18 = new Exercise(); ex18.name = "Dips"; ex18.type = "WEIGHT"; ex18.tags = "Chest,Triceps,Shoulders,Push,Bodyweight"; long id18 = db.exerciseDao().insert(ex18);
        Exercise ex19 = new Exercise(); ex19.name = "Arnold Press"; ex19.type = "WEIGHT"; ex19.tags = "Shoulders,Triceps,Push,Dumbbell"; long id19 = db.exerciseDao().insert(ex19);
        Exercise ex20 = new Exercise(); ex20.name = "Lateral Raises"; ex20.type = "WEIGHT"; ex20.tags = "Shoulders,Push,Dumbbell"; long id20 = db.exerciseDao().insert(ex20);
        Exercise ex21 = new Exercise(); ex21.name = "Front Raises"; ex21.type = "WEIGHT"; ex21.tags = "Shoulders,Push,Dumbbell"; long id21 = db.exerciseDao().insert(ex21);
        Exercise ex22 = new Exercise(); ex22.name = "Tricep Extensions"; ex22.type = "WEIGHT"; ex22.tags = "Triceps,Push,Arms,Dumbbell"; long id22 = db.exerciseDao().insert(ex22);
        Exercise ex23 = new Exercise(); ex23.name = "Skull Crushers"; ex23.type = "WEIGHT"; ex23.tags = "Triceps,Push,Arms,Barbell"; long id23 = db.exerciseDao().insert(ex23);
        Exercise ex24 = new Exercise(); ex24.name = "Overhead Tricep Extensions"; ex24.type = "WEIGHT"; ex24.tags = "Triceps,Push,Arms,Dumbbell"; long id24 = db.exerciseDao().insert(ex24);
        Exercise ex25 = new Exercise(); ex25.name = "Chin Ups"; ex25.type = "WEIGHT"; ex25.tags = "Back,Biceps,Pull,Bodyweight"; long id25 = db.exerciseDao().insert(ex25);
        Exercise ex26 = new Exercise(); ex26.name = "Lat Pulldowns"; ex26.type = "WEIGHT"; ex26.tags = "Back,Biceps,Pull,Machine"; long id26 = db.exerciseDao().insert(ex26);
        Exercise ex27 = new Exercise(); ex27.name = "Seated Rows"; ex27.type = "WEIGHT"; ex27.tags = "Back,Biceps,Pull,Machine"; long id27 = db.exerciseDao().insert(ex27);
        Exercise ex28 = new Exercise(); ex28.name = "Face Pulls"; ex28.type = "WEIGHT"; ex28.tags = "Shoulders,Back,Pull,Cable"; long id28 = db.exerciseDao().insert(ex28);
        Exercise ex29 = new Exercise(); ex29.name = "Hammer Curls"; ex29.type = "WEIGHT"; ex29.tags = "Biceps,Arms,Pull,Dumbbell"; long id29 = db.exerciseDao().insert(ex29);
        Exercise ex30 = new Exercise(); ex30.name = "Preacher Curls"; ex30.type = "WEIGHT"; ex30.tags = "Biceps,Arms,Pull,Barbell"; long id30 = db.exerciseDao().insert(ex30);
        Exercise ex31 = new Exercise(); ex31.name = "Concentration Curls"; ex31.type = "WEIGHT"; ex31.tags = "Biceps,Arms,Pull,Dumbbell"; long id31 = db.exerciseDao().insert(ex31);
        Exercise ex32 = new Exercise(); ex32.name = "Leg Extensions"; ex32.type = "WEIGHT"; ex32.tags = "Legs,Quads,Machine"; long id32 = db.exerciseDao().insert(ex32);
        Exercise ex33 = new Exercise(); ex33.name = "Leg Curls"; ex33.type = "WEIGHT"; ex33.tags = "Legs,Hamstrings,Machine"; long id33 = db.exerciseDao().insert(ex33);
        Exercise ex34 = new Exercise(); ex34.name = "Calf Raises"; ex34.type = "WEIGHT"; ex34.tags = "Legs,Calves,Bodyweight"; long id34 = db.exerciseDao().insert(ex34);
        Exercise ex35 = new Exercise(); ex35.name = "Seated Calf Raises"; ex35.type = "WEIGHT"; ex35.tags = "Legs,Calves,Machine"; long id35 = db.exerciseDao().insert(ex35);
        Exercise ex36 = new Exercise(); ex36.name = "Lunges"; ex36.type = "WEIGHT"; ex36.tags = "Legs,Quads,Glutes,Bodyweight"; long id36 = db.exerciseDao().insert(ex36);
        Exercise ex37 = new Exercise(); ex37.name = "Walking Lunges"; ex37.type = "WEIGHT"; ex37.tags = "Legs,Quads,Glutes,Dumbbell"; long id37 = db.exerciseDao().insert(ex37);
        Exercise ex38 = new Exercise(); ex38.name = "Romanian Deadlifts"; ex38.type = "WEIGHT"; ex38.tags = "Legs,Hamstrings,Back,Glutes,Barbell"; long id38 = db.exerciseDao().insert(ex38);
        Exercise ex39 = new Exercise(); ex39.name = "Good Mornings"; ex39.type = "WEIGHT"; ex39.tags = "Legs,Hamstrings,Back,Barbell"; long id39 = db.exerciseDao().insert(ex39);
        Exercise ex40 = new Exercise(); ex40.name = "Hip Thrusts"; ex40.type = "WEIGHT"; ex40.tags = "Glutes,Barbell"; long id40 = db.exerciseDao().insert(ex40);
        Exercise ex41 = new Exercise(); ex41.name = "Glute Bridges"; ex41.type = "WEIGHT"; ex41.tags = "Glutes,Bodyweight"; long id41 = db.exerciseDao().insert(ex41);
        Exercise ex42 = new Exercise(); ex42.name = "Crunches"; ex42.type = "WEIGHT"; ex42.tags = "Abs,Core,Bodyweight"; long id42 = db.exerciseDao().insert(ex42);
        Exercise ex43 = new Exercise(); ex43.name = "Russian Twists"; ex43.type = "WEIGHT"; ex43.tags = "Abs,Core,Bodyweight"; long id43 = db.exerciseDao().insert(ex43);
        Exercise ex44 = new Exercise(); ex44.name = "Leg Raises"; ex44.type = "WEIGHT"; ex44.tags = "Abs,Core,Bodyweight"; long id44 = db.exerciseDao().insert(ex44);
        Exercise ex45 = new Exercise(); ex45.name = "Bicycle Crunches"; ex45.type = "WEIGHT"; ex45.tags = "Abs,Core,Bodyweight"; long id45 = db.exerciseDao().insert(ex45);
        Exercise ex46 = new Exercise(); ex46.name = "Swimming"; ex46.type = "CARDIO"; ex46.tags = "Cardio,FullBody"; long id46 = db.exerciseDao().insert(ex46);
        Exercise ex47 = new Exercise(); ex47.name = "Rowing"; ex47.type = "CARDIO"; ex47.tags = "Cardio,Back,Legs"; long id47 = db.exerciseDao().insert(ex47);
        Exercise ex48 = new Exercise(); ex48.name = "Elliptical"; ex48.type = "CARDIO"; ex48.tags = "Cardio,Legs"; long id48 = db.exerciseDao().insert(ex48);
        Exercise ex49 = new Exercise(); ex49.name = "Stair Climber"; ex49.type = "CARDIO"; ex49.tags = "Cardio,Legs,Glutes"; long id49 = db.exerciseDao().insert(ex49);
        Exercise ex50 = new Exercise(); ex50.name = "Plank"; ex50.type = "CARDIO"; ex50.tags = "Abs,Core,Bodyweight"; long id50 = db.exerciseDao().insert(ex50);
        Exercise ex51 = new Exercise(); ex51.name = "Wall Sit"; ex51.type = "CARDIO"; ex51.tags = "Legs,Quads,Bodyweight"; long id51 = db.exerciseDao().insert(ex51);
        Exercise ex52 = new Exercise(); ex52.name = "Burpees"; ex52.type = "CARDIO"; ex52.tags = "Cardio,FullBody,Bodyweight"; long id52 = db.exerciseDao().insert(ex52);
        Exercise ex53 = new Exercise(); ex53.name = "Mountain Climbers"; ex53.type = "CARDIO"; ex53.tags = "Cardio,Core,Bodyweight"; long id53 = db.exerciseDao().insert(ex53);
        Exercise ex54 = new Exercise(); ex54.name = "Jumping Jacks"; ex54.type = "CARDIO"; ex54.tags = "Cardio,Bodyweight"; long id54 = db.exerciseDao().insert(ex54);
        Exercise ex55 = new Exercise(); ex55.name = "High Knees"; ex55.type = "CARDIO"; ex55.tags = "Cardio,Legs,Bodyweight"; long id55 = db.exerciseDao().insert(ex55);
        Exercise ex56 = new Exercise(); ex56.name = "Box Jumps"; ex56.type = "CARDIO"; ex56.tags = "Cardio,Legs"; long id56 = db.exerciseDao().insert(ex56);
        Exercise ex57 = new Exercise(); ex57.name = "Kettlebell Swings"; ex57.type = "WEIGHT"; ex57.tags = "Glutes,Hamstrings,Back,Kettlebell"; long id57 = db.exerciseDao().insert(ex57);
        Exercise ex58 = new Exercise(); ex58.name = "Farmer's Walk"; ex58.type = "WEIGHT"; ex58.tags = "FullBody,Grip,Dumbbell"; long id58 = db.exerciseDao().insert(ex58);
        Exercise ex59 = new Exercise(); ex59.name = "Cable Crossovers"; ex59.type = "WEIGHT"; ex59.tags = "Chest,Push,Cable"; long id59 = db.exerciseDao().insert(ex59);
        Exercise ex60 = new Exercise(); ex60.name = "Upright Rows"; ex60.type = "WEIGHT"; ex60.tags = "Shoulders,Traps,Pull,Barbell"; long id60 = db.exerciseDao().insert(ex60);
        Exercise ex61 = new Exercise(); ex61.name = "Shrugs"; ex61.type = "WEIGHT"; ex61.tags = "Traps,Shoulders,Dumbbell"; long id61 = db.exerciseDao().insert(ex61);
        Exercise ex62 = new Exercise(); ex62.name = "Reverse Flyes"; ex62.type = "WEIGHT"; ex62.tags = "Back,Shoulders,Pull,Dumbbell"; long id62 = db.exerciseDao().insert(ex62);
        Exercise ex63 = new Exercise(); ex63.name = "Step Ups"; ex63.type = "WEIGHT"; ex63.tags = "Legs,Quads,Glutes,Bodyweight"; long id63 = db.exerciseDao().insert(ex63);
        Exercise ex64 = new Exercise(); ex64.name = "Bulgarian Split Squats"; ex64.type = "WEIGHT"; ex64.tags = "Legs,Quads,Glutes,Dumbbell"; long id64 = db.exerciseDao().insert(ex64);
        Exercise ex65 = new Exercise(); ex65.name = "Side Lunges"; ex65.type = "WEIGHT"; ex65.tags = "Legs,Glutes,Bodyweight"; long id65 = db.exerciseDao().insert(ex65);
        Exercise ex66 = new Exercise(); ex66.name = "Cable Kickbacks"; ex66.type = "WEIGHT"; ex66.tags = "Triceps,Push,Arms,Cable"; long id66 = db.exerciseDao().insert(ex66);
        Exercise ex67 = new Exercise(); ex67.name = "Wrist Curls"; ex67.type = "WEIGHT"; ex67.tags = "Forearms,Arms,Dumbbell"; long id67 = db.exerciseDao().insert(ex67);
        Exercise ex68 = new Exercise(); ex68.name = "Reverse Wrist Curls"; ex68.type = "WEIGHT"; ex68.tags = "Forearms,Arms,Dumbbell"; long id68 = db.exerciseDao().insert(ex68);
        Exercise ex69 = new Exercise(); ex69.name = "Bird Dog"; ex69.type = "WEIGHT"; ex69.tags = "Core,Back,Bodyweight"; long id69 = db.exerciseDao().insert(ex69);
        Exercise ex70 = new Exercise(); ex70.name = "Superman"; ex70.type = "WEIGHT"; ex70.tags = "Back,Core,Bodyweight"; long id70 = db.exerciseDao().insert(ex70);
        Exercise ex71 = new Exercise(); ex71.name = "Medicine Ball Slams"; ex71.type = "CARDIO"; ex71.tags = "Cardio,FullBody"; long id71 = db.exerciseDao().insert(ex71);
        Exercise ex72 = new Exercise(); ex72.name = "Battle Ropes"; ex72.type = "CARDIO"; ex72.tags = "Cardio,Shoulders,Arms"; long id72 = db.exerciseDao().insert(ex72);
        Exercise ex73 = new Exercise(); ex73.name = "Sprints"; ex73.type = "CARDIO"; ex73.tags = "Cardio,Legs"; long id73 = db.exerciseDao().insert(ex73);
        Exercise ex74 = new Exercise(); ex74.name = "Incline Walking"; ex74.type = "CARDIO"; ex74.tags = "Cardio,Legs"; long id74 = db.exerciseDao().insert(ex74);
        Exercise ex75 = new Exercise(); ex75.name = "Shadow Boxing"; ex75.type = "CARDIO"; ex75.tags = "Cardio,Arms,Shoulders"; long id75 = db.exerciseDao().insert(ex75);
        Exercise ex76 = new Exercise(); ex76.name = "Close-Grip Bench Press"; ex76.type = "WEIGHT"; ex76.tags = "Chest,Triceps,Push,Barbell"; long id76 = db.exerciseDao().insert(ex76);
        Exercise ex77 = new Exercise(); ex77.name = "Pendlay Row"; ex77.type = "WEIGHT"; ex77.tags = "Back,Pull,Barbell"; long id77 = db.exerciseDao().insert(ex77);
        Exercise ex78 = new Exercise(); ex78.name = "T-Bar Row"; ex78.type = "WEIGHT"; ex78.tags = "Back,Biceps,Pull,Barbell"; long id78 = db.exerciseDao().insert(ex78);
        Exercise ex79 = new Exercise(); ex79.name = "Single-Arm Dumbbell Row"; ex79.type = "WEIGHT"; ex79.tags = "Back,Biceps,Pull,Dumbbell"; long id79 = db.exerciseDao().insert(ex79);
        Exercise ex80 = new Exercise(); ex80.name = "Deficit Deadlift"; ex80.type = "WEIGHT"; ex80.tags = "Legs,Back,Hamstrings,Glutes,Barbell"; long id80 = db.exerciseDao().insert(ex80);
        Exercise ex81 = new Exercise(); ex81.name = "Sumo Deadlift"; ex81.type = "WEIGHT"; ex81.tags = "Legs,Quads,Glutes,Back,Barbell"; long id81 = db.exerciseDao().insert(ex81);
        Exercise ex82 = new Exercise(); ex82.name = "Front Squat"; ex82.type = "WEIGHT"; ex82.tags = "Legs,Quads,Core,Barbell"; long id82 = db.exerciseDao().insert(ex82);
        Exercise ex83 = new Exercise(); ex83.name = "Goblet Squat"; ex83.type = "WEIGHT"; ex83.tags = "Legs,Quads,Glutes,Dumbbell"; long id83 = db.exerciseDao().insert(ex83);
        Exercise ex84 = new Exercise(); ex84.name = "Hack Squat"; ex84.type = "WEIGHT"; ex84.tags = "Legs,Quads,Machine"; long id84 = db.exerciseDao().insert(ex84);
        Exercise ex85 = new Exercise(); ex85.name = "Zercher Squat"; ex85.type = "WEIGHT"; ex85.tags = "Legs,Quads,Core,Barbell"; long id85 = db.exerciseDao().insert(ex85);
        Exercise ex86 = new Exercise(); ex86.name = "Landmine Press"; ex86.type = "WEIGHT"; ex86.tags = "Shoulders,Chest,Push,Barbell"; long id86 = db.exerciseDao().insert(ex86);
        Exercise ex87 = new Exercise(); ex87.name = "Landmine Row"; ex87.type = "WEIGHT"; ex87.tags = "Back,Pull,Barbell"; long id87 = db.exerciseDao().insert(ex87);
        Exercise ex88 = new Exercise(); ex88.name = "Cable Row"; ex88.type = "WEIGHT"; ex88.tags = "Back,Biceps,Pull,Cable"; long id88 = db.exerciseDao().insert(ex88);
        Exercise ex89 = new Exercise(); ex89.name = "Pec Deck Flyes"; ex89.type = "WEIGHT"; ex89.tags = "Chest,Push,Machine"; long id89 = db.exerciseDao().insert(ex89);
        Exercise ex90 = new Exercise(); ex90.name = "Incline Dumbbell Flyes"; ex90.type = "WEIGHT"; ex90.tags = "Chest,Push,Dumbbell"; long id90 = db.exerciseDao().insert(ex90);
        Exercise ex91 = new Exercise(); ex91.name = "Rear Delt Flyes"; ex91.type = "WEIGHT"; ex91.tags = "Shoulders,Back,Pull,Dumbbell"; long id91 = db.exerciseDao().insert(ex91);
        Exercise ex92 = new Exercise(); ex92.name = "Cable Lateral Raises"; ex92.type = "WEIGHT"; ex92.tags = "Shoulders,Push,Cable"; long id92 = db.exerciseDao().insert(ex92);
        Exercise ex93 = new Exercise(); ex93.name = "EZ-Bar Curl"; ex93.type = "WEIGHT"; ex93.tags = "Biceps,Arms,Pull,Barbell"; long id93 = db.exerciseDao().insert(ex93);
        Exercise ex94 = new Exercise(); ex94.name = "Cable Curl"; ex94.type = "WEIGHT"; ex94.tags = "Biceps,Arms,Pull,Cable"; long id94 = db.exerciseDao().insert(ex94);
        Exercise ex95 = new Exercise(); ex95.name = "Rope Pushdown"; ex95.type = "WEIGHT"; ex95.tags = "Triceps,Push,Arms,Cable"; long id95 = db.exerciseDao().insert(ex95);
        Exercise ex96 = new Exercise(); ex96.name = "Straight-Bar Pushdown"; ex96.type = "WEIGHT"; ex96.tags = "Triceps,Push,Arms,Cable"; long id96 = db.exerciseDao().insert(ex96);
        Exercise ex97 = new Exercise(); ex97.name = "Diamond Push Ups"; ex97.type = "WEIGHT"; ex97.tags = "Triceps,Chest,Push,Bodyweight"; long id97 = db.exerciseDao().insert(ex97);
        Exercise ex98 = new Exercise(); ex98.name = "Pike Push Ups"; ex98.type = "WEIGHT"; ex98.tags = "Shoulders,Push,Bodyweight"; long id98 = db.exerciseDao().insert(ex98);
        Exercise ex99 = new Exercise(); ex99.name = "Handstand Push Ups"; ex99.type = "WEIGHT"; ex99.tags = "Shoulders,Triceps,Push,Bodyweight"; long id99 = db.exerciseDao().insert(ex99);
        Exercise ex100 = new Exercise(); ex100.name = "Muscle Ups"; ex100.type = "WEIGHT"; ex100.tags = "Back,Chest,Triceps,Pull,Bodyweight"; long id100 = db.exerciseDao().insert(ex100);
        Exercise ex101 = new Exercise(); ex101.name = "Glute-Ham Raise"; ex101.type = "WEIGHT"; ex101.tags = "Hamstrings,Glutes,Bodyweight"; long id101 = db.exerciseDao().insert(ex101);
        Exercise ex102 = new Exercise(); ex102.name = "Nordic Hamstring Curl"; ex102.type = "WEIGHT"; ex102.tags = "Hamstrings,Bodyweight"; long id102 = db.exerciseDao().insert(ex102);
        Exercise ex103 = new Exercise(); ex103.name = "Donkey Calf Raises"; ex103.type = "WEIGHT"; ex103.tags = "Calves,Machine"; long id103 = db.exerciseDao().insert(ex103);
        Exercise ex104 = new Exercise(); ex104.name = "Single-Leg Calf Raise"; ex104.type = "WEIGHT"; ex104.tags = "Calves,Bodyweight"; long id104 = db.exerciseDao().insert(ex104);
        Exercise ex105 = new Exercise(); ex105.name = "Ab Wheel Rollout"; ex105.type = "WEIGHT"; ex105.tags = "Abs,Core,Bodyweight"; long id105 = db.exerciseDao().insert(ex105);
        Exercise ex106 = new Exercise(); ex106.name = "Hanging Leg Raise"; ex106.type = "WEIGHT"; ex106.tags = "Abs,Core,Bodyweight"; long id106 = db.exerciseDao().insert(ex106);
        Exercise ex107 = new Exercise(); ex107.name = "Dragon Flag"; ex107.type = "WEIGHT"; ex107.tags = "Abs,Core,Bodyweight"; long id107 = db.exerciseDao().insert(ex107);
        Exercise ex108 = new Exercise(); ex108.name = "Woodchoppers"; ex108.type = "WEIGHT"; ex108.tags = "Abs,Core,Cable"; long id108 = db.exerciseDao().insert(ex108);
        Exercise ex109 = new Exercise(); ex109.name = "Turkish Get-Up"; ex109.type = "WEIGHT"; ex109.tags = "FullBody,Core,Kettlebell"; long id109 = db.exerciseDao().insert(ex109);
        Exercise ex110 = new Exercise(); ex110.name = "Snatch"; ex110.type = "WEIGHT"; ex110.tags = "FullBody,Power,Barbell"; long id110 = db.exerciseDao().insert(ex110);
        Exercise ex111 = new Exercise(); ex111.name = "Clean and Jerk"; ex111.type = "WEIGHT"; ex111.tags = "FullBody,Power,Barbell"; long id111 = db.exerciseDao().insert(ex111);
        Exercise ex112 = new Exercise(); ex112.name = "Thrusters"; ex112.type = "WEIGHT"; ex112.tags = "Legs,Shoulders,FullBody,Barbell"; long id112 = db.exerciseDao().insert(ex112);
        Exercise ex113 = new Exercise(); ex113.name = "Sled Push"; ex113.type = "CARDIO"; ex113.tags = "Cardio,Legs,FullBody"; long id113 = db.exerciseDao().insert(ex113);
        Exercise ex114 = new Exercise(); ex114.name = "Sled Pull"; ex114.type = "CARDIO"; ex114.tags = "Cardio,Legs,Back"; long id114 = db.exerciseDao().insert(ex114);
        Exercise ex115 = new Exercise(); ex115.name = "Tire Flips"; ex115.type = "CARDIO"; ex115.tags = "Cardio,FullBody,Power"; long id115 = db.exerciseDao().insert(ex115);
        Exercise ex116 = new Exercise(); ex116.name = "Assault Bike"; ex116.type = "CARDIO"; ex116.tags = "Cardio,Legs,FullBody"; long id116 = db.exerciseDao().insert(ex116);
        Exercise ex117 = new Exercise(); ex117.name = "Versa Climber"; ex117.type = "CARDIO"; ex117.tags = "Cardio,FullBody"; long id117 = db.exerciseDao().insert(ex117);
        Exercise ex118 = new Exercise(); ex118.name = "Side Plank"; ex118.type = "CARDIO"; ex118.tags = "Abs,Core,Bodyweight"; long id118 = db.exerciseDao().insert(ex118);
        Exercise ex119 = new Exercise(); ex119.name = "Flutter Kicks"; ex119.type = "CARDIO"; ex119.tags = "Abs,Core,Bodyweight"; long id119 = db.exerciseDao().insert(ex119);
        Exercise ex120 = new Exercise(); ex120.name = "Jump Squats"; ex120.type = "CARDIO"; ex120.tags = "Legs,Quads,Cardio,Bodyweight"; long id120 = db.exerciseDao().insert(ex120);
        Exercise ex121 = new Exercise(); ex121.name = "Single-Leg Romanian Deadlift"; ex121.type = "WEIGHT"; ex121.tags = "Hamstrings,Glutes,Balance,Dumbbell"; long id121 = db.exerciseDao().insert(ex121);
        Exercise ex122 = new Exercise(); ex122.name = "Pistol Squat"; ex122.type = "WEIGHT"; ex122.tags = "Legs,Quads,Glutes,Bodyweight"; long id122 = db.exerciseDao().insert(ex122);
        Exercise ex123 = new Exercise(); ex123.name = "Reverse Hyperextensions"; ex123.type = "WEIGHT"; ex123.tags = "Glutes,Hamstrings,Machine"; long id123 = db.exerciseDao().insert(ex123);
        Exercise ex124 = new Exercise(); ex124.name = "Pull-Throughs"; ex124.type = "WEIGHT"; ex124.tags = "Glutes,Hamstrings,Cable"; long id124 = db.exerciseDao().insert(ex124);
        Exercise ex125 = new Exercise(); ex125.name = "Glute Kickbacks"; ex125.type = "WEIGHT"; ex125.tags = "Glutes,Cable"; long id125 = db.exerciseDao().insert(ex125);
        Exercise ex126 = new Exercise(); ex126.name = "Clamshells"; ex126.type = "WEIGHT"; ex126.tags = "Glutes,Bodyweight"; long id126 = db.exerciseDao().insert(ex126);
        Exercise ex127 = new Exercise(); ex127.name = "Fire Hydrants"; ex127.type = "WEIGHT"; ex127.tags = "Glutes,Bodyweight"; long id127 = db.exerciseDao().insert(ex127);
        Exercise ex128 = new Exercise(); ex128.name = "Seated Overhead Press"; ex128.type = "WEIGHT"; ex128.tags = "Shoulders,Triceps,Push,Barbell"; long id128 = db.exerciseDao().insert(ex128);
        Exercise ex129 = new Exercise(); ex129.name = "Behind-the-Neck Press"; ex129.type = "WEIGHT"; ex129.tags = "Shoulders,Triceps,Push,Barbell"; long id129 = db.exerciseDao().insert(ex129);
        Exercise ex130 = new Exercise(); ex130.name = "Plate Front Raise"; ex130.type = "WEIGHT"; ex130.tags = "Shoulders,Push,Plate"; long id130 = db.exerciseDao().insert(ex130);
        Exercise ex131 = new Exercise(); ex131.name = "Trap Bar Deadlift"; ex131.type = "WEIGHT"; ex131.tags = "Legs,Back,Glutes,TrapBar"; long id131 = db.exerciseDao().insert(ex131);
        Exercise ex132 = new Exercise(); ex132.name = "Zottman Curl"; ex132.type = "WEIGHT"; ex132.tags = "Biceps,Forearms,Pull,Dumbbell"; long id132 = db.exerciseDao().insert(ex132);
        Exercise ex133 = new Exercise(); ex133.name = "Spider Curl"; ex133.type = "WEIGHT"; ex133.tags = "Biceps,Arms,Pull,Dumbbell"; long id133 = db.exerciseDao().insert(ex133);
        Exercise ex134 = new Exercise(); ex134.name = "Incline Curl"; ex134.type = "WEIGHT"; ex134.tags = "Biceps,Arms,Pull,Dumbbell"; long id134 = db.exerciseDao().insert(ex134);
        Exercise ex135 = new Exercise(); ex135.name = "Bayesian Cable Curl"; ex135.type = "WEIGHT"; ex135.tags = "Biceps,Arms,Pull,Cable"; long id135 = db.exerciseDao().insert(ex135);
        Exercise ex136 = new Exercise(); ex136.name = "Overhead Cable Curl"; ex136.type = "WEIGHT"; ex136.tags = "Biceps,Arms,Pull,Cable"; long id136 = db.exerciseDao().insert(ex136);
        Exercise ex137 = new Exercise(); ex137.name = "Kickbacks"; ex137.type = "WEIGHT"; ex137.tags = "Triceps,Push,Arms,Dumbbell"; long id137 = db.exerciseDao().insert(ex137);
        Exercise ex138 = new Exercise(); ex138.name = "Tate Press"; ex138.type = "WEIGHT"; ex138.tags = "Triceps,Push,Dumbbell"; long id138 = db.exerciseDao().insert(ex138);
        Exercise ex139 = new Exercise(); ex139.name = "JM Press"; ex139.type = "WEIGHT"; ex139.tags = "Triceps,Push,Barbell"; long id139 = db.exerciseDao().insert(ex139);
        Exercise ex140 = new Exercise(); ex140.name = "Bench Dips"; ex140.type = "WEIGHT"; ex140.tags = "Triceps,Push,Bodyweight"; long id140 = db.exerciseDao().insert(ex140);
        Exercise ex141 = new Exercise(); ex141.name = "Weighted Dips"; ex141.type = "WEIGHT"; ex141.tags = "Chest,Triceps,Push,Bodyweight"; long id141 = db.exerciseDao().insert(ex141);
        Exercise ex142 = new Exercise(); ex142.name = "Weighted Pull Ups"; ex142.type = "WEIGHT"; ex142.tags = "Back,Biceps,Pull,Bodyweight"; long id142 = db.exerciseDao().insert(ex142);
        Exercise ex143 = new Exercise(); ex143.name = "Inverted Row"; ex143.type = "WEIGHT"; ex143.tags = "Back,Pull,Bodyweight"; long id143 = db.exerciseDao().insert(ex143);
        Exercise ex144 = new Exercise(); ex144.name = "Dead Hang"; ex144.type = "CARDIO"; ex144.tags = "Grip,Back,Bodyweight"; long id144 = db.exerciseDao().insert(ex144);
        Exercise ex145 = new Exercise(); ex145.name = "Farmer's Carry"; ex145.type = "WEIGHT"; ex145.tags = "FullBody,Grip,Dumbbell"; long id145 = db.exerciseDao().insert(ex145);
        Exercise ex146 = new Exercise(); ex146.name = "Suitcase Carry"; ex146.type = "WEIGHT"; ex146.tags = "Core,Grip,Obliques,Dumbbell"; long id146 = db.exerciseDao().insert(ex146);
        Exercise ex147 = new Exercise(); ex147.name = "Overhead Carry"; ex147.type = "WEIGHT"; ex147.tags = "Shoulders,Core,Dumbbell"; long id147 = db.exerciseDao().insert(ex147);
        Exercise ex148 = new Exercise(); ex148.name = "Bear Crawl"; ex148.type = "CARDIO"; ex148.tags = "Cardio,Core,FullBody,Bodyweight"; long id148 = db.exerciseDao().insert(ex148);
        Exercise ex149 = new Exercise(); ex149.name = "Crab Walk"; ex149.type = "CARDIO"; ex149.tags = "Cardio,Shoulders,Core,Bodyweight"; long id149 = db.exerciseDao().insert(ex149);
        Exercise ex150 = new Exercise(); ex150.name = "Inchworm"; ex150.type = "CARDIO"; ex150.tags = "Cardio,Core,Hamstrings,Bodyweight"; long id150 = db.exerciseDao().insert(ex150);

        WorkoutPlan plan1 = new WorkoutPlan(); plan1.name = "Push Day"; plan1.exerciseIds = id1 + "," + id2 + "," + id3; plan1.displayOrder = 1; db.workoutPlanDao().insert(plan1);
        WorkoutPlan plan2 = new WorkoutPlan(); plan2.name = "Pull Day"; plan2.exerciseIds = id4 + "," + id5 + "," + id6; plan2.displayOrder = 2; db.workoutPlanDao().insert(plan2);
        WorkoutPlan plan3 = new WorkoutPlan(); plan3.name = "Leg Day"; plan3.exerciseIds = id7 + "," + id8 + "," + id9; plan3.displayOrder = 3; db.workoutPlanDao().insert(plan3);
        WorkoutPlan plan4 = new WorkoutPlan(); plan4.name = "Cardio Day"; plan4.exerciseIds = id10 + "," + id11 + "," + id12; plan4.displayOrder = 4; db.workoutPlanDao().insert(plan4);
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
