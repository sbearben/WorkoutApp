package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.ExerciseTable;
import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.RoutineDayTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity(tableName = ExerciseTable.NAME,
        foreignKeys = { @ForeignKey(
                entity = RoutineDay.class,
                parentColumns = RoutineDayTable.Cols.ROUTINE_DAY_ID,
                childColumns = ExerciseTable.Cols.PARENT_ROUTINE_DAY_ID)},
        indices = { @Index(value = ExerciseTable.Cols.PARENT_ROUTINE_DAY_ID)}
        )
public class Exercise {

    public static final String REPPED = "repped";
    public static final String TIMED = "timed";
    public static final int MAX_SETS = 5;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_ID)
    private int id;

    @ColumnInfo(name = ExerciseTable.Cols.PARENT_ROUTINE_DAY_ID)
    private int routineDayId;

    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_NAME)
    private String name;

    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_NUM)
    private int number; // is the number of the exercise in the routine - might not be needed

    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_NUM_SETS)
    private int mTargetNumberSets;

    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_TYPE)
    private String type;

    @Ignore
    private List<Set> sets = null;


    /*public Exercise (String name, String type, int number) {
        this(UUID.randomUUID(), name, type, number);
    }*/

    @Ignore
    public Exercise() {
    }

    public Exercise (int id, int routineDayId, String name, int number, String type) {
        //mNumberSets = 3;
        this.id = id;
        this.routineDayId = routineDayId;
        this.name = name;
        this.number = number;
        this.type = type;

        this.sets = new ArrayList<>();
        //initializeSets();
    }

    @Ignore
    public Exercise (int routineDayId, String name, int number, String type) {
        //mNumberSets = 3;
        this.routineDayId = routineDayId;
        this.name = name;
        this.number = number;
        this.type = type;

        //initializeSets();
    }

    private void initializeSets() {
        // Initialize our 5 sets
        this.sets = new ArrayList<>();
        for (int i=0; i<MAX_SETS; i++) {
            this.sets.add(Set.newInstance(id, this.type, i+1));
        }
    }


    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoutineDayId() {
        return this.routineDayId;
    }

    public void setRoutineDayId(int routineDayId) {
        this.routineDayId = routineDayId;
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

    public int getTargetNumberSets() {
        return mTargetNumberSets;
    }

    public void setTargetNumberSets(int targetNumberSets) {
        this.mTargetNumberSets = targetNumberSets;
    }

    public String getType() {
        return this.type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public List<Set> getSets() {
        return this.sets;
    }

    public Set getSet (int id) {
        for (Set set : this.sets) {
            if (set.getId() == id) {
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

    public void addSet (Set set) {
        this.sets.add(set);
    }

    public void addSets (List<Set> sets) {
        this.sets.addAll(sets);
    }
}
