package com.example.workoutlogger.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_logs",
        foreignKeys = {
                @ForeignKey(entity = WorkoutPlan.class,
                        parentColumns = "uid",
                        childColumns = "planId",
                        onDelete = ForeignKey.CASCADE)
        })
public class WorkoutLog {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    public long planId;

    // The date the workout was performed, stored as a long (milliseconds)
    public long date;

    // The duration of the workout in seconds
    public long duration;

    // The total volume lifted in the workout
    public float totalVolume;

    // The name of the workout plan at the time of logging
    public String workoutName;
}
