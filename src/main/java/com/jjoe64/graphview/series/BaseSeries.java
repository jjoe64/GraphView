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
package com.jjoe64.graphview.series;

import android.graphics.PointF;
import android.util.Log;

import com.jjoe64.graphview.GraphView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Basis implementation for series.
 * Used for series that are plotted on
 * a default x/y 2d viewport.
 *
 * Extend this class to implement your own custom
 * graph type.
 *
 * This implementation uses a internal Array to store
 * the data. If you want to implement a custom data provider
 * you may want to implement {@link com.jjoe64.graphview.series.Series}.
 *
 * @author jjoe64
 */
public abstract class BaseSeries<E extends DataPointInterface> implements Series<E> {
    /**
     * holds the data
     */
    final private List<E> mData = new ArrayList<E>();

    /**
     * stores the used coordinates to find the
     * corresponding data point on a tap
     *
     * Key => x/y pixel
     * Value => Plotted Datapoint
     *
     * will be filled while drawing via {@link #registerDataPoint(float, float, DataPointInterface)}
     */
    private Map<PointF, E> mDataPoints = new HashMap<PointF, E>();

    /**
     * title for this series that can be displayed
     * in the legend.
     */
    private String mTitle;

    /**
     * base color for this series. will be used also in
     * the legend
     */
    private int mColor = 0xff0077cc;

    /**
     * listener to handle tap events on a data point
     */
    protected OnDataPointTapListener mOnDataPointTapListener;

    /**
     * stores the graphviews where this series is used.
     * Can be more than one.
     */
    private List<GraphView> mGraphViews;

    /**
     * creates series without data
     */
    public BaseSeries() {
        mGraphViews = new ArrayList<GraphView>();
    }

    /**
     * creates series with data
     *
     * @param data  data points
     *              important: array has to be sorted from lowest x-value to the highest
     */
    public BaseSeries(E[] data) {
        mGraphViews = new ArrayList<GraphView>();
        for (E d : data) {
            mData.add(d);
        }
    }

    /**
     * @return the lowest x value, or 0 if there is no data
     */
    public double getLowestValueX() {
        if (mData.isEmpty()) return 0d;
        return mData.get(0).getX();
    }

    /**
     * @return the highest x value, or 0 if there is no data
     */
    public double getHighestValueX() {
        if (mData.isEmpty()) return 0d;
        return mData.get(mData.size()-1).getX();
    }

    /**
     * @return the lowest y value, or 0 if there is no data
     */
    public double getLowestValueY() {
        if (mData.isEmpty()) return 0d;
        double l = mData.get(0).getY();
        for (int i = 1; i < mData.size(); i++) {
            double c = mData.get(i).getY();
            if (l > c) {
                l = c;
            }
        }
        return l;
    }

    /**
     * @return the highest y value, or 0 if there is no data
     */
    public double getHighestValueY() {
        if (mData.isEmpty()) return 0d;
        double h = mData.get(0).getY();
        for (int i = 1; i < mData.size(); i++) {
            double c = mData.get(i).getY();
            if (h < c) {
                h = c;
            }
        }
        return h;
    }

