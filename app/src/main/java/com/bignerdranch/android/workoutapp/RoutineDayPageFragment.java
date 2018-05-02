package com.bignerdranch.android.workoutapp;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.android.workoutapp.global.BasicApp;
import com.bignerdranch.android.workoutapp.global.DataRepository;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.ReppedSet;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;
import com.bignerdranch.android.workoutapp.model.Set;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoutineDayPageFragment extends Fragment {

    private static final String TAG = "RoutineDayPageFragment";
    private static final String ARG_ROUTINEDAY_ID = "routineday_id";

    private DataRepository mDataRepository;

    private int mRoutineDayId;
    private RoutineDay mRoutineDay;

    private RecyclerView mExerciseRecyclerView;
    private ExerciseAdapter mAdapter;


    public static RoutineDayPageFragment newInstance(int routineDayId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ROUTINEDAY_ID, routineDayId);

        RoutineDayPageFragment fragment = new RoutineDayPageFragment();
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
        mExerciseRecyclerView.setLayoutManager (new LinearLayoutManager(getActivity()));

        /* Background AsynTask that calls createRoutineDayObject(mRoutineDayId) on the background thread and assigns
           the returned RoutineDay to mRoutineDay, then calls updateUI() */
        //new CreateRoutineDayTask(mRoutineDayId);

        mRoutineDay = createRoutineDayObject(mRoutineDayId);
        updateUI();

        return v;
    }

    // Creates a full Routine object if all the required data exists
    private RoutineDay createRoutineDayObject (int routineDayId) {
        RoutineDay routineDay = null;

        routineDay = mDataRepository.loadRoutineDay(routineDayId);

        // Get first three exercises for each day
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

    // This method, amongst other things, connects the Adapter to our RecyclerView
    public void updateUI() {
        List<Exercise> exercises = mRoutineDay.getExercises();

        /* Only want to create a new Adapter if this is the first time updateUI is called
           - if not we call notifyDataSetChanged() to update the UI */
        if (mAdapter == null) {
            mAdapter = new ExerciseAdapter(exercises);
            mExerciseRecyclerView.setAdapter(mAdapter);
        }
        else {
            mAdapter.setExercises(exercises);
            mAdapter.notifyDataSetChanged();
        }
    }

    // Define the ViewHolder that will inflate and own the list_item_crime.xml layout. Define it as an inner class in CrimeListFragment
    private class ExerciseHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ExerciseViews mExerciseViews;
        private Exercise mExercise;


        public ExerciseHolder (LayoutInflater inflater, ViewGroup parent) {
            super (inflater.inflate (R.layout.list_item_exercise, parent, false));
            //itemView.setOnClickListener (this);

            mExerciseViews = new ExerciseViews();

            int resId = mExerciseViews.resIdGenerator("exercise_name_textview");
            mExerciseViews.mExerciseNameTextView = (TextView) itemView.findViewById(resId);

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

        // This bind(Crime) method will be called each time a new Crime should be displayed in our CrimeHolder.
        public void bind (@NonNull Exercise exercise) {
            mExercise = exercise;
            mExerciseViews.mExerciseNameTextView.setText(mExercise.getName());

            for (int i=0; i < Exercise.MAX_SETS; i++) {
                ExerciseViews.SetViews setViews = mExerciseViews.mSetViews.get(i);

                setViews.mActualMeasurementButton.setOnClickListener((View v) -> {
                    // Don't do anything yet
                });

                setViews.mTargetWeightTextView.setOnClickListener((View v) -> {
                    // Don't do anything yet
                });

                /* Check if the relevant exercise set exists, if it doesn't we disable the button and display a dash for the
                   weight TextView (all proceeding set views will get the same treatment) */
                if (mExercise.getSets().size() > i) {
                    Set exerciseSet = mExercise.getSets().get(i);

                    // Check if our set was performed or skipped
                    if (!exerciseSet.isSetNull()) {
                        setViews.mActualMeasurementButton.setEnabled(true);
                        setViews.mActualMeasurementButton.setText(exerciseSet.actualMeasurementString());
                    }
                    // Our set is currently set to skipped
                    else {
                        setViews.mActualMeasurementButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_exercise_set_notperformed));
                    }

                    String weightString = exerciseSet.getTargetWeight() + "lbs"; // Using this variable since performing string concat in setText() makes Android Studio whine
                    setViews.mTargetWeightTextView.setText(weightString); // TODO: Remove hardcoded lbs and make flexible for both lbs and kg
                }
                // This set doesn't exist so we disable the set by changing its look
                else {
                    setViews.mActualMeasurementButton.setEnabled(false);
                    setViews.mActualMeasurementButton.setText("✕");

                    setViews.mTargetWeightTextView.setEnabled(false);
                    setViews.mTargetWeightTextView.setText("‒"); // TODO: Should maybe remove all hardcoded strings and move them to resources (locale/language concerns)

                    setViews.mTargetWeightUnderlineView.setEnabled(false);
                }
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

        // This method calls bind(Crime) each time the RecyclerView requests that a given CrimeHolder be bound to a particular crime
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
    private class ExerciseViews {
        private static final String VIEW_ID_PREFIX = "routineday_page_";

        private TextView mExerciseNameTextView;
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

        private class SetViews {
            private SquareButton mActualMeasurementButton;
            private TextView mTargetWeightTextView;
            private View mTargetWeightUnderlineView;

            private SetViews() { }

            private void disableSetViews() {
                mActualMeasurementButton.setEnabled(false);
                mActualMeasurementButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                mActualMeasurementButton.setText("✕");
            }
        }
    }

    // Create a background thread to perform the queries to create the RoutineDay object
    private class CreateRoutineDayTask extends AsyncTask<Void, Void, RoutineDay> {

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
            updateUI();
        }
    }
}
