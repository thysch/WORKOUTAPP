package com.example.workoutlogger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.WorkoutLog;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.WorkoutHistoryViewHolder> {

    private List<WorkoutLog> workoutLogs;

    public WorkoutHistoryAdapter(List<WorkoutLog> workoutLogs) {
        this.workoutLogs = workoutLogs;
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

    static class WorkoutHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView workoutDateTextView;
        TextView workoutNameTextView;
        TextView workoutDurationTextView;
        TextView totalVolumeTextView;

        public WorkoutHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutDateTextView = itemView.findViewById(R.id.workout_date_text_view);
            workoutNameTextView = itemView.findViewById(R.id.workout_name_text_view);
            workoutDurationTextView = itemView.findViewById(R.id.workout_duration_text_view);
            totalVolumeTextView = itemView.findViewById(R.id.total_volume_text_view);
        }

        public void bind(WorkoutLog workoutLog) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            workoutDateTextView.setText(dateFormat.format(new Date(workoutLog.date)));

            workoutNameTextView.setText(workoutLog.workoutName);

            long minutes = TimeUnit.MILLISECONDS.toMinutes(workoutLog.duration);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(workoutLog.duration) % 60;
            workoutDurationTextView.setText(String.format(Locale.getDefault(), "Duration: %dm %ds", minutes, seconds));

            totalVolumeTextView.setText(String.format(Locale.getDefault(), "Volume: %.1f lbs", workoutLog.totalVolume));
        }
    }
}
