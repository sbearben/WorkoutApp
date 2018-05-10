package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.SetTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.TimedSetTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ExerciseTable;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.ForeignKey.RESTRICT;


@Entity(tableName = TimedSetTable.NAME,
        foreignKeys = { @ForeignKey(
                entity = Exercise.class,
                parentColumns = ExerciseTable.Cols.EXERCISE_ID,
                childColumns = SetTable.Cols.PARENT_EXERCISE_ID,
                onDelete = CASCADE,
                onUpdate = RESTRICT)},
        indices = { @Index(value = SetTable.Cols.PARENT_EXERCISE_ID)}
        )
public class TimedSet extends Set<Date> {

    @Ignore
    public TimedSet() {
    }

    @Ignore
    public TimedSet (int id, int exerciseId, int setNumber, int targetWeight, Date targetMeasurement, Date actualMeasurement) {
        super(id, exerciseId, setNumber, targetWeight, targetMeasurement, actualMeasurement);
    }

    public TimedSet (int exerciseId, int setNumber, int targetWeight, Date targetMeasurement, Date actualMeasurement) {
        super(exerciseId, setNumber, targetWeight, targetMeasurement, actualMeasurement);
    }

    @Override
    public String actualMeasurementString() {
        return dateToString(getActualMeasurement());
    }

    @Override
    public String targetMeasurementString() {
        return dateToString(getTargetMeasurement());
    }

    @Override
    public boolean isSetNull() {
        return false; // TODO: Implement this properly
    }

    private String dateToString (Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        return minutes + ":" + seconds;
    }

    /*
    @Ignore
    public TimedSet (int exerciseId, int setNumber) {
        super(exerciseId, setNumber, 0);
        this.targetMeasurement = new GregorianCalendar(0, 0, 0, 0, 0, 30).getTime();
    }
    */
}
