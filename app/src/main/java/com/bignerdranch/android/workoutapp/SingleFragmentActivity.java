package com.bignerdranch.android.workoutapp;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Armon on 15/03/2018.
 */

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();

    // @LayoutRes tells Android Studio that any implementation of this method should return a valid layout resource ID
    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.bottomnav_activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inflate the activity's view
        setContentView(getLayoutResId());

        FragmentManager fm = getSupportFragmentManager();
        // The following code (up until .commit()) gives the FragmentManager a fragment to manage
        Fragment fragment = fm.findFragmentById (R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            // this code creates a new fragment transaction, includes one add operation in it,
            // and then commits it
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
