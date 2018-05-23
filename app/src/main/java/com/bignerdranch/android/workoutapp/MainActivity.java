package com.bignerdranch.android.workoutapp;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BottomNavigationView mBottomNavView;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inflate the activity's view
        setContentView(R.layout.bottomnav_activity_fragment);

        // Initializing the BottomNavigationView
        mBottomNavView = (BottomNavigationView) findViewById(R.id.workoutapp_bottom_navigation);
        // lambda expression used for the OnNavigationItemSelectedListener - the overrided method is boolean onNavigationItemSelected(@NonNull MenuItem item);
        mBottomNavView.setOnNavigationItemSelectedListener((MenuItem item) -> {
            Fragment selectedFragment = null;
            String fragmentTag = null;

            switch (item.getItemId()) {
                case R.id.action_home:
                    fragmentTag = "action_home";
                    selectedFragment = RecentWorkoutsFragment.newInstance();
                    break;
                case R.id.action_routines:
                    fragmentTag = "action_routines";
                    selectedFragment = RoutineListFragment.newInstance();
                    break;
                case R.id.action_history:
                    fragmentTag = "action_history";
                    selectedFragment = RoutineHistoryFragment.newInstance();
                    break;
            }

            FragmentManager fm = getSupportFragmentManager();

            if (fm.findFragmentByTag(fragmentTag) == null && selectedFragment != null) {
                fm.beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment, fragmentTag)
                        .commit();
                return true;
            }

            return false;
        });

        // Set starting position to be action_home -> so this will attach the RecentWorkoutsFragment
        mBottomNavView.setSelectedItemId(R.id.action_home);

    }
}
