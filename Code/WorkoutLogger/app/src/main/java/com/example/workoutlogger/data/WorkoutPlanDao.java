package com.example.workoutlogger.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WorkoutPlanDao {
    @Query("SELECT * FROM workout_plans ORDER BY displayOrder ASC")
    List<WorkoutPlan> getAllWorkoutPlans();

    @Query("SELECT * FROM workout_plans WHERE uid = :id")
    WorkoutPlan getWorkoutPlanById(long id);

    @Insert
    long insert(WorkoutPlan workoutPlan);

    @Update
    void update(WorkoutPlan workoutPlan);

    @Update
    void updateAll(List<WorkoutPlan> workoutPlans);

    @Delete
    void delete(WorkoutPlan workoutPlan);
}
