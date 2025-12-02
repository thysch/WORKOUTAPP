package com.example.workoutlogger.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_plans")
public class WorkoutPlan {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    public String name;
    public String exerciseIds;
    public int displayOrder;
}
