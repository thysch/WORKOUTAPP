package com.example.workoutlogger.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_sets",
        foreignKeys = {
                @ForeignKey(entity = WorkoutPlan.class,
                        parentColumns = "uid",
                        childColumns = "workoutPlanId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Exercise.class,
                        parentColumns = "uid",
                        childColumns = "exerciseId",
                        onDelete = ForeignKey.CASCADE)
        })
public class WorkoutSet {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    public long workoutPlanId;
    public long exerciseId;

    public int setNumber;
    public String plannedReps;
    public float weight;

    // For Cardio
    public float duration; // in minutes
    public float distance; // in miles

    @Ignore
    public boolean isCompleted;
}
