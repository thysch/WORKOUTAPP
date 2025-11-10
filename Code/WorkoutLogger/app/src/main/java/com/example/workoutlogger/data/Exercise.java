package com.example.workoutlogger.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercises")
public class Exercise {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    public String name;

    // Type can be "WEIGHT" or "CARDIO" to distinguish the exercise type
    public String type;
}