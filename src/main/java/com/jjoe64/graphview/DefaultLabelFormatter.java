package com.jjoe64.graphview;

import java.text.NumberFormat;

/**
 * Created by jonas on 15.08.14.
 */
public class DefaultLabelFormatter implements LabelFormatter {
    protected NumberFormat[] numberformatter = new NumberFormat[2];
    protected Viewport mViewport;

    public DefaultLabelFormatter(Viewport viewport) {
        mViewport = viewport;
    }

    public String formatLabel(double value, boolean isValueX) {
        int i = isValueX ? 1 : 0;
        if (numberformatter[i] == null) {
            numberformatter[i] = NumberFormat.getNumberInstance();
            double highestvalue = isValueX ? mViewport.getMaxX() : mViewport.getMaxY();
            double lowestvalue = isValueX ? mViewport.getMinX() : mViewport.getMinY();
            if (highestvalue - lowestvalue < 0.1) {
                numberformatter[i].setMaximumFractionDigits(6);
            } else if (highestvalue - lowestvalue < 1) {
                numberformatter[i].setMaximumFractionDigits(4);
            } else if (highestvalue - lowestvalue < 20) {
                numberformatter[i].setMaximumFractionDigits(3);
            } else if (highestvalue - lowestvalue < 100) {
                numberformatter[i].setMaximumFractionDigits(1);
            } else {
                numberformatter[i].setMaximumFractionDigits(0);
            }
        }
        return numberformatter[i].format(value);
    }
}
