package com.bignerdranch.android.workoutapp.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.bignerdranch.android.workoutapp.model.Exercise;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Query("SELECT * FROM exercises")
    LiveData<List<Exercise>> getAllExercises();

    @Query("SELECT * FROM exercises WHERE exercise_id = :id")
    LiveData<Exercise> getExercise(int id);

    @Query("SELECT * FROM exercises WHERE routine_day_id = :routineDayId")
    LiveData<List<Exercise>> getAllExercisesInRoutineDay (int routineDayId);

    @Query("SELECT * " +
            "FROM exercises " +
            "WHERE routine_day_id = :routineDayId " +
            "ORDER BY exercise_number " +
            "LIMIT :numberExercises")
    LiveData<List<Exercise>> getFirstNExercisesInRoutineDay (int routineDayId, int numberExercises);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllExercises(List<Exercise> exercises);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExercise(Exercise exercise);
}
