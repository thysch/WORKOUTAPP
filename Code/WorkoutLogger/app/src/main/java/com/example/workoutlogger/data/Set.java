package com.example.workoutlogger.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "sets",
        foreignKeys = {
                @ForeignKey(entity = WorkoutLog.class,
                        parentColumns = "uid",
                        childColumns = "workoutLogId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Exercise.class,
                        parentColumns = "uid",
                        childColumns = "exerciseId",
                        onDelete = ForeignKey.CASCADE)
        })
public class Set {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    public long workoutLogId;
    public long exerciseId;

    public int reps;
    public float weight;
    public int setNumber;
}
