package com.bignerdranch.android.workoutapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.bignerdranch.android.workoutapp.global.BasicApp;
import com.bignerdranch.android.workoutapp.global.DataRepository;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;
import com.bignerdranch.android.workoutapp.model.Set;

import java.util.ArrayList;
import java.util.List;

public class EditRoutineActivity extends AppCompatActivity {

    private static final String TAG = "EditRoutineActivity";

    private static final String EXTRA_ROUTINE_ID = "com.bignerdranch.android.workoutapp.routine_id";
    private static final int NEW_ROUTINE = -1;

    private DataRepository mDataRepository;

    private Routine mRoutine;
    private List<RoutineDay> mRoutineDays;
    private List<Integer> mRoutineDayIds;

    private ViewPager mViewPager;
    private RoutineDayPagerAdapter mPagerAdapter;


    // For when we have a Routine we want to load
    public static Intent newIntent (Context packageContext, int routineDayId) {
        Intent intent = new Intent(packageContext, EditRoutineActivity.class);
        intent.putExtra(EXTRA_ROUTINE_ID, routineDayId);
        return intent;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_routine_pager);

        int routineId = (int) getIntent().getSerializableExtra(EXTRA_ROUTINE_ID);

        // Get our DataRepository so we can make queries
        Application application = getApplication();
        mDataRepository = ((BasicApp) application).getRepository();

        mViewPager = (ViewPager) findViewById(R.id.edit_routine_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new RoutineDayPagerAdapter(fragmentManager));

        // Set current item to the first day in the list
        //mViewPager.setCurrentItem(0);

        // TODO: need to check to make sure our ID isn't set to -1 (NEW_ROUTINE) since that means we're creating a new Routine, not editing one
        new CreateRoutineTask(routineId).execute();
    }

    // Essentially just get the template routine days for the routine day
    private Routine createRoutineObject (int routineId) { // TODO: need to abstract all this object creation code - currently repeating way too much code
        Routine activeRoutine = null;

        // Initialize our list of RoutineDayIds
        mRoutineDayIds = new ArrayList<>();

        activeRoutine = mDataRepository.loadRoutine(routineId);

        // Get RoutineDays
        List<RoutineDay> routineDays = mDataRepository.loadTemplateRoutineDays();
        if (routineDays == null) {
            return null;
        }
        activeRoutine.addRoutineDays(routineDays);

        return activeRoutine;
    }

    public void updatePagerAdapter() {
        /* Only want to create a new Adapter if this is the first time updateUI is called
           - if not we call notifyDataSetChanged() to update the UI */
        if (mPagerAdapter == null) {
            mPagerAdapter = new RoutineDayPagerAdapter(getSupportFragmentManager());
            mViewPager.setAdapter(mPagerAdapter);
        }
        else {
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    private class RoutineDayPagerAdapter extends FragmentStatePagerAdapter {

        public RoutineDayPagerAdapter (FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem (int position) {
            RoutineDay routineDay = mRoutineDays.get(position);
            return EditRoutineFragment.newInstance(routineDay.getId());
        }

        @Override
        public int getCount() {
            return mRoutineDays.size();
        }

        @Override
        public CharSequence getPageTitle (int position) {
            return "Day " + (position + 1);
        }
    }

    // Create a background thread to perform the queries to create the Routine object
    private class CreateRoutineTask extends AsyncTask<Void, Void, Routine> {

        private int mRoutineId;

        public CreateRoutineTask (int routineId) {
            mRoutineId = routineId;
        }

        @Override
        protected Routine doInBackground (Void... params) {
            return createRoutineObject(mRoutineId);
        }

        @Override
        protected void onPostExecute (Routine routine) {
            mRoutine = routine;
            updatePagerAdapter();
        }
    }
}
