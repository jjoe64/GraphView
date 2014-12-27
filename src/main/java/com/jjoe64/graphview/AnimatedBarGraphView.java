package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * @author nick
 */
public class AnimatedBarGraphView extends BarGraphView {

    private float mMaxGraphY = 0;

    public AnimatedBarGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatedBarGraphView(Context context, String title) {
        super(context, title);
    }

    @Override
    public void drawSeries(Canvas canvas, GraphViewDataInterface[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart, GraphViewSeries.GraphViewSeriesStyle style) {
        double max = 0;

        for(int i = 0; i < values.length; i++) {
            GraphViewDataInterface value = values[i];
            if(value.getY() > mMaxGraphY) {
                values[i] = new GraphViewData(value.getX(), mMaxGraphY);
            }
            max = Math.max(max, value.getY());
        }
        mMaxGraphY = (float) Math.min(mMaxGraphY+600, max);


        super.drawSeries(canvas, values, graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart, style);

    }

}
