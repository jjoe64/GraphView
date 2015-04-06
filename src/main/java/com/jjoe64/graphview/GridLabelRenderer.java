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

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.util.TypedValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The default renderer for the grid
 * and the labels.
 *
 * @author jjoe64
 */
public class GridLabelRenderer {
    /**
     * wrapper for the styles regarding
     * to the grid and the labels
     */
    public final class Styles {
        /**
         * the general text size of the axis titles.
         * can be overwritten with #verticalAxisTitleTextSize
         * and #horizontalAxisTitleTextSize
         */
        public float textSize;

        /**
         * the alignment of the vertical labels
         */
        public Paint.Align verticalLabelsAlign;

        /**
         * the alignment of the labels on the right side
         */
        public Paint.Align verticalLabelsSecondScaleAlign;

        /**
         * the color of the vertical labels
         */
        public int verticalLabelsColor;

        /**
         * the color of the labels on the right side
         */
        public int verticalLabelsSecondScaleColor;

        /**
         * the color of the horizontal labels
         */
        public int horizontalLabelsColor;

        /**
         * the color of the grid lines
         */
        public int gridColor;

        /**
         * flag whether the zero-lines (vertical+
         * horizontal) shall be highlighted
         */
        public boolean highlightZeroLines;

        /**
         * the padding around the graph and labels
         */
        public int padding;

        /**
         * font size of the vertical axis title
         */
        public float verticalAxisTitleTextSize;

        /**
         * font color of the vertical axis title
         */
        public int verticalAxisTitleColor;

        /**
         * font size of the horizontal axis title
         */
        public float horizontalAxisTitleTextSize;

        /**
         * font color of the horizontal axis title
         */
        public int horizontalAxisTitleColor;

        /**
         * flag whether the horizontal labels are
         * visible
         */
        boolean horizontalLabelsVisible;

        /**
         * flag whether the vertical labels are
         * visible
         */
        boolean verticalLabelsVisible;

        /**
         * defines which lines will be drawn in the background
         */
        GridStyle gridStyle;

        /**
         * the space between the labels text and the graph content
         */
        int labelsSpace;
    }

    /**
     * Definition which lines will be drawn in the background
     */
    public enum GridStyle {
        BOTH, VERTICAL, HORIZONTAL, NONE;

        public boolean drawVertical() { return this == BOTH || this == VERTICAL && this != NONE; }
        public boolean drawHorizontal() { return this == BOTH || this == HORIZONTAL && this != NONE; }
    }

    /**
     * wraps the styles regarding the
     * grid and labels
     */
    protected Styles mStyles;

    /**
     * reference to graphview
     */
    private final GraphView mGraphView;

    /**
     * cache of the vertical steps
     * (horizontal lines and vertical labels)
     * Key      = Pixel (y)
     * Value    = y-value
     */
    private Map<Integer, Double> mStepsVertical;

    /**
     * cache of the vertical steps for the
     * second scale, which is on the right side
     * (horizontal lines and vertical labels)
     * Key      = Pixel (y)
     * Value    = y-value
     */
    private Map<Integer, Double> mStepsVerticalSecondScale;

    /**
     * cache of the horizontal steps
     * (vertical lines and horizontal labels)
     * Key      = Pixel (x)
     * Value    = x-value
     */
    private Map<Integer, Double> mStepsHorizontal;

    /**
     * the paint to draw the grid lines
     */
    private Paint mPaintLine;

    /**
     * the paint to draw the labels
     */
    private Paint mPaintLabel;

    /**
     * the paint to draw axis titles
     */
    private Paint mPaintAxisTitle;

    /**
     * flag whether is bounds are automatically
     * adjusted for nice human-readable numbers
     */
    private boolean mIsAdjusted;

    /**
     * the width of the vertical labels
     */
    private Integer mLabelVerticalWidth;

    /**
     * indicates if the width was set manually
     */
    private boolean mLabelVerticalWidthFixed;

    /**
     * the height of the vertical labels
     */
    private Integer mLabelVerticalHeight;

    /**
     * indicates if the height was set manually
     */
    private boolean mLabelHorizontalHeightFixed;

    /**
     * the width of the vertical labels
     * of the second scale
     */
    private Integer mLabelVerticalSecondScaleWidth;

    /**
     * the height of the vertical labels
     * of the second scale
     */
    private Integer mLabelVerticalSecondScaleHeight;

    /**
     * the width of the horizontal labels
     */
    private Integer mLabelHorizontalWidth;

    /**
     * the height of the horizontal labels
     */
    private Integer mLabelHorizontalHeight;

    /**
     * the label formatter, that converts
     * the raw numbers to strings
     */
    private LabelFormatter mLabelFormatter;

    /**
     * the title of the horizontal axis
     */
    private String mHorizontalAxisTitle;

    /**
     * the title of the vertical axis
     */
    private String mVerticalAxisTitle;

    /**
     * count of the vertical labels, that
     * will be shown at one time.
     */
    private int mNumVerticalLabels;

    /**
     * count of the horizontal labels, that
     * will be shown at one time.
     */
    private int mNumHorizontalLabels;

    /**
     * create the default grid label renderer.
     *
     * @param graphView the corresponding graphview object
     */
    public GridLabelRenderer(GraphView graphView) {
        mGraphView = graphView;
        setLabelFormatter(new DefaultLabelFormatter());
        mStyles = new Styles();
        resetStyles();
        mNumVerticalLabels = 5;
        mNumHorizontalLabels = 5;
    }

