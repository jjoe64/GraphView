package com.jjoe64.graphview.series;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by jonas on 28.08.14.
 */
public abstract class BaseSeries<E extends DataPointInterface> implements Series<E> {
    private List<E> mData = new ArrayList<E>();

    private String mTitle;
    private int mColor = 0xff0077cc;

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
}
