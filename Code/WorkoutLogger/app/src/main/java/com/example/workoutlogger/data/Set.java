package com.example.workoutlogger.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sets")
public class Set {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long workoutLogId;
    public long exerciseId;

    // For weight-based exercises
    public int plannedReps;
    public int actualReps;
    public float weight; // Storing as float for flexibility

    // For cardio exercises
    public long duration; // in seconds
    public float distance; // in preferred unit (e.g., miles or km)

    public String notes;
}