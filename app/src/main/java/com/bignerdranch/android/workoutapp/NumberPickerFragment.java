package com.bignerdranch.android.workoutapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;


public class NumberPickerFragment extends DialogFragment {

    // Name of for the Extra that we're using to pass back the number we select from the NumberPicker back to whatever launched the dialog
    public static final String EXTRA_NUMBER = "com.bignerdranch.android.workoutapp.number";

    private static final String ARG_NUMBER = "number";
    private static final String ARG_MIN_VALUE = "min_value";
    private static final String ARG_MAX_VALUE = "max_value";
    private static final String ARG_UNITS = "units";
    private static final String ARG_TITLE = "title";

    private NumberPicker mNumberPicker;


    // For passing the clicked on number to the NumberPickerFragment
    public static NumberPickerFragment newInstance(int number, int minValue, int maxValue, @NonNull String units, @NonNull String title) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_NUMBER, number);
        args.putSerializable(ARG_MIN_VALUE, minValue);
        args.putSerializable(ARG_MAX_VALUE, maxValue);
        args.putSerializable(ARG_UNITS, units);
        args.putSerializable(ARG_TITLE, title);

        NumberPickerFragment fragment = new NumberPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog (Bundle savedInstanceState) {
        // Get the clicked on number argument that was passed in
        int number = (int) getArguments().getSerializable(ARG_NUMBER);
        int min_value = (int) getArguments().getSerializable(ARG_MIN_VALUE);
        int max_value = (int) getArguments().getSerializable(ARG_MAX_VALUE);
        String units = (String) getArguments().getSerializable(ARG_UNITS);
        String title = (String) getArguments().getSerializable(ARG_TITLE);

        // Inflate the NumberPicker View object (that we later pass into setView in order to add the NumberPicker widget to the AlertDialog)
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_numberpicker, null);

        mNumberPicker = (NumberPicker) v.findViewById(R.id.dialog_number_picker);

        mNumberPicker.setMinValue(min_value);
        mNumberPicker.setMaxValue(max_value);
        mNumberPicker.setFormatter((int value) -> {
            return value + units;
        });

        mNumberPicker.setValue(number);

        return new AlertDialog.Builder (getActivity())
            .setView(v)
            .setTitle(title)
            .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) -> {
                int result = mNumberPicker.getValue();
                sendResult (Activity.RESULT_OK, result);
            })
            .create();
    }

    // Used to send the result of the number we pick in the NumberPicker back to the Fragment that started it
    private void sendResult (int resultCode, int result) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_NUMBER, result);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
