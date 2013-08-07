package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;

import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;

/**
 * Draws a Bar Chart.
 *
 * @author Muhammad Shahab Hameed
 */
public class BarGraphView extends GraphView {
	
	/**
	 * Instantiates a new bar graph view.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 */
	public BarGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * Instantiates a new bar graph view.
	 *
	 * @param context the context
	 * @param title
	 */
	public BarGraphView(Context context, String title) {
		super(context, title);
	}

	/**
	 * Instantiates a new bar graph view.
	 *
	 * @param context the context
	 * @param title
	 * @param gvStyle the GraphViewStyle
	 */
	public BarGraphView(Context context, String title, GraphViewStyle gvStyle) {
		super(context, title, gvStyle);
	}

	/* (non-Javadoc)
	 * @see com.jjoe64.graphview.GraphView#drawSeries(android.graphics.Canvas, com.jjoe64.graphview.GraphView.GraphViewData[], float, float, float, double, double, double, double, float, com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle)
	 */
	@Override
	public void drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth, float graphheight,
			float border, double minX, double minY, double diffX, double diffY,
			float horstart, GraphViewSeriesStyle style) {
		GraphViewStyle graphStyle = getGraphViewStyle();
		Paint vpaint = new Paint();
		
		float colwidth = (graphwidth - (2 * border)) / values.length;

		paint.setStrokeWidth(style.thickness);
		paint.setColor(style.color);

		// draw data
		for (int i = 0; i < values.length; i++) {
			float valY = (float) (values[i].valueY - minY);
			float ratY = (float) (valY / diffY);
			float y = graphheight * ratY;

			// hook for value dependent color
			if (style.getValueDependentColor() != null) {
				paint.setColor(style.getValueDependentColor().get(values[i]));
			}

			float left = (i * colwidth) + horstart;
			float top = (border - y) + graphheight;
			float right = ((i * colwidth) + horstart) + (colwidth - 1);
			float bottom = graphheight + border - 1;
			canvas.drawRect(left, top, right, bottom, paint);
			
			// horizontal labels + lines
			float middle = Math.abs(left + right)/2;
			if (!graphStyle.getvLinesDraw()) {
				float valX = (float) (values[i].valueX - minX);
				vpaint.setColor(graphStyle.getGridColor());
				//canvas.drawLine(middle, (float) (graphheight + border), middle, border, vpaint);
				vpaint.setTextAlign(Align.CENTER);
				/*
				if (i==values.length-1)
					vpaint.setTextAlign(Align.RIGHT);
				if (i==0)
					vpaint.setTextAlign(Align.LEFT);
				*/
				vpaint.setColor(graphStyle.getHorizontalLabelsColor());
				String label = formatLabel(valX, true);
				//canvas.drawText(label, middle, graphheight + border - 4, vpaint);
				canvas.drawText(label, middle, graphheight + 2* border - 4, vpaint);
			}		
			
		}
	}
}
