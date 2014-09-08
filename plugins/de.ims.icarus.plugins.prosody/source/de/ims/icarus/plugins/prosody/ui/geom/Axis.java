/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.prosody.ui.geom;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class Axis {
	public static final int DEFAULT_LABEL_TO_MARKER_DISTANCE = 4;
	public static final int DEFAULT_MARKER_HEIGHT = 3;
	public static final String DEFAULT_DUMMY_LABEL = "XXXX"; //$NON-NLS-1$
	public static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 11); //$NON-NLS-1$
	public static final Color DEFAULT_AXIS_COLOR = Color.black;
	public static final Color DEFAULT_MARKER_COLOR = Color.red;
	public static final Color DEFAULT_LABEL_COLOR = Color.blue;
	public static final boolean DEFAULT_LABEL_VERTICAL = false;

	private final boolean vertical;

	private boolean labelVertical = DEFAULT_LABEL_VERTICAL;

	private double minValue;
	private double maxValue;
	// Value space between markers
	private double markerStepSize;
	// Number of markers between labels
	private int labelStepSize;

	private int markerHeight = DEFAULT_MARKER_HEIGHT;
	private int labelToMarkerDistance = DEFAULT_LABEL_TO_MARKER_DISTANCE;
	private String dummyLabel = DEFAULT_DUMMY_LABEL;

	private Font labelFont = DEFAULT_FONT;
	private transient Font labelFont90;

	private Color axisColor = DEFAULT_AXIS_COLOR;
	private Color markerColor = DEFAULT_MARKER_COLOR;
	private Color labelColor = DEFAULT_LABEL_COLOR;

	private boolean paintLabels = true;

	private static final AffineTransform ROTATE_90 = new AffineTransform();
	static {
		ROTATE_90.rotate(-Math.PI / 2);
	}

	/**
	 * Creates a horizontal axis
	 */
	protected Axis() {
		this(false);
	}

	/**
	 * Creates an axis with the specified orientation ({@code true} for vertical)
	 * @param vertical
	 */
	protected Axis(boolean vertical) {
		this.vertical = vertical;
	}

	private Font getRotatedFont() {
		if(labelFont90==null) {
			labelFont90 = labelFont.deriveFont(ROTATE_90);
		}
		return labelFont90;
	}

	/**
	 * @return the paintLabels
	 */
	public boolean isPaintLabels() {
		return paintLabels;
	}

	/**
	 * @param paintLabels the paintLabels to set
	 */
	public void setPaintLabels(boolean paintLabels) {
		this.paintLabels = paintLabels;
	}

	/**
	 * @return the labelFont
	 */
	public Font getLabelFont() {
		return labelFont;
	}

	/**
	 * @param labelFont the labelFont to set
	 */
	public void setLabelFont(Font labelFont) {
		if (labelFont == null)
			throw new NullPointerException("Invalid labelFont"); //$NON-NLS-1$

		this.labelFont = labelFont;
		labelFont90 = null;
	}

	/**
	 * @return the markerHeight
	 */
	public int getMarkerHeight() {
		return markerHeight;
	}

	/**
	 * @return the labelToMarkerDistance
	 */
	public int getLabelToMarkerDistance() {
		return labelToMarkerDistance;
	}

	/**
	 * @return the dummyLabel
	 */
	public String getDummyLabel() {
		return dummyLabel;
	}

	/**
	 * @param markerHeight the markerHeight to set
	 */
	public void setMarkerHeight(int markerHeight) {
		this.markerHeight = markerHeight;
	}

	/**
	 * @param labelToMarkerDistance the labelToMarkerDistance to set
	 */
	public void setLabelToMarkerDistance(int labelToMarkerDistance) {
		this.labelToMarkerDistance = labelToMarkerDistance;
	}

	/**
	 * @param dummyLabel the dummyLabel to set
	 */
	public void setDummyLabel(String dummyLabel) {
		if (dummyLabel == null)
			throw new NullPointerException("Invalid dummyLabel"); //$NON-NLS-1$
		if(dummyLabel.isEmpty())
			throw new IllegalArgumentException("Dummy label must not be empty"); //$NON-NLS-1$

		this.dummyLabel = dummyLabel;
	}

	/**
	 * @return the axisColor
	 */
	public Color getAxisColor() {
		return axisColor;
	}

	/**
	 * @return the markerColor
	 */
	public Color getMarkerColor() {
		return markerColor;
	}

	/**
	 * @return the labelColor
	 */
	public Color getLabelColor() {
		return labelColor;
	}

	/**
	 * @param axisColor the axisColor to set
	 */
	public void setAxisColor(Color axisColor) {
		if (axisColor == null)
			throw new NullPointerException("Invalid axisColor"); //$NON-NLS-1$

		this.axisColor = axisColor;
	}

	/**
	 * @param markerColor the markerColor to set
	 */
	public void setMarkerColor(Color markerColor) {
		if (markerColor == null)
			throw new NullPointerException("Invalid markerColor"); //$NON-NLS-1$

		this.markerColor = markerColor;
	}

	/**
	 * @param labelColor the labelColor to set
	 */
	public void setLabelColor(Color labelColor) {
		if (labelColor == null)
			throw new NullPointerException("Invalid labelColor"); //$NON-NLS-1$

		this.labelColor = labelColor;
	}

	public double getRange() {
		return maxValue-minValue;
	}

	/**
	 * @return the vertical
	 */
	public boolean isVertical() {
		return vertical;
	}

	/**
	 * @return the labelVertical
	 */
	public boolean isLabelVertical() {
		return labelVertical;
	}

	/**
	 * @return the minValue
	 */
	public double getMinValue() {
		return minValue;
	}

	/**
	 * @return the maxValue
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * @return the markerStepSize
	 */
	public double getMarkerStepSize() {
		return markerStepSize;
	}

	/**
	 * @return the labelStepSize
	 */
	public int getLabelStepSize() {
		return labelStepSize;
	}

	/**
	 * @param labelVertical the labelVertical to set
	 */
	public void setLabelVertical(boolean labelVertical) {
		this.labelVertical = labelVertical;
	}

	/**
	 * @param labelStepSize the labelStepSize to set
	 */
	public void setLabelStepSize(int labelStepSize) {
		this.labelStepSize = labelStepSize;
	}

	public int getRequiredWidth(Graphics g) {
		if(!vertical) {
			return -1;
		}

		int width = markerHeight;

		if(paintLabels) {
			width += labelToMarkerDistance;

			FontMetrics fm = g.getFontMetrics(labelFont);

			if(labelVertical) {
				width += fm.getHeight();
			} else {
				width += fm.stringWidth(dummyLabel);
			}
		}

		return width;
	}

	public int getRequiredHeight(Graphics g) {
		if(vertical) {
			return -1;
		}

		int height = markerHeight;

		if(paintLabels) {
			height += labelToMarkerDistance;

			FontMetrics fm = g.getFontMetrics(labelFont);

			if(labelVertical) {
				height += fm.stringWidth(dummyLabel);
			} else {
				height += fm.getHeight();
			}
		}

		return height;
	}

	public void paint(Graphics graphics, Rectangle area) {

		final int x = area.x;
		final int y = area.y;
		final int w = area.width;
		final int h = area.height;

		Graphics2D g = (Graphics2D) graphics;

		final Color c = g.getColor();
		final Stroke s = g.getStroke();
		final Font f = g.getFont();

		g.translate(x, y);

		FontMetrics fm = g.getFontMetrics(labelFont);

		if(labelVertical) {
			g.setFont(getRotatedFont());
		} else {
			g.setFont(labelFont);
		}

		if(vertical) {
			double scale = h/getRange();

			g.setColor(axisColor);
			g.drawLine(w, y, w, y+h);

			int cursor = h;
			int marker = 0;
			while(cursor>=0) {

				// Draw marker
				g.setColor(markerColor);
				g.drawLine(w-markerHeight, cursor, w, cursor);

				// Draw label (skip the first possible marker)
				if(paintLabels && (marker-1)%labelStepSize==0) {
					String label = getLabel(marker);
					int sw = fm.stringWidth(label);

					g.setColor(labelColor);

					if(labelVertical) {
						int xl = w-labelToMarkerDistance-markerHeight;
						int yl = cursor + sw/2;

						if(yl-sw>0) {
							g.drawString(label, xl, yl);
						}
					} else {
						int xl = w-labelStepSize-markerHeight-sw;
						int yl = cursor + fm.getAscent()/2 - 1;

						if(yl-fm.getHeight()>0) {
							g.drawString(label, xl, yl);
						}
					}
				}

				cursor -= (int) (markerStepSize*scale);
				marker++;
			}
		} else {
			double scale = w/getRange();

			g.setColor(axisColor);
			g.drawLine(0, 0, w, 0);

			int cursor = 0;
			int markerCount = 0;
			while(cursor<=w) {

				// Draw marker
				g.setColor(markerColor);
				g.drawLine(cursor, 0, cursor, markerHeight);

				// Draw label (skip the first possible marker)
				if(paintLabels && (markerCount-1)%labelStepSize==0) {
					String label = getLabel(markerCount);
					int sw = fm.stringWidth(label);

					g.setColor(labelColor);

					if(labelVertical) {
						int xl = cursor + fm.getDescent() + 1;
						int yl = labelToMarkerDistance + markerHeight + sw;

						if(xl<=w && xl-fm.getHeight()>=0) {
							g.drawString(label, xl, yl);
						}
					} else {
						int xl = cursor - sw/2;
						int yl = labelToMarkerDistance + markerHeight + fm.getAscent();

						if(xl>=0 && xl+sw<=w) {
							g.drawString(label, xl, yl);
						}
					}
				}

				cursor += (int) (markerStepSize*scale);
				markerCount++;
			}
		} // end vertical

		g.setColor(c);
		g.setStroke(s);
		g.setFont(f);
		g.translate(-x, -y);
	}

	/**
	 * Returns the relative location of the marker at position {@code index}.
	 * The first marker (with index 0) is at the left most location in case the
	 * axis is horizontal, and at the bottom most location if it is vertical.
	 * Returned values are in the range 0 to 1, spanning the respective axis of
	 * the paint area with 0 located at the top or left border, depending on axis
	 * orientation.
	 *
	 * @param marker
	 * @return
	 */
	public double getMarkerLocation(int index) {
		double value = (index * markerStepSize) / (maxValue-minValue);
		if(vertical) {
			value = 1-value;
		}
		return value;
	}

	public int getMarkerCount() {
		return (int) Math.floor((maxValue-minValue) / markerStepSize);
	}

	public double translate(int value, int range) {
		return getMinValue() + ((double)value/range * getRange());
	}

	public abstract String getLabel(int marker);

	protected void setMinValue0(double minValue) {
		this.minValue = minValue;
	}

	protected void setMaxValue0(double maxValue) {
		this.maxValue = maxValue;
	}

	protected void setMarkerStepSize0(double markerStepSize) {
		this.markerStepSize = markerStepSize;
	}

	public static class Integer extends Axis {

		public Integer() {
			// no-op
		}

		public Integer(boolean vertical) {
			super(vertical);
		}

		public Integer(boolean vertical, int minValue, int maxValue, int markerStepSize) {
			this(vertical);

			setMinValue(minValue);
			setMaxValue(maxValue);
			setMarkerStepSize(markerStepSize);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.ui.geom.Axis#getLabel(int)
		 */
		@Override
		public String getLabel(int marker) {
			int value = (int)getMinValue() + (marker * (int)getMarkerStepSize());
			return String.valueOf(value);
		}

		/**
		 * @param minValue the minValue to set
		 */
		public void setMinValue(int minValue) {
			setMinValue0(minValue);
		}

		/**
		 * @param maxValue the maxValue to set
		 */
		public void setMaxValue(int maxValue) {
			setMaxValue0(maxValue);
		}

		/**
		 * @param markerStepSize the markerStepSize to set
		 */
		public void setMarkerStepSize(int markerStepSize) {
			setMarkerStepSize0(markerStepSize);
		}

	}

	public static class Float extends Axis {

		public Float() {
			// no-op
		}

		public Float(boolean vertical) {
			super(vertical);
		}

		public Float(boolean vertical, float minValue, float maxValue, float markerStepSize) {
			this(vertical);

			setMinValue(minValue);
			setMaxValue(maxValue);
			setMarkerStepSize(markerStepSize);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.ui.geom.Axis#getLabel(int)
		 */
		@Override
		public String getLabel(int marker) {
			float value = (float)getMinValue() + (marker * (float)getMarkerStepSize());
			return String.valueOf(value);
		}

		/**
		 * @param minValue the minValue to set
		 */
		public void setMinValue(float minValue) {
			setMinValue0(minValue);
		}

		/**
		 * @param maxValue the maxValue to set
		 */
		public void setMaxValue(float maxValue) {
			setMaxValue0(maxValue);
		}

		/**
		 * @param markerStepSize the markerStepSize to set
		 */
		public void setMarkerStepSize(float markerStepSize) {
			setMarkerStepSize0(markerStepSize);
		}

	}

	public static class Double extends Axis {

		public Double() {
			// no-op
		}

		public Double(boolean vertical) {
			super(vertical);
		}

		public Double(boolean vertical, double minValue, double maxValue, double markerStepSize) {
			this(vertical);

			setMinValue(minValue);
			setMaxValue(maxValue);
			setMarkerStepSize(markerStepSize);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.ui.geom.Axis#getLabel(int)
		 */
		@Override
		public String getLabel(int marker) {
			double value = getMinValue() + (marker * getMarkerStepSize());
			return String.valueOf(value);
		}

		/**
		 * @param minValue the minValue to set
		 */
		public void setMinValue(double minValue) {
			setMinValue0(minValue);
		}

		/**
		 * @param maxValue the maxValue to set
		 */
		public void setMaxValue(double maxValue) {
			setMaxValue0(maxValue);
		}

		/**
		 * @param markerStepSize the markerStepSize to set
		 */
		public void setMarkerStepSize(double markerStepSize) {
			setMarkerStepSize0(markerStepSize);
		}

	}
}
