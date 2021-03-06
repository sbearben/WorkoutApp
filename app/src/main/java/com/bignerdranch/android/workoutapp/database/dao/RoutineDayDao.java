package com.bignerdranch.android.workoutapp.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.bignerdranch.android.workoutapp.model.RoutineDay;

import java.util.List;

@Dao
public interface RoutineDayDao {
    @Query("SELECT * FROM routine_days")
    List<RoutineDay> getAllRoutineDays();

    @Query("SELECT * FROM routine_days WHERE routine_day_id = :id")
    RoutineDay getRoutineDay (int id);

    @Query("SELECT * " +
            "FROM routine_days " +
            "WHERE routine_id = :routineId AND routine_day_completed = 1 " +
            "ORDER BY routine_day_date_performed DESC")
    List<RoutineDay> getAllCompletedRoutineDaysInRoutine (int routineId);

    @Query("SELECT routine_id " +
            "FROM routine_days " +
            "WHERE routine_day_completed = 1 " +
            "ORDER BY routine_day_date_performed DESC " +
            "LIMIT 1")
    int getMostRecentRoutineId();

    @Query("SELECT * " +
            "FROM routine_days " +
            "WHERE routine_day_completed = 1 AND routine_id = :routineId " +
            "ORDER BY routine_day_date_performed DESC, routine_day_number " +
            "LIMIT :numberDays")
    List<RoutineDay> getMostRecentDaysInRoutine (int routineId, int numberDays);

    @Query("SELECT * " +
            "FROM routine_days " +
            "WHERE routine_id = :routineId AND routine_day_completed = 0 AND routine_day_template = 0 " +
            "ORDER BY routine_day_date_performed DESC ")
    List<RoutineDay> getOngoingDaysInRoutine (int routineId);

    // 1 means TRUE
    @Query("SELECT * " +
            "FROM routine_days " +
            "WHERE routine_id = :routineId AND routine_day_template = 1 " +
            "ORDER BY routine_day_number")
    List<RoutineDay> getTemplateRoutineDays (int routineId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllRoutineDays(List<RoutineDay> routineDays);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRoutineDay(RoutineDay routineDay);

    @Update
    void updateRoutineDay (RoutineDay routineDay);

    @Delete
    public void deleteRoutineDay (RoutineDay routineDay);

    @Delete
    public void deleteRoutineDays (RoutineDay... routineDays);

}
