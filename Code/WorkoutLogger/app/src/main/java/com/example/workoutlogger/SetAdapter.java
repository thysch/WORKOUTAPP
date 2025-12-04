package com.example.workoutlogger;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.Exercise;
import com.example.workoutlogger.data.WorkoutSet;
import java.util.List;
import java.util.Locale;

public class SetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_WEIGHT = 1;
    private static final int VIEW_TYPE_CARDIO = 2;

    private List<WorkoutSet> sets;
    private final Exercise exercise;
    private final AddedExercisesAdapter.OnDataChangedListener dataChangedListener;

    public SetAdapter(List<WorkoutSet> sets, Exercise exercise, AddedExercisesAdapter.OnDataChangedListener listener) {
        this.sets = sets;
        this.exercise = exercise;
        this.dataChangedListener = listener;
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_workout_cardio_set_item, parent, false);
            return new CardioSetViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item, parent, false);
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

    public void notifyDataChanged() {
        if (dataChangedListener != null) {
            dataChangedListener.onDataChanged();
        }
    }

    class WeightSetViewHolder extends RecyclerView.ViewHolder {
        TextView setNumberTextView;
        EditText repsEditText, weightEditText;
        ImageButton deleteSetButton;

        public WeightSetViewHolder(@NonNull View itemView) {
            super(itemView);
            setNumberTextView = itemView.findViewById(R.id.set_number_text_view);
            repsEditText = itemView.findViewById(R.id.reps_edit_text);
            weightEditText = itemView.findViewById(R.id.weight_edit_text);
            deleteSetButton = itemView.findViewById(R.id.delete_set_button);
        }

        public void bind(final WorkoutSet set) {
            setNumberTextView.setText("Set " + (getAdapterPosition() + 1));
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

            repsEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    set.plannedReps = s.toString();
                    notifyDataChanged();
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
                    notifyDataChanged();
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            deleteSetButton.setOnClickListener(v -> {
                int currentPosition = getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    sets.remove(currentPosition);
                    notifyItemRemoved(currentPosition);
                    notifyItemRangeChanged(currentPosition, sets.size() - currentPosition);
                    notifyDataChanged();
                }
            });
        }
    }

    class CardioSetViewHolder extends RecyclerView.ViewHolder {
        TextView setNumberTextView;
        EditText durationEditText, distanceEditText;
        ImageButton deleteSetButton;

        public CardioSetViewHolder(@NonNull View itemView) {
            super(itemView);
            setNumberTextView = itemView.findViewById(R.id.set_number_text_view);
            durationEditText = itemView.findViewById(R.id.duration_edit_text);
            distanceEditText = itemView.findViewById(R.id.distance_edit_text);
            deleteSetButton = itemView.findViewById(R.id.delete_set_button);
        }

        public void bind(final WorkoutSet set) {
            setNumberTextView.setText("Set " + (getAdapterPosition() + 1));

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

            durationEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        set.duration = Float.parseFloat(s.toString());
                    } catch (NumberFormatException e) {
                        set.duration = 0;
                    }
                    notifyDataChanged();
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            distanceEditText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        set.distance = Float.parseFloat(s.toString());
                    } catch (NumberFormatException e) {
                        set.distance = 0;
                    }
                    notifyDataChanged();
                }
                @Override public void afterTextChanged(Editable s) {}
            });

            deleteSetButton.setOnClickListener(v -> {
                int currentPosition = getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    sets.remove(currentPosition);
                    notifyItemRemoved(currentPosition);
                    notifyItemRangeChanged(currentPosition, sets.size() - currentPosition);
                    notifyDataChanged();
                }
            });
        }
    }
}
