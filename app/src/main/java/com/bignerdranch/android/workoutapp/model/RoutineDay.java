package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.RoutineDayTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.RoutineTable;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity (tableName = RoutineDayTable.NAME,
         foreignKeys = @ForeignKey(entity = Routine.class,
                                   parentColumns = RoutineTable.Cols.ROUTINE_ID,
                                   childColumns = RoutineDayTable.Cols.ROUTINE_DAY_ID))
public class RoutineDay {

    @PrimaryKey
    @ColumnInfo(name = RoutineDayTable.Cols.ROUTINE_DAY_ID)
    private UUID mId;

    @ColumnInfo(name = RoutineDayTable.Cols.ROUTINE_DAY_NUM)
    private int mDayNumber;

    @ColumnInfo(name = RoutineDayTable.Cols.ROUTINE_DAY_DATE)
    private Date mDate; // date of the workout day

    @Ignore
    private List<Exercise> mExercises;


    public RoutineDay(int dayNumber) {
        this(UUID.randomUUID(), dayNumber);
    }

    public RoutineDay (UUID id, int dayNumber) {
        mId = id;
        mDayNumber = dayNumber;
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public int getDayNumber() {
        return mDayNumber;
    }

    public void setDayNumber(int dayNumber) {
        mDayNumber = dayNumber;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public void addExercise (Exercise e) {
        mExercises.add(e);
    }

    public void removeExercise (Exercise e) {
        mExercises.remove(e);
    }

    public List<Exercise> getExercises() {
        return mExercises;
    }

    public Exercise getExercise (UUID id) {
        for (Exercise exercise : mExercises) {
            if (exercise.getId().equals(id)) {
                return exercise;
            }
        }
        return null;
    }

}
