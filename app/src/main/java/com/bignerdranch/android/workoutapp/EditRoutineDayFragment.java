package com.bignerdranch.android.workoutapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bignerdranch.android.workoutapp.global.BasicApp;
import com.bignerdranch.android.workoutapp.global.DataRepository;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.ReppedSet;
import com.bignerdranch.android.workoutapp.model.RoutineDay;
import com.bignerdranch.android.workoutapp.model.Set;
import com.bignerdranch.android.workoutapp.model.TimedSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class EditRoutineDayFragment extends RoutineDayPageFragment {

    private static final String TAG = "EditRoutineDayFragment";

    // The Views contained in our custom ToolBar
    private TextView mRoutineNameTextView;
    private AppCompatSpinner mRoutineDaySpinner;
    private TextView mRoutineDayDateTextView;
    private TextView mRoutineDayDoneTextView;

    private final Object lock = new Object(); // https://stackoverflow.com/a/5861918/7648952


    public static EditRoutineDayFragment newInstance(int routineDayId, ArrayList<Integer> templateDayIds, String routineName) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ROUTINEDAY_ID, routineDayId);
        args.putSerializable(ARG_TEMPLATE_DAY_IDS, templateDayIds);
        args.putSerializable(ARG_ROUTINE_NAME, routineName);

        EditRoutineDayFragment fragment = new EditRoutineDayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onPauseActionsPerformed() {
        synchronized (lock) { // Not sure if this is necessary - see comment below in onOptionsItemSelected(..) for the Delete Routine Day menu item
            if (mRoutineDay != null) {

                if (mRoutineDay.isCompleted()) {
                    new WriteRoutineDayTask(mRoutineDay).execute();
                }
                else if (mRoutineDay.isTemplate()) {
                    if (mRoutineDay.isStarted()) {
                        // if we have a template day, copy it and save the new RoutineDay - set isComplete to false
                        RoutineDay routineDay = mRoutineDay.createDeepCopy();
                        routineDay.setCompleted(false);
                        routineDay.setTemplate(false);
                        routineDay.setDate(mRoutineDayDate);

                        mRoutineDay = routineDay;
                        new WriteRoutineDayTask(mRoutineDay).execute();
                    }
                    else {
                        // don't save anything
                    }
                }
            }
        }
    }

    public void onDeleteRoutineDayMenuItemSelected() {
        if (!mRoutineDay.isTemplate()) { // Check to make sure the RoutineDay we have loaded isn't a template day, so that we don't accidentally delete a template
            new Thread(() -> {
                synchronized (lock) { // Think I might need this to ensure that this completes and mRoutineDay is set null before the code in onPause() runs
                    mDataRepository.deleteRoutineDay(mRoutineDay);
                    mRoutineDay = null;
                }
            }).start();
        }
        getActivity().onBackPressed();
    }

    @Override
    @SuppressLint("RestrictedApi") // without this we get an error calling setDefaultDisplayHomeAsUpEnabled(true) - solution found here: https://stackoverflow.com/a/44926919/7648952
    public void init_toolbar (Context context) {
        ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
        View actionBarView;

        // Set up our custom toolbar
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar_routineday_page);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true); // Make sure we show the home (up) button (NOTE: THIS ISN'T WORKING)

        actionBarView = actionBar.getCustomView(); // Get a reference to the custom toolbar view we just inflated

        // ********** Set up the TextView that holds the Routine name of the RoutineDay
        mRoutineNameTextView = (TextView) actionBarView.findViewById(R.id.actionbar_routineday_name_textview);

        // ********** Set up the RoutineDay spinner
        mRoutineDaySpinner = (AppCompatSpinner) actionBarView.findViewById(R.id.actionbar_routineday_day_spinner);
        List<String> spinnerDayNames = new ArrayList<>(); // List that holds the String List of day names (Day 1, Day 2, etc...)
        for (int i=0; i<mTemplateDayIds.size(); i++) {
            spinnerDayNames.add("Day " + (i+1));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String> (context, R.layout.simple_spinner_item, spinnerDayNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRoutineDaySpinner.setAdapter(adapter);

        // Item selection listener for the Spinner
        mRoutineDaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i (TAG, "onItemSelected() CALLED");
                if (!mIsInitialSpinnerSelection) {
                    //Log.i(TAG, "position: " + position + "   size:" + mTemplateDayIds.size());
                    mRoutineDayId = mTemplateDayIds.get(position);
                    new CreateRoutineDayTask(mRoutineDayId).execute();
                }
                else {
                    mIsInitialSpinnerSelection = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // ********** Set up the TextView that holds the RoutineDay date (also will be a button that launches a DatePicker DialogFragment
        mRoutineDayDateTextView = (TextView) actionBarView.findViewById(R.id.actionbar_routineday_date_textview);

        mRoutineDayDateTextView.setOnClickListener((View v) -> {
            FragmentManager manager = getFragmentManager();
            DatePickerFragment dialog = DatePickerFragment.newInstance(mRoutineDayDate);

            dialog.setTargetFragment (EditRoutineDayFragment.this, REQUEST_ROUTINEDAY_DATE);
            dialog.show (manager, DIALOG_ROUTINEDAY_DATE);
        });

        // ********** Set up the TextView that holds the DONE button
        mRoutineDayDoneTextView = (TextView) actionBarView.findViewById(R.id.actionbar_routineday_done_textview);
        mRoutineDayDoneTextView.setOnClickListener((View v) -> {
            if (mRoutineDay.isTemplate()) {
                RoutineDay routineDay = mRoutineDay.createDeepCopy();
                routineDay.setCompleted(true);
                routineDay.setTemplate(false);
                routineDay.setDate(mRoutineDayDate);

                if (!isTemplateId(mLoadedRoutineDayId, mTemplateDayIds)) // If the ORIGINAL RoutineDay that was loaded was a non template day (ie it was a completed day), we set its ID back on the RoutineDay we're about to save
                    routineDay.setId(mLoadedRoutineDayId);

                /* We set mRoutineDay to routineDay so that when we end the Fragment/Acitivity in the call to getActivity().onBackPressed()
                   below, the new routineDay will be written to the Db in onPause() through the WriteRoutineDayTask Async */
                mRoutineDay = routineDay;
            }
            getActivity().onBackPressed();
        });
        mRoutineDayDoneTextView.setVisibility(View.GONE); // Initially hide the Done TextView

        //updateToolbar();
    }

    // To refresh the Views contained in the Toolbar
    @Override
    public void updateToolbar() {
        mRoutineNameTextView.setText(mRoutineName);

        /*** NOTE ***: I was having issues with onItemSelected() being called twice sometimes randomly (and once other times); using
         * this two argument setSelection call with false as the second argument fixed it for some reason
         * Solution found: https://stackoverflow.com/a/30253459/7648952 */
        mRoutineDaySpinner.setSelection(mRoutineDay.getDayNumber()-1, false); // Set the active routine as the selected item of the spinner

        // Display today's date if this is a RoutineDay without a date set (aka a new day)
        if (mRoutineDayDate == null) {
            mRoutineDayDate = new Date();
        }
        mRoutineDayDateTextView.setText(RoutineDay.createDateString(mRoutineDayDate, false, false));

        mRoutineDayDoneTextView.setVisibility((mRoutineDay.isCompleted() || mRoutineDay.isStarted()) ? View.VISIBLE : View.GONE);
    }

    private boolean isTemplateId (int routineDayId, ArrayList<Integer> templateDayIds) {
        for (int id : templateDayIds) {
            if (routineDayId == id)
                return true;
        }
        return false;
    }

    @Override
    public View.OnClickListener actualMeasurementButtonClick (Exercise exercise, ExerciseViews.SetViews setViews, int setIndex, boolean setExists) {
        return (View v) -> { // TODO: this code is ugly and seems "anti object-oriented"
            if (setExists) { // Not sure if I need these check since the button should be disabled at this point if the set doesn't exist
                Set exerciseSet = exercise.getSets().get(setIndex);

                if (exercise.getType().equals(Exercise.REPPED)) { // TODO: need to add TimedSet implementation
                    ReppedSet reppedSet = (ReppedSet) exerciseSet;

                    int new_value = (reppedSet.getActualMeasurement() == ReppedSet.ACTUAL_REPS_NULL) ? reppedSet.getTargetMeasurement() : reppedSet.getActualMeasurement()-1;
                    reppedSet.setActualMeasurement(new_value);

                    setViews.redrawEnabledViews(reppedSet, actualButtonString(reppedSet), actualButtonBackgroundChangeable());
                    // TODO: need to start a timer here (Broadcast Intent?)
                }
                updateToolbar(); // Need to update toolbar since if we went from a Routineday that wasn't "started" to one that was, the Done button needs to appear
            }
        };
    }

    @Override
    public String actualButtonString (Set exerciseSet) {
        return exerciseSet.actualMeasurementString();
    }

    @Override
    public boolean actualButtonBackgroundChangeable() {
        return true;
    }
}
