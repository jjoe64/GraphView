/**
 * This file is part of GraphView.
 *
 * GraphView is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GraphView is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GraphView.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 *
 * Copyright Jonas Gehring
 */

package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.util.AttributeSet;

import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;

/**
 * Draws a Bar Chart
 * @author Muhammad Shahab Hameed
 */
public class BarGraphView extends GraphView {
	public BarGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BarGraphView(Context context, String title) {
		super(context, title);
	}

	@Override
	protected void drawHorizontalLabels(Canvas canvas, float border,
			float horstart, float height, String[] horlabels, float graphwidth) {
		// horizontal labels + lines
		paint.setTextAlign(Align.CENTER);

		int hors = horlabels.length;
		float barwidth = graphwidth/horlabels.length;
		float textOffset = barwidth/2;
		for (int i = 0; i < horlabels.length; i++) {
			// lines
			float x = ((graphwidth / hors) * i) + horstart;
			paint.setColor(graphViewStyle.getGridColor());
			canvas.drawLine(x, height - border, x, border, paint);

			// text
			x = barwidth*i + textOffset + horstart;
			paint.setColor(graphViewStyle.getHorizontalLabelsColor());
			canvas.drawText(horlabels[i], x, height - 4, paint);
		}
	}

	@Override
	public void drawSeries(Canvas canvas, GraphViewDataInterface[] values, float graphwidth, float graphheight,
			float border, double minX, double minY, double diffX, double diffY,
			float horstart, GraphViewSeriesStyle style) {
		float colwidth = graphwidth / (values.length);

		paint.setStrokeWidth(style.thickness);
		paint.setColor(style.color);

		float offset = 0;

		// draw data
		for (int i = 0; i < values.length; i++) {
			float valY = (float) (values[i].getY() - minY);
			float ratY = (float) (valY / diffY);
			float y = graphheight * ratY;

			// hook for value dependent color
			if (style.getValueDependentColor() != null) {
				paint.setColor(style.getValueDependentColor().get(values[i]));
			}

			canvas.drawRect((i * colwidth) + horstart -offset, (border - y) + graphheight, ((i * colwidth) + horstart) + (colwidth - 1) -offset, graphheight + border - 1, paint);
		}
	}

}
