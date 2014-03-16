package com.jjoe64.graphview.renderer;

import java.util.List;

import android.graphics.Canvas;

import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.model.GraphViewDataInterface;

public interface Renderer<T extends GraphViewDataInterface> {
	public void drawSeries(Canvas canvas, 
			List<T> values, 
			float graphwidth, 
			float graphheight, 
			float border, 
			double minX, 
			double minY, 
			double diffX, 
			double diffY, 
			float horstart, 
			GraphViewSeriesStyle style);
	
}
