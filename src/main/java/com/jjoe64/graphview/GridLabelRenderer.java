package com.jjoe64.graphview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jonas on 13.08.14.
 */
public class GridLabelRenderer {
    private final class Styles {
        float textSize;
        Paint.Align verticalLabelsAlign;
        int verticalLabelsColor;
    }

    protected Styles mStyles;
    private final GraphView mGraphView;
    private Map<Integer, Double> mStepsVertical;
    private Map<Double, String> mVerticalLabels;
    private Paint mPaintLine;
    private Paint mPaintLabel;
    private boolean mIsAdjusted;
    private Integer mLabelVerticalWidth;
    private Integer mLabelVerticalHeight;
    private LabelFormatter mLabelFormatter;

    public GridLabelRenderer(GraphView graphView) {
        mGraphView = graphView;
        mLabelFormatter = new DefaultLabelFormatter(graphView.getViewport());
        mStyles = new Styles();
        resetStyles();
    }

    public void resetStyles() {
        mStyles.textSize = 20;
        mStyles.verticalLabelsAlign = Paint.Align.RIGHT;
        mStyles.verticalLabelsColor = Color.RED;

        reloadStyles();
    }

    public void reloadStyles() {
        mPaintLine = new Paint();
        mPaintLine.setColor(Color.RED);
        mPaintLine.setStrokeWidth(2);

        mPaintLabel = new Paint();
        mPaintLabel.setTextAlign(getVerticalLabelsAlign());
        mPaintLabel.setTextSize(getTextSize());
    }

    public float getTextSize() { return mStyles.textSize; }
    public int getVerticalLabelsColor() { return mStyles.verticalLabelsColor; }
    public Paint.Align getVerticalLabelsAlign() { return mStyles.verticalLabelsAlign; }

    public void invalide() {
        mIsAdjusted = false;
        mLabelVerticalWidth = null;
        mLabelVerticalHeight = null;
    }

    /**
     * adjusts the grid and labels to match to the data
     */
    protected void adjust() {
        // get min/max of current viewport
        double minX = mGraphView.getViewport().getMinX();
        double maxX = mGraphView.getViewport().getMaxX();
        double minY = mGraphView.getViewport().getMinY();
        double maxY = mGraphView.getViewport().getMaxY();

        // find the number of labels
        int numVerticalLabels = 5;

        // find good steps
        // TODO Math.abs for negative numbers?
        boolean adjusting = true;
        double newMinY = minY;
        double exactSteps = 0d;
        while (adjusting) {
            double rangeY = maxY - newMinY;
            exactSteps = rangeY / (numVerticalLabels - 1);
            exactSteps = humanRound(exactSteps);

            // adjust viewport
            // wie oft passt STEP in minY rein?
            int count = 0;
            if (newMinY >= 0d) {
                // positive number
                while (newMinY - exactSteps >= 0) {
                    newMinY -= exactSteps;
                    count++;
                }
                newMinY = exactSteps * count;
            } else {
                // negative number
                count++;
                while (newMinY + exactSteps < 0) {
                    newMinY += exactSteps;
                    count++;
                }
                newMinY = exactSteps * count * -1;
            }

            // wenn minY sich geändert hat, steps nochmal berechnen
            // wenn nicht, fertig
            if (newMinY == minY) {
                adjusting = false;
            } else {
                minY = newMinY;
            }
        }

        double newMaxY = newMinY + (numVerticalLabels-1)*exactSteps;
        mGraphView.getViewport().setMinY(newMinY);
        mGraphView.getViewport().setMaxY(newMaxY);

        mStepsVertical = new LinkedHashMap<Integer, Double>(numVerticalLabels);
        int height = mGraphView.getHeight();
        double v = newMaxY;
        int p = 0;
        int pixelStep = height/(numVerticalLabels-1);
        for (int i = 0; i < numVerticalLabels; i++) {
            mStepsVertical.put(p, v);
            p += pixelStep;
            v -= exactSteps;
        }

        mIsAdjusted = true;
    }

    protected void calcLabelVerticalSize(Canvas canvas) {
        // test label
        double testY = ((mGraphView.getViewport().getMaxY()-mGraphView.getViewport().getMinY())*0.783)+mGraphView.getViewport().getMinY();
        String testLabel = mLabelFormatter.formatLabel(testY, false);
        Rect textBounds = new Rect();
        mPaintLabel.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
        mLabelVerticalWidth = textBounds.width();
        mLabelVerticalHeight = textBounds.height();
    }

    public void draw(Canvas canvas) {
        if (!mIsAdjusted) {
            adjust();
        }
        if (mLabelVerticalWidth == null) {
            calcLabelVerticalSize(canvas);
        }

        // draw vertical steps (horizontal lines and vertical labels)
        float marginLeft = 20;
        mPaintLabel.setColor(getVerticalLabelsColor());
        for (Map.Entry<Integer, Double> e : mStepsVertical.entrySet()) {
            // draw line
            canvas.drawLine(marginLeft, e.getKey(), canvas.getWidth(), e.getKey(), mPaintLine);

            // draw label
            int labelsWidth = mLabelVerticalWidth;
            int labelsOffset = 0;
            if (getVerticalLabelsAlign() == Paint.Align.RIGHT) {
                labelsOffset = labelsWidth;
            } else if (getVerticalLabelsAlign() == Paint.Align.CENTER) {
                labelsOffset = labelsWidth / 2;
            }

            float y = e.getKey();

            String[] lines = mLabelFormatter.formatLabel(e.getValue(), false).split("\n");
            for (int li=0; li<lines.length; li++) {
                // for the last line y = height
                float y2 = y - (lines.length-li-1)*getTextSize()*1.1f;
                canvas.drawText(lines[li], labelsOffset, y2, mPaintLabel);
            }
        }
    }

    protected double humanRound(double in) {
        // round-up to 1-steps, 2-steps or 5-steps
        int ten = 0;
        while (in >= 10d) {
            in /= 10d;
            ten++;
        }
        while (in < 1d) {
            in *= 10d;
            ten--;
        }
        if (in == 1d) {
        } else if (in <= 2d) {
            in = 2d;
        } else if (in <= 5d) {
            in = 5d;
        } else if (in < 10d) {
            in = 10d;
        }
        return in*Math.pow(10d, ten);
    }
}