    /**
     * resets the styles. This loads the style
     * from reading the values of the current
     * theme.
     */
    public void resetStyles() {
        // get matching styles from theme
        TypedValue typedValue = new TypedValue();
        mGraphView.getContext().getTheme().resolveAttribute(android.R.attr.textAppearanceSmall, typedValue, true);

        int color1;
        int color2;
        int size;
        int size2;

        TypedArray array = null;
        try {
            array = mGraphView.getContext().obtainStyledAttributes(typedValue.data, new int[]{
                    android.R.attr.textColorPrimary
                    , android.R.attr.textColorSecondary
                    , android.R.attr.textSize
                    , android.R.attr.horizontalGap});
            color1 = array.getColor(0, Color.BLACK);
            color2 = array.getColor(1, Color.GRAY);
            size = array.getDimensionPixelSize(2, 20);
            size2 = array.getDimensionPixelSize(3, 20);
            array.recycle();
        } catch (Exception e) {
            color1 = Color.BLACK;
            color2 = Color.GRAY;
            size = 20;
            size2 = 20;
        }

        mStyles.verticalLabelsColor = color1;
        mStyles.verticalLabelsSecondScaleColor = color1;
        mStyles.horizontalLabelsColor = color1;
        mStyles.gridColor = color2;
        mStyles.textSize = size;
        mStyles.padding = size2;
        mStyles.labelsSpace = (int) mStyles.textSize/5;

        mStyles.verticalLabelsAlign = Paint.Align.RIGHT;
        mStyles.verticalLabelsSecondScaleAlign = Paint.Align.LEFT;
        mStyles.highlightZeroLines = true;

        mStyles.verticalAxisTitleColor = mStyles.verticalLabelsColor;
        mStyles.horizontalAxisTitleColor = mStyles.horizontalLabelsColor;
        mStyles.verticalAxisTitleTextSize = mStyles.textSize;
        mStyles.horizontalAxisTitleTextSize = mStyles.textSize;

        mStyles.horizontalLabelsVisible = true;
        mStyles.verticalLabelsVisible = true;

        mStyles.gridStyle = GridStyle.BOTH;

        reloadStyles();
    }

