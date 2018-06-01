package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.RoutineTable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Entity (tableName = RoutineTable.NAME) // Caution - table names in SQLLite are case-INsensitive
public class Routine implements Copyable<Routine> {

    public static final int MAX_ROUTINE_DAYS = 7;


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = RoutineTable.Cols.ROUTINE_ID)
    private int id;
    //private UUID id;

    @ColumnInfo(name = RoutineTable.Cols.ROUTINE_NAME)
    private String name;

    @ColumnInfo(name = RoutineTable.Cols.ROUTINE_DATE_CREATED)
    private Date dateCreated; // date the routine was created

    @Ignore
    private List<RoutineDay> routineDays = null;


    @Ignore
    public Routine() {
        this.routineDays = new ArrayList<>();
    }

    public Routine (int id, String name, Date dateCreated) {
        this.id = id;
        this.name = name;
        this.dateCreated = dateCreated;
        this.routineDays = new ArrayList<>();
    }

    @Ignore
    public Routine (String name, Date dateCreated) {
        this.name = name;
        this.dateCreated = dateCreated;
        this.routineDays = new ArrayList<>();
    }

    // A copy means a new instance of Routine with all the same fields, except its ID is uninitialized
    @Override
    public Routine createCopy() {
        return new Routine(this.name, this.dateCreated);
    }

    @Override
    public Routine createDeepCopy() {
        Routine routine = this.createCopy();
        for (RoutineDay routineDay : this.getRoutineDays())
            routine.addRoutineDay(routineDay.createDeepCopy());

        return routine;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateCreated() {
        return this.dateCreated;
    }

    public void setDateCreated (Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void addRoutineDay (RoutineDay routineDay) {
        this.routineDays.add(routineDay);
    }

    public void addRoutineDays (List<RoutineDay> routineDays) {
        this.routineDays.addAll(routineDays);
    }

    public void removeRoutineDay (RoutineDay routineDay) {
        this.routineDays.remove(routineDay);
    }

    public List<RoutineDay> getRoutineDays() {
        return this.routineDays;
    }

    public RoutineDay getRoutineDay (int id) {
        for (RoutineDay routineDay : this.routineDays) {
            if (routineDay.getId() == id) {
                return routineDay;
            }
        }
        return null;
    }

    public static ArrayList<Integer> createRoutineDayIdList(@NonNull List<RoutineDay> routineDays) {
        ArrayList<Integer> routineDayIdList = new ArrayList<>();

        for (RoutineDay routineDay : routineDays) {
            try {
                routineDayIdList.add(routineDay.getId());
            }
            catch (Exception e) {
                return null;
            }
        }
        return routineDayIdList;
    }

    public ArrayList<Integer> createRoutineDayIdList() {
        return createRoutineDayIdList(getRoutineDays());
    }

    @Override
    public String toString() {
        String str = "";

        str += "Routine: " + getId() + "-" + getName() + ", " + "Created: " + getDateCreated().toString() + "\n";
        if (routineDays != null) {
            for (RoutineDay routineDay : routineDays) {
                str += routineDay.toString();
            }
        }

        return str;
    }
}
