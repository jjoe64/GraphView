/**
 * GraphView
 * Copyright 2016 Jonas Gehring
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jjoe64.graphview.helper;

import android.content.Context;

import com.jjoe64.graphview.DefaultLabelFormatter;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Helper class to use date objects as x-values.
 * This will use your own Date Format or by default
 * the Android default date format to convert
 * the x-values (that has to be millis from
 * 01-01-1970) into a formatted date string.
 *
 * See the DateAsXAxis example in the GraphView-Demos project
 * to see a working example.
 *
 * @author jjoe64
 */
public class LogarithmicAsYAxisLabelFormatter extends DefaultLabelFormatter {

    /**
     * create the formatter with the logarithmic scale for
     * the y-values
     *
     * @param context the application context
     */
    public LogarithmicAsYAxisLabelFormatter(Context context) {
    }

    /**
     * formats the y-values to logarithm values.
     *
     * @param value raw value
     * @param isValueX true if it's a x value, otherwise false
     * @return value converted to string
     */
    @Override
    public String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
            return super.formatLabel(value, isValueX);
        } else {
            String yValue=super.formatLabel(value, isValueX).replaceAll(",",".");
            Float yLogValue = (float)Math.floor((Math.pow(10, Float.valueOf(yValue))));
            return (value<=0)? "-∞":String.valueOf(yLogValue);
        }
    }
}