    /**
     * will load the styles to the internal
     * paint objects (color, text size, text align)
     */
    public void reloadStyles() {
        mPaintLine = new Paint();
        mPaintLine.setColor(mStyles.gridColor);
        mPaintLine.setStrokeWidth(0);

        mPaintLabel = new Paint();
        mPaintLabel.setTextSize(getTextSize());

        mPaintAxisTitle = new Paint();
        mPaintAxisTitle.setTextSize(getTextSize());
        mPaintAxisTitle.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * @return the general text size for the axis titles
     */
    public float getTextSize() {
        return mStyles.textSize;
    }

    /**
     * @return the font color of the vertical labels
     */
    public int getVerticalLabelsColor() {
        return mStyles.verticalLabelsColor;
    }

    /**
     * @return  the alignment of the text of the
     *          vertical labels
     */
    public Paint.Align getVerticalLabelsAlign() {
        return mStyles.verticalLabelsAlign;
    }

    /**
     * @return the font color of the horizontal labels
     */
    public int getHorizontalLabelsColor() {
        return mStyles.horizontalLabelsColor;
    }

    /**
     * clears the internal cache and forces
     * to redraw the grid and labels.
     * Normally you should always call {@link GraphView#onDataChanged(boolean, boolean)}
     * which will call this method.
     *
     * @param keepLabelsSize true if you don't want
     *                       to recalculate the size of
     *                       the labels. It is recommended
     *                       to use "true" because this will
     *                       improve performance and prevent
     *                       a flickering.
     * @param keepViewport true if you don't want that
     *                     the viewport will be recalculated.
     *                     It is recommended to use "true" for
     *                     performance.
     */
    public void invalidate(boolean keepLabelsSize, boolean keepViewport) {
        if (!keepViewport) {
            mIsAdjusted = false;
        }
        if (!keepLabelsSize) {
            if (!mLabelVerticalWidthFixed) {
                mLabelVerticalWidth = null;
            }
            mLabelVerticalHeight = null;
            mLabelVerticalSecondScaleWidth = null;
            mLabelVerticalSecondScaleHeight = null;
        }
        //reloadStyles();
    }

    /**
     * calculates the vertical steps of
     * the second scale.
     * This will not do any automatically update
     * of the bounds.
     * Use always manual bounds for the second scale.
     *
     * @return true if it is ready
     */
    protected boolean adjustVerticalSecondScale() {
        if (mLabelHorizontalHeight == null) {
            return false;
        }
        if (mGraphView.mSecondScale == null) {
            return true;
        }

        double minY = mGraphView.mSecondScale.getMinY();
        double maxY = mGraphView.mSecondScale.getMaxY();

        // TODO find the number of labels
        int numVerticalLabels = mNumVerticalLabels;

        double newMinY;
        double exactSteps;

        if (mGraphView.mSecondScale.isYAxisBoundsManual()) {
            newMinY = minY;
            double rangeY = maxY - newMinY;
            exactSteps = rangeY / (numVerticalLabels - 1);
        } else {
            // TODO auto adjusting
            throw new IllegalStateException("Not yet implemented");
        }

        double newMaxY = newMinY + (numVerticalLabels - 1) * exactSteps;

        // TODO auto adjusting
        //mGraphView.getViewport().setMinY(newMinY);
        //mGraphView.getViewport().setMaxY(newMaxY);

        //if (!mGraphView.getViewport().isYAxisBoundsManual()) {
        //    mGraphView.getViewport().setYAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);
        //}

        if (mStepsVerticalSecondScale != null) {
            mStepsVerticalSecondScale.clear();
        } else {
            mStepsVerticalSecondScale = new LinkedHashMap<Integer, Double>(numVerticalLabels);
        }
        int height = mGraphView.getGraphContentHeight();
        double v = newMaxY;
        int p = mGraphView.getGraphContentTop(); // start
        int pixelStep = height / (numVerticalLabels - 1);
        for (int i = 0; i < numVerticalLabels; i++) {
            mStepsVerticalSecondScale.put(p, v);
            p += pixelStep;
            v -= exactSteps;
        }

        return true;
    }

    /**
     * calculates the vertical steps. This will
     * automatically change the bounds to nice
     * human-readable min/max.
     *
     * @return true if it is ready
     */
    protected boolean adjustVertical() {
        if (mLabelHorizontalHeight == null) {
            return false;
        }

        double minY = mGraphView.getViewport().getMinY(false);
        double maxY = mGraphView.getViewport().getMaxY(false);

        if (minY == maxY) {
            return false;
        }

        // TODO find the number of labels
        int numVerticalLabels = mNumVerticalLabels;

        double newMinY;
        double exactSteps;

        if (mGraphView.getViewport().isYAxisBoundsManual()) {
            newMinY = minY;
            double rangeY = maxY - newMinY;
            exactSteps = rangeY / (numVerticalLabels - 1);
        } else {
            // find good steps
            boolean adjusting = true;
            newMinY = minY;
            exactSteps = 0d;
            while (adjusting) {
                double rangeY = maxY - newMinY;
                exactSteps = rangeY / (numVerticalLabels - 1);
                exactSteps = humanRound(exactSteps, true);

                // adjust viewport
                // wie oft passt STEP in minY rein?
                int count = 0;
                if (newMinY >= 0d) {
                    // positive number
                    while (newMinY - exactSteps >= 0) {
                        newMinY -= exactSteps;
                        count++;
                    }
                    newMinY = exactSteps * count;
                } else {
                    // negative number
                    count++;
                    while (newMinY + exactSteps < 0) {
                        newMinY += exactSteps;
                        count++;
                    }
                    newMinY = exactSteps * count * -1;
                }

                // wenn minY sich geändert hat, steps nochmal berechnen
                // wenn nicht, fertig
                if (newMinY == minY) {
                    adjusting = false;
                } else {
                    minY = newMinY;
                }
            }
        }

        double newMaxY = newMinY + (numVerticalLabels - 1) * exactSteps;
        mGraphView.getViewport().setMinY(newMinY);
        mGraphView.getViewport().setMaxY(newMaxY);

        if (!mGraphView.getViewport().isYAxisBoundsManual()) {
            mGraphView.getViewport().setYAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);
        }

        if (mStepsVertical != null) {
            mStepsVertical.clear();
        } else {
            mStepsVertical = new LinkedHashMap<Integer, Double>(numVerticalLabels);
        }
        int height = mGraphView.getGraphContentHeight();
        double v = newMaxY;
        int p = mGraphView.getGraphContentTop(); // start
        int pixelStep = height / (numVerticalLabels - 1);
        for (int i = 0; i < numVerticalLabels; i++) {
            mStepsVertical.put(p, v);
            p += pixelStep;
            v -= exactSteps;
        }

        return true;
    }

    /**
     * calculates the horizontal steps. This will
     * automatically change the bounds to nice
     * human-readable min/max.
     *
     * @return true if it is ready
     */
    protected boolean adjustHorizontal() {
        if (mLabelVerticalWidth == null) {
            return false;
        }

        double minX = mGraphView.getViewport().getMinX(false);
        double maxX = mGraphView.getViewport().getMaxX(false);
        if (minX == maxX) return false;

        // TODO find the number of labels
        int numHorizontalLabels = mNumHorizontalLabels;

        double newMinX;
        double exactSteps;

        float scalingOffset = 0f;
        if (mGraphView.getViewport().isXAxisBoundsManual() && mGraphView.getViewport().getXAxisBoundsStatus() != Viewport.AxisBoundsStatus.READJUST_AFTER_SCALE) {
            // scaling
            if (mGraphView.getViewport().mScalingActive) {
                minX = mGraphView.getViewport().mScalingBeginLeft;
                maxX = minX + mGraphView.getViewport().mScalingBeginWidth;

                //numHorizontalLabels *= (mGraphView.getViewport().mCurrentViewport.width()+oldStep)/(mGraphView.getViewport().mScalingBeginWidth+oldStep);
                //numHorizontalLabels = (float) Math.ceil(numHorizontalLabels);
            }

            newMinX = minX;
            double rangeX = maxX - newMinX;
            exactSteps = rangeX / (numHorizontalLabels - 1);
        } else {
            // find good steps
            boolean adjusting = true;
            newMinX = minX;
            exactSteps = 0d;
            while (adjusting) {
                double rangeX = maxX - newMinX;
                exactSteps = rangeX / (numHorizontalLabels - 1);

                boolean roundAlwaysUp = true;
                if (mGraphView.getViewport().getXAxisBoundsStatus() == Viewport.AxisBoundsStatus.READJUST_AFTER_SCALE) {
                    // if viewports gets smaller, round down
                    if (mGraphView.getViewport().mCurrentViewport.width() < mGraphView.getViewport().mScalingBeginWidth) {
                        roundAlwaysUp = false;
                    }
                }
                exactSteps = humanRound(exactSteps, roundAlwaysUp);

                // adjust viewport
                // wie oft passt STEP in minX rein?
                int count = 0;
                if (newMinX >= 0d) {
                    // positive number
                    while (newMinX - exactSteps >= 0) {
                        newMinX -= exactSteps;
                        count++;
                    }
                    newMinX = exactSteps * count;
                } else {
                    // negative number
                    count++;
                    while (newMinX + exactSteps < 0) {
                        newMinX += exactSteps;
                        count++;
                    }
                    newMinX = exactSteps * count * -1;
                }

                // wenn minX sich geändert hat, steps nochmal berechnen
                // wenn nicht, fertig
                if (newMinX == minX) {
                    adjusting = false;
                } else {
                    minX = newMinX;
                }
            }

            double newMaxX = newMinX + (numHorizontalLabels - 1) * exactSteps;
            mGraphView.getViewport().setMinX(newMinX);
            mGraphView.getViewport().setMaxX(newMaxX);
            if (mGraphView.getViewport().getXAxisBoundsStatus() == Viewport.AxisBoundsStatus.READJUST_AFTER_SCALE) {
                mGraphView.getViewport().setXAxisBoundsStatus(Viewport.AxisBoundsStatus.FIX);
            } else {
                mGraphView.getViewport().setXAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);
            }
        }

