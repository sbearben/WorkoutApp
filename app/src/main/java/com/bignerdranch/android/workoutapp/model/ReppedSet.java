package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.SetTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ReppedSetTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ExerciseTable;


@Entity(tableName = ReppedSetTable.NAME,
        foreignKeys = { @ForeignKey(
                entity = Exercise.class,
                parentColumns = ExerciseTable.Cols.EXERCISE_ID,
                childColumns = SetTable.Cols.PARENT_EXERCISE_ID)},
        indices = { @Index(value = SetTable.Cols.PARENT_EXERCISE_ID)}
        )
public class ReppedSet extends Set<Integer> {

    public static final int ACTUAL_REPS_NULL = -1;

    @Ignore
    public ReppedSet() {
        setActualMeasurement (ACTUAL_REPS_NULL);
    }

    public ReppedSet (int id, int exerciseId, int setNumber, int targetWeight, Integer targetMeasurement, Integer actualMeasurement) {
        super(id, exerciseId, setNumber, targetWeight, targetMeasurement, actualMeasurement);
    }

    @Ignore
    public ReppedSet (int exerciseId, int setNumber) {
        super(exerciseId, setNumber, 45);
        this.targetMeasurement = 8;
    }

    @Override
    public String toString() {
        return "ReppedSet: " + getId() + ", Parent Exercise Id: " + getExerciseId() + "\n" +
                "\t" + "Set Number: " + getSetNumber() + ", Target Weight: " + getTargetWeight() + "\n" +
                "\t" + "Target Reps: " + getTargetMeasurement() + ", Actual Reps: " + getActualMeasurement() + "\n";
    }
}
