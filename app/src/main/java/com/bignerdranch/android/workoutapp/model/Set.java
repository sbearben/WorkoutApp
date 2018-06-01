package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.SetTable;

import java.util.UUID;

public abstract class Set<T> implements Copyable<Set> {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = SetTable.Cols.SET_ID)
    private int id;

    @ColumnInfo(name = SetTable.Cols.PARENT_EXERCISE_ID)
    private int exerciseId;

    @ColumnInfo(name = SetTable.Cols.SET_NUM)
    private int setNumber;

    @ColumnInfo(name = SetTable.Cols.SET_TARGET_WEIGHT)
    private int targetWeight;

    @ColumnInfo(name = SetTable.Cols.SET_TARGET_MEASUREMENT)
    private T targetMeasurement; // Measurement is simply how success of the set is measured (for barbell exercises this is reps, for others such as planks this is timed)

    @ColumnInfo(name = SetTable.Cols.SET_ACTUAL_MEASUREMENT)
    private T actualMeasurement;


    public static Set newDefaultInstance(int exerciseId, @Exercise.ExerciseType String exerciseType, int setNumber, int targetWeight) {
        switch(exerciseType) {
            case Exercise.REPPED:
                return ReppedSet.createNewDefaultSet(exerciseId, setNumber, targetWeight);
            case Exercise.TIMED:
                return TimedSet.createNewDefaultSet(exerciseId, setNumber, targetWeight);
            default:
                return null;
        }
    }

    public static Set newDefaultInstance(@Exercise.ExerciseType String exerciseType, int setNumber, int targetWeight) {
        switch(exerciseType) {
            case Exercise.REPPED:
                return ReppedSet.createNewDefaultSet(setNumber, targetWeight);
            case Exercise.TIMED:
                return TimedSet.createNewDefaultSet(setNumber, targetWeight);
            default:
                return null;
        }
    }

    public Set() {
    }

    public Set (int id, int exerciseId, int setNumber, int targetWeight, T targetMeasurement, T actualMeasurement) {
        this.id = id;
        this.exerciseId = exerciseId;
        this.setNumber = setNumber;
        this.targetWeight = targetWeight;
        this.targetMeasurement = targetMeasurement;
        this.actualMeasurement = actualMeasurement;
    }

    public Set (int exerciseId, int setNumber, int targetWeight, T targetMeasurement, T actualMeasurement) {
        this.exerciseId = exerciseId;
        this.setNumber = setNumber;
        this.targetWeight = targetWeight;
        this.targetMeasurement = targetMeasurement;
        this.actualMeasurement = actualMeasurement;
    }

    public Set (int setNumber, int targetWeight, T targetMeasurement, T actualMeasurement) {
        this.setNumber = setNumber;
        this.targetWeight = targetWeight;
        this.targetMeasurement = targetMeasurement;
        this.actualMeasurement = actualMeasurement;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExerciseId() {
        return this.exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getSetNumber() {
        return this.setNumber;
    }

    public void setSetNumber (int setNumber) {
        this.setNumber = setNumber;
    }

    public int getTargetWeight() {
        return this.targetWeight;
    }

    public void setTargetWeight (int targetWeight) {
        this.targetWeight = targetWeight;
    }

    public T getTargetMeasurement() {
        return this.targetMeasurement;
    }

    public void setTargetMeasurement (T targetMeasurement) {
        this.targetMeasurement = targetMeasurement;
    }

    public T getActualMeasurement() {
        return this.actualMeasurement;
    }

    public void setActualMeasurement(T actualMeasurement) {
        this.actualMeasurement = actualMeasurement;
    }

    // Abstract method that subclasses implement in order to properly print their mActualMeasurement fields
    public abstract String actualMeasurementString();

    // Abstract method that subclasses implement in order to properly print their mTargetMeasurement fields
    public abstract String targetMeasurementString();

    // Abstract method that returns a boolean based on whether the set is null (was performed/skipped) or not
    // - if this returns true then the set was skipped, if not then the set was performed
    public abstract boolean isSetNull();

    // Abstract method to set the actuMeasurement field of our Set to its null value (whatever we may be using to represent that for each set type)
    public abstract void setActualMeasurementNull();

    public abstract Set createCopy();

    public abstract Set createDeepCopy();

    @Override
    public String toString() {
        return "Set: " + getId() + ", Parent Exercise Id: " + getExerciseId() + "\n" +
                "\t" + "Set Number: " + getSetNumber() + ", Target Weight: " + getTargetWeight() + "\n" +
                "\t" + "Target Measurement: " + getTargetMeasurement().toString() + ", Actual Measurement: " + getActualMeasurement().toString() + "\n";
    }
}
