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

    @PrimaryKey // Room can assign automatic IDs to entities with @PrimaryKey (autoGenerate=true)
    @ColumnInfo(name = RoutineTable.Cols.ROUTINE_ID)
    private UUID mId;

    @ColumnInfo(name = RoutineTable.Cols.ROUTINE_NAME)
    private String mName;

    @ColumnInfo(name = RoutineTable.Cols.ROUTINE_DATE_CREATED)
    private Date mDateCreated; // date the routine was created

    @Ignore
    private List<RoutineDay> mRoutineDays;


    public Routine (String name) {
        this(UUID.randomUUID(), name);
    }

    public Routine (UUID id, String name) {
        mId = id;
        mName = name;
        mDateCreated = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Date getDateCreated() {
        return mDateCreated;
    }

    public void setDateCreated (Date dateCreated) {
        mDateCreated = dateCreated;
    }

    public void addRoutineDay (RoutineDay routineDay) {
        mRoutineDays.add(routineDay);
    }

    public void removeRoutineDay (RoutineDay routineDay) {
        mRoutineDays.remove(routineDay);
    }

    public List<RoutineDay> getRoutineDays() {
        return mRoutineDays;
    }

    public RoutineDay getRoutineDay (UUID id) {
        for (RoutineDay routineDay : mRoutineDays) {
            if (routineDay.getId().equals(id)) {
                return routineDay;
            }
        }
        return null;
    }
}
