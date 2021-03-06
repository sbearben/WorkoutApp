package com.bignerdranch.android.workoutapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bignerdranch.android.workoutapp.global.BasicApp;
import com.bignerdranch.android.workoutapp.global.DataRepository;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.ReppedSet;
import com.bignerdranch.android.workoutapp.model.RoutineDay;
import com.bignerdranch.android.workoutapp.model.Set;
import com.bignerdranch.android.workoutapp.model.TimedSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public abstract class RoutineDayPageFragment extends Fragment {

    private static final String TAG = "RoutineDayPageFragment";

    protected static final String ARG_ROUTINEDAY_ID = "routineday_id";
    protected static final String ARG_TEMPLATE_DAY_IDS = "template_day_ids";
    protected static final String ARG_ROUTINE_NAME = "routine_name";

    private static final String DIALOG_SETS_NUMBER = "DialogSetsNumber";
    private static final int REQUEST_SETS_NUMBER = 0;

    private static final String DIALOG_REPS_NUMBER = "DialogRepsNumber";
    private static final int REQUEST_REPS_NUMBER = 1;

    private static final String DIALOG_WEIGHT_NUMBER = "DialogWeightNumber";
    private static final int REQUEST_WEIGHT_NUMBER = 2;

    private static final String DIALOG_NEW_EXERCISE = "DialogNewExercise";
    private static final int REQUEST_NEW_EXERCISE = 3;

    protected static final String DIALOG_ROUTINEDAY_DATE = "DialogRoutineDayDate";
    protected static final int REQUEST_ROUTINEDAY_DATE = 4;

    protected DataRepository mDataRepository;

    private RecyclerView mExerciseRecyclerView;
    private ExerciseAdapter mAdapter;
    private ItemTouchHelper mSwipeDismiss; // For swiping on an exercise to delete it

    private Exercise mCurrentExercise; // Need this because at one point we need to update an exercise across methods (see onActivityResult(..))
    private Set mCurrentExerciseSet; // Need this because at one point we need to update a set across methods (see onActivityResult(..))
    private ExerciseViews mCurrentExerciseViews;

    private SquareButton mActualMeasurementButton; // Need this because at one point we need to update a button across methods (see onActivityResult(..))
    private TextView mTargetWeightTextView; // Need this because at one point we need to update a set's weight textview across methods (see onActivityResult(..))

    protected int mRoutineDayId, mLoadedRoutineDayId; // NOTE: THIS ID WON'T CHANGE THROUGHOUT THE LIFE OF THE FRAGMENT - WE USE mRoutineDay.getId() when we want the ID in the future
    protected RoutineDay mRoutineDay;
    protected ArrayList<Integer> mTemplateDayIds;
    protected String mRoutineName;
    protected Date mRoutineDayDate; // Need this due to complications with normal and template days, for the Date TextView in ToolBar

    protected boolean mIsInitialSpinnerSelection = true; // boolean to make sure the Spinner callback onItemSelected doesn't execute its contained code when we initialize its first item with setSelection()


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // let the FragmentManager know that this fragment needs to receive menu callbacks

        mRoutineDayId = (int) getArguments().getSerializable(ARG_ROUTINEDAY_ID);
        mTemplateDayIds = (ArrayList<Integer>) getArguments().getSerializable(ARG_TEMPLATE_DAY_IDS);
        mRoutineName = (String) getArguments().getSerializable(ARG_ROUTINE_NAME);

        mLoadedRoutineDayId = mRoutineDayId; // We make a copy of the RoutineDayId that we keep constant throughout, so that if we change a completed day to a template day, we save it using the correct ID

        // Get our DataRepository so that we can easily execute our Db queries
        Application application = getActivity().getApplication();
        mDataRepository = ((BasicApp) application).getRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_routineday_page, container, false);

        mExerciseRecyclerView = (RecyclerView) v.findViewById(R.id.routineday_page_recycler_view);
        mExerciseRecyclerView.setLayoutManager (new LinearLayoutManager(getActivity()));

        mIsInitialSpinnerSelection = true;

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setup our custom toolbar and Spinner
        init_toolbar(getActivity());
        new CreateRoutineDayTask(mRoutineDayId).execute();
    }

    // Abstract class that subclasses must implement that determines what gets done when the fragment lifecycle method onPause() is called
    public abstract void onPauseActionsPerformed();

    /* We want to write the RoutineDay back to the Db when onPause() is called. The tutorial located in the following link suggests
       this is the best place to do that: https://www.tutorialspoint.com/android/android_fragments.htm
       - and frankly calling it here was the only way that worked properly for my needs of the previous fragment getting the updated data */
    @Override
    public void onPause() {
        super.onPause();
        onPauseActionsPerformed();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_routineday_page, menu);
    }

    // Abstract class that subclasses must implement that determines what gets done when the delete_routine menu item is selected
    public abstract void onDeleteRoutineDayMenuItemSelected();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_exercise:
                FragmentManager manager = getFragmentManager();
                NewExerciseFragment dialog = NewExerciseFragment.newInstance();

                // we use setTargetFragment to later get the new exercise data from the NewExerciseFragment (similar to startActivityForResult(..) used by Activities)
                dialog.setTargetFragment (RoutineDayPageFragment.this, REQUEST_NEW_EXERCISE);
                dialog.show (manager, DIALOG_NEW_EXERCISE);
                return true;
            case R.id.delete_routineday:
                onDeleteRoutineDayMenuItemSelected();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Abstract method to setup/initialize the fragment's custom toolbar - if any
    @SuppressLint("RestrictedApi") // without this we get an error calling setDefaultDisplayHomeAsUpEnabled(true) - solution found here: https://stackoverflow.com/a/44926919/7648952
    public abstract void init_toolbar (Context context);

    // Abstract method to update the fragment's custom toolbar - if any
    public abstract void updateToolbar();

    // This method, amongst other things, connects the Adapter to our RecyclerView
    public void updateUI() {
        List<Exercise> exercises = new ArrayList<>();
        if (mRoutineDay != null) {
            exercises = mRoutineDay.getExercises();
        }

        /* Only want to create a new Adapter if this is the first time updateUI is called
           - if not we call notifyDataSetChanged() to update the UI */
        if (mAdapter == null) {
            mAdapter = new ExerciseAdapter(exercises);
            mExerciseRecyclerView.setAdapter(mAdapter);

            mSwipeDismiss = new ItemTouchHelper (new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    if (direction == ItemTouchHelper.RIGHT) {
                        List<Exercise> exercises = mAdapter.mExercises;
                        int exerciseNum = viewHolder.getAdapterPosition();
                        Exercise exercise = exercises.get(exerciseNum);

                        // Update the exercise number field for our remaining exercises
                        for (int i=exercise.getNumber(); i<exercises.size(); i++) {
                            exercises.get(i).setNumber(i);
                        }

                        // Delete the exercise from our DB
                        new Thread(() -> { mDataRepository.deleteExercise(exercise); }).start(); // Background thread to delete swiped on Exercise
                        exercises.remove(exercise); // Remove the exercise from our local RoutineDay object's list of exercises

                        updateUI();
                    }
                }
            });

            mSwipeDismiss.attachToRecyclerView(mExerciseRecyclerView);
        }
        else {
            mAdapter.setExercises(exercises);
            mAdapter.notifyDataSetChanged();
        }
    }

    // Creates a full RoutineDay object if all the required data exists
    private RoutineDay createRoutineDayObject (int routineDayId) {
        RoutineDay routineDay = null;

        routineDay = mDataRepository.loadRoutineDay(routineDayId);

        List<Exercise> dayExercises = mDataRepository.loadAllExercisesInRoutineDay(routineDayId);
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

        // Log.i (TAG, routineDay.toString());

        return routineDay;
    }

    // Write our RoutineDay object back to the DB when we back out of the fragment (or the fragment view is destroyed)
    private void writeRoutineDayObject (RoutineDay routineDay) {

        long routineId = mDataRepository.insertRoutineDay(routineDay);
        routineDay.setId((int) routineId);

        List<Exercise> dayExercises = routineDay.getExercises();
        for (Exercise exercise : dayExercises) { // Set the routineDayId of each of our exercise to the correct parent id
            exercise.setRoutineDayId((int) routineId);
        }
        long[] exerciseIds = mDataRepository.insertExercises(dayExercises);

        // Get sets in Exercises
        for (Exercise exercise : routineDay.getExercises()) {
            int exerciseId = (int) exerciseIds[routineDay.getExercises().indexOf(exercise)];
            exercise.setId(exerciseId); // Set the ID of each exercise

            List<Set> exerciseSets = exercise.getSets();
            for (Set exerciseSet : exerciseSets) { // Set the exerciseId of each of our sets to the correct parent id
                exerciseSet.setExerciseId(exerciseId);
            }

            if (exercise.getType().equals(Exercise.REPPED)) {
                // Solution to cast list of supertype List<Set> to list of subtype List<ReppedSet> found here:
                // - https://stackoverflow.com/a/933600/7648952
                List<ReppedSet> reppedSets = (List<ReppedSet>)(List<?>) exerciseSets;

                long[] reppedSetIds = mDataRepository.insertReppedSets(reppedSets);
                for (ReppedSet reppedSet : reppedSets) {
                    reppedSet.setId ((int) reppedSetIds[reppedSets.indexOf(reppedSet)]); // Set the ID of each ReppedSet
                }
            }
            else {
                List<TimedSet> timedSets = (List<TimedSet>)(List<?>) exerciseSets;

                long[] timedSetIds = mDataRepository.insertTimedSets(timedSets);
                for (TimedSet timedSet : timedSets) {
                    timedSet.setId ((int) timedSetIds[timedSets.indexOf(timedSet)]); // Set the ID of each TimedSet
                }
            }
        }
    }

    // For getting the results back from the Picker Dialogs
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_REPS_NUMBER) {
            ReppedSet reppedSet = (ReppedSet) mCurrentExerciseSet;
            int result = (int) data.getSerializableExtra(NumberPickerFragment.EXTRA_NUMBER);
            reppedSet.setTargetMeasurement(result);

            // Check if the new target number of reps is less than the actual amount we have set; if so we have to update actual reps = target reps
            if (result < reppedSet.getActualMeasurement()) {
                reppedSet.setActualMeasurement(result);
                mActualMeasurementButton.setText(reppedSet.actualMeasurementString());
            }
        }
        else if (requestCode == REQUEST_WEIGHT_NUMBER) {
            int result = (int) data.getSerializableExtra(NumberPickerFragment.EXTRA_NUMBER);
            mCurrentExerciseSet.setTargetWeight(result);

            String weightString = mCurrentExerciseSet.getTargetWeight() + "lbs"; // Using this variable since performing string concat in setText() makes Android Studio whine
            mTargetWeightTextView.setText(weightString); // TODO: Remove hardcoded lbs and make flexible for both lbs and kg
        }
        else if (requestCode == REQUEST_SETS_NUMBER) {
            int result = (int) data.getSerializableExtra(NumberPickerFragment.EXTRA_NUMBER);
            int diff = result - mCurrentExercise.getTargetNumberSets();

            String setsStr = "Sets: " + result; // TODO: Need to create string resource for this
            mCurrentExerciseViews.mExerciseSetsTextView.setText(setsStr);

            if (diff < 0) {
                deleteSets (mDataRepository, mCurrentExerciseViews, mCurrentExercise, mCurrentExercise.getTargetNumberSets()+diff, mCurrentExercise.getTargetNumberSets());
            }
            else if (diff > 0) {
                insertSets (mDataRepository, mCurrentExerciseViews, mCurrentExercise, mCurrentExercise.getTargetNumberSets(), mCurrentExercise.getTargetNumberSets()+diff);
            }
        }
        else if (requestCode == REQUEST_NEW_EXERCISE) {
            String exerciseName = (String) data.getSerializableExtra(NewExerciseFragment.EXTRA_EXERCISE_NAME);
            int numberSets = (int) data.getSerializableExtra(NewExerciseFragment.EXTRA_EXERCISE_SETS);
            String exerciseType = (String) data.getSerializableExtra(NewExerciseFragment.EXTRA_EXERCISE_TYPE);

            Exercise exercise = new Exercise();
            exercise.setRoutineDayId(mRoutineDayId);
            exercise.setNumber(mRoutineDay.getExercises().size()+1);
            exercise.setName(exerciseName);
            exercise.setTargetNumberSets(numberSets);

            // Make sure the exercise type is valid, if not we default to Repped exercise type
            if (Exercise.isValidExerciseType(exerciseType))
                exercise.setType(exerciseType);
            else
                exercise.setType(Exercise.REPPED);

            // Use an Async to insert our new Exercise, which will return its ID, the use that ID when we create its Sets in postExecute()
            //new WriteNewExerciseTask(exercise).execute();

            for (int i=0; i<exercise.getTargetNumberSets(); i++) {
                Set exerciseSet = Set.newDefaultInstance(exercise.getType(), i+1, 0);
                exercise.addSet((Set) exerciseSet);
                Log.i(TAG, "Repped set added: " + i);
            }

            mRoutineDay.addExercise(exercise);
            updateUI();
        }
        else if (requestCode == REQUEST_ROUTINEDAY_DATE) {
            Date date = (Date) data.getSerializableExtra (DatePickerFragment.EXTRA_DATE);
            mRoutineDayDate = date;

            if (!mRoutineDay.isTemplate())
                mRoutineDay.setDate(mRoutineDayDate);
            updateToolbar();
        }

        updateUI();
    }

    private void deleteSets (DataRepository dataRepository,  ExerciseViews exerciseViews, Exercise exercise, int start, int end) {
        exercise.setTargetNumberSets(start);

        for (int i=start; i<end; i++) {
            Set exerciseSet = exercise.getSets().get(start); // We need to use "start" as the index since we are removing sets from the list at the end of this loop (if we use 'i' we get out of bounds error)
            exerciseViews.mSetViews.get(i).redrawDisabledViews(); // Set all the UI elements of that set to their disabled views

            if (exercise.getType().equals(Exercise.REPPED)) { // TODO: I don't love this code
                ReppedSet reppedSet = (ReppedSet) exerciseSet;
                new Thread(() -> {
                    dataRepository.deleteReppedSet(reppedSet); // Delete the set from the DB (we need to do this here to make sure it gets deleted)
                    dataRepository.updateExercise(exercise); // Also want to make sure if we're deleting sets we should update the exercise
                }).start();
            }
            else {
                TimedSet timedSet = (TimedSet) exerciseSet;
                new Thread(() -> {
                    dataRepository.deleteTimedSet(timedSet); // Delete the set from the DB (we need to do this here to make sure it gets deleted)
                    dataRepository.updateExercise(exercise); // Also want to make sure if we're deleting sets we should update the exercise
                }).start();
            }
            exercise.getSets().remove(exerciseSet);
        }
    }

    private void insertSets (DataRepository dataRepository,  ExerciseViews exerciseViews, Exercise exercise, int start, int end) {
        exercise.setTargetNumberSets(end); // Set out new number of sets on our Exercise object
        Set lastSet = exercise.getSets().get(start-1); // Get the last set in our current version of the Exercise object so we can base the new sets on the old one

        for (int i=start; i<end; i++) {
            if (exercise.getType().equals(Exercise.REPPED)) { // TODO: I don't love this code
                ReppedSet lastReppedSet = (ReppedSet) lastSet;
                ReppedSet reppedSet = new ReppedSet (exercise.getId(), i+1, lastReppedSet.getTargetWeight(), lastReppedSet.getTargetMeasurement(), ReppedSet.ACTUAL_REPS_NULL);

                //new Thread(() -> { dataRepository.insertReppedSet(reppedSet); }).start(); // Not sure if I need to do this here (since writing to Db when we back out)
                exercise.getSets().add ((Set) reppedSet);
            }
            else {
                TimedSet timedSet = new TimedSet (exercise.getId(), i+1, 0,
                        new GregorianCalendar(0, 0, 0, 0, 0, 30).getTime(),
                        null); // TODO: need to add equivalent to ACTUAL_REPS_NULL to TimedSet

                //new Thread(() -> { dataRepository.insertTimedSet(timedSet); }).start(); // Not sure if I need to do this here (since writing to Db when we back out)
                exercise.getSets().add ((Set) timedSet);
            }
        }
    }

    // Abstract method that determines what the fragment wants to do when the ActualMeasurementButton is clicked (bit of misnomer since the click doesn't necessarily always modify the set's actualMeasurement field)
    public abstract View.OnClickListener actualMeasurementButtonClick (Exercise exercise, ExerciseViews.SetViews setViews, int setIndex, boolean setExists);

    // Abstract method that determines what String we want displayed inside the actualMeasurementButtons (again, is a misnomer)
    public abstract String actualButtonString (Set exerciseSet);

    // Abstract method that determines whether we want the background of the actualMeasurementButtons to be changeable depending on if the set actual measurement field is null or not
    public abstract boolean actualButtonBackgroundChangeable();

    // Define the ViewHolder that will inflate and own the list_item_exercise.xml layout
    private class ExerciseHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ExerciseViews mExerciseViews;
        private Exercise mExercise;

        public ExerciseHolder (LayoutInflater inflater, ViewGroup parent) {
            super (inflater.inflate (R.layout.list_item_exercise, parent, false));
            //itemView.setOnClickListener (this);

            mExerciseViews = new ExerciseViews();

            int resId = mExerciseViews.resIdGenerator("exercise_name_textview");
            mExerciseViews.mExerciseNameTextView = (TextView) itemView.findViewById(resId);

            resId = mExerciseViews.resIdGenerator("exercise_sets_button");
            mExerciseViews.mExerciseSetsTextView = (TextView) itemView.findViewById(resId);

            for (int i=0; i < Exercise.MAX_SETS; i++) {
                ExerciseViews.SetViews setViews = mExerciseViews.mSetViews.get(i);

                resId = mExerciseViews.resIdGenerator("set" + (i+1) + "_button");
                setViews.mActualMeasurementButton = (SquareButton) itemView.findViewById(resId);

                resId = mExerciseViews.resIdGenerator("set" + (i+1) + "_weight_textview");
                setViews.mTargetWeightTextView = (TextView) itemView.findViewById(resId);

                resId = mExerciseViews.resIdGenerator("set" + (i+1) + "_weight_underline");
                setViews.mTargetWeightUnderlineView = (View) itemView.findViewById(resId);
            }
        }

        // This bind(Exercise) method will be called each time a new Exercise should be displayed in our ExerciseHolder.
        public void bind (@NonNull Exercise exercise) {
            mExercise = exercise; // this is our local ExerciseHolder variable that we use throughout the rest of this method (THIS IS IMPORTANT AND REQUIRED)

            mExerciseViews.mExerciseNameTextView.setText(mExercise.getName());
            String setsStr = "Sets: " + mExercise.getTargetNumberSets(); // TODO: need to create string resource for this
            mExerciseViews.mExerciseSetsTextView.setText(setsStr);

            // Set up our click listener for the number of sets
            mExerciseViews.mExerciseSetsTextView.setOnClickListener((View v) -> {
                mCurrentExercise = exercise; // this is our EditRoutineDayFragment global exercise variable that we also need in onActivityResult(...)
                mCurrentExerciseViews = mExerciseViews; // this is our EditRoutineDayFragment global ExerciseViews variable that we also need in onActivityResult(...)

                FragmentManager manager = getFragmentManager();
                NumberPickerFragment dialog = NumberPickerFragment.newInstance(mExercise.getTargetNumberSets(),
                        1, 5, "", "Number of Sets:"); // TODO; shouldn't have hardcoded strings

                // we use setTargetFragment to later get the new number from the NumberPickerFragment (similar to startActivityForResult(..) used by Activities)
                dialog.setTargetFragment (RoutineDayPageFragment.this, REQUEST_SETS_NUMBER);
                dialog.show (manager, DIALOG_SETS_NUMBER);
            });

            for (int i=0; i < Exercise.MAX_SETS; i++) {
                final int i_copy = i; // // Needed to create this variable because can'tS use 'i' in the onClickListeners since 'i' must be final
                boolean setExists = mExercise.getSets().size() > i; // boolean to check if the set exists
                Log.i (TAG, "Set exists: " + setExists + "  Sets size: " + mExercise.getSets().size());
                ExerciseViews.SetViews setViews = mExerciseViews.mSetViews.get(i);

                /* Check if the relevant exercise set exists, if it doesn't we disable the "set" (which includes button and text) and display a dash for the
                   weight TextView (all proceeding set views will get the same treatment) */
                if (setExists) {
                    Set exerciseSet = mExercise.getSets().get(i);
                    setViews.redrawEnabledViews(exerciseSet, actualButtonString(exerciseSet), actualButtonBackgroundChangeable());
                }
                // This set doesn't exist so we disable the set by changing its look
                else {
                    setViews.redrawDisabledViews();
                }

                // Use of abstract implementation of the onClickListener for mActualMeasurementButton
                setViews.mActualMeasurementButton.setOnClickListener(actualMeasurementButtonClick(mExercise, setViews, i_copy, setExists));

                // Set longClickListener on our set buttons
                setViews.mActualMeasurementButton.setOnLongClickListener((View v) -> {

                    mActualMeasurementButton = (SquareButton) v;

                    if (setExists) { // Not sure if I need these check since the button should be disabled at this point if the set doesn't exist
                        mCurrentExerciseSet = mExercise.getSets().get(i_copy); // Need to use a global mCurrentExerciseSet because we need to update the set in another method (onActivityResult(..))
                        FragmentManager manager = getFragmentManager();

                        if (exercise.getType().equals(Exercise.REPPED)) { // TODO: need to add TimedSet implementation
                            ReppedSet reppedSet = (ReppedSet) mCurrentExerciseSet;

                            NumberPickerFragment dialog = NumberPickerFragment.newInstance(reppedSet.getTargetMeasurement(),
                                    1, 100, "", "Target Reps:"); // TODO; shouldn't have hardcoded strings

                            // we use setTargetFragment to later get the new number from the NumberPickerFragment (similar to startActivityForResult(..) used by Activities)
                            dialog.setTargetFragment (RoutineDayPageFragment.this, REQUEST_REPS_NUMBER);
                            dialog.show (manager, DIALOG_REPS_NUMBER);
                        }
                    }

                    return true;
                });

                setViews.mTargetWeightTextView.setOnClickListener((View v) -> {

                    mTargetWeightTextView = (TextView) v;

                    if (setExists) { // Not sure if I need these check since the button should be disabled at this point if the set doesn't exist
                        mCurrentExerciseSet = mExercise.getSets().get(i_copy); // Need to use a global mCurrentExerciseSet because we need to update the set in another method (onActivityResult(..))
                        FragmentManager manager = getFragmentManager();

                        NumberPickerFragment dialog = NumberPickerFragment.newInstance(mCurrentExerciseSet.getTargetWeight(),
                                0, 5000, "lbs", "Target Weight:"); // TODO; Remove hardcoded lbs and make flexible for both lbs and kg

                        // we use setTargetFragment to later get the new number from the NumberPickerFragment (similar to startActivityForResult(..) used by Activities)
                        dialog.setTargetFragment (RoutineDayPageFragment.this, REQUEST_WEIGHT_NUMBER);
                        dialog.show (manager, DIALOG_WEIGHT_NUMBER);
                    }
                });
            }
        }

        // Called when the user clicks on one of the items in the list (held in itemView, which is the View for the entire row)
        @Override
        public void onClick (View view) {
        }
    }

    private class ExerciseAdapter extends RecyclerView.Adapter<ExerciseHolder> {

        private List<Exercise> mExercises;

        public ExerciseAdapter (List<Exercise> exercises) {
            mExercises = exercises;
        }

        // This method is called by the RecyclerView when it needs a new ViewHolder to display an item with
        @Override
        @NonNull
        public ExerciseHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ExerciseHolder (layoutInflater, parent);
        }

        // This method calls bind(Exercise) each time the RecyclerView requests that a given ExerciseHolder be bound to a particular exercise
        // - in order to keep the scrolling animation smooth, keep this method small and efficient doing only the min amount of necessary work here
        @Override
        public void onBindViewHolder (@NonNull ExerciseHolder holder, int position) {
            Exercise exercise = mExercises.get(position);
            holder.bind(exercise);
        }

        @Override
        public int getItemCount() {
            return mExercises.size();
        }

        public void setExercises (List<Exercise> exercises) {
            mExercises = exercises;
        }
    }

    // Inner class that holds all the Views in each ViewHolder in a structured way
    protected class ExerciseViews {
        private static final String VIEW_ID_PREFIX = "routineday_page_";

        private TextView mExerciseNameTextView;
        private TextView mExerciseSetsTextView;
        private List<SetViews> mSetViews;


        private ExerciseViews() {
            mSetViews = new ArrayList<>(Exercise.MAX_SETS);
            // Each exercise will have 5 SetViews - need to create them and add to our list
            for (int i=0; i<Exercise.MAX_SETS; i++) {
                mSetViews.add(new SetViews());
            }
        }

        private int resIdGenerator (String suffix) {
            return getResources().getIdentifier(VIEW_ID_PREFIX + suffix, "id", getActivity().getPackageName());
        }

        // Need this method because outside of this class we can't create an SetViews object since it isn't static, and we can't make it static since it's an inner class
        private void addEmptySetView() {
            SetViews setViews = new SetViews();
            mSetViews.add(setViews);
            //return mSetViews.size();
        }

        protected class SetViews {
            private SquareButton mActualMeasurementButton;
            private TextView mTargetWeightTextView;
            private View mTargetWeightUnderlineView;

            private SetViews() { }

            public void redrawEnabledViews (Set exerciseSet, String buttonString, boolean changeableBackground) {
                mActualMeasurementButton.setEnabled(true);
                mActualMeasurementButton.setText(buttonString);

                // Check if our set was performed or skipped
                if (!exerciseSet.isSetNull() || !changeableBackground) {
                    mActualMeasurementButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_exercise_set_performed));
                }
                // Our set is currently set to not performed (or skipped)
                else {
                    mActualMeasurementButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_exercise_set_notperformed));
                }

                mTargetWeightTextView.setEnabled(true);
                String weightString = exerciseSet.getTargetWeight() + "lbs"; // Using this variable since performing string concat in setText() makes Android Studio whine
                mTargetWeightTextView.setText(weightString); // TODO: Remove hardcoded lbs and make flexible for both lbs and kg

                mTargetWeightUnderlineView.setEnabled(true);
            }

            public void redrawDisabledViews () {
                mActualMeasurementButton.setEnabled(false);
                mActualMeasurementButton.setText("✕");

                mTargetWeightTextView.setEnabled(false);
                mTargetWeightTextView.setText("‒"); // TODO: Should maybe remove all hardcoded strings and move them to resources (locale/language concerns)

                mTargetWeightUnderlineView.setEnabled(false);
            }
        }
    }

    // Create a background thread to perform the queries to create the RoutineDay object
    protected class CreateRoutineDayTask extends AsyncTask<Void, Void, RoutineDay> {

        private int mRoutineDayId;

        public CreateRoutineDayTask (int routineId) {
            mRoutineDayId = routineId;
        }

        @Override
        protected RoutineDay doInBackground (Void... params) {
            return createRoutineDayObject(mRoutineDayId);
        }

        @Override
        protected void onPostExecute (RoutineDay routineDay) {
            mRoutineDay = routineDay;

            if (mRoutineDay.getDate() != null)
                mRoutineDayDate = mRoutineDay.getDate(); // Get the date for our global variable mRoutineDayDate

            updateUI();
            updateToolbar();
        }
    }

    // Create a background thread to perform the queries to write the RoutineDay object to our Db
    protected class WriteRoutineDayTask extends AsyncTask<Void, Void, Void> {

        private RoutineDay mRoutineDay;

        public WriteRoutineDayTask (RoutineDay routineDay) {
            mRoutineDay = routineDay;
        }

        @Override
        protected Void doInBackground (Void... params) {
            writeRoutineDayObject(mRoutineDay);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

    /* This is used for putting a NEW exercise in our Db, since we need it's ID returned from the insert
       statement so we can give it to the exercise's newly created Sets (called in postExecute())
     */
    private class WriteNewExerciseTask extends AsyncTask<Void, Void, Long> {

        private Exercise mExercise;

        public WriteNewExerciseTask (Exercise exercise) {
            mExercise = exercise;
        }

        @Override
        protected Long doInBackground (Void... params) {
            return mDataRepository.insertExercise(mExercise);
        }

        @Override
        protected void onPostExecute (Long exerciseId) {
            // Here we are creating the Sets for the new exercise, using the ID that was just returned to us by the Db insert query
            mExercise.setId(exerciseId.intValue());
            /*if (mExercise.getType().equals(Exercise.REPPED)) {
                for (int i=0; i<mExercise.getTargetNumberSets(); i++) {
                    ReppedSet reppedSet = new ReppedSet (mExercise.getId(), i+1, 0, 8, ReppedSet.ACTUAL_REPS_NULL);
                    mExercise.addSet((Set) reppedSet);
                    Log.i (TAG, "Repped set added: " + i);
                }
            }
            else {
                for (int i=0; i<mExercise.getTargetNumberSets(); i++) {
                    TimedSet timedSet = new TimedSet (mExercise.getId(), i+1, 0,
                            new GregorianCalendar(0, 0, 0, 0, 0, 30).getTime(),
                            null); // TODO: need to add equivalent to ACTUAL_REPS_NULL to TimedSet
                    mExercise.addSet((Set) timedSet);
                }
            }*/
            for (int i=0; i<mExercise.getTargetNumberSets(); i++) {
                Set exerciseSet = Set.newDefaultInstance(mExercise.getId(), mExercise.getType(), i+1, 0);
                mExercise.addSet((Set) exerciseSet);
                Log.i(TAG, "Repped set added: " + i);
            }

            mRoutineDay.addExercise(mExercise);
            updateUI();
        }
    }
}
