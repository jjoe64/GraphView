package com.jjoe64.graphview.series;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.jjoe64.graphview.GraphView;

import java.util.Iterator;
import java.util.List;

/**
 * Created by jonas on 13.08.14.
 */
public class LineGraphSeries<E extends DataPointInterface> extends BaseSeries<E> {
    private final class Styles {
        private int thickness = 5;
        private boolean drawBackground = false;
        private boolean drawDataPoints = false;
        private float dataPointsRadius = 10f;
        private int backgroundColor = Color.argb(100, 172, 218, 255);
    }
    
    private Styles mStyles;
    private Paint mPaint;
    private Paint mPaintBackground;
    private Path mPath;

    public LineGraphSeries(E[] data) {
        super(data);

        mStyles = new Styles();
        mPaint = new Paint();
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaintBackground = new Paint();

        mPath = new Path();
    }

    @Override
    public void draw(GraphView graphView, Canvas canvas) {
        // get data
        double maxX = graphView.getViewport().getMaxX(false);
        double minX = graphView.getViewport().getMinX(false);
        double maxY = graphView.getViewport().getMaxY(false);
        double minY = graphView.getViewport().getMinY(false);
        Iterator<E> values = getValues(minX, maxX);

        // draw background
        double lastEndY = 0;
        double lastEndX = 0;

        // draw data
        mPaint.setStrokeWidth(mStyles.thickness);
        mPaint.setColor(getColor());
        mPaintBackground.setColor(mStyles.backgroundColor);

        if (mStyles.drawBackground) {
            mPath.reset();
        }

        double diffY = maxY - minY;
        double diffX = maxX - minX;

        // TODO use getContentWidth etc.
        float border = graphView.getGridLabelRenderer().getStyles().padding;
        float graphheight = graphView.getHeight() - (2 * border) - graphView.getGridLabelRenderer().getLabelHorizontalHeight();
        float graphwidth = graphView.getWidth() - (2 * border) - graphView.getGridLabelRenderer().getLabelVerticalWidth();
        float horstart = border + graphView.getGridLabelRenderer().getLabelVerticalWidth();

        lastEndY = 0;
        lastEndX = 0;
        double lastUsedEndX = 0;
        float firstX = 0;
        int i=0;
        while (values.hasNext()) {
            E value = values.next();

            double valY = value.getY() - minY;
            double ratY = valY / diffY;
            double y = graphheight * ratY;

            double valX = value.getX() - minX;
            double ratX = valX / diffX;
            double x = graphwidth * ratX;

            double orgX = x;
            double orgY = y;

            if (i > 0) {
                // overdraw
                if (x > graphwidth) { // end right
                    double b = ((graphwidth - lastEndX) * (y - lastEndY)/(x - lastEndX));
                    y = lastEndY+b;
                    x = graphwidth;
                }
                if (y < 0) { // end bottom
                    double b = ((0 - lastEndY) * (x - lastEndX)/(y - lastEndY));
                    x = lastEndX+b;
                    y = 0;
                }
                if (y > graphheight) { // end top
                    double b = ((graphheight - lastEndY) * (x - lastEndX)/(y - lastEndY));
                    x = lastEndX+b;
                    y = graphheight;
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
                if (lastEndY > graphheight) { // start top
                    double b = ((graphheight - y) * (x - lastEndX)/(lastEndY - y));
                    lastEndX = x-b;
                    lastEndY = graphheight;
                }

                float startX = (float) lastEndX + (horstart + 1);
                float startY = (float) (border - lastEndY) + graphheight;
                float endX = (float) x + (horstart + 1);
                float endY = (float) (border - y) + graphheight;

                // draw data point
                if (mStyles.drawDataPoints) {
                    //fix: last value was not drawn. Draw here now the end values
                    canvas.drawCircle(endX, endY, mStyles.dataPointsRadius, mPaint);
                }

                canvas.drawLine(startX, startY, endX, endY, mPaint);
                if (mStyles.drawBackground) {
                    if (i==1) {
                        firstX = startX;
                        mPath.moveTo(startX, startY);
                    }
                    mPath.lineTo(endX, endY);
                }
                lastUsedEndX = endX;
            } else if (mStyles.drawDataPoints) {
                //fix: last value not drawn as datapoint. Draw first point here, and then on every step the end values (above)
                float first_X = (float) x + (horstart + 1);
                float first_Y = (float) (border - y) + graphheight;
                //TODO canvas.drawCircle(first_X, first_Y, dataPointsRadius, mPaint);
            }
            lastEndY = orgY;
            lastEndX = orgX;
            i++;
        }

        if (mStyles.drawBackground) {
            // end / close path
            mPath.lineTo((float) lastUsedEndX, graphheight + border);
            mPath.lineTo(firstX, graphheight + border);
            mPath.close();
            canvas.drawPath(mPath, mPaintBackground);
        }

    }

    public int getThickness() {
        return mStyles.thickness;
    }

    public void setThickness(int thickness) {
        mStyles.thickness = thickness;
    }

    public boolean isDrawBackground() {
        return mStyles.drawBackground;
    }

    public void setDrawBackground(boolean drawBackground) {
        mStyles.drawBackground = drawBackground;
    }

    public boolean isDrawDataPoints() {
        return mStyles.drawDataPoints;
    }

    public void setDrawDataPoints(boolean drawDataPoints) {
        mStyles.drawDataPoints = drawDataPoints;
    }

    public float getDataPointsRadius() {
        return mStyles.dataPointsRadius;
    }

    public void setDataPointsRadius(float dataPointsRadius) {
        mStyles.dataPointsRadius = dataPointsRadius;
    }

    public int getBackgroundColor() {
        return mStyles.backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        mStyles.backgroundColor = backgroundColor;
    }
}
