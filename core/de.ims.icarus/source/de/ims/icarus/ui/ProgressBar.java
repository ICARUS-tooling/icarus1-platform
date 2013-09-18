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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProgressBar extends Component {

	private static final long serialVersionUID = -3747918011324445106L;
	
	private int minValue = 0;
	private int maxValue = 100;
	private int value = 0;
	
	private double progress = 0d;
	
	private boolean indeterminate = false;
	
	private Color barColor = Color.BLUE;
	private Color borderColor = Color.BLACK;

	/**
	 * 
	 */
	public ProgressBar() {
		this(new Dimension(100, 20));
	}

	public ProgressBar(Dimension size) {
		setMinimumSize(size);
		setPreferredSize(size);
	}

	/**
	 * Creates a new progress bar with the min- and max-value
	 * set to the provided arguments and an initial value of
	 * {@code 0}.
	 * @param minValue
	 * @param maxValue
	 */
	public ProgressBar(int minValue, int maxValue) {
		this();
		
		setMinValue(minValue);
		setMaxValue(maxValue);
	}
	
	@Override
	public void paint(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		
		g.setColor(getBorderColor());
		g.fillRect(0, 0, width, height);
		
		g.setColor(getBackground());
		g.fillRect(1, 1, width-2, height-2);
		
		if(indeterminate) {
			g.setColor(Color.RED);
			int x = 1;
			int w = 5;
			while(x<width-1) {
				int x2 = Math.min(x + w, width-1);
				g.fillRect(x, 1, x2-1, height-2);
				x = x2+w;
			}
		} else {
			int fill = (int) ((width-2) * progress);
			
			if(fill>0) {
				g.setColor(getBarColor());
				g.fillRect(1, 1, fill, height-2);
			}
		}
	}
	
	private void recalcProgress() {	
		double min = minValue;
		double progress = (value-min) / (maxValue-min);

		progress = Math.min(progress, 1d);
		
		// restrict precision to 3 digits
		progress = Math.floor(1000*progress) * 0.001;
		
		if(this.progress!=progress) {
			this.progress = progress;
			repaint();
		}
	}

	/**
	 * @return the minValue
	 */
	public int getMinValue() {
		return minValue;
	}

	/**
	 * @param value the minValue to set
	 */
	public void setMinValue(int value) {
		if(value>=maxValue)
			throw new IllegalArgumentException("minValue too large"); //$NON-NLS-1$
		
		int oldValue = minValue;
		minValue = value;
		firePropertyChange("minValue", oldValue, value); //$NON-NLS-1$
		
		recalcProgress();
	}

	/**
	 * @return the maxValue
	 */
	public int getMaxValue() {
		return maxValue;
	}

	/**
	 * @param value the maxValue to set
	 */
	public void setMaxValue(int value) {
		if(value<=minValue)
			throw new IllegalArgumentException("maxValue too small"); //$NON-NLS-1$
		
		int oldValue = maxValue;
		maxValue = value;
		firePropertyChange("maxValue", oldValue, value); //$NON-NLS-1$
		
		recalcProgress();
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		if(value>maxValue || value<minValue)
			throw new IllegalArgumentException("value out of range"); //$NON-NLS-1$
		
		int oldValue = this.value;
		this.value = value;
		firePropertyChange("value", oldValue, value); //$NON-NLS-1$
		
		recalcProgress();
	}
	
	public void step() {
		step(1);
	}
	
	public void step(int amount) {
		setValue(Math.min(getValue()+amount, getMaxValue()));
	}

	/**
	 * @return the barColor
	 */
	public Color getBarColor() {
		return barColor;
	}

	/**
	 * @param value the barColor to set
	 */
	public void setBarColor(Color value) {
		if(value==null)
			throw new IllegalArgumentException("invalid barColor"); //$NON-NLS-1$
		
		Color oldValue = barColor;
		barColor = value;
		firePropertyChange("barColor", oldValue, value); //$NON-NLS-1$
		
		if(!value.equals(oldValue))
			repaint();
	}

	/**
	 * @return the borderColor
	 */
	public Color getBorderColor() {
		return borderColor;
	}

	/**
	 * @param value the borderColor to set
	 */
	public void setBorderColor(Color value) {
		if(value==null)
			throw new IllegalArgumentException("invalid borderColor"); //$NON-NLS-1$
		
		Color oldValue = borderColor;
		borderColor = value;
		firePropertyChange("borderColor", oldValue, value); //$NON-NLS-1$
		
		if(!value.equals(oldValue))
			repaint();
	}

	public boolean isIndeterminate() {
		return indeterminate;
	}

	public void setIndeterminate(boolean indeterminate) {
		this.indeterminate = indeterminate;
	}

}
