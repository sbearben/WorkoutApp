package com.bignerdranch.android.workoutapp.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.bignerdranch.android.workoutapp.model.Exercise;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Query("SELECT * FROM exercises")
    List<Exercise> getAllExercises();

    @Query("SELECT * FROM exercises WHERE exercise_id = :id")
    Exercise getExercise(int id);

    @Query("SELECT * FROM exercises WHERE routine_day_id = :routineDayId")
    List<Exercise> getAllExercisesInRoutineDay (int routineDayId);

    @Query("SELECT * " +
            "FROM exercises " +
            "WHERE routine_day_id = :routineDayId " +
            "ORDER BY exercise_number " +
            "LIMIT :numberExercises")
    List<Exercise> getFirstNExercisesInRoutineDay (int routineDayId, int numberExercises);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAllExercises(List<Exercise> exercises);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertExercise(Exercise exercise);

    @Update
    void updateExercise(Exercise exercise);

    @Delete
    public void deleteExercise (Exercise exercise);

    @Delete
    public void deleteExercises (Exercise... exercises);
}
