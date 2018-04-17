package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;

import java.util.Date;
import java.util.GregorianCalendar;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.SetTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.TimedSetTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ExerciseTable;


@Entity(tableName = TimedSetTable.NAME,
        foreignKeys = @ForeignKey(
                entity = Exercise.class,
                parentColumns = ExerciseTable.Cols.EXERCISE_ID,
                childColumns = SetTable.Cols.SET_ID
        ))
public class TimedSet extends Set<Date> {

    public TimedSet (int setNumber) {
        super(setNumber, 0);
        this.targetMeasurement = new GregorianCalendar(0, 0, 0, 0, 0, 30).getTime();
    }
}
