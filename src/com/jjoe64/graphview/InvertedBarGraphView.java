package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;

import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;

/**
 * Draws a Inverted Bar Chart
 * @author Camilo Montes
 */
public class InvertedBarGraphView extends GraphView {
	public InvertedBarGraphView(Context context, String title) {
		super(context, title);
	}

	@Override
	public void drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth, float graphheight,
			float border, double minX, double minY, double diffX, double diffY,
			float horstart, GraphViewSeriesStyle style) {
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

			canvas.drawRect((i * colwidth) + horstart, border, ((i * colwidth) + horstart) + (colwidth - 1), y+border, paint);
		}
	}
	
	@Override
	protected synchronized String[] generateVerlabels(float graphheight) {
		String[] standarVLabels = super.generateVerlabels(graphheight);
		String[] invertedVLabels =  new String[standarVLabels.length];
		
		for(int i = 0; i < invertedVLabels.length; i ++) {
			invertedVLabels[i] = standarVLabels[(standarVLabels.length-1) - i];
		}
		
		return invertedVLabels;
		
	}
}
