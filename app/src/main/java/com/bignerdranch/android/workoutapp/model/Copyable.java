package com.bignerdranch.android.workoutapp.model;

public interface Copyable<T> {

    public T createCopy();
    public T createDeepCopy();
}
