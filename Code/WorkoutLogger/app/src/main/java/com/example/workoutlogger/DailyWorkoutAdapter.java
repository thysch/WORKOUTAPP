package com.example.workoutlogger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.WorkoutLog;
import java.util.List;

public class DailyWorkoutAdapter extends RecyclerView.Adapter<DailyWorkoutAdapter.WorkoutViewHolder> {

    private List<WorkoutLog> workoutLogs;

    public DailyWorkoutAdapter(List<WorkoutLog> workoutLogs) {
        this.workoutLogs = workoutLogs;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_workout_item, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WorkoutLog workoutLog = workoutLogs.get(position);
        holder.workoutNameTextView.setText(workoutLog.workoutName);
    }

    @Override
    public int getItemCount() {
        return workoutLogs.size();
    }

    public void updateData(List<WorkoutLog> newWorkoutLogs) {
        this.workoutLogs = newWorkoutLogs;
        notifyDataSetChanged();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView workoutNameTextView;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutNameTextView = itemView.findViewById(R.id.workout_name_text_view);
        }
    }
}
