package com.bignerdranch.android.workoutapp.global;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.bignerdranch.android.workoutapp.AppDatabase;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.ReppedSet;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;
import com.bignerdranch.android.workoutapp.model.Set;
import com.bignerdranch.android.workoutapp.model.TimedSet;

import java.util.List;


public class DataRepository {

    private static final String TAG = "DataRepository";

    private static DataRepository sInstance;

    private final AppDatabase mDatabase;


    private DataRepository (final AppDatabase database) {
        mDatabase = database;
    }

    public static DataRepository getInstance(final AppDatabase database) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database);
                }
            }
        }
        return sInstance;
    }

    /**
     * Routine queries
     */
    public LiveData<List<Routine>> loadRoutines() {
        return mDatabase.routineDao().getAllRoutines();
    }

    public LiveData<Routine> loadRoutine (final int routineId) {
        return mDatabase.routineDao().getRoutine(routineId);
    }

    public void insertRoutines (final List<Routine> routines) {
        mDatabase.routineDao().insertAllRoutines(routines);
    }

    public void insertRoutine (final Routine routine) {
        mDatabase.routineDao().insertRoutine(routine);
    }

    /**
     * RoutineDay queries
     */
    public LiveData<List<RoutineDay>> loadRoutineDays() {
        return mDatabase.routineDayDao().getAllRoutineDays();
    }

    public LiveData<RoutineDay> loadRoutineDay (final int routineDayId) {
        return mDatabase.routineDayDao().getRoutineDay(routineDayId);
    }

    public LiveData<List<RoutineDay>> loadAllRoutineDaysInRoutine (final int routineId) {
        return mDatabase.routineDayDao().getAllRoutineDaysInRoutine(routineId);
    }

    public LiveData<Integer> loadMostRecentRoutineId() {
        return mDatabase.routineDayDao().getMostRecentRoutineId();
    }

    public LiveData<List<RoutineDay>> loadMostRecentDaysInRoutine (final int routineId, final int numberDays) {
        return mDatabase.routineDayDao().getMostRecentDaysInRoutine(routineId, numberDays);
    }

    public void insertRoutineDays (final List<RoutineDay> routineDays) {
        mDatabase.routineDayDao().insertAllRoutineDays(routineDays);
    }

    public void insertRoutineDay (final RoutineDay routineDay) {
        mDatabase.routineDayDao().insertRoutineDay(routineDay );
    }

    /**
     * Exercise queries
     */
    public LiveData<List<Exercise>> loadExercises() {
        return mDatabase.exerciseDao().getAllExercises();
    }

    public LiveData<Exercise> loadExercise (final int exerciseId) {
        return mDatabase.exerciseDao().getExercise(exerciseId);
    }

    public LiveData<List<Exercise>> loadAllExercisesInRoutineDay (final int routineDayId) {
        return mDatabase.exerciseDao().getAllExercisesInRoutineDay(routineDayId);
    }

    public LiveData<List<Exercise>> loadFirstNExercisesInRoutineDay (final int routineDayId, final int numberExercises) {
        return mDatabase.exerciseDao().getFirstNExercisesInRoutineDay(routineDayId, numberExercises);
    }

    public void insertExercises (final List<Exercise> exercises) {
        mDatabase.exerciseDao().insertAllExercises(exercises);
    }

    public void insertExercise (final Exercise exercise) {
        mDatabase.exerciseDao().insertExercise(exercise);
    }

    /**
     * ReppedSet queries
     */
    public LiveData<List<ReppedSet>> loadReppedSets() {
        return mDatabase.reppedSetDao().getAllReppedSets();
    }

    public LiveData<ReppedSet> loadReppedSet (final int reppedSetId) {
        return mDatabase.reppedSetDao().getReppedSet(reppedSetId);
    }

    public LiveData<List<ReppedSet>> loadAllReppedExerciseSets(final int exerciseId) {
        return mDatabase.reppedSetDao().getAllReppedExerciseSets(exerciseId);
    }

    public void insertReppedSets (final List<ReppedSet> reppedSets) {
        mDatabase.reppedSetDao().insertAllReppedSets(reppedSets);
    }

    public void insertReppedSet (final ReppedSet reppedSet) {
        mDatabase.reppedSetDao().insertReppedSet(reppedSet);
    }

    /**
     * TimedSet queries
     */
    public LiveData<List<TimedSet>> loadTimedSets() {
        return mDatabase.timedSetDao().getAllTimedSets();
    }

    public LiveData<TimedSet> loadTimedSet (final int timedSetId) {
        return mDatabase.timedSetDao().getTimedSet(timedSetId);
    }

    public LiveData<List<TimedSet>> loadAllTimedExerciseSets(final int exerciseId) {
        return mDatabase.timedSetDao().getAllTimedExerciseSets(exerciseId);
    }

    public void insertTimedSets (final List<TimedSet> timedSets) {
        mDatabase.timedSetDao().insertAllTimedSets(timedSets);
    }

    public void insertTimedSet (final TimedSet timedSet) {
        mDatabase.timedSetDao().insertTimedSet(timedSet);
    }

}
