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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddWorkoutActivity extends AppCompatActivity {

    private static final int SELECT_EXERCISE_REQUEST = 1;
    private RecyclerView addedExercisesList;
    private AddedExercisesAdapter adapter;
    private List<Exercise> addedExercises = new ArrayList<>();
    private AppDatabase db;
    private EditText workoutNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        db = AppDatabase.getDatabase(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add Workout");

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

        String exerciseIds = addedExercises.stream().map(exercise -> String.valueOf(exercise.id)).collect(Collectors.joining(","));

        WorkoutPlan newWorkoutPlan = new WorkoutPlan();
        newWorkoutPlan.name = workoutName;
        newWorkoutPlan.exerciseIds = exerciseIds;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.workoutPlanDao().insert(newWorkoutPlan);
            runOnUiThread(() -> {
                Toast.makeText(this, "Workout saved!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
