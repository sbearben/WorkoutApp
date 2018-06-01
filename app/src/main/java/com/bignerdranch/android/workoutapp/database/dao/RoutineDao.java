package com.bignerdranch.android.workoutapp.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.bignerdranch.android.workoutapp.model.Routine;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface RoutineDao {

    @Query("SELECT * FROM routines")
    List<Routine> getAllRoutines();

    @Query("SELECT * FROM routines WHERE routine_id = :id")
    Routine getRoutine(int id);

    @Query("SELECT routine_name FROM routines")
    List<String> getAllRoutineNames();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllRoutines(List<Routine> routines);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRoutine(Routine routine);

    @Delete
    public void deleteRoutine (Routine routine);

    @Delete
    public void deleteRoutines(Routine... routines);
}
