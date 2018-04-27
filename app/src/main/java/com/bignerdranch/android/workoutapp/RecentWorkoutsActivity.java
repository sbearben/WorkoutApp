package com.bignerdranch.android.workoutapp;

import android.app.Application;
import android.support.v4.app.Fragment;

import com.bignerdranch.android.workoutapp.global.AppExecutors;
import com.bignerdranch.android.workoutapp.global.DataRepository;

public class RecentWorkoutsActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return RecentWorkoutsFragment.newInstance();
    }

}
