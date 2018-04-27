package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.RoutineDayTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.RoutineTable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity(tableName = RoutineDayTable.NAME,
        foreignKeys = { @ForeignKey(
                entity = Routine.class,
                parentColumns = RoutineTable.Cols.ROUTINE_ID,
                childColumns = RoutineDayTable.Cols.PARENT_ROUTINE_ID)},
        indices = { @Index(value = RoutineDayTable.Cols.PARENT_ROUTINE_ID)}
        )
public class RoutineDay {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = RoutineDayTable.Cols.ROUTINE_DAY_ID)
    private int id;

    @ColumnInfo(name = RoutineDayTable.Cols.PARENT_ROUTINE_ID)
    private int routineId;

    @ColumnInfo(name = RoutineDayTable.Cols.ROUTINE_DAY_NUM)
    private int dayNumber;

    @ColumnInfo(name = RoutineDayTable.Cols.ROUTINE_DAY_DATE)
    private Date date; // date of the workout day

    @ColumnInfo(name = RoutineDayTable.Cols.ROUTINE_DAY_COMPLETED)
    private boolean completed; // indicates whether the current workout day has been submitted as completed/finished

    @Ignore
    private List<Exercise> exercises = null;


    /*public RoutineDay(int dayNumber) {
        this(UUID.randomUUID(), dayNumber);
    }

    public RoutineDay (UUID id, int dayNumber) {
        this.id = id;
        this.dayNumber = dayNumber;
        this.date = new Date();
    }*/

    @Ignore
    public RoutineDay() {
    }

    public RoutineDay (int id, int routineId, int dayNumber, Date date, boolean completed) {
        this.id = id;
        this.routineId = routineId;
        this.dayNumber = dayNumber;
        this.date = date;
        this.completed = completed;
        this.exercises = new ArrayList<>();
    }

    @Ignore
    public RoutineDay (int routineId, int dayNumber) {
        this.routineId = routineId;
        this.dayNumber = dayNumber;
        this.date = new Date();
        this.completed = false;
    }


    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoutineId() {
        return this.routineId;
    }

    public void setRoutineId(int routineId) {
        this.routineId = routineId;
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

    public boolean isCompleted() {
        return this.completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }


    public void addExercise (Exercise e) {
        this.exercises.add(e);
    }

    public void addExercises (List<Exercise> exercises) {
        this.exercises.addAll(exercises);
    }

    public void removeExercise (Exercise e) {
        this.exercises.remove(e);
    }

    public List<Exercise> getExercises() {
        return this.exercises;
    }

    public Exercise getExercise (int id) {
        for (Exercise exercise : this.exercises) {
            if (exercise.getId() == id) {
                return exercise;
            }
        }
        return null;
    }

}
