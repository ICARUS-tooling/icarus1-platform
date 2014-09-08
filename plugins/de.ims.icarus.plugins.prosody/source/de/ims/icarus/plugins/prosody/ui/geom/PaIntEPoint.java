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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntEPoint {

	private double x, y;

	private Axis axis;

	public PaIntEPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public PaIntEPoint(double x, double y, Axis axis) {
		this(x, y);

		this.axis = axis;
	}

	public PaIntEPoint(PaIntEPoint source) {
		x = source.x;
		y = source.y;
		axis = source.axis;
	}

	public PaIntEPoint() {
		// no-op
	}

	@Override
	public PaIntEPoint clone() {
		return new PaIntEPoint(this);
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @return the axis
	 */
	public Axis getAxis() {
		return axis;
	}

	public double getAxisValue() {
		return axis.isVertical() ? y : x;
	}

	/**
	 * @param axis the axis to set
	 */
	public void setAxis(Axis axis) {
		this.axis = axis;
	}
}
