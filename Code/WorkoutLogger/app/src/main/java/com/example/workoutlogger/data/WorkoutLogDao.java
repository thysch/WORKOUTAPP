package com.example.workoutlogger.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WorkoutLogDao {
    @Insert
    void insert(WorkoutLog workoutLog);

    @Query("SELECT * FROM workout_logs")
    List<WorkoutLog> getAllWorkoutLogs();
}