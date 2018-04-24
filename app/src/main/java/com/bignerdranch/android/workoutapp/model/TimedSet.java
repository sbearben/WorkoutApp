package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;

import java.util.Date;
import java.util.GregorianCalendar;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.SetTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.TimedSetTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ExerciseTable;


@Entity(tableName = TimedSetTable.NAME,
        foreignKeys = { @ForeignKey(
                entity = Exercise.class,
                parentColumns = ExerciseTable.Cols.EXERCISE_ID,
                childColumns = SetTable.Cols.PARENT_EXERCISE_ID)},
        indices = { @Index(value = SetTable.Cols.PARENT_EXERCISE_ID)}
        )
public class TimedSet extends Set<Date> {

    @Ignore
    public TimedSet() {
    }

    public TimedSet (int id, int exerciseId, int setNumber, int targetWeight, Date targetMeasurement, Date actualMeasurement) {
        super(id, exerciseId, setNumber, targetWeight, targetMeasurement, actualMeasurement);
    }

    @Ignore
    public TimedSet (int exerciseId, int setNumber) {
        super(exerciseId, setNumber, 0);
        this.targetMeasurement = new GregorianCalendar(0, 0, 0, 0, 0, 30).getTime();
    }
}
