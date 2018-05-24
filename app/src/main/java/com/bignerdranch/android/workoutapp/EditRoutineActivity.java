package com.bignerdranch.android.workoutapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bignerdranch.android.workoutapp.global.BasicApp;
import com.bignerdranch.android.workoutapp.global.DataRepository;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class EditRoutineActivity extends AppCompatActivity implements EditRoutineFragment.Callbacks {

    private static final String TAG = "EditRoutineActivity";

    private static final String EXTRA_ROUTINE_ID = "com.bignerdranch.android.workoutapp.routine_id";
    private static final int NEW_ROUTINE = -1;

    private DataRepository mDataRepository;

    private Routine mRoutine;
    private List<RoutineDay> mTemplateDays;
    private ArrayList<Integer> mTemplateDayIds;

    private ViewPager mViewPager;
    private RoutineDayPagerAdapter mPagerAdapter;

    // TEST THING
    private LinkedHashSet<Fragment> mFragments = new LinkedHashSet<>();



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

        // Set current item to the first day in the list
        //mViewPager.setCurrentItem(0);

        // TODO: need to check to make sure our ID isn't set to -1 (NEW_ROUTINE) since that means we're creating a new Routine, not editing one
        new CreateRoutineTask(routineId).execute();
    }

    // Essentially just get the template routine days for the routine day
    private Routine createRoutineObject (int routineId) { // TODO: need to abstract all this object creation code - currently repeating way too much code
        Routine activeRoutine = null;

        activeRoutine = mDataRepository.loadRoutine(routineId);

        // Get RoutineDays
        List<RoutineDay> routineDays = mDataRepository.loadTemplateRoutineDays(activeRoutine.getId());
        if (routineDays == null) {
            return null;
        }
        activeRoutine.addRoutineDays(routineDays);

        mTemplateDayIds = new ArrayList<>();
        for (RoutineDay templateDay : routineDays) {
            mTemplateDayIds.add(templateDay.getId()); // add the template day IDs to our EditRoutineActivity global list
        }

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

    @Override
    public void onAddRoutineDayClicked() {
        RoutineDay routineDay = new RoutineDay();
        routineDay.setRoutineId(mRoutine.getId());
        routineDay.setDayNumber(mTemplateDayIds.size()+1);
        routineDay.setDate(null);
        routineDay.setCompleted(false);
        routineDay.setTemplate(true);

        new InsertNewTemplateDayTask(routineDay).execute();
    }

    @Override
    public void onDeleteRoutineDayClicked (RoutineDay routineDay) {
        new DeleteTemplateDayTask(routineDay, mTemplateDays, mTemplateDayIds).execute();
    }

    @Override
    public List<RoutineDay> getTemplateDays() {
        return mTemplateDays;
    }

    @Override
    public void addFragment (Fragment fragment) {
        mFragments.add(fragment);
    }

    @Override
    public void removeFragment (Fragment fragment) {
        mFragments.remove(fragment);
    }

    private class RoutineDayPagerAdapter extends FragmentStatePagerAdapter {

        public RoutineDayPagerAdapter (FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem (int position) {
            RoutineDay routineDay = mTemplateDays.get(position);
            return EditRoutineFragment.newInstance(routineDay.getId(), mTemplateDayIds, mRoutine.getName());
        }

        @Override
        public int getCount() {
            return mTemplateDays.size();
        }

        @Override
        public CharSequence getPageTitle (int position) {
            return "DAY " + (position + 1);
        }

        /* We need this for the issue of the Adapter not working properly when we delete Template Days/Fragments discussed here: https://stackoverflow.com/q/10396321/7648952
           - solution found here: https://stackoverflow.com/a/10399127/7648952 */
        @Override
        public int getItemPosition (Object object) {
            return PagerAdapter.POSITION_NONE;
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
            mTemplateDays = mRoutine.getRoutineDays();

            updatePagerAdapter();
        }
    }

    private class InsertNewTemplateDayTask extends AsyncTask<Void, Void, Long> {

        private RoutineDay mTemplateDay;

        public InsertNewTemplateDayTask (RoutineDay templateDay) {
            mTemplateDay = templateDay;
        }

        @Override
        protected Long doInBackground (Void... params) {
            return mDataRepository.insertRoutineDay(mTemplateDay);
        }

        @Override
        protected void onPostExecute (Long templateDayId) {
            mTemplateDay.setId(templateDayId.intValue());

            mTemplateDays.add(mTemplateDay);
            mTemplateDayIds.add(mTemplateDay.getId());

            updatePagerAdapter();
            mViewPager.setCurrentItem(mTemplateDays.size()-1);
        }
    }

    private class DeleteTemplateDayTask extends AsyncTask<Void, Void, List<RoutineDay>> {

        private RoutineDay mTemplateDay;
        private List<RoutineDay> mTemplateDays;
        private ArrayList<Integer> mTemplateDayIds;
        private int mDeletedItemIndex;

        public DeleteTemplateDayTask (RoutineDay routineDay, List<RoutineDay> templateDays, ArrayList<Integer> templateDayIds) {
            mTemplateDay = routineDay;
            mTemplateDays = templateDays;
            mTemplateDayIds = templateDayIds;
        }

        @Override
        protected List<RoutineDay> doInBackground (Void... params) {
            // Need the index of the day that we are deleting in our list of days
            mDeletedItemIndex = mTemplateDay.getDayNumber()-1;

            // Delete the template day from the DB
            mDataRepository.deleteRoutineDay(mTemplateDay);

            // Update the day numbers for the remaining Template Days
            for (int i=(mDeletedItemIndex+1); i<mTemplateDays.size(); i++) {
                RoutineDay templateDay = mTemplateDays.get(i);
                templateDay.setDayNumber(i);
                // Update those template days in the DB
                mDataRepository.updateRoutineDay(templateDay);
            }

            // Remove the template day from the List of template days and the List of their IDs
            mTemplateDays.remove(mDeletedItemIndex);
            mTemplateDayIds.remove(mDeletedItemIndex);

            return mTemplateDays;
        }

        @Override
        protected void onPostExecute (List<RoutineDay> templateDays) {
            EditRoutineActivity.this.mTemplateDays = templateDays;
            EditRoutineActivity.this.mTemplateDayIds = mTemplateDayIds;

            // Check if we've deleted all the template days and there are now non remaining
            if (templateDays.size() != 0) {
                // Iterate through our list of Fragments currently hosted by our Activity through ViewPager
                for (Fragment fragment : mFragments) {
                    // Make sure the fragment isn't null (I'm not sure if this is necessary - feel like it isn't)
                    if(fragment != null) {
                        // Cast the fragment to the ChangeableRoutineDayDay interface type so that we can access the RoutineDay on the fragment
                        ChangeableRoutineDayDay editRoutineFragment = (ChangeableRoutineDayDay) fragment;
                        RoutineDay routineDay = editRoutineFragment.getRoutineDay();
                        // Make sure the routineDay on the fragment hasn't been set to null as in the case for when we're deleting the day
                        if (routineDay != null) {
                            if ((routineDay.getDayNumber() - 1) > mDeletedItemIndex)
                                // Changing the day number here will prevent us from rewriting the RoutineDay back to the DB in EditRoutineFragment in onPause()
                                editRoutineFragment.setRoutineDayDay(routineDay.getDayNumber() - 1);
                        }
                    }
                }

                // Update the PagerAdapter and load up the correct RoutineDay
                updatePagerAdapter();
                if (mDeletedItemIndex == templateDays .size()) {
                    mViewPager.setCurrentItem(mDeletedItemIndex - 1);
                } else {
                    mViewPager.setCurrentItem(mDeletedItemIndex);
                }
            }
            else {
                // We just deleted the last template day - so we also delete the Routine
                //new Thread(() -> { mDataRepository.deleteRoutine(mRoutine); }).start();
                new DeleteRoutineTask(mRoutine).execute();
            }
        }
    }

    private class DeleteRoutineTask extends AsyncTask<Void, Void, Void> {

        private Routine mRoutine;

        public DeleteRoutineTask (Routine routine) {
            mRoutine = routine;
        }

        @Override
        protected Void doInBackground (Void... params) {
            mDataRepository.deleteRoutine(mRoutine);
            return null;
        }

        @Override
        protected void onPostExecute (Void result) {
            // If the Routine we're deleting is currently the one saved in SharedPreferences, we its ID from being saved there
            SharedPreferences.deleteActiveRoutineId(EditRoutineActivity.this, mRoutine.getId());
            // The Routine has been deleted since we deleted its last RoutineDay - therefore we fire off an onBackPressed() to go back to RoutineListFragment
            onBackPressed();
        }
    }
}
