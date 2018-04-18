package com.bignerdranch.android.workoutapp.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.bignerdranch.android.workoutapp.model.RoutineDay;

import java.util.List;

@Dao
public interface RoutineDayDao {
    @Query("SELECT * FROM routine_days")
    List<RoutineDay> getAllRoutineDays();

    @Query("SELECT * FROM routine_days WHERE routine_day_id = :id")
    List<RoutineDay> getRoutineDay (int id);

    @Query("SELECT * FROM routine_days WHERE routine_id = :routineId")
    List<RoutineDay> getAllRoutineDaysInRoutine (int routineId);

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
            "LIMIT :number_days")
    List<RoutineDay> getMostRecentDaysInRoutine(int routineId, int number_days);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllRoutineDays(List<RoutineDay> routineDays);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRoutineDay(RoutineDay routineDay);

}
