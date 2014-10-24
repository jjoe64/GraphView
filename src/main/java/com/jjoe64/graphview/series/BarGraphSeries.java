package com.jjoe64.graphview.series;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.jjoe64.graphview.GraphView;

import java.util.Iterator;

/**
 * Created by jonas on 24.10.14.
 */
public class BarGraphSeries<E extends DataPointInterface> extends BaseSeries<E> {
    private Paint mPaint;

    public BarGraphSeries(E[] data) {
        super(data);
        mPaint = new Paint();
    }

    @Override
    public void draw(GraphView graphView, Canvas canvas) {
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

        //TODO mPaint.setStrokeWidth(style.thickness);

        // TODO style: spacing
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

            // TODO overdraw

            // TODO hook for value dependent color
            //if (style.getValueDependentColor() != null) {
            //    paint.setColor(style.getValueDependentColor().get(values[i]));
            //} else {
                mPaint.setColor(getColor());
            //}

            float left = (float)x + contentLeft - offset;
            float top = (contentTop - (float)y) + contentHeight;
            float right = left + colwidth;
            float bottom = (contentTop - (float)y0) + contentHeight - (graphView.getGridLabelRenderer().isHighlightZeroLines()?4:1);

            if (top > bottom) {
                float tmp = top;
                top = bottom + (graphView.getGridLabelRenderer().isHighlightZeroLines()?4:1);
                bottom = tmp;
            }

            canvas.drawRect(left, top, right, bottom, mPaint);

            // TODO -----Set values on top of graph---------
            //if (drawValuesOnTop) {
            //    top -= 4;
            //    if (top<=border) top+=border+4;
            //    paint.setTextAlign(Paint.Align.CENTER);
            //    paint.setColor(valuesOnTopColor );
            //    canvas.drawText(formatLabel(values[i].getY(), false), (left+right)/2, top, paint);
            //}

            i++;
        }
    }
}
