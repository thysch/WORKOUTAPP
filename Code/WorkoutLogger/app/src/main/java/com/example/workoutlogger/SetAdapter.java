package com.example.workoutlogger;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.workoutlogger.data.WorkoutSet;
import java.util.List;

public class SetAdapter extends RecyclerView.Adapter<SetAdapter.SetViewHolder> {

    private List<WorkoutSet> sets;

    public SetAdapter(List<WorkoutSet> sets) {
        this.sets = sets;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item, parent, false);
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
        EditText repsEditText;
        EditText weightEditText;
        TextWatcher repsWatcher;
        TextWatcher weightWatcher;


        public SetViewHolder(@NonNull View itemView) {
            super(itemView);
            setNumberTextView = itemView.findViewById(R.id.set_number_text_view);
            repsEditText = itemView.findViewById(R.id.reps_edit_text);
            weightEditText = itemView.findViewById(R.id.weight_edit_text);
        }

        public void bind(final WorkoutSet set) {
            setNumberTextView.setText(String.format("%d.", getAdapterPosition() + 1));

            if (repsWatcher != null) {
                repsEditText.removeTextChangedListener(repsWatcher);
            }
            if (weightWatcher != null) {
                weightEditText.removeTextChangedListener(weightWatcher);
            }

            repsEditText.setText(set.reps);
            if (set.weight > 0) {
                weightEditText.setText(String.valueOf(set.weight));
            } else {
                weightEditText.setText("");
            }

            repsWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(getAdapterPosition() != RecyclerView.NO_POSITION) {
                        sets.get(getAdapterPosition()).reps = s.toString();
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            };
            repsEditText.addTextChangedListener(repsWatcher);

            weightWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                     if(getAdapterPosition() != RecyclerView.NO_POSITION) {
                        try {
                            sets.get(getAdapterPosition()).weight = Float.parseFloat(s.toString());
                        } catch (NumberFormatException e) {
                            sets.get(getAdapterPosition()).weight = 0;
                        }
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            };
            weightEditText.addTextChangedListener(weightWatcher);
        }
    }
}
