package com.bignerdranch.android.workoutapp;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.android.workoutapp.global.BasicApp;
import com.bignerdranch.android.workoutapp.global.DataRepository;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.ReppedSet;
import com.bignerdranch.android.workoutapp.model.RoutineDay;
import com.bignerdranch.android.workoutapp.model.Set;
import com.bignerdranch.android.workoutapp.model.TimedSet;

import java.util.ArrayList;
import java.util.List;

public class EditRoutineFragment extends Fragment {

    private static final String TAG = "EditRoutineFragment";

    private static final String ARG_ROUTINEDAY_ID = "routineday_id";
    private static final int NEW_ROUTINEDAY = -1;

    private static final String DIALOG_REPS_NUMBER = "DialogRepsNumber";
    private static final int REQUEST_REPS_NUMBER = 0;

    private static final String DIALOG_WEIGHT_NUMBER = "DialogWeightNumber";
    private static final int REQUEST_WEIGHT_NUMBER = 1;

    private DataRepository mDataRepository;

    private RecyclerView mExerciseRecyclerView;
    private ExerciseAdapter mAdapter;

    private Set mExerciseSet; // Need this because at one point we need to update a set across methods (see onActivityResult(..))
    private SquareButton mActualMeasurementButton; // Need this because at one point we need to update a button across methods (see onActivityResult(..))
    private TextView mTargetWeightTextView; // Need this because at one point we need to update a set's weight textview across methods (see onActivityResult(..))

    private int mRoutineDayId;
    private RoutineDay mRoutineDay;


    public static EditRoutineFragment newInstance(int routineId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ROUTINEDAY_ID, routineId);

        EditRoutineFragment fragment = new EditRoutineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRoutineDayId = (int) getArguments().getSerializable(ARG_ROUTINEDAY_ID);

