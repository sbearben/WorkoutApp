package com.bignerdranch.android.workoutapp.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.bignerdranch.android.workoutapp.model.Set;
import com.bignerdranch.android.workoutapp.model.TimedSet;

import java.util.List;

@Dao
public interface TimedSetDao {

    @Query("SELECT * FROM timed_sets")
    List<TimedSet> getAllTimedSets();

    @Query("SELECT * FROM timed_sets WHERE set_id = :setId")
    TimedSet getTimedSet(int setId);

    @Query("SELECT * FROM repped_sets WHERE exercise_id = :exerciseId")
    List<TimedSet> getAllTimedExerciseSets(int exerciseId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllTimedSets(List<TimedSet> timedSets);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTimedSet(TimedSet timedSet);
}