        if (mStepsHorizontal != null) {
            mStepsHorizontal.clear();
        } else {
            mStepsHorizontal = new LinkedHashMap<Integer, Double>((int) numHorizontalLabels);
        }
        int width = mGraphView.getGraphContentWidth();

        float scrolled = 0;
        float scrolledPixels = 0;

        double v = newMinX;
        int p = mGraphView.getGraphContentLeft(); // start
        float pixelStep = width / (numHorizontalLabels - 1);

        if (mGraphView.getViewport().mScalingActive) {
            float oldStep = mGraphView.getViewport().mScalingBeginWidth / (numHorizontalLabels - 1);
            float factor = (mGraphView.getViewport().mCurrentViewport.width() + oldStep) / (mGraphView.getViewport().mScalingBeginWidth + oldStep);
            pixelStep *= 1f / factor;

            //numHorizontalLabels *= (mGraphView.getViewport().mCurrentViewport.width()+oldStep)/(mGraphView.getViewport().mScalingBeginWidth+oldStep);
            //numHorizontalLabels = (float) Math.ceil(numHorizontalLabels);

            //scrolled = ((float) mGraphView.getViewport().getMinX(false) - mGraphView.getViewport().mScalingBeginLeft)*2;
            float newWidth = width * 1f / factor;
            scrolledPixels = (newWidth - width) * -0.5f;

        }

        // scrolling
        if (!Float.isNaN(mGraphView.getViewport().mScrollingReferenceX)) {
            scrolled = mGraphView.getViewport().mScrollingReferenceX - (float) newMinX;
            scrolledPixels += scrolled * (pixelStep / (float) exactSteps);

            if (scrolled < 0 - exactSteps) {
                mGraphView.getViewport().mScrollingReferenceX += exactSteps;
            } else if (scrolled > exactSteps) {
                mGraphView.getViewport().mScrollingReferenceX -= exactSteps;
            }
        }
        p += scrolledPixels;
        v += scrolled;

        for (int i = 0; i < numHorizontalLabels; i++) {
            // don't draw steps before 0 (scrolling)
            if (p >= mGraphView.getGraphContentLeft()) {
                mStepsHorizontal.put(p, v);
            }
            p += pixelStep;
            v += exactSteps;
        }

