# Chart and Graph Library for Android

## What is GraphView

GraphView is a library for Android to programmatically create
flexible and nice-looking diagrams.
It is **easy** to understand, to integrate and to customize.

Supported graph types:
* Line Graphs
* Bar Graphs
* Point Graphs
* or implement your own custom types.

<img src="https://raw.githubusercontent.com/jjoe64/GraphView/master/anim.gif" title="Animation" />
<img src="https://raw.githubusercontent.com/jjoe64/GraphView/master/zooming.gif" title="Zooming, Scrolling" />
<img src="https://lh3.googleusercontent.com/v2Ga3ReduITLtP3QJCc8GhOhBHzpyoKiW8SoRh35ydPP45f05BoDN3vSDJxvWB9uY0RU=h900-rw" height="200" />
<img src="https://lh5.ggpht.com/29xLPw1BGGyyvUYgTsRt3E2rlGIH8lTbzYfnxuZJgNOT3hXy3Ggu4o3fPQZWw9u_aso=h900-rw" height="200" />
<img src="http://www.android-graphview.org/uploads/4/1/5/7/41578917/6509264_orig.png" height="200" />

## Top Features

* Line Chart, Bar Chart, Points
* Combination of different graph types
* Scrolling vertical and horizontal
. You can scroll with a finger touch move gesture.
* Scaling / Zooming vertical and horizontal
. With two-fingers touch scale gesture (Multi-touch), the viewport can be changed.
* Realtime Graph (Live change of data)
* Second scale axis
* Draw multiple series of data
. Let the diagram show more that one series in a graph. You can set a color and a description for every series.
* Show legend
. A legend can be displayed inline the chart. You can set the width and the vertical align (top, middle, bottom).
* Custom labels
. The labels for the x- and y-axis are generated automatically. But you can set your own labels, Strings are possible.
* Handle incomplete data
. It's possible to give the data in different frequency.
* Viewport
. You can limit the viewport so that only a part of the data will be displayed.
* Manual Y axis limits
* And much more... Check out the project page and/or the demo app

## How to use

1) Add gradle dependency:
```
compile 'com.jjoe64:graphview:4.2.1'
```

2) Add view to layout:
```
<com.jjoe64.graphview.GraphView
        android:layout_width="match_parent"
        android:layout_height="200dip"
        android:id="@+id/graph" />
```

3) Add some data:
```
GraphView graph = (GraphView) findViewById(R.id.graph);
LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
          new DataPoint(0, 1),
          new DataPoint(1, 5),
          new DataPoint(2, 3),
          new DataPoint(3, 2),
          new DataPoint(4, 6)
});
graph.addSeries(series);
```

## Download Demo project at Google Play Store

<img src="https://lh3.googleusercontent.com/yOrpE93m3N_Zr7kEUbnET6eFLGwmWA_a5kGa8XQcVrHeOse7K-zFN9s-gmwdRX3LN7A=w300-rw" width="64" /><br/>
<a href="https://play.google.com/store/apps/details?id=com.jjoe64.graphview_demos" title="GraphView Demos">Showcase GraphView Demo App</a>

## More examples and documentation

<a href="http://android-graphview.org">Get started at project homepage</a>

To show you how to integrate the library into an existing project see the GraphView-Demos project!
See GraphView-Demos for examples.
<a href="https://github.com/jjoe64/GraphView-Demos">https://github.com/jjoe64/GraphView-Demos<br/>
<a href="http://android-graphview.org">View GraphView page http://android-graphview.org</a>
