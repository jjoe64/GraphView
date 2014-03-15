package com.jjoe64.graphview;

public interface GraphViewOHLCDataInterface extends GraphViewDataInterface {
	public double getHigh();
    public double getLow();
    public double getOpen();
    public double getClose();
    public double getVolume();

}
