package com.bignerdranch.android.workoutapp;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

public class RoutineDayPageActivity extends SingleFragmentActivity {

    private static final String EXTRA_ROUTINEDAY_ID = "com.bignerdranch.android.workoutapp.routineday_id";
    private static final String EXTRA_TEMPLATE_DAY_IDS = "com.bignerdranch.android.workoutapp.template_day_ids";
    private static final String EXTRA_ROUTINE_NAME = "com.bignerdranch.android.workoutapp.routine_name";


    public static Intent newIntent (Context packageContext, int routineDayId, ArrayList<Integer> templateDayIds, String routineName) {
        Intent intent = new Intent(packageContext, RoutineDayPageActivity.class);
        intent.putExtra(EXTRA_ROUTINEDAY_ID, routineDayId);
        intent.putExtra(EXTRA_TEMPLATE_DAY_IDS, templateDayIds);
        intent.putExtra(EXTRA_ROUTINE_NAME, routineName);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        int routineDayId = (int) getIntent().getSerializableExtra(EXTRA_ROUTINEDAY_ID);
        ArrayList<Integer> templateDayIds = (ArrayList<Integer>) getIntent().getSerializableExtra(EXTRA_TEMPLATE_DAY_IDS);
        String routineName = (String) getIntent().getSerializableExtra(EXTRA_ROUTINE_NAME);

        return RoutineDayPageFragment.newInstance(routineDayId, templateDayIds, routineName);
    }
}
