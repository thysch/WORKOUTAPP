package com.example.workoutlogger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.Set;

import java.util.List;
import java.util.Locale;

public class WorkoutDetailSetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_WEIGHT = 1;
    private static final int VIEW_TYPE_CARDIO = 2;

    private final List<Set> sets;
    private final Exercise exercise;

    public WorkoutDetailSetAdapter(List<Set> sets, Exercise exercise) {
        this.sets = sets;
        this.exercise = exercise;
    }

    @Override
    public int getItemViewType(int position) {
        if ("CARDIO".equals(exercise.type)) {
            return VIEW_TYPE_CARDIO;
        }
        return VIEW_TYPE_WEIGHT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CARDIO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_detail_cardio_set_item, parent, false);
            return new CardioSetViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_detail_set_item, parent, false);
        return new WeightSetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_CARDIO) {
            ((CardioSetViewHolder) holder).bind(sets.get(position));
        } else {
            ((WeightSetViewHolder) holder).bind(sets.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    static class WeightSetViewHolder extends RecyclerView.ViewHolder {
        private final TextView setNumberTextView;
        private final TextView setDetailsTextView;

        public WeightSetViewHolder(@NonNull View itemView) {
            super(itemView);
            setNumberTextView = itemView.findViewById(R.id.set_number_text_view);
            setDetailsTextView = itemView.findViewById(R.id.set_details_text_view);
        }

        public void bind(Set set) {
            setNumberTextView.setText(String.format(Locale.getDefault(), "Set %d", set.setNumber));
            setDetailsTextView.setText(String.format(Locale.getDefault(), "%d reps x %.1f lbs", set.reps, set.weight));
        }
    }

    static class CardioSetViewHolder extends RecyclerView.ViewHolder {
        private final TextView setNumberTextView;
        private final TextView setDetailsTextView;

        public CardioSetViewHolder(@NonNull View itemView) {
            super(itemView);
            setNumberTextView = itemView.findViewById(R.id.set_number_text_view);
            setDetailsTextView = itemView.findViewById(R.id.set_details_text_view);
        }

        public void bind(Set set) {
            setNumberTextView.setText(String.format(Locale.getDefault(), "Set %d", set.setNumber));
            setDetailsTextView.setText(String.format(Locale.getDefault(), "%.1f min - %.1f miles", set.duration, set.distance));
        }
    }
}
