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

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.ForeignKey.RESTRICT;


@Entity(tableName = ExerciseTable.NAME,
        foreignKeys = { @ForeignKey(
                entity = RoutineDay.class,
                parentColumns = RoutineDayTable.Cols.ROUTINE_DAY_ID,
                childColumns = ExerciseTable.Cols.PARENT_ROUTINE_DAY_ID,
                onDelete = CASCADE,
                onUpdate = RESTRICT)},
        indices = { @Index(value = ExerciseTable.Cols.PARENT_ROUTINE_DAY_ID)}
        )
public class Exercise implements Copyable<Exercise> {

    public static final String REPPED = "Repped";
    public static final String TIMED = "Timed";

    public static final int MAX_SETS = 5;
    public static final int DEFAULT_SETS = 3;

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
    private int targetNumberSets;

    @ColumnInfo(name = ExerciseTable.Cols.EXERCISE_TYPE)
    private String type;

    @Ignore
    private List<Set> sets = null;


    @Ignore
    public Exercise() {
        this.sets = new ArrayList<>();
    }

    public Exercise (int id, int routineDayId, String name, int number, int targetNumberSets, String type) {
        //mNumberSets = 3;
        this.id = id;
        this.routineDayId = routineDayId;
        this.name = name;
        this.number = number;
        this.targetNumberSets = targetNumberSets;
        this.type = type;

        this.sets = new ArrayList<>();
        //initializeSets();
    }

    @Ignore
    public Exercise (int routineDayId, String name, int number, int targetNumberSets, String type) {
        this.routineDayId = routineDayId;
        this.name = name;
        this.number = number;
        this.targetNumberSets = targetNumberSets;
        this.type = type;

        this.sets = new ArrayList<>();

    }

    @Override
    public Exercise createCopy() {
        return new Exercise(this.routineDayId, this.name, this.number, this.targetNumberSets, this.type);
    }

    @Override
    public Exercise createDeepCopy() {
        Exercise exercise = this.createCopy();
        for (Set exerciseSet : this.getSets()) {
            exercise.addSet(exerciseSet.createCopy());
        }

        return exercise;
    }

    /*private void initializeSets() {
        // Initialize our 5 sets
        this.sets = new ArrayList<>();
        for (int i=0; i<MAX_SETS; i++) {
            this.sets.add(Set.newInstance(id, this.type, i+1));
        }
    }*/


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
        return targetNumberSets;
    }

    public void setTargetNumberSets(int targetNumberSets) {
        this.targetNumberSets = targetNumberSets;
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

    @Override
    public String toString() {
        String str = "";

        str += "Exercise: " + getId() + ", Name: " + getName() + "\n" +
                "\t" + "Parent RoutineDay Id: " + getRoutineDayId() + ", Exercise#: " + getNumber() + "\n" +
                "\t" + "Target # sets: " + getTargetNumberSets() + ", Type: " + getType() + "\n";
        if (sets != null) {
            for (Set set : sets) {
                str += set.toString();
            }
        }

        return str;
    }

    // Static method to make sure a given string is a valid exercise type (should maybe be keeping types in a final List?)
    public static boolean isValidExerciseType(String type) {
        return type.equals(Exercise.REPPED) || type.equals(Exercise.TIMED);
    }
}
