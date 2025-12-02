package com.example.workoutlogger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.Set;
import com.example.workoutlogger.data.WorkoutLog;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.WorkoutHistoryViewHolder> {

    private final List<WorkoutLog> workoutLogs;
    private final List<Set> allSets;
    private final List<Exercise> allExercises;

    public WorkoutHistoryAdapter(List<WorkoutLog> workoutLogs, List<Set> allSets, List<Exercise> allExercises) {
        this.workoutLogs = workoutLogs;
        this.allSets = allSets;
        this.allExercises = allExercises;
    }

    @NonNull
    @Override
    public WorkoutHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_history_item, parent, false);
        return new WorkoutHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutHistoryViewHolder holder, int position) {
        holder.bind(workoutLogs.get(position), allSets, allExercises);
    }

    @Override
    public int getItemCount() {
        return workoutLogs.size();
    }

    static class WorkoutHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView workoutDateTextView;
        TextView workoutNameTextView;
        TextView workoutDurationTextView;
        TextView totalVolumeTextView;
        ImageView expandCollapseIndicator;
        RecyclerView exercisesRecyclerView;
        View workoutHistoryHeader;

        public WorkoutHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutDateTextView = itemView.findViewById(R.id.workout_date_text_view);
            workoutNameTextView = itemView.findViewById(R.id.workout_name_text_view);
            workoutDurationTextView = itemView.findViewById(R.id.workout_duration_text_view);
            totalVolumeTextView = itemView.findViewById(R.id.total_volume_text_view);
            expandCollapseIndicator = itemView.findViewById(R.id.expand_collapse_indicator);
            exercisesRecyclerView = itemView.findViewById(R.id.exercises_recycler_view);
            workoutHistoryHeader = itemView.findViewById(R.id.workout_history_header);
        }

        public void bind(WorkoutLog workoutLog, List<Set> allSets, List<Exercise> allExercises) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            workoutDateTextView.setText(dateFormat.format(new Date(workoutLog.date)));

            workoutNameTextView.setText(workoutLog.workoutName);

            long minutes = TimeUnit.MILLISECONDS.toMinutes(workoutLog.duration);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(workoutLog.duration) % 60;
            workoutDurationTextView.setText(String.format(Locale.getDefault(), "Duration: %dm %ds", minutes, seconds));

            totalVolumeTextView.setText(String.format(Locale.getDefault(), "Volume: %.1f lbs", workoutLog.totalVolume));

            workoutHistoryHeader.setOnClickListener(v -> {
                if (exercisesRecyclerView.getVisibility() == View.VISIBLE) {
                    exercisesRecyclerView.setVisibility(View.GONE);
                    expandCollapseIndicator.setImageResource(R.drawable.ic_arrow_right);
                } else {
                    exercisesRecyclerView.setVisibility(View.VISIBLE);
                    expandCollapseIndicator.setImageResource(R.drawable.ic_arrow_down);
                }
            });

            // Filter and group sets for the current workout log
            Map<Long, List<Set>> setsByExerciseId = allSets.stream()
                    .filter(set -> set.workoutLogId == workoutLog.uid)
                    .collect(Collectors.groupingBy(set -> set.exerciseId));

            // Get the list of exercise IDs for the current workout
            List<Long> exerciseIds = allSets.stream()
                    .filter(set -> set.workoutLogId == workoutLog.uid)
                    .map(set -> set.exerciseId)
                    .distinct()
                    .collect(Collectors.toList());

            // Filter the full list of exercises to get only the ones in this workout
            List<Exercise> exercisesForWorkout = allExercises.stream()
                    .filter(exercise -> exerciseIds.contains(exercise.uid))
                    .collect(Collectors.toList());

            // Set up the nested RecyclerView
            WorkoutDetailExerciseAdapter exerciseAdapter = new WorkoutDetailExerciseAdapter(exercisesForWorkout, setsByExerciseId);
            exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            exercisesRecyclerView.setAdapter(exerciseAdapter);
        }
    }
}
