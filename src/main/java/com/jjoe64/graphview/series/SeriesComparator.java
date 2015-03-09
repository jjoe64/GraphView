package com.jjoe64.graphview.series;

import java.util.Comparator;

/**
 * Comparator to sort Series so that the bar graphs are drawn first (behind) other graphs.
 *
 * @author nt-complete
 */
public class SeriesComparator implements Comparator<Series> {


    @Override
    public int compare(Series baseSeries, Series baseSeries2) {

        if(baseSeries instanceof BarGraphSeries) {
            if (baseSeries2 instanceof BarGraphSeries) {
                return 0;
            }
            return -1;
        }

        if(baseSeries2 instanceof BarGraphSeries) {
            return 1;
        }
        return 0;

    }
}