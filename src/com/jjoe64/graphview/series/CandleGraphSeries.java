package com.jjoe64.graphview.series;

import java.util.List;

import com.jjoe64.graphview.GraphViewOHLCDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.renderer.Renderer;

public class CandleGraphSeries <T extends GraphViewOHLCDataInterface> extends GraphViewSeries<T>{
	public CandleGraphSeries(List<T> values, Renderer<T> r) {
		super(values, r);
	}
	public CandleGraphSeries(String description,
			com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle style,
			List<T> values, Renderer<T> r) {
		super(description, style, values, r);
	}
	@Override
	public double getMaxX() {
		return super.getMaxX()+1;
	}
	@Override
	public double getMinX() {
		return super.getMinX()-1;
	}
	@Override
	public double getMinY(double minx, double sizex) {
		double smallest;
		smallest = Integer.MAX_VALUE;
		List<T> values = valuesToDraw(minx, sizex);
		for (int ii=0; ii<values.size(); ii++)
			if (values.get(ii).getLow() < smallest)
				smallest = values.get(ii).getLow();
		return smallest - 0.0001;
	}
	@Override
	public double getMinY() {
		double smallest;
		smallest = Integer.MAX_VALUE;
		List<T> values = valuesToDraw();
		for (int ii=0; ii<values.size(); ii++)
			if (values.get(ii).getLow() < smallest)
				smallest = values.get(ii).getLow();
		return smallest - 0.0001;
	}


	@Override
	public double getMaxY() {
		double largest;
		largest = Integer.MIN_VALUE;
	
		List<T> values = valuesToDraw();
		for (int ii=0; ii<values.size(); ii++)
			if (values.get(ii).getHigh() > largest)
				largest = values.get(ii).getHigh();
		
		return largest + 0.0001;
	}
	
	@Override
	public double getMaxY(double minx, double sizex) {
		double largest;
		largest = Integer.MIN_VALUE;
	
		List<T> values = valuesToDraw(minx,sizex);
		for (int ii=0; ii<values.size(); ii++)
			if (values.get(ii).getHigh() > largest)
				largest = values.get(ii).getHigh();
		
		return largest + 0.0001;
	}
}
