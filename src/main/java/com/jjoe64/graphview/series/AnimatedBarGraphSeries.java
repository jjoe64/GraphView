package com.jjoe64.graphview.series;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.jjoe64.graphview.GraphView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author ntcomplete
 */
public class AnimatedBarGraphSeries<E extends DataPointInterface> extends BarGraphSeries<E> implements AnimatedGraphInterface {


    public enum BarAnimationStyle {
        ALL_AT_ONCE,
        BAR_AT_A_TIME
    }

    private BarAnimationStyle mAnimationStyle = BarAnimationStyle.ALL_AT_ONCE;

    protected boolean mRequiresRedraw = true;

    protected int mAnimateBar = 0;
    protected float mMaxGraphY = 0;
    protected double mHighestYValue = Float.MIN_VALUE;
    
    protected boolean mAddingElement = false;


    public AnimatedBarGraphSeries(E[] data) {
        super(data);
    }



    @Override
    public boolean requiresRedraw() {
        return mRequiresRedraw;
    }


    /**
     * Adds the data point, and sets the variables to the correct points.
     * @param data : DataPointInterface to add to the Series
     * @return : ignored.
     */
    @Override
    public double appendData(E data) {
        mAnimationStyle = BarAnimationStyle.BAR_AT_A_TIME;
        
        int preAddIndex = getIndexOfDataX(data);
        mMaxGraphY = (float) super.appendData(data);
        mAnimateBar = getIndexOfDataX(data);
        mAddingElement = true;
        mRequiresRedraw = true;
        return 0;
    }

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



            Iterator<E> values = getValues(minX, maxX);

            List<DataPointInterface> dataArrayList = new ArrayList<DataPointInterface>();

            // this works only if the data has no "hole" and if the interval is always the same
            int numOfBars = 0;
            int i = 0;
            double currentPointHighest = 0;

        if(mAddingElement) {
            while (values.hasNext()) {

                DataPoint dataPoint = (DataPoint) values.next();
                if (i == mAnimateBar && mRequiresRedraw) {
                    mMaxGraphY = mMaxGraphY + 100;
                    currentPointHighest = dataPoint.getY();
                    dataPoint = new DataPoint(dataPoint.getX(), Math.min(mMaxGraphY, currentPointHighest));

                    if(graphView.getViewport().getMaxY(false) < mMaxGraphY) {
                        maxY = Math.min(mMaxGraphY, currentPointHighest);
                        graphView.getViewport().setMaxY(maxY);
                        graphView.onDataChanged(true, false);
                    }
                }

                dataArrayList.add(dataPoint);
                i++;
                numOfBars++;
                
            }
            if (numOfBars == 0) {
                return;
            }
            if (mMaxGraphY < currentPointHighest) {
                values = getValuesFromData(minX, maxX, (List<E>) dataArrayList);
            } else {
                values = getValues(minX, maxX);
                mRequiresRedraw = false;
            }

        } else if(mAnimationStyle == BarAnimationStyle.ALL_AT_ONCE) {
            while (values.hasNext()) {

                DataPoint dataPoint = (DataPoint) values.next();
                mHighestYValue = Math.max(mHighestYValue, dataPoint.getY());
                double newY = Math.min(mMaxGraphY, dataPoint.getY());

                dataPoint = new DataPoint(dataPoint.getX(), newY);
                dataArrayList.add(dataPoint);
                i++;
                numOfBars++;
            }
            if (numOfBars == 0) {
                return;
            }
            mMaxGraphY += (mHighestYValue - minY) / 60.0;
            mMaxGraphY = (float) Math.min(mMaxGraphY, mHighestYValue);

            values = getValuesFromData(minX, maxX, (List<E>) dataArrayList);

        } else if(mAnimationStyle == BarAnimationStyle.BAR_AT_A_TIME) {
            while (values.hasNext()) {

                DataPoint dataPoint = (DataPoint) values.next();
                if (i <= mAnimateBar) {
                    if (i == mAnimateBar) {
                        mMaxGraphY += (dataPoint.getY() / 8);
                        currentPointHighest = dataPoint.getY();
                        dataPoint = new DataPoint(dataPoint.getX(), mMaxGraphY);
                    }

                    dataArrayList.add(dataPoint);
                }
                i++;
                numOfBars++;
            }
            if (numOfBars == 0) {
                return;
            }
            if (mMaxGraphY >= currentPointHighest) {
                mMaxGraphY = 0;
                mAnimateBar = Math.min(mAnimateBar+1, numOfBars + 1);
            }
            if (mAnimateBar < numOfBars + 1) {
                values = getValuesFromData(minX, maxX, (List<E>) dataArrayList);
            } else {
                values = getValues(minX, maxX);
                mRequiresRedraw = false;
            }

        }

        drawColumns(graphView, canvas, maxX, minX, maxY, minY, values, numOfBars);
    }


    private void drawColumns(GraphView graphView, Canvas canvas, double maxX, double minX, double maxY, double minY, Iterator<E> values, int numOfBars) {
        float colwidth = graphView.getGraphContentWidth() / (numOfBars-1);
//        Log.d("BarGraphSeries", "numBars=" + numOfBars);

        float spacing = Math.min(colwidth*mSpacing/100, colwidth*0.98f);
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


}
