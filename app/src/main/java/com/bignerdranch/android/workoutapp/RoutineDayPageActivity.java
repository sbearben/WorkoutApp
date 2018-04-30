package com.bignerdranch.android.workoutapp;

import android.support.v4.app.Fragment;

public class RoutineDayPageActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return RoutineDayPageFragment.newInstance();
    }

}
