package com.jjoe64.graphview.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jjoe64.graphview.FloatLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private GraphView mGraphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGraphView = (GraphView)findViewById(R.id.graphView);
        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.getViewport().setYAxisBoundsManual(true);
        mGraphView.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                return value + "";
            }

            @Override
            public void setViewport(Viewport viewport) {
            }
        });
        mGraphView.getGridLabelRenderer().setHighlightZeroLines(false);
        mGraphView.getGridLabelRenderer().reloadStyles();

        ArrayList<SimplePoint> values = new ArrayList<>();
        for(int i = 0; i < 50; i++){
            values.add(new SimplePoint(i, Math.random() * 100));
        }

        mGraphView.getViewport().setMinX(0);
        mGraphView.getViewport().setMaxX(values.size() - 1);
        mGraphView.getViewport().setMinY(0);
        mGraphView.getViewport().setMaxY(120);

        LineGraphSeries<SimplePoint> dataSet = new LineGraphSeries<>(values);
        dataSet.setTitle("Test");
        dataSet.setColor(Color.GRAY);
        dataSet.setBackgroundColor(Color.argb(150, Color.red(Color.GRAY), Color.green(Color.GRAY), Color.blue(Color.GRAY)));
        dataSet.setDrawBackground(true);
        dataSet.setThickness(2);

        dataSet.setFloatLabelFormatter(new FloatLabelFormatter<SimplePoint>() {
            @Override
            public String formatFloatLabel(SimplePoint point) {
                return "X: " + point.getX() + "\nY: " + point.getY();
            }
        });

        mGraphView.addSeries(dataSet);
    }

    private class SimplePoint implements DataPointInterface {
        private double mX, mY;

        public SimplePoint(double x, double y){
            mX = x;
            mY = y;
        }

        @Override
        public double getX() {
            return mX;
        }

        @Override
        public double getY() {
            return mY;
        }
    }
}
