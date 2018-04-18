package com.bignerdranch.android.workoutapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.bignerdranch.android.workoutapp.database.WorkoutDbSchema.RoutineTable;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@Entity (tableName = RoutineTable.NAME) // Caution - table names in SQLLite are case-INsensitive
public class Routine {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = RoutineTable.Cols.ROUTINE_ID)
    private int id;
    //private UUID id;

    @ColumnInfo(name = RoutineTable.Cols.ROUTINE_NAME)
    private String name;

    @ColumnInfo(name = RoutineTable.Cols.ROUTINE_DATE_CREATED)
    private Date dateCreated; // date the routine was created

    @Ignore
    private List<RoutineDay> routineDays;


    /*public Routine (String name) {
        this(UUID.randomUUID(), name);
    }

    public Routine (UUID id, String name) {
        this.id = id;
        this.name = name;
        this.dateCreated = new Date();
    }*/

    public Routine() {
    }

    public Routine (int id, String name, Date dateCreated) {
        this.id = id;
        this.name = name;
        this.dateCreated = dateCreated;
    }

    public Routine (String name) {
        this.name = name;
        this.dateCreated = new Date();
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
}
