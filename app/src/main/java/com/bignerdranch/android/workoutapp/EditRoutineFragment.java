package com.bignerdranch.android.workoutapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.TextView;

import com.bignerdranch.android.workoutapp.global.BasicApp;
import com.bignerdranch.android.workoutapp.global.DataRepository;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.ReppedSet;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;
import com.bignerdranch.android.workoutapp.model.Set;
import com.bignerdranch.android.workoutapp.model.TimedSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class EditRoutineFragment extends RoutineDayPageFragment implements ChangeableRoutineDayDay {

    private static final String TAG = "EditRoutineFragment";

    private List<RoutineDay> mTemplateDays;

    // The Views contained in our custom ToolBar
    private TextView mRoutineNameTextView;
    private TextView mAddRoutineDayTextView;

    // So we can execute the callback methods on the hosting activity
    private Callbacks mCallbacks;


    /**
     * Required interface for hosting activities
     */
    public interface Callbacks {
        public void onAddRoutineDayClicked();
        public void onDeleteRoutineDayClicked(RoutineDay routineDay);
        public List<RoutineDay> getTemplateDays();
        public void addFragment (Fragment fragment);
        public void removeFragment (Fragment fragment);
    }

    // Implemented method defined in the ChangeableRoutineDayDay interface
    @Override
    public RoutineDay setRoutineDayDay(int dayNumber) {
        mRoutineDay.setDayNumber(dayNumber);
        return mRoutineDay;
    }

    // Implemented method defined in the ChangeableRoutineDayDay interface
    @Override
    public RoutineDay getRoutineDay() {
        return mRoutineDay;
    }

    public static EditRoutineFragment newInstance(int routineDayId, ArrayList<Integer> templateDayIds, String routineName) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ROUTINEDAY_ID, routineDayId);
        args.putSerializable(ARG_TEMPLATE_DAY_IDS, templateDayIds);
        args.putSerializable(ARG_ROUTINE_NAME, routineName);

        EditRoutineFragment fragment = new EditRoutineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
        // Add this fragment to the HashSet in the hosting activity
        mCallbacks.addFragment(this);
    }

    /* We need this method and the callbacks because when using a FragmentStatePagerAdapter as the adapter for a ViewPager, when we add a new RoutineDay, the TemplateDay dataset changes,
       but a given EditRoutineFragment won't necessarily be destroyed (only its View will be destroyed). As such onActivityCreated won't be called, and we won't be able to get updates to the
       dataset from the DB, so will have to rely on the hosting Activity to get it to us. */
    @Override
    public void onResume() {
        super.onResume();
        mTemplateDays = mCallbacks.getTemplateDays();

        if (mTemplateDays != null)
            mTemplateDayIds = Routine.createRoutineDayIdList(mTemplateDays);
    }

    @Override
    public void onPauseActionsPerformed() {
        if (mRoutineDay != null) {
            new WriteRoutineDayTask(mRoutineDay).execute();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Remove this fragment from the HashSet in the hosting activity
        mCallbacks.removeFragment(this);
        mCallbacks = null;
    }

    @Override
    public void onDeleteRoutineDayMenuItemSelected() {
        /* We only want to show the dialog if we have one template day left; it notifies us that deleting the last
           template day will also delete the whole Routine, and asking the user whether they want to continue */
        if (mTemplateDays.size() == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.delete_routine_dialog_title)
                    .setMessage(R.string.delete_routine_dialog_message)
                    .setPositiveButton(R.string.delete_routine_dialog_positive_button, (DialogInterface d, int which) -> {
                        // We've confirmed that we want to delete the RoutineDay and subsequently the whole Routine
                        deleteRoutineDay();
                    })
                    .setNegativeButton(R.string.delete_routine_dialog_negative_button, null)
                    .show();
        }
        else {
            deleteRoutineDay();
        }
    }

    // Method called within onDeleteRoutineDayMenuItemSelected() since we have to use this block of code twice
    private void deleteRoutineDay() {
        RoutineDay templateDay = mRoutineDay.createCopy(); //
        templateDay.setId(mRoutineDay.getId());
        /* Need to do this because if we set mRoutineDay = null after the callback method is called (commented out line below), then
           mRoutineDay isn't set to null before onPaused() is called, and then mRoutineDay gets written back to the DB after we delete it */
        mRoutineDay = null;
        mCallbacks.onDeleteRoutineDayClicked(templateDay);
    }

    @Override
    @SuppressLint("RestrictedApi") // without this we get an error calling setDefaultDisplayHomeAsUpEnabled(true) - solution found here: https://stackoverflow.com/a/44926919/7648952
    public void init_toolbar (Context context) {
        ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
        View actionBarView;

        // Set up our custom toolbar
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar_edit_routine);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true); // Make sure we show the home (up) button (NOTE: THIS ISN'T WORKING)

        actionBarView = actionBar.getCustomView(); // Get a reference to the custom toolbar view we just inflated

        mRoutineNameTextView = (TextView) actionBarView.findViewById(R.id.actionbar_edit_routine_name);

        mAddRoutineDayTextView = (TextView) actionBarView.findViewById(R.id.actionbar_edit_routine_add_routineday);
        mAddRoutineDayTextView.setOnClickListener((View v) -> {
            mCallbacks.onAddRoutineDayClicked();
        });

        updateToolbar();
    }

    // To refresh the Views contained in the Toolbar
    @Override
    public void updateToolbar() {
        mRoutineNameTextView.setText(mRoutineName);
    }

    @Override
    public View.OnClickListener actualMeasurementButtonClick (Exercise exercise, ExerciseViews.SetViews setViews, int setIndex, boolean setExists) {
        return (View v) -> { // TODO: this code is ugly and seems "anti object-oriented"
            if (setExists) { // Not sure if I need these check since the button should be disabled at this point if the set doesn't exist
                Set exerciseSet = exercise.getSets().get(setIndex);

                if (exercise.getType().equals(Exercise.REPPED)) { // TODO: need to add TimedSet implementation
                    ReppedSet reppedSet = (ReppedSet) exerciseSet;

                    int new_value = (reppedSet.getTargetMeasurement() == ReppedSet.MIN_TARGET_REPS) ? ReppedSet.MAX_TARGET_REPS : reppedSet.getTargetMeasurement()-1;
                    reppedSet.setTargetMeasurement(new_value);

                    setViews.redrawEnabledViews(reppedSet, actualButtonString(reppedSet), actualButtonBackgroundChangeable());
                }
                updateToolbar();
            }
        };
    }

    @Override
    public String actualButtonString (Set exerciseSet) {
        return exerciseSet.targetMeasurementString();
    }

    @Override
    public boolean actualButtonBackgroundChangeable() {
        return false;
    }
}