package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;

import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;

/**
 * Draws a Bar Chart
 * @author Muhammad Shahab Hameed
 */
public class BarGraphView extends GraphView {
	public BarGraphView(Context context, String title) {
		super(context, title);
	}

	@Override
	public void drawSeries(Canvas canvas, GraphViewSeries series, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart)
	{
//	public void drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth, float graphheight,
//			float border, double minX, double minY, double diffX, double diffY,
//			float horstart, GraphViewSeriesStyle style) {
		float colwidth = (graphwidth - (2 * border)) / series.seriesLength;

		paint.setStrokeWidth(series.style.thickness);
		paint.setColor(series.style.color);

		// draw data
		for (int i = 0; i < series.seriesLength; i++) {
			float valY = (float) (series.yvalues[i] - minY);
			float ratY = (float) (valY / diffY);
			float y = graphheight * ratY;

			// hook for value dependent color
			if (series.style.getValueDependentColor() != null) {
				paint.setColor(series.style.getValueDependentColor().get(series.xvalues[i],series.yvalues[i]));
			}

			canvas.drawRect((i * colwidth) + horstart, (border - y) + graphheight, ((i * colwidth) + horstart) + (colwidth - 1), graphheight + border - 1, paint);
		}
	}
}
