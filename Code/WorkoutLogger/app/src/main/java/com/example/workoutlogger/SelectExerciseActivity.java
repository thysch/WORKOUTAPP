package com.example.workoutlogger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.Exercise;
import java.util.List;

public class SelectExerciseActivity extends AppCompatActivity implements ExerciseSelectionAdapter.OnItemClickListener {

    private RecyclerView exerciseSelectionList;
    private ExerciseSelectionAdapter adapter;
    private AppDatabase db;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_exercise);

        db = AppDatabase.getDatabase(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Select Exercise");

        exerciseSelectionList = findViewById(R.id.exercise_selection_list);
        exerciseSelectionList.setLayoutManager(new LinearLayoutManager(this));

        searchView = findViewById(R.id.exercise_search_view);

        loadExercises();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    private void loadExercises() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Exercise> exercises = db.exerciseDao().getAllExercises();
            runOnUiThread(() -> {
                adapter = new ExerciseSelectionAdapter(exercises, this);
                exerciseSelectionList.setAdapter(adapter);
            });
        });
    }

    @Override
    public void onItemClick(Exercise exercise) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("exerciseId", exercise.uid);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
