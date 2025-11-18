package com.example.workoutlogger.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WorkoutLogDao {
    @Insert
    long insert(WorkoutLog workoutLog);

    @Query("SELECT * FROM workout_logs")
    List<WorkoutLog> getAllWorkoutLogs();

    @Query("SELECT * FROM workout_logs WHERE uid = :workoutLogId")
    WorkoutLog getWorkoutLogById(long workoutLogId);
}
