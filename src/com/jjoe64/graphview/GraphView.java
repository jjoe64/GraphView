package com.jjoe64.graphview;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;

/**
 * GraphView is a Android View for creating zoomable and scrollable graphs.
 * This is the abstract base class for all graphs. Extend this class and implement {@link #drawSeries(Canvas, GraphViewData[], float, float, float, double, double, double, double, float)} to display a custom graph.
 * Use {@link LineGraphView} for creating a line chart.
 *
 * @author jjoe64 - jonas gehring - http://www.jjoe64.com
 *
 * Copyright (C) 2011 Jonas Gehring
 * Licensed under the GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Modifications by OttoES Jan 2013
 *   Changed data array storage to allow faster access
 *   Removed views within a linear layout to a single view.
 * Modifications by OttoES 2 Feb 2013
 *   Changed scrolling
 *   Changed pinch gesture
 * Modifications by OttoES 17 Feb 2013
 *   Added zoom in y direction
 */
abstract public class GraphView extends View  {
	static final private class GraphViewConfig {
		static final float BORDER = 30;
		static final float LEFT_BORDER = 40;
		static final float VERTICAL_LABEL_WIDTH = 100;
		static final float HORIZONTAL_LABEL_HEIGHT = 80;
	}

		private float graphwidth;
		
		protected final Paint paint;
		private String[] horlabels;
		private String[] verlabels;
		private String title;
		private boolean scrollable;
		private float viewportStart;
		private float viewportSize;
		private boolean scalable;
		private boolean yScalable = true;
		private final NumberFormat[] numberformatter = new NumberFormat[2];
		private final List<GraphViewSeries> graphSeries;
		private boolean showLegend = false;
		private float legendWidth = 120;
		private LegendAlign legendAlign = LegendAlign.MIDDLE;
		private boolean manualYAxis;
		private float manualMaxYValue;
		private float manualMinYValue;
		private GraphViewStyle graphViewStyle;

		/**
		 *
		 * @param context
		 * @param title [optional]
		 */
		public GraphView(Context context, String title) {
			super(context);
			if (title == null)
				title = "";
			else
				this.title = title;
			graphViewStyle = new GraphViewStyle();
			paint = new Paint();
			graphSeries = new ArrayList<GraphViewSeries>();
		} // end GraphView()

				
		/**
		 * @param canvas
		 */
		@Override
		protected void onDraw(Canvas canvas) {
            paint.setAntiAlias(true);
			// normal
			paint.setStrokeWidth(0);
			float border = GraphViewConfig.BORDER;
			float horstart = GraphViewConfig.LEFT_BORDER;
			float height = getHeight();
			float width = getWidth() - 1-horstart;
			float maxY = getMaxY();
			float minY = getMinY();
			float maxX = getMaxX(false);
			float minX = getMinX(false);
			float diffX = maxX - minX;
			float graphheight = height - (2 * border);
			graphwidth = width-border;

			if (horlabels == null) {
				horlabels = generateHorlabels(graphwidth);
			}
			if (verlabels == null) {
				verlabels = generateVerlabels(graphheight);
			}

			// vertical lines
			paint.setTextAlign(Align.LEFT);
			int vers = verlabels.length - 1;
			for (int i = 0; i < verlabels.length; i++) {
				paint.setColor(graphViewStyle.getGridColor());
				float y = ((graphheight / vers) * i) + border;
				canvas.drawLine(horstart, y, width, y, paint);
			} // for i

			// horizontal labels + lines
			int hors = horlabels.length - 1;
			for (int i = 0; i < horlabels.length; i++) {
				paint.setColor(graphViewStyle.getGridColor());
				float x = ((graphwidth / hors) * i) + horstart;
				canvas.drawLine(x, height - border, x, border, paint);
				paint.setTextAlign(Align.CENTER);
				if (i==horlabels.length-1)
					paint.setTextAlign(Align.RIGHT);
				if (i==0)
					paint.setTextAlign(Align.LEFT);
				paint.setColor(graphViewStyle.getHorizontalLabelsColor());
				canvas.drawText(horlabels[i], x, height - 4, paint);
			} // for i

			paint.setTextAlign(Align.CENTER);
			canvas.drawText(title, (graphwidth / 2) + horstart, border - 4, paint);

			if (maxY == minY) {
				// if min and max is the same, fake it so that we can render a line
				if (maxY == 0f)  {
					maxY = 1f;
					minX = -1.0f;
				} else {
					maxY = maxY*1.05f;
					minY = minY*0.95f;
				}
			} // if maxY==minY

			if (showLegend) 
				drawLegend(canvas, height, width);
			drawVertLables(canvas);
			
			double diffY = maxY - minY;
			paint.setStrokeCap(Paint.Cap.ROUND);
			
			// set the active area of the view to clip the lines outside the area
			canvas.clipRect(horstart, border, horstart+graphwidth,border+ graphheight);

			for (int i=0; i<graphSeries.size(); i++) {
				drawSeries(canvas, graphSeries.get(i), graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart);
			}

		} // end onDraw

