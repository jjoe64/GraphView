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
import java.util.Collections;
import java.util.List;

import android.graphics.Canvas;

import com.jjoe64.graphview.model.GraphViewDataInterface;
import com.jjoe64.graphview.renderer.Renderer;

/**
 * a graphview series.
 * holds the data, description and styles
 */
public class GraphViewSeries <T extends GraphViewDataInterface>{
	/**
	 * graph series style: color and thickness
	 */
	static public class GraphViewSeriesStyle {
		public int color = 0xff0077cc;
		public int thickness = 3;
		private ValueDependentColor valueDependentColor;

		public GraphViewSeriesStyle() {
			super();
		}
		public GraphViewSeriesStyle(int color, int thickness) {
			super();
			this.color = color;
			this.thickness = thickness;
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
	}

	final String description;
	final GraphViewSeriesStyle style;
	private List<T> values;
	private final List<GraphView> graphViews = new ArrayList<GraphView>();

	public GraphViewSeries(List<T> values, Renderer<T> renderer) {
		description = null;
		style = new GraphViewSeriesStyle();
		this.values = values;
		this.renderer = renderer;
	}

	public GraphViewSeries(String description, GraphViewSeriesStyle style, List<T> values, Renderer<T> renderer) {
		super();
		this.description = description;
		if (style == null) {
			style = new GraphViewSeriesStyle();
		}
		this.style = style;
		this.values = values;
		this.renderer = renderer;
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
	public void appendData(T value, boolean scrollToEnd) {
		values.add(value);
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
	public void appendData(T value, boolean scrollToEnd, int maxDataCount) {
		synchronized (values) {
			int curDataCount = values.size();
			if (curDataCount < maxDataCount) {
				values.add(value);
			} else {
				values.remove(0);
				values.add(value);
			}
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
	void removeGraphView(GraphView graphView) {
		graphViews.remove(graphView);
	}

	/**
	 * clears the current data and set the new.
	 * redraws the graphview(s)
	 * @param values new data
	 */
	public void resetData(List<T> values) {
		this.values = values; 
		for (GraphView g : graphViews) {
			g.redrawAll();
		}
	}
	private Renderer<T> renderer;
	/**
	 * without viewport. return all values
	 * @return
	 */
	public List<T> valuesToDraw() {
		return Collections.unmodifiableList(values);
	}
	/**
	 * with X viewport
	 * @param minx
	 * @param sizex
	 * @return
	 */
	public List<T> valuesToDraw(double minx, double sizex) {
		return valuesToDraw(minx, sizex, 0, 0);
	}
	/**
	 * with XY viewport
	 * @param minx
	 * @param sizex
	 * @param miny
	 * @param sizey
	 * @return
	 */
	public List<T> valuesToDraw(double minx, double sizex, double miny, double sizey) {
		synchronized (values) {
			List<T> listData = new ArrayList<T>();
			boolean found = false;
			for (int i=0; i<values.size(); i++) {
				if ((values.get(i).getX() >= minx) && (values.get(i).getX() <= minx+sizex)) {
					// one before, for nice scrolling
					if (!found) {
						if (listData.isEmpty()) {
							listData.add(values.get(i));
						} else {
							listData.set(0, values.get(i));
						}
						found = true;
					}
					// append data
					listData.add(values.get(i));
				} else if (found) {
					// one more for nice scrolling
					listData.add(values.get(i)); 
					break;
				}
			}
			return Collections.unmodifiableList(listData);
		}
	}
	public double getMaxX() {
		double highest = 0;
		if (values.size() > 0) {
			highest = Math.max(highest, values.get(values.size()-1).getX());
		}
		return highest;
	}
	public double getMaxY() {
		double largest= Integer.MIN_VALUE;
		List<T> values = valuesToDraw();
		for (int ii=0; ii<values.size(); ii++) {
			if (values.get(ii).getY() > largest) {
				largest = values.get(ii).getY();
			}
		}
		return largest;
	}
	public double getMaxY(double minx, double sizex) {
		double largest= Integer.MIN_VALUE;
		List<T> values = valuesToDraw(minx, sizex);
		for (int ii=0; ii<values.size(); ii++) {
			if (values.get(ii).getY() > largest) {
				largest = values.get(ii).getY();
			}
		}
		return largest;
	}

	public double getMinX() {
		// 
		double lowest = 0;
		if (values.size() > 0) {
			lowest = values.get(0).getX();
		}
		return lowest;
	}
	
	public double getMinY() {
		double smallest;
		smallest = Integer.MAX_VALUE;
		List<T> values = valuesToDraw();
		for (int ii=0; ii<values.size(); ii++) {
			if (values.get(ii).getY() < smallest) {
				smallest = values.get(ii).getY();
			}
		}
		return smallest;
	}
	
	public double getMinY(double minx, double sizex) {
		double smallest;
		smallest = Integer.MAX_VALUE;
		List<T> values = valuesToDraw(minx, sizex);
		for (int ii=0; ii<values.size(); ii++) {
			if (values.get(ii).getY() < smallest) {
				smallest = values.get(ii).getY();
			}
		}
		return smallest;
	}
	public void drawSeries(Canvas canvas,
			float graphwidth, float graphheight, float border, double minX,
			double minY, double diffX, double diffY, float horstart) {
		renderer.drawSeries(canvas, valuesToDraw(), graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart, style);
	}

}
