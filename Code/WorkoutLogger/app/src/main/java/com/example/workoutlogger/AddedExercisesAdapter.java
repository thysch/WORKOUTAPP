package com.example.workoutlogger;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    private List<Exercise> exercises;
    private final OnDataChangedListener dataChangedListener;
    private Map<Long, List<WorkoutSet>> setsByExercise = new HashMap<>();

    public AddedExercisesAdapter(List<Exercise> exercises, OnDataChangedListener listener) {
        this.exercises = exercises;
        this.dataChangedListener = listener;
    }

    public void updateExercises(List<Exercise> newExercises) {
        this.exercises.clear();
        this.exercises.addAll(newExercises);
        notifyDataSetChanged();
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

    public void populateSets(List<WorkoutSet> allSets) {
        if (allSets == null) {
            return;
        }
        setsByExercise.clear();
        for (WorkoutSet set : allSets) {
            if (!setsByExercise.containsKey(set.exerciseId)) {
                setsByExercise.put(set.exerciseId, new ArrayList<>());
            }
            setsByExercise.get(set.exerciseId).add(set);
        }
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseNameTextView;
        Button addSetButton;
        ImageButton deleteExerciseButton;
        RecyclerView setsRecyclerView;
        SetAdapter setAdapter;
        ImageView expandCollapseIndicator;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseNameTextView = itemView.findViewById(R.id.exercise_name_text_view);
            addSetButton = itemView.findViewById(R.id.add_set_button);
            deleteExerciseButton = itemView.findViewById(R.id.delete_exercise_button);
            setsRecyclerView = itemView.findViewById(R.id.sets_recycler_view);
            expandCollapseIndicator = itemView.findViewById(R.id.expand_collapse_indicator);
        }

        public void bind(final Exercise exercise) {
            exerciseNameTextView.setText(exercise.name);

            long exerciseId = exercise.uid;
            if (!setsByExercise.containsKey(exerciseId)) {
                setsByExercise.put(exerciseId, new ArrayList<>());
            }

            List<WorkoutSet> sets = setsByExercise.get(exerciseId);
            setAdapter = new SetAdapter(sets, exercise, dataChangedListener);
            setsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            setsRecyclerView.setAdapter(setAdapter);

            addSetButton.setOnClickListener(v -> {
                int setNumber = sets.size() + 1;
                WorkoutSet newSet = new WorkoutSet();
                newSet.exerciseId = exerciseId;
                newSet.setNumber = setNumber;
                sets.add(newSet);
                setAdapter.notifyItemInserted(sets.size() - 1);
                if (dataChangedListener != null) {
                    dataChangedListener.onDataChanged();
                }
            });

            deleteExerciseButton.setOnClickListener(v -> {
                new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Delete Exercise")
                        .setMessage("Are you sure you want to delete this exercise and all its sets?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                exercises.remove(position);
                                setsByExercise.remove(exercise.uid);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, exercises.size());
                                if (dataChangedListener != null) {
                                    dataChangedListener.onDataChanged();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            View.OnClickListener expandCollapseListener = v -> {
                if (setsRecyclerView.getVisibility() == View.VISIBLE) {
                    setsRecyclerView.setVisibility(View.GONE);
                    addSetButton.setVisibility(View.GONE);
                    expandCollapseIndicator.setImageResource(R.drawable.ic_arrow_right);
                } else {
                    setsRecyclerView.setVisibility(View.VISIBLE);
                    addSetButton.setVisibility(View.VISIBLE);
                    expandCollapseIndicator.setImageResource(R.drawable.ic_arrow_down);
                }
            };
            exerciseNameTextView.setOnClickListener(expandCollapseListener);
            expandCollapseIndicator.setOnClickListener(expandCollapseListener);
        }
    }
}