		protected void drawLegend(Canvas canvas, float height, float width) {
			int shapeSize = 15;

			// rect
			paint.setARGB(180, 100, 100, 100);
			float legendHeight = (shapeSize+5)*graphSeries.size() +5;
			float lLeft = width-legendWidth - 10;
			float lTop;
			switch (legendAlign) {
			case TOP:
				lTop = 10;
				break;
			case MIDDLE:
				lTop = height/2 - legendHeight/2;
				break;
			default:
				lTop = height - GraphViewConfig.BORDER - legendHeight -10;
			} // switch
			float lRight = lLeft+legendWidth;
			float lBottom = lTop+legendHeight;
			canvas.drawRoundRect(new RectF(lLeft, lTop, lRight, lBottom), 8, 8, paint);

			for (int i=0; i<graphSeries.size(); i++) {
				paint.setColor(graphSeries.get(i).style.color);
				canvas.drawRect(new RectF(lLeft+5, lTop+5+(i*(shapeSize+5)), lLeft+5+shapeSize, lTop+((i+1)*(shapeSize+5))), paint);
				if (graphSeries.get(i).description != null) {
					paint.setColor(Color.WHITE);
					paint.setTextAlign(Align.LEFT);
					canvas.drawText(graphSeries.get(i).description, lLeft+5+shapeSize+5, lTop+shapeSize+(i*(shapeSize+5)), paint);
				}
			} // for i
		}

		abstract public void drawSeries(Canvas canvas, GraphViewSeries series, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart);

		/**
		 * For compatibility with the old function only. Will be deprecated. 
		 */
	    public void drawSeries(Canvas canvas, GraphViewSeries series, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart, GraphViewSeriesStyle style)
	    {
		   drawSeries(canvas,series, graphwidth,graphheight,border,minX, minY,diffX,diffY, horstart);
	    } // end

		
		public void redrawAll() {
			verlabels = null;
			horlabels = null;
			numberformatter[0] = null;
			numberformatter[1] = null;
			invalidate();
		} // end redrawAll

		
		
		
		private void onMoveGesture(float f) {
			// view port update
			
			if (viewportSize != 0) {
				float dx=  (f*viewportSize/graphwidth);
				//if (Math.abs(dx)< 0.20f) {
				//	graphScrollingBusy = false;
				//}
				viewportStart -= dx;
				Log.w("MOVE", "d= "+ Float.toString(f) + "  start= "+Float.toString((float) viewportStart));

				// minimal and maximal view limit
				float minX = getMinX(true);
				float maxX = getMaxX(true);
				if (viewportStart < minX) {
					viewportStart = minX;
					speedAnimDx = 0f;
				} else if (viewportStart+viewportSize > maxX) {
					viewportStart = maxX - viewportSize;
					// cancel any scrolling if busy
//					graphScrollingBusy = false;
					speedAnimDx = 0f;
				}

				// labels have to be regenerated
				horlabels = null;
				verlabels = null;
			}
			invalidate();
		} // end onMoveGesture


