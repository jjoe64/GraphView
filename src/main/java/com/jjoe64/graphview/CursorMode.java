package com.jjoe64.graphview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by jonas on 22/02/2017.
 */

public class CursorMode {
    protected final Paint mPaintLine;
    protected final GraphView mGraphView;
    protected float mPosX;
    protected boolean mVisible;

    public CursorMode(GraphView graphView) {
        mGraphView = graphView;
        mPaintLine = new Paint();
        mPaintLine.setColor(Color.argb(128, 180, 180, 180));
        mPaintLine.setStrokeWidth(10f);
    }

    public void onDown(MotionEvent e) {
        mPosX = e.getX();
        mVisible = true;
        mGraphView.invalidate();
    }

    public void onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    }

    public void draw(Canvas canvas) {
        if (!mVisible) return;

        canvas.drawLine(mPosX, 0, mPosX, canvas.getHeight(), mPaintLine);
    }
}
