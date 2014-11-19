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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import de.ims.icarus.plugins.prosody.painte.PaIntEParams;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntECurve {

	public static final int DEFAULT_MAX_SAMPLE_COUNT = 50;
	public static final int DEFAULT_SAMPLE_DISTANCE = 2;
	public static final Color DEFAULT_COLOR = Color.black;
	public static final AntiAliasingType DEFAULT_ANTIALIASING_TYPE = AntiAliasingType.DEFAULT;
	public static final boolean DEFAULT_PAINT_COMPACT = false;
	public static final boolean DEFAULT_CLIP_CURVE = true;

	private int maxSampleCount = DEFAULT_MAX_SAMPLE_COUNT;
	private int sampleDistanc = DEFAULT_SAMPLE_DISTANCE;

	private AntiAliasingType antiAliasingType = DEFAULT_ANTIALIASING_TYPE;

	private Color color = DEFAULT_COLOR;
	private Stroke stroke;

	private boolean paintComapct = DEFAULT_PAINT_COMPACT;
	private boolean clipCurve = DEFAULT_CLIP_CURVE;

	public void paint(Graphics graphics, PaIntEParams params, Rectangle area, Axis xAxis, Axis yAxis) {

		final int x = area.x;
		final int y = area.y;

		Graphics2D g = (Graphics2D) graphics;

		final Color c = g.getColor();
		final Stroke s = g.getStroke();
		final Shape clip = g.getClip();

		g.setColor(color);
		if(stroke!=null) {
			g.setStroke(stroke);
		}

		if(clipCurve) {
			if(clip==null) {
				g.setClip(area);
			} else {
				Rectangle newClip = new Rectangle();
				Rectangle2D.intersect(area, clip.getBounds(), newClip);
				g.setClip(newClip);
			}
		}

		g.translate(x, y);

		antiAliasingType.apply(g);

		final double rangeX = xAxis.getRange();
		final double rangeY = yAxis.getRange();
		final double scaleX = area.width/rangeX;
		final double scaleY = area.height/rangeY;

		if(paintComapct) {
			// Only paint 2 lines

			//TODO fix calculation of peekY
			double peekX = (params.getB()-xAxis.getMinValue())*scaleX;
//			double peekY = (yAxis.getMaxValue()-params.getD())*scaleY;
			double peekY = area.height-(params.getD()-yAxis.getMinValue())*scaleY;

			double startY = area.height-(params.getD()-params.getC1()-yAxis.getMinValue())*scaleY;
			double endY = area.height-(params.getD()-params.getC2()-yAxis.getMinValue())*scaleY;

			g.drawLine(0, (int)startY, (int)peekX, (int)peekY);
			g.drawLine((int)peekX, (int)peekY, area.width, (int)endY);

		} else {
			// Paint complete curve

			// Number of samples
			final int stepCount = Math.min(area.width/sampleDistanc, maxSampleCount);
			// Distance between two sample points in the figure space
			final double stepSize = rangeX/stepCount;
			// Translation from figure space to drawing coordinates

//			System.out.printf("offsetX=%d offsetY=%d rangeX=%1.02f rangeY=%1.02f stepCount=%d stepSize=%1.02f scaleX=%1.02f scaleY=%1.02f\n",
//					offsetX, offsetY, rangeX, rangeY, stepCount, stepSize, scaleX, scaleY);

			double lastX = 0D;
			double lastY = 0D;

			for(int i=0; i<stepCount; i++) {

				// Raw coordinates
				double px = xAxis.getMinValue()+ (i * stepSize);
				double py = params.calc(px);

				// Translate according to figure constraints in a format suitable for user space
				px -= xAxis.getMinValue();
//				py = yAxis.getMaxValue()-py;
				py -= yAxis.getMinValue();

				// Translate to device coordinates
				px *= scaleX;
				py *= scaleY;

				py = area.height-py;

				if(i>0) {
					g.drawLine((int)lastX, (int)lastY, (int)px, (int)py);
				}

				lastX = px;
				lastY = py;
			}
		}

		g.translate(-x, -y);
		g.setClip(clip);
		g.setColor(c);
		g.setStroke(s);
	}

	public PaIntEHitBox translate(int x, int y, Rectangle area, PaIntEParams params, double accuracy, Axis xAxis, Axis yAxis) {
		final double vx = xAxis.getMinValue() + (double)x/area.width * xAxis.getRange();
		final double vy = params.calc(vx);

		final double dif = (yAxis.getMaxValue() - (double)y/area.height * yAxis.getRange()) - vy;

//		System.out.printf("p.x=%d p.y=%d x=%.02f y=%.02f dif=%.02f w=%d h=%d r.x=%.02f r.y=%.02f\n",
//				x, y, vx, vy, dif, area.width, area.height, xAxis.getRange(), yAxis.getRange());

		if(Math.abs(dif/yAxis.getRange())<=accuracy) {
			return new PaIntEHitBox(vx, vy);
		}

		return null;
	}

	/**
	 * @return the antiAliasingType
	 */
	public AntiAliasingType getAntiAliasingType() {
		return antiAliasingType;
	}

	/**
	 * @param antiAliasingType the antiAliasingType to set
	 */
	public void setAntiAliasingType(AntiAliasingType antiAliasingType) {
		if (antiAliasingType == null)
			throw new NullPointerException("Invalid antiAliasingType"); //$NON-NLS-1$

		this.antiAliasingType = antiAliasingType;
	}

	/**
	 * @return the maxSampleCount
	 */
	public int getMaxSampleCount() {
		return maxSampleCount;
	}

	/**
	 * @return the sampleDistanc
	 */
	public int getSampleDistanc() {
		return sampleDistanc;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @return the stroke
	 */
	public Stroke getStroke() {
		return stroke;
	}

	/**
	 * @param maxSampleCount the maxSampleCount to set
	 */
	public void setMaxSampleCount(int maxSampleCount) {
		this.maxSampleCount = maxSampleCount;
	}

	/**
	 * @param sampleDistanc the sampleDistanc to set
	 */
	public void setSampleDistanc(int sampleDistanc) {
		this.sampleDistanc = sampleDistanc;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(Color color) {
		if (color == null)
			throw new NullPointerException("Invalid color"); //$NON-NLS-1$

		this.color = color;
	}

	/**
	 * @param stroke the stroke to set
	 */
	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public boolean isPaintComapct() {
		return paintComapct;
	}

	public void setPaintComapct(boolean paintComapct) {
		this.paintComapct = paintComapct;
	}

	public boolean isClipCurve() {
		return clipCurve;
	}

	public void setClipCurve(boolean clipCurve) {
		this.clipCurve = clipCurve;
	}
}
