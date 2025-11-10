package com.example.workoutlogger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.WorkoutPlan;
import com.example.workoutlogger.data.WorkoutSet;
import com.example.workoutlogger.data.WorkoutPlanDao;
import com.example.workoutlogger.data.WorkoutSetDao;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AddWorkoutActivity extends AppCompatActivity {

    private static final int SELECT_EXERCISE_REQUEST = 1;
    private RecyclerView addedExercisesList;
    private AddedExercisesAdapter adapter;
    private List<Exercise> addedExercises = new ArrayList<>();
    private AppDatabase db;
    private EditText workoutNameEditText;
    private long existingWorkoutPlanId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        db = AppDatabase.getDatabase(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        workoutNameEditText = findViewById(R.id.workout_name_edit_text);

        addedExercisesList = findViewById(R.id.added_exercises_list);
        addedExercisesList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AddedExercisesAdapter(addedExercises);
        addedExercisesList.setAdapter(adapter);

        Button addExerciseButton = findViewById(R.id.add_exercise_button);
        addExerciseButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddWorkoutActivity.this, SelectExerciseActivity.class);
            startActivityForResult(intent, SELECT_EXERCISE_REQUEST);
        });

        if (getIntent().hasExtra("WORKOUT_PLAN_ID")) {
            existingWorkoutPlanId = getIntent().getLongExtra("WORKOUT_PLAN_ID", -1);
            getSupportActionBar().setTitle("Edit Workout");
            loadExistingWorkout();
        } else {
            getSupportActionBar().setTitle("Add Workout");
        }
    }

    private void loadExistingWorkout() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            WorkoutPlan workoutPlan = db.workoutPlanDao().getWorkoutPlanById(existingWorkoutPlanId);
            if (workoutPlan != null) {
                List<WorkoutSet> sets = db.workoutSetDao().getSetsForWorkoutPlan(existingWorkoutPlanId);
                List<Long> exerciseIds = Arrays.stream(workoutPlan.exerciseIds.split(","))
                                               .map(String::trim)
                                               .map(Long::parseLong)
                                               .collect(Collectors.toList());

                List<Exercise> exercises = new ArrayList<>();
                for (Long id : exerciseIds) {
                    Exercise exercise = db.exerciseDao().getExerciseById(id);
                    if (exercise != null) {
                        exercises.add(exercise);
                    }
                }

                runOnUiThread(() -> {
                    workoutNameEditText.setText(workoutPlan.name);
                    addedExercises.addAll(exercises);
                    adapter.notifyDataSetChanged();
                    // This is a simplified way to re-populate sets; a more robust
                    // solution would involve a more direct way of associating sets to exercises
                    // in the adapter after they have been loaded.
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_EXERCISE_REQUEST && resultCode == RESULT_OK && data != null) {
            long exerciseId = data.getLongExtra("exerciseId", -1);
            if (exerciseId != -1) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    Exercise exercise = db.exerciseDao().getExerciseById(exerciseId);
                    if (exercise != null) {
                        runOnUiThread(() -> {
                            addedExercises.add(exercise);
                            adapter.notifyDataSetChanged();
                        });
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_workout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.discard_workout) {
            finish();
            return true;
        } else if (itemId == R.id.save_workout) {
            saveWorkout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveWorkout() {
        String workoutName = workoutNameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(workoutName)) {
            Toast.makeText(this, "Please enter a workout name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (addedExercises.isEmpty()) {
            Toast.makeText(this, "Please add at least one exercise.", Toast.LENGTH_SHORT).show();
            return;
        }

        String exerciseIds = addedExercises.stream().map(exercise -> String.valueOf(exercise.uid)).collect(Collectors.joining(","));

        WorkoutPlan workoutPlan = new WorkoutPlan();
        workoutPlan.name = workoutName;
        workoutPlan.exerciseIds = exerciseIds;
        if (existingWorkoutPlanId != -1) {
            workoutPlan.uid = existingWorkoutPlanId;
        }

        Map<Long, List<WorkoutSet>> setsByExercise = adapter.getSetsByExercise();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (existingWorkoutPlanId == -1) {
                long planId = db.workoutPlanDao().insert(workoutPlan);
                saveSets(planId, setsByExercise);
            } else {
                db.workoutPlanDao().update(workoutPlan);
                // For simplicity, we delete and re-insert sets. A more advanced implementation
                // would perform a more granular update.
                db.workoutSetDao().deleteSetsForWorkoutPlan(existingWorkoutPlanId);
                saveSets(existingWorkoutPlanId, setsByExercise);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Workout saved!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void saveSets(long planId, Map<Long, List<WorkoutSet>> setsByExercise) {
        List<WorkoutSet> allSets = new ArrayList<>();
        for (List<WorkoutSet> sets : setsByExercise.values()) {
            for (WorkoutSet set : sets) {
                set.workoutPlanId = planId;
                allSets.add(set);
            }
        }
        if (!allSets.isEmpty()) {
            db.workoutSetDao().insertAll(allSets.toArray(new WorkoutSet[0]));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
