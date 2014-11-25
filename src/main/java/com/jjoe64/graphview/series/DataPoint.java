package com.jjoe64.graphview.series;

import android.provider.ContactsContract;

import java.util.Date;

/**
 * Created by jonas on 28.08.14.
 */
public class DataPoint implements DataPointInterface {
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
}
