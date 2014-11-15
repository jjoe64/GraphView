package com.jjoe64.graphview.compat;

import android.annotation.TargetApi;
import android.os.Build;
import android.widget.OverScroller;

/**
 * Created by jonas on 15.11.14.
 */
/**
 * A utility class for using {@link android.widget.OverScroller} in a backward-compatible fashion.
 */
public class OverScrollerCompat {
    /**
     * Disallow instantiation.
     */
    private OverScrollerCompat() {
    }
    /**
     * @see android.view.ScaleGestureDetector#getCurrentSpanY()
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static float getCurrVelocity(OverScroller overScroller) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return overScroller.getCurrVelocity();
        } else {
            return 0;
        }
    }
}