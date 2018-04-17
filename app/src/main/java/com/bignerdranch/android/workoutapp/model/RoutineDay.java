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


@Entity(tableName = RoutineDayTable.NAME,
        foreignKeys = @ForeignKey(
                entity = Routine.class,
                parentColumns = RoutineTable.Cols.ROUTINE_ID,
                childColumns = RoutineDayTable.Cols.ROUTINE_DAY_ID
        ))
public class RoutineDay {

    @PrimaryKey
    @ColumnInfo(name = RoutineDayTable.Cols.ROUTINE_DAY_ID)
    private UUID id;

    @ColumnInfo(name = RoutineDayTable.Cols.ROUTINE_DAY_NUM)
    private int dayNumber;

    @ColumnInfo(name = RoutineDayTable.Cols.ROUTINE_DAY_DATE)
    private Date date; // date of the workout day

    @Ignore
    private List<Exercise> exercises;


    public RoutineDay(int dayNumber) {
        this(UUID.randomUUID(), dayNumber);
    }

    public RoutineDay (UUID id, int dayNumber) {
        this.id = id;
        this.dayNumber = dayNumber;
        this.date = new Date();
    }

    public UUID getId() {
        return this.id;
    }

    public int getDayNumber() {
        return this.dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void addExercise (Exercise e) {
        this.exercises.add(e);
    }

    public void removeExercise (Exercise e) {
        this.exercises.remove(e);
    }

    public List<Exercise> getExercises() {
        return this.exercises;
    }

    public Exercise getExercise (UUID id) {
        for (Exercise exercise : this.exercises) {
            if (exercise.getId().equals(id)) {
                return exercise;
            }
        }
        return null;
    }

}
