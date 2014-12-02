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

/**
 * Interface to use as label formatter.
 * Implement this in order to generate
 * your own labels format.
 * It is recommended to override {@link com.jjoe64.graphview.DefaultLabelFormatter}.
 *
 * @author jjoe64
 */
public interface LabelFormatter {
    /**
     * converts a raw number as input to
     * a formatted string for the label.
     *
     * @param value raw input number
     * @param isValueX  true if it is a value for the x axis
     *                  false if it is a value for the y axis
     * @return the formatted number as string
     */
    public String formatLabel(double value, boolean isValueX);

    /**
     * will be called in order to have a
     * reference to the current viewport.
     * This is useful if you need the bounds
     * to generate your labels.
     * You store this viewport in as member variable
     * and access it e.g. in the {@link #formatLabel(double, boolean)}
     * method.
     *
     * @param viewport the used viewport
     */
    public void setViewport(Viewport viewport);
}
