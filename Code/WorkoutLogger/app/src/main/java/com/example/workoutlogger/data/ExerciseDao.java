package com.example.workoutlogger.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Insert
    long insert(Exercise exercise);

    @Query("SELECT * FROM exercises")
    List<Exercise> getAllExercises();

    @Query("SELECT * FROM exercises WHERE uid = :exerciseId")
    Exercise getExerciseById(long exerciseId);

    @Query("SELECT * FROM exercises WHERE name LIKE :query OR tags LIKE :query")
    List<Exercise> searchExercises(String query);
}
