package com.jjoe64.graphview;

import android.graphics.Color;

/**
 * The Class ReferenceLine.
 */
public class ReferenceLine {

	/**
	 * The Enum LabelAlign.
	 */
	public enum LabelAlign {

		/** The top align. */
		TOP,
		/** The middle align. */
		MIDDLE,
		/** The bottom align. */
		BOTTOM
	}
	
	/** The value. */
	private Float value;
	
	/** The label. */
	private String label;
	
	/** The align. */
	private LabelAlign align = LabelAlign.TOP;

	private int color = Color.RED;
	
	/**
	 * Instantiates a new reference line.
	 *
	 * @param value the value
	 */
	public ReferenceLine(Float value) {
		this(value, null, null);
	}
	
	/**
	 * Instantiates a new reference line.
	 *
	 * @param value the value
	 * @param label the label
	 */
	public ReferenceLine(Float value, String label) {
		this(value, label, null);
	}
	
	/**
	 * Instantiates a new reference line.
	 *
	 * @param value the value
	 * @param label the label
	 * @param align the align
	 */
	public ReferenceLine(Float value, String label, LabelAlign align) {
		if (value != null)
			setValue(value);
		if (label != null) 
			setLabel(label);
		if (align != null) 
			setAlign(align);
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public Float getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the value to set
	 */
	public void setValue(Float value) {
		this.value = value;
		if (this.value != null) 
			label = String.valueOf(this.value);
	}

	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the align.
	 *
	 * @return the align
	 */
	public LabelAlign getAlign() {
		return align;
	}

	/**
	 * Sets the align.
	 *
	 * @param align the align to set
	 */
	public void setAlign(LabelAlign align) {
		this.align = align;
	}

	/**
	 * @return the color
	 */
	public int getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(int color) {
		this.color = color;
	}
	
}
