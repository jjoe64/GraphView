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

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * a graphview series.
 * holds the data, description and styles
 */
public class GraphViewSeries {
	/**
	 * graph series style: color and thickness
	 */
	static public class GraphViewSeriesStyle {
		public int color = 0xff0077cc;
		public int thickness = 3;
		private ValueDependentColor valueDependentColor;

		private final Paint paintBackground;
		private boolean drawBackground;

		public GraphViewSeriesStyle() {
			super();

			paintBackground = new Paint();
			paintBackground.setColor(Color.rgb(20, 40, 60));
			paintBackground.setStrokeWidth(4);
			paintBackground.setAlpha(128);
		}

		public GraphViewSeriesStyle(int color, int thickness) {
			super();
			this.color = color;
			this.thickness = thickness;

			paintBackground = new Paint();
			paintBackground.setColor(Color.rgb(20, 40, 60));
			paintBackground.setStrokeWidth(4);
			paintBackground.setAlpha(128);
		}
		
		public ValueDependentColor getValueDependentColor() {
			return valueDependentColor;
		}
		
		/**
		 * the color depends on the value of the data.
		 * only possible in BarGraphView
		 * @param valueDependentColor
		 */
		public void setValueDependentColor(ValueDependentColor valueDependentColor) {
			this.valueDependentColor = valueDependentColor;
		}

		public boolean getDrawBackground() {
			return drawBackground;
		}

		public void setDrawBackground(boolean drawBackground) {
			this.drawBackground = drawBackground;
		}

		public Paint getPaintBackground() {
			return paintBackground;
		}

		public int getBackgroundColor() {
			return paintBackground.getColor();
		}

		/**
		 * sets the background colour for the series. This is not the background
		 * colour of the whole graph.
		 */
		public void setBackgroundColor(int color) {
			paintBackground.setColor(color);
		}
	}

	final String description;
	final GraphViewSeriesStyle style;
	GraphViewDataInterface[] values;
	private final List<GraphView> graphViews = new ArrayList<GraphView>();

	public GraphViewSeries(GraphViewDataInterface[] values) {
		description = null;
		style = new GraphViewSeriesStyle();
		this.values = values;
	}

	public GraphViewSeries(String description, GraphViewSeriesStyle style, GraphViewDataInterface[] values) {
		super();
		this.description = description;
		if (style == null) {
			style = new GraphViewSeriesStyle();
		}
		this.style = style;
		this.values = values;
	}

	/**
	 * this graphview will be redrawn if data changes
	 * @param graphView
	 */
	public void addGraphView(GraphView graphView) {
		this.graphViews.add(graphView);
	}

	/**
	 * add one data to current data
	 * @param value the new data to append
	 * @param scrollToEnd true => graphview will scroll to the end (maxX)
	 * @deprecated please use {@link #appendData(GraphViewDataInterface, boolean, int)} to avoid memory overflow
	 */
	@Deprecated
	public void appendData(GraphViewDataInterface value, boolean scrollToEnd) {
		GraphViewDataInterface[] newValues = new GraphViewDataInterface[values.length + 1];
		int offset = values.length;
		System.arraycopy(values, 0, newValues, 0, offset);

		newValues[values.length] = value;
		values = newValues;
		for (GraphView g : graphViews) {
			if (scrollToEnd) {
				g.scrollToEnd();
			}
		}
	}

	/**
	 * add one data to current data
	 * @param value the new data to append
	 * @param scrollToEnd true => graphview will scroll to the end (maxX)
	 * @param maxDataCount if max data count is reached, the oldest data value will be lost
	 */
	public void appendData(GraphViewDataInterface value, boolean scrollToEnd, int maxDataCount) {
		synchronized (values) {
			int curDataCount = values.length;
			GraphViewDataInterface[] newValues;
			if (curDataCount < maxDataCount) {
				// enough space
				newValues = new GraphViewDataInterface[curDataCount + 1];
				System.arraycopy(values, 0, newValues, 0, curDataCount);
				// append new data
				newValues[curDataCount] = value;
			} else {
				// we have to trim one data
				newValues = new GraphViewDataInterface[maxDataCount];
				System.arraycopy(values, 1, newValues, 0, curDataCount-1);
				// append new data
				newValues[maxDataCount-1] = value;
			}
			values = newValues;
		}

		// update linked graph views
		for (GraphView g : graphViews) {
			if (scrollToEnd) {
				g.scrollToEnd();
			}
		}
	}

	/**
	 * @return series styles. never null
	 */
	public GraphViewSeriesStyle getStyle() {
		return style;
	}

	/**
	 * you should use {@link GraphView#removeSeries(GraphViewSeries)}
	 * @param graphView
	 */
	public void removeGraphView(GraphView graphView) {
		graphViews.remove(graphView);
	}

	/**
	 * clears the current data and set the new.
	 * redraws the graphview(s)
	 * @param values new data
	 */
	public void resetData(GraphViewDataInterface[] values) {
		this.values = values;
		for (GraphView g : graphViews) {
			g.redrawAll();
		}
	}
}
