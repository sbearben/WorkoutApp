package com.bignerdranch.android.workoutapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.bignerdranch.android.workoutapp.model.Routine;

/**
 * Created by Armon on 21/03/2018.
 */

public class NewRoutineFragment extends DialogFragment {

    public static final String EXTRA_ROUTINE_NAME = "com.bignerdranch.android.workoutapp.routine_name";
    public static final String EXTRA_ROUTINE_DAYS = "com.bignerdranch.android.workoutapp.routine_days";

    private TextInputLayout mNameInputLayout;
    private TextInputEditText mNameInputEditText;
    private TextInputLayout mDaysInputLayout;
    private TextInputEditText mDaysInputEditText;


    public static NewRoutineFragment newInstance() {
        return new NewRoutineFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_routine, null);

        mNameInputLayout = (TextInputLayout) v.findViewById(R.id.dialog_new_routine_name_layout);
        mNameInputEditText = (TextInputEditText) v.findViewById(R.id.dialog_new_routine_name_edittext);
        mDaysInputLayout = (TextInputLayout) v.findViewById(R.id.dialog_new_routine_days_layout);
        mDaysInputEditText = (TextInputEditText) v.findViewById(R.id.dialog_new_routine_days_edittext);

        // Add TextChangedListener for the routine name edit text
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
                    mNameInputLayout.setError(getString(R.string.new_routine_name_empty_error));
                else
                    mNameInputLayout.setError(null);
            }
        });

        // Add TextChangedListener for the routine days edit text
        mDaysInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals(""))
                    mDaysInputLayout.setError(getString(R.string.new_routine_days_empty_error, Routine.MAX_ROUTINE_DAYS));
                else if (Integer.parseInt(s.toString()) < 1 || Integer.parseInt(s.toString()) > Routine.MAX_ROUTINE_DAYS)
                    mDaysInputLayout.setError(getString(R.string.new_routine_days_empty_error, Routine.MAX_ROUTINE_DAYS));
                else
                    mDaysInputLayout.setError(null);
            }
        });

        final AlertDialog newRoutineAlertDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.new_routine_title)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        /* We need to set the onShowListener, and then within it set the positive button onClickListener in order to make sure that the dialog
           doesn't dismiss when the user clicks "ok" when the input isn't valid
           - found here: https://stackoverflow.com/a/26087003/7648952 */
        newRoutineAlertDialog.setOnShowListener((DialogInterface dialog) -> {
            Button b = newRoutineAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

            b.setOnClickListener((View view) -> {
                String name = mNameInputEditText.getText().toString();
                String daysStr = mDaysInputEditText.getText().toString();
                int daysNumber;
                boolean invalidInput = false;

                if (name.equals("")) {
                    mNameInputLayout.setError(getString(R.string.new_routine_name_empty_error));
                    invalidInput = true;
                }
                if (daysStr.equals("")) {
                    mDaysInputLayout.setError(getString(R.string.new_routine_days_empty_error, Routine.MAX_ROUTINE_DAYS));
                    return; // Don't need to set the invalidInput flag here because if we reached this point we've already set all the error messages and can return
                }
                daysNumber = Integer.parseInt(daysStr);

                if (!invalidInput && (daysNumber > 0 && daysNumber <= Routine.MAX_ROUTINE_DAYS)) {
                    sendResult(Activity.RESULT_OK, name, Integer.parseInt(daysStr));
                    newRoutineAlertDialog.dismiss();
                }
            });
        });

        return newRoutineAlertDialog;
    }

    private void sendResult (int resultCode, String routineName, int numberDays) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_ROUTINE_NAME, routineName);
        intent.putExtra(EXTRA_ROUTINE_DAYS, numberDays);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
