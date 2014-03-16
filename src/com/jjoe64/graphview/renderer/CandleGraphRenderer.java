package com.jjoe64.graphview.renderer;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.model.GraphViewOHLCDataInterface;

public class CandleGraphRenderer <T extends GraphViewOHLCDataInterface> implements Renderer<T> {
	private final Paint linePaint;
	private final Paint upPaint;
	private final Paint downPaint;
	private double candleWidth = 20;
	
	public CandleGraphRenderer() {
		linePaint = new Paint();
		linePaint.setColor(Color.BLACK);
		linePaint.setStrokeWidth(4);
		linePaint.setAlpha(128);
		
		upPaint = new Paint();
		upPaint.setColor(Color.GREEN);
		
		downPaint = new Paint();
		downPaint.setColor(Color.RED);
	}
	public CandleGraphRenderer(Paint line, Paint up, Paint down) {
		linePaint = line;
		upPaint = up;
		downPaint = down;
	}
	@Override
	public void drawSeries(Canvas canvas, List<T> series,
			float graphwidth, float graphheight, float border, double minX,
			double minY, double diffX, double diffY, float horstart,
			GraphViewSeriesStyle style) {
		List<T> values = series;
		candleWidth = Math.max(2,graphwidth / (values.size()+2) - 10);
		float startgap = (float)(horstart+ 1);
		for (int i = 0; i < values.size(); i++) {
			double x = valueToJava2d(values.get(i).getX(), minX, diffX, graphwidth);
			
	        double yHigh = valueToJava2d(values.get(i).getHigh(), minY, diffY, graphheight);
	        double yLow = valueToJava2d(values.get(i).getLow(), minY, diffY, graphheight);
	        double yOpen = valueToJava2d(values.get(i).getOpen(), minY, diffY, graphheight);
	        double yClose = valueToJava2d(values.get(i).getClose(), minY, diffY, graphheight);

	        double maxOpenClose = Math.max(yOpen, yClose);
	        double minOpenClose = Math.min(yOpen, yClose);

	        
	        float startX = (float) x + startgap;
			float startY = (float) (border - yHigh) + graphheight;
			float endX = (float) x + startgap;
			float endY = (float) (border - yLow) + graphheight;
	        canvas.drawLine(startX, startY, endX, endY, linePaint);
	        
			startX = (float) x - ((float)candleWidth/2) + startgap;
			startY = (float) (border - maxOpenClose) + graphheight;
			endX = (float) x + (float)(candleWidth - (candleWidth/2)) + startgap;
			endY = (float) (border - minOpenClose) + graphheight;
			
			
			if (yClose > yOpen) {
				canvas.drawRect(startX, startY, endX, endY, upPaint);
	        } else {
	        	canvas.drawRect(startX, startY, endX, endY, downPaint);
	        }
		}
			
	}
	protected static double valueToJava2d(double value, double min, double realDiff, double drawSize){
		return drawSize * ((value-min) / realDiff);
	}
}
