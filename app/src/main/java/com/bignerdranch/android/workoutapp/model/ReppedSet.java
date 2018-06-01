package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.SetTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ReppedSetTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ExerciseTable;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.ForeignKey.RESTRICT;


@Entity(tableName = ReppedSetTable.NAME,
        foreignKeys = { @ForeignKey(
                entity = Exercise.class,
                parentColumns = ExerciseTable.Cols.EXERCISE_ID,
                childColumns = SetTable.Cols.PARENT_EXERCISE_ID,
                onDelete = CASCADE,
                onUpdate = RESTRICT)},
        indices = { @Index(value = SetTable.Cols.PARENT_EXERCISE_ID)}
        )
public class ReppedSet extends Set<Integer> {

    public static final int MIN_TARGET_REPS = 1;
    public static final int MAX_TARGET_REPS = 100;
    public static final int ACTUAL_REPS_NULL = -1;

    @Ignore
    public ReppedSet() {
    }

    public ReppedSet (int id, int exerciseId, int setNumber, int targetWeight, Integer targetMeasurement, Integer actualMeasurement) {
        super(id, exerciseId, setNumber, targetWeight, targetMeasurement, actualMeasurement);
    }

    @Ignore
    public ReppedSet (int exerciseId, int setNumber, int targetWeight, Integer targetMeasurement, Integer actualMeasurement) {
        super(exerciseId, setNumber, targetWeight, targetMeasurement, actualMeasurement);
    }

    @Ignore
    public ReppedSet (int setNumber, int targetWeight, Integer targetMeasurement, Integer actualMeasurement) {
        super(setNumber, targetWeight, targetMeasurement, actualMeasurement);
    }

    @Override
    public Set createCopy() {
        return new ReppedSet(this.getExerciseId(), this.getSetNumber(), this.getTargetWeight(), this.getTargetMeasurement(), this.getActualMeasurement());
    }

    @Override
    public Set createDeepCopy() {
        return createCopy();
    }

    public static Set createNewDefaultSet (int exerciseId, int setNumber, int targetWeight) {
        return new ReppedSet (exerciseId, setNumber, targetWeight, 8, ReppedSet.ACTUAL_REPS_NULL);

    }

    public static Set createNewDefaultSet (int setNumber, int targetWeight) {
        return new ReppedSet (setNumber, targetWeight, 8, ReppedSet.ACTUAL_REPS_NULL);
    }

    @Override
    public String actualMeasurementString() {
        return getActualMeasurement() + "";
    }

    @Override
    public String targetMeasurementString() {
        return getTargetMeasurement() + "";
    }

    @Override
    public boolean isSetNull() {
        return getActualMeasurement() == ACTUAL_REPS_NULL;
    }

    @Override
    public void setActualMeasurementNull() {
        setActualMeasurement(ACTUAL_REPS_NULL);
    }

    @Override
    public String toString() {
        return "ReppedSet: " + getId() + ", Parent Exercise Id: " + getExerciseId() + "\n" +
                "\t" + "Set Number: " + getSetNumber() + ", Target Weight: " + getTargetWeight() + "\n" +
                "\t" + "Target Reps: " + getTargetMeasurement() + ", Actual Reps: " + getActualMeasurement() + "\n";
    }
}
