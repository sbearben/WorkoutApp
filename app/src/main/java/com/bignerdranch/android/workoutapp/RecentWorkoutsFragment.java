package com.bignerdranch.android.workoutapp;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.bignerdranch.android.workoutapp.global.BasicApp;
import com.bignerdranch.android.workoutapp.global.DataRepository;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;

import java.util.ArrayList;
import java.util.List;


public class RecentWorkoutsFragment extends Fragment {

    private static final String TAG = "RecentWorkoutsFragment";
    private static final int MAX_RECENT_WORKOUT_DAYS = 3;

    private DataRepository mDataRepository;
    private Application mApplication;

    private List<Routine> mRoutines;
    private int mActiveRoutineId;

    private AppCompatSpinner mToolbarSpinner;
    private View mEmptyRoutinesView;
    private Button mEmptyRoutinesButton;

    private FloatingActionButton mFloatingActionButton;
    private BottomNavigationView mBottomNavView;

    // All the items that hold the details of our recent workouts
    private RecentWorkoutViews[] mRecentWorkoutViews = new RecentWorkoutViews[MAX_RECENT_WORKOUT_DAYS];



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

        // Get the Empty Routines view
        mEmptyRoutinesView = (View) v.findViewById(R.id.empty_routines_view);
        if (mRoutines != null) {
            mEmptyRoutinesView.setVisibility(View.GONE);
        }


        List<RoutineDay> recentWorkoutDays = new ArrayList<>();
        Log.i (TAG, "" + mActiveRoutineId);
        recentWorkoutDays = mDataRepository.loadMostRecentDaysInRoutine(mActiveRoutineId, MAX_RECENT_WORKOUT_DAYS).getValue();

        if (recentWorkoutDays.size() > 0) {
            // Nested for loops to initialize all of our text views for our most recent workout days
            for (int i = 0; i < recentWorkoutDays.size(); i++) {
                int resId = mRecentWorkoutViews[i].resIdGenerator((i + 1) + "_date");
                mRecentWorkoutViews[i].mDateTextView = (TextView) v.findViewById(resId);

                for (int j = 0; j < RecentWorkoutViews.EXERCISES_PER_CARDVIEW; j++) {
                    resId = mRecentWorkoutViews[i].resIdGenerator((i + 1) + "_exercise" + (j + 1));
                    mRecentWorkoutViews[i].mExerciseViews[j].mNameTextView = (TextView) v.findViewById(resId);

                    resId = mRecentWorkoutViews[i].resIdGenerator((i + 1) + "_exercise" + (j + 1) + "_details");
                    mRecentWorkoutViews[i].mExerciseViews[j].mDetailsTextView = (TextView) v.findViewById(resId);
                }
            }
        }

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

        mRoutines  = mDataRepository.loadRoutines().getValue();
        mActiveRoutineId = SharedPreferences.getActiveRoutineId(getActivity());

        String activeRoutineName = "";
        List<String> routineNames = new ArrayList<>();
        int activeRoutineIndex;

        mToolbarSpinner = (AppCompatSpinner) actionBarView.findViewById(R.id.routines_spinner);


        if (mRoutines != null) {
            if (mActiveRoutineId != SharedPreferences.NO_ACTIVE_ROUTINE) {
                for (Routine routine : mRoutines) {
                    routineNames.add(routine.getName());
                    if (routine.getId() == mActiveRoutineId) {
                        activeRoutineName = routine.getName();
                    }
                }
            }
            else {
                for (Routine routine : mRoutines) {
                    routineNames.add(routine.getName());
                }
                activeRoutineName = routineNames.get(0);
            }
            activeRoutineIndex = routineNames.indexOf((Object) activeRoutineName);

            // Get and configure our Spinner
            //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource (context, R.array.test_routines_array, R.layout.simple_spinner_item);
            ArrayAdapter<String> adapter = new ArrayAdapter<String> (context, R.layout.simple_spinner_item, routineNames);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mToolbarSpinner.setAdapter(adapter);

            // set the active routine as the selected item of the spinner
            mToolbarSpinner.setSelection(activeRoutineIndex);

            // Item selection listener for the Spinner
            mToolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        else {
            // Display a message indicating this shit is empty and need to add routine
            mToolbarSpinner.setVisibility (View.GONE);
        }
    }


    // We have three cards that hold three most recent workout days. Each contains a lot of TextViews, so abstracting the details in order to make UI generation of most recent workouts easier
    private class RecentWorkoutViews {
        private static final int EXERCISES_PER_CARDVIEW = 3;
        private static final String VIEW_ID_PREFIX = "recent_workout";

        private CardView mCardView;
        private TextView mDateTextView;
        private ExerciseViews[] mExerciseViews;


        public RecentWorkoutViews() {
            mExerciseViews = new ExerciseViews[EXERCISES_PER_CARDVIEW];
        }

        private int resIdGenerator (String suffix) {
            return getResources().getIdentifier(VIEW_ID_PREFIX + suffix, "id", getActivity().getPackageName());
        }

        private class ExerciseViews {
            private TextView mNameTextView;
            private TextView mDetailsTextView;

            public ExerciseViews() { }
        }
    }
}
