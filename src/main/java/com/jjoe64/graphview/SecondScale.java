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

import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.List;

/**
 * To be used to plot a second scale
 * on the graph.
 * The second scale has always to have
 * manual bounds.
 * Use {@link #setMinY(double)} and {@link #setMaxY(double)}
 * to set them.
 * The second scale has it's own array of series.
 *
 * @author jjoe64
 */
public class SecondScale {
    /**
     * reference to the viewport of the graph
     */
    protected final Viewport mViewport;

    /**
     * array of series for the second
     * scale
     */
    protected List<Series> mSeries;

    /**
     * flag whether the y axis bounds
     * are manual.
     * For the current version this is always
     * true.
     */
    private boolean mYAxisBoundsManual = true;

    /**
     * min y value for the y axis bounds
     */
    private double mMinY;

    /**
     * max y value for the y axis bounds
     */
    private double mMaxY;

    /**
     * label formatter for the y labels
     * on the right side
     */
    protected LabelFormatter mLabelFormatter;

    /**
     * creates the second scale.
     * normally you do not call this contructor.
     * Use {@link com.jjoe64.graphview.GraphView#getSecondScale()}
     * in order to get the instance.
     */
    SecondScale(Viewport viewport) {
        mViewport = viewport;
        mSeries = new ArrayList<Series>();
        mLabelFormatter = new DefaultLabelFormatter();
        mLabelFormatter.setViewport(mViewport);
    }

    /**
     * add a series to the second scale.
     * Don't add this series also to the GraphView
     * object.
     *
     * @param s the series
     */
    public void addSeries(Series s) {
        mSeries.add(s);
    }

    //public void setYAxisBoundsManual(boolean mYAxisBoundsManual) {
    //    this.mYAxisBoundsManual = mYAxisBoundsManual;
    //}

    /**
     * set the min y bounds
     *
     * @param d min y value
     */
    public void setMinY(double d) {
        mMinY = d;
    }

    /**
     * set the max y bounds
     *
     * @param d max y value
     */
    public void setMaxY(double d) {
        mMaxY = d;
    }

    /**
     * @return the series of the second scale
     */
    public List<Series> getSeries() {
        return mSeries;
    }

    /**
     * @return min y bound
     */
    public double getMinY() {
        return mMinY;
    }

    /**
     * @return max y bound
     */
    public double getMaxY() {
        return mMaxY;
    }

    /**
     * @return always true for the current implementation
     */
    public boolean isYAxisBoundsManual() {
        return mYAxisBoundsManual;
    }

    /**
     * @return label formatter for the y labels on the right side
     */
    public LabelFormatter getLabelFormatter() {
        return mLabelFormatter;
    }

    /**
     * Set a custom label formatter that is used
     * for the y labels on the right side.
     *
     * @param formatter label formatter for the y labels
     */
    public void setLabelFormatter(LabelFormatter formatter) {
        mLabelFormatter = formatter;
        mLabelFormatter.setViewport(mViewport);
    }
}
