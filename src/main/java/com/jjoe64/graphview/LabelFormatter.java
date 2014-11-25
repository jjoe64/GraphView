package com.jjoe64.graphview;

/**
 * Created by jonas on 15.08.14.
 */
public interface LabelFormatter {
    public String formatLabel(double value, boolean isValueX);
    public void setViewport(Viewport viewport);
}
