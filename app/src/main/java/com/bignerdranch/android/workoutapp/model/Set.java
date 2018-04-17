package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.PrimaryKey;

import java.util.UUID;

public abstract class Set<T> {

    @PrimaryKey

    private UUID mId;
    private int mSetNumber;
    private int mTargetWeight;
    protected T mTargetMeasurement; // Measurement is simply how success of the set is measured (for barbell exercises this is reps, for others such as planks this is timed)
    private T mActualMeasurement;


    public static Set newInstance (String type, int setNumber) {
        switch(type) {
            case Exercise.REPPED:
                return new ReppedSet(setNumber);
            case Exercise.TIMED:
                return new TimedSet(setNumber);
            default:
                return null;
        }
    }

    public Set (int setNumber, int targetWeight) {
        this (UUID.randomUUID(), setNumber, targetWeight);
    }

    public Set (UUID id, int setNumber, int targetWeight) {
        mSetNumber = setNumber;
        mTargetWeight = targetWeight;
        mActualMeasurement = null;
    }

    public UUID getId() {
        return mId;
    }

    public int getSetNumber() {
        return mSetNumber;
    }

    public void setSetNumber (int setNumber) {
        mSetNumber = setNumber;
    }

    public int getTargetWeight() {
        return mTargetWeight;
    }

    public void setTargetWeight (int targetWeight) {
        mTargetWeight = targetWeight;
    }

    public T getTargetMeasurement() {
        return mTargetMeasurement;
    }

    public void setTargetMeasurement (T targetMeasurement) {
        mTargetMeasurement = targetMeasurement;
    }

    public T getActualMeasurement() {
        return mActualMeasurement;
    }

    public void setActualMeasurement(T actualMeasurement) {
        mActualMeasurement = actualMeasurement;
    }
}
