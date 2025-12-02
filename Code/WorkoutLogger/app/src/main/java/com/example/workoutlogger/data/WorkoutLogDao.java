package com.example.workoutlogger.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WorkoutLogDao {
    @Insert
    long insert(WorkoutLog workoutLog);

    @Query("SELECT * FROM workout_logs ORDER BY date DESC")
    List<WorkoutLog> getAllWorkoutLogs();

    @Query("SELECT * FROM workout_logs WHERE uid = :workoutLogId")
    WorkoutLog getWorkoutLogById(long workoutLogId);

    @Query("SELECT COUNT(*) FROM workout_logs")
    int getWorkoutLogCount();
}
