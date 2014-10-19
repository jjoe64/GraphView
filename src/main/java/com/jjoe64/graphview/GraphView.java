package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonas on 13.08.14.
 */
public class GraphView extends View {
    private List<Series> mSeries;
    private GridLabelRenderer mGridLabelRenderer;
    private Viewport mViewport;
    private LegendRenderer mLegendRenderer;
    private TitleRenderer mTitleRenderer;

    public GraphView(Context context) {
        super(context);
        init();
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        mViewport = new Viewport(this);
        mGridLabelRenderer = new GridLabelRenderer(this);
        mLegendRenderer = new LegendRenderer(this);
        mTitleRenderer = new TitleRenderer(this);

        mSeries = new ArrayList<Series>();
    }

    public GridLabelRenderer getGridLabelRenderer() {
        return mGridLabelRenderer;
    }

    public void addSeries(Series s) {
        mSeries.add(s);
    }

    public List<Series> getSeries() {
        // TODO immutable array
        return mSeries;
    }

    protected void onDataChanged() {
        // adjust grid system
        mGridLabelRenderer.adjust();
        mViewport.calcCompleteRange();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mGridLabelRenderer.draw(canvas);
        for (Series s : mSeries) {
            s.draw(this, canvas);
        }
        mViewport.draw(canvas);
    }

    public Viewport getViewport() {
        return mViewport;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onDataChanged();
    }

    int getGraphContentLeft() {
        int border = getGridLabelRenderer().getStyles().padding;
        return border + getGridLabelRenderer().getLabelVerticalWidth();
    }

    int getGraphContentTop() {
        int border = getGridLabelRenderer().getStyles().padding;
        return border + getGridLabelRenderer().getLabelHorizontalHeight();
    }

    int getGraphContentHeight() {
        int border = getGridLabelRenderer().getStyles().padding;
        int graphheight = getHeight() - (2 * border) - getGridLabelRenderer().getLabelHorizontalHeight();
        return graphheight;
    }

    int getGraphContentWidth() {
        int border = getGridLabelRenderer().getStyles().padding;
        int graphwidth = getWidth() - (2 * border) - getGridLabelRenderer().getLabelVerticalWidth();
        return graphwidth;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean b = mViewport.onTouchEvent(event);
        return b || super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        mViewport.computeScroll();
    }
}
