/**
 * Modifications using float array for better real-time data performance
 * Otto 2013
*/
 
package com.jjoe64.graphview;

import java.util.ArrayList;
import java.util.List;

public class GraphViewSeries {
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
		public void setValueDependentColor(ValueDependentColor valueDependentColor) {
			this.valueDependentColor = valueDependentColor;
		}
		public ValueDependentColor getValueDependentColor() {
			return valueDependentColor;
		}
	}

	final String description;
	final GraphViewSeriesStyle style;
	// Change by Otto:
	// The following change was made specifically for fast updating of 
	// realtime graphs with high sampling rates (>10Hz)
	// Graphs with static data are not going to benefit much from these changes unless 
	// you have a very large data set (10 000's of points)
	// Data used to be stored as x,y coordinates in an object and an 
	// array of objects was used to store all data points.
	// Changed to store data in float arrays.
	// Float arrays have the following advantages compared to the previous implementation:
	// 1) uses significantly less storage space
	// 2) should be faster (not sure how much without benchmarking)
	// 3) less garbage collection to be done
	// 4) less memory fragmentation
	// 5) faster to add data points to the existing data
	// The new structure also changes the way the data storage is allocated for appending data
	// to the existing data.
	// The data arrays will now be 'oversized' when single points are added/appended.
	// The number of values to display in the array will therefore differ from the size.
	// The result is that there might already be space in the array when new values are 
	// added, the new value can be added directly without copying all the elements 
	// in the array to a newly allocated array of the new size.
	// If there is no space a new array object is created with extra space for future use.
	float[]  xvalues,yvalues;
	// this is the number of valid graph points in the series. Do not use  
	// the array length since the array might contain some invalid data at the end.
	int      seriesLength = 0;   
	
	private final List<GraphView> graphViews = new ArrayList<GraphView>();

	public GraphViewSeries(float[] xvalues,float[] yvalues) {
		super();
		description = null;
		style = new GraphViewSeriesStyle();
		this.xvalues = xvalues;
		this.yvalues = yvalues;
		seriesLength = xvalues.length;
	}

	public GraphViewSeries(String description, GraphViewSeriesStyle style, float[] xvalues,float[] yvalues) {
		super();
		this.description = description;
		if (style == null) {
			style = new GraphViewSeriesStyle();
		}
		this.style = style;
		this.xvalues = xvalues;
		this.yvalues = yvalues;
		seriesLength = xvalues.length;
	}

	/**
	 * Get the minimum x value. It is assumed that the 
	 * smallest x value is the first value in the series.  
	 * @param 
	 * @return  largest value
	 */
	public float getMinX() {
		if (xvalues == null) return 0.0f;
		return xvalues[0];
	}
	/**
	 * Get the maximum x value. It is assumed that the 
	 * largest x value is the last value in the series.  
	 * @param 
	 * @return  largest value
	 */
	public float getMaxX() {
		if (xvalues == null) return 10.0f;
		return xvalues[seriesLength-1];
	}

	/**
	 * Get the minimum y value. 
	 * @param 
	 * @return  largest value
	 */
	public float getMinY() {
		if (yvalues == null) return -1.0f;
		float smallest = Float.MAX_VALUE;
		for (int i=0; i<seriesLength; i++) {
			if (yvalues[i]<smallest) smallest = yvalues[i];
		} // for i		
		return smallest;
	}
	/**
	 * Get the maximum y value.
	 * @param 
	 * @return  largest value
	 */
	public float getMaxY() {
		if (yvalues == null) return 1.0f;
		float largest = Float.MIN_VALUE;
		for (int i=0; i<seriesLength; i++) {
			if (yvalues[i]>largest) largest = yvalues[i];
		} // for i		
		return largest;
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
	 */
	public void appendData( float xvalue,float yvalue, boolean scrollToEnd) {
		// check if all space in array already in use?
        if (xvalues.length <= seriesLength) {
            // allocate new array
        	float[] newx = new float[seriesLength+50];
        	// copy to new memory pos
        	System.arraycopy(xvalues, 0, newx, 0, seriesLength);
        	xvalues = newx;
        } // if
    	xvalues[seriesLength] = xvalue;

		// check if all space in array already in use?
        if (yvalues.length <= seriesLength) {
            // allocate new array
        	float[] newy = new float[seriesLength+50];
        	// copy to new memory pos
        	System.arraycopy(yvalues, 0, newy, 0, seriesLength);
        	yvalues = newy;
        } // if
    	yvalues[seriesLength] = yvalue;
    	
    	seriesLength ++;
                
		if (scrollToEnd) {
		    for (GraphView g : graphViews) {
				g.scrollToEnd();
			}
		} // if
	} // end

	/**
	 * clears the current data and set the new.
	 * redraws the graphview(s)
	 * @param values new data
	 */
	public void resetData( float[] xvalues,float[] yvalues, int len) {
		this.xvalues = xvalues;
		this.yvalues = yvalues;
		if (len <= xvalues.length)
		   seriesLength = len;
		else seriesLength = xvalues.length;
		for (GraphView g : graphViews) {
			g.redrawAll();
		}
	}
}
