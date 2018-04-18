package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.SetTable;

import java.util.UUID;

public abstract class Set<T> {

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
    protected T targetMeasurement; // Measurement is simply how success of the set is measured (for barbell exercises this is reps, for others such as planks this is timed)

    @ColumnInfo(name = SetTable.Cols.SET_ACTUAL_MEASUREMENT)
    private T actualMeasurement;


    public static Set newInstance (int exerciseId, String type, int setNumber) {
        switch(type) {
            case Exercise.REPPED:
                return new ReppedSet(exerciseId, setNumber);
            case Exercise.TIMED:
                return new TimedSet(exerciseId, setNumber);
            default:
                return null;
        }
    }

    /*public Set (int setNumber, int targetWeight) {
        this (UUID.randomUUID(), setNumber, targetWeight);
    }*/

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

    public Set (int exerciseId, int setNumber, int targetWeight) {
        this.exerciseId = exerciseId;
        this.setNumber = setNumber;
        this.targetWeight = targetWeight;
        this.actualMeasurement = null;
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
}
