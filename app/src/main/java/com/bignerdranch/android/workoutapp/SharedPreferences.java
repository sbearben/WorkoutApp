package com.bignerdranch.android.workoutapp;

import android.content.Context;
import android.preference.PreferenceManager;

public class SharedPreferences {

    // Used as the key for the query preference
    private static final String PREF_ACTIVE_ROUTINE_ID = "activeRoutineId";
    public static final int NO_ACTIVE_ROUTINE = -1;

    private static final String PREF_ACTIVE_ROUTINE_NAME = "activeRoutineName";
    public static final String NO_ACTIVE_ROUTINE_NAME = "";
    
    // Maybe think about saving activeRoutineName as well which will make intializing the toolbar in RoutineHistoryFragment easier


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
        if (getActiveRoutineId(context) == activeRoutineId) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .remove(PREF_ACTIVE_ROUTINE_ID)
                    .apply();
        }
    }

    // Get the saved active Routine name - return "" if there isn't one
    public static String getActiveRoutineName (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_ACTIVE_ROUTINE_NAME, NO_ACTIVE_ROUTINE_NAME);
    }

    // Set the saved active Routine name
    public static void setActiveRoutineName (Context context, String activeRoutineName) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_ACTIVE_ROUTINE_NAME, activeRoutineName)
                .apply();
    }

    // This function will delete the saved routineName if it is the same as the activeRoutineName that is passed in as an argument
    public static void deleteActiveRoutineName (Context context, String activeRoutineName) {
        if (getActiveRoutineName(context).equals(activeRoutineName)) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .remove(PREF_ACTIVE_ROUTINE_NAME)
                    .apply();
        }
    }

    // This function will save both a given Routine Id and its name
    public static void setActiveRoutineIdAndName (Context context, int activeRoutineId, String activeRoutineName) {
        setActiveRoutineId (context, activeRoutineId);
        setActiveRoutineName (context, activeRoutineName);
    }

    // This function will delete the saved routineId and routineName if it is the same as the activeRoutineId and activeRoutineName that are passed in as arguments
    public static void deleteActiveRoutineIdAndName (Context context, int activeRoutineId, String activeRoutineName) {
        deleteActiveRoutineId (context, activeRoutineId);
        deleteActiveRoutineName (context, activeRoutineName);
    }
}
