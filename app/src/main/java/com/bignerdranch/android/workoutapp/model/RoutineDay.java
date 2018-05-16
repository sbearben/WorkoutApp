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

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.ForeignKey.RESTRICT;


@Entity(tableName = RoutineDayTable.NAME,
        foreignKeys = { @ForeignKey(
                entity = Routine.class,
                parentColumns = RoutineTable.Cols.ROUTINE_ID,
                childColumns = RoutineDayTable.Cols.PARENT_ROUTINE_ID,
                onDelete = CASCADE,
                onUpdate = RESTRICT)},
        indices = { @Index(value = RoutineDayTable.Cols.PARENT_ROUTINE_ID)}
        )
public class RoutineDay implements Copyable<RoutineDay> {

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

    @ColumnInfo(name = RoutineDayTable.Cols.ROUTINE_DAY_TEMPLATE)
    private boolean template; // indicates whether this workout day is the "template" for all future days (of the respective day number) of that routine

    @Ignore
    private List<Exercise> exercises = null;


    @Ignore
    public RoutineDay() {
        this.exercises = new ArrayList<>();
    }

    public RoutineDay (int id, int routineId, int dayNumber, Date date, boolean completed, boolean template) {
        this.id = id;
        this.routineId = routineId;
        this.dayNumber = dayNumber;
        this.date = date;
        this.completed = completed;
        this.template = template;
        this.exercises = new ArrayList<>();
    }

    @Ignore
    public RoutineDay (int routineId, int dayNumber, Date date, boolean completed, boolean template) {
        this.routineId = routineId;
        this.dayNumber = dayNumber;
        this.date = date;
        this.completed = completed;
        this.template = template;
        this.exercises = new ArrayList<>();
    }

    // A copy means a new instance of RoutineDay with all the same fields, except its ID is uninitialized
    @Override
    public RoutineDay createCopy() {
        return new RoutineDay(this.routineId, this.dayNumber, this.date, this.completed, this.template);
    }

    @Override
    public RoutineDay createDeepCopy() {
        RoutineDay routineDay = this.createCopy();
        for (Exercise exercise : this.getExercises()) {
            routineDay.addExercise(exercise.createDeepCopy());
        }

        return routineDay;
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

    public boolean isTemplate() {
        return this.template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
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

    // Method that returns a boolean depending on if any of the Sets in the Exercises of the RoutineDay are "non-null" aka have any sets been performed or is this a new RoutineDau
    public boolean isStarted() {
        for (Exercise exercise : this.getExercises()) {
            for (Set exerciseSet : exercise.getSets()) {
                if (!exerciseSet.isSetNull())
                    return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        String str = "";

        str += "RoutineDay: " + getId() + "\n" +
                "\t" + "Parent Routine Id: " + getRoutineId() + ", day#: " + getDayNumber() + ", date: " + (getDate() != null ? getDate().toString() : "") + "\n";
        if (exercises != null) {
            for (Exercise exercise : exercises) {
                str += exercise.toString();
            }
        }

        return str;
    }

}
