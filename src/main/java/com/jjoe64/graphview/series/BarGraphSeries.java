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
package com.jjoe64.graphview.series;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Series with Bars to visualize the data.
 * The Bars are always vertical.
 *
 * @author jjoe64
 */
public class BarGraphSeries<E extends DataPointInterface> extends BaseSeries<E> {
    /**
     * paint to do drawing on canvas
     */
    private Paint mPaint;

    /**
     * spacing between the bars in percentage.
     * 0 => no spacing
     * 100 => the space bewetten the bars is as big as the bars itself
     */
    private int mSpacing;

    /**
     * callback to generate value-dependent colors
     * of the bars
     */
    private ValueDependentColor<E> mValueDependentColor;

    /**
     * flag whether the values should drawn
     * above the bars as text
     */
    private boolean mDrawValuesOnTop;

    /**
     * color of the text above the bars.
     *
     * @see #mDrawValuesOnTop
     */
    private int mValuesOnTopColor;

    /**
     * font size of the text above the bars.
     *
     * @see #mDrawValuesOnTop
     */
    private float mValuesOnTopSize;

    /**
     * stores the coordinates of the bars to
     * trigger tap on series events.
     */
    private Map<RectF, E> mDataPoints = new HashMap<RectF, E>();

    /**
     * creates bar series without any data
     */
    public BarGraphSeries() {
        mPaint = new Paint();
    }

    /**
     * creates bar series with data
     *
     * @param data values
     */
    public BarGraphSeries(E[] data) {
        super(data);
        mPaint = new Paint();
    }

    /**
     * draws the bars on the canvas
     *
     * @param graphView corresponding graphview
     * @param canvas canvas
     * @param isSecondScale whether we are plotting the second scale or not
     */
    @Override
    public void draw(GraphView graphView, Canvas canvas, boolean isSecondScale) {
        mPaint.setTextAlign(Paint.Align.CENTER);
        if (mValuesOnTopSize == 0) {
            mValuesOnTopSize = graphView.getGridLabelRenderer().getTextSize();
        }
        mPaint.setTextSize(mValuesOnTopSize);

        // get data
        double maxX = graphView.getViewport().getMaxX(false);
        double minX = graphView.getViewport().getMinX(false);

        double maxY;
        double minY;
        if (isSecondScale) {
            maxY = graphView.getSecondScale().getMaxY();
            minY = graphView.getSecondScale().getMinY();
        } else {
            maxY = graphView.getViewport().getMaxY(false);
            minY = graphView.getViewport().getMinY(false);
        }

        // Iterate through all bar graph series
        // so we know how wide to make our bar,
        // and in what position to put it in
        int numBarSeries = 0;
        int currentSeriesOrder = 0;
        int numValues = 0;
        boolean isCurrentSeries;
        SortedSet<Double> xVals = new TreeSet<Double>();
        for(Series inspectedSeries: graphView.getSeries()) {
            if(inspectedSeries instanceof BarGraphSeries) {
                isCurrentSeries = (inspectedSeries == this);
                if(isCurrentSeries) {
                    currentSeriesOrder = numBarSeries;
                }
                numBarSeries++;

                // calculate the number of slots for bars based on the minimum distance between
                // x coordinates in the series.  This is divided into the range to find
                // the placement and width of bar slots
                // (sections of the x axis for each bar or set of bars)
                // TODO: Move this somewhere more general and cache it, so we don't recalculate it for each series
                Iterator<E> curValues = inspectedSeries.getValues(minX, maxX);
                if (curValues.hasNext()) {
                    xVals.add(curValues.next().getX());
                    if(isCurrentSeries) { numValues++; }
                    while (curValues.hasNext()) {
                        xVals.add(curValues.next().getX());
                        if(isCurrentSeries) { numValues++; }
                    }
                }
            }
        }
        if (numValues == 0) {
            return;
        }

        Double lastVal = null;
        double minGap = 0;
        for(Double curVal: xVals) {
            if(lastVal != null) {
                double curGap = Math.abs(curVal - lastVal);
                if (minGap == 0 || (curGap > 0 && curGap < minGap)) {
                    minGap = curGap;
                }
            }
            lastVal = curVal;
        }

        int numBarSlots = (minGap == 0) ? 1 : (int)Math.round((maxX - minX)/minGap) + 1;

        Iterator<E> values = getValues(minX, maxX);

        // Calculate the overall bar slot width - this includes all bars across
        // all series, and any spacing between sets of bars
        float barSlotWidth = numBarSlots == 1
            ? graphView.getGraphContentWidth()
            : graphView.getGraphContentWidth() / (numBarSlots-1);
        Log.d("BarGraphSeries", "numBars=" + numBarSlots);

        // Total spacing (both sides) between sets of bars
        float spacing = Math.min((float) barSlotWidth*mSpacing/100, barSlotWidth*0.98f);
        // Width of an individual bar
        float barWidth = (barSlotWidth - spacing) / numBarSeries;
        // Offset from the center of a given bar to start drawing
        float offset = barSlotWidth/2;

        double diffY = maxY - minY;
        double diffX = maxX - minX;
        float contentHeight = graphView.getGraphContentHeight();
        float contentWidth = graphView.getGraphContentWidth();
        float contentLeft = graphView.getGraphContentLeft();
        float contentTop = graphView.getGraphContentTop();

        // draw data
        int i=0;
        while (values.hasNext()) {
            E value = values.next();

            double valY = value.getY() - minY;
            double ratY = valY / diffY;
            double y = contentHeight * ratY;

            double valY0 = 0 - minY;
            double ratY0 = valY0 / diffY;
            double y0 = contentHeight * ratY0;

            double valX = value.getX() - minX;
            double ratX = valX / diffX;
            double x = contentWidth * ratX;

            // hook for value dependent color
            if (getValueDependentColor() != null) {
                mPaint.setColor(getValueDependentColor().get(value));
            } else {
                mPaint.setColor(getColor());
            }

            float left = (float)x + contentLeft - offset + spacing/2 + currentSeriesOrder*barWidth;
            float top = (contentTop - (float)y) + contentHeight;
            float right = left + barWidth;
            float bottom = (contentTop - (float)y0) + contentHeight - (graphView.getGridLabelRenderer().isHighlightZeroLines()?4:1);

            boolean reverse = top > bottom;
            if (reverse) {
                float tmp = top;
                top = bottom + (graphView.getGridLabelRenderer().isHighlightZeroLines()?4:1);
                bottom = tmp;
            }

            // overdraw
            left = Math.max(left, contentLeft);
            right = Math.min(right, contentLeft+contentWidth);
            bottom = Math.min(bottom, contentTop+contentHeight);
            top = Math.max(top, contentTop);

            mDataPoints.put(new RectF(left, top, right, bottom), value);

            canvas.drawRect(left, top, right, bottom, mPaint);

            // set values on top of graph
            if (mDrawValuesOnTop) {
                if (reverse) {
                    top = bottom + mValuesOnTopSize + 4;
                    if (top > contentTop+contentHeight) top = contentTop + contentHeight;
                } else {
                    top -= 4;
                    if (top<=contentTop) top+=contentTop+4;
                }

                mPaint.setColor(mValuesOnTopColor);
                canvas.drawText(
                        graphView.getGridLabelRenderer().getLabelFormatter().formatLabel(value.getY(), false)
                        , (left+right)/2, top, mPaint);
            }

            i++;
        }
    }

