package com.example.workoutlogger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutlogger.data.Set;

import java.util.List;
import java.util.Locale;

public class WorkoutDetailSetAdapter extends RecyclerView.Adapter<WorkoutDetailSetAdapter.SetViewHolder> {

    private final List<Set> sets;

    public WorkoutDetailSetAdapter(List<Set> sets) {
        this.sets = sets;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_detail_set_item, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        holder.bind(sets.get(position));
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    static class SetViewHolder extends RecyclerView.ViewHolder {
        private final TextView setNumberTextView;
        private final TextView setDetailsTextView;

        public SetViewHolder(@NonNull View itemView) {
            super(itemView);
            setNumberTextView = itemView.findViewById(R.id.set_number_text_view);
            setDetailsTextView = itemView.findViewById(R.id.set_details_text_view);
        }

        public void bind(Set set) {
            setNumberTextView.setText(String.format(Locale.getDefault(), "Set %d", set.setNumber));
            setDetailsTextView.setText(String.format(Locale.getDefault(), "%d reps x %.1f lbs", set.reps, set.weight));
        }
    }
}
