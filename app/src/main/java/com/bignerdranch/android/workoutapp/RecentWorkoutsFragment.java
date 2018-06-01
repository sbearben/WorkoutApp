package com.bignerdranch.android.workoutapp;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import android.widget.CheckBox;
import android.widget.TextView;

import com.bignerdranch.android.workoutapp.database.AppDatabase;
import com.bignerdranch.android.workoutapp.global.BasicApp;
import com.bignerdranch.android.workoutapp.global.DataRepository;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;
import com.bignerdranch.android.workoutapp.model.Set;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class RecentWorkoutsFragment extends Fragment {

    private static final String TAG = "RecentWorkoutsFragment";
    private static final int MAX_RECENT_WORKOUT_DAYS = 3;

    private static final String DIALOG_NEW_ROUTINE = "DialogNewRoutine";
    private static final int REQUEST_NEW_ROUTINE = 0;

    private DataRepository mDataRepository;

    // All of the variables for the data displayed on this screen
    private int mActiveRoutineId;
    private Routine mActiveRoutine;
    private List<Routine> mRoutines;
    private Map<String, Integer> mRoutineIdNameMap;
    private List<Integer> mRoutineDayIds;

    private List<RoutineDay> mActiveRoutineTemplateDays; // List of our template RoutineDays as well as a matching list of their respective IDs
    private ArrayList<Integer> mActiveRoutineTemplateDayIds; // Need to use an ArrayList since we are passing it as Intent/Fragament EXTRA/ARG and it is serializable

    private AppCompatSpinner mToolbarSpinner;
    private View mEmptyRoutinesView;
    private Button mEmptyRoutinesButton;
    private View mEmptyRoutineDaysView;
    private FloatingActionButton mFloatingActionButton;

    private boolean mIsInitialSpinnerSelection = true; // boolean to make sure the Spinner callback onItemSelected doesn't execute its contained code when we initialize its first item with setSelection()

    private List<RecentWorkoutViews> mRecentWorkoutViews; // All the items that hold the details of our recent workouts


    public static RecentWorkoutsFragment newInstance() {
        return new RecentWorkoutsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get our DataRepository so that we can easily execute our Db queries
        Application application = getActivity().getApplication();
        mDataRepository = ((BasicApp) application).getRepository();
        AppDatabase appDatabase = ((BasicApp) application).getDatabase();

        // Create the DB observer that updates the UI in the case where we're generating data for the DB
        final Observer<Boolean> dbGeneratorObserver = (@Nullable final Boolean databaseGenerated) -> {
            /* If we are generating data for the DB, when the data is generated mIsDatabaseCreated in AppDatabase will
               be set to true, at which point the change of value will result in this method being called since we're
               registered as an observer of mIsDatabaseCreated */
            try {
                if (databaseGenerated)
                    new GetRoutinesTask().execute();
            }
            catch (NullPointerException e) {
                Log.i (TAG, e.getMessage());
            }
        };
        appDatabase.getDatabaseCreated().observe(this, dbGeneratorObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recent_workouts, container, false);

        // Get the Empty Routines view
        mEmptyRoutinesView = (View) v.findViewById(R.id.recent_workouts_empty_routines_view);
        // Initially hide the emptyRoutinesView
        mEmptyRoutinesView.setVisibility(View.GONE);

        // Inflate no routines button
        mEmptyRoutinesButton = (Button) v.findViewById(R.id.recent_workouts_empty_routines_button);
        mEmptyRoutinesButton.setOnClickListener((View view) -> {
            FragmentManager manager = getFragmentManager();
            NewRoutineFragment dialog = NewRoutineFragment.newInstance(Routine.createRoutineNameList(mRoutines));

            dialog.setTargetFragment (RecentWorkoutsFragment.this, REQUEST_NEW_ROUTINE);
            dialog.show (manager, DIALOG_NEW_ROUTINE);
        });
        mEmptyRoutinesButton.setVisibility(View.GONE);

        // Get the Empty RoutineDays view
        mEmptyRoutineDaysView = (View) v.findViewById(R.id.recent_workouts_empty_routinedays_view);
        // Initially hide the emptyRoutinesView
        mEmptyRoutineDaysView.setVisibility(View.GONE);

        mRecentWorkoutViews = new ArrayList<>();
        // Nested for loops to initialize all of our TextViews for our most recent workout days
        for (int i = 0; i < MAX_RECENT_WORKOUT_DAYS; i++) {
            final int i_copy = i; // Needed to create this variable because can't use 'i' in the CardView onClickListener since 'i' must be final
            RecentWorkoutViews recentWorkoutView = new RecentWorkoutViews();

            // Get a reference to the CardView that holds all the below TextViews
            int resId = recentWorkoutView.resIdGenerator((i + 1) + "");
            recentWorkoutView.mCardView = (CardView) v.findViewById(resId);
            // Also want to initially hide our cards
            recentWorkoutView.mCardView.setVisibility(View.GONE);
            // Set the CardView's onClickListener to open up the RoutineDay page if the routine day is clicked on
            recentWorkoutView.mCardView.setOnClickListener((View view) -> {
                if (i_copy < mRoutineDayIds.size()) { // Checking this so that clicking on a card when the RoutineDay doesn't exist doesn't cause a crash
                    Intent intent = EditRoutineDayActivity.newIntent(getActivity(), mRoutineDayIds.get(i_copy), mActiveRoutineTemplateDayIds, mActiveRoutine.getName());
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
        mFloatingActionButton = v.findViewById(R.id.fab_new_workout_day); // we set the onClickListener in init_new_routineday_fab(..)
        mFloatingActionButton.setVisibility(View.GONE);

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
        /* put this here so RoutineHistoryFragment doesn't crash before we use the Spinner for the first
           time (since that was previously the only place we were setting the saved routine id) */
        //SharedPreferences.setActiveRoutineId(getActivity(), mActiveRoutineId);
        /*SharedPreferences.setActiveRoutineIdAndName(getActivity(), mActiveRoutineId, getRoutineName(mActiveRoutineId, mRoutines));
        init_toolbar(getActivity());*/

    }

    @Override
    public void onResume() {
        super.onResume();
        /* Background AsyncTask that calls createRoutineObject(mRoutineId) on the background thread and assigns
           the returned Routine to mActiveRoutine, then calls updateUI() */
        new GetRoutinesTask().execute();
    }

    // Creates a full Routine object if all the required data exists
    private Routine createRoutineObject (int routineId) {
        Routine activeRoutine = null;

        // Initialize our list of RoutineDayIds
        mRoutineDayIds = new ArrayList<>();

        if (routineId != SharedPreferences.NO_ACTIVE_ROUTINE) {
            activeRoutine = mDataRepository.loadRoutine(routineId);

            // We first want to check if we have any Ongoing RoutineDays in the Routine (should only be a max of 1 if any)
            List<RoutineDay> routineDays = new ArrayList<>(mDataRepository.loadOngoingDaysInRoutine(routineId));
            if (routineDays.size() > 0) {
                routineDays.addAll(mDataRepository.loadMostRecentDaysInRoutine(routineId, MAX_RECENT_WORKOUT_DAYS-1));
            }
            else {
                routineDays.addAll(mDataRepository.loadMostRecentDaysInRoutine(routineId, MAX_RECENT_WORKOUT_DAYS));
            }

            /*if (routineDays.size() == 0) {
                //return null;
            }*/
            activeRoutine.addRoutineDays(routineDays);

            // Get the Exercises for each RoutineDay
            for (RoutineDay routineDay : activeRoutine.getRoutineDays()) {
                // Add the RoutineDay's Id to our list we're keeping track of
                mRoutineDayIds.add(routineDay.getId());

                // Get first three exercises for each day
                List<Exercise> dayExercises = mDataRepository.loadFirstNExercisesInRoutineDay(routineDay.getId(), RecentWorkoutViews.EXERCISES_PER_CARDVIEW);
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
                    exercise.addSets(exerciseSets);
                }
            }
        }

        return activeRoutine;
    }

    private List<RoutineDay> getTemplateDays (int routineId) {
        return mDataRepository.loadTemplateRoutineDays(routineId);
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
                    if (!mIsInitialSpinnerSelection) {
                        String selectedRoutineName = (String) parent.getItemAtPosition(position);
                        mActiveRoutineId = mRoutineIdNameMap.get(selectedRoutineName);
                        mFloatingActionButton.setOnClickListener(null);

                        new CreateRoutineTask(mActiveRoutineId).execute();
                        // Save the selected RoutineId in SharedPreferences so that we can load the correct Routine if we navigate away and back again
                        //SharedPreferences.setActiveRoutineId(context, mActiveRoutineId);
                        SharedPreferences.setActiveRoutineIdAndName(context, mActiveRoutineId, selectedRoutineName);
                    }
                    else {
                        mIsInitialSpinnerSelection = false;
                    }
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

    private void init_new_routineday_fab (List<RoutineDay> templateDays, int dayNumber, ArrayList<Integer> templateDayIds, String routineName) {
        if (dayNumber == templateDays.size()) {
            dayNumber = 0;
        }

        final int dayNumber_copy = dayNumber; // Need this because Android Studio whines if we use dayNumber because of the above if statement
        mFloatingActionButton.setOnClickListener((View view) -> {

            List<RoutineDay> routineDays = mActiveRoutine.getRoutineDays();
            if (!(routineDays.size() == 0) && !mActiveRoutine.getRoutineDays().get(0).isCompleted()) { // Check if we have an Ongoing RoutineDay - only want to display the AlertDialog if we do
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_ongoing_routineday, null);
                CheckBox checkBox = (CheckBox) v.findViewById(R.id.dialog_ongoing_routineday_checkbox);

                builder.setView(v)
                        .setTitle(R.string.new_routineday_dialog_title)
                        .setPositiveButton(R.string.new_routineday_dialog_positive_button, (DialogInterface dialog, int which) -> {
                            RoutineDay ongoingRoutine = mActiveRoutine.getRoutineDays().get(0);
                            // If the checkbox is checked, set the ongoing RoutineDay as completed, then update its row in the DB - if not delete the ongoing RoutineDay from the DB
                            if (checkBox.isChecked()) {
                                ongoingRoutine.setCompleted(true);
                                new Thread(() -> { mDataRepository.updateRoutineDay(ongoingRoutine); }).start();
                            }
                            else {
                                new Thread(() -> { mDataRepository.deleteRoutineDay(ongoingRoutine); }).start();
                            }

                            Intent intent = EditRoutineDayActivity.newIntent(getActivity(), templateDays.get(dayNumber_copy).getId(), templateDayIds, routineName);
                            startActivity(intent);
                        })
                        .setNegativeButton(R.string.new_routineday_dialog_negative_button, null)
                        .show();
            }
            else {
                Intent intent = EditRoutineDayActivity.newIntent(getActivity(), templateDays.get(dayNumber_copy).getId(), templateDayIds, routineName);
                startActivity(intent);
            }
        });
    }

    private void updateUI() {
        if (mActiveRoutine != null && mActiveRoutine.getRoutineDays() != null) {
            // Nested for loops to initialize all of our text views for our most recent workout days
            for (int i=0; i < mRecentWorkoutViews.size(); i++) {
                RecentWorkoutViews recentWorkoutView = mRecentWorkoutViews.get(i);
                RoutineDay routineDay;

                // Check if the relevant RoutineDay exists, if it doesn't we hide the CardView and go back to the top of the loop (all proceeding CardViews will be hidden)
                Log.i (TAG, "SIZE: " + mActiveRoutine.getRoutineDays().size());
                if (mActiveRoutine.getRoutineDays().size() > i) {
                    routineDay = mActiveRoutine.getRoutineDays().get(i);
                    recentWorkoutView.mCardView.setVisibility(View.VISIBLE);
                }
                else {
                    recentWorkoutView.mCardView.setVisibility(View.GONE);
                    continue;
                }

                // Create the date string, and set the TextView
                String dateString = RoutineDay.createDateString(routineDay.getDate(), !routineDay.isCompleted(), true);
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
                    String detailsString = Exercise.createExerciseDetailsString(exercise, Exercise.NEWLINE_NULL);
                    exerciseViews.mDetailsTextView.setText(detailsString);
                }
            }
        }
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_NEW_ROUTINE) {
            String routineName = (String) data.getSerializableExtra(NewRoutineFragment.EXTRA_ROUTINE_NAME);
            int numberDays = (int) data.getSerializableExtra(NewRoutineFragment.EXTRA_ROUTINE_DAYS);

            Routine routine = new Routine();
            routine.setName(routineName);
            routine.setDateCreated(new Date());

            new RoutineListFragment.InsertNewRoutineTask(routine, numberDays, mDataRepository, getActivity(), this, 0).execute();
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
        private List<RoutineDay> mTemplateDays;
        private ArrayList<Integer> mTemplateDayIds; // Need this because we initialize this list in doInBackground(), but need to assign it to a our global list of TemplateDayIds in onPostExecute()

        public CreateRoutineTask (int routineId) {
            mRoutineId = routineId;
            mTemplateDayIds = new ArrayList<>();
        }

        @Override
        protected Routine doInBackground (Void... params) {
            mTemplateDays = getTemplateDays(mRoutineId); // Need to get the ID's of the template days so that we can use them when we add a new RoutineDay
            mTemplateDayIds = Routine.createRoutineDayIdList(mTemplateDays);
            /*for (RoutineDay templateDay : mTemplateDays) {
                mTemplateDayIds.add(templateDay.getId());
            }*/
            return createRoutineObject(mRoutineId);
        }

        @Override
        protected void onPostExecute (Routine routine) {
            mActiveRoutine = routine; // Assign the routine to our global variable mActiveRoutine so we can use outside the AsynTask
            mActiveRoutineTemplateDayIds = mTemplateDayIds; // Assign the list of template day IDs to our global variable mActiveRoutineTemplateDayIds so we can use outside the AsynTask

            if (routine != null) { // need this since the call to getRoutineDays() was crashing the app the first time we installed it and the data is being generated (can probably remove once we stop doing data generation)
                // Code to determine what day number the last completed workout day was in the active routine
                int lastDayNumber = 0; // 0 in this case would indicate that there have been no previous RoutineDays, so we should start at Day 1
                if (routine.getRoutineDays().size() != 0) { // originally routine.getRoutineDays() != null
                    lastDayNumber = routine.getRoutineDays().get(0).getDayNumber();
                }

                // Determine of the visibility of the recent workout cards and the empty routineDays view
                setRecentWorkoutCardsVisibility(routine.getRoutineDays());
                setEmptyRoutineDaysViewsVisibility(routine.getRoutineDays());

                // Initialize the fab used to start a new workout day
                init_new_routineday_fab(mTemplateDays, lastDayNumber, mTemplateDayIds, routine.getName()); // Now that we have our template days we can set the fab onClickListener
            }
            updateUI();
        }
    }

    // Async used to load the routines, set the activeRoutineId and save that in the PreferenceManager, then execute the CreateRoutineTask Async
    private class GetRoutinesTask extends AsyncTask<Void, Void, Void> {

        List<Routine> mRoutines;
        private Map<String, Integer> mRoutineIdNameMap;

        public GetRoutinesTask() {
        }

        @Override
        protected Void doInBackground (Void... params) {
            mRoutineIdNameMap = new LinkedHashMap<>();
            mRoutines  = mDataRepository.loadRoutines();

            // Create RoutineId/Name map used to easily flip between routines when the user uses the spinner
            for (Routine routine : mRoutines) {
                mRoutineIdNameMap.put(routine.getName(), routine.getId());
            }

            return null;
        }

        @Override
        protected void onPostExecute (Void result) {
            // Set values for some global fields
            RecentWorkoutsFragment.this.mRoutines = mRoutines;
            RecentWorkoutsFragment.this.mRoutineIdNameMap = mRoutineIdNameMap;
            RecentWorkoutsFragment.this.mActiveRoutineId = determineActiveRoutineId(mRoutines);

            // Determine the visibility of the Empty Routines views
            setEmptyRoutineViewsVisibility(mActiveRoutineId);

            // Save the active routine Id and name to the preference manager
            SharedPreferences.setActiveRoutineIdAndName(getActivity(), mActiveRoutineId, getRoutineName(mActiveRoutineId, mRoutines));
            // Now that we have our routines we initialize our toolbar
            init_toolbar(getActivity());

            new CreateRoutineTask(mActiveRoutineId).execute();
        }
    }

    // Determine the visibility of the Empty Routines views based on the given RoutineId
    private void setEmptyRoutineViewsVisibility (int routineId) {
        if (routineId != SharedPreferences.NO_ACTIVE_ROUTINE) {
            mEmptyRoutinesView.setVisibility(View.GONE);
            mEmptyRoutinesButton.setVisibility(View.GONE);
            mFloatingActionButton.setVisibility(View.VISIBLE);
        }
        else {
            mEmptyRoutinesView.setVisibility(View.VISIBLE);
            mEmptyRoutinesButton.setVisibility(View.VISIBLE);
            mFloatingActionButton.setVisibility(View.GONE);
        }
    }

    // Determine the visibility of the Empty Routines views based on the routineDays we load in createRoutineObject
    private void setEmptyRoutineDaysViewsVisibility (List<RoutineDay> routineDays) {
        if (routineDays.size() == 0)
            mEmptyRoutineDaysView.setVisibility(View.VISIBLE);
        else
            mEmptyRoutineDaysView.setVisibility(View.GONE);
    }

    // Determine of the visibility of the recent workout cards based on the routineDays we load in createRoutineObject
    private void setRecentWorkoutCardsVisibility (List<RoutineDay> routineDays) {
        for (int i=0; i<MAX_RECENT_WORKOUT_DAYS; i++) {
            RecentWorkoutViews recentWorkoutView = mRecentWorkoutViews.get(i);
            if (i < routineDays.size())
                recentWorkoutView.mCardView.setVisibility(View.VISIBLE);
            else
                recentWorkoutView.mCardView.setVisibility(View.GONE);
        }
    }
}
