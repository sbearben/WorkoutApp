package com.bignerdranch.android.workoutapp.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.bignerdranch.android.workoutapp.database.converter.BooleanConverter;
import com.bignerdranch.android.workoutapp.database.converter.DateConverter;
import com.bignerdranch.android.workoutapp.database.dao.ExerciseDao;
import com.bignerdranch.android.workoutapp.database.dao.ReppedSetDao;
import com.bignerdranch.android.workoutapp.database.dao.RoutineDao;
import com.bignerdranch.android.workoutapp.database.dao.RoutineDayDao;
import com.bignerdranch.android.workoutapp.database.dao.TimedSetDao;
import com.bignerdranch.android.workoutapp.global.AppExecutors;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.ReppedSet;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;
import com.bignerdranch.android.workoutapp.model.TimedSet;

import java.util.List;


@Database(version = 1, entities = {Routine.class, RoutineDay.class, Exercise.class, ReppedSet.class, TimedSet.class})
@TypeConverters({DateConverter.class, BooleanConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = "AppDatabase";

    private static AppDatabase sInstance;

    @VisibleForTesting
    public static final String DATABASE_NAME = "workoutapp-db";

    // Our Dao(s)
    public abstract RoutineDao routineDao();
    public abstract RoutineDayDao routineDayDao();
    public abstract ExerciseDao exerciseDao();
    public abstract ReppedSetDao reppedSetDao();
    public abstract TimedSetDao timedSetDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();


    public static AppDatabase getInstance (final Context context, final AppExecutors executors) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase (context.getApplicationContext(), executors);
                    sInstance.updateDatabaseCreated (context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    /**
     * Build the database. {@link Builder#build()} only sets up the database configuration and
     * creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
     */
    private static AppDatabase buildDatabase (final Context appContext, final AppExecutors executors) {
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME)
                .allowMainThreadQueries()
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Log.i(TAG, "DATABSE CREATED");
                        executors.diskIO().execute(() -> {
                            // Add a delay to simulate a long-running operation
                            // addDelay();

                            // Generate the data for pre-population
                            AppDatabase database = AppDatabase.getInstance(appContext, executors);

                            List<Routine> routines = DataGenerator.generateRoutines();
                            List<RoutineDay> routineDays = DataGenerator.generateRoutineDays(routines);
                            List<Exercise> exercises = DataGenerator.generateExercises(routineDays, routines);
                            List<ReppedSet> reppedSets = DataGenerator.generateReppedSets(exercises, routineDays);

                            insertData(database, routines, routineDays, exercises, reppedSets);
                            // notify that the database was created and it's ready to be used
                            database.setDatabaseCreated();
                        });
                    }
                })
                .build();
    }

    /**
     * Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
     */
    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    private void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }

    private static void insertData(final AppDatabase database, final List<Routine> routines, final List<RoutineDay> routineDays,
                                   final List<Exercise> exercises, final List<ReppedSet> reppedSets) {
        database.runInTransaction(() -> {
            database.routineDao().insertAllRoutines(routines);
            database.routineDayDao().insertAllRoutineDays(routineDays);
            database.exerciseDao().insertAllExercises(exercises);
            database.reppedSetDao().insertAllReppedSets(reppedSets);
        });
    }

    private static void addDelay() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ignored) {
        }
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }

    public static void destroyInstance() {
        sInstance = null;
    }


}
