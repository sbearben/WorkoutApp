package com.bignerdranch.android.workoutapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RoutineDayPageFragment extends Fragment {

    public static RoutineDayPageFragment newInstance() {
        return new RoutineDayPageFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recent_workouts, container, false);

        return v;
    }
}
