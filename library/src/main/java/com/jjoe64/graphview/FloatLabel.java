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

/**
 * Created by Jean on 19/10/15.
 */
public class FloatLabel {
    public final static String TAG = "FloatLabel";

    private GraphView mGraphView;
    private PointF mCurrentTouch = null;
    private Paint mPaint = new Paint();
    private Paint mTextPaint = new Paint();

    private int mTextPadding = 20;
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
            setTextSize(a.getDimensionPixelSize(0, 20));
            mTextPadding = a.getDimensionPixelSize(1, 20);
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
        textRect.top = pointPos.y - labelHeight - mTextPadding;
        textRect.bottom = textRect.top + labelHeight;

        if (!isGoodPosition(textRect, graphRect, mLabels)){
            //Bottom
            textRect.top = pointPos.y + labelHeight + mTextPadding;
            textRect.bottom = textRect.top + labelHeight;
        }

        if (!isGoodPosition(textRect, graphRect, mLabels)){
            //Left
            textRect.left = pointPos.x - labelWidth  - mTextPadding;
            textRect.right = textRect.left + labelWidth;
            textRect.top = pointPos.y - labelHeight / 2f;
            textRect.bottom = textRect.top + labelHeight;
        }

        if (!isGoodPosition(textRect, graphRect, mLabels)){
            //Right
            textRect.left = pointPos.x + mTextPadding;
            textRect.right = textRect.left + labelWidth;
        }

        if (!isGoodPosition(textRect, graphRect, mLabels)){
            if (distance < graphRect.width()){
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

            for(Series s : mGraphView.getSeries()){
                if (s.getFloatLabelFormatter() != null){
                    mPaint.setColor(s.getColor());

                    DataPointInterface point = s.findDataPoint(mCurrentTouch.x);
                    if (point != null){
                        PointF pointPos = s.getDataPointPosition(point);
                        if (pointPos != null){
                            c.drawCircle(pointPos.x, pointPos.y, 10, mPaint);

                            RectF graphRect = new RectF(mGraphView.getGraphContentLeft(), mGraphView.getGraphContentTop(), mGraphView.getGraphContentLeft() + mGraphView.getGraphContentWidth(), mGraphView.getGraphContentTop() + mGraphView.getGraphContentHeight());

                            c.drawLine(graphRect.left, pointPos.y, graphRect.right, pointPos.y, mPaint);
                            c.drawLine(pointPos.x, graphRect.top, pointPos.x, graphRect.bottom, mPaint);

                            String[] texts = s.getFloatLabelFormatter().formatFloatLabel(point).split("\n");

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
    }
}
