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

public class WorkoutDetailSetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_WEIGHT = 1;
    private static final int VIEW_TYPE_CARDIO = 2;
    private final List<Set> sets;
    private final String exerciseType;
    private final String weightUnit;
    private final String distanceUnit;

    public WorkoutDetailSetAdapter(List<Set> sets, String exerciseType, String weightUnit, String distanceUnit) {
        this.sets = sets;
        this.exerciseType = exerciseType;
        this.weightUnit = weightUnit;
        this.distanceUnit = distanceUnit;
    }

    @Override
    public int getItemViewType(int position) {
        if ("CARDIO".equals(exerciseType)) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_detail_weight_set_item, parent, false);
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

    class WeightSetViewHolder extends RecyclerView.ViewHolder {
        TextView setInfoTextView;

        public WeightSetViewHolder(@NonNull View itemView) {
            super(itemView);
            setInfoTextView = itemView.findViewById(R.id.set_info_text_view);
        }

        public void bind(Set set) {
            float weight = set.weight;
            if ("kg".equals(weightUnit)) {
                weight *= 0.453592f;
            }
            setInfoTextView.setText(String.format(Locale.getDefault(), "Set %d: %d reps, %.1f %s",
                    set.setNumber, set.reps, weight, weightUnit));
        }
    }

    class CardioSetViewHolder extends RecyclerView.ViewHolder {
        TextView setInfoTextView;

        public CardioSetViewHolder(@NonNull View itemView) {
            super(itemView);
            setInfoTextView = itemView.findViewById(R.id.set_info_text_view);
        }

        public void bind(Set set) {
            float distance = set.distance;
            if ("km".equals(distanceUnit)) {
                distance *= 1.60934f;
            }
            setInfoTextView.setText(String.format(Locale.getDefault(), "Set %d: %.1f mins, %.2f %s",
                    set.setNumber, set.duration, distance, distanceUnit));
        }
    }
}
