package com.example.workoutlogger.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_plans")
public class WorkoutPlan {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;

    // Comma-separated list of exercise IDs in this plan
    public String exerciseIds;
}