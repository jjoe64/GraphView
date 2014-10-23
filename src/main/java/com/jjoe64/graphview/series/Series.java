package com.jjoe64.graphview.series;

import android.graphics.Canvas;

import com.jjoe64.graphview.GraphView;

import java.util.Iterator;

/**
 * Created by jonas on 28.08.14.
 */
public interface Series<E extends DataPointInterface> {
    public double getLowestValueX();
    public double getHighestValueX();
    public double getLowestValueY();
    public double getHighestValueY();
    public Iterator<E> getValues(double from, double until);
    public void draw(GraphView graphView, Canvas canvas);
    public String getTitle();
    public int getColor();
}
