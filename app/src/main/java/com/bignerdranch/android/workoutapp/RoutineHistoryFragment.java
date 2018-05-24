package com.bignerdranch.android.workoutapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.bignerdranch.android.workoutapp.global.BasicApp;
import com.bignerdranch.android.workoutapp.global.DataRepository;
import com.bignerdranch.android.workoutapp.model.Exercise;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;
import com.bignerdranch.android.workoutapp.model.Set;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class RoutineHistoryFragment extends Fragment {

    private static final String TAG = "RoutineHistoryFragment";

    private DataRepository mDataRepository;

    private RecyclerView mRoutineDayRecyclerView;
    private RoutineDayAdapter mAdapter;

    private Routine mRoutine;
    private int mRoutineId;
    private List<RoutineDay> mRoutineDays;
    private ArrayList<Integer> mTemplateDayIds;



    public static RoutineHistoryFragment newInstance() {
        return new RoutineHistoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get our DataRepository so that we can easily execute our Db queries
        Application application = getActivity().getApplication();
        mDataRepository = ((BasicApp) application).getRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_routine_history, container, false);

        // Get the currently active Routine ID (selected on the Home tab/RecentWorkoutsFragment)
        mRoutineId = SharedPreferences.getActiveRoutineId(getActivity());

        mRoutineDayRecyclerView = (RecyclerView) v.findViewById(R.id.routine_history_recycler_view);
        // When setting the layout manager we set reverseLayout = true in the LinearLayoutManager constructor so that we start at the end of the 'list'
        mRoutineDayRecyclerView.setLayoutManager (new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));
        mRoutineDayRecyclerView.addItemDecoration (new DividerItemDecoration(getActivity(), LinearLayoutManager.HORIZONTAL));

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setup our custom toolbar (also doing this in onPostExecute() in the Async now since the subtitle we're setting on the toolbar requires mRoutine to be instantiated)
        init_toolbar(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        new CreateRoutineTask(mRoutineId).execute();
    }

    // Creates a full Routine object if all the required data exists
    private Routine createRoutineObject (int routineId) {
        Routine activeRoutine = null;

        // Check that routineId is not set to SharedPreferences.NO_ACTIVE_ROUTINE
        if (routineId != SharedPreferences.NO_ACTIVE_ROUTINE) {
            activeRoutine = mDataRepository.loadRoutine(routineId);

            // Get all the RoutineDay in the Routine
            List<RoutineDay> routineDays = mDataRepository.loadAllCompletedRoutineDaysInRoutine(routineId);
            if (routineDays == null) {
                return null;
            }
            activeRoutine.addRoutineDays(routineDays);

            // Get the Exercises for each RoutineDay
            for (RoutineDay routineDay : activeRoutine.getRoutineDays()) {
                // Get first three exercises for each day
                List<Exercise> dayExercises = mDataRepository.loadAllExercisesInRoutineDay(routineDay.getId());
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
            }
        }

        return activeRoutine;
    }

    // Method to setup our custom toolbar and call the method init_spinner(..) in order to set up the spinner
    private void init_toolbar (Context context) {
        ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();

        // Restore the default toolbar
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);

        if (mRoutine == null)
            actionBar.setSubtitle(R.string.actionbar_routine_history_subtitle_noroutine);
        else
            actionBar.setSubtitle(getString(R.string.actionbar_routine_history_subtitle, mRoutine.getName()));
    }

    public void updateUI() {
        List<RoutineDay> routineDays = new ArrayList<>();
        if (mRoutine != null) {
            routineDays = mRoutine.getRoutineDays();
        }

        /* Only want to create a new Adapter if this is the first time updateUI is called
           - if not we call notifyDataSetChanged() to update the UI */
        if (mAdapter == null) {
            mAdapter = new RoutineDayAdapter(routineDays);
            mRoutineDayRecyclerView.setAdapter(mAdapter);
        }
        else {
            mAdapter.setRoutineDays(routineDays);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class RoutineDayHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mDateTextView;
        private TextView mDetailsTextView;

        private RoutineDay mRoutineDay;


        public RoutineDayHolder (LayoutInflater inflater, ViewGroup parent) {
            super (inflater.inflate (R.layout.list_item_routine_history, parent, false));

            // This code is used to get the screen dimensions of the user's device
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;

            itemView.setOnClickListener (this);
            // Set the ViewHolder width to be a third of the screen size, and height to wrap content
            itemView.setLayoutParams(new RecyclerView.LayoutParams(width/3, RecyclerView.LayoutParams.WRAP_CONTENT));

            mDateTextView = (TextView) itemView.findViewById(R.id.routine_history_routineday_date);
            mDetailsTextView = (TextView) itemView.findViewById(R.id.routine_history_routineday_details);
        }

        public void bind (@NonNull RoutineDay routineDay) {
            mRoutineDay = routineDay;
            String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            String[] monthNames = { "Jan.", "Feb.", "Mar.", "Apr.", "May", "June", "July", "Aug.", "Sep.", "Oct.", "Nov.", "Dec." };

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mRoutineDay.getDate());

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            String dateString = dayNames[dayOfWeek-1] + Exercise.NEWLINE_NORMAL + monthNames[month] + " " + day + ", " + year;

            mDateTextView.setText(dateString);

            StringBuilder sb = new StringBuilder(500); // TODO: unsure of what a reasonable number for the capacity of this StringBuilder should be
            for (Exercise exercise : mRoutineDay.getExercises()) {
                sb.append("<b>"); // We want the exercise name to be bolded in the TextView
                sb.append(exercise.getName());
                sb.append("</b>");
                sb.append(Exercise.NEWLINE_HTML);

                sb.append(Exercise.createExerciseDetailsString(exercise, Exercise.NEWLINE_HTML));
                sb.append(Exercise.NEWLINE_HTML);
                sb.append(Exercise.NEWLINE_HTML);
            }

            mDetailsTextView.setText(Html.fromHtml(sb.toString()));
        }

        // Called when the user clicks on one of the items in the list (held in itemView, which is the View for the entire row)
        @Override
        public void onClick (View view) {
            Intent intent = EditRoutineDayActivity.newIntent(getActivity(), mRoutineDay.getId(), mTemplateDayIds, mRoutine.getName());
            startActivity (intent);
        }
    }

    private class RoutineDayAdapter extends RecyclerView.Adapter<RoutineDayHolder> {

        private List<RoutineDay> mRoutineDays;

        public RoutineDayAdapter (List<RoutineDay> routineDays) {
            mRoutineDays = routineDays;
        }

        // This method is called by the RecyclerView when it needs a new ViewHolder to display an item with
        @Override
        @NonNull
        public RoutineDayHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new RoutineDayHolder (layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder (@NonNull RoutineDayHolder holder, int position) {
            RoutineDay routineDay = mRoutineDays.get(position);
            holder.bind(routineDay);
        }

        @Override
        public int getItemCount() {
            return mRoutineDays.size();
        }

        public void setRoutineDays (List<RoutineDay> routineDays) {
            mRoutineDays = routineDays;
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
            mTemplateDays = mDataRepository.loadTemplateRoutineDays(mRoutineId); // Need to get the ID's of the template days so that we can use them when we add a new RoutineDay
            for (RoutineDay templateDay : mTemplateDays) {
                mTemplateDayIds.add(templateDay.getId());
            }
            return createRoutineObject(mRoutineId);
        }

        @Override
        protected void onPostExecute (Routine routine) {
            mRoutine = routine; // Assign the routine to our global variable mRoutine so we can use outside the AsyncTask
            RoutineHistoryFragment.this.mTemplateDayIds = mTemplateDayIds;

            updateUI();
            // Put this in a try-catch block just since I'm not sure if getActivity() will return non-null since this Async is getting fired off in onResume()
            try {
                init_toolbar(getActivity());
            }
            catch (Exception e) {
                Log.i(TAG, e.getMessage());
            }
        }
    }
}