package com.bignerdranch.android.workoutapp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

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
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        executors.diskIO().execute(() -> {
                            // Add a delay to simulate a long-running operation
                            //addDelay();
                            // Generate the data for pre-population
                            AppDatabase database = AppDatabase.getInstance(appContext, executors);
                            //List<ProductEntity> products = DataGenerator.generateProducts();
                            //List<CommentEntity> comments = DataGenerator.generateCommentsForProducts(products);

                            //insertData(database, products, comments);
                            // notify that the database was created and it's ready to be used
                            database.setDatabaseCreated();
                        });
                    }
                }).build();
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

    /* private static void insertData(final AppDatabase database, final List<ProductEntity> products, final List<CommentEntity> comments) {
        database.runInTransaction(() -> {
            database.productDao().insertAll(products);
            database.commentDao().insertAll(comments);
        });
    } */

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
