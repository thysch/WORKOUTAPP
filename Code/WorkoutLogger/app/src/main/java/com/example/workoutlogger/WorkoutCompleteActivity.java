package com.example.workoutlogger;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class WorkoutCompleteActivity extends AppCompatActivity {

    public static final String EXTRA_WORKOUT_LOG_ID = "WORKOUT_LOG_ID";
    public static final String EXTRA_USER_NAME = "USER_NAME";

    private AppDatabase db;
    private long workoutLogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_complete);

        db = AppDatabase.getDatabase(getApplicationContext());
        workoutLogId = getIntent().getLongExtra(EXTRA_WORKOUT_LOG_ID, -1);
        String userName = getIntent().getStringExtra(EXTRA_USER_NAME);

        TextView userNameTextView = findViewById(R.id.user_name_text_view);
        userNameTextView.setText("Great job, " + userName + "!");

        Button doneButton = findViewById(R.id.done_button);
        doneButton.setOnClickListener(v -> finish());

        if (workoutLogId != -1) {
            loadWorkoutDetails();
        }

        KonfettiView konfettiView = findViewById(R.id.konfetti_view);
        EmitterConfig emitterConfig = new Emitter(1L, TimeUnit.SECONDS).perSecond(200);
        Party party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeedBetween(0f, 90f)
                .timeToLive(2000L)
                .sizes(new nl.dionsegijn.konfetti.core.models.Size(12, 1f, 2f))
                .position(new Position.Relative(0.5, 0.8))
                .build();

        konfettiView.start(party);
    }

    private void loadWorkoutDetails() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            WorkoutLog workoutLog = db.workoutLogDao().getWorkoutLogById(workoutLogId);
            List<Set> sets = db.setDao().getSetsForWorkoutLog(workoutLogId);

            Map<Long, List<Set>> setsByExerciseId = sets.stream().collect(Collectors.groupingBy(s -> s.exerciseId));
            List<Long> exerciseIds = new ArrayList<>(setsByExerciseId.keySet());
            List<Exercise> exercises = db.exerciseDao().getExercisesByIds(exerciseIds);

            runOnUiThread(() -> {
                if (workoutLog == null) return;

                TextView dateTextView = findViewById(R.id.date_text_view);
                TextView durationTextView = findViewById(R.id.duration_text_view);
                TextView volumeTextView = findViewById(R.id.volume_text_view);
                RecyclerView exercisesRecyclerView = findViewById(R.id.exercises_recycler_view);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy - h:mm a", Locale.getDefault());
                dateTextView.setText(dateFormat.format(new Date(workoutLog.date)));

                long minutes = TimeUnit.MILLISECONDS.toMinutes(workoutLog.duration);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(workoutLog.duration) % 60;
                durationTextView.setText(String.format(Locale.getDefault(), "Duration: %dm %ds", minutes, seconds));

                volumeTextView.setText(String.format(Locale.getDefault(), "Total Volume: %.1f lbs", workoutLog.totalVolume));

                exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                WorkoutDetailExerciseAdapter adapter = new WorkoutDetailExerciseAdapter(exercises, setsByExerciseId, true);
                exercisesRecyclerView.setAdapter(adapter);
            });
        });
    }
}
