package com.jjoe64.graphview.series;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jonas on 28.08.14.
 */
public abstract class BaseSeries<E extends DataPointInterface> implements Series<E> {
    private List<E> mData = new ArrayList<E>();

    public BaseSeries(E[] data) {
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

    @Override
    public Iterator<E> getValues(double from, double until) {
        if (from <= getLowestValueX() && until >= getHighestValueX()) {
            return mData.iterator();
        } else {
            // TODO
            throw new IllegalStateException();
        }
    }
}
