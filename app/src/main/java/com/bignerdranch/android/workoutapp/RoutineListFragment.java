package com.bignerdranch.android.workoutapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.android.workoutapp.global.BasicApp;
import com.bignerdranch.android.workoutapp.global.DataRepository;
import com.bignerdranch.android.workoutapp.model.Routine;
import com.bignerdranch.android.workoutapp.model.RoutineDay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class RoutineListFragment extends Fragment {

    private static final String TAG = "RoutineListFragment";

    private static final String DIALOG_NEW_ROUTINE = "DialogNewRoutine";
    private static final int REQUEST_NEW_ROUTINE = 0;

    private DataRepository mDataRepository;

    private RecyclerView mRoutineRecyclerView;
    private RoutineAdapter mAdapter;

    private List<Routine> mRoutines;

    private FloatingActionButton mFloatingActionButton;


    public static RoutineListFragment newInstance() {
        return new RoutineListFragment();
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
        View v = inflater.inflate(R.layout.fragment_routinelist, container, false);

        mRoutineRecyclerView = (RecyclerView) v.findViewById(R.id.routinelist_recycler_view);
        mRoutineRecyclerView.setLayoutManager (new LinearLayoutManager(getActivity()));
        mRoutineRecyclerView.addItemDecoration (new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        // Initializing the FloatingActionButton
        mFloatingActionButton = v.findViewById(R.id.fab_new_routine);
        mFloatingActionButton.setOnClickListener((View view) -> {
            FragmentManager manager = getFragmentManager();
            NewRoutineFragment dialog = NewRoutineFragment.newInstance();

            dialog.setTargetFragment (RoutineListFragment.this, REQUEST_NEW_ROUTINE);
            dialog.show (manager, DIALOG_NEW_ROUTINE);
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setup our custom toolbar and Spinner
        init_toolbar(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        new GetRoutinesTask().execute();
    }

    // Method to setup our custom toolbar and call the method init_spinner(..) in order to set up the spinner
    private void init_toolbar (Context context) {
        ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();

        // Restore the default toolbar
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
    }

    public void updateUI() {
        List<Routine> routines = new ArrayList<>();
        if (mRoutines != null) {
            routines = mRoutines;
        }

        /* Only want to create a new Adapter if this is the first time updateUI is called
           - if not we call notifyDataSetChanged() to update the UI */
        if (mAdapter == null) {
            mAdapter = new RoutineAdapter(routines);
            mRoutineRecyclerView.setAdapter(mAdapter);
        }
        else {
            mAdapter.setRoutines(routines);
            mAdapter.notifyDataSetChanged();
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

            new InsertNewRoutineTask(routine, numberDays).execute();
        }

        updateUI();
    }

    private class RoutineHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mNameTextView;
        private TextView mDateTextView;

        private Routine mRoutine;


        public RoutineHolder (LayoutInflater inflater, ViewGroup parent) {
            super (inflater.inflate (R.layout.list_item_routine, parent, false));
            itemView.setOnClickListener (this);

            mNameTextView = (TextView) itemView.findViewById(R.id.routinelist_routine_name);
            mDateTextView = (TextView) itemView.findViewById(R.id.routinelist_date_created);
        }

        public void bind (@NonNull Routine routine) {
            mRoutine = routine;
            String[] monthNames = { "Jan.", "Feb.", "Mar.", "Apr.", "May", "June", "July", "Aug.", "Sep.", "Oct.", "Nov.", "Dec." };

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mRoutine.getDateCreated());

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            String dateCreated = "Created: " + monthNames[month] + " " + day + ", " + year;

            mNameTextView.setText(mRoutine.getName());
            mDateTextView.setText(dateCreated);
        }

        // Called when the user clicks on one of the items in the list (held in itemView, which is the View for the entire row)
        @Override
        public void onClick (View view) {
            Intent intent = EditRoutineActivity.newIntent(getActivity(), mRoutine.getId());
            startActivity (intent);
        }
    }

    private class RoutineAdapter extends RecyclerView.Adapter<RoutineHolder> {

        private List<Routine> mRoutines;

        public RoutineAdapter (List<Routine> routines) {
            mRoutines = routines;
        }

        // This method is called by the RecyclerView when it needs a new ViewHolder to display an item with
        @Override
        @NonNull
        public RoutineHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new RoutineHolder (layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder (@NonNull RoutineHolder holder, int position) {
            Routine routine = mRoutines.get(position);
            holder.bind(routine);
        }

        @Override
        public int getItemCount() {
            return mRoutines.size();
        }

        public void setRoutines (List<Routine> routines) {
            mRoutines = routines;
        }
    }

    // Create a background thread to perform the query to get our list of Routines
    private class GetRoutinesTask extends AsyncTask<Void, Void, List<Routine>> {

        @Override
        protected List<Routine> doInBackground (Void... params) {
            return mDataRepository.loadRoutines();
        }

        @Override
        protected void onPostExecute (List<Routine> routines) {
            mRoutines = routines;
            updateUI();
        }
    }

    private class InsertNewRoutineTask extends AsyncTask<Void, Void, Routine> {

        private Routine mRoutine;
        private int mNumberDays;

        public InsertNewRoutineTask (Routine routine, int numberDays) {
            mRoutine = routine;
            mNumberDays = numberDays;
        }

        @Override
        protected Routine doInBackground (Void... params) {
            Long routineId = mDataRepository.insertRoutine(mRoutine);
            mRoutine.setId(routineId.intValue());

            // Create the new routine (template) days
            for (int i=0; i<mNumberDays; i++) {
                RoutineDay routineDay = new RoutineDay();
                routineDay.setRoutineId(routineId.intValue());
                routineDay.setDayNumber(i+1);
                routineDay.setDate(null);
                routineDay.setCompleted(false);
                routineDay.setTemplate(true);

                Long routineDayId = mDataRepository.insertRoutineDay(routineDay);
                routineDay.setId(routineDayId.intValue());
                mRoutine.addRoutineDay(routineDay);
            }
            return mRoutine;
        }

        @Override
        protected void onPostExecute (Routine routine) {
            // Start the EditRoutineActivity with our new Routine
            Intent intent = EditRoutineActivity.newIntent(getActivity(), routine.getId());
            startActivity (intent);
        }
    }

}
