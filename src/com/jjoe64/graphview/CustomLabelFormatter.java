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

import java.text.NumberFormat;

/**
 * if you want to show different labels,
 * you can use this label formatter.
 * As Input you get the raw value (x or y) and
 * you return a String that will be displayed.
 * {@code
 * 		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
			public String formatLabel(double value, boolean isValueX) {
				if (isValueX) {
					if (value < 5) {
						return "small";
					} else if (value < 15) {
						return "middle";
					} else {
						return "big";
					}
				}
				return null; // let graphview generate Y-axis label for us
			}
		});
 * }
 */
public interface CustomLabelFormatter {

	/**
	 * will be called when the labels were generated
	 * @param value the raw input value (x or y)
	 * @param isValueX true if value is a x-value, false if otherwise
	 * @param highestvalue the highest x or y value that is viewable
	 * @param lowestvalue the lowest x or y value that is viewable
	 * @return the string that will be displayed. return null if you want graphview to generate the label for you.
	 */
	boolean needsBounds(boolean isValueX);
	void clearBounds();
	void setBounds(double highestvalue, double lowestvalue, boolean isValueX);
	String formatLabel(double value, boolean isValueX);
	
	class Default implements CustomLabelFormatter {
		private final NumberFormat[] numberformatter = new NumberFormat[2];
		
		@Override
		public boolean needsBounds(boolean isValueX) {
			return numberformatter[isValueX ? 1 : 0] == null;
		}

		@Override
		public void clearBounds() {
			numberformatter[0] = null;
			numberformatter[1] = null;
		}

		@Override
		public void setBounds(double highestvalue, double lowestvalue,
				boolean isValueX) {
			int i = isValueX ? 1 : 0;
			numberformatter[i] = NumberFormat.getNumberInstance();
			if (highestvalue - lowestvalue < 0.1) {
				numberformatter[i].setMaximumFractionDigits(6);
			} else if (highestvalue - lowestvalue < 1) {
				numberformatter[i].setMaximumFractionDigits(4);
			} else if (highestvalue - lowestvalue < 20) {
				numberformatter[i].setMaximumFractionDigits(3);
			} else if (highestvalue - lowestvalue < 100) {
				numberformatter[i].setMaximumFractionDigits(1);
			} else {
				numberformatter[i].setMaximumFractionDigits(0);
			}
		}
		
		@Override
		public String formatLabel(double value, boolean isValueX) {
			return numberformatter[isValueX ? 1 : 0].format(value);
		}
	}
	
	class IntegerOnly implements CustomLabelFormatter {
		private final NumberFormat[] numberformatter = new NumberFormat[2];
		
		@Override
		public boolean needsBounds(boolean isValueX) {
			return numberformatter[isValueX ? 1 : 0] == null;
		}

		@Override
		public void clearBounds() {
			numberformatter[0] = null;
			numberformatter[1] = null;
		}

		@Override
		public void setBounds(double highestvalue, double lowestvalue,
				boolean isValueX) {
			int i = isValueX ? 1 : 0;
			numberformatter[i] = NumberFormat.getNumberInstance();
			numberformatter[i].setMaximumFractionDigits(0);
		}
		
		@Override
		public String formatLabel(double value, boolean isValueX) {
			return numberformatter[isValueX ? 1 : 0].format(value);
		}
	}
}
