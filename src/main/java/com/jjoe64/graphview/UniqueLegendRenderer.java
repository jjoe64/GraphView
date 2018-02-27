package com.jjoe64.graphview;

import android.util.Pair;

import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A LegendRenderer that renders items with the same name and color only once in the legend
 * Created by poseidon on 27.02.18.
 */
public class UniqueLegendRenderer extends LegendRenderer {
    /**
     * creates legend renderer
     *
     * @param graphView regarding graphview
     */
    public UniqueLegendRenderer(GraphView graphView) {
        super(graphView);
    }

    @Override
    protected List<Series> getAllSeries() {
        List<Series> originalSeries = super.getAllSeries();
        List<Series> distinctSeries = new ArrayList<Series>();
        Set<Pair<Integer,String>> uniqueSeriesKeys = new HashSet<Pair<Integer,String>>();
        for(Series series : originalSeries)
            if(uniqueSeriesKeys.add(new Pair<Integer, String>(series.getColor(), series.getTitle())))
                distinctSeries.add(series);
        return distinctSeries;
    }
}
