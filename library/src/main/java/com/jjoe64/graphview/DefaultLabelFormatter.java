/**
 * GraphView
 * Copyright (C) 2014  Jonas Gehring
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * with the "Linking Exception", which can be found at the license.txt
 * file in this program.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with the "Linking Exception" along with this program; if not,
 * write to the author Jonas Gehring <g.jjoe64@gmail.com>.
 */
package com.jjoe64.graphview;

import java.text.NumberFormat;

/**
 * The label formatter that will be used
 * by default.
 * It will use the NumberFormat from Android
 * and sets the maximal fraction digits
 * depending on the range between min and max
 * value of the current viewport.
 *
 * It is recommended to use this label formatter
 * as base class to implement a custom formatter.
 *
 * @author jjoe64
 */
public class DefaultLabelFormatter implements LabelFormatter {
    /**
     * number formatter for x and y values
     */
    protected NumberFormat[] mNumberFormatter = new NumberFormat[2];

    /**
     * reference to the viewport of the
     * graph.
     * Will be used to calculate the current
     * range of values.
     */
    protected Viewport mViewport;

    /**
     * uses the default number format for the labels
     */
    public DefaultLabelFormatter() {
    }

    /**
     * use custom number format
     *
     * @param xFormat the number format for the x labels
     * @param yFormat the number format for the y labels
     */
    public DefaultLabelFormatter(NumberFormat xFormat, NumberFormat yFormat) {
        mNumberFormatter[0] = yFormat;
        mNumberFormatter[1] = xFormat;
    }

    /**
     * @param viewport the viewport of the graph
     */
    @Override
    public void setViewport(Viewport viewport) {
        mViewport = viewport;
    }

    /**
     * Formats the raw value to a nice
     * looking label, depending on the
     * current range of the viewport.
     *
     * @param value raw value
     * @param isValueX true if it's a x value, otherwise false
     * @return the formatted value as string
     */
    public String formatLabel(double value, boolean isValueX) {
        int i = isValueX ? 1 : 0;
        if (mNumberFormatter[i] == null) {
            mNumberFormatter[i] = NumberFormat.getNumberInstance();
            double highestvalue = isValueX ? mViewport.getMaxX(false) : mViewport.getMaxY(false);
            double lowestvalue = isValueX ? mViewport.getMinX(false) : mViewport.getMinY(false);
            if (highestvalue - lowestvalue < 0.1) {
                mNumberFormatter[i].setMaximumFractionDigits(6);
            } else if (highestvalue - lowestvalue < 1) {
                mNumberFormatter[i].setMaximumFractionDigits(4);
            } else if (highestvalue - lowestvalue < 20) {
                mNumberFormatter[i].setMaximumFractionDigits(3);
            } else if (highestvalue - lowestvalue < 100) {
                mNumberFormatter[i].setMaximumFractionDigits(1);
            } else {
                mNumberFormatter[i].setMaximumFractionDigits(0);
            }
        }
        return mNumberFormatter[i].format(value);
    }
}
