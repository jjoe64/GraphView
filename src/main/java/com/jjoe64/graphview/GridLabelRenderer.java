package com.jjoe64.graphview;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

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
    private Paint mPaintAxisTitle;
    private boolean mIsAdjusted;
    private Integer mLabelVerticalWidth;
    private Integer mLabelVerticalHeight;
    private Integer mLabelHorizontalWidth;
    private Integer mLabelHorizontalHeight;
    private LabelFormatter mLabelFormatter;
    private String mHorizontalAxisTitle;

    public GridLabelRenderer(GraphView graphView) {
        mGraphView = graphView;
        mLabelFormatter = new DefaultLabelFormatter(graphView.getViewport());
        mStyles = new Styles();
        resetStyles();
    }

    public void resetStyles() {
        // get matching styles from theme
        TypedValue typedValue = new TypedValue();
        mGraphView.getContext().getTheme().resolveAttribute(android.R.attr.textAppearanceSmall, typedValue, true);

        TypedArray array = mGraphView.getContext().obtainStyledAttributes(typedValue.data, new int[] {
                android.R.attr.textColorPrimary
                , android.R.attr.textColorSecondary
                , android.R.attr.textSize
                , android.R.attr.horizontalGap});
        int color1 = array.getColor(0, Color.BLACK);
        int color2 = array.getColor(1, Color.GRAY);
        int size = array.getDimensionPixelSize(2, 20);
        int size2 = array.getDimensionPixelSize(3, 20);
        array.recycle();

        mStyles.verticalLabelsColor = color1;
        mStyles.horizontalLabelsColor = color1;
        mStyles.gridColor = color2;
        mStyles.textSize = size;
        mStyles.padding = size2;

        mStyles.verticalLabelsAlign = Paint.Align.RIGHT;
        mStyles.highlightZeroLines = true;

        reloadStyles();
    }

    public void reloadStyles() {
        mPaintLine = new Paint();
        mPaintLine.setColor(mStyles.gridColor);
        mPaintLine.setStrokeWidth(0);

        mPaintLabel = new Paint();
        mPaintLabel.setTextAlign(getVerticalLabelsAlign());
        mPaintLabel.setTextSize(getTextSize());

        mPaintAxisTitle = new Paint();
        mPaintAxisTitle.setTextSize(getTextSize());
        mPaintAxisTitle.setTextAlign(Paint.Align.CENTER);
    }

    public float getTextSize() { return mStyles.textSize; }
    public int getVerticalLabelsColor() { return mStyles.verticalLabelsColor; }
    public Paint.Align getVerticalLabelsAlign() { return mStyles.verticalLabelsAlign; }
    public int getHorizontalLabelsColor() { return mStyles.horizontalLabelsColor; }

    public void invalide() {
        mIsAdjusted = false;
        mLabelVerticalWidth = null;
        mLabelVerticalHeight = null;
        reloadStyles();
    }

    protected boolean adjustVertical() {
        if (mLabelHorizontalHeight == null) {
            return false;
        }

        double minY = mGraphView.getViewport().getMinY(false);
        double maxY = mGraphView.getViewport().getMaxY(false);

        Log.d("GridLabelRenderer", "minY="+minY+"/maxY="+maxY);

        // TODO find the number of labels
        int numVerticalLabels = 5;

        double newMinY;
        double exactSteps;

        if (mGraphView.getViewport().getYAxisBoundsStatus() == Viewport.AxisBoundsStatus.MANUAL) {
            newMinY = minY;
            double rangeY = maxY - newMinY;
            exactSteps = rangeY / (numVerticalLabels - 1);
        } else {
            // find good steps
            boolean adjusting = true;
            newMinY = minY;
            exactSteps = 0d;
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
        }

        double newMaxY = newMinY + (numVerticalLabels-1)*exactSteps;
        mGraphView.getViewport().setMinY(newMinY);
        mGraphView.getViewport().setMaxY(newMaxY);
        if (mGraphView.getViewport().getYAxisBoundsStatus() != Viewport.AxisBoundsStatus.MANUAL) {
            mGraphView.getViewport().setYAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);
        }

        mStepsVertical = new LinkedHashMap<Integer, Double>(numVerticalLabels);
        int height = mGraphView.getGraphContentHeight();
        double v = newMaxY;
        int p = mGraphView.getGraphContentTop(); // start
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

        double minX = mGraphView.getViewport().getMinX(false);
        double maxX = mGraphView.getViewport().getMaxX(false);

        // TODO find the number of labels
        int numHorizontalLabels = 5;

        double newMinX;
        double exactSteps;

        if (mGraphView.getViewport().getXAxisBoundsStatus() == Viewport.AxisBoundsStatus.MANUAL) {
            newMinX = minX;
            double rangeX = maxX - newMinX;
            exactSteps = rangeX / (numHorizontalLabels - 1);
        } else {
            // find good steps
            boolean adjusting = true;
            newMinX = minX;
            exactSteps = 0d;
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

            double newMaxX = newMinX + (numHorizontalLabels - 1) * exactSteps;
            mGraphView.getViewport().setMinX(newMinX);
            mGraphView.getViewport().setMaxX(newMaxX);
            mGraphView.getViewport().setXAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);
        }

        mStepsHorizontal = new LinkedHashMap<Integer, Double>(numHorizontalLabels);
        int width = mGraphView.getGraphContentWidth();


        double v = newMinX;
        int p = mGraphView.getGraphContentLeft(); // start
        int pixelStep = width/(numHorizontalLabels-1);

        float scrolled = 0;
        float scrolledPixels = 0;
        if (!Float.isNaN(mGraphView.getViewport().mScrollingReferenceX)) {
            scrolled = mGraphView.getViewport().mScrollingReferenceX - (float) newMinX;
            scrolledPixels = scrolled * ((float)pixelStep/(float)exactSteps);

            if (scrolled < 0-exactSteps) {
                mGraphView.getViewport().mScrollingReferenceX += exactSteps;
            } else if (scrolled > exactSteps) {
                mGraphView.getViewport().mScrollingReferenceX -= exactSteps;
            }
        }
        p += scrolledPixels;
        v += scrolled;
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
        double testY = ((mGraphView.getViewport().getMaxY(false)-mGraphView.getViewport().getMinY(false))*0.783)+mGraphView.getViewport().getMinY(false);
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
        double testX = ((mGraphView.getViewport().getMaxX(false)-mGraphView.getViewport().getMinX(false))*0.783)+mGraphView.getViewport().getMinX(false);
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
        boolean labelSizeChanged = false;
        if (mLabelHorizontalWidth == null) {
            calcLabelHorizontalSize(canvas);
            labelSizeChanged = true;
        }
        if (mLabelVerticalWidth == null) {
            calcLabelVerticalSize(canvas);
            labelSizeChanged = true;
        }
        if (labelSizeChanged) {
            // redraw
            mGraphView.postInvalidateOnAnimation();
            return;
        }

        if (!mIsAdjusted) {
            adjust();
        }

        if (mIsAdjusted) {
            drawVerticalSteps(canvas);
            drawHorizontalSteps(canvas);
        }

        drawHorizontalAxisTitle(canvas);
    }

    protected void drawHorizontalAxisTitle(Canvas canvas) {
        if (mHorizontalAxisTitle != null && mHorizontalAxisTitle.length()>0) {
            mPaintAxisTitle.setColor(getHorizontalLabelsColor());
            float x = canvas.getWidth()/2;
            float y = canvas.getHeight()- mStyles.padding;
            canvas.drawText(mHorizontalAxisTitle, x, y, mPaintAxisTitle);
        }
    }

    public int getHorizontalAxisTitleHeight() {
        if (mHorizontalAxisTitle != null && mHorizontalAxisTitle.length() > 0) {
            return (int) mPaintAxisTitle.getTextSize();
        } else {
            return 0;
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
            canvas.drawLine(e.getKey(), mGraphView.getGraphContentTop(), e.getKey(), mGraphView.getGraphContentTop()+mGraphView.getGraphContentHeight(), mPaintLine);

            // draw label
            mPaintLabel.setTextAlign(Paint.Align.CENTER);
            if (i==mStepsHorizontal.size()-1)
                mPaintLabel.setTextAlign(Paint.Align.RIGHT);
            if (i==0)
                mPaintLabel.setTextAlign(Paint.Align.LEFT);

            // multiline labels
            String[] lines = mLabelFormatter.formatLabel(e.getValue(), true).split("\n");
            for (int li=0; li<lines.length; li++) {
                // for the last line y = height
                float y = (canvas.getHeight()- mStyles.padding -getHorizontalAxisTitleHeight()) - (lines.length-li-1)*getTextSize()*1.1f;
                canvas.drawText(lines[li], e.getKey(), y, mPaintLabel);
            }
            i++;
        }
    }

    protected void drawVerticalSteps(Canvas canvas) {
        // draw vertical steps (horizontal lines and vertical labels)
        float startLeft = mGraphView.getGraphContentLeft();
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
            canvas.drawLine(startLeft, e.getKey(), startLeft+mGraphView.getGraphContentWidth(), e.getKey(), mPaintLine);

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

    public int getGridColor() {
        return mStyles.gridColor;
    }

    public boolean isHighlightZeroLines() {
        return mStyles.highlightZeroLines;
    }

    public int getPadding() {
        return mStyles.padding;
    }

    public void setTextSize(float textSize) {
        mStyles.textSize = textSize;
    }

    public void setVerticalLabelsAlign(Paint.Align verticalLabelsAlign) {
        mStyles.verticalLabelsAlign = verticalLabelsAlign;
    }

    public void setVerticalLabelsColor(int verticalLabelsColor) {
        mStyles.verticalLabelsColor = verticalLabelsColor;
    }

    public void setHorizontalLabelsColor(int horizontalLabelsColor) {
        mStyles.horizontalLabelsColor = horizontalLabelsColor;
    }

    public void setGridColor(int gridColor) {
        mStyles.gridColor = gridColor;
    }

    public void setHighlightZeroLines(boolean highlightZeroLines) {
        mStyles.highlightZeroLines = highlightZeroLines;
    }

    public void setPadding(int padding) {
        mStyles.padding = padding;
    }

    public LabelFormatter getLabelFormatter() {
        return mLabelFormatter;
    }

    public void setLabelFormatter(LabelFormatter mLabelFormatter) {
        this.mLabelFormatter = mLabelFormatter;
    }

    public String getHorizontalAxisTitle() {
        return mHorizontalAxisTitle;
    }

    public void setHorizontalAxisTitle(String mHorizontalAxisTitle) {
        this.mHorizontalAxisTitle = mHorizontalAxisTitle;
    }
}
