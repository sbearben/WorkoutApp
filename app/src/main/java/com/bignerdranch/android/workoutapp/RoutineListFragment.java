package com.bignerdranch.android.workoutapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class RoutineListFragment extends Fragment {

    private static final String TAG = "RoutineListFragment";

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
            // Add and go to new RoutineDay
        });

        new GetRoutinesTask().execute();

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setup our custom toolbar and Spinner
        init_toolbar(getActivity());
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
            //Intent intent = EditRoutineActivity.newIntent(getActivity(), mRoutine.getId());
            //startActivity (intent);
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

}
