package com.jjoe64.graphview.series;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;

import java.util.Iterator;

/**
 * Created by jonas on 24.10.14.
 */
public class BarGraphSeries<E extends DataPointInterface> extends BaseSeries<E> {
    private Paint mPaint;
    private int mSpacing;
    private ValueDependentColor<E> mValueDependentColor;
    private boolean mDrawValuesOnTop;
    private int mValuesOnTopColor;
    private float mValuesOnTopSize;

    public BarGraphSeries(E[] data) {
        super(data);
        mPaint = new Paint();
    }

    @Override
    public void draw(GraphView graphView, Canvas canvas) {
        mPaint.setTextAlign(Paint.Align.CENTER);
        if (mValuesOnTopSize == 0) {
            mValuesOnTopSize = graphView.getGridLabelRenderer().getTextSize();
        }
        mPaint.setTextSize(mValuesOnTopSize);

        // get data
        double maxX = graphView.getViewport().getMaxX(false);
        double minX = graphView.getViewport().getMinX(false);
        double maxY = graphView.getViewport().getMaxY(false);
        double minY = graphView.getViewport().getMinY(false);
        Iterator<E> values = getValues(minX, maxX);

        // this works only if the data has no "hole" and if the interval is always the same
        // TODO do a check
        int numOfBars = 0;
        while (values.hasNext()) {
            values.next();
            numOfBars++;
        }
        if (numOfBars == 0) {
            return;
        }

        values = getValues(minX, maxX);

        float colwidth = graphView.getGraphContentWidth() / (numOfBars-1);
        Log.d("BarGraphSeries", "numBars=" + numOfBars);

        float spacing = Math.min((float) colwidth*mSpacing/100, colwidth*0.98f);
        float offset = colwidth/2;

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

            float left = (float)x + contentLeft - offset + spacing/2;
            float top = (contentTop - (float)y) + contentHeight;
            float right = left + colwidth - spacing;
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

    public ValueDependentColor<E> getValueDependentColor() {
        return mValueDependentColor;
    }

    public void setValueDependentColor(ValueDependentColor<E> mValueDependentColor) {
        this.mValueDependentColor = mValueDependentColor;
    }

    public int getSpacing() {
        return mSpacing;
    }

    /**
     * 0-100
     * @param mSpacing
     */
    public void setSpacing(int mSpacing) {
        this.mSpacing = mSpacing;
    }

    public boolean isDrawValuesOnTop() {
        return mDrawValuesOnTop;
    }

    public void setDrawValuesOnTop(boolean mDrawValuesOnTop) {
        this.mDrawValuesOnTop = mDrawValuesOnTop;
    }

    public int getValuesOnTopColor() {
        return mValuesOnTopColor;
    }

    public void setValuesOnTopColor(int mValuesOnTopColor) {
        this.mValuesOnTopColor = mValuesOnTopColor;
    }

    public float getValuesOnTopSize() {
        return mValuesOnTopSize;
    }

    public void setValuesOnTopSize(float mValuesOnTopSize) {
        this.mValuesOnTopSize = mValuesOnTopSize;
    }
}
