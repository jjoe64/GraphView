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
package com.jjoe64.graphview.series;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.jjoe64.graphview.GraphView;

import java.util.Iterator;

/**
 * Series that plots the data as points.
 * The points can be different shapes or a
 * complete custom drawing.
 *
 * @author jjoe64
 */
public class PointsGraphSeries<E extends DataPointInterface> extends BaseSeries<E> {
    /**
     * interface to implement a custom
     * drawing for the data points.
     */
    public static interface CustomShape {
        /**
         * called when drawing a single data point.
         * use the x and y coordinates to render your
         * drawing at this point.
         *
         * @param canvas canvas to draw on
         * @param paint internal paint object. this has the correct color.
         *              But you can use your own paint.
         * @param x x-coordinate the point has to be drawn to
         * @param y y-coordinate the point has to be drawn to
         * @param dataPoint the related data point
         */
        void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint);
    }

    /**
     * choose a predefined shape to render for
     * each data point.
     * You can also render a custom drawing via {@link com.jjoe64.graphview.series.PointsGraphSeries.CustomShape}
     */
    public enum Shape {
        /**
         * draws a point / circle
         */
        POINT,

        /**
         * draws a triangle
         */
        TRIANGLE,

        /**
         * draws a rectangle
         */
        RECTANGLE
    }

    /**
     * wrapped styles for this series
     */
    private final class Styles {
        /**
         * this is used for the size of the shape that
         * will be drawn.
         * This is useless if you are using a custom shape.
         */
        float size;

        /**
         * the shape that will be drawn for each point.
         */
        Shape shape;
    }

    /**
     * wrapped styles
     */
    private Styles mStyles;

    /**
     * internal paint object
     */
    private Paint mPaint;

    /**
     * handler to use a custom drawing
     */
    private CustomShape mCustomShape;

    /**
     * creates the series without data
     */
    public PointsGraphSeries() {
        init();
    }

    /**
     * creates the series with data
     *
     * @param data datapoints
     */
    public PointsGraphSeries(E[] data) {
        super(data);
        init();
    }

    /**
     * inits the internal objects
     * set the defaults
     */
    protected void init() {
        mStyles = new Styles();
        mStyles.size = 20f;
        mPaint = new Paint();
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        setShape(Shape.POINT);
    }

    /**
     * plot the data to the viewport
     *
     * @param graphView graphview
     * @param canvas canvas to draw on
     * @param isSecondScale whether it is the second scale
     */
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

        Iterator<E> values = getValues(minX, maxX);

        // draw background
        double lastEndY = 0;
        double lastEndX = 0;

        // draw data
        mPaint.setColor(getColor());

        double diffY = maxY - minY;
        double diffX = maxX - minX;

        float graphHeight = graphView.getGraphContentHeight();
        float graphWidth = graphView.getGraphContentWidth();
        float graphLeft = graphView.getGraphContentLeft();
        float graphTop = graphView.getGraphContentTop();

        lastEndY = 0;
        lastEndX = 0;
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

            // overdraw
            boolean overdraw = false;
            if (x > graphWidth) { // end right
                overdraw = true;
            }
            if (y < 0) { // end bottom
                overdraw = true;
            }
            if (y > graphHeight) { // end top
                overdraw = true;
            }

            float endX = (float) x + (graphLeft + 1);
            float endY = (float) (graphTop - y) + graphHeight;
            registerDataPoint(endX, endY, value);

            // draw data point
            if (!overdraw) {
                if (mCustomShape != null) {
                    mCustomShape.draw(canvas, mPaint, endX, endY, value);
                } else if (mStyles.shape == Shape.POINT) {
                    canvas.drawCircle(endX, endY, mStyles.size, mPaint);
                } else if (mStyles.shape == Shape.RECTANGLE) {
                    canvas.drawRect(endX-mStyles.size, endY-mStyles.size, endX+mStyles.size, endY+mStyles.size, mPaint);
                } else if (mStyles.shape == Shape.TRIANGLE) {
                    Point[] points = new Point[3];
                    points[0] = new Point((int)endX, (int)(endY-getSize()));
                    points[1] = new Point((int)(endX+getSize()), (int)(endY+getSize()*0.67));
                    points[2] = new Point((int)(endX-getSize()), (int)(endY+getSize()*0.67));
                    drawArrows(points, canvas, mPaint);
                }
            }

            i++;
        }

    }

    /**
     * helper to render triangle
     *
     * @param point array with 3 coordinates
     * @param canvas canvas to draw on
     * @param paint paint object
     */
    private void drawArrows(Point[] point, Canvas canvas, Paint paint) {
        float [] points  = new float[8];
        points[0] = point[0].x;
        points[1] = point[0].y;
        points[2] = point[1].x;
        points[3] = point[1].y;
        points[4] = point[2].x;
        points[5] = point[2].y;
        points[6] = point[0].x;
        points[7] = point[0].y;

        canvas.drawVertices(Canvas.VertexMode.TRIANGLES, 8, points, 0, null, 0, null, 0, null, 0, 0, paint);
        Path path = new Path();
        path.moveTo(point[0].x , point[0].y);
        path.lineTo(point[1].x,point[1].y);
        path.lineTo(point[2].x,point[2].y);
        canvas.drawPath(path,paint);
    }

    /**
     * This is used for the size of the shape that
     * will be drawn.
     * This is useless if you are using a custom shape.
     *
     * @return the size of the shape
     */
    public float getSize() {
        return mStyles.size;
    }

    /**
     * This is used for the size of the shape that
     * will be drawn.
     * This is useless if you are using a custom shape.
     *
     * @param radius the size of the shape
     */
    public void setSize(float radius) {
        mStyles.size = radius;
    }

    /**
     * @return the shape that will be drawn for each point
     */
    public Shape getShape() {
        return mStyles.shape;
    }

    /**
     * @param s the shape that will be drawn for each point
     */
    public void setShape(Shape s) {
        mStyles.shape = s;
    }

    /**
     * Use a custom handler to render your own
     * drawing for each data point.
     *
     * @param shape handler to use a custom drawing
     */
    public void setCustomShape(CustomShape shape) {
        mCustomShape = shape;
    }
}
