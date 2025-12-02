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

import java.util.List;
import java.util.Map;

public class WorkoutDetailExerciseAdapter extends RecyclerView.Adapter<WorkoutDetailExerciseAdapter.ExerciseViewHolder> {

    private final List<Exercise> exercises;
    private final Map<Long, List<Set>> setsByExerciseId;
    private final boolean isExpandedByDefault;

    public WorkoutDetailExerciseAdapter(List<Exercise> exercises, Map<Long, List<Set>> setsByExerciseId) {
        this(exercises, setsByExerciseId, false);
    }

    public WorkoutDetailExerciseAdapter(List<Exercise> exercises, Map<Long, List<Set>> setsByExerciseId, boolean isExpandedByDefault) {
        this.exercises = exercises;
        this.setsByExerciseId = setsByExerciseId;
        this.isExpandedByDefault = isExpandedByDefault;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_detail_exercise_item, parent, false);
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

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private final TextView exerciseNameTextView;
        private final RecyclerView setsRecyclerView;
        private final ImageView expandCollapseIndicator;
        private final View exerciseHeader;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseNameTextView = itemView.findViewById(R.id.exercise_name_text_view);
            setsRecyclerView = itemView.findViewById(R.id.sets_recycler_view);
            expandCollapseIndicator = itemView.findViewById(R.id.expand_collapse_indicator);
            exerciseHeader = itemView.findViewById(R.id.exercise_header);
        }

        public void bind(Exercise exercise) {
            exerciseNameTextView.setText(exercise.name);

            List<Set> sets = setsByExerciseId.get(exercise.uid);
            if (sets != null && !sets.isEmpty()) {
                WorkoutDetailSetAdapter setAdapter = new WorkoutDetailSetAdapter(sets, exercise);
                setsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                setsRecyclerView.setAdapter(setAdapter);
            }

            if (isExpandedByDefault) {
                setsRecyclerView.setVisibility(View.VISIBLE);
                expandCollapseIndicator.setImageResource(R.drawable.ic_arrow_down);
            } else {
                setsRecyclerView.setVisibility(View.GONE);
                expandCollapseIndicator.setImageResource(R.drawable.ic_arrow_right);
            }

            View.OnClickListener expandCollapseListener = v -> {
                if (setsRecyclerView.getVisibility() == View.VISIBLE) {
                    setsRecyclerView.setVisibility(View.GONE);
                    expandCollapseIndicator.setImageResource(R.drawable.ic_arrow_right);
                } else {
                    setsRecyclerView.setVisibility(View.VISIBLE);
                    expandCollapseIndicator.setImageResource(R.drawable.ic_arrow_down);
                }
            };
            exerciseHeader.setOnClickListener(expandCollapseListener);
        }
    }
}
