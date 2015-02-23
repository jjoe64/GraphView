package com.jjoe64.graphview.series;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.jjoe64.graphview.GraphView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author nick
 */
public class AnimatedLineGraphSeries<E extends DataPointInterface> extends LineGraphSeries<E> implements AnimatedGraphInterface
{


    protected boolean mRequiresRedraw = true;
    private double mMaxChange = 0.0;
    private float mMaxGraphX = 0.0f;
    private float mMaxGraphY = 0.0f;

    
    private float mIncreasingPointY = 0;
    private boolean mIncreasingPointBool = false;
    private int mIncreasingPointIndex = -1;

    private LineAnimationType mLineAnimationType = LineAnimationType.VERTICAL_ANIMATION;

    public enum LineAnimationType {
        HORIZONTAL_ANIMATION,
        VERTICAL_ANIMATION
    }


    public AnimatedLineGraphSeries(E[] data) {
        super(data);
    }


    @Override
    public boolean requiresRedraw() {
        return mRequiresRedraw;
    }

    public void setLineAnimationType(LineAnimationType type) {
        mLineAnimationType = type;
    }

    @Override
    public double appendData(E data) {
        mIncreasingPointIndex = getIndexOfDataX(data);
        mMaxGraphX = (float) getHighestValueX();

        mIncreasingPointY = (float) super.appendData(data);
        
        mMaxGraphY = (float) (mIncreasingPointY + data.getY());
        mIncreasingPointBool = mIncreasingPointIndex != -1;
        if(!mIncreasingPointBool) {
            mLineAnimationType = LineAnimationType.HORIZONTAL_ANIMATION;
            mMaxChange = ((data.getX() - mMaxGraphX) / 100.0);
        } else {

            mMaxChange = 0;
        }
        mRequiresRedraw = true;
        return mMaxGraphY;
    }
    

    @Override
    public void draw(GraphView graphView, Canvas canvas, boolean isSecondScale) {
        resetDataPoints();

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



        // draw data
        mPaint.setStrokeWidth(mStyles.thickness);
        mPaint.setColor(getColor());
        mPaintBackground.setColor(mStyles.backgroundColor);

        Paint paint;
        if (mCustomPaint != null) {
            paint = mCustomPaint;
        } else {
            paint = mPaint;
        }

        if (mStyles.drawBackground) {
            mPathBackground.reset();
        }

        double diffY = maxY - minY;
        double diffX = maxX - minX;


        Iterator<E> values = getValues(minX, maxX);
        ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
        int i, first = 0, sec = 0;
        DataPoint dataPoint;

        double lowestX = Double.MAX_VALUE, highestX = Double.MIN_VALUE, highestY = Double.MIN_VALUE, lowestY = Double.MAX_VALUE;
        while(values.hasNext()) {
            dataPoint = (DataPoint) values.next();
            dataPoints.add(dataPoint);
            lowestX = Math.min(dataPoint.getX(), lowestX);
            highestX = Math.max(dataPoint.getX(), highestX);
            highestY = Math.max(dataPoint.getY(), highestY);
            lowestY = Math.min(dataPoint.getY(), lowestY);

        }


        if (!mRequiresRedraw) {
            drawLines(graphView, canvas, minX, minY, getValues(minX, maxX), paint, diffY, diffX);
            return;
        }

        if (mIncreasingPointBool) {
            if(mIncreasingPointY + mMaxChange > mMaxGraphY) { // || mIncreasingPointY + mMaxChange >= maxY) {
                drawLines(graphView, canvas, minX, minY, getValues(minX, maxX), paint, diffY, diffX);
                mRequiresRedraw = false;
                mIncreasingPointBool = false;

            } else {
                ArrayList<DataPoint> newDataPoints = new ArrayList<DataPoint>();
                for (i = 0; i < dataPoints.size(); i++) {
                    dataPoint = dataPoints.get(i);

                    if(i == mIncreasingPointIndex) {
//                        double y = Math.min(mIncreasingPointY + mMaxChange, maxY);
                        double y = mIncreasingPointY + mMaxChange;
                        if(graphView.getViewport().getMaxY(false) < y) {
                            graphView.getViewport().setMaxY(y);
                            diffY = y - minY;
                            graphView.onDataChanged(true, false);
                        }
                        newDataPoints.add(i, new DataPoint(dataPoint.getX(), y));
                    } else {
                        newDataPoints.add(i, dataPoint);
                    }
                }


                mMaxChange += ((mMaxGraphY - mIncreasingPointY) / 10.0);

                Iterator<E> newValues = getValuesFromData(minX, maxX, (List<E>) newDataPoints);

                drawLines(graphView, canvas, minX, minY, newValues, paint, diffY, diffX);
            }
        } else if (mLineAnimationType == LineAnimationType.VERTICAL_ANIMATION) {

            if(mMaxChange > highestY) {
                drawLines(graphView, canvas, minX, minY, getValues(minX, maxX), paint, diffY, diffX);
                mRequiresRedraw = false;

            } else {
                ArrayList<DataPoint> newDataPoints = new ArrayList<DataPoint>();
                for (i = 0; i < dataPoints.size(); i++) {
                    dataPoint = dataPoints.get(i);
                    newDataPoints.add(i, new DataPoint(dataPoint.getX(), (dataPoint.getY() - highestY) + mMaxChange));

                }


                mMaxChange += (highestY / 100.0);

                Iterator<E> newValues = getValuesFromData(minX, maxX, (List<E>) newDataPoints);

                drawLines(graphView, canvas, minX, minY, newValues, paint, diffY, diffX);
            }
        } else {

            if (mMaxChange == 0.0) {

//            float maxx = Float.MIN_VALUE;
//            for (GraphViewDataInterface value : values) {
//                maxx = (float) Math.max(maxx, value.getX());
//            }

                mMaxChange = ((highestX - lowestX) / 100);
                mMaxGraphX = (float) lowestX;
            }

            mMaxGraphX = (float) Math.min(mMaxGraphX + mMaxChange, highestX);

            if (mMaxGraphX == highestX) {
                mRequiresRedraw = false;
            }

            if (!mRequiresRedraw) {
                drawLines(graphView, canvas, minX, minY, getValues(minX, maxX), paint, diffY, diffX);
                return;
            }


            for (i = 1; i < dataPoints.size(); i++) {
                dataPoint = dataPoints.get(i);
                if (mMaxGraphX >= dataPoints.get(i - 1).getX() && mMaxGraphX <= dataPoint.getX()) {
                    first = i - 1;
                    sec = i;
                }

            }


            double xchange = dataPoints.get(sec).getX() - dataPoints.get(first).getX();
            double ychange = dataPoints.get(sec).getY() - dataPoints.get(first).getY();
            double xfrac = (mMaxGraphX - dataPoints.get(first).getX()) / xchange;

            ArrayList<DataPoint> subValues = new ArrayList<DataPoint>();

            for (i = 0; i < dataPoints.size(); i++) {
                DataPoint value = dataPoints.get(i);
                if (i <= first || sec == first) {
                    subValues.add(value);
                } else {
                    subValues.add(new DataPoint(mMaxGraphX, dataPoints.get(first).getY() + (xfrac * ychange)));
                }
            }

            Iterator<E> newValues = getValuesFromData(minX, maxX, (List<E>) subValues);

            drawLines(graphView, canvas, minX, minY, newValues, paint, diffY, diffX);
        }

    }

