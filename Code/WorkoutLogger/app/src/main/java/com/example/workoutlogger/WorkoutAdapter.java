package com.example.workoutlogger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.WorkoutPlan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<WorkoutPlan> workoutPlans;
    private List<Exercise> allExercises;

    public WorkoutAdapter(List<WorkoutPlan> workoutPlans, List<Exercise> allExercises) {
        this.workoutPlans = workoutPlans;
        this.allExercises = allExercises;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_item, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WorkoutPlan workoutPlan = workoutPlans.get(position);
        holder.workoutName.setText(workoutPlan.name);

        if (workoutPlan.exerciseIds != null && !workoutPlan.exerciseIds.isEmpty()) {
            List<String> exerciseIds = Arrays.asList(workoutPlan.exerciseIds.split(","));
            List<String> exerciseNames = new ArrayList<>();
            for (String idStr : exerciseIds) {
                Exercise exercise = findExerciseById(Long.parseLong(idStr.trim()));
                if (exercise != null) {
                    exerciseNames.add(exercise.name);
                }
            }
            holder.exerciseList.setText(String.join(", ", exerciseNames));
        } else {
            holder.exerciseList.setText("");
        }

        holder.optionsButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.workout_options_menu, popup.getMenu());
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return workoutPlans.size();
    }

    private Exercise findExerciseById(long id) {
        for (Exercise exercise : allExercises) {
            if (exercise.id == id) {
                return exercise;
            }
        }
        return null;
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView workoutName;
        TextView exerciseList;
        Button startWorkout;
        ImageButton optionsButton;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            workoutName = itemView.findViewById(R.id.workout_name);
            exerciseList = itemView.findViewById(R.id.exercise_list);
            startWorkout = itemView.findViewById(R.id.start_workout);
            optionsButton = itemView.findViewById(R.id.options_button);
        }
    }
}