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
 * Created by jonas on 28.08.14.
 */
public abstract class BaseSeries<E extends DataPointInterface> implements Series<E> {
    final private List<E> mData = new ArrayList<E>();
    private Map<PointF, E> mDataPoints = new HashMap<PointF, E>();

    private String mTitle;
    private int mColor = 0xff0077cc;
    protected OnDataPointTapListener mOnDataPointTapListener;
    private List<GraphView> mGraphViews;

    public BaseSeries() {
        mGraphViews = new ArrayList<GraphView>();
    }

    public BaseSeries(E[] data) {
        mGraphViews = new ArrayList<GraphView>();
        for (E d : data) {
            mData.add(d);
        }
    }

    public double getLowestValueX() {
        if (mData.isEmpty()) return 0d;
        return mData.get(0).getX();
    }

    public double getHighestValueX() {
        if (mData.isEmpty()) return 0d;
        return mData.get(mData.size()-1).getX();
    }

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
     * get the values for a given x span. if from and until are bigger or equal than
     * all the data, the original data is returned.
     * If it is only a part of the data, the span is returned plus one datapoint
     * before and after to get a nice scrolling.
     *
     * @param from
     * @param until
     * @return
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

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
    }

    public void setOnDataPointTapListener(OnDataPointTapListener l) {
        this.mOnDataPointTapListener = l;
    }

    @Override
    public void onTap(float x, float y) {
        if (mOnDataPointTapListener != null) {
            E p = findDataPoint(x, y);
            if (p != null) {
                mOnDataPointTapListener.onTap(this, p);
            }
        }
    }

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
            } else {
                Log.d("BaseSeries", "point too far: " + shortestDistance);
            }
        }
        return null;
    }

    /**
     * register the datapoint to find it at a tap
     * @param x
     * @param y
     * @param dp
     */
    protected void registerDataPoint(float x, float y, E dp) {
        mDataPoints.put(new PointF(x, y), dp);
    }

    protected void resetDataPoints() {
        mDataPoints.clear();
    }

    public void resetData(E[] data) {
        mData.clear();
        for (E d : data) {
            mData.add(d);
        }

        // update graphview
        for (GraphView gv : mGraphViews) {
            gv.onDataChanged(true, false);
        }
    }

    @Override
    public void onGraphViewAttached(GraphView graphView) {
        mGraphViews.add(graphView);
    }

    public void appendData(E dataPoint, boolean scrollToEnd, int maxDataPoints) {
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

    @Override
    public boolean isEmpty() {
        return mData.isEmpty();
    }
}
