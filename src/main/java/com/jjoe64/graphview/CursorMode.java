package com.jjoe64.graphview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.Series;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jonas on 22/02/2017.
 */

public class CursorMode {
    protected final Paint mPaintLine;
    protected final GraphView mGraphView;
    protected float mPosX;
    protected boolean mCursorVisible;
    protected final Map<BaseSeries, DataPointInterface> mCurrentSelection;

    public CursorMode(GraphView graphView) {
        mGraphView = graphView;
        mPaintLine = new Paint();
        mPaintLine.setColor(Color.argb(128, 180, 180, 180));
        mPaintLine.setStrokeWidth(10f);
        mCurrentSelection = new HashMap<>();
    }

    public void onDown(MotionEvent e) {
        mPosX = e.getX();
        mCursorVisible = true;
        mGraphView.invalidate();
    }

    public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mCursorVisible) {
            mPosX = e2.getX();
            findCurrentDataPoint();
            mGraphView.invalidate();
        }
    }

    public void draw(Canvas canvas) {
        if (mCursorVisible) {
            canvas.drawLine(mPosX, 0, mPosX, canvas.getHeight(), mPaintLine);
        }

        // selection
        for (Map.Entry<BaseSeries, DataPointInterface> entry : mCurrentSelection.entrySet()) {
            entry.getKey().drawSelection(mGraphView, canvas, false, entry.getValue());
        }
    }

    public boolean onUp(MotionEvent event) {
        mCursorVisible = false;
        findCurrentDataPoint();
        mGraphView.invalidate();
        return true;
    }

    private void findCurrentDataPoint() {
        mCurrentSelection.clear();
        for (Series series : mGraphView.getSeries()) {
            if (series instanceof BaseSeries) {
                DataPointInterface p = ((BaseSeries) series).findDataPointAtX(mPosX);
                if (p != null) {
                    mCurrentSelection.put((BaseSeries) series, p);
                }
            }
        }
    }
}
