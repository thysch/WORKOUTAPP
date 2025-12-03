package com.example.workoutlogger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    private int mCurrentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int colorRes = sharedPreferences.getInt("selected_theme_color", R.color.colorPrimary);
        mCurrentTheme = getThemeResId(colorRes);
        setTheme(mCurrentTheme);

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
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int colorRes = sharedPreferences.getInt("selected_theme_color", R.color.colorPrimary);
        if (mCurrentTheme != getThemeResId(colorRes)) {
            recreate();
        }
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
