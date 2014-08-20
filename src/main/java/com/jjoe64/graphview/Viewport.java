package com.jjoe64.graphview;

/**
 * Created by jonas on 13.08.14.
 */
public class Viewport {
    private final GraphView mGraphView;
    private double mMinX;
    private double mMaxX;
    private double mMinY;
    private double mMaxY;

    public Viewport(GraphView graphView) {
        mGraphView = graphView;
    }

    public double getMinX() {
        return mMinX;
    }

    public double getMaxX() {
        return mMaxX;
    }

    public double getMinY() {
        return mMinY;
    }

    public double getMaxY() {
        return mMaxY;
    }

    public void setMaxY(double y) {
        mMaxY = y;
    }

    public void setMinY(double y) {
        mMinY = y;
    }
}
