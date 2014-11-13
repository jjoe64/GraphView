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
