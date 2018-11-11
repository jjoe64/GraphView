/**
 * GraphView
 * Copyright 2016 Jonas Gehring
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jjoe64.graphview.series;

import android.provider.ContactsContract;

import java.io.Serializable;
import java.util.Date;

/**
 * default data point implementation.
 * This stores the x and y values.
 *
 * @author jjoe64
 */
public class DataPoint implements DataPointInterface, Serializable {
    private static final long serialVersionUID=1428263322645L;

    private double x;
    private double y;

    public DataPoint(double x, double y) {
        this.x=x;
        this.y=y;
    }

    public DataPoint(Date x, double y) {
        this.x = x.getTime();
        this.y = y;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "["+x+"/"+y+"]";
    }
    
    /**
     * Sorts an array of DataPoints in-place by their x-values using the quicksort algorithm
     * <p>
     * DataPoints must be sorted by x-value prior to insertion into a Graph
     * @param points The array of DataPoints to sort
     */
    public static void sort(DataPoint[] points)
    {
        sortDataPointsHelper(points, 0, points.length);
    }

    /**
     * Recursive quicksort helper method
     * @param points The array of points to sort
     * @param lowIdx The low index of the array segment
     * @param highIdx The high index of the array segment
     */
    private static void sortDataPointsHelper(DataPoint[] points, int lowIdx, int highIdx)
    {
        int partitionIdx;

        if (lowIdx < highIdx)
        {
            partitionIdx = partitionDataPoints(points, lowIdx, highIdx);
            sortDataPointsHelper(points, lowIdx, partitionIdx - 1);
            sortDataPointsHelper(points, partitionIdx + 1, highIdx);
        }
    }

    /**
     * Partitions array and sorts based on pivot element
     * @param points The array of points to sort
     * @param lowIdx The low index of the array segment
     * @param highIdx The high index of the array segment
     */
    private static int partitionDataPoints(DataPoint[] points, int lowIdx, int highIdx)
    {
        int pivotIdx = (highIdx + lowIdx) / 2;
        DataPoint pivot = points[pivotIdx];
        DataPoint swapPoint;

        int iterIdx;
        int swapIdx = lowIdx - 1;

        for (iterIdx = lowIdx; iterIdx < highIdx; iterIdx++)
        {
            if (points[iterIdx].getX() < pivot.getX() && swapIdx != iterIdx)
            {
                swapIdx++;
                swapPoint = points[swapIdx];
                points[swapIdx] = points[iterIdx];
                points[iterIdx] = swapPoint;
            }
        }
        swapIdx++;
        points[pivotIdx] = points[swapIdx];
        points[swapIdx] = pivot;
        pivotIdx = swapIdx;

        return pivotIdx;
    }
}
