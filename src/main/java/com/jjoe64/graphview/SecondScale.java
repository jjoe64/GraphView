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
 * Created by jonas on 11.11.14.
 */
public class SecondScale {
    private List<Series> mSeries;
    private boolean mYAxisBoundsManual = true;
    private double mMinY;
    private double mMaxY;

    public SecondScale() {
        mSeries = new ArrayList<Series>();
    }

    public void addSeries(Series s) {
        mSeries.add(s);
    }

    //public void setYAxisBoundsManual(boolean mYAxisBoundsManual) {
    //    this.mYAxisBoundsManual = mYAxisBoundsManual;
    //}

    public void setMinY(double d) {
        mMinY = d;
    }

    public void setMaxY(double d) {
        mMaxY = d;
    }

    public List<Series> getSeries() {
        return mSeries;
    }

    public double getMinY() {
        return mMinY;
    }

    public double getMaxY() {
        return mMaxY;
    }

    public boolean isYAxisBoundsManual() {
        return mYAxisBoundsManual;
    }
}