    /**
     * get the values for a given x range. if from and until are bigger or equal than
     * all the data, the original data is returned.
     * If it is only a part of the data, the range is returned plus one datapoint
     * before and after to get a nice scrolling.
     *
     * @param from minimal x-value
     * @param until maximal x-value
     * @return data for the range +/- 1 datapoint
     */
    @Override
    public Iterator<E> getValues(final double from, final double until) {
        if (from <= getLowestValueX() && until >= getHighestValueX()) {
            return mData.iterator();
        } else {
            return new Iterator<E>() {
                Iterator<E> org = mData.iterator();
                E nextValue = null;
                E nextNextValue = null;
                boolean plusOne = true;

                {
                    // go to first
                    boolean found = false;
                    E prevValue = null;
                    if (org.hasNext()) {
                        prevValue = org.next();
                    }
                    if (prevValue.getX() >= from) {
                        nextValue = prevValue;
                        found = true;
                    } else {
                        while (org.hasNext()) {
                            nextValue = org.next();
                            if (nextValue.getX() >= from) {
                                found = true;
                                nextNextValue = nextValue;
                                nextValue = prevValue;
                                break;
                            }
                            prevValue = nextValue;
                        }
                    }
                    if (!found) {
                        nextValue = null;
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public E next() {
                    if (hasNext()) {
                        E r = nextValue;
                        if (r.getX() > until) {
                            plusOne = false;
                        }
                        if (nextNextValue != null) {
                            nextValue = nextNextValue;
                            nextNextValue = null;
                        } else if (org.hasNext()) nextValue = org.next();
                        else nextValue = null;
                        return r;
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public boolean hasNext() {
                    return nextValue != null && (nextValue.getX() <= until || plusOne);
                }
            };
        }
    }

    /**
     * @return the title of the series
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * set the title of the series. This will be used in
     * the legend.
     *
     * @param mTitle title of the series
     */
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    /**
     * @return color of the series
     */
    public int getColor() {
        return mColor;
    }

    /**
     * set the color of the series. This will be used in
     * plotting (depends on the series implementation) and
     * is used in the legend.
     *
     * @param mColor
     */
    public void setColor(int mColor) {
        this.mColor = mColor;
    }

    /**
     * set a listener for tap on a data point.
     *
     * @param l listener
     */
    public void setOnDataPointTapListener(OnDataPointTapListener l) {
        this.mOnDataPointTapListener = l;
    }

    /**
     * called by the tap detector in order to trigger
     * the on tap on datapoint event.
     *
     * @param x pixel
     * @param y pixel
     */
    @Override
    public void onTap(float x, float y) {
        if (mOnDataPointTapListener != null) {
            E p = findDataPoint(x, y);
            if (p != null) {
                mOnDataPointTapListener.onTap(this, p);
            }
        }
    }

    /**
     * find the data point which is next to the
     * coordinates
     *
     * @param x pixel
     * @param y pixel
     * @return the data point or null if nothing was found
     */
    protected E findDataPoint(float x, float y) {
        float shortestDistance = Float.NaN;
        E shortest = null;
        for (Map.Entry<PointF, E> entry : mDataPoints.entrySet()) {
            float x1 = entry.getKey().x;
            float y1 = entry.getKey().y;
            float x2 = x;
            float y2 = y;

            float distance = (float) Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
            if (shortest == null || distance < shortestDistance) {
                shortestDistance = distance;
                shortest = entry.getValue();
            }
        }
        if (shortest != null) {
            if (shortestDistance < 120) {
                return shortest;
            }
        }
        return null;
    }

    /**
     * register the datapoint to find it at a tap
     *
     * @param x pixel
     * @param y pixel
     * @param dp the data point to save
     */
    protected void registerDataPoint(float x, float y, E dp) {
        mDataPoints.put(new PointF(x, y), dp);
    }

    /**
     * clears the cached data point coordinates
     */
    protected void resetDataPoints() {
        mDataPoints.clear();
    }

    /**
     * clears the data of this series and sets new.
     * will redraw the graph
     *
     * @param data the values must be in the correct order!
     *             x-value has to be ASC. First the lowest x value and at least the highest x value.
     */
    public void resetData(E[] data) {
        mData.clear();
        for (E d : data) {
            mData.add(d);
        }
        checkValueOrder(null);

        // update graphview
        for (GraphView gv : mGraphViews) {
            gv.onDataChanged(true, false);
        }
    }

    /**
     * called when the series was added to a graph
     *
     * @param graphView graphview
     */
    @Override
    public void onGraphViewAttached(GraphView graphView) {
        mGraphViews.add(graphView);
    }

    /**
     *
     * @param dataPoint values the values must be in the correct order!
     *                  x-value has to be ASC. First the lowest x value and at least the highest x value.
     * @param scrollToEnd true => graphview will scroll to the end (maxX)
     * @param maxDataPoints if max data count is reached, the oldest data
     *                      value will be lost to avoid memory leaks
     */
    public void appendData(E dataPoint, boolean scrollToEnd, int maxDataPoints) {
        checkValueOrder(dataPoint);

        if (!mData.isEmpty() && dataPoint.getX() < mData.get(mData.size()-1).getX()) {
            throw new IllegalArgumentException("new x-value must be greater then the last value. x-values has to be ordered in ASC.");
        }
        synchronized (mData) {
            int curDataCount = mData.size();
            if (curDataCount < maxDataPoints) {
                // enough space
                mData.add(dataPoint);
            } else {
                // we have to trim one data
                mData.remove(0);
                mData.add(dataPoint);
            }
        }

        // recalc the labels when it was the first data
        boolean keepLabels = mData.size() != 1;

        // update linked graph views
        // update graphview
        for (GraphView gv : mGraphViews) {
            gv.onDataChanged(keepLabels, scrollToEnd);
            if (scrollToEnd) {
                gv.getViewport().scrollToEnd();
            }
        }
    }

    /**
     * @return whether there are data points
     */
    @Override
    public boolean isEmpty() {
        return mData.isEmpty();
    }

    /**
     * checks that the data is in the correct order
     *
     * @param onlyLast  if not null, it will only check that this
     *                  datapoint is after the last point.
     */
    protected void checkValueOrder(DataPointInterface onlyLast) {
        if (mData.size()>1) {
            if (onlyLast != null) {
                // only check last
                if (onlyLast.getX() < mData.get(mData.size()-1).getX()) {
                    throw new IllegalArgumentException("new x-value must be greater then the last value. x-values has to be ordered in ASC.");
                }
            } else {
                double lx = mData.get(0).getX();

                for (int i = 1; i < mData.size(); i++) {
                    if (mData.get(i).getX() != Double.NaN) {
                        if (lx > mData.get(i).getX()) {
                            throw new IllegalArgumentException("The order of the values is not correct. X-Values have to be ordered ASC. First the lowest x value and at least the highest x value.");
                        }
                        lx = mData.get(i).getX();
                    }
                }
            }
        }
    }
}
