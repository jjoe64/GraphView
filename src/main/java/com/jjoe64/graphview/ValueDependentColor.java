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

import com.jjoe64.graphview.series.DataPointInterface;

/**
 * you can change the color depending on the value.
 * takes only effect for BarGraphSeries.
 *
 * @see com.jjoe64.graphview.series.BarGraphSeries#setValueDependentColor(ValueDependentColor)
 */
public interface ValueDependentColor<T extends DataPointInterface> {
    /**
     * this is called when a bar is about to draw
     * and the color is be loaded.
     *
     * @param data the current input value
     * @return  the color that the bar should be drawn with
     *          Generate the int via the android.graphics.Color class.
     */
    public int get(T data);
}
