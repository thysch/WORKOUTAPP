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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.WorkoutHistoryViewHolder> {

    private final List<WorkoutLog> workoutLogs;
    private final Map<Long, List<Set>> setsByWorkoutLogId;
    private final Map<Long, Exercise> exerciseById;
    private final String weightUnit;
    private final String distanceUnit;

    public WorkoutHistoryAdapter(List<WorkoutLog> workoutLogs, Map<Long, List<Set>> setsByWorkoutLogId, Map<Long, Exercise> exerciseById, String weightUnit, String distanceUnit) {
        this.workoutLogs = workoutLogs;
        this.setsByWorkoutLogId = setsByWorkoutLogId;
        this.exerciseById = exerciseById;
        this.weightUnit = weightUnit;
        this.distanceUnit = distanceUnit;
    }

    @NonNull
    @Override
    public WorkoutHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_history_item, parent, false);
        return new WorkoutHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutHistoryViewHolder holder, int position) {
        holder.bind(workoutLogs.get(position));
    }

    @Override
    public int getItemCount() {
        return workoutLogs.size();
    }

    class WorkoutHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView workoutNameTextView, dateTextView, durationTextView, volumeTextView;
        RecyclerView exercisesRecyclerView;
        ImageView expandCollapseIndicator;

        public WorkoutHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutNameTextView = itemView.findViewById(R.id.workout_name_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            durationTextView = itemView.findViewById(R.id.duration_text_view);
            volumeTextView = itemView.findViewById(R.id.volume_text_view);
            exercisesRecyclerView = itemView.findViewById(R.id.exercises_recycler_view);
            expandCollapseIndicator = itemView.findViewById(R.id.expand_collapse_indicator);
        }

        public void bind(WorkoutLog workoutLog) {
            workoutNameTextView.setText(workoutLog.workoutName);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy - h:mm a", Locale.getDefault());
            dateTextView.setText(dateFormat.format(new Date(workoutLog.date)));

            long minutes = TimeUnit.MILLISECONDS.toMinutes(workoutLog.duration);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(workoutLog.duration) % 60;
            durationTextView.setText(String.format(Locale.getDefault(), "Duration: %dm %ds", minutes, seconds));

            float totalVolume = workoutLog.totalVolume;
            if ("kg".equals(weightUnit)) {
                totalVolume *= 0.453592f;
            }
            volumeTextView.setText(String.format(Locale.getDefault(), "Total Volume: %.1f %s", totalVolume, weightUnit));

            List<Set> setsForWorkout = setsByWorkoutLogId.get(workoutLog.uid);
            if (setsForWorkout != null) {
                Map<Long, List<Set>> setsByExerciseId = setsForWorkout.stream().collect(Collectors.groupingBy(s -> s.exerciseId));
                List<Exercise> exercisesInWorkout = new ArrayList<>();
                for (Long exerciseId : setsByExerciseId.keySet()) {
                    if (exerciseById.containsKey(exerciseId)) {
                        exercisesInWorkout.add(exerciseById.get(exerciseId));
                    }
                }

                WorkoutDetailExerciseAdapter exerciseAdapter = new WorkoutDetailExerciseAdapter(exercisesInWorkout, setsByExerciseId, false, weightUnit, distanceUnit);
                exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                exercisesRecyclerView.setAdapter(exerciseAdapter);
            }

            View.OnClickListener expandCollapseListener = v -> {
                if (exercisesRecyclerView.getVisibility() == View.VISIBLE) {
                    exercisesRecyclerView.setVisibility(View.GONE);
                    expandCollapseIndicator.setImageResource(R.drawable.ic_arrow_right);
                } else {
                    exercisesRecyclerView.setVisibility(View.VISIBLE);
                    expandCollapseIndicator.setImageResource(R.drawable.ic_arrow_down);
                }
            };

            workoutNameTextView.setOnClickListener(expandCollapseListener);
            expandCollapseIndicator.setOnClickListener(expandCollapseListener);
        }
    }
}
