package com.example.workoutlogger.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Insert
    long insert(Exercise exercise);

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    List<Exercise> getAllExercises();

    @Query("SELECT * FROM exercises WHERE uid = :exerciseId")
    Exercise getExerciseById(long exerciseId);

    @Query("SELECT * FROM exercises WHERE name LIKE :query OR tags LIKE :query")
    List<Exercise> searchExercises(String query);

    /**
     * Selects all exercises from the Exercise table that have an ID in the provided list.
     * @param exerciseIds A list of exercise IDs to fetch.
     * @return A List of Exercise objects matching the given IDs.
     */
    @Query("SELECT * FROM exercises WHERE uid IN (:exerciseIds)")
    List<Exercise> getExercisesByIds(List<Long> exerciseIds);


}
