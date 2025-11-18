package com.example.workoutlogger.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WorkoutSetDao {
    @Insert
    long[] insertAll(WorkoutSet... workoutSets);

    @Query("SELECT * FROM workout_sets WHERE workoutPlanId = :planId")
    List<WorkoutSet> getSetsForWorkoutPlan(long planId);

    @Query("DELETE FROM workout_sets WHERE workoutPlanId = :planId")
    void deleteSetsForWorkoutPlan(long planId);
}
