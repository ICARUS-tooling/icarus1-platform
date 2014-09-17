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

import de.ims.icarus.plugins.prosody.params.PaIntEParams;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntEHitBox {

	private double x, y;

	private int wordIndex = -1, sylIndex = -1;

	private Axis axis;

	private PaIntEParams params;

	private Type type;

	public PaIntEHitBox(double x, double y) {
		this.x = x;
		this.y = y;
		type = Type.CURVE;
	}

	public PaIntEHitBox(double x, double y, Axis axis) {
		this(x, y);

		this.axis = axis;
		type = Type.AXIS;
	}

	public PaIntEHitBox(PaIntEParams params) {
		this.params = params;
		type = Type.GRAPH;
	}

	public PaIntEHitBox(PaIntEHitBox source) {
		x = source.x;
		y = source.y;
		axis = source.axis;
		wordIndex = source.wordIndex;
		sylIndex = source.sylIndex;
		type = source.type;
	}

	public PaIntEHitBox(int wordIndex) {
		this.wordIndex = wordIndex;
		type = Type.WORD_LABEL;
	}

	public PaIntEHitBox(int wordIndex, int sylIndex) {
		this.wordIndex = wordIndex;
		this.sylIndex = sylIndex;
		type = Type.SYL_LABEL;
	}

	public PaIntEHitBox() {
		// no-op
	}

	@Override
	public PaIntEHitBox clone() {
		return new PaIntEHitBox(this);
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

	/**
	 * @return the wordIndex
	 */
	public int getWordIndex() {
		return wordIndex;
	}

	/**
	 * @return the sylIndex
	 */
	public int getSylIndex() {
		return sylIndex;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param wordIndex the wordIndex to set
	 */
	public void setWordIndex(int wordIndex) {
		this.wordIndex = wordIndex;
	}

	/**
	 * @param sylIndex the sylIndex to set
	 */
	public void setSylIndex(int sylIndex) {
		this.sylIndex = sylIndex;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return the params
	 */
	public PaIntEParams getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(PaIntEParams params) {
		this.params = params;
	}

	public enum Type {
		CURVE,
		AXIS,
		WORD_LABEL,
		SYL_LABEL,
		GRAPH,
	}
}
