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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jjoe64
 * @version 4.0.0
 */
public class GraphView extends View {
    /**
     * Class to wrap style options that are general
     * to graphs.
     *
     * @author jjoe64
     */
    private static final class Styles {
        /**
         * The font size of the title that can be displayed
         * above the graph.
         *
         * @see GraphView#setTitle(String)
         */
        float titleTextSize;

        /**
         * The font color of the title that can be displayed
         * above the graph.
         *
         * @see GraphView#setTitle(String)
         */
        int titleColor;
    }

    /**
     * Helper class to detect tap events on the
     * graph.
     *
     * @author jjoe64
     */
    private class TapDetector {
        /**
         * save the time of the last down event
         */
        private long lastDown;

        /**
         * point of the tap down event
         */
        private PointF lastPoint;

        /**
         * to be called to process the events
         *
         * @param event
         * @return true if there was a tap event. otherwise returns false.
         */
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastDown = System.currentTimeMillis();
                lastPoint = new PointF(event.getX(), event.getY());
            } else if (lastDown > 0 && event.getAction() == MotionEvent.ACTION_MOVE) {
                if (Math.abs(event.getX() - lastPoint.x) > 60
                        || Math.abs(event.getY() - lastPoint.y) > 60) {
                    lastDown = 0;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (System.currentTimeMillis() - lastDown < 400) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * our series (this does not contain the series
     * that can be displayed on the right side. The
     * right side series is a special feature of
     * the {@link SecondScale} feature.
     */
    private List<Series> mSeries;

    /**
     * the renderer for the grid and labels
     */
    private GridLabelRenderer mGridLabelRenderer;

    /**
     * viewport that holds the current bounds of
     * view.
     */
    private Viewport mViewport;

    /**
     * title of the graph that will be shown above
     */
    private String mTitle;

    /**
     * wraps the general styles
     */
    private Styles mStyles;

    /**
     * feature to have a second scale e.g. on the
     * right side
     */
    protected SecondScale mSecondScale;

    /**
     * tap detector
     */
    private TapDetector mTapDetector;

    /**
     * renderer for the legend
     */
    private LegendRenderer mLegendRenderer;

    /**
     * paint for the graph title
     */
    private Paint mPaintTitle;

    /**
     * paint for the preview (in the SDK)
     */
    private Paint mPreviewPaint;

    /**
     * Initialize the GraphView view
     * @param context
     */
    public GraphView(Context context) {
        super(context);
        init();
    }

    /**
     * Initialize the GraphView view.
     *
     * @param context
     * @param attrs
     */
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize the GraphView view
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * initialize the internal objects.
     * This method has to be called directly
     * in the constructors.
     */
    protected void init() {
        mPreviewPaint = new Paint();
        mPreviewPaint.setTextAlign(Paint.Align.CENTER);
        mPreviewPaint.setColor(Color.BLACK);
        mPreviewPaint.setTextSize(50);

        mStyles = new Styles();
        mViewport = new Viewport(this);
        mGridLabelRenderer = new GridLabelRenderer(this);
        mLegendRenderer = new LegendRenderer(this);

        mSeries = new ArrayList<Series>();
        mPaintTitle = new Paint();

        mTapDetector = new TapDetector();

        loadStyles();
    }

    /**
     * loads the font
     */
    protected void loadStyles() {
        mStyles.titleColor = mGridLabelRenderer.getHorizontalLabelsColor();
        mStyles.titleTextSize = mGridLabelRenderer.getTextSize();
    }

    /**
     * @return the renderer for the grid and labels
     */
    public GridLabelRenderer getGridLabelRenderer() {
        return mGridLabelRenderer;
    }

    /**
     * Add a new series to the graph. This will
     * automatically redraw the graph.
     * @param s the series to be added
     */
    public void addSeries(Series s) {
        s.onGraphViewAttached(this);
        mSeries.add(s);
        onDataChanged(false, false);
    }

    /**
     * important: do not do modifications on the list
     * object that will be returned.
     * Use {link #removeSeries} and {link #addSeries}
     *
     * @return all series
     */
    public List<Series> getSeries() {
        // TODO immutable array
        return mSeries;
    }

    /**
     *
     * @param keepLabelsSize
     * @param keepViewport
     */
    public void onDataChanged(boolean keepLabelsSize, boolean keepViewport) {
        // adjust grid system
        mViewport.calcCompleteRange();
        mGridLabelRenderer.invalidate(keepLabelsSize, keepViewport);
        invalidate();
    }

    /**
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            canvas.drawColor(Color.rgb(200, 200, 200));
            canvas.drawText("GraphView: No Preview available", canvas.getWidth()/2, canvas.getHeight()/2, mPreviewPaint);
        } else {
            drawTitle(canvas);
            mViewport.drawFirst(canvas);
            mGridLabelRenderer.draw(canvas);
            for (Series s : mSeries) {
                s.draw(this, canvas, false);
            }
            if (mSecondScale != null) {
                for (Series s : mSecondScale.getSeries()) {
                    s.draw(this, canvas, true);
                }
            }
            mViewport.draw(canvas);
            mLegendRenderer.draw(canvas);
        }
    }

    /**
     *
     * @param canvas
     */
    protected void drawTitle(Canvas canvas) {
        if (mTitle != null && mTitle.length()>0) {
            mPaintTitle.setColor(mStyles.titleColor);
            mPaintTitle.setTextSize(mStyles.titleTextSize);
            mPaintTitle.setTextAlign(Paint.Align.CENTER);
            float x = canvas.getWidth()/2;
            float y = mPaintTitle.getTextSize();
            canvas.drawText(mTitle, x, y, mPaintTitle);
        }
    }

    /**
     *
     * @return
     */
    protected int getTitleHeight() {
        if (mTitle != null && mTitle.length()>0) {
            return (int) mPaintTitle.getTextSize();
        } else {
            return 0;
        }
    }

    /**
     *
     * @return
     */
    public Viewport getViewport() {
        return mViewport;
    }

    /**
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onDataChanged(true, true);
    }

    /**
     *
     * @return
     */
    public int getGraphContentLeft() {
        int border = getGridLabelRenderer().getStyles().padding;
        return border + getGridLabelRenderer().getLabelVerticalWidth() + getGridLabelRenderer().getVerticalAxisTitleWidth();
    }

    /**
     *
     * @return
     */
    public int getGraphContentTop() {
        int border = getGridLabelRenderer().getStyles().padding + getTitleHeight();
        return border;
    }

    /**
     *
     * @return
     */
    public int getGraphContentHeight() {
        int border = getGridLabelRenderer().getStyles().padding;
        int graphheight = getHeight() - (2 * border) - getGridLabelRenderer().getLabelHorizontalHeight() - getTitleHeight();
        graphheight -= getGridLabelRenderer().getHorizontalAxisTitleHeight();
        return graphheight;
    }

    /**
     *
     * @return
     */
    public int getGraphContentWidth() {
        int border = getGridLabelRenderer().getStyles().padding;
        int graphwidth = getWidth() - (2 * border) - getGridLabelRenderer().getLabelVerticalWidth();
        if (mSecondScale != null) {
            graphwidth -= getGridLabelRenderer().getLabelVerticalSecondScaleWidth();
        }
        return graphwidth;
    }

    /**
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean b = mViewport.onTouchEvent(event);
        boolean a = super.onTouchEvent(event);

        // is it a click?
        if (mTapDetector.onTouchEvent(event)) {
            Log.d("GraphView", "tap detected");
            for (Series s : mSeries) {
                s.onTap(event.getX(), event.getY());
            }
            if (mSecondScale != null) {
                for (Series s : mSecondScale.getSeries()) {
                    s.onTap(event.getX(), event.getY());
                }
            }
        }

        return b || a;
    }

    /**
     *
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        mViewport.computeScroll();
    }

    /**
     *
     * @return
     */
    public LegendRenderer getLegendRenderer() {
        return mLegendRenderer;
    }

    /**
     *
     * @param mLegendRenderer
     */
    public void setLegendRenderer(LegendRenderer mLegendRenderer) {
        this.mLegendRenderer = mLegendRenderer;
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     *
     * @param mTitle
     */
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    /**
     *
     * @return
     */
    public float getTitleTextSize() {
        return mStyles.titleTextSize;
    }

    /**
     *
     * @param titleTextSize
     */
    public void setTitleTextSize(float titleTextSize) {
        mStyles.titleTextSize = titleTextSize;
    }

    /**
     *
     * @return
     */
    public int getTitleColor() {
        return mStyles.titleColor;
    }

    /**
     *
     * @param titleColor
     */
    public void setTitleColor(int titleColor) {
        mStyles.titleColor = titleColor;
    }

    /**
     *
     * @return
     */
    public SecondScale getSecondScale() {
        if (mSecondScale == null) {
            mSecondScale = new SecondScale();
        }
        return mSecondScale;
    }

    /**
     *
     */
    public void removeAllSeries() {
        mSeries.clear();
        onDataChanged(false, false);
    }

    /**
     *
     * @param series
     */
    public void removeSeries(Series<?> series) {
        mSeries.remove(series);
        onDataChanged(false, false);
    }
}