    private void drawLines(GraphView graphView, Canvas canvas, double minX, double minY, Iterator<E> values, Paint paint, double diffY, double diffX) {

        // draw background
        double lastEndY = 0;
        double lastEndX = 0;
        float graphHeight = graphView.getGraphContentHeight();
        float graphWidth = graphView.getGraphContentWidth();
        float graphLeft = graphView.getGraphContentLeft();
        float graphTop = graphView.getGraphContentTop();


        double lastUsedEndX = 0;
        float firstX = 0;
        int i=0;
        while (values.hasNext()) {
            E value = values.next();

            double valY = value.getY() - minY;
            double ratY = valY / diffY;
            double y = graphHeight * ratY;

            double valX = value.getX() - minX;
            double ratX = valX / diffX;
            double x = graphWidth * ratX;

            double orgX = x;
            double orgY = y;

            if (i > 0) {
                // overdraw
                if (x > graphWidth) { // end right
                    double b = ((graphWidth - lastEndX) * (y - lastEndY)/(x - lastEndX));
                    y = lastEndY+b;
                    x = graphWidth;
                }
                if (y < 0) { // end bottom
                    double b = ((0 - lastEndY) * (x - lastEndX)/(y - lastEndY));
                    if(b != b) {
                        b = 0;
                    }
                    x = lastEndX+b;
                    y = 0;
                }
                if (y > graphHeight) { // end top
                    double b = ((graphHeight - lastEndY) * (x - lastEndX)/(y - lastEndY));
                    x = lastEndX+b;
                    y = graphHeight;
                }
                if (lastEndY < 0) { // start bottom
                    double b = ((0 - y) * (x - lastEndX)/(lastEndY - y));
                    lastEndX = x-b;
                    lastEndY = 0;
                }
                if (lastEndX < 0) { // start left
                    double b = ((0 - x) * (y - lastEndY)/(lastEndX - x));
                    lastEndY = y-b;
                    lastEndX = 0;
                }
                if (lastEndY > graphHeight) { // start top
                    double b = ((graphHeight - y) * (x - lastEndX)/(lastEndY - y));
                    lastEndX = x-b;
                    lastEndY = graphHeight;
                }

                float startX = (float) lastEndX + (graphLeft + 1);
                float startY = (float) (graphTop - lastEndY) + graphHeight;
                float endX = (float) x + (graphLeft + 1);
                float endY = (float) (graphTop - y) + graphHeight;

                // draw data point
                if (mStyles.drawDataPoints) {
                    //fix: last value was not drawn. Draw here now the end values
                    canvas.drawCircle(endX, endY, mStyles.dataPointsRadius, mPaint);
                }
                registerDataPoint(endX, endY, value);

                mPath.reset();
                mPath.moveTo(startX, startY);
                mPath.lineTo(endX, endY);
                canvas.drawPath(mPath, paint);
                if (mStyles.drawBackground) {
                    if (i==1) {
                        firstX = startX;
                        mPathBackground.moveTo(startX, Math.max(0, startY));
                    }
                    mPathBackground.lineTo(endX, Math.max(0, endY));
                }
                lastUsedEndX = endX;
            } else if (mStyles.drawDataPoints) {
                //fix: last value not drawn as datapoint. Draw first point here, and then on every step the end values (above)
                float first_X = (float) x + (graphLeft + 1);
                float first_Y = (float) (graphTop - y) + graphHeight;
                //TODO canvas.drawCircle(first_X, first_Y, dataPointsRadius, mPaint);
            }
            lastEndY = orgY;
            lastEndX = orgX;
            i++;
        }

        if (mStyles.drawBackground) {
            // end / close path
            mPathBackground.lineTo((float) lastUsedEndX, Math.max(0, graphHeight + graphTop));

            mPathBackground.lineTo(firstX, Math.max(0, graphHeight + graphTop));

            mPathBackground.close();
            canvas.drawPath(mPathBackground, mPaintBackground);
        }
    }
}
