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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectExerciseActivity extends AppCompatActivity implements ExerciseSelectionAdapter.OnItemClickListener {

    private RecyclerView exerciseSelectionList;
    private ExerciseSelectionAdapter adapter;
    private AppDatabase db;
    private SearchView searchView;
    private ChipGroup tagChipGroup;
    private List<Exercise> allExercises;

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
        tagChipGroup = findViewById(R.id.tag_chip_group);

        loadAllExercises();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterExercises();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterExercises();
                return false;
            }
        });

        for (int i = 0; i < tagChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) tagChipGroup.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> filterExercises());
        }
    }

    private void loadAllExercises() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            allExercises = db.exerciseDao().getAllExercises();
            runOnUiThread(() -> {
                adapter = new ExerciseSelectionAdapter(new ArrayList<>(allExercises), this);
                exerciseSelectionList.setAdapter(adapter);
            });
        });
    }

    private void filterExercises() {
        String query = searchView.getQuery().toString().toLowerCase();

        List<String> selectedTags = new ArrayList<>();
        for (int i = 0; i < tagChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) tagChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedTags.add(chip.getText().toString().toLowerCase());
            }
        }

        List<Exercise> filteredExercises = allExercises.stream()
                .filter(exercise -> {
                    boolean nameMatches = exercise.name.toLowerCase().contains(query);
                    boolean tagsMatch = true;
                    if (!selectedTags.isEmpty()) {
                        for (String tag : selectedTags) {
                            if (!exercise.tags.toLowerCase().contains(tag)) {
                                tagsMatch = false;
                                break;
                            }
                        }
                    }
                    return nameMatches && tagsMatch;
                })
                .collect(Collectors.toList());

        adapter.updateExercises(filteredExercises);
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
