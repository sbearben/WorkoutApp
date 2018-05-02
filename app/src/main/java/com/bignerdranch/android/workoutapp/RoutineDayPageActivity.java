package com.bignerdranch.android.workoutapp;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class RoutineDayPageActivity extends SingleFragmentActivity {

    private static final String EXTRA_ROUTINEDAY_ID = "com.bignerdranch.android.workoutapp.routineday_id";


    public static Intent newIntent (Context packageContext, int routineDayId) {
        Intent intent = new Intent(packageContext, RoutineDayPageActivity.class);
        intent.putExtra(EXTRA_ROUTINEDAY_ID, routineDayId);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        int routineDayId = (int) getIntent().getSerializableExtra(EXTRA_ROUTINEDAY_ID);
        return RoutineDayPageFragment.newInstance(routineDayId);
    }
}
