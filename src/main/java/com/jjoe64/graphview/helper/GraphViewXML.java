package com.jjoe64.graphview.helper;

import android.content.Context;
import android.util.AttributeSet;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * Created by jonas on 18.11.14.
 */
public class GraphViewXML extends GraphView {
    public GraphViewXML(Context context, AttributeSet attrs) {
        super(context, attrs);

        addSeries(new LineGraphSeries(new DataPointInterface[] {
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 8)
        }));
    }
}