        // Get our DataRepository so that we can easily execute our Db queries
        Application application = getActivity().getApplication();
        mDataRepository = ((BasicApp) application).getRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_routineday_page, container, false);

        mExerciseRecyclerView = (RecyclerView) v.findViewById(R.id.routineday_page_recycler_view);
        mExerciseRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        new CreateRoutineDayTask(mRoutineDayId).execute();

        return v;
    }

    /* We want to write the RoutineDay back to the Db when onPause() is called. The tutorial located in the following link suggests
       this is the best place to do that: https://www.tutorialspoint.com/android/android_fragments.htm
       - and frankly calling it here was the only way that worked properly for my needs of the previous fragment getting the updated data */
    @Override
    public void onPause() {
        super.onPause();
        new WriteRoutineDayTask(mRoutineDay).execute();
    }

    // Creates a full RoutineDay object if all the required data exists
    private RoutineDay createRoutineDayObject(int routineDayId) {
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
                exerciseSets = (List<Set>) (List<?>) mDataRepository.loadAllReppedExerciseSets(exercise.getId());
            } else {
                exerciseSets = (List<Set>) (List<?>) mDataRepository.loadAllTimedExerciseSets(exercise.getId());
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
    private void writeRoutineDayObject(RoutineDay routineDay) {

        mDataRepository.insertRoutineDay(routineDay);

        List<Exercise> dayExercises = routineDay.getExercises();
        mDataRepository.insertExercises(dayExercises);

        // Get sets in Exercises
        for (Exercise exercise : routineDay.getExercises()) {
            List<Set> exerciseSets = exercise.getSets();
            if (exercise.getType().equals(Exercise.REPPED)) {
                // Solution to cast list of supertype List<Set> to list of subtype List<ReppedSet> found here:
                // - https://stackoverflow.com/a/933600/7648952
                List<ReppedSet> reppedSets = (List<ReppedSet>) (List<?>) exerciseSets;
                mDataRepository.insertReppedSets(reppedSets);
            } else {
                List<TimedSet> timedSets = (List<TimedSet>) (List<?>) exerciseSets;
                mDataRepository.insertTimedSets(timedSets);
            }
        }
    }

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
        } else {
            mAdapter.setExercises(exercises);
            mAdapter.notifyDataSetChanged();
        }
    }

    // For getting the results back from the Picker Dialogs
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_REPS_NUMBER) {
            ReppedSet reppedSet = (ReppedSet) mExerciseSet;
            int result = (int) data.getSerializableExtra(NumberPickerFragment.EXTRA_NUMBER);
            reppedSet.setTargetMeasurement(result);

            // Check if the new target number of reps is less than the actual amount we have set; if so we have to update actual reps = target reps
            if (result < reppedSet.getActualMeasurement()) {
                reppedSet.setActualMeasurement(result);
                mActualMeasurementButton.setText(reppedSet.actualMeasurementString());
            }
        } else if (requestCode == REQUEST_WEIGHT_NUMBER) {
            int result = (int) data.getSerializableExtra(NumberPickerFragment.EXTRA_NUMBER);
            mExerciseSet.setTargetWeight(result);

            String weightString = mExerciseSet.getTargetWeight() + "lbs"; // Using this variable since performing string concat in setText() makes Android Studio whine
            mTargetWeightTextView.setText(weightString); // TODO: Remove hardcoded lbs and make flexible for both lbs and kg

        }
    }

    // Define the ViewHolder that will inflate and own the list_item_exercise.xml layout
    private class ExerciseHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ExerciseViews mExerciseViews;
        private Exercise mExercise;


        public ExerciseHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_exercise, parent, false));
            //itemView.setOnClickListener (this);

            mExerciseViews = new ExerciseViews();

            int resId = mExerciseViews.resIdGenerator("exercise_name_textview");
            mExerciseViews.mExerciseNameTextView = (TextView) itemView.findViewById(resId);

            for (int i = 0; i < Exercise.MAX_SETS; i++) {
                ExerciseViews.SetViews setViews = mExerciseViews.mSetViews.get(i);

                resId = mExerciseViews.resIdGenerator("set" + (i + 1) + "_button");
                setViews.mActualMeasurementButton = (SquareButton) itemView.findViewById(resId);

                resId = mExerciseViews.resIdGenerator("set" + (i + 1) + "_weight_textview");
                setViews.mTargetWeightTextView = (TextView) itemView.findViewById(resId);

                resId = mExerciseViews.resIdGenerator("set" + (i + 1) + "_weight_underline");
                setViews.mTargetWeightUnderlineView = (View) itemView.findViewById(resId);
            }
        }

        // This bind(Exercise) method will be called each time a new Exercise should be displayed in our ExerciseHolder.
        public void bind(@NonNull Exercise exercise) {
            mExercise = exercise;
            mExerciseViews.mExerciseNameTextView.setText(mExercise.getName());

            for (int i = 0; i < Exercise.MAX_SETS; i++) {
                final int i_copy = i; // // Needed to create this variable because can't use 'i' in the onClickListeners since 'i' must be final
                boolean setExists = mExercise.getSets().size() > i; // boolean to check if the set exists
                ExerciseViews.SetViews setViews = mExerciseViews.mSetViews.get(i);

                /* Check if the relevant exercise set exists, if it doesn't we disable the "set" (which includes button and text) and display a dash for the
                   weight TextView (all proceeding set views will get the same treatment) */
                if (setExists) {
                    Set exerciseSet = mExercise.getSets().get(i);
                    setViews.redrawEnabledViews(exerciseSet);
                }
                // This set doesn't exist so we disable the set by changing its look
                else {
                    setViews.redrawDisabledViews();
                }

                // Set all of the button click listeners
                setViews.mActualMeasurementButton.setOnClickListener((View v) -> { // TODO: this code is ugly and seems "anti object-oriented"
                    if (setExists) { // Not sure if I need these check since the button should be disabled at this point if the set doesn't exist
                        Set exerciseSet = mExercise.getSets().get(i_copy);

                        if (exercise.getType().equals(Exercise.REPPED)) { // TODO: need to add TimedSet implementation
                            ReppedSet reppedSet = (ReppedSet) exerciseSet;

                            int new_value = (reppedSet.getActualMeasurement() == ReppedSet.ACTUAL_REPS_NULL) ? reppedSet.getTargetMeasurement() : reppedSet.getActualMeasurement() - 1;
                            reppedSet.setActualMeasurement(new_value);

                            setViews.redrawEnabledViews(reppedSet);

                            // TODO: need to start a timer here (Broadcast Intent?)
                        }
                    }
                });

                // Set longClickListener on our set buttons
                setViews.mActualMeasurementButton.setOnLongClickListener((View v) -> {

                    mActualMeasurementButton = (SquareButton) v;

                    if (setExists) { // Not sure if I need these check since the button should be disabled at this point if the set doesn't exist
                        mExerciseSet = mExercise.getSets().get(i_copy); // Need to use a global mExerciseSet because we need to update the set in another method (onActivityResult(..))
                        FragmentManager manager = getFragmentManager();

                        if (exercise.getType().equals(Exercise.REPPED)) { // TODO: need to add TimedSet implementation
                            ReppedSet reppedSet = (ReppedSet) mExerciseSet;

                            NumberPickerFragment dialog = NumberPickerFragment.newInstance(reppedSet.getTargetMeasurement(),
                                    1, 100, "", "Target Reps:"); // TODO; shouldn't have hardcoded strings

                            // we use setTargetFragment to later get the new number from the NumberPickerFragment (similar to startActivityForResult(..) used by Activities)
                            dialog.setTargetFragment(EditRoutineFragment.this, REQUEST_REPS_NUMBER);
                            dialog.show(manager, DIALOG_REPS_NUMBER);
                        }
                    }

                    return true;
                });

                setViews.mTargetWeightTextView.setOnClickListener((View v) -> {

                    mTargetWeightTextView = (TextView) v;

                    if (setExists) { // Not sure if I need these check since the button should be disabled at this point if the set doesn't exist
                        mExerciseSet = mExercise.getSets().get(i_copy); // Need to use a global mExerciseSet because we need to update the set in another method (onActivityResult(..))
                        FragmentManager manager = getFragmentManager();

                        NumberPickerFragment dialog = NumberPickerFragment.newInstance(mExerciseSet.getTargetWeight(),
                                0, 9999, "lbs", "Target Weight:"); // TODO; Remove hardcoded lbs and make flexible for both lbs and kg

                        // we use setTargetFragment to later get the new number from the NumberPickerFragment (similar to startActivityForResult(..) used by Activities)
                        dialog.setTargetFragment(EditRoutineFragment.this, REQUEST_WEIGHT_NUMBER);
                        dialog.show(manager, DIALOG_WEIGHT_NUMBER);
                    }
                });
            }
        }

        // Called when the user clicks on one of the items in the list (held in itemView, which is the View for the entire row)
        @Override
        public void onClick(View view) {
        }
    }

    private class ExerciseAdapter extends RecyclerView.Adapter<ExerciseHolder> {

        private List<Exercise> mExercises;

        public ExerciseAdapter(List<Exercise> exercises) {
            mExercises = exercises;
        }

        // This method is called by the RecyclerView when it needs a new ViewHolder to display an item with
        @Override
        @NonNull
        public ExerciseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ExerciseHolder(layoutInflater, parent);
        }

        // This method calls bind(Exercise) each time the RecyclerView requests that a given ExerciseHolder be bound to a particular exercise
        // - in order to keep the scrolling animation smooth, keep this method small and efficient doing only the min amount of necessary work here
        @Override
        public void onBindViewHolder(@NonNull ExerciseHolder holder, int position) {
            Exercise exercise = mExercises.get(position);
            holder.bind(exercise);
        }

        @Override
        public int getItemCount() {
            return mExercises.size();
        }

        public void setExercises(List<Exercise> exercises) {
            mExercises = exercises;
        }
    }

    // Inner class that holds all the Views in each ViewHolder in a structured way
    private class ExerciseViews {
        private static final String VIEW_ID_PREFIX = "routineday_page_";

        private TextView mExerciseNameTextView;
        private List<SetViews> mSetViews;


        private ExerciseViews() {
            mSetViews = new ArrayList<>(Exercise.MAX_SETS);
            // Each exercise will have 5 SetViews - need to create them and add to our list
            for (int i = 0; i < Exercise.MAX_SETS; i++) {
                mSetViews.add(new SetViews());
            }
        }

        private int resIdGenerator(String suffix) {
            return getResources().getIdentifier(VIEW_ID_PREFIX + suffix, "id", getActivity().getPackageName());
        }

        // Need this method because outside of this class we can't create an SetViews object since it isn't static, and we can't make it static since it's an inner class
        private void addEmptySetView() {
            SetViews setViews = new SetViews();
            mSetViews.add(setViews);
            //return mSetViews.size();
        }

        private class SetViews {
            private SquareButton mActualMeasurementButton;
            private TextView mTargetWeightTextView;
            private View mTargetWeightUnderlineView;

            private SetViews() {
            }

            public void redrawEnabledViews(Set exerciseSet) {
                mActualMeasurementButton.setEnabled(true);
                mActualMeasurementButton.setText(exerciseSet.actualMeasurementString());

                // Check if our set was performed or skipped
                if (!exerciseSet.isSetNull()) {
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

            public void redrawDisabledViews() {
                mActualMeasurementButton.setEnabled(false);
                mActualMeasurementButton.setText("✕");

                mTargetWeightTextView.setEnabled(false);
                mTargetWeightTextView.setText("‒"); // TODO: Should maybe remove all hardcoded strings and move them to resources (locale/language concerns)

                mTargetWeightUnderlineView.setEnabled(false);
            }
        }
    }

    // Create a background thread to perform the queries to create the RoutineDay object
    private class CreateRoutineDayTask extends AsyncTask<Void, Void, RoutineDay> {

        private int mRoutineDayId;

        public CreateRoutineDayTask(int routineId) {
            mRoutineDayId = routineId;
        }

        @Override
        protected RoutineDay doInBackground(Void... params) {
            return createRoutineDayObject(mRoutineDayId);
        }

        @Override
        protected void onPostExecute(RoutineDay routineDay) {
            mRoutineDay = routineDay;
            updateUI();
        }
    }

    // Create a background thread to perform the queries to write the RoutineDay object to our Db
    private class WriteRoutineDayTask extends AsyncTask<Void, Void, Void> {

        private RoutineDay mRoutineDay;

        public WriteRoutineDayTask(RoutineDay routineDay) {
            mRoutineDay = routineDay;
        }

        @Override
        protected Void doInBackground(Void... params) {
            writeRoutineDayObject(mRoutineDay);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }
}