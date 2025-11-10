package com.example.workoutlogger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.WorkoutSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddedExercisesAdapter extends RecyclerView.Adapter<AddedExercisesAdapter.ExerciseViewHolder> {

    private List<Exercise> exercises;
    private Map<Long, List<WorkoutSet>> setsByExercise = new HashMap<>();

    public AddedExercisesAdapter(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.added_exercise_item, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        holder.bind(exercises.get(position));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public Map<Long, List<WorkoutSet>> getSetsByExercise() {
        return setsByExercise;
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseNameTextView;
        Button addSetButton;
        ImageButton minimizeButton;
        RecyclerView setsRecyclerView;
        SetAdapter setAdapter;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseNameTextView = itemView.findViewById(R.id.exercise_name_text_view);
            addSetButton = itemView.findViewById(R.id.add_set_button);
            minimizeButton = itemView.findViewById(R.id.minimize_button);
            setsRecyclerView = itemView.findViewById(R.id.sets_recycler_view);
        }

        public void bind(final Exercise exercise) {
            exerciseNameTextView.setText(exercise.name);

            long exerciseId = exercise.uid;
            if (!setsByExercise.containsKey(exerciseId)) {
                setsByExercise.put(exerciseId, new ArrayList<>());
            }

            List<WorkoutSet> sets = setsByExercise.get(exerciseId);
            setAdapter = new SetAdapter(sets);
            setsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            setsRecyclerView.setAdapter(setAdapter);

            addSetButton.setOnClickListener(v -> {
                int setNumber = sets.size() + 1;
                WorkoutSet newSet = new WorkoutSet();
                newSet.exerciseId = exerciseId;
                newSet.setNumber = setNumber;
                sets.add(newSet);
                setAdapter.notifyItemInserted(sets.size() - 1);
            });

            minimizeButton.setOnClickListener(v -> {
                if (setsRecyclerView.getVisibility() == View.VISIBLE) {
                    setsRecyclerView.setVisibility(View.GONE);
                } else {
                    setsRecyclerView.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
