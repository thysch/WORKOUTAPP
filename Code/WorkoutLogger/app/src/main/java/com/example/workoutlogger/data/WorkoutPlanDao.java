package com.example.workoutlogger.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WorkoutPlanDao {
    @Insert
    long insert(WorkoutPlan workoutPlan);

    @Query("SELECT * FROM workout_plans")
    List<WorkoutPlan> getAllWorkoutPlans();

    @Query("SELECT * FROM workout_plans WHERE uid = :id")
    WorkoutPlan getWorkoutPlanById(long id);

    @Update
    void update(WorkoutPlan workoutPlan);

    @Delete
    void delete(WorkoutPlan workoutPlan);
}
