package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ExerciseTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.RoutineDayTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity(tableName = ExerciseTable.NAME,
        foreignKeys = @ForeignKey(
                entity = RoutineDay.class,
                parentColumns = RoutineDayTable.Cols.ROUTINE_DAY_ID,
                childColumns = ExerciseTable.Cols.EXERCISE_ID
        ))
public abstract class Exercise {

    public static final String REPPED = "repped";
    public static final String TIMED = "timed";
    public static final int MAX_SETS = 5;

    @PrimaryKey
    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_ID)
    private UUID id;

    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_NAME)
    private String name;

    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_NUM)
    private int number; // is the number of the exercise in the routine - might not be needed
    //private int mNumberSets;

    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_TYPE)
    private String type;

    @Ignore
    private List<Set> sets;


    public Exercise (String name, String type, int number) {
        this(UUID.randomUUID(), name, type, number);
    }

    public Exercise (UUID id, String name, String type, int number) {
        this.id = id;
        //mNumberSets = 3;
        this.name = name;
        this.type = type;
        this.number = number;
        this.sets = new ArrayList<>();

        // Initialize our 5 sets
        for (int i=0; i<MAX_SETS; i++) {
            this.sets.add(Set.newInstance(this.type, i+1));
        }
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    /* public int getNumberSets() {
        return mNumberSets;
    } */

    public String getType() {
        return this.type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public List<Set> getSets() {
        return this.sets;
    }

    public Set getSet (UUID id) {
        for (Set set : this.sets) {
            if (set.getId().equals(id)) {
                return set;
            }
        }
        return null;
    }

    /* public void addSet() {
        Set s = Set.newInstance(mType, mNumberSets);
        mSets.add(s);
        mNumberSets++;
    }

    public void removeSet (Set s) {
        mSets.remove(s.getSetNumber());
        mNumberSets--;
    } */
}
