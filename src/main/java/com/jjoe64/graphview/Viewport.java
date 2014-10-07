package com.jjoe64.graphview;

import com.jjoe64.graphview.series.Series;

import java.util.List;

/**
 * Created by jonas on 13.08.14.
 */
public class Viewport {
    public enum AxisBoundsStatus {
        INITIAL, AUTO_ADJUSTED, MANUAL
    }

    private final GraphView mGraphView;
    private double mMinX;
    private double mMaxX;
    private double mMinY;
    private double mMaxY;

    public AxisBoundsStatus getXAxisBoundsStatus() {
        return mXAxisBoundsStatus;
    }

    public AxisBoundsStatus getYAxisBoundsStatus() {
        return mYAxisBoundsStatus;
    }

    private AxisBoundsStatus mXAxisBoundsStatus;
    private AxisBoundsStatus mYAxisBoundsStatus;

    public Viewport(GraphView graphView) {
        mGraphView = graphView;
        mXAxisBoundsStatus = AxisBoundsStatus.INITIAL;
        mYAxisBoundsStatus = AxisBoundsStatus.INITIAL;
    }

    public void setXAxisBoundsStatus(AxisBoundsStatus s) {
        mXAxisBoundsStatus = s;
    }

    public void setYAxisBoundsStatus(AxisBoundsStatus s) {
        mYAxisBoundsStatus = s;
    }

    public double getMinX() {
        if (mXAxisBoundsStatus == AxisBoundsStatus.INITIAL) {
            List<Series> series = mGraphView.getSeries();
            if (series.isEmpty()) {
                return 0;
            }
            double d = series.get(0).getLowestValueX();
            for (Series s : series) {
                if (d > s.getLowestValueX()) {
                    d = s.getLowestValueX();
                }
            }
            return d;
        } else {
            return mMinX;
        }
    }

    public double getMaxX() {
        if (mXAxisBoundsStatus == AxisBoundsStatus.INITIAL) {
            List<Series> series = mGraphView.getSeries();
            if (series.isEmpty()) {
                return 0;
            }
            double d = series.get(0).getHighestValueX();
            for (Series s : series) {
                if (d < s.getHighestValueX()) {
                    d = s.getHighestValueX();
                }
            }
            return d;
        } else {
            return mMaxX;
        }
    }

    public double getMinY() {
        if (mYAxisBoundsStatus == AxisBoundsStatus.INITIAL) {
            List<Series> series = mGraphView.getSeries();
            if (series.isEmpty()) {
                return 0;
            }
            double d = series.get(0).getLowestValueY();
            for (Series s : series) {
                if (d > s.getLowestValueY()) {
                    d = s.getLowestValueY();
                }
            }
            return d;
        } else {
            return mMinY;
        }
    }

    public double getMaxY() {
        if (mYAxisBoundsStatus == AxisBoundsStatus.INITIAL) {
            List<Series> series = mGraphView.getSeries();
            if (series.isEmpty()) {
                return 0;
            }
            double d = series.get(0).getHighestValueY();
            for (Series s : series) {
                if (d < s.getHighestValueY()) {
                    d = s.getHighestValueY();
                }
            }
            return d;
        } else {
            return mMaxY;
        }
    }

    public void setMaxY(double y) {
        mMaxY = y;
    }

    public void setMinY(double y) {
        mMinY = y;
    }

    public void setMaxX(double x) {
        mMaxX = x;
    }

    public void setMinX(double x) {
        mMinX = x;
    }
}
