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
 * Created by jonas on 13.08.14.
 */
public class GraphView extends View {
    private static final class Styles {
        float titleTextSize;
        int titleColor;
    }

    private class TapDetector {
        private long lastDown;
        private PointF lastPoint;

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

    private List<Series> mSeries;
    private GridLabelRenderer mGridLabelRenderer;
    private Viewport mViewport;
    private String mTitle;
    private Styles mStyles;
    protected SecondScale mSecondScale;
    private TapDetector mTapDetector;

    private LegendRenderer mLegendRenderer;
    private TitleRenderer mTitleRenderer;

    private Paint mPaintTitle;

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
        mStyles = new Styles();
        mViewport = new Viewport(this);
        mGridLabelRenderer = new GridLabelRenderer(this);
        mLegendRenderer = new LegendRenderer(this);
        mTitleRenderer = new TitleRenderer(this);

        mSeries = new ArrayList<Series>();
        mPaintTitle = new Paint();

        mTapDetector = new TapDetector();

        loadStyles();
    }

    public void loadStyles() {
        mStyles.titleColor = mGridLabelRenderer.getHorizontalLabelsColor();
        mStyles.titleTextSize = mGridLabelRenderer.getTextSize();
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

    public void onDataChanged() {
        // adjust grid system
        mViewport.calcCompleteRange();
        mGridLabelRenderer.adjust();
    }

    @Override
    protected void onDraw(Canvas canvas) {
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

    protected int getTitleHeight() {
        if (mTitle != null && mTitle.length()>0) {
            return (int) mPaintTitle.getTextSize();
        } else {
            return 0;
        }
    }

    public Viewport getViewport() {
        return mViewport;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onDataChanged();
    }

    public int getGraphContentLeft() {
        int border = getGridLabelRenderer().getStyles().padding;
        return border + getGridLabelRenderer().getLabelVerticalWidth() + getGridLabelRenderer().getVerticalAxisTitleWidth();
    }

    public int getGraphContentTop() {
        int border = getGridLabelRenderer().getStyles().padding + getTitleHeight();
        return border;
    }

    public int getGraphContentHeight() {
        int border = getGridLabelRenderer().getStyles().padding;
        int graphheight = getHeight() - (2 * border) - getGridLabelRenderer().getLabelHorizontalHeight() - getTitleHeight();
        graphheight -= getGridLabelRenderer().getHorizontalAxisTitleHeight();
        return graphheight;
    }

    public int getGraphContentWidth() {
        int border = getGridLabelRenderer().getStyles().padding;
        int graphwidth = getWidth() - (2 * border) - getGridLabelRenderer().getLabelVerticalWidth();
        if (mSecondScale != null) {
            graphwidth -= getGridLabelRenderer().getLabelVerticalSecondScaleWidth();
        }
        return graphwidth;
    }

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

    @Override
    public void computeScroll() {
        super.computeScroll();
        mViewport.computeScroll();
    }

    public LegendRenderer getLegendRenderer() {
        return mLegendRenderer;
    }

    public void setLegendRenderer(LegendRenderer mLegendRenderer) {
        this.mLegendRenderer = mLegendRenderer;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public float getTitleTextSize() {
        return mStyles.titleTextSize;
    }

    public void setTitleTextSize(float titleTextSize) {
        mStyles.titleTextSize = titleTextSize;
    }

    public int getTitleColor() {
        return mStyles.titleColor;
    }

    public void setTitleColor(int titleColor) {
        mStyles.titleColor = titleColor;
    }

    public SecondScale getSecondScale() {
        if (mSecondScale == null) {
            mSecondScale = new SecondScale();
        }
        return mSecondScale;
    }
}
