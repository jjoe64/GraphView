package com.jjoe64.graphview;

import android.graphics.Color;
import android.graphics.Typeface;

/**
 * Styles for the GraphView
 * Important: Use GraphViewSeries.GraphViewSeriesStyle for series-specify styling
 *
 */
public class GraphViewStyle {
	private int vLabelsColor;
	private int hLabelsColor;
	private int gridColor;
	private float vLabelsFontSize;
	private float hLabelsFontSize;
	private Typeface labelsTypeFace;

	public GraphViewStyle() {
		vLabelsColor = Color.WHITE;
		hLabelsColor = Color.WHITE;
		gridColor = Color.DKGRAY;
		vLabelsFontSize = 12f;
		hLabelsFontSize = 12f;
		labelsTypeFace = null;
	}

	public GraphViewStyle(int vLabelsColor, int hLabelsColor, int gridColor) {
		this();
		this.vLabelsColor = vLabelsColor;
		this.hLabelsColor = hLabelsColor;
		this.gridColor = gridColor;
	}

	public int getVerticalLabelsColor() {
		return vLabelsColor;
	}

	public int getHorizontalLabelsColor() {
		return hLabelsColor;
	}

	public int getGridColor() {
		return gridColor;
	}
	
	public float getHorizontalLabelsFontSize() {
		return hLabelsFontSize;
	}
	
	public float getVerticalLabelsFontSize() {
		return vLabelsFontSize;
	}
	
	public Typeface getLabelsTypeFace() {
		return labelsTypeFace;
	}

	public void setVerticalLabelsColor(int c) {
		vLabelsColor = c;
	}

	public void setHorizontalLabelsColor(int c) {
		hLabelsColor = c;
	}

	public void setGridColor(int c) {
		gridColor = c;
	}

	public void setVerticalLabelsFontSize(float fs) {
		this.vLabelsFontSize = fs;
	}

	public void setHorizontalLabelsFontSize(float fs) {
		this.hLabelsFontSize = fs;
	}

	public void setLabelsTypeFace(Typeface labelsTypeFace) {
		this.labelsTypeFace = labelsTypeFace;
	}
}
