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
    public final class Styles {
        public float textSize;
        public Paint.Align verticalLabelsAlign;
        public int verticalLabelsColor;
        public int horizontalLabelsColor;
        public int gridColor;
        public boolean highlightZeroLines;
        public int padding;
    }

    protected Styles mStyles;
    private final GraphView mGraphView;
    private Map<Integer, Double> mStepsVertical;
    private Map<Integer, Double> mStepsHorizontal;
    private Map<Double, String> mVerticalLabels;
    private Paint mPaintLine;
    private Paint mPaintLabel;
    private boolean mIsAdjusted;
    private Integer mLabelVerticalWidth;
    private Integer mLabelVerticalHeight;
    private Integer mLabelHorizontalWidth;
    private Integer mLabelHorizontalHeight;
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
        mStyles.horizontalLabelsColor = Color.RED;

        mStyles.gridColor = Color.RED;
        mStyles.highlightZeroLines = true;

        mStyles.padding = 20;

        reloadStyles();
    }

    public void reloadStyles() {
        mPaintLine = new Paint();
        mPaintLine.setColor(mStyles.gridColor);
        mPaintLine.setStrokeWidth(0);

        mPaintLabel = new Paint();
        mPaintLabel.setTextAlign(getVerticalLabelsAlign());
        mPaintLabel.setTextSize(getTextSize());
    }

    public float getTextSize() { return mStyles.textSize; }
    public int getVerticalLabelsColor() { return mStyles.verticalLabelsColor; }
    public Paint.Align getVerticalLabelsAlign() { return mStyles.verticalLabelsAlign; }
    public int getHorizontalLabelsColor() { return mStyles.horizontalLabelsColor; }

    public void invalide() {
        mIsAdjusted = false;
        mLabelVerticalWidth = null;
        mLabelVerticalHeight = null;
    }

    protected boolean adjustVertical() {
        if (mLabelHorizontalHeight == null) {
            return false;
        }

        double minY = mGraphView.getViewport().getMinY();
        double maxY = mGraphView.getViewport().getMaxY();

        // find the number of labels
        int numVerticalLabels = 5;

        // find good steps
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
        mGraphView.getViewport().setYAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);

        mStepsVertical = new LinkedHashMap<Integer, Double>(numVerticalLabels);
        int height = mGraphView.getHeight() - mStyles.padding*2 - mLabelHorizontalHeight;
        double v = newMaxY;
        int p = mStyles.padding;
        int pixelStep = height/(numVerticalLabels-1);
        for (int i = 0; i < numVerticalLabels; i++) {
            mStepsVertical.put(p, v);
            p += pixelStep;
            v -= exactSteps;
        }

        return true;
    }

    protected boolean adjustHorizontal() {
        if (mLabelVerticalWidth == null) {
            return false;
        }

        double minX = mGraphView.getViewport().getMinX();
        double maxX = mGraphView.getViewport().getMaxX();

        // find the number of labels
        int numHorizontalLabels = 5;

        // find good steps
        boolean adjusting = true;
        double newMinX = minX;
        double exactSteps = 0d;
        while (adjusting) {
            double rangeX = maxX - newMinX;
            exactSteps = rangeX / (numHorizontalLabels - 1);
            exactSteps = humanRound(exactSteps);

            // adjust viewport
            // wie oft passt STEP in minX rein?
            int count = 0;
            if (newMinX >= 0d) {
                // positive number
                while (newMinX - exactSteps >= 0) {
                    newMinX -= exactSteps;
                    count++;
                }
                newMinX = exactSteps * count;
            } else {
                // negative number
                count++;
                while (newMinX + exactSteps < 0) {
                    newMinX += exactSteps;
                    count++;
                }
                newMinX = exactSteps * count * -1;
            }

            // wenn minX sich geändert hat, steps nochmal berechnen
            // wenn nicht, fertig
            if (newMinX == minX) {
                adjusting = false;
            } else {
                minX = newMinX;
            }
        }

        double newMaxX = newMinX + (numHorizontalLabels-1)*exactSteps;
        mGraphView.getViewport().setMinX(newMinX);
        mGraphView.getViewport().setMaxX(newMaxX);
        mGraphView.getViewport().setXAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);

        mStepsHorizontal = new LinkedHashMap<Integer, Double>(numHorizontalLabels);
        int width = mGraphView.getWidth() - mStyles.padding*2 - mLabelVerticalWidth;
        double v = newMinX;
        int p = mStyles.padding + mLabelVerticalWidth;
        int pixelStep = width/(numHorizontalLabels-1);
        for (int i = 0; i < numHorizontalLabels; i++) {
            mStepsHorizontal.put(p, v);
            p += pixelStep;
            v += exactSteps;
        }

        return true;
    }

    /**
     * adjusts the grid and labels to match to the data
     */
    protected void adjust() {
        mIsAdjusted = adjustVertical();
        mIsAdjusted &= adjustHorizontal();
    }

    protected void calcLabelVerticalSize(Canvas canvas) {
        // test label
        double testY = ((mGraphView.getViewport().getMaxY()-mGraphView.getViewport().getMinY())*0.783)+mGraphView.getViewport().getMinY();
        String testLabel = mLabelFormatter.formatLabel(testY, false);
        Rect textBounds = new Rect();
        mPaintLabel.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
        mLabelVerticalWidth = textBounds.width();
        mLabelVerticalHeight = textBounds.height();

        // multiline
        int lines = 1;
        for (byte c : testLabel.getBytes()) {
            if (c == '\n') lines++;
        }
        mLabelVerticalHeight *= lines;
    }

    protected void calcLabelHorizontalSize(Canvas canvas) {
        // test label
        double testX = ((mGraphView.getViewport().getMaxX()-mGraphView.getViewport().getMinX())*0.783)+mGraphView.getViewport().getMinX();
        String testLabel = mLabelFormatter.formatLabel(testX, true);
        Rect textBounds = new Rect();
        mPaintLabel.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
        mLabelHorizontalWidth = textBounds.width();
        mLabelHorizontalHeight = textBounds.height();

        // multiline
        int lines = 1;
        for (byte c : testLabel.getBytes()) {
            if (c == '\n') lines++;
        }
        mLabelHorizontalHeight *= lines;
    }

    public void draw(Canvas canvas) {
        if (mLabelHorizontalWidth == null) {
            calcLabelHorizontalSize(canvas);
        }
        if (mLabelVerticalWidth == null) {
            calcLabelVerticalSize(canvas);
        }

        if (!mIsAdjusted) {
            adjust();
        }

        if (mIsAdjusted) {
            drawVerticalSteps(canvas);
            drawHorizontalSteps(canvas);
        }
    }

    protected void drawHorizontalSteps(Canvas canvas) {
        // draw horizontal steps (vertical lines and horizontal labels)
        mPaintLabel.setColor(getHorizontalLabelsColor());
        int i=0;
        for (Map.Entry<Integer, Double> e : mStepsHorizontal.entrySet()) {
            // draw line
            if (mStyles.highlightZeroLines) {
                if (e.getValue() == 0d) {
                    mPaintLine.setStrokeWidth(5);
                } else {
                    mPaintLine.setStrokeWidth(0);
                }
            }
            canvas.drawLine(e.getKey(), mStyles.padding, e.getKey(), canvas.getHeight()- mStyles.padding, mPaintLine);

            // draw label
            mPaintLabel.setTextAlign(Paint.Align.CENTER);
            if (i==mStepsHorizontal.size()-1)
                mPaintLabel.setTextAlign(Paint.Align.RIGHT);
            if (i==0)
                mPaintLabel.setTextAlign(Paint.Align.LEFT);
            String[] lines = mLabelFormatter.formatLabel(e.getValue(), true).split("\n");
            for (int li=0; li<lines.length; li++) {
                // for the last line y = height
                float y = (canvas.getHeight()- mStyles.padding) - (lines.length-li-1)*getTextSize()*1.1f;
                canvas.drawText(lines[li], e.getKey(), y, mPaintLabel);
            }
            i++;
        }
    }

    protected void drawVerticalSteps(Canvas canvas) {
        // draw vertical steps (horizontal lines and vertical labels)
        float startLeft = mStyles.padding + mLabelVerticalWidth;
        mPaintLabel.setColor(getVerticalLabelsColor());
        for (Map.Entry<Integer, Double> e : mStepsVertical.entrySet()) {
            // draw line
            if (mStyles.highlightZeroLines) {
                if (e.getValue() == 0d) {
                    mPaintLine.setStrokeWidth(5);
                } else {
                    mPaintLine.setStrokeWidth(0);
                }
            }
            canvas.drawLine(startLeft, e.getKey(), canvas.getWidth()- mStyles.padding, e.getKey(), mPaintLine);

            // draw label
            int labelsWidth = mLabelVerticalWidth;
            int labelsOffset = 0;
            if (getVerticalLabelsAlign() == Paint.Align.RIGHT) {
                labelsOffset = labelsWidth;
            } else if (getVerticalLabelsAlign() == Paint.Align.CENTER) {
                labelsOffset = labelsWidth / 2;
            }
            labelsOffset += mStyles.padding;

            float y = e.getKey();

            String[] lines = mLabelFormatter.formatLabel(e.getValue(), false).split("\n");
            y += (lines.length*getTextSize()*1.1f) / 2; // center text vertically
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

    public Styles getStyles() {
        return mStyles;
    }

    public int getLabelVerticalWidth() {
        return mLabelVerticalWidth==null?0:mLabelVerticalWidth;
    }

    public int getLabelHorizontalHeight() {
        return mLabelHorizontalHeight==null?0:mLabelHorizontalHeight;
    }
}
