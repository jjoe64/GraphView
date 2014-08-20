package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonas on 13.08.14.
 */
public class GraphView extends View {
    private List<LineGraphSeries> mSeries;
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

        mSeries = new ArrayList<LineGraphSeries>();
    }

    public void addSeries(LineGraphSeries s) {
        mSeries.add(s);
    }

    public List<LineGraphSeries> getSeries() {
        // TODO immutable array
        return null;
    }

    protected void onDataChanged() {
        // adjust grid system
        mGridLabelRenderer.adjust();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mGridLabelRenderer.draw(canvas);
    }

    public Viewport getViewport() {
        return mViewport;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // test data
        mViewport.setMaxY(650);
        mViewport.setMinY(-50);
        onDataChanged();

    }
}
