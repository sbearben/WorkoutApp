package com.bignerdranch.android.workoutapp;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.bignerdranch.android.workoutapp.database.dao.RoutineDao;
import com.bignerdranch.android.workoutapp.global.BasicApp;
import com.bignerdranch.android.workoutapp.global.DataRepository;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.ReppedSet;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;
import com.bignerdranch.android.workoutapp.model.Set;
import com.bignerdranch.android.workoutapp.model.TimedSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RecentWorkoutsFragment extends Fragment {

    private static final String TAG = "RecentWorkoutsFragment";
    private static final int MAX_RECENT_WORKOUT_DAYS = 3;

    private DataRepository mDataRepository;

    // All of the variables for the data displayed on this screen
    private int mActiveRoutineId;
    private Routine mActiveRoutine;
    private List<Routine> mRoutines;
    private Map<String, Integer> mRoutineIdNameMap;
    private List<Integer> mRoutineDayIds;

    private AppCompatSpinner mToolbarSpinner;
    private View mEmptyRoutinesView;
    private Button mEmptyRoutinesButton;
    private FloatingActionButton mFloatingActionButton;

    // All the items that hold the details of our recent workouts
    private List<RecentWorkoutViews> mRecentWorkoutViews;


    public static RecentWorkoutsFragment newInstance() {
        return new RecentWorkoutsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get our DataRepository so that we can easily execute our Db queries
        Application application = getActivity().getApplication();
        mDataRepository = ((BasicApp) application).getRepository();


        mRoutineIdNameMap = new LinkedHashMap<>();
        mRoutines  = mDataRepository.loadRoutines();

        // Create RoutineId/Name map used to easily flip between routines when the user uses the spinner
        for (Routine routine : mRoutines) {
            mRoutineIdNameMap.put(routine.getName(), routine.getId());
        }

        mActiveRoutineId = determineActiveRoutineId(mRoutines);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recent_workouts, container, false);

        // Get the Empty Routines view
        mEmptyRoutinesView = (View) v.findViewById(R.id.empty_routines_view);
        if (mRoutines != null) {
            mEmptyRoutinesView.setVisibility(View.GONE);
        }

        mRecentWorkoutViews = new ArrayList<>();

        // Nested for loops to initialize all of our TextViews for our most recent workout days
        for (int i = 0; i < MAX_RECENT_WORKOUT_DAYS; i++) {
            final int i_copy = i; // Needed to create this variable because can't use 'i' in the CardView onClickListener since 'i' must be final
            RecentWorkoutViews recentWorkoutView = new RecentWorkoutViews();

            // Get a reference to the CardView that holds all the below TextViews
            int resId = recentWorkoutView.resIdGenerator((i + 1) + "");
            recentWorkoutView.mCardView = (CardView) v.findViewById(resId);
            // Set the CardView's onClickListner to open up the RoutineDay page if the routine day is clicked on
            recentWorkoutView.mCardView.setOnClickListener((View view) -> {
                if (mRoutineDayIds != null) {
                    Intent intent = RoutineDayPageActivity.newIntent(getActivity(), mRoutineDayIds.get(i_copy));
                    startActivity(intent);
                }
            });

            // Get a reference to the date text view, create the date string, and set the TextView
            resId = recentWorkoutView.resIdGenerator((i + 1) + "_date");
            recentWorkoutView.mDateTextView = (TextView) v.findViewById(resId);

            for (int j = 0; j < RecentWorkoutViews.EXERCISES_PER_CARDVIEW; j++) {
                // Add a new ExerciseViews within the recentWorkoutView and then retrieve it
                recentWorkoutView.addEmptyExerciseView();
                RecentWorkoutViews.ExerciseViews exerciseViews = recentWorkoutView.mExerciseViews.get(j);

                // Get a reference to the exercise name text view and set it to the current exercise's name
                resId = recentWorkoutView.resIdGenerator((i + 1) + "_exercise" + (j + 1));
                exerciseViews.mNameTextView = (TextView) v.findViewById(resId);

                // Get a reference to the exercise details text view, create the details string, and set the TextView
                resId = recentWorkoutView.resIdGenerator((i + 1) + "_exercise" + (j + 1) + "_details");
                exerciseViews.mDetailsTextView = (TextView) v.findViewById(resId);
            }
            // Add the recentWorkoutView element to the list of RecentWorkoutViews
            mRecentWorkoutViews.add(recentWorkoutView);
        }

        // Initializing the FloatingActionButton
        mFloatingActionButton = v.findViewById(R.id.fab_new_workout_day);
        mFloatingActionButton.setOnClickListener((View view) -> {
            // Add and go to new RoutineDay
        });

        /* Background AsynTask that calls createRoutineObject(mRoutineId) on the background thread and assigns
           the returned Routine to mActiveRoutine, then calls updateUI() */
        //new CreateRoutineTask(mActiveRoutineId).execute();

        return v;
    }

    /* In order to get valid access to the hosting activity through getActivity() we need to call it here).
       Also, to get our support action bar we need to call getSupportActionBar() here and NOT in Fragment.onAttach() since if
       we call getSupportActionBar() in Fragment.onAttach() our app will crash with a NullPointerException when the activity is rotated
       NOTE: onActivityCreated is called AFTER onCreateView (see fragment lifecycle: https://developer.android.com/guide/components/fragments.html
       - see: https://stackoverflow.com/a/24152394/7648952 */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setup our custom toolbar and Spinner
        init_toolbar(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i (TAG, "onResume() called");
        new CreateRoutineTask(mActiveRoutineId).execute();
    }

    // Creates a full Routine object if all the required data exists
    private Routine createRoutineObject (int routineId) {
        Routine activeRoutine = null;

        // Initialize our list of RoutineDayIds
        mRoutineDayIds = new ArrayList<>(MAX_RECENT_WORKOUT_DAYS);

        if (routineId != SharedPreferences.NO_ACTIVE_ROUTINE) {
            activeRoutine = mDataRepository.loadRoutine(routineId);

            // Get RoutineDays
            List<RoutineDay> routineDays = mDataRepository.loadMostRecentDaysInRoutine(routineId, MAX_RECENT_WORKOUT_DAYS);
            if (routineDays == null) {
                return null;
            }
            activeRoutine.addRoutineDays(routineDays);

            // Get the Exercises for each RoutineDay
            for (RoutineDay routineDay : activeRoutine.getRoutineDays()) {
                // Add the RoutineDay's Id to our list we're keeping track of
                mRoutineDayIds.add(routineDay.getId());

                // Get first three exercises for each day
                List<Exercise> dayExercises = mDataRepository.loadFirstNExercisesInRoutineDay(routineDay.getId(), RecentWorkoutViews.EXERCISES_PER_CARDVIEW);
                if (dayExercises == null) {
                    return null;
                }
                routineDay.addExercises(dayExercises);

                // Get sets in Exercises
                for (Exercise exercise : routineDay.getExercises()) {
                    List<Set> exerciseSets;
                    if (exercise.getType().equals(Exercise.REPPED)) {
                        // Solution to cast list of subtype List<ReppedSet> to list of supertype List<Set> found here:
                        // - https://stackoverflow.com/a/933600/7648952
                        exerciseSets = (List<Set>)(List<?>) mDataRepository.loadAllReppedExerciseSets(exercise.getId());
                    }
                    else {
                        exerciseSets = (List<Set>)(List<?>) mDataRepository.loadAllTimedExerciseSets(exercise.getId());
                    }

                    if (exerciseSets == null) {
                        return null;
                    }
                    exercise.addSets(exerciseSets);
                }
            }
        }

        //if (activeRoutine != null)
            //Log.i (TAG, activeRoutine.toString());

        return activeRoutine;
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

    private void init_spinner (Context context, View actionBarView) {
        mToolbarSpinner = (AppCompatSpinner) actionBarView.findViewById(R.id.routines_spinner);

        // If there are saved routines
        if (mActiveRoutineId != SharedPreferences.NO_ACTIVE_ROUTINE) {
            List<String> routineNames = new ArrayList<>(mRoutineIdNameMap.keySet());
            String activeRoutineName = getRoutineName(mActiveRoutineId, mRoutines);

            ArrayAdapter<String> adapter = new ArrayAdapter<String> (context, R.layout.simple_spinner_item, routineNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mToolbarSpinner.setAdapter(adapter);

            // set the active routine as the selected item of the spinner
            mToolbarSpinner.setSelection(routineNames.indexOf(activeRoutineName));

            // Item selection listener for the Spinner
            mToolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedRoutineName = (String) parent.getItemAtPosition(position);
                    mActiveRoutineId = mRoutineIdNameMap.get(selectedRoutineName);
                    new CreateRoutineTask(mActiveRoutineId).execute();
                    // Save the selected RoutineId in SharedPreferences so that we can load the correct Routine if we navigate away and back again
                    SharedPreferences.setActiveRoutineId(context, mActiveRoutineId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Another interface callback
                }
            });
        }
        else {
            // Hide the spinner since we don't have any routines created that we can select
            mToolbarSpinner.setVisibility (View.GONE);
        }
    }

    private void updateUI() {
        if (mActiveRoutine != null && mActiveRoutine.getRoutineDays() != null) {
            // Nested for loops to initialize all of our text views for our most recent workout days
            for (int i=0; i < mRecentWorkoutViews.size(); i++) {
                RecentWorkoutViews recentWorkoutView = mRecentWorkoutViews.get(i);
                RoutineDay routineDay;

                // Check if the relevant RoutineDay exists, if it doesn't we hide the CardView and go back to the top of the loop (all proceeding CardViews will be hidden)
                if (mActiveRoutine.getRoutineDays().size() > i) {
                    routineDay = mActiveRoutine.getRoutineDays().get(i);
                    recentWorkoutView.mCardView.setVisibility(View.VISIBLE);
                }
                else {
                    recentWorkoutView.mCardView.setVisibility(View.GONE);
                    continue;
                }

                // Create the date string, and set the TextView
                String dateString = recentWorkoutView.createDateString(routineDay.getDate(), routineDay.isCompleted());
                recentWorkoutView.mDateTextView.setText(dateString);

                //for (int j=0; j < routineDay.getExercises().size(); j++) {
                for (int j=0; j < recentWorkoutView.mExerciseViews.size(); j++) {
                    // Retrieve the relevant ExerciseViews within the recentWorkoutView
                    RecentWorkoutViews.ExerciseViews exerciseViews = recentWorkoutView.mExerciseViews.get(j);
                    Exercise exercise;

                    // Check if the relevant Exercise exists, if it doesn't we clear the text from the relevant TextViews
                    // and go back to the top of the loop (all proceeding TextViews will have their text cleared)
                    if (routineDay.getExercises().size() > j) {
                        exercise = routineDay.getExercises().get(j);
                    }
                    else {
                        exerciseViews.mNameTextView.setText("");
                        exerciseViews.mDetailsTextView.setText("");
                        continue;
                    }

                    // Set the exercise name TextView to the current exercise's name
                    exerciseViews.mNameTextView.setText(exercise.getName());

                    // Create the exercise details string and set the TextView
                    String detailsString = recentWorkoutView.createExerciseDetailsString(exercise);
                    exerciseViews.mDetailsTextView.setText(detailsString);
                }
            }
        }
    }

    private String getRoutineName (int routineId, List<Routine> routines) {
        for (Routine routine : routines) {
            if (routine.getId() == routineId)
                return routine.getName();
        }
        return null;
    }

    // Method with a bunch of logic to determine the active routine id based on the routines we have in the DB and the saved SharedPreference routineId
    private int determineActiveRoutineId(List<Routine> routines) {
        int activeRoutineId = SharedPreferences.getActiveRoutineId(getActivity());

        if (routines.size() != 0) {
            if (activeRoutineId != SharedPreferences.NO_ACTIVE_ROUTINE) {
                for (Routine routine : routines) {
                    if (routine.getId() == activeRoutineId) {
                        return activeRoutineId;
                    }
                }
            }
            // If we have routines in the DB but don't have a saved selected one for some reason, return the first routine
            return routines.get(0).getId();
        }

        // If we've made it this far we don't have any routines, return NO_ACTIVE_ROUTINE
        return SharedPreferences.NO_ACTIVE_ROUTINE;
    }


    // We have three cards that hold three most recent workout days. Each contains a lot of TextViews, so abstracting the details in order to make UI generation of most recent workouts easier
    private class RecentWorkoutViews {
        private static final int EXERCISES_PER_CARDVIEW = 3;
        private static final String VIEW_ID_PREFIX = "recent_workout";

        private CardView mCardView;
        private TextView mDateTextView;
        private List<ExerciseViews> mExerciseViews;


        private RecentWorkoutViews() {
            mExerciseViews = new ArrayList<>(EXERCISES_PER_CARDVIEW);
        }

        private int resIdGenerator (String suffix) {
            return getResources().getIdentifier(VIEW_ID_PREFIX + suffix, "id", getActivity().getPackageName());
        }

        // TODO: there's going to eventually be some logic here to potentially set this string to "Next" when we don't have completed or ongoing RoutineDay's
        private String createDateString (Date date, boolean complete) {
            if (!complete) {
                return "Ongoing";
            }
            String[] dayNames = { "Sun", "Mon", "Tues", "Weds", "Thurs", "Fri", "Sat" };
            String[] monthNames = { "Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec" };

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);

            return dayNames[dayOfWeek-1] + "\n" + day + " " + monthNames[month];
        }

        // TODO: This function hardcodes the set weights in pounds - eventually need to add flexibility for switch between pounds and kg
        private String createExerciseDetailsString (Exercise exercise) {
            List<Set> exerciseSets = exercise.getSets();
            String detailsString = "";
            int targetNumSets = exercise.getTargetNumberSets();

            if (exercise.getType().equals(Exercise.REPPED)) {
                int maxTargetWeight = 0;
                int previousSetTargetWeight = 0;
                int maxTargetReps = 0;
                int previousSetTargetReps = 0;

                boolean allSetsSameTargetWeight = true; // flag to determine if all sets in exercise are at the same target weight
                boolean allSetsSameTargetReps = true; // flag to determine if all sets in exercise are at the same target reps
                boolean allSetsSuccessfullyCompleted = true; // flag to determine if all sets were successful (aka we hit our target number of reps)
                int skippedSetCount = 0; // integer to keep track of how many sets were skipped - if this count equals the number of sets, then we know the exercise was completely skipped
                boolean exerciseSkipped = true; // flag to determine of the exercise was completely skipped

                // Loop through each reppedSet in the exercise
                for (Set reppedSet : exerciseSets) {
                    int setTargetWeight = reppedSet.getTargetWeight();
                    int setTargetReps = ((ReppedSet) reppedSet).getTargetMeasurement();
                    int setActualReps = ((ReppedSet) reppedSet).getActualMeasurement();

                    // Logic to get the exercise's highest weight set
                    if (setTargetWeight > maxTargetWeight) {
                        maxTargetWeight = setTargetWeight;
                    }
                    // If at any point, the current set targetWeight is not equal to the previous set targetWeight, then we know all sets are not the same target weight
                    if (setTargetWeight != previousSetTargetWeight && exerciseSets.indexOf(reppedSet) > 0) {
                        allSetsSameTargetWeight = false;
                    }
                    // Logic to get the exercise's highest target reps set
                    if (setTargetReps > maxTargetReps) {
                        maxTargetReps = setTargetReps;
                    }
                    if (setTargetReps != previousSetTargetReps && exerciseSets.indexOf(reppedSet) > 0) {
                        allSetsSameTargetReps = false;
                    }
                    // If at any point, the current set targetReps is not equal to the set actualReps, then we know that all of the exercise's sets were not completed successfully
                    if (setActualReps != setTargetReps) {
                        allSetsSuccessfullyCompleted = false;
                    }

                    previousSetTargetWeight = setTargetWeight;
                    previousSetTargetReps = setTargetReps;

                    // If the setActualReps equals ReppedSet.ACTUAL_REPS_NULL the set has been skipped, so we increment our skippedSetCount
                    if (setActualReps == ReppedSet.ACTUAL_REPS_NULL) {
                        skippedSetCount++;
                        detailsString += "‒/"; // The dash here indicates the set was skipped
                        continue; // We want to go back to the top of the for loop since the code below that adds to detailsString will double count the set if we don't

                    }

                    detailsString += setActualReps + "/";
                }

                detailsString += "  " + maxTargetWeight + "lb"; // TODO: change hardcode of lb (pounds) here

                if ((allSetsSameTargetWeight && allSetsSameTargetReps && allSetsSuccessfullyCompleted) || (targetNumSets == 1 && skippedSetCount == 0)) {
                    detailsString = targetNumSets + "x" + maxTargetReps + " " + maxTargetWeight + "lb";  // TODO: change hardcode of lb (pounds) here
                } else if (targetNumSets == skippedSetCount) {
                    detailsString = "Skipped";
                }
            } else if (exercise.getType().equals(Exercise.TIMED)) {
                // Some other logic here for when we implement timed sets - a lot of it will be similar to the above, so we'll have to make the above code more abstract eventually
            }

            return detailsString;
        }

        // Need this method because outside of this class we can't create an ExerciseViews object since it isn't static, and we can't make it static since it's an inner class
        private void addEmptyExerciseView() {
            ExerciseViews exerciseViews = new ExerciseViews();
            mExerciseViews.add(exerciseViews);
            //return mExerciseViews.size();
        }

        private class ExerciseViews {
            private TextView mNameTextView;
            private TextView mDetailsTextView;

            private ExerciseViews() { }
        }
    }

    // Create a background thread to perform the queries to create the Routine object
    private class CreateRoutineTask extends AsyncTask<Void, Void, Routine> {

        private int mRoutineId;

        public CreateRoutineTask (int routineId) {
            mRoutineId = routineId;
        }

        @Override
        protected Routine doInBackground (Void... params) {
            return createRoutineObject(mRoutineId);
        }

        @Override
        protected void onPostExecute (Routine routine) {
            mActiveRoutine = routine;
            updateUI();
        }
    }
}
