package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ReppedSetTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ExerciseTable;

import java.util.UUID;


@Entity (tableName = ReppedSetTable.NAME,
        foreignKeys = @ForeignKey(entity = Exercise.class,
                parentColumns = ExerciseTable.Cols.EXERCISE_ID,
                childColumns = ReppedSetTable.Cols.REPPED_EXERCISE_ID))
public class ReppedSet extends Set<Integer> {

    public ReppedSet (int setNumber) {
        super(setNumber, 45);
        mTargetMeasurement = 8;
    }
}
