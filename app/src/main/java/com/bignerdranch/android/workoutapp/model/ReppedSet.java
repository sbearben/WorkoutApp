package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.SetTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ReppedSetTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ExerciseTable;


@Entity(tableName = ReppedSetTable.NAME,
        foreignKeys = @ForeignKey(
                entity = Exercise.class,
                parentColumns = ExerciseTable.Cols.EXERCISE_ID,
                childColumns = SetTable.Cols.PARENT_EXERCISE_ID
        ))
public class ReppedSet extends Set<Integer> {

    public ReppedSet() {
    }

    public ReppedSet (int id, int exerciseId, int setNumber, int targetWeight, Integer targetMeasurement, Integer actualMeasurement) {
        super(id, exerciseId, setNumber, targetWeight, targetMeasurement, actualMeasurement);
    }

    public ReppedSet (int exerciseId, int setNumber) {
        super(exerciseId, setNumber, 45);
        this.targetMeasurement = 8;
    }
}
