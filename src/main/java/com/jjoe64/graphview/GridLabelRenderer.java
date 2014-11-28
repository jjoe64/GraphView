package com.jjoe64.graphview;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.util.TypedValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jonas on 13.08.14.
 */
public class GridLabelRenderer {
    public final class Styles {
        public float textSize;
        public Paint.Align verticalLabelsAlign;
        public Paint.Align verticalLabelsSecondScaleAlign;
        public int verticalLabelsColor;
        public int verticalLabelsSecondScaleColor;
        public int horizontalLabelsColor;
        public int gridColor;
        public boolean highlightZeroLines;
        public int padding;
        public float verticalAxisTitleTextSize;
        public int verticalAxisTitleColor;
        public float horizontalAxisTitleTextSize;
        public int horizontalAxisTitleColor;
        boolean horizontalLabelsVisible;
        boolean verticalLabelsVisible;
    }

    protected Styles mStyles;
    private final GraphView mGraphView;
    private Map<Integer, Double> mStepsVertical;
    private Map<Integer, Double> mStepsVerticalSecondScale;
    private Map<Integer, Double> mStepsHorizontal;
    private Map<Double, String> mVerticalLabels;
    private Paint mPaintLine;
    private Paint mPaintLabel;
    private Paint mPaintAxisTitle;
    private boolean mIsAdjusted;
    private Integer mLabelVerticalWidth;
    private Integer mLabelVerticalHeight;
    private Integer mLabelVerticalSecondScaleWidth;
    private Integer mLabelVerticalSecondScaleHeight;
    private Integer mLabelHorizontalWidth;
    private Integer mLabelHorizontalHeight;
    private LabelFormatter mLabelFormatter;
    private String mHorizontalAxisTitle;
    private String mVerticalAxisTitle;
    private int mNumVerticalLabels;
    private int mNumHorizontalLabels;

    public GridLabelRenderer(GraphView graphView) {
        mGraphView = graphView;
        setLabelFormatter(new DefaultLabelFormatter());
        mStyles = new Styles();
        resetStyles();
        mNumVerticalLabels = 5;
        mNumHorizontalLabels = 5;
    }

    public void resetStyles() {
        // get matching styles from theme
        TypedValue typedValue = new TypedValue();
        mGraphView.getContext().getTheme().resolveAttribute(android.R.attr.textAppearanceSmall, typedValue, true);

        int color1;
        int color2;
        int size;
        int size2;

        TypedArray array = null;
        try {
            array = mGraphView.getContext().obtainStyledAttributes(typedValue.data, new int[]{
                    android.R.attr.textColorPrimary
                    , android.R.attr.textColorSecondary
                    , android.R.attr.textSize
                    , android.R.attr.horizontalGap});
            color1 = array.getColor(0, Color.BLACK);
            color2 = array.getColor(1, Color.GRAY);
            size = array.getDimensionPixelSize(2, 20);
            size2 = array.getDimensionPixelSize(3, 20);
            array.recycle();
        } catch (Exception e) {
            color1 = Color.BLACK;
            color2 = Color.GRAY;
            size = 20;
            size2 = 20;
        }

        mStyles.verticalLabelsColor = color1;
        mStyles.verticalLabelsSecondScaleColor = color1;
        mStyles.horizontalLabelsColor = color1;
        mStyles.gridColor = color2;
        mStyles.textSize = size;
        mStyles.padding = size2;

        mStyles.verticalLabelsAlign = Paint.Align.RIGHT;
        mStyles.verticalLabelsSecondScaleAlign = Paint.Align.LEFT;
        mStyles.highlightZeroLines = true;

        mStyles.verticalAxisTitleColor = mStyles.verticalLabelsColor;
        mStyles.horizontalAxisTitleColor = mStyles.horizontalLabelsColor;
        mStyles.verticalAxisTitleTextSize = mStyles.textSize;
        mStyles.horizontalAxisTitleTextSize = mStyles.textSize;

        mStyles.horizontalLabelsVisible = true;
        mStyles.verticalLabelsVisible = true;

        reloadStyles();
    }

    public void reloadStyles() {
        mPaintLine = new Paint();
        mPaintLine.setColor(mStyles.gridColor);
        mPaintLine.setStrokeWidth(0);

        mPaintLabel = new Paint();
        mPaintLabel.setTextSize(getTextSize());

        mPaintAxisTitle = new Paint();
        mPaintAxisTitle.setTextSize(getTextSize());
        mPaintAxisTitle.setTextAlign(Paint.Align.CENTER);
    }

