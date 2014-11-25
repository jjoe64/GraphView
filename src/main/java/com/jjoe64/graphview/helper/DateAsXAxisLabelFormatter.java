package com.jjoe64.graphview.helper;

import android.content.Context;

import com.jjoe64.graphview.DefaultLabelFormatter;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jonas on 25.11.14.
 */
public class DateAsXAxisLabelFormatter extends DefaultLabelFormatter {
    protected final DateFormat mDateFormat;
    protected final Calendar mCalendar;

    public DateAsXAxisLabelFormatter(Context context) {
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);
        mCalendar = Calendar.getInstance();
    }

    @Override
    public String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
            // format as date
            mCalendar.setTimeInMillis((long) value);
            return mDateFormat.format(mCalendar.getTimeInMillis());
        } else {
            return super.formatLabel(value, isValueX);
        }
    }
}
