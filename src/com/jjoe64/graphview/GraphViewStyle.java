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

import java.util.EnumSet;

import android.graphics.Color;
import android.graphics.Paint.Align;

/**
 * Styles for the GraphView Important: Use GraphViewSeries.GraphViewSeriesStyle
 * for series-specify styling
 * 
 */
public class GraphViewStyle {
	private int verticalLabelsColor;
	private int horizontalLabelsColor;
	private int gridColor;
	private float textSize = 30f;
	private int verticalLabelsWidth;
	private int numVerticalLabels;
	private int numHorizontalLabels;

	private Align verticalLabelAlign = Align.LEFT;
	// Constraints for setters
	private static EnumSet<Align> VERTICAL_ALIGN_CONTRAINT = EnumSet.of(
			Align.LEFT, Align.RIGHT);

	// private static final EnumSet<Align> VERTICAL_ALIGN_CONTRAINT =
	// EnumSet<Align>();
	public GraphViewStyle() {
		verticalLabelsColor = Color.WHITE;
		horizontalLabelsColor = Color.WHITE;
		gridColor = Color.DKGRAY;
	}

	public GraphViewStyle(int vLabelsColor, int hLabelsColor, int gridColor) {
		this.verticalLabelsColor = vLabelsColor;
		this.horizontalLabelsColor = hLabelsColor;
		this.gridColor = gridColor;
	}

	public int getGridColor() {
		return gridColor;
	}

	public int getHorizontalLabelsColor() {
		return horizontalLabelsColor;
	}

	public int getNumHorizontalLabels() {
		return numHorizontalLabels;
	}

	public int getNumVerticalLabels() {
		return numVerticalLabels;
	}

	public float getTextSize() {
		return textSize;
	}

	public int getVerticalLabelsColor() {
		return verticalLabelsColor;
	}

	public int getVerticalLabelsWidth() {
		return verticalLabelsWidth;
	}

	public void setGridColor(int c) {
		gridColor = c;
	}

	public void setHorizontalLabelsColor(int c) {
		horizontalLabelsColor = c;
	}

	/**
	 * @param numHorizontalLabels
	 *            0 = auto
	 */
	public void setNumHorizontalLabels(int numHorizontalLabels) {
		this.numHorizontalLabels = numHorizontalLabels;
	}

	/**
	 * @param numVerticalLabels
	 *            0 = auto
	 */
	public void setNumVerticalLabels(int numVerticalLabels) {
		this.numVerticalLabels = numVerticalLabels;
	}

	public void setTextSize(float textSize) {
		this.textSize = textSize;
	}

	public void setVerticalLabelsColor(int c) {
		verticalLabelsColor = c;
	}

	/**
	 * @param verticalLabelsWidth
	 *            0 = auto
	 */
	public void setVerticalLabelsWidth(int verticalLabelsWidth) {
		this.verticalLabelsWidth = verticalLabelsWidth;
	}

	/**
	 * Sets the Alignment of the vertical label
	 * 
	 * @param align
	 */
	public void setVerticalLabelAlignment(Align align) {
		if (!VERTICAL_ALIGN_CONTRAINT.contains(align)) {
			throw new IllegalArgumentException(
					"Vertical Label Alignment wrong:" + align + ", use Any of:"
							+ VERTICAL_ALIGN_CONTRAINT.toString());
		}
		verticalLabelAlign = align;
	}

	/**
	 * 
	 * @return {@link Align}
	 * @see #setVerticalLabelAlignment(Align)
	 */
	public Align getVericalAlignment() {
		return verticalLabelAlign;
	}
}