    public float getTextSize() {
        return mStyles.textSize;
    }

    public int getVerticalLabelsColor() {
        return mStyles.verticalLabelsColor;
    }

    public Paint.Align getVerticalLabelsAlign() {
        return mStyles.verticalLabelsAlign;
    }

    public int getHorizontalLabelsColor() {
        return mStyles.horizontalLabelsColor;
    }

    public void invalidate(boolean keepLabelsSize, boolean keepViewport) {
        if (!keepViewport) {
            mIsAdjusted = false;
        }
        if (!keepLabelsSize) {
            mLabelVerticalWidth = null;
            mLabelVerticalHeight = null;
            mLabelVerticalSecondScaleWidth = null;
            mLabelVerticalSecondScaleHeight = null;
        }
        reloadStyles();
    }

    protected boolean adjustVerticalSecondScale() {
        if (mLabelHorizontalHeight == null) {
            return false;
        }
        if (mGraphView.mSecondScale == null) {
            return true;
        }

        double minY = mGraphView.mSecondScale.getMinY();
        double maxY = mGraphView.mSecondScale.getMaxY();

        Log.d("GridLabelRenderer", "minY=" + minY + "/maxY=" + maxY);

        // TODO find the number of labels
        int numVerticalLabels = mNumVerticalLabels;

        double newMinY;
        double exactSteps;

        if (mGraphView.mSecondScale.isYAxisBoundsManual()) {
            newMinY = minY;
            double rangeY = maxY - newMinY;
            exactSteps = rangeY / (numVerticalLabels - 1);
        } else {
            // TODO auto adjusting
            throw new IllegalStateException("Not yet implemented");
        }

        double newMaxY = newMinY + (numVerticalLabels - 1) * exactSteps;

        // TODO auto adjusting
        //mGraphView.getViewport().setMinY(newMinY);
        //mGraphView.getViewport().setMaxY(newMaxY);

        //if (!mGraphView.getViewport().isYAxisBoundsManual()) {
        //    mGraphView.getViewport().setYAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);
        //}

        mStepsVerticalSecondScale = new LinkedHashMap<Integer, Double>(numVerticalLabels);
        int height = mGraphView.getGraphContentHeight();
        double v = newMaxY;
        int p = mGraphView.getGraphContentTop(); // start
        int pixelStep = height / (numVerticalLabels - 1);
        for (int i = 0; i < numVerticalLabels; i++) {
            mStepsVerticalSecondScale.put(p, v);
            p += pixelStep;
            v -= exactSteps;
        }

        return true;
    }

