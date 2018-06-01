package com.bignerdranch.android.workoutapp.global;

import com.bignerdranch.android.workoutapp.database.AppDatabase;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.ReppedSet;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;
import com.bignerdranch.android.workoutapp.model.TimedSet;

import java.util.ArrayList;
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
    public List<Routine> loadRoutines() {
        return mDatabase.routineDao().getAllRoutines();
    }

    public Routine loadRoutine (final int routineId) {
        return mDatabase.routineDao().getRoutine(routineId);
    }

    public List<String> loadAllRoutineNames() {
        return mDatabase.routineDao().getAllRoutineNames();
    }

    public void insertRoutines (final List<Routine> routines) {
        mDatabase.routineDao().insertAllRoutines(routines);
    }

    public long insertRoutine (final Routine routine) {
        return mDatabase.routineDao().insertRoutine(routine);
    }

    public void deleteRoutine (final Routine routine) {
        mDatabase.routineDao().deleteRoutine(routine);
    }

    public void deleteRoutines (final Routine... routines) {
        mDatabase.routineDao().deleteRoutines(routines);
    }

    /**
     * RoutineDay queries
     */
    public List<RoutineDay> loadRoutineDays() {
        return mDatabase.routineDayDao().getAllRoutineDays();
    }

    public RoutineDay loadRoutineDay (final int routineDayId) {
        return mDatabase.routineDayDao().getRoutineDay(routineDayId);
    }

    public List<RoutineDay> loadAllCompletedRoutineDaysInRoutine (final int routineId) {
        return mDatabase.routineDayDao().getAllCompletedRoutineDaysInRoutine(routineId);
    }

    public int loadMostRecentRoutineId() {
        return mDatabase.routineDayDao().getMostRecentRoutineId();
    }

    public List<RoutineDay> loadMostRecentDaysInRoutine (final int routineId, final int numberDays) {
        return mDatabase.routineDayDao().getMostRecentDaysInRoutine(routineId, numberDays);
    }

    public List<RoutineDay> loadOngoingDaysInRoutine (final int routineId) {
        return mDatabase.routineDayDao().getOngoingDaysInRoutine(routineId);
    }

    public List<RoutineDay> loadTemplateRoutineDays(final int routineId) {
        return mDatabase.routineDayDao().getTemplateRoutineDays(routineId);
    }

    public void insertRoutineDays (final List<RoutineDay> routineDays) {
        mDatabase.routineDayDao().insertAllRoutineDays(routineDays);
    }

    public long insertRoutineDay (final RoutineDay routineDay) {
        return mDatabase.routineDayDao().insertRoutineDay(routineDay );
    }

    public void updateRoutineDay (final RoutineDay routineDay) {
        mDatabase.routineDayDao().updateRoutineDay(routineDay);
    }

    public void deleteRoutineDay (final RoutineDay routineDay) {
        mDatabase.routineDayDao().deleteRoutineDay(routineDay);
    }

    public void deleteRoutineDays (final RoutineDay... routineDays) {
        mDatabase.routineDayDao().deleteRoutineDays(routineDays);
    }

    /**
     * Exercise queries
     */
    public List<Exercise> loadExercises() {
        return mDatabase.exerciseDao().getAllExercises();
    }

    public Exercise loadExercise (final int exerciseId) {
        return mDatabase.exerciseDao().getExercise(exerciseId);
    }

    public List<Exercise> loadAllExercisesInRoutineDay (final int routineDayId) {
        return mDatabase.exerciseDao().getAllExercisesInRoutineDay(routineDayId);
    }

    public List<Exercise> loadFirstNExercisesInRoutineDay (final int routineDayId, final int numberExercises) {
        return mDatabase.exerciseDao().getFirstNExercisesInRoutineDay(routineDayId, numberExercises);
    }

    public long[] insertExercises (final List<Exercise> exercises) {
        return mDatabase.exerciseDao().insertAllExercises(exercises);
    }

    public long insertExercise (final Exercise exercise) {
        return mDatabase.exerciseDao().insertExercise(exercise);
    }

    public void updateExercise (final Exercise exercise) {
        mDatabase.exerciseDao().updateExercise(exercise);
    }

    public void deleteExercise (final Exercise exercise) {
        mDatabase.exerciseDao().deleteExercise(exercise);
    }

    public void deleteExercises (final Exercise... exercises) {
        mDatabase.exerciseDao().deleteExercises(exercises);
    }

    /**
     * ReppedSet queries
     */
    public List<ReppedSet> loadReppedSets() {
        return mDatabase.reppedSetDao().getAllReppedSets();
    }

    public ReppedSet loadReppedSet (final int reppedSetId) {
        return mDatabase.reppedSetDao().getReppedSet(reppedSetId);
    }

    public List<ReppedSet> loadAllReppedExerciseSets(final int exerciseId) {
        return mDatabase.reppedSetDao().getAllReppedExerciseSets(exerciseId);
    }

    public long[] insertReppedSets (final List<ReppedSet> reppedSets) {
        return mDatabase.reppedSetDao().insertAllReppedSets(reppedSets);
    }

    public long insertReppedSet (final ReppedSet reppedSet) {
        return mDatabase.reppedSetDao().insertReppedSet(reppedSet);
    }

    public void deleteReppedSet (final ReppedSet reppedSet) {
        mDatabase.reppedSetDao().deleteReppedSet(reppedSet);
    }

    public void deleteReppedSets (final ReppedSet... reppedSets) {
        mDatabase.reppedSetDao().deleteReppedSets(reppedSets);
    }

    /**
     * TimedSet queries
     */
    public List<TimedSet> loadTimedSets() {
        return mDatabase.timedSetDao().getAllTimedSets();
    }

    public TimedSet loadTimedSet (final int timedSetId) {
        return mDatabase.timedSetDao().getTimedSet(timedSetId);
    }

    public List<TimedSet> loadAllTimedExerciseSets(final int exerciseId) {
        return mDatabase.timedSetDao().getAllTimedExerciseSets(exerciseId);
    }

    public long[] insertTimedSets (final List<TimedSet> timedSets) {
        return mDatabase.timedSetDao().insertAllTimedSets(timedSets);
    }

    public long insertTimedSet (final TimedSet timedSet) {
        return mDatabase.timedSetDao().insertTimedSet(timedSet);
    }

    public void deleteTimedSet (final TimedSet timedSet) {
        mDatabase.timedSetDao().deleteTimedSet(timedSet);
    }

    public void deleteTimedSets (final TimedSet... timedSets) {
        mDatabase.timedSetDao().deleteTimedSets(timedSets);
    }

}
