package com.example.workoutlogger.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SetDao {
    @Insert
    void insert(Set set);

    @Query("SELECT * FROM sets WHERE workoutLogId = :workoutLogId")
    List<Set> getSetsForWorkoutLog(long workoutLogId);
}