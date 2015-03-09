package com.jjoe64.graphview.series;

/**
 * @author nick
 */
public interface AnimatedGraphInterface {


    public enum AnimationType {
        HORIZONTAL_ANIMATION,
        VERTICAL_ANIMATION,
        NONE
    }
    
    public boolean requiresRedraw();
    public void setAnimationType(AnimationType animationType);
    
}
