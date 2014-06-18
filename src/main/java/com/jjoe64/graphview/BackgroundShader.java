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
 */

package com.jjoe64.graphview;

/**
 * Shades the background of the graph on a customizable basis.
 *
 * This can improve the readability of the graph by making it easier
 * to see related values in sequence as they're shaded together.
 *
 * For example, this trivial implementation would highlight only the even X values:
 * {@code
 * graphView.setShader(new BackgroundShader() {
 *  @Override public boolean shade(Double x) {
 *    if ( x.intValue() %2 ==0) {
 *      return true;
 *    } else {
 *      return false;
 *    }
 *  }
 * });
 * }
 *
 * Another more useful example would be to highlight the the weekend if
 * your X values were days of the week.
 */
public interface BackgroundShader {
	/**
	 * Will be called when the graph content is being rendered.
	 * @param x is the x value
	 * @return if true the value will be shaded
	 */
	boolean shade(double x);
}
