package com.bignerdranch.android.workoutapp;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.bignerdranch.android.workoutapp.global.BasicApp;
import com.bignerdranch.android.workoutapp.global.DataRepository;
import com.bignerdranch.android.workoutapp.model.Routine;

import java.util.List;

public class RecentWorkoutsFragment extends Fragment {

    private static final String TAG = "RecentWorkoutsFragment";

    private DataRepository mDataRepository;
    private Application mApplication;
    private AppDatabase mAppDatabase;


    public static RecentWorkoutsFragment newInstance() {
        return new RecentWorkoutsFragment();
    }

    /* In order to get valid access to the hosting activity through getActivity() we need to call it here).
       Also, to get our support action bar we need to call getSupportActionBar() here and NOT in Fragment.onAttach() since if
       we call getSupportActionBar() in Fragment.onAttach() our app will crash with a NullPointerException when the activity is rotated
       - see: https://stackoverflow.com/a/24152394/7648952 */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get our DataRepository so that we can easily execute our Db queries
        mApplication = getActivity().getApplication();
        mDataRepository = ((BasicApp) mApplication).getRepository();

        // Setup our custom toolbar and Spinner
        init_toolbar(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recent_workouts, container, false);

        return v;
    }

    // Method to setup our custom toolbar and call the method init_spinner(..) in order to set up the spinner
    private void init_toolbar (Context context) {
        ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();

        // Set up our custom toolbar
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar_spinner);

        // Set up the spinner
        init_spinner (context, actionBar.getCustomView());
    }

    // Method to setup the spinner contained in the toolbar
    private void init_spinner (Context context, View actionBarView) {
        LiveData<List<Routine>> routines  = mDataRepository.loadRoutines();


        // Get and configure our Spinner
        // TODO: need to make a query to get our routines as opposed to using the test static ones defined in strings.xml
        AppCompatSpinner spinner = (AppCompatSpinner) actionBarView.findViewById(R.id.routines_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource (context, R.array.test_routines_array, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Item selection listener for the Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // An item was selected. You can retrieve the selected item using parent.getItemAtPosition(pos)
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
    }
}
