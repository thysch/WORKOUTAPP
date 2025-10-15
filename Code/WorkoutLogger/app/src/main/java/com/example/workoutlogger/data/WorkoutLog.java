package com.example.workoutlogger.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_logs")
public class WorkoutLog {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long planId;

    // The date the workout was performed, stored as a long (milliseconds)
    public long date;
}