package com.example.workoutlogger;

import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
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
        holder.bind(sets.get(position));
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    class SetViewHolder extends RecyclerView.ViewHolder {
        TextView setNumberTextView;
        EditText repsEditText, weightEditText;
        CheckBox setCompleteCheckbox;
        ImageButton deleteSetButton;

        private TextWatcher repsWatcher;
        private TextWatcher weightWatcher;

        public SetViewHolder(@NonNull View itemView) {
            super(itemView);
            setNumberTextView = itemView.findViewById(R.id.set_number_text_view);
            repsEditText = itemView.findViewById(R.id.reps_edit_text);
            weightEditText = itemView.findViewById(R.id.weight_edit_text);
            setCompleteCheckbox = itemView.findViewById(R.id.set_complete_checkbox);
            deleteSetButton = itemView.findViewById(R.id.delete_set_button);
        }

        public void bind(final WorkoutSet set) {
            setNumberTextView.setText("Set " + (getAdapterPosition() + 1));

            // Remove listeners before setting text to avoid triggering them
            if (repsWatcher != null) repsEditText.removeTextChangedListener(repsWatcher);
            if (weightWatcher != null) weightEditText.removeTextChangedListener(weightWatcher);

            repsEditText.setText(set.plannedReps);
            if (set.weight > 0) {
                weightEditText.setText(String.valueOf(set.weight));
            } else {
                weightEditText.setText("");
            }
            setCompleteCheckbox.setChecked(set.isCompleted);

            setCompleteCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                set.isCompleted = isChecked;
                if (volumeChangedListener != null) {
                    volumeChangedListener.onVolumeChanged();
                }
            });

            deleteSetButton.setOnClickListener(v -> {
                new AlertDialog.Builder(itemView.getContext())
                        .setTitle("Delete Set")
                        .setMessage("Are you sure you want to delete this set?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            int position = getAdapterPosition();
                            if (position != RecyclerView.NO_POSITION) {
                                sets.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, sets.size());
                                if (volumeChangedListener != null) {
                                    volumeChangedListener.onVolumeChanged();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            repsWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    set.plannedReps = s.toString();
                    if (!s.toString().isEmpty() && !setCompleteCheckbox.isChecked()) {
                        setCompleteCheckbox.setChecked(true); // This triggers the OnCheckedChangeListener
                    } else if (setCompleteCheckbox.isChecked()) {
                        if (volumeChangedListener != null) volumeChangedListener.onVolumeChanged();
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            };

            weightWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        set.weight = Float.parseFloat(s.toString());
                    } catch (NumberFormatException e) {
                        set.weight = 0;
                    }
                    if (!s.toString().isEmpty() && !setCompleteCheckbox.isChecked()) {
                        setCompleteCheckbox.setChecked(true);
                    } else if (setCompleteCheckbox.isChecked()) {
                        if (volumeChangedListener != null) volumeChangedListener.onVolumeChanged();
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            };

            repsEditText.addTextChangedListener(repsWatcher);
            weightEditText.addTextChangedListener(weightWatcher);
        }
    }
}