    /**
     * @return the hook to generate value-dependent color. default null
     */
    public ValueDependentColor<E> getValueDependentColor() {
        return mValueDependentColor;
    }

    /**
     * set a hook to make the color of the bars depending
     * on the actually value/data.
     *
     * @param mValueDependentColor  hook
     *                              null to disable
     */
    public void setValueDependentColor(ValueDependentColor<E> mValueDependentColor) {
        this.mValueDependentColor = mValueDependentColor;
    }

    /**
     * @return the spacing between the bars in percentage
     */
    public int getSpacing() {
        return mSpacing;
    }

    /**
     * @param mSpacing  spacing between the bars in percentage.
     *                  0 => no spacing
     *                  100 => the space between the bars is as big as the bars itself
     */
    public void setSpacing(int mSpacing) {
        this.mSpacing = mSpacing;
    }

    /**
     * @return whether the values should be drawn above the bars
     */
    public boolean isDrawValuesOnTop() {
        return mDrawValuesOnTop;
    }

    /**
     * @param mDrawValuesOnTop  flag whether the values should drawn
     *                          above the bars as text
     */
    public void setDrawValuesOnTop(boolean mDrawValuesOnTop) {
        this.mDrawValuesOnTop = mDrawValuesOnTop;
    }

    /**
     * @return font color of the values on top of the bars
     * @see #setDrawValuesOnTop(boolean)
     */
    public int getValuesOnTopColor() {
        return mValuesOnTopColor;
    }

    /**
     * @param mValuesOnTopColor the font color of the values on top of the bars
     * @see #setDrawValuesOnTop(boolean)
     */
    public void setValuesOnTopColor(int mValuesOnTopColor) {
        this.mValuesOnTopColor = mValuesOnTopColor;
    }

    /**
     * @return font size of the values above the bars
     * @see #setDrawValuesOnTop(boolean)
     */
    public float getValuesOnTopSize() {
        return mValuesOnTopSize;
    }

    /**
     * @param mValuesOnTopSize font size of the values above the bars
     * @see #setDrawValuesOnTop(boolean)
     */
    public void setValuesOnTopSize(float mValuesOnTopSize) {
        this.mValuesOnTopSize = mValuesOnTopSize;
    }

    /**
     * resets the cached coordinates of the bars
     */
    @Override
    protected void resetDataPoints() {
        mDataPoints.clear();
    }

    /**
     * find the corresponding data point by
     * coordinates.
     *
     * @param x pixels
     * @param y pixels
     * @return datapoint or null
     */
    @Override
    protected E findDataPoint(float x, float y) {
        for (Map.Entry<RectF, E> entry : mDataPoints.entrySet()) {
            if (x >= entry.getKey().left && x <= entry.getKey().right
                && y >= entry.getKey().top && y <= entry.getKey().bottom) {
                return entry.getValue();
            }
        }
        return null;
    }
}
