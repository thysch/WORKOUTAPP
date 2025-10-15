package com.example.workoutlogger.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WorkoutPlanDao {
    @Insert
    void insert(WorkoutPlan workoutPlan);

    @Query("SELECT * FROM workout_plans")
    List<WorkoutPlan> getAllWorkoutPlans();
}