package com.jjoe64.graphview;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Class for show a float label on touch
 * Get instance with graphView.getFloatLabel() to customize
 * For enable this feature, set a FloatLabelFormater on the Series.
 *
 * lineGraphSeries.setFloatLabelFormatter(new FloatLabelFormatter<DataPointInterface>() {
 *     @Override
 *     public String formatFloatLabel(DataPointInterface point) {
 *         return "X: " + point.getX() + "\nY: " + point.getY();
 *     }
 * });
 *
 * @author Nartex
 */
public class FloatLabel {
    private GraphView mGraphView;
    private PointF mCurrentTouch = null;
    private Paint mPaint = new Paint();
    private Paint mTextPaint = new Paint();

    private int mChipRadius = 5;
    private int mTextPadding = 10;
    private int mTextRadius = 0;

    public FloatLabel(GraphView graphView) {
        mGraphView = graphView;

        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);

        try {
            TypedValue typedValue = new TypedValue();
            mGraphView.getContext().getTheme().resolveAttribute(android.R.attr.textAppearance, typedValue, true);
            TypedArray a = mGraphView.getContext().obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textSize, android.R.attr.horizontalGap});
            setTextSize(a.getDimensionPixelSize(0, 10));
            mTextPadding = a.getDimensionPixelSize(1, 10);
            mChipRadius = Math.round(mTextPadding / 2f);
            a.recycle();
        }catch (Exception e){
            setTextSize(20);
        }
    }

    public void setTextPadding(int padding){
        mTextPadding = padding;
    }

    public int getTextPadding(){
        return mTextPadding;
    }

    public void setColor(int color){
        mPaint.setColor(color);
    }

    public void setTextColor(int color){
        mTextPaint.setColor(color);
    }

    public void setTextRadius(int radius){
        mTextRadius = radius;
    }

    public void setChipRadius(int radius){
        mChipRadius = radius;
    }

    public void setTextSize(int textSize){
        mTextPaint.setTextSize(textSize);
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean intercept = false;

        for(Series s : mGraphView.getSeries()){
            if (s.getFloatLabelFormatter() != null){
                intercept = true;
                break;
            }
        }

        if (intercept){
            boolean handled = false;

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mCurrentTouch = new PointF(event.getX(), event.getY());
                    handled = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mCurrentTouch != null) {
                        mCurrentTouch.x = event.getX();
                        mCurrentTouch.y = event.getY();
                    }
                    handled = true;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mCurrentTouch = null;
                    handled = true;
                    break;
            }

            if (handled){
                ViewCompat.postInvalidateOnAnimation(mGraphView);
                return true;
            }
        }

        return false;
    }

    private boolean isGoodPosition(RectF rectF, RectF zone, ArrayList<RectF> others){
        if (!zone.contains(rectF)){
            return false;
        }

        for(RectF otherRect : others){
            if (RectF.intersects(rectF, otherRect)){
                return false;
            }
        }

        return true;
    }

    private void findPosition(RectF textRect, PointF pointPos, int distance, float labelWidth, float labelHeight, RectF graphRect, ArrayList<RectF> mLabels){
        //Top
        textRect.left = pointPos.x - labelWidth / 2f;
        textRect.right = textRect.left + labelWidth;
        textRect.top = pointPos.y - labelHeight - distance;
        textRect.bottom = textRect.top + labelHeight;

        if (!isGoodPosition(textRect, graphRect, mLabels)){
            //Top-Right
            textRect.left = pointPos.x + distance;
            textRect.right = textRect.left + labelWidth;
        }

        if (!isGoodPosition(textRect, graphRect, mLabels)){
            //Top-Left
            textRect.left = pointPos.x - labelWidth  - distance;
            textRect.right = textRect.left + labelWidth;
        }

        if (!isGoodPosition(textRect, graphRect, mLabels)){
            //Bottom
            textRect.top = pointPos.y + distance;
            textRect.bottom = textRect.top + labelHeight;
            textRect.left = pointPos.x - labelWidth / 2f;
            textRect.right = textRect.left + labelWidth;
        }

        if (!isGoodPosition(textRect, graphRect, mLabels)){
            //Bottom-Right
            textRect.left = pointPos.x + distance;
            textRect.right = textRect.left + labelWidth;
        }

        if (!isGoodPosition(textRect, graphRect, mLabels)){
            //Bottom-Left
            textRect.left = pointPos.x - labelWidth  - distance;
            textRect.right = textRect.left + labelWidth;
        }

        if (!isGoodPosition(textRect, graphRect, mLabels)){
            //Right
            textRect.left = pointPos.x + distance;
            textRect.right = textRect.left + labelWidth;
            textRect.top = pointPos.y - labelHeight / 2f;
            textRect.bottom = textRect.top + labelHeight;
        }

        if (!isGoodPosition(textRect, graphRect, mLabels)){
            //Left
            textRect.left = pointPos.x - labelWidth  - distance;
            textRect.right = textRect.left + labelWidth;
        }

        if (!isGoodPosition(textRect, graphRect, mLabels)){
            if (distance < Math.min(graphRect.width(), graphRect.height())){
                findPosition(textRect, pointPos, distance * 2, labelWidth, labelHeight, graphRect, mLabels);
            }else{
                //Top
                textRect.left = pointPos.x - labelWidth / 2f;
                textRect.right = textRect.left + labelWidth;
                textRect.top = pointPos.y - labelHeight - mTextPadding;
                textRect.bottom = textRect.top + labelHeight;
            }
        }
    }

    public void draw(Canvas c) {
        if (mCurrentTouch != null){
            ArrayList<RectF> mLabels = new ArrayList<>();

            ArrayList<FloatPoint> floatPoints = new ArrayList<>();
            for(Series s : mGraphView.getSeries()){
                if (s.getFloatLabelFormatter() != null){
                    floatPoints.add(new FloatPoint(s.findDataPoint(mCurrentTouch.x), s));
                }
            }

            Collections.sort(floatPoints);

            for(FloatPoint floatPoint : floatPoints){
                mPaint.setColor(floatPoint.serie.getColor());

                if (floatPoint.point != null){
                    PointF pointPos = floatPoint.serie.getDataPointPosition(floatPoint.point);
                    if (pointPos != null){
                        c.drawCircle(pointPos.x, pointPos.y, mChipRadius, mPaint);

                        RectF graphRect = new RectF(mGraphView.getGraphContentLeft(), mGraphView.getGraphContentTop(), mGraphView.getGraphContentLeft() + mGraphView.getGraphContentWidth(), mGraphView.getGraphContentTop() + mGraphView.getGraphContentHeight());

                        c.drawLine(graphRect.left, pointPos.y, graphRect.right, pointPos.y, mPaint);
                        c.drawLine(pointPos.x, graphRect.top, pointPos.x, graphRect.bottom, mPaint);

                        String[] texts = floatPoint.serie.getFloatLabelFormatter().formatFloatLabel(floatPoint.point).split("\n");

                        Rect tmpTect = new Rect();
                        float labelWidth = 0;
                        float labelHeight = mTextPadding;
                        for(int i = 0; i < texts.length; i++){
                            mTextPaint.getTextBounds(texts[i], 0, texts[i].length(), tmpTect);
                            labelWidth = Math.max(labelWidth, tmpTect.width());
                            labelHeight += tmpTect.height() + mTextPadding;
                        }
                        labelWidth += mTextPadding * 2;

                        RectF textRect = new RectF();
                        findPosition(textRect, pointPos, mTextPadding, labelWidth, labelHeight, graphRect, mLabels);

                        mLabels.add(textRect);

                        if (mTextRadius > 0){
                            c.drawRoundRect(textRect, mTextRadius, mTextRadius, mPaint);
                        }else{
                            c.drawRect(textRect, mPaint);
                        }

                        for(int i = 0; i < texts.length; i++){
                            mTextPaint.getTextBounds(texts[i], 0, texts[i].length(), tmpTect);
                            c.drawText(texts[i], textRect.left + textRect.width() / 2f, textRect.bottom - mTextPadding - (texts.length - i - 1) * (tmpTect.height() + mTextPadding), mTextPaint);
                        }
                    }
                }
            }
        }
    }

    class FloatPoint implements Comparable<FloatPoint>{
        public final DataPointInterface point;
        public final Series serie;

        FloatPoint(DataPointInterface point, Series serie){
            this.point = point;
            this.serie = serie;
        }

        @Override
        public int compareTo(FloatPoint another) {
            return another.point.getY() > point.getY() ? 1 : -1;
        }
    }
}
