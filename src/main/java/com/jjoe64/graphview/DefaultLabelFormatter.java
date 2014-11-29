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
package com.jjoe64.graphview;

import java.text.NumberFormat;

/**
 * Created by jonas on 15.08.14.
 */
public class DefaultLabelFormatter implements LabelFormatter {
    protected NumberFormat[] numberformatter = new NumberFormat[2];
    protected Viewport mViewport;

    public DefaultLabelFormatter() {
    }

    @Override
    public void setViewport(Viewport viewport) {
        mViewport = viewport;
    }

    public String formatLabel(double value, boolean isValueX) {
        int i = isValueX ? 1 : 0;
        if (numberformatter[i] == null) {
            numberformatter[i] = NumberFormat.getNumberInstance();
            double highestvalue = isValueX ? mViewport.getMaxX(false) : mViewport.getMaxY(false);
            double lowestvalue = isValueX ? mViewport.getMinX(false) : mViewport.getMinY(false);
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
