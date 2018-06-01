package com.bignerdranch.android.workoutapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.bignerdranch.android.workoutapp.model.Exercise;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Armon on 21/03/2018.
 */

public class NewExerciseFragment extends DialogFragment {

    // Name of for the Extras that we're using to pass back the exercise from the this DialogFragment back to the calling Fragment
    public static final String EXTRA_EXERCISE_NAME = "com.bignerdranch.android.workoutapp.exercise_name";
    public static final String EXTRA_EXERCISE_SETS = "com.bignerdranch.android.workoutapp.exercise_sets";
    public static final String EXTRA_EXERCISE_TYPE = "com.bignerdranch.android.workoutapp.exercise_type";

    private TextInputLayout mNameInputLayout;
    private TextInputEditText mNameInputEditText;
    private TextInputLayout mSetsInputLayout;
    private TextInputEditText mSetsInputEditText;
    private AppCompatSpinner mTypeSpinner;

    private String mExerciseType;


    public static NewExerciseFragment newInstance() {
        return new NewExerciseFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_exercise, null);

        mNameInputLayout = (TextInputLayout) v.findViewById(R.id.dialog_new_exercise_name_layout);
        mNameInputEditText = (TextInputEditText) v.findViewById(R.id.dialog_new_exercise_name_edittext);
        mSetsInputLayout = (TextInputLayout) v.findViewById(R.id.dialog_new_exercise_sets_layout);
        mSetsInputEditText = (TextInputEditText) v.findViewById(R.id.dialog_new_exercise_sets_edittext);
        mTypeSpinner = (AppCompatSpinner) v.findViewById(R.id.dialog_new_exercise_type_spinner);

        // Add TextChangedListener for the exercise name edit text
        mNameInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(""))
                    mNameInputLayout.setError(getString(R.string.new_exercise_name_empty_error));
                else
                    mNameInputLayout.setError(null);
            }
        });

        // Add TextChangedListener for the exercise sets edit text
        mSetsInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(""))
                    mSetsInputLayout.setError(getString(R.string.new_exercise_sets_empty_error, Exercise.MAX_SETS));
                else if (Integer.parseInt(s.toString()) < 1 || Integer.parseInt(s.toString()) > Exercise.MAX_SETS)
                    mSetsInputLayout.setError(getString(R.string.new_exercise_sets_empty_error, Exercise.MAX_SETS));
                else
                    mSetsInputLayout.setError(null);
            }
        });

        // Don't love the way this gets options for exercise type - not sure of an alternative
        List<String> exerciseTypes = new ArrayList<>();
        exerciseTypes.add(Exercise.REPPED);
        exerciseTypes.add(Exercise.TIMED);

        // TODO: change this once compatibility for TimedSets is added
        // This custom ArrayAdapter greys out and disables the Timed exercise type option since this app can't handle that yet
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_spinner_item, exerciseTypes) {
            @Override
            public boolean isEnabled(int position) {
                return !(exerciseTypes.get(position).equals(Exercise.TIMED));
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View mView = super.getDropDownView(position, convertView, parent);
                TextView mTextView = (TextView) mView;
                if (exerciseTypes.get(position).equals(Exercise.TIMED)) {
                    mTextView.setTextColor(Color.GRAY);
                } else {
                    mTextView.setTextColor(Color.BLACK);
                }
                return mView;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(adapter);

        // Item selection listener for the Spinner
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mExerciseType = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final AlertDialog newExerciseAlertDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.new_exercise_title)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        /* We need to set the onShowListener, and then within it set the positive button onClickListener in order to make sure that the dialog
           doesn't dismiss when the user clicks "ok" when the input isn't valid
           - found here: https://stackoverflow.com/a/26087003/7648952 */
        newExerciseAlertDialog.setOnShowListener((DialogInterface dialog) -> {
            Button b = newExerciseAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

            b.setOnClickListener((View view) -> {
                String name = mNameInputEditText.getText().toString();
                String setsStr = mSetsInputEditText.getText().toString();
                int setsNumber;
                boolean invalidInput = false;

                if (name.equals("")) {
                    mNameInputLayout.setError(getString(R.string.new_exercise_name_empty_error));
                    invalidInput = true;
                }
                if (setsStr.equals("")) {
                    mSetsInputLayout.setError(getString(R.string.new_exercise_sets_empty_error, Exercise.MAX_SETS));
                    return; // Don't need to set the invalidInput flag here because if we reached this point we've already set all the error messages and can return
                }
                setsNumber = Integer.parseInt(setsStr);

                if (!invalidInput && (setsNumber > 0 && setsNumber <= Exercise.MAX_SETS)) {
                    sendResult(Activity.RESULT_OK, name, Integer.parseInt(setsStr), mExerciseType);
                    newExerciseAlertDialog.dismiss();
                }
            });
        });

        return newExerciseAlertDialog;
    }

    // Used to send the result of the date we pick in the DatePicker back to CrimeFragment
    private void sendResult (int resultCode, String exerciseName, int numberSets, String exerciseType) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_EXERCISE_NAME, exerciseName);
        intent.putExtra(EXTRA_EXERCISE_SETS, numberSets);
        intent.putExtra(EXTRA_EXERCISE_TYPE, exerciseType);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
