package com.bignerdranch.android.workoutapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Armon on 21/03/2018.
 */

public class DatePickerFragment extends DialogFragment {

    // Name of for the Extra that we're using to pass back the date we select from the DatePicker back to RoutineDayPageFragment
    public static final String EXTRA_DATE = "com.bignerdranch.android.workoutapp.date";

    private static final String ARG_DATE = "date";

    private DatePicker mDatePicker;

    // For passing the clicked on date to the DatePickerFragment
    public static DatePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState) {
        // Get the clicked on date argument that was passed in
        Date date = (Date) getArguments().getSerializable(ARG_DATE);

        // Create a Calendar from the Date object since Calendar's are more flexible
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Inflate the DatePicker View object (that we later pass into setView in order to add the DatePicker widget to the AlertDialog)
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date, null);

        mDatePicker = (DatePicker) v.findViewById(R.id.dialog_date_picker);
        mDatePicker.init(year, month, day, null);

        return new AlertDialog.Builder (getActivity())
            .setView(v)
            .setTitle(R.string.date_picker_title)
            .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                            int year = mDatePicker.getYear();
                            int month = mDatePicker.getMonth();
                            int day = mDatePicker.getDayOfMonth();
                            Date date = new GregorianCalendar(year, month, day).getTime();
                            sendResult (Activity.RESULT_OK, date);
                        }
                    })
            .create();
    }

    // Used to send the result of the date we pick in the DatePicker back to RoutineDayPageFragment
    private void sendResult (int resultCode, Date date) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