	public enum LegendAlign {
		TOP, MIDDLE, BOTTOM
	}

		/**
		 * @param canvas
		 */
	protected void drawVertLables(Canvas canvas) {
			// normal
			paint.setStrokeWidth(0);

			float border = GraphViewConfig.BORDER;
			float height = getHeight();
			float graphheight = height - (2 * border);

			if (verlabels == null) {
				verlabels = generateVerlabels(graphheight);
			}

			// vertical labels
			paint.setTextAlign(Align.LEFT);
			int vers = verlabels.length - 1;
			for (int i = 0; i < verlabels.length; i++) {
				float y = ((graphheight / vers) * i) + border;
				paint.setColor(graphViewStyle.getVerticalLabelsColor());
				canvas.drawText(verlabels[i], 0, y, paint);
			}
		}  // end


	public GraphViewStyle getGraphViewStyle() {
		return graphViewStyle;
	}

	public void setGraphViewStyle(GraphViewStyle style) {
		graphViewStyle = style;
	}

	public void addSeries(GraphViewSeries series) {
		series.addGraphView(this);
		graphSeries.add(series);
	}

	/**
	 * formats the label
	 * can be overwritten
	 * @param value x and y values
	 * @param isValueX if false, value y wants to be formatted
	 * @return value to display
	 */
	protected String formatLabel(double value, boolean isValueX) {
		int i = isValueX ? 1 : 0;
		if (numberformatter[i] == null) {
			numberformatter[i] = NumberFormat.getNumberInstance();
			double highestvalue = isValueX ? getMaxX(false) : getMaxY();
			double lowestvalue = isValueX ? getMinX(false) : getMinY();
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
		return numberformatter[i].format(value);
	}

	private String[] generateHorlabels(float graphwidth) {
		int numLabels = (int) (graphwidth/GraphViewConfig.VERTICAL_LABEL_WIDTH);
		String[] labels = new String[numLabels+1];
		double min = getMinX(false);
		float max = getMaxX(false);
		for (int i=0; i<=numLabels; i++) {
			labels[i] = formatLabel(min + ((max-min)*i/numLabels), true);
		}
		return labels;
	}

	synchronized private String[] generateVerlabels(float graphheight) {
		int numLabels = (int) (graphheight/GraphViewConfig.HORIZONTAL_LABEL_HEIGHT);
		String[] labels = new String[numLabels+1];
		double min = getMinY();
		double max = getMaxY();
		if (max == min) {
			// if min/max is the same, fake it so that we can render a line
			max = max*1.05d;
			min = min*0.95d;
		}

		for (int i=0; i<=numLabels; i++) {
			labels[numLabels-i] = formatLabel(min + ((max-min)*i/numLabels), false);
		}
		return labels;
	}

	public LegendAlign getLegendAlign() {
		return legendAlign;
	}

	public float getLegendWidth() {
		return legendWidth;
	}

	/**
	 * returns the maximal X value of the current viewport (if viewport is set)
	 * otherwise maximal X value of all data.
	 * @param ignoreViewport
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected float getMaxX(boolean ignoreViewport) {
		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart+viewportSize;
		} else {
			// otherwise use the max x value
			float highest = Float.MIN_VALUE;
			for (int i=0; i<graphSeries.size(); i++) {
				float mm = graphSeries.get(i).getMaxX();
				if (mm>highest) highest = mm;
			} // for
			return highest;
		} // else
	} // end

	/**
	 * returns the maximal Y value of all data.
	 *
	 * warning: only override this, if you really know what you're doing!
	 */
	protected float getMaxY() {
		float largest;
		if (manualYAxis) {
			return manualMaxYValue;
		} else {
			largest = Float.MIN_VALUE;
			for (int i=0; i<graphSeries.size(); i++) {
				float mm = graphSeries.get(i).getMaxY();
				if (mm>largest) largest = mm;
			} // for i
		}
		return largest;
	}

	/**
	 * returns the minimal X value of the current viewport (if viewport is set)
	 * otherwise minimal X value of all data.
	 * @param ignoreViewport
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected float getMinX(boolean ignoreViewport) {
		// if viewport is set, use this
		if (!ignoreViewport && viewportSize != 0) {
			return viewportStart;
		} else {
			// otherwise use the min x value
			float lowest = Float.MAX_VALUE;
			for (int i=0; i<graphSeries.size(); i++) {
				float mm = graphSeries.get(i).getMinX();
				if (mm<lowest) lowest = mm;
			} // for
			return lowest;
		} // else
	}

	/**
	 * returns the minimal Y value of all data.
	 *
	 * warning: only override this, if you really know want you're doing!
	 */
	protected float getMinY() {
		float smallest;
		if (manualYAxis) {
			return manualMinYValue;
		} else {
	    	smallest = Float.MAX_VALUE;
			for (int i=0; i<graphSeries.size(); i++) {
				float mm = graphSeries.get(i).getMinY();
				if (mm<smallest) smallest = mm;
			} // for
			
		} // else
		return smallest;
	}

	public boolean isScrollable() {
		return scrollable;
	}

	public boolean isShowLegend() {
		return showLegend;
	}

	public void removeSeries(GraphViewSeries series)
	{
		graphSeries.remove(series);
	}

	public void removeSeries(int index)
	{
		if (index < 0 || index >= graphSeries.size())	{
			throw new IndexOutOfBoundsException("No series at index " + index);
		}
		graphSeries.remove(index);
	}

	public void scrollToEnd() {
		float max = getMaxX(true);
		viewportStart = max-viewportSize;
		redrawAll();
	}

	public void scrollToStart() {
		viewportStart = getMinX(true);
		redrawAll();
	}

	
	
	/**
	 * set's static horizontal labels (from left to right)
	 * @param horlabels if null, labels were generated automatically
	 */
	public void setHorizontalLabels(String[] horlabels) {
		this.horlabels = horlabels;
	}

	public void setLegendAlign(LegendAlign legendAlign) {
		this.legendAlign = legendAlign;
	}

	public void setLegendWidth(float legendWidth) {
		this.legendWidth = legendWidth;
	}

	/**
	 * you have to set the bounds {@link #setManualYAxisBounds(double, double)}. That automatically enables manualYAxis-flag.
	 * if you want to disable the menual y axis, call this method with false.
	 * @param manualYAxis
	 */
	public void setManualYAxis(boolean manualYAxis) {
		this.manualYAxis = manualYAxis;
	}

	/**
	 * set manual Y axis limit
	 * @param max
	 * @param min
	 */
	public void setManualYAxisBounds(double max, double min) {
		manualMaxYValue = (float) max;
		manualMinYValue = (float) min;
		manualYAxis = true;
	}

	
	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	/**
	 * set's static vertical labels (from top to bottom)
	 * @param verlabels if null, labels were generated automatically
	 */
	public void setVerticalLabels(String[] verlabels) {
		this.verlabels = verlabels;
	}

	/**
	 * set's the viewport for the graph.
	 * @param start x-value
	 * @param size
	 */
	public void setViewPort(double start, double size) {
		viewportStart = (float) start;
		viewportSize = (float) size;
	}
	
	
    // TOUCH events functions =========================================================== 	
	// these values keep track of the touch.
	private float lastTouchEventX;
	private float touchDownX1,touchDownY1;
    private float touchDownX2,touchDownY2;
    private float graphDownY1,graphDownY2;
    
    private float refViewportSize = 1f;   // viewport size at start of zoom
    private float refYviewportSize = 1f;   // viewport size in Y direction at start of zoom
//    private float 
	private float moveDist;
	
	
	// OttoES: The original gesture detectors are preferable but it does not quite work out
	// if you want to use different methods to scroll horizontal (a single graph)
	// and vertically (vertical scroll must be passed on so that you can scroll vertically if the 
	// graph is embedded in a vertical scroller)
	// A very customized gesture detector is therefore implemented.
    private float   speedAnimDx = 0f;
    private boolean graphScrollingBusy = false;
    
    private GestureDetector gestureDetector;
	/**
	 * this forces scrollable = true
	 * 
	 * @param scalable
	 */
	
	synchronized public void setScalable(boolean scalable) {
		this.scalable = scalable;
		// at this point scrollable must be on to scale
		if (scalable) setScrollable(true);
	}

	/**
	 * the user can scroll (horizontal) the graph. This is only useful if you use a viewport {@link #setViewPort(double, double)} which doesn't displays all data.
	 * @param scrollable
	 */
	public void setScrollable(boolean scrollable) {
		this.scrollable = scrollable;
		if (scrollable) 
	      gestureDetector = new android.view.GestureDetector(this.getContext(),this.new GestureListener());
		else gestureDetector = null;
	}

    public void onAnimateMove(float dx, float dy, long duration) {
    	speedAnimDx = dx;
//        startTime = System.currentTimeMillis();
        post(new Runnable() {
            @Override
            public void run() {
                onAnimateStep();
            }
        });
    } // end
    
    
    private void onAnimateStep() {
        // this test is to detect when the user touched 
    	// the screen while a fling animation was still in progress
    	// the animation will then be stopped
    	if (graphScrollingBusy == false) return;
 //   	long curTime = System.currentTimeMillis();
 //       float percentTime = (float) (curTime - startTime)
 //               / (float) (endTime - startTime);
//        float percentDistance = animateInterpolator.getInterpolation(percentTime);
//        float curDy = percentDistance * totalAnimDy;
        speedAnimDx = speedAnimDx*0.97f; 
        onMove(speedAnimDx, 0);

        //Log.v(DEBUG_TAG, "We're " + percentDistance + " of the way there!");
        if (Math.abs(speedAnimDx) > viewportSize/100f) {  // stop when % of screen movement small
            post(new Runnable() {
                @Override
                public void run() {
                    onAnimateStep();
                }
            });
        }
    } // end onAnimateStep

    public void onMove(float dx, float dy) {
	    moveDist = dx;
		Log.v("MOVE", "dx= "+Float.toString(moveDist)+ " dy= "+Float.toString(dy));
		onMoveGesture(moveDist);
//        invalidate();
    }

    public void onResetLocation() {
        lastTouchEventX = touchDownX1;
	    moveDist=0;
        invalidate();
    }

    
    /**
     * 
     * @param newViewportSize  the new size of the viewport
     * @param zoomCentreX      the centre around which to zoom in pixels from left
     * @param scrollX          the distance in pixels to scroll in the X direction
     */
	public void onScaleGraph(float newViewportSize, float zoomCentreX, float scrollX) {
		float border = GraphViewConfig.BORDER;
		float horstart = GraphViewConfig.LEFT_BORDER;
//		float height = getHeight();
		float width = getWidth() - 1-horstart;
//		double maxY = getMaxY();
//		double minY = getMinY();
//		double maxX = getMaxX(false);
//		double minX = getMinX(false);
//		double diffX = maxX - minX;
//		float graphheight = height - (2 * border);
		graphwidth = width-border;
		if (newViewportSize != 0f) {
			// recalculate the zoom centre position
			float proportion = (zoomCentreX-horstart)/(graphwidth);
			if (proportion > 1f) proportion = 1f;
			if (proportion < 0f) proportion = 0f;
			float zoomPointX = (float) (viewportStart) + (float)viewportSize *proportion ;
			viewportStart = zoomPointX - newViewportSize * proportion + 
					        scrollX  *viewportSize/graphwidth;
			viewportSize = newViewportSize;
	    } // if

		Log.w("SCALE", "size= "+ Float.toString((float)viewportSize) + "  start= "+Float.toString((float) viewportStart));

		// viewportStart must not be < minX
		float minX = getMinX(true);
		if (viewportStart < minX) {
			viewportStart = minX;
		}

		// viewportStart + viewportSize must not be > maxX
		float maxX = getMaxX(true);
		float overlap = viewportStart + viewportSize - maxX;
		if (overlap > 0) {
			// scroll left
			if (viewportStart-overlap > minX) {
				viewportStart -= overlap;
			} else {
				// maximal scale
				viewportStart = minX;
				viewportSize = maxX - viewportStart;
			}
		}
        invalidate();
	} // end onScale
    
	
	private float mapYCoordScr2Graph(float screenY) 
	{
		float height = getHeight();
	    float sdiff = height - (2 * GraphViewConfig.BORDER);
		float maxY  = getMaxY();
		float minY  = getMinY();
	    float rdiff = maxY-minY;
	    float rel   = (screenY-GraphViewConfig.BORDER)/ sdiff;
	    return maxY - rdiff*rel;
	} // end mapY
	
	// keep track of the state of the touch events
    enum TouchState_t { TS_NONE, TS_SCROLL,TS_FLING,TS_ZOOM,TS_PASS_TO_NEXT,TS_CANCEL };
    TouchState_t    touchState = TouchState_t.TS_NONE;
    // TODO:
    // last time a fling took place - to be used to determine fly wheel effect on fling
	long lastFlingTime = 0;
    
	/**
			// the touch have 3 different functions depending on the action
			// 1) can be a page scroll if the movement is vertical > 50 px
			// 2) if movement horizontal - pan graph
			// 3) pinch will zoom
	 * 
	 */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
  		switch (motionEvent.getActionMasked()) { 
		case  MotionEvent.ACTION_DOWN:
			// stop the parent from hijacking the event - happens when embedded in vertical scroller
			getParent().requestDisallowInterceptTouchEvent(true);
			touchState = TouchState_t.TS_SCROLL;   // assume scroll state on first touch
			graphScrollingBusy = false;   // have to stop any current fling animation
			
			touchDownX1 = motionEvent.getX();
		    touchDownY1 = motionEvent.getY();
		    lastTouchEventX = touchDownX1;
		    moveDist=0;
			Log.v("DOWN", "onTouch up"+Float.toString(moveDist));
		    break;
		case MotionEvent.ACTION_POINTER_DOWN:
			if (this.scrollable == false) break;
			touchState = TouchState_t.TS_ZOOM;  // a second touch event - this is a pinch action
			touchDownX1 = motionEvent.getX(0);
			touchDownX2 = motionEvent.getX(1);
		    touchDownY1 = motionEvent.getY(0);
			touchDownY2 = motionEvent.getY(1);
	        //if (touchx>touchDownX2) { float t = touchx; touchx = touchDownX2; touchDownX2 = t;  }
			refViewportSize = (float) viewportSize;
			refYviewportSize = (float) getMaxY() - getMinY();
			graphDownY1 = mapYCoordScr2Graph(touchDownY1);
			graphDownY2 = mapYCoordScr2Graph(touchDownY2);
			Log.v("DOWN", "onTouch pointer down x1= "+Float.toString(touchDownX1)+ "x2= " +Float.toString(touchDownX2));
			// store the 2nd finger position
			break;
		case MotionEvent.ACTION_POINTER_UP:
			touchState = TouchState_t.TS_CANCEL;  // a second touch stopped
			Log.v("DOWN", "onTouch pointer up");
			break;
		case MotionEvent.ACTION_MOVE:{
			float x = motionEvent.getX();
		    moveDist= x-lastTouchEventX;
		    lastTouchEventX = x;
		    if (touchState == TouchState_t.TS_SCROLL) {
			    // check if it is a vertical movement (and not in zoom mode)
			    // if it is, pass the event on to the next view
			    float y =  motionEvent.getY();
			    float dy = y-touchDownY1;
				if ((dy> 50) || (dy< -50) ) { // give up touch control to parent
					getParent().requestDisallowInterceptTouchEvent(false);
					touchState = TouchState_t.TS_PASS_TO_NEXT;
					Log.v("MOVE", "give up control");
					return false;					
				} // if
				Log.v("MOVE", "dx= "+Float.toString(moveDist));
		    } // if touchState
		    else if (touchState == TouchState_t.TS_ZOOM) {
			    if (motionEvent.getPointerCount() < 2) break;
		    	// zoom in the y direction if enabled
		    	if (this.yScalable) {
		    		// scale so that the touch positions stay at the same graph coordinates
			    	float y1=0f,y2=0f;
	    			y1 = motionEvent.getY(0);
	    			y2 = motionEvent.getY(1);
	    			//float dd = Math.abs(y2-y1); 
	    			//if (dd < 2f) dd = 1f; ;  // difference is to small and we risk overflow below
	    			float height  = getHeight();
	    		    float sdiff   = height - (2 * GraphViewConfig.BORDER);
	    			float rel1    = (y1-GraphViewConfig.BORDER)/sdiff;
	    			float rdiff   = (graphDownY1-graphDownY2)*sdiff/(y2-y1);
	    			float newmaxy =  graphDownY1 + rdiff* rel1;
	    			float newminy =  -rdiff + newmaxy;
			        setManualYAxisBounds(newmaxy,newminy);
		    	} // if yScaleable
		    	// zooming is done manually and not using the scale gesture detector
		    	float x1=0f,x2=0f;
    			x1 = motionEvent.getX(0);
    			x2 = motionEvent.getX(1);
    			float dd = Math.abs(x2-x1); 
    			if (dd < 2f) break;  // difference is to small and we risk overflow below
		        float  scale =  Math.abs(touchDownX2-touchDownX1)/dd ; 
		        onScaleGraph(refViewportSize*scale,(x1+x2)/2,-moveDist);
	            Log.d("ZOOM:"," x= "+ Float.toString(x1) + " x2= "+ Float.toString(x2) );
		    } // else if
		    break;}
		case MotionEvent.ACTION_UP: {
			Log.v("UP", "onTouch up"+Float.toString(moveDist));
		    break; }		
		case MotionEvent.ACTION_CANCEL: {
			touchState = TouchState_t.TS_CANCEL;
		    break; }		
		default:
		    Log.v("default", "event= "+Integer.toString(motionEvent.getAction()));
		} // switch
		if (gestureDetector != null)	return gestureDetector.onTouchEvent(motionEvent);
		return false;
	} // onTouch    
	
	
	private class GestureListener 
	implements GestureDetector.OnGestureListener,	GestureDetector.OnDoubleTapListener  
	{
        String  DEBUG_TAG = "GESTURE";
		@Override
		public boolean onDown(MotionEvent e) {
			Log.v(DEBUG_TAG, "onDown");
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2,
				final float velocityX, final float velocityY) {
			//Log.v(DEBUG_TAG, "onFling");
			final float distanceTimeFactor = 0.4f;
			final float totalDx = (distanceTimeFactor * velocityX / 20);
			final float totalDy = (distanceTimeFactor * velocityY / 20);
			graphScrollingBusy = true;

			onAnimateMove(totalDx, totalDy,
					(long) (1000 *3 * distanceTimeFactor));
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Log.v(DEBUG_TAG, "onDoubleTap");
//			view.onResetLocation();
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			Log.v(DEBUG_TAG, "onLongPress");
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			//Log.v(DEBUG_TAG, "onScroll");
			graphScrollingBusy = true;  
			onMove(-distanceX, -distanceY);
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			Log.v(DEBUG_TAG, "onShowPress");
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Log.v(DEBUG_TAG, "onSingleTapUp");
			return false;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			Log.v(DEBUG_TAG, "onDoubleTapEvent");
			scrollToStart();
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.v(DEBUG_TAG, "onSingleTapConfirmed");
			return false;
		}

	} // end class GestureListener

	
	
} // end class
