package com.bignerdranch.android.workoutapp;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // For saving our instance state so that we open the correct fragment after rotation
    private static final String SAVED_SELECTED_FRAGMENT = "selected_fragment_tag";

    private static final String ACTION_HOME_TAG = "action_home";
    private static final String ACTION_ROUTINES_TAG = "action_routines";
    private static final String ACTION_HISTORY_TAG = "action_history";

    private BottomNavigationView mBottomNavView;
    private String mSelectedFragmentTag = ACTION_HOME_TAG;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inflate the activity's view
        setContentView(R.layout.bottomnav_activity_fragment);

        // Check for saved fragment tag so that we can load up the correct fragment after rotation
        if (savedInstanceState != null) {
            mSelectedFragmentTag = savedInstanceState.getString (SAVED_SELECTED_FRAGMENT);
        }

        // Initializing the BottomNavigationView
        mBottomNavView = (BottomNavigationView) findViewById(R.id.workoutapp_bottom_navigation);
        // lambda expression used for the OnNavigationItemSelectedListener - the overrided method is boolean onNavigationItemSelected(@NonNull MenuItem item);
        mBottomNavView.setOnNavigationItemSelectedListener((MenuItem item) -> {
            Fragment selectedFragment = null;
            //String fragmentTag = null;

            switch (item.getItemId()) {
                case R.id.action_home:
                    mSelectedFragmentTag = ACTION_HOME_TAG;
                    selectedFragment = RecentWorkoutsFragment.newInstance();
                    break;
                case R.id.action_routines:
                    mSelectedFragmentTag = ACTION_ROUTINES_TAG;
                    selectedFragment = RoutineListFragment.newInstance();
                    break;
                case R.id.action_history:
                    mSelectedFragmentTag = ACTION_HISTORY_TAG;
                    selectedFragment = RoutineHistoryFragment.newInstance();
                    break;
            }

            FragmentManager fm = getSupportFragmentManager();

            if (fm.findFragmentByTag(mSelectedFragmentTag) == null && selectedFragment != null) {
                fm.beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment, mSelectedFragmentTag)
                        .commit();
                return true;
            }

            return false;
        });

        // Set starting position to be whatever mSelectedFragmentTag is
        // mBottomNavView.setSelectedItemId(R.id.action_home);
        switch (mSelectedFragmentTag) {
            case ACTION_HOME_TAG:
                mBottomNavView.setSelectedItemId(R.id.action_home);
                break;
            case ACTION_ROUTINES_TAG:
                mBottomNavView.setSelectedItemId(R.id.action_routines);
                break;
            case ACTION_HISTORY_TAG:
                mBottomNavView.setSelectedItemId(R.id.action_history);
                break;
        }
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_SELECTED_FRAGMENT, mSelectedFragmentTag);
    }
}
