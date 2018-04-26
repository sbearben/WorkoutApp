package com.bignerdranch.android.workoutapp.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.bignerdranch.android.workoutapp.model.ReppedSet;
import com.bignerdranch.android.workoutapp.model.Set;

import java.util.List;

@Dao
public interface ReppedSetDao {

    @Query("SELECT * FROM repped_sets")
    LiveData<List<ReppedSet>> getAllReppedSets();

    @Query("SELECT * FROM repped_sets WHERE set_id = :setId")
    LiveData<ReppedSet> getReppedSet(int setId);

    @Query("SELECT * FROM repped_sets WHERE exercise_id = :exerciseId")
    LiveData<List<ReppedSet>> getAllReppedExerciseSets(int exerciseId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllReppedSets(List<ReppedSet> reppedSets);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReppedSet(ReppedSet reppedSet);
}
