package com.example.workoutlogger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutlogger.data.AppDatabase;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.WorkoutPlan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<WorkoutPlan> workoutPlans;
    private List<Exercise> allExercises;
    private Context context;

    // Constructor
    public WorkoutAdapter(List<WorkoutPlan> workoutPlans, List<Exercise> allExercises) {
        this.workoutPlans = workoutPlans;
        this.allExercises = allExercises;
    }

    // Inflates the layout for the workout item
    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.workout_item, parent, false);
        return new WorkoutViewHolder(view);
    }

    // Sets the data for the workout item
    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        WorkoutPlan workoutPlan = workoutPlans.get(position);
        holder.workoutName.setText(workoutPlan.name);

        if (workoutPlan.exerciseIds != null && !workoutPlan.exerciseIds.isEmpty()) {
            List<String> exerciseIds = Arrays.asList(workoutPlan.exerciseIds.split(","));
            List<String> exerciseNames = new ArrayList<>();
            for (String idStr : exerciseIds) {
                try {
                    Exercise exercise = findExerciseById(Long.parseLong(idStr.trim()));
                    if (exercise != null) {
                        exerciseNames.add(exercise.name);
                    }
                } catch (NumberFormatException e) {
                    // Ignore if an id is malformed
                }
            }
            holder.exerciseList.setText(String.join(", ", exerciseNames));
        } else {
            holder.exerciseList.setText("");
        }


        // Starts the workout
        holder.startWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(context, StartWorkoutActivity.class);
            intent.putExtra("WORKOUT_PLAN_ID", workoutPlan.uid);
            context.startActivity(intent);
        });

        // Opens the little window for "Edit"/"Delete"
        holder.optionsButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.workout_options_menu, popup.getMenu());

            // Sets the click listener for the menu items
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.delete_workout) {
                    // Deletes the workout
                    deleteWorkout(workoutPlan, holder.getAdapterPosition());
                    return true;
                } else if (itemId == R.id.edit_workout) {
                    // Opens the edit workout activity
                    editWorkout(workoutPlan);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }


    private void editWorkout(WorkoutPlan workoutPlan) {
        Intent intent = new Intent(context, AddWorkoutActivity.class);
        intent.putExtra("WORKOUT_PLAN_ID", workoutPlan.uid);
        context.startActivity(intent);
    }


    private void deleteWorkout(WorkoutPlan workoutPlan, int position) {
        AppDatabase db = AppDatabase.getDatabase(context.getApplicationContext());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.workoutPlanDao().delete(workoutPlan);
            ((Activity) context).runOnUiThread(() -> {
                workoutPlans.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, workoutPlans.size());
                Toast.makeText(context, "Workout Deleted", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public int getItemCount() {
        return workoutPlans.size();
    }


    private Exercise findExerciseById(long id) {
        for (Exercise exercise : allExercises) {
            if (exercise.uid == id) {
                return exercise;
            }
        }
        return null;
    }

    // Updates the data for the adapter
    public void updateData(List<WorkoutPlan> newWorkoutPlans, List<Exercise> newAllExercises) {
        this.workoutPlans = newWorkoutPlans;
        this.allExercises = newAllExercises;
        notifyDataSetChanged();
    }


    // ViewHolder for the workout item
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
