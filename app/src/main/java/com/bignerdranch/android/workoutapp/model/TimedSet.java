package com.bignerdranch.android.workoutapp.model;

import java.util.Date;
import java.util.GregorianCalendar;

public class TimedSet extends Set<Date> {

    public TimedSet (int setNumber) {
        super(setNumber, 0);
        mTargetMeasurement = new GregorianCalendar(0, 0, 0, 0, 1, 0).getTime();
    }
}
