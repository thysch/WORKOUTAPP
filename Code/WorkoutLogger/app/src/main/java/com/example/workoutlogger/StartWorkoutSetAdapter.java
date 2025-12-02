package com.example.workoutlogger;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.WorkoutSet;
import java.util.List;

public class StartWorkoutSetAdapter extends RecyclerView.Adapter<StartWorkoutSetAdapter.SetViewHolder> {

    public interface OnVolumeChangedListener {
        void onVolumeChanged();
    }

    private List<WorkoutSet> sets;
    private OnVolumeChangedListener volumeChangedListener;

    public StartWorkoutSetAdapter(List<WorkoutSet> sets, OnVolumeChangedListener listener) {
        this.sets = sets;
        this.volumeChangedListener = listener;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.start_workout_set_item, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        holder.bind(sets.get(position), position);
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    class SetViewHolder extends RecyclerView.ViewHolder {
        TextView setNumberTextView;
        EditText repsEditText, weightEditText;
        CheckBox setCompleteCheckbox;

        public SetViewHolder(@NonNull View itemView) {
            super(itemView);
            setNumberTextView = itemView.findViewById(R.id.set_number_text_view);
            repsEditText = itemView.findViewById(R.id.reps_edit_text);
            weightEditText = itemView.findViewById(R.id.weight_edit_text);
            setCompleteCheckbox = itemView.findViewById(R.id.set_complete_checkbox);
        }

        public void bind(final WorkoutSet set, final int position) {
            setNumberTextView.setText("Set " + (position + 1));

            repsEditText.setText(set.plannedReps);
            if (set.weight > 0) {
                weightEditText.setText(String.valueOf(set.weight));
            } else {
                weightEditText.setText("");
            }

            repsEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    set.plannedReps = s.toString();
                    if (volumeChangedListener != null) {
                        volumeChangedListener.onVolumeChanged();
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            weightEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        set.weight = Float.parseFloat(s.toString());
                    } catch (NumberFormatException e) {
                        set.weight = 0;
                    }
                    if (volumeChangedListener != null) {
                        volumeChangedListener.onVolumeChanged();
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }
}
