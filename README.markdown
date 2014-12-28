Chart and Graph Library for Android
====================================

<h2>What is this project?</h2>
This is an extension on <a href="https://github.com/jjoe64/GraphView">jjoe64's GraphView</a> (specifically the 3.1.3 version distributed on Gradle) that adds two new classes: AnimatedBarGraphView and LineBarGraph.

These new classes allow the user to have their graphs animated when shown. 


<h2>Features</h2>

Both bar graphs and line graphs will animate the loading of their data. 

<h4>Line Graphs</h4>

Line graphs animate from left to right by default.

![Line graph animation](public/lineFill.gif "Line Graph Animation")

<h4>Bar Graphs</h4>

Bar graphs can be animated in two different ways:

<h6>Fill all bars at once</h6>

![Fill all bars animation](public/fullBarsFill.gif?raw=true "Fill All Bars Animation")


<h6>Fill the bars one at a time</h6>

![One at a time animation](public/barByBarFill.gif?raw=truee "One at a Time Animation")


<h2>How to Use</h2>

Use the jjoe64's library like usual, except, instead of using BarGraphView or LineGraphView, use <b>AnimatedBarGraphView</b> and <b>AnimatedLineGraphView</b> respectively.

For AnimatedLineGraphView, that's it!

For AnimatedBarGraphView, there's some added functionality. You can set which version of animation you prefer by using ```setBarAnimationStyle(BarAnimationStyle)```

```java
AnimatedBarGraphView animatedBarGraphView = new AnimatedBarGraphView(context, "Graph Title!");

// For the bar-by-bar animation style
animatedBarGraphView.setBarAnimationStyle(BarAnimationStyle.BAR_AT_A_TIME);

// For the all bars animation style
animatedBarGraphView.setBarAnimationStyle(BarAnimationStyle.ALL_AT_ONCE);
```

I also added (for my own use) the ability to have the graph go beyond what's filled. For example, if you want your graph's x-axis to extend to 50, but only have x values up to 25, you can call 
```java
animatedBarGraphView.setMaxXSize(size);
```