        return true;
    }

    /**
     * adjusts the grid and labels to match to the data
     * this will automatically change the bounds to
     * nice human-readable values, except the bounds
     * are manual.
     */
    protected void adjust() {
        mIsAdjusted = adjustVertical();
        mIsAdjusted &= adjustVerticalSecondScale();
        mIsAdjusted &= adjustHorizontal();
    }

    /**
     * calculates the vertical label size
     * @param canvas canvas
     */
    protected void calcLabelVerticalSize(Canvas canvas) {
        // test label with first and last label
        String testLabel = mLabelFormatter.formatLabel(mGraphView.getViewport().getMaxY(false), false);
        if (testLabel == null) testLabel = "";

        Rect textBounds = new Rect();
        mPaintLabel.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
        mLabelVerticalWidth = textBounds.width();
        mLabelVerticalHeight = textBounds.height();

        testLabel = mLabelFormatter.formatLabel(mGraphView.getViewport().getMinY(false), false);
        if (testLabel == null) testLabel = "";

        mPaintLabel.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
        mLabelVerticalWidth = Math.max(mLabelVerticalWidth, textBounds.width());

        // add some pixel to get a margin
        mLabelVerticalWidth += 6;

        // space between text and graph content
        mLabelVerticalWidth += mStyles.labelsSpace;

        // multiline
        int lines = 1;
        for (byte c : testLabel.getBytes()) {
            if (c == '\n') lines++;
        }
        mLabelVerticalHeight *= lines;
    }

    /**
     * calculates the vertical second scale
     * label size
     * @param canvas canvas
     */
    protected void calcLabelVerticalSecondScaleSize(Canvas canvas) {
        if (mGraphView.mSecondScale == null) {
            mLabelVerticalSecondScaleWidth = 0;
            mLabelVerticalSecondScaleHeight = 0;
            return;
        }

        // test label
        double testY = ((mGraphView.mSecondScale.getMaxY() - mGraphView.mSecondScale.getMinY()) * 0.783) + mGraphView.mSecondScale.getMinY();
        String testLabel = mGraphView.mSecondScale.getLabelFormatter().formatLabel(testY, false);
        Rect textBounds = new Rect();
        mPaintLabel.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
        mLabelVerticalSecondScaleWidth = textBounds.width();
        mLabelVerticalSecondScaleHeight = textBounds.height();

        // multiline
        int lines = 1;
        for (byte c : testLabel.getBytes()) {
            if (c == '\n') lines++;
        }
        mLabelVerticalSecondScaleHeight *= lines;
    }

    /**
     * calculates the horizontal label size
     * @param canvas canvas
     */
    protected void calcLabelHorizontalSize(Canvas canvas) {
        // test label
        double testX = ((mGraphView.getViewport().getMaxX(false) - mGraphView.getViewport().getMinX(false)) * 0.783) + mGraphView.getViewport().getMinX(false);
        String testLabel = mLabelFormatter.formatLabel(testX, true);
        if (testLabel == null) {
            testLabel = "";
        }
        Rect textBounds = new Rect();
        mPaintLabel.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
        mLabelHorizontalWidth = textBounds.width();

        if (!mLabelHorizontalHeightFixed) {
            mLabelHorizontalHeight = textBounds.height();

            // multiline
            int lines = 1;
            for (byte c : testLabel.getBytes()) {
                if (c == '\n') lines++;
            }
            mLabelHorizontalHeight *= lines;

            mLabelHorizontalHeight = (int) Math.max(mLabelHorizontalHeight, mStyles.textSize);
        }

        // space between text and graph content
        mLabelHorizontalHeight += mStyles.labelsSpace;
    }

    /**
     * do the drawing of the grid
     * and labels
     * @param canvas canvas
     */
    public void draw(Canvas canvas) {
        boolean labelSizeChanged = false;
        if (mLabelHorizontalWidth == null) {
            calcLabelHorizontalSize(canvas);
            labelSizeChanged = true;
        }
        if (mLabelVerticalWidth == null) {
            calcLabelVerticalSize(canvas);
            labelSizeChanged = true;
        }
        if (mLabelVerticalSecondScaleWidth == null) {
            calcLabelVerticalSecondScaleSize(canvas);
            labelSizeChanged = true;
        }
        if (labelSizeChanged) {
            // redraw
            ViewCompat.postInvalidateOnAnimation(mGraphView);
            return;
        }

        if (!mIsAdjusted) {
            adjust();
        }

        if (mIsAdjusted) {
            drawVerticalSteps(canvas);
            drawVerticalStepsSecondScale(canvas);
            drawHorizontalSteps(canvas);
        } else {
            // we can not draw anything
            return;
        }

        drawHorizontalAxisTitle(canvas);
        drawVerticalAxisTitle(canvas);
    }

    /**
     * draws the horizontal axis title if
     * it is set
     * @param canvas canvas
     */
    protected void drawHorizontalAxisTitle(Canvas canvas) {
        if (mHorizontalAxisTitle != null && mHorizontalAxisTitle.length() > 0) {
            mPaintAxisTitle.setColor(getHorizontalAxisTitleColor());
            mPaintAxisTitle.setTextSize(getHorizontalAxisTitleTextSize());
            float x = canvas.getWidth() / 2;
            float y = canvas.getHeight() - mStyles.padding;
            canvas.drawText(mHorizontalAxisTitle, x, y, mPaintAxisTitle);
        }
    }

    /**
     * draws the vertical axis title if
     * it is set
     * @param canvas canvas
     */
    protected void drawVerticalAxisTitle(Canvas canvas) {
        if (mVerticalAxisTitle != null && mVerticalAxisTitle.length() > 0) {
            mPaintAxisTitle.setColor(getVerticalAxisTitleColor());
            mPaintAxisTitle.setTextSize(getVerticalAxisTitleTextSize());
            float x = getVerticalAxisTitleWidth();
            float y = canvas.getHeight() / 2;
            canvas.save();
            canvas.rotate(-90, x, y);
            canvas.drawText(mVerticalAxisTitle, x, y, mPaintAxisTitle);
            canvas.restore();
        }
    }

    /**
     * @return  the horizontal axis title height
     *          or 0 if there is no title
     */
    public int getHorizontalAxisTitleHeight() {
        if (mHorizontalAxisTitle != null && mHorizontalAxisTitle.length() > 0) {
            return (int) getHorizontalAxisTitleTextSize();
        } else {
            return 0;
        }
    }

    /**
     * @return  the vertical axis title width
     *          or 0 if there is no title
     */
    public int getVerticalAxisTitleWidth() {
        if (mVerticalAxisTitle != null && mVerticalAxisTitle.length() > 0) {
            return (int) getVerticalAxisTitleTextSize();
        } else {
            return 0;
        }
    }

    /**
     * draws the horizontal steps
     * vertical lines and horizontal labels
     *
     * @param canvas canvas
     */
    protected void drawHorizontalSteps(Canvas canvas) {
        // draw horizontal steps (vertical lines and horizontal labels)
        mPaintLabel.setColor(getHorizontalLabelsColor());
        int i = 0;
        for (Map.Entry<Integer, Double> e : mStepsHorizontal.entrySet()) {
            // draw line
            if (mStyles.highlightZeroLines) {
                if (e.getValue() == 0d) {
                    mPaintLine.setStrokeWidth(5);
                } else {
                    mPaintLine.setStrokeWidth(0);
                }
            }
            if (mStyles.gridStyle.drawVertical()) {
                canvas.drawLine(e.getKey(), mGraphView.getGraphContentTop(), e.getKey(), mGraphView.getGraphContentTop() + mGraphView.getGraphContentHeight(), mPaintLine);
            }

            // draw label
            if (isHorizontalLabelsVisible()) {
                mPaintLabel.setTextAlign(Paint.Align.CENTER);
                if (i == mStepsHorizontal.size() - 1)
                    mPaintLabel.setTextAlign(Paint.Align.RIGHT);
                if (i == 0)
                    mPaintLabel.setTextAlign(Paint.Align.LEFT);

                // multiline labels
                String label = mLabelFormatter.formatLabel(e.getValue(), true);
                if (label == null) {
                    label = "";
                }
                String[] lines = label.split("\n");
                for (int li = 0; li < lines.length; li++) {
                    // for the last line y = height
                    float y = (canvas.getHeight() - mStyles.padding - getHorizontalAxisTitleHeight()) - (lines.length - li - 1) * getTextSize() * 1.1f + mStyles.labelsSpace;
                    canvas.drawText(lines[li], e.getKey(), y, mPaintLabel);
                }
            }
            i++;
        }
    }

    /**
     * draws the vertical steps for the
     * second scale on the right side
     *
     * @param canvas canvas
     */
    protected void drawVerticalStepsSecondScale(Canvas canvas) {
        if (mGraphView.mSecondScale == null) {
            return;
        }

        // draw only the vertical labels on the right
        float startLeft = mGraphView.getGraphContentLeft() + mGraphView.getGraphContentWidth();
        mPaintLabel.setColor(getVerticalLabelsSecondScaleColor());
        mPaintLabel.setTextAlign(getVerticalLabelsSecondScaleAlign());
        for (Map.Entry<Integer, Double> e : mStepsVerticalSecondScale.entrySet()) {
            // draw label
            int labelsWidth = mLabelVerticalSecondScaleWidth;
            int labelsOffset = (int) startLeft;
            if (getVerticalLabelsSecondScaleAlign() == Paint.Align.RIGHT) {
                labelsOffset += labelsWidth;
            } else if (getVerticalLabelsSecondScaleAlign() == Paint.Align.CENTER) {
                labelsOffset += labelsWidth / 2;
            }

            float y = e.getKey();

            String[] lines = mGraphView.mSecondScale.mLabelFormatter.formatLabel(e.getValue(), false).split("\n");
            y += (lines.length * getTextSize() * 1.1f) / 2; // center text vertically
            for (int li = 0; li < lines.length; li++) {
                // for the last line y = height
                float y2 = y - (lines.length - li - 1) * getTextSize() * 1.1f;
                canvas.drawText(lines[li], labelsOffset, y2, mPaintLabel);
            }
        }
    }

    /**
     * draws the vertical steps
     * horizontal lines and vertical labels
     *
     * @param canvas canvas
     */
    protected void drawVerticalSteps(Canvas canvas) {
        // draw vertical steps (horizontal lines and vertical labels)
        float startLeft = mGraphView.getGraphContentLeft();
        mPaintLabel.setColor(getVerticalLabelsColor());
        mPaintLabel.setTextAlign(getVerticalLabelsAlign());
        for (Map.Entry<Integer, Double> e : mStepsVertical.entrySet()) {
            // draw line
            if (mStyles.highlightZeroLines) {
                if (e.getValue() == 0d) {
                    mPaintLine.setStrokeWidth(5);
                } else {
                    mPaintLine.setStrokeWidth(0);
                }
            }
            if (mStyles.gridStyle.drawHorizontal()) {
                canvas.drawLine(startLeft, e.getKey(), startLeft + mGraphView.getGraphContentWidth(), e.getKey(), mPaintLine);
            }

            // draw label
            if (isVerticalLabelsVisible()) {
                int labelsWidth = mLabelVerticalWidth;
                int labelsOffset = 0;
                if (getVerticalLabelsAlign() == Paint.Align.RIGHT) {
                    labelsOffset = labelsWidth;
                    labelsOffset -= mStyles.labelsSpace;
                } else if (getVerticalLabelsAlign() == Paint.Align.CENTER) {
                    labelsOffset = labelsWidth / 2;
                }
                labelsOffset += mStyles.padding + getVerticalAxisTitleWidth();

                float y = e.getKey();

                String label = mLabelFormatter.formatLabel(e.getValue(), false);
                if (label == null) {
                    label = "";
                }
                String[] lines = label.split("\n");
                y += (lines.length * getTextSize() * 1.1f) / 2; // center text vertically
                for (int li = 0; li < lines.length; li++) {
                    // for the last line y = height
                    float y2 = y - (lines.length - li - 1) * getTextSize() * 1.1f;
                    canvas.drawText(lines[li], labelsOffset, y2, mPaintLabel);
                }
            }
        }
    }

    /**
     * this will do rounding to generate
     * nice human-readable bounds.
     *
     * @param in the raw value that is to be rounded
     * @param roundAlwaysUp true if it shall always round up (ceil)
     * @return the rounded number
     */
    protected double humanRound(double in, boolean roundAlwaysUp) {
        // round-up to 1-steps, 2-steps or 5-steps
        int ten = 0;
        while (in >= 10d) {
            in /= 10d;
            ten++;
        }
        while (in < 1d) {
            in *= 10d;
            ten--;
        }
        if (roundAlwaysUp) {
            if (in == 1d) {
            } else if (in <= 2d) {
                in = 2d;
            } else if (in <= 5d) {
                in = 5d;
            } else if (in < 10d) {
                in = 10d;
            }
        } else { // always round down
            if (in == 1d) {
            } else if (in <= 4.9d) {
                in = 2d;
            } else if (in <= 9.9d) {
                in = 5d;
            } else if (in < 15d) {
                in = 10d;
            }
        }
        return in * Math.pow(10d, ten);
    }

    /**
     * @return the wrapped styles
     */
    public Styles getStyles() {
        return mStyles;
    }

    /**
     * @return  the vertical label width
     *          0 if there are no vertical labels
     */
    public int getLabelVerticalWidth() {
        return mLabelVerticalWidth == null || !isVerticalLabelsVisible() ? 0 : mLabelVerticalWidth;
    }

    /**
     * sets a manual and fixed with of the space for
     * the vertical labels. This will prevent GraphView to
     * calculate the width automatically.
     *
     * @param width     the width of the space for the vertical labels.
     *                  Use null to let GraphView automatically calculate the width.
     */
    public void setLabelVerticalWidth(Integer width) {
        mLabelVerticalWidth = width;
        mLabelVerticalWidthFixed = mLabelVerticalWidth != null;
    }

    /**
     * @return  the horizontal label height
     *          0 if there are no horizontal labels
     */
    public int getLabelHorizontalHeight() {
        return mLabelHorizontalHeight == null || !isHorizontalLabelsVisible() ? 0 : mLabelHorizontalHeight;
    }

    /**
     * sets a manual and fixed height of the space for
     * the horizontal labels. This will prevent GraphView to
     * calculate the height automatically.
     *
     * @param height     the height of the space for the horizontal labels.
     *                  Use null to let GraphView automatically calculate the height.
     */
    public void setLabelHorizontalHeight(Integer height) {
        mLabelHorizontalHeight = height;
        mLabelHorizontalHeightFixed = mLabelHorizontalHeight != null;
    }

    /**
     * @return the grid line color
     */
    public int getGridColor() {
        return mStyles.gridColor;
    }

    /**
     * @return whether the line at 0 are highlighted
     */
    public boolean isHighlightZeroLines() {
        return mStyles.highlightZeroLines;
    }

    /**
     * @return the padding around the grid and labels
     */
    public int getPadding() {
        return mStyles.padding;
    }

    /**
     * @param textSize  the general text size of the axis titles.
     *                  can be overwritten with {@link #setVerticalAxisTitleTextSize(float)}
     *                  and {@link #setHorizontalAxisTitleTextSize(float)}
     */
    public void setTextSize(float textSize) {
        mStyles.textSize = textSize;
    }

    /**
     * @param verticalLabelsAlign the alignment of the vertical labels
     */
    public void setVerticalLabelsAlign(Paint.Align verticalLabelsAlign) {
        mStyles.verticalLabelsAlign = verticalLabelsAlign;
    }

    /**
     * @param verticalLabelsColor the color of the vertical labels
     */
    public void setVerticalLabelsColor(int verticalLabelsColor) {
        mStyles.verticalLabelsColor = verticalLabelsColor;
    }

    /**
     * @param horizontalLabelsColor the color of the horizontal labels
     */
    public void setHorizontalLabelsColor(int horizontalLabelsColor) {
        mStyles.horizontalLabelsColor = horizontalLabelsColor;
    }

    /**
     * @param gridColor the color of the grid lines
     */
    public void setGridColor(int gridColor) {
        mStyles.gridColor = gridColor;
    }

    /**
     * @param highlightZeroLines    flag whether the zero-lines (vertical+
     *                              horizontal) shall be highlighted
     */
    public void setHighlightZeroLines(boolean highlightZeroLines) {
        mStyles.highlightZeroLines = highlightZeroLines;
    }

    /**
     * @param padding the padding around the graph and labels
     */
    public void setPadding(int padding) {
        mStyles.padding = padding;
    }

    /**
     * @return  the label formatter, that converts
     *          the raw numbers to strings
     */
    public LabelFormatter getLabelFormatter() {
        return mLabelFormatter;
    }

    /**
     * @param mLabelFormatter   the label formatter, that converts
     *                          the raw numbers to strings
     */
    public void setLabelFormatter(LabelFormatter mLabelFormatter) {
        this.mLabelFormatter = mLabelFormatter;
        mLabelFormatter.setViewport(mGraphView.getViewport());
    }

    /**
     * @return the title of the horizontal axis
     */
    public String getHorizontalAxisTitle() {
        return mHorizontalAxisTitle;
    }

    /**
     * @param mHorizontalAxisTitle the title of the horizontal axis
     */
    public void setHorizontalAxisTitle(String mHorizontalAxisTitle) {
        this.mHorizontalAxisTitle = mHorizontalAxisTitle;
    }

    /**
     * @return the title of the vertical axis
     */
    public String getVerticalAxisTitle() {
        return mVerticalAxisTitle;
    }

    /**
     * @param mVerticalAxisTitle the title of the vertical axis
     */
    public void setVerticalAxisTitle(String mVerticalAxisTitle) {
        this.mVerticalAxisTitle = mVerticalAxisTitle;
    }

    /**
     * @return font size of the vertical axis title
     */
    public float getVerticalAxisTitleTextSize() {
        return mStyles.verticalAxisTitleTextSize;
    }

    /**
     * @param verticalAxisTitleTextSize font size of the vertical axis title
     */
    public void setVerticalAxisTitleTextSize(float verticalAxisTitleTextSize) {
        mStyles.verticalAxisTitleTextSize = verticalAxisTitleTextSize;
    }

    /**
     * @return font color of the vertical axis title
     */
    public int getVerticalAxisTitleColor() {
        return mStyles.verticalAxisTitleColor;
    }

    /**
     * @param verticalAxisTitleColor font color of the vertical axis title
     */
    public void setVerticalAxisTitleColor(int verticalAxisTitleColor) {
        mStyles.verticalAxisTitleColor = verticalAxisTitleColor;
    }

    /**
     * @return font size of the horizontal axis title
     */
    public float getHorizontalAxisTitleTextSize() {
        return mStyles.horizontalAxisTitleTextSize;
    }

    /**
     * @param horizontalAxisTitleTextSize font size of the horizontal axis title
     */
    public void setHorizontalAxisTitleTextSize(float horizontalAxisTitleTextSize) {
        mStyles.horizontalAxisTitleTextSize = horizontalAxisTitleTextSize;
    }

    /**
     * @return font color of the horizontal axis title
     */
    public int getHorizontalAxisTitleColor() {
        return mStyles.horizontalAxisTitleColor;
    }

    /**
     * @param horizontalAxisTitleColor font color of the horizontal axis title
     */
    public void setHorizontalAxisTitleColor(int horizontalAxisTitleColor) {
        mStyles.horizontalAxisTitleColor = horizontalAxisTitleColor;
    }

    /**
     * @return the alignment of the labels on the right side
     */
    public Paint.Align getVerticalLabelsSecondScaleAlign() {
        return mStyles.verticalLabelsSecondScaleAlign;
    }

    /**
     * @param verticalLabelsSecondScaleAlign the alignment of the labels on the right side
     */
    public void setVerticalLabelsSecondScaleAlign(Paint.Align verticalLabelsSecondScaleAlign) {
        mStyles.verticalLabelsSecondScaleAlign = verticalLabelsSecondScaleAlign;
    }

    /**
     * @return the color of the labels on the right side
     */
    public int getVerticalLabelsSecondScaleColor() {
        return mStyles.verticalLabelsSecondScaleColor;
    }

    /**
     * @param verticalLabelsSecondScaleColor the color of the labels on the right side
     */
    public void setVerticalLabelsSecondScaleColor(int verticalLabelsSecondScaleColor) {
        mStyles.verticalLabelsSecondScaleColor = verticalLabelsSecondScaleColor;
    }

    /**
     * @return  the width of the vertical labels
     *          of the second scale
     */
    public int getLabelVerticalSecondScaleWidth() {
        return mLabelVerticalSecondScaleWidth==null?0:mLabelVerticalSecondScaleWidth;
    }

    /**
     * @return  flag whether the horizontal labels are
     *          visible
     */
    public boolean isHorizontalLabelsVisible() {
        return mStyles.horizontalLabelsVisible;
    }

    /**
     * @param horizontalTitleVisible    flag whether the horizontal labels are
     *                                  visible
     */
    public void setHorizontalLabelsVisible(boolean horizontalTitleVisible) {
        mStyles.horizontalLabelsVisible = horizontalTitleVisible;
    }

    /**
     * @return  flag whether the vertical labels are
     *          visible
     */
    public boolean isVerticalLabelsVisible() {
        return mStyles.verticalLabelsVisible;
    }

    /**
     * @param verticalTitleVisible  flag whether the vertical labels are
     *                              visible
     */
    public void setVerticalLabelsVisible(boolean verticalTitleVisible) {
        mStyles.verticalLabelsVisible = verticalTitleVisible;
    }

    /**
     * @return  count of the vertical labels, that
     *          will be shown at one time.
     */
    public int getNumVerticalLabels() {
        return mNumVerticalLabels;
    }

    /**
     * @param mNumVerticalLabels    count of the vertical labels, that
     *                              will be shown at one time.
     */
    public void setNumVerticalLabels(int mNumVerticalLabels) {
        this.mNumVerticalLabels = mNumVerticalLabels;
    }

    /**
     * @return  count of the horizontal labels, that
     *          will be shown at one time.
     */
    public int getNumHorizontalLabels() {
        return mNumHorizontalLabels;
    }

    /**
     * @param mNumHorizontalLabels  count of the horizontal labels, that
     *                              will be shown at one time.
     */
    public void setNumHorizontalLabels(int mNumHorizontalLabels) {
        this.mNumHorizontalLabels = mNumHorizontalLabels;
    }

    /**
     * @return the grid style
     */
    public GridStyle getGridStyle() {
        return mStyles.gridStyle;
    }

    /**
     * Define which grid lines shall be drawn
     *
     * @param gridStyle the grid style
     */
    public void setGridStyle(GridStyle gridStyle) {
        mStyles.gridStyle = gridStyle;
    }

    /**
     * @return the space between the labels text and the graph content
     */
    public int getLabelsSpace() {
        return mStyles.labelsSpace;
    }

    /**
     * the space between the labels text and the graph content
     *
     * @param labelsSpace the space between the labels text and the graph content
     */
    public void setLabelsSpace(int labelsSpace) {
        mStyles.labelsSpace = labelsSpace;
    }
}
