package com.example.workoutlogger;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.Set;
import com.example.workoutlogger.data.WorkoutLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WorkoutDetailActivity extends AppCompatActivity {

    public static final String EXTRA_WORKOUT_LOG_ID = "WORKOUT_LOG_ID";

    private AppDatabase db;
    private long workoutLogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);

        db = AppDatabase.getDatabase(getApplicationContext());
        workoutLogId = getIntent().getLongExtra(EXTRA_WORKOUT_LOG_ID, -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadWorkoutDetails();
    }

    private void loadWorkoutDetails() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            WorkoutLog workoutLog = db.workoutLogDao().getWorkoutLogById(workoutLogId);
            List<Set> sets = db.setDao().getSetsForWorkoutLog(workoutLogId);

            // Group sets by exerciseId and get a list of unique exercise IDs
            Map<Long, List<Set>> setsByExerciseId = sets.stream().collect(Collectors.groupingBy(s -> s.exerciseId));
            List<Long> exerciseIds = new ArrayList<>(setsByExerciseId.keySet());

            // Fetch the exercise details for the IDs
            List<Exercise> exercises = db.exerciseDao().getExercisesByIds(exerciseIds);

            runOnUiThread(() -> {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(workoutLog.workoutName);
                }

                TextView dateTextView = findViewById(R.id.workout_date_text_view);
                TextView volumeTextView = findViewById(R.id.total_volume_text_view);
                TextView durationTextView = findViewById(R.id.workout_duration_text_view);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                dateTextView.setText(dateFormat.format(new Date(workoutLog.date)));

                long minutes = TimeUnit.MILLISECONDS.toMinutes(workoutLog.duration);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(workoutLog.duration) % 60;
                durationTextView.setText(String.format(Locale.getDefault(), "Duration: %dm %ds", minutes, seconds));

                volumeTextView.setText(String.format(Locale.getDefault(), "Total Volume: %.1f lbs", workoutLog.totalVolume));

                RecyclerView recyclerView = findViewById(R.id.exercises_recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                WorkoutDetailExerciseAdapter adapter = new WorkoutDetailExerciseAdapter(exercises, setsByExerciseId);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
