package com.example.workoutlogger;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.WorkoutPlan;
import com.example.workoutlogger.data.WorkoutSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private boolean isWorkoutEdited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        db = AppDatabase.getDatabase(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        workoutNameEditText = findViewById(R.id.workout_name_edit_text);

        // Listen for changes in the workout name
        workoutNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isWorkoutEdited = true; // Mark as edited
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        addedExercisesList = findViewById(R.id.added_exercises_list);
        addedExercisesList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AddedExercisesAdapter(addedExercises, () -> isWorkoutEdited = true); // Pass a lambda as the listener
        addedExercisesList.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                Collections.swap(addedExercises, fromPosition, toPosition);
                adapter.notifyItemMoved(fromPosition, toPosition);
                isWorkoutEdited = true;
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Swiping is not enabled
            }
        });
        itemTouchHelper.attachToRecyclerView(addedExercisesList);

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
                List<Exercise> exercises = new ArrayList<>();
                if (workoutPlan.exerciseIds != null && !workoutPlan.exerciseIds.isEmpty()) {
                    List<Long> exerciseIds = Arrays.stream(workoutPlan.exerciseIds.split(","))
                                                   .map(String::trim)
                                                   .map(Long::parseLong)
                                                   .collect(Collectors.toList());

                    for (Long id : exerciseIds) {
                        Exercise exercise = db.exerciseDao().getExerciseById(id);
                        if (exercise != null) {
                            exercises.add(exercise);
                        }
                    }
                }

                List<WorkoutSet> sets = db.workoutSetDao().getSetsForWorkoutPlan(existingWorkoutPlanId);

                runOnUiThread(() -> {
                    workoutNameEditText.setText(workoutPlan.name);
                    adapter.updateExercises(exercises);
                    adapter.populateSets(sets);
                    // Post a runnable to the view's message queue to run after the layout pass
                    addedExercisesList.post(() -> {
                        isWorkoutEdited = false;
                    });
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
                            isWorkoutEdited = true;
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
        for (Map.Entry<Long, List<WorkoutSet>> entry : setsByExercise.entrySet()) {
            long exerciseId = entry.getKey();
            for (WorkoutSet set : entry.getValue()) {
                set.workoutPlanId = planId;
                set.exerciseId = exerciseId;
                allSets.add(set);
            }
        }
        if (!allSets.isEmpty()) {
            db.workoutSetDao().insertAll(allSets.toArray(new WorkoutSet[0]));
        }
    }

    @Override
    public void onBackPressed() {
        if (isWorkoutEdited) {
            // If changes were made, show the confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Save this workout?")
                    .setPositiveButton("Save", (dialog, which) -> saveWorkout())
                    .setNegativeButton("Discard Changes", (dialog, which) -> finish())
                    .setNeutralButton("Cancel", null)
                    .show();
        } else {
            // If no changes were made, just finish the activity
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
