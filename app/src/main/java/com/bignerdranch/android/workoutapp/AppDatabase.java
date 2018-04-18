package com.bignerdranch.android.workoutapp;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.bignerdranch.android.workoutapp.database.converter.BooleanConverter;
import com.bignerdranch.android.workoutapp.database.converter.DateConverter;
import com.bignerdranch.android.workoutapp.database.dao.ExerciseDao;
import com.bignerdranch.android.workoutapp.database.dao.ReppedSetDao;
import com.bignerdranch.android.workoutapp.database.dao.RoutineDao;
import com.bignerdranch.android.workoutapp.database.dao.RoutineDayDao;
import com.bignerdranch.android.workoutapp.database.dao.TimedSetDao;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.ReppedSet;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;
import com.bignerdranch.android.workoutapp.model.TimedSet;


@Database(version = 1, entities = {Routine.class, RoutineDay.class, Exercise.class, ReppedSet.class, TimedSet.class})
@TypeConverters({DateConverter.class, BooleanConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase sInstance;

    @VisibleForTesting
    public static final String DATABASE_NAME = "workoutapp-db";

    // Our Dao's
    public abstract RoutineDao routineDao();
    public abstract RoutineDayDao routineDayDao();
    public abstract ExerciseDao exerciseDao();
    public abstract ReppedSetDao reppedSetDao();
    public abstract TimedSetDao timedSetDao();


}
