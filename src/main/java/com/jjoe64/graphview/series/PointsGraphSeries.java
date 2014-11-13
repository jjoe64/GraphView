package com.jjoe64.graphview.series;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.jjoe64.graphview.GraphView;

import java.util.Iterator;

/**
 * Created by jonas on 13.11.14.
 */
public class PointsGraphSeries<E extends DataPointInterface> extends BaseSeries<E> {
    public static interface CustomShape {
        void draw(Canvas canvas, Paint paint, float x, float y);
    }

    public enum Shape {
        POINT, TRIANGLE, RECTANGLE
    }

    private final class Styles {
        float size;
        Shape shape;
    }

    private Styles mStyles;
    private Paint mPaint;
    private CustomShape mCustomShape;

    public PointsGraphSeries(E[] data) {
        super(data);

        mStyles = new Styles();
        mStyles.size = 20f;
        mPaint = new Paint();
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        setShape(Shape.POINT);
    }

    @Override
    public void draw(GraphView graphView, Canvas canvas, boolean isSecondScale) {
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

            // draw data point
            if (!overdraw) {
                if (mCustomShape != null) {
                    mCustomShape.draw(canvas, mPaint, endX, endY);
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


    public float getSize() {
        return mStyles.size;
    }

    public void setSize(float radius) {
        mStyles.size = radius;
    }

    public Shape getShape() {
        return mStyles.shape;
    }

    public void setShape(Shape s) {
        mStyles.shape = s;
    }

    public void setCustomShape(CustomShape shape) {
        mCustomShape = shape;
    }
}
