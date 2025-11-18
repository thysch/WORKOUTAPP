package com.example.workoutlogger.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "sets",
        foreignKeys = {
                @ForeignKey(entity = Exercise.class,
                        parentColumns = "uid",
                        childColumns = "exerciseId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = WorkoutLog.class,
                        parentColumns = "uid",
                        childColumns = "workoutLogId",
                        onDelete = ForeignKey.CASCADE)
        })
public class Set {
    @PrimaryKey(autoGenerate = true)
    public long uid;

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