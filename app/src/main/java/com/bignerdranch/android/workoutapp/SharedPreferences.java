package com.bignerdranch.android.workoutapp;

import android.content.Context;
import android.preference.PreferenceManager;

public class SharedPreferences {

    // Used as the key for the query preference
    private static final String PREF_ACTIVE_ROUTINE_ID = "activeRoutineId";
    public static final int NO_ACTIVE_ROUTINE = -1;


    public static int getActiveRoutineId (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_ACTIVE_ROUTINE_ID, NO_ACTIVE_ROUTINE);
    }

    public static void setActiveRoutineId (Context context, int activeRoutineId) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_ACTIVE_ROUTINE_ID, activeRoutineId)
                .apply();
    }

    // This function will delete the saved routineId if it is the same as the activeRoutineId that is passed in as an argument
    public static void deleteActiveRoutineId (Context context, int activeRoutineId) {
        if (PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_ACTIVE_ROUTINE_ID, NO_ACTIVE_ROUTINE) == activeRoutineId) {

            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .remove(PREF_ACTIVE_ROUTINE_ID)
                    .apply();
        }
    }
}