    protected boolean adjustVertical() {
        if (mLabelHorizontalHeight == null) {
            return false;
        }

        double minY = mGraphView.getViewport().getMinY(false);
        double maxY = mGraphView.getViewport().getMaxY(false);

        if (minY == maxY) {
            return false;
        }

        Log.d("GridLabelRenderer", "minY=" + minY + "/maxY=" + maxY);

        // TODO find the number of labels
        int numVerticalLabels = mNumVerticalLabels;

        double newMinY;
        double exactSteps;

        if (mGraphView.getViewport().isYAxisBoundsManual()) {
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
                exactSteps = humanRound(exactSteps, true);

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

        double newMaxY = newMinY + (numVerticalLabels - 1) * exactSteps;
        mGraphView.getViewport().setMinY(newMinY);
        mGraphView.getViewport().setMaxY(newMaxY);

        if (!mGraphView.getViewport().isYAxisBoundsManual()) {
            mGraphView.getViewport().setYAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);
        }

        mStepsVertical = new LinkedHashMap<Integer, Double>(numVerticalLabels);
        int height = mGraphView.getGraphContentHeight();
        double v = newMaxY;
        int p = mGraphView.getGraphContentTop(); // start
        int pixelStep = height / (numVerticalLabels - 1);
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
        if (minX == maxX) return false;

        // TODO find the number of labels
        int numHorizontalLabels = mNumHorizontalLabels;

        double newMinX;
        double exactSteps;

        float scalingOffset = 0f;
        if (mGraphView.getViewport().isXAxisBoundsManual() && mGraphView.getViewport().getXAxisBoundsStatus() != Viewport.AxisBoundsStatus.READJUST_AFTER_SCALE) {
            // scaling
            if (mGraphView.getViewport().mScalingActive) {
                minX = mGraphView.getViewport().mScalingBeginLeft;
                maxX = minX + mGraphView.getViewport().mScalingBeginWidth;

                //numHorizontalLabels *= (mGraphView.getViewport().mCurrentViewport.width()+oldStep)/(mGraphView.getViewport().mScalingBeginWidth+oldStep);
                //numHorizontalLabels = (float) Math.ceil(numHorizontalLabels);
                Log.d("GridLabelRenderer", "hhier scaling");
            }

            newMinX = minX;
            double rangeX = maxX - newMinX;
            exactSteps = rangeX / (numHorizontalLabels - 1);
        } else {
            Log.d("GridLabelRenderer", "find good steps for: " + minX + "/" + maxX);
            // find good steps
            boolean adjusting = true;
            newMinX = minX;
            exactSteps = 0d;
            while (adjusting) {
                double rangeX = maxX - newMinX;
                exactSteps = rangeX / (numHorizontalLabels - 1);

                boolean roundAlwaysUp = true;
                if (mGraphView.getViewport().getXAxisBoundsStatus() == Viewport.AxisBoundsStatus.READJUST_AFTER_SCALE) {
                    // if viewports gets smaller, round down
                    if (mGraphView.getViewport().mCurrentViewport.width() < mGraphView.getViewport().mScalingBeginWidth) {
                        roundAlwaysUp = false;
                    }
                }
                exactSteps = humanRound(exactSteps, roundAlwaysUp);

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
            if (mGraphView.getViewport().getXAxisBoundsStatus() == Viewport.AxisBoundsStatus.READJUST_AFTER_SCALE) {
                mGraphView.getViewport().setXAxisBoundsStatus(Viewport.AxisBoundsStatus.FIX);
            } else {
                mGraphView.getViewport().setXAxisBoundsStatus(Viewport.AxisBoundsStatus.AUTO_ADJUSTED);
            }
        }

        mStepsHorizontal = new LinkedHashMap<Integer, Double>((int) numHorizontalLabels);
        int width = mGraphView.getGraphContentWidth();

        float scrolled = 0;
        float scrolledPixels = 0;

        double v = newMinX;
        int p = mGraphView.getGraphContentLeft(); // start
        float pixelStep = width / (numHorizontalLabels - 1);

        if (mGraphView.getViewport().mScalingActive) {
            float oldStep = mGraphView.getViewport().mScalingBeginWidth / (numHorizontalLabels - 1);
            float factor = (mGraphView.getViewport().mCurrentViewport.width() + oldStep) / (mGraphView.getViewport().mScalingBeginWidth + oldStep);
            pixelStep *= 1f / factor;

            //numHorizontalLabels *= (mGraphView.getViewport().mCurrentViewport.width()+oldStep)/(mGraphView.getViewport().mScalingBeginWidth+oldStep);
            //numHorizontalLabels = (float) Math.ceil(numHorizontalLabels);
            Log.d("GridLabelRenderer", "hhier scaling");

            //scrolled = ((float) mGraphView.getViewport().getMinX(false) - mGraphView.getViewport().mScalingBeginLeft)*2;
            float newWidth = width * 1f / factor;
            scrolledPixels = (newWidth - width) * -0.5f;

        }


        // scrolling
        if (!Float.isNaN(mGraphView.getViewport().mScrollingReferenceX)) {
            scrolled = mGraphView.getViewport().mScrollingReferenceX - (float) newMinX;
            scrolledPixels += scrolled * (pixelStep / (float) exactSteps);

            if (scrolled < 0 - exactSteps) {
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
        mIsAdjusted &= adjustVerticalSecondScale();
        mIsAdjusted &= adjustHorizontal();
    }

    protected void calcLabelVerticalSize(Canvas canvas) {
        // test label
        double testY = ((mGraphView.getViewport().getMaxY(false) - mGraphView.getViewport().getMinY(false)) * 0.783) + mGraphView.getViewport().getMinY(false);
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

    protected void calcLabelVerticalSecondScaleSize(Canvas canvas) {
        if (mGraphView.mSecondScale == null) {
            mLabelVerticalSecondScaleWidth = 0;
            mLabelVerticalSecondScaleHeight = 0;
            return;
        }

        // test label
        double testY = ((mGraphView.mSecondScale.getMaxY() - mGraphView.mSecondScale.getMinY()) * 0.783) + mGraphView.mSecondScale.getMinY();
        String testLabel = mLabelFormatter.formatLabel(testY, false);
        Rect textBounds = new Rect();
        mPaintLabel.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
        mLabelVerticalSecondScaleWidth = textBounds.width();
        mLabelVerticalSecondScaleHeight = textBounds.height();

        // multiline
        int lines = 1;
        for (byte c : testLabel.getBytes()) {
            if (c == '\n') lines++;
        }
        mLabelVerticalSecondScaleHeight *= lines;
    }

    protected void calcLabelHorizontalSize(Canvas canvas) {
        // test label
        double testX = ((mGraphView.getViewport().getMaxX(false) - mGraphView.getViewport().getMinX(false)) * 0.783) + mGraphView.getViewport().getMinX(false);
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
        if (mLabelVerticalSecondScaleWidth == null) {
            calcLabelVerticalSecondScaleSize(canvas);
            labelSizeChanged = true;
        }
        if (labelSizeChanged) {
            // redraw
            ViewCompat.postInvalidateOnAnimation(mGraphView);
            return;
        }

        if (!mIsAdjusted) {
            adjust();
        }

        if (mIsAdjusted) {
            drawVerticalSteps(canvas);
            drawVerticalStepsSecondScale(canvas);
            drawHorizontalSteps(canvas);
        } else {
            // we can not draw anything
            return;
        }

        drawHorizontalAxisTitle(canvas);
        drawVerticalAxisTitle(canvas);
    }

    protected void drawHorizontalAxisTitle(Canvas canvas) {
        if (mHorizontalAxisTitle != null && mHorizontalAxisTitle.length() > 0) {
            mPaintAxisTitle.setColor(getHorizontalAxisTitleColor());
            mPaintAxisTitle.setTextSize(getHorizontalAxisTitleTextSize());
            float x = canvas.getWidth() / 2;
            float y = canvas.getHeight() - mStyles.padding;
            canvas.drawText(mHorizontalAxisTitle, x, y, mPaintAxisTitle);
        }
    }

    protected void drawVerticalAxisTitle(Canvas canvas) {
        if (mVerticalAxisTitle != null && mVerticalAxisTitle.length() > 0) {
            mPaintAxisTitle.setColor(getVerticalAxisTitleColor());
            mPaintAxisTitle.setTextSize(getVerticalAxisTitleTextSize());
            float x = getVerticalAxisTitleWidth();
            float y = canvas.getHeight() / 2;
            canvas.save();
            canvas.rotate(-90, x, y);
            canvas.drawText(mVerticalAxisTitle, x, y, mPaintAxisTitle);
            canvas.restore();
        }
    }

    public int getHorizontalAxisTitleHeight() {
        if (mHorizontalAxisTitle != null && mHorizontalAxisTitle.length() > 0) {
            return (int) getHorizontalAxisTitleTextSize();
        } else {
            return 0;
        }
    }

    public int getVerticalAxisTitleWidth() {
        if (mVerticalAxisTitle != null && mVerticalAxisTitle.length() > 0) {
            return (int) getVerticalAxisTitleTextSize();
        } else {
            return 0;
        }
    }

    protected void drawHorizontalSteps(Canvas canvas) {
        // draw horizontal steps (vertical lines and horizontal labels)
        mPaintLabel.setColor(getHorizontalLabelsColor());
        int i = 0;
        for (Map.Entry<Integer, Double> e : mStepsHorizontal.entrySet()) {
            // draw line
            if (mStyles.highlightZeroLines) {
                if (e.getValue() == 0d) {
                    mPaintLine.setStrokeWidth(5);
                } else {
                    mPaintLine.setStrokeWidth(0);
                }
            }
            canvas.drawLine(e.getKey(), mGraphView.getGraphContentTop(), e.getKey(), mGraphView.getGraphContentTop() + mGraphView.getGraphContentHeight(), mPaintLine);

            // draw label
            if (isHorizontalLabelsVisible()) {
                mPaintLabel.setTextAlign(Paint.Align.CENTER);
                if (i == mStepsHorizontal.size() - 1)
                    mPaintLabel.setTextAlign(Paint.Align.RIGHT);
                if (i == 0)
                    mPaintLabel.setTextAlign(Paint.Align.LEFT);

                // multiline labels
                String[] lines = mLabelFormatter.formatLabel(e.getValue(), true).split("\n");
                for (int li = 0; li < lines.length; li++) {
                    // for the last line y = height
                    float y = (canvas.getHeight() - mStyles.padding - getHorizontalAxisTitleHeight()) - (lines.length - li - 1) * getTextSize() * 1.1f;
                    canvas.drawText(lines[li], e.getKey(), y, mPaintLabel);
                }
            }
            i++;
        }
    }

    protected void drawVerticalStepsSecondScale(Canvas canvas) {
        if (mGraphView.mSecondScale == null) {
            return;
        }

        // draw only the vertical labels on the right
        float startLeft = mGraphView.getGraphContentLeft() + mGraphView.getGraphContentWidth();
        mPaintLabel.setColor(getVerticalLabelsSecondScaleColor());
        mPaintLabel.setTextAlign(getVerticalLabelsSecondScaleAlign());
        for (Map.Entry<Integer, Double> e : mStepsVerticalSecondScale.entrySet()) {
            // draw label
            int labelsWidth = mLabelVerticalSecondScaleWidth;
            int labelsOffset = (int) startLeft;
            if (getVerticalLabelsSecondScaleAlign() == Paint.Align.RIGHT) {
                labelsOffset += labelsWidth;
            } else if (getVerticalLabelsSecondScaleAlign() == Paint.Align.CENTER) {
                labelsOffset += labelsWidth / 2;
            }

            float y = e.getKey();

            String[] lines = mLabelFormatter.formatLabel(e.getValue(), false).split("\n");
            y += (lines.length * getTextSize() * 1.1f) / 2; // center text vertically
            for (int li = 0; li < lines.length; li++) {
                // for the last line y = height
                float y2 = y - (lines.length - li - 1) * getTextSize() * 1.1f;
                canvas.drawText(lines[li], labelsOffset, y2, mPaintLabel);
            }
        }
    }

    protected void drawVerticalSteps(Canvas canvas) {
        // draw vertical steps (horizontal lines and vertical labels)
        float startLeft = mGraphView.getGraphContentLeft();
        mPaintLabel.setColor(getVerticalLabelsColor());
        mPaintLabel.setTextAlign(getVerticalLabelsAlign());
        for (Map.Entry<Integer, Double> e : mStepsVertical.entrySet()) {
            // draw line
            if (mStyles.highlightZeroLines) {
                if (e.getValue() == 0d) {
                    mPaintLine.setStrokeWidth(5);
                } else {
                    mPaintLine.setStrokeWidth(0);
                }
            }
            canvas.drawLine(startLeft, e.getKey(), startLeft + mGraphView.getGraphContentWidth(), e.getKey(), mPaintLine);

            // draw label
            if (isVerticalLabelsVisible()) {
                int labelsWidth = mLabelVerticalWidth;
                int labelsOffset = 0;
                if (getVerticalLabelsAlign() == Paint.Align.RIGHT) {
                    labelsOffset = labelsWidth;
                } else if (getVerticalLabelsAlign() == Paint.Align.CENTER) {
                    labelsOffset = labelsWidth / 2;
                }
                labelsOffset += mStyles.padding + getVerticalAxisTitleWidth();

                float y = e.getKey();

                String[] lines = mLabelFormatter.formatLabel(e.getValue(), false).split("\n");
                y += (lines.length * getTextSize() * 1.1f) / 2; // center text vertically
                for (int li = 0; li < lines.length; li++) {
                    // for the last line y = height
                    float y2 = y - (lines.length - li - 1) * getTextSize() * 1.1f;
                    canvas.drawText(lines[li], labelsOffset, y2, mPaintLabel);
                }
            }
        }
    }

    protected double humanRound(double in, boolean roundAlwaysUp) {
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
        if (roundAlwaysUp) {
            if (in == 1d) {
            } else if (in <= 2d) {
                in = 2d;
            } else if (in <= 5d) {
                in = 5d;
            } else if (in < 10d) {
                in = 10d;
            }
        } else { // always round down
            Log.d("GridLabelRenderer", "round down " + in);
            if (in == 1d) {
            } else if (in <= 4.9d) {
                in = 2d;
            } else if (in <= 9.9d) {
                in = 5d;
            } else if (in < 15d) {
                in = 10d;
            }
            Log.d("GridLabelRenderer", "-> " + in);

        }
        return in * Math.pow(10d, ten);
    }

    public Styles getStyles() {
        return mStyles;
    }

    public int getLabelVerticalWidth() {
        return mLabelVerticalWidth == null || !isVerticalLabelsVisible() ? 0 : mLabelVerticalWidth;
    }

    public int getLabelHorizontalHeight() {
        return mLabelHorizontalHeight == null || !isHorizontalLabelsVisible() ? 0 : mLabelHorizontalHeight;
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
        mLabelFormatter.setViewport(mGraphView.getViewport());
    }

    public String getHorizontalAxisTitle() {
        return mHorizontalAxisTitle;
    }

    public void setHorizontalAxisTitle(String mHorizontalAxisTitle) {
        this.mHorizontalAxisTitle = mHorizontalAxisTitle;
    }

    public String getVerticalAxisTitle() {
        return mVerticalAxisTitle;
    }

    public void setVerticalAxisTitle(String mVerticalAxisTitle) {
        this.mVerticalAxisTitle = mVerticalAxisTitle;
    }

    public float getVerticalAxisTitleTextSize() {
        return mStyles.verticalAxisTitleTextSize;
    }

    public void setVerticalAxisTitleTextSize(float verticalAxisTitleTextSize) {
        mStyles.verticalAxisTitleTextSize = verticalAxisTitleTextSize;
    }

    public int getVerticalAxisTitleColor() {
        return mStyles.verticalAxisTitleColor;
    }

    public void setVerticalAxisTitleColor(int verticalAxisTitleColor) {
        mStyles.verticalAxisTitleColor = verticalAxisTitleColor;
    }

    public float getHorizontalAxisTitleTextSize() {
        return mStyles.horizontalAxisTitleTextSize;
    }

    public void setHorizontalAxisTitleTextSize(float horizontalAxisTitleTextSize) {
        mStyles.horizontalAxisTitleTextSize = horizontalAxisTitleTextSize;
    }

    public int getHorizontalAxisTitleColor() {
        return mStyles.horizontalAxisTitleColor;
    }

    public void setHorizontalAxisTitleColor(int horizontalAxisTitleColor) {
        mStyles.horizontalAxisTitleColor = horizontalAxisTitleColor;
    }

    public Paint.Align getVerticalLabelsSecondScaleAlign() {
        return mStyles.verticalLabelsSecondScaleAlign;
    }

    public void setVerticalLabelsSecondScaleAlign(Paint.Align verticalLabelsSecondScaleAlign) {
        mStyles.verticalLabelsSecondScaleAlign = verticalLabelsSecondScaleAlign;
    }

    public int getVerticalLabelsSecondScaleColor() {
        return mStyles.verticalLabelsSecondScaleColor;
    }

    public void setVerticalLabelsSecondScaleColor(int verticalLabelsSecondScaleColor) {
        mStyles.verticalLabelsSecondScaleColor = verticalLabelsSecondScaleColor;
    }

    public int getLabelVerticalSecondScaleWidth() {
        return mLabelVerticalSecondScaleWidth;
    }

    public boolean isHorizontalLabelsVisible() {
        return mStyles.horizontalLabelsVisible;
    }

    public void setHorizontalLabelsVisible(boolean horizontalTitleVisible) {
        mStyles.horizontalLabelsVisible = horizontalTitleVisible;
    }

    public boolean isVerticalLabelsVisible() {
        return mStyles.verticalLabelsVisible;
    }

    public void setVerticalLabelsVisible(boolean verticalTitleVisible) {
        mStyles.verticalLabelsVisible = verticalTitleVisible;
    }

    public int getNumVerticalLabels() {
        return mNumVerticalLabels;
    }

    public void setNumVerticalLabels(int mNumVerticalLabels) {
        this.mNumVerticalLabels = mNumVerticalLabels;
    }

    public int getNumHorizontalLabels() {
        return mNumHorizontalLabels;
    }

    public void setNumHorizontalLabels(int mNumHorizontalLabels) {
        this.mNumHorizontalLabels = mNumHorizontalLabels;
    }
}
