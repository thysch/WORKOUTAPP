package com.example.workoutlogger.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_sets",
        foreignKeys = {
                @ForeignKey(entity = Exercise.class,
                        parentColumns = "uid", // Changed from "uid" to "id"
                        childColumns = "exerciseId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = WorkoutPlan.class,
                        parentColumns = "uid",
                        childColumns = "workoutPlanId",
                        onDelete = ForeignKey.CASCADE)
        })

public class WorkoutSet {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    public long exerciseId;
    public long workoutPlanId;

    public String reps;
    public float weight;
    public int setNumber;
}
