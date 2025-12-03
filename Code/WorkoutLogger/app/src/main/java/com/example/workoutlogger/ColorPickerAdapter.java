package com.example.workoutlogger;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder> {

    private final Context context;
    private final List<Integer> colors;
    private int selectedColor;
    private final OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(int colorResId);
    }

    public ColorPickerAdapter(Context context, List<Integer> colors, int selectedColor, OnColorSelectedListener listener) {
        this.context = context;
        this.colors = colors;
        this.selectedColor = selectedColor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.color_picker_item, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        int colorRes = colors.get(position);
        holder.bind(colorRes);
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    class ColorViewHolder extends RecyclerView.ViewHolder {
        View colorView;
        ImageView checkMark;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.color_view);
            checkMark = itemView.findViewById(R.id.check_mark);
        }

        void bind(final int colorRes) {
            colorView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)));

            if (colorRes == selectedColor) {
                checkMark.setVisibility(View.VISIBLE);
            } else {
                checkMark.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                int previouslySelected = selectedColor;
                selectedColor = colorRes;
                notifyItemChanged(colors.indexOf(previouslySelected));
                notifyItemChanged(colors.indexOf(selectedColor));
                listener.onColorSelected(colorRes);
            });
        }
    }
}