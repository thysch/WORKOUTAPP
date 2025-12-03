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
import java.util.Locale;
import java.util.function.Supplier;

public class StartWorkoutSetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnVolumeChangedListener {
        void onVolumeChanged();
    }

    private static final int VIEW_TYPE_WEIGHT = 1;
    private static final int VIEW_TYPE_CARDIO = 2;

    private List<WorkoutSet> sets;
    private OnVolumeChangedListener volumeChangedListener;
    private final StartWorkoutExerciseAdapter.OnWorkoutCompleteListener workoutCompleteListener;
    private final String exerciseType;
    private final Supplier<Integer> uncheckedSetsCountSupplier;

    public StartWorkoutSetAdapter(List<WorkoutSet> sets, String exerciseType, OnVolumeChangedListener volumeListener, StartWorkoutExerciseAdapter.OnWorkoutCompleteListener completeListener, Supplier<Integer> uncheckedSetsCountSupplier) {
        this.sets = sets;
        this.exerciseType = exerciseType;
        this.volumeChangedListener = volumeListener;
        this.workoutCompleteListener = completeListener;
        this.uncheckedSetsCountSupplier = uncheckedSetsCountSupplier;
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.start_workout_cardio_set_item, parent, false);
            return new CardioSetViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.start_workout_set_item, parent, false);
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

    private void triggerWorkoutComplete() {
        if (workoutCompleteListener != null) {
            workoutCompleteListener.onWorkoutComplete();
        }
    }

    class WeightSetViewHolder extends RecyclerView.ViewHolder {
        TextView setNumberTextView;
        EditText repsEditText, weightEditText;
        CheckBox setCompleteCheckbox;
        ImageButton deleteSetButton;

        private TextWatcher repsWatcher;
        private TextWatcher weightWatcher;

        public WeightSetViewHolder(@NonNull View itemView) {
            super(itemView);
            setNumberTextView = itemView.findViewById(R.id.set_number_text_view);
            repsEditText = itemView.findViewById(R.id.reps_edit_text);
            weightEditText = itemView.findViewById(R.id.weight_edit_text);
            setCompleteCheckbox = itemView.findViewById(R.id.set_complete_checkbox);
            deleteSetButton = itemView.findViewById(R.id.delete_set_button);
        }

        public void bind(final WorkoutSet set) {
            setNumberTextView.setText("Set " + (getAdapterPosition() + 1));

            if (repsWatcher != null) repsEditText.removeTextChangedListener(repsWatcher);
            if (weightWatcher != null) weightEditText.removeTextChangedListener(weightWatcher);

            repsEditText.setText(set.plannedReps);
            if (set.weight > 0) {
                if (set.weight == (long) set.weight) {
                    weightEditText.setText(String.format(Locale.getDefault(), "%d", (long) set.weight));
                } else {
                    weightEditText.setText(String.valueOf(set.weight));
                }
            } else {
                weightEditText.setText("");
            }
            setCompleteCheckbox.setChecked(set.isCompleted);

            setCompleteCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                set.isCompleted = isChecked;
                if (volumeChangedListener != null) {
                    volumeChangedListener.onVolumeChanged();
                }
                triggerWorkoutComplete();
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
                    if (setCompleteCheckbox.isChecked() && volumeChangedListener != null) {
                        volumeChangedListener.onVolumeChanged();
                    }
                }
                @Override public void afterTextChanged(Editable s) {
                    checkWeightCompletion();
                }
            };

            weightWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        set.weight = Float.parseFloat(s.toString());
                    } catch (NumberFormatException e) {
                        set.weight = 0;
                    }
                    if (setCompleteCheckbox.isChecked() && volumeChangedListener != null) {
                        volumeChangedListener.onVolumeChanged();
                    }
                }
                @Override public void afterTextChanged(Editable s) {
                    checkWeightCompletion();
                }
            };

            repsEditText.addTextChangedListener(repsWatcher);
            weightEditText.addTextChangedListener(weightWatcher);
        }

        private void checkWeightCompletion() {
            if (uncheckedSetsCountSupplier.get() <= 1) return;
            String repsStr = repsEditText.getText().toString();
            String weightStr = weightEditText.getText().toString();

            if (!repsStr.isEmpty() && !weightStr.isEmpty()) {
                try {
                    int reps = Integer.parseInt(repsStr);
                    float weight = Float.parseFloat(weightStr);
                    if (reps > 0 && weight > 0 && !setCompleteCheckbox.isChecked()) {
                        setCompleteCheckbox.setChecked(true);
                    }
                } catch (NumberFormatException e) {
                    // Do nothing if parsing fails
                }
            }
        }
    }

    class CardioSetViewHolder extends RecyclerView.ViewHolder {
        TextView setNumberTextView;
        EditText durationEditText, distanceEditText;
        CheckBox setCompleteCheckbox;
        ImageButton deleteSetButton;

        private TextWatcher durationWatcher;
        private TextWatcher distanceWatcher;

        public CardioSetViewHolder(@NonNull View itemView) {
            super(itemView);
            setNumberTextView = itemView.findViewById(R.id.set_number_text_view);
            durationEditText = itemView.findViewById(R.id.duration_edit_text);
            distanceEditText = itemView.findViewById(R.id.distance_edit_text);
            setCompleteCheckbox = itemView.findViewById(R.id.set_complete_checkbox);
            deleteSetButton = itemView.findViewById(R.id.delete_set_button);
        }

        public void bind(final WorkoutSet set) {
            setNumberTextView.setText("Set " + (getAdapterPosition() + 1));

            if (durationWatcher != null) durationEditText.removeTextChangedListener(durationWatcher);
            if (distanceWatcher != null) distanceEditText.removeTextChangedListener(distanceWatcher);

            if (set.duration > 0) {
                if (set.duration == (long) set.duration) {
                    durationEditText.setText(String.format(Locale.getDefault(), "%d", (long) set.duration));
                } else {
                    durationEditText.setText(String.valueOf(set.duration));
                }
            } else {
                durationEditText.setText("");
            }
            if (set.distance > 0) {
                if (set.distance == (long) set.distance) {
                    distanceEditText.setText(String.format(Locale.getDefault(), "%d", (long) set.distance));
                } else {
                    distanceEditText.setText(String.valueOf(set.distance));
                }
            } else {
                distanceEditText.setText("");
            }
            setCompleteCheckbox.setChecked(set.isCompleted);

            setCompleteCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                set.isCompleted = isChecked;
                triggerWorkoutComplete();
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
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            durationWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        set.duration = Float.parseFloat(s.toString());
                    } catch (NumberFormatException e) {
                        set.duration = 0;
                    }
                }
                @Override public void afterTextChanged(Editable s) {
                    checkCardioCompletion();
                }
            };

            distanceWatcher = new TextWatcher() {
                 @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        set.distance = Float.parseFloat(s.toString());
                    } catch (NumberFormatException e) {
                        set.distance = 0;
                    }
                }
                @Override public void afterTextChanged(Editable s) {
                    checkCardioCompletion();
                }
            };

            durationEditText.addTextChangedListener(durationWatcher);
            distanceEditText.addTextChangedListener(distanceWatcher);
        }

        private void checkCardioCompletion() {
            if (uncheckedSetsCountSupplier.get() <= 1) return;
            boolean durationEntered = false;
            try {
                if (!durationEditText.getText().toString().trim().isEmpty()) {
                    durationEntered = Float.parseFloat(durationEditText.getText().toString().trim()) > 0;
                }
            } catch (NumberFormatException e) { /* Do nothing */ }

            boolean distanceEntered = false;
            try {
                if (!distanceEditText.getText().toString().trim().isEmpty()) {
                    distanceEntered = Float.parseFloat(distanceEditText.getText().toString().trim()) > 0;
                }
            } catch (NumberFormatException e) { /* Do nothing */ }

            if ((durationEntered || distanceEntered) && !setCompleteCheckbox.isChecked()) {
                setCompleteCheckbox.setChecked(true);
            }
        }
    }
}
