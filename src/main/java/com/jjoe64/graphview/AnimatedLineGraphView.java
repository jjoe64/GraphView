package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

/**
 * @author nick
 */
public class AnimatedLineGraphView extends LineGraphView {

    private float mMaxGraphX = 0;
    private double mMaxChange = 0;


    public AnimatedLineGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatedLineGraphView(Context context, String title) {
        super(context, title);
    }


    @Override
    public void drawSeries(Canvas canvas, GraphViewDataInterface[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart, GraphViewSeries.GraphViewSeriesStyle style) {
//        GraphViewDataInterface[] subValues = new GraphViewDataInterface[(int) (mMaxGraphX+1)];
//        for(int i = 0; i <= mMaxGraphX; i++) {
//            subValues[i] = values[i];
//        }
        if(mMaxChange == 0.0) {

//            float maxx = Float.MIN_VALUE;
//            for (GraphViewDataInterface value : values) {
//                maxx = (float) Math.max(maxx, value.getX());
//            }

            mMaxChange = (diffX/100);
        }

        mMaxGraphX = (float) Math.min(mMaxGraphX+mMaxChange, diffX);

        int first = 0, sec = 0;
        for(int i = 1; i < values.length; i++) {
            if(mMaxGraphX >= values[i-1].getX() && mMaxGraphX <= values[i].getX()) {
                first = i-1;
                sec = i;
            }
        }

        GraphViewDataInterface[] subValues = new GraphViewDataInterface[values.length];

        double xchange = values[sec].getX() - values[first].getX();
        double ychange = values[sec].getY() - values[first].getY();
        double xfrac = (mMaxGraphX-values[first].getX())/xchange;

        for(int i = 0; i < values.length; i++) {
            GraphViewDataInterface value = values[i];
            if(i <= first) {
                subValues[i] = value;
            } else {
                subValues[i] = new GraphViewData(mMaxGraphX, values[first].getY()+(xfrac*ychange));
            }
        }

        super.drawSeries(canvas, subValues, graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart, style);
    }
}
