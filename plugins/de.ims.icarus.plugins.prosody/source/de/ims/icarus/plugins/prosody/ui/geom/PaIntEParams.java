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

import java.io.Serializable;

import de.ims.icarus.plugins.prosody.ProsodicSentenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PaIntEParams implements Serializable {

	public static final double DEFAULT_ALIGNMENT = 3.6;

	private static final long serialVersionUID = 2000754079504418219L;

	private double a1, a2, b, c1, c2, d, alignment = DEFAULT_ALIGNMENT;
//	private double minX, minY, maxX, maxY;

	public void clear() {
		a1 = a2 = b = c1 = c2 = d = 0.0;
		alignment = DEFAULT_ALIGNMENT;
	}

	public void setParams(double[] params) {
		setA1(params[0]);
		setA2(params[1]);
		setB(params[2]);
		setC1(params[3]);
		setC2(params[4]);
		setD(params[5]);
	}

	public void setParams(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
		setA1(sentence.getPainteA1(wordIndex, sylIndex));
		setA2(sentence.getPainteA2(wordIndex, sylIndex));
		setB(sentence.getPainteB(wordIndex, sylIndex));
		setC1(sentence.getPainteC1(wordIndex, sylIndex));
		setC2(sentence.getPainteC2(wordIndex, sylIndex));
		setD(sentence.getPainteD(wordIndex, sylIndex));
	}

	public double calc(double x) {
		return d - (c1/(1+Math.exp(-a1*(b-x)+alignment))) - (c2/(1+Math.exp(-a2*(x-b)+alignment)));
	}

	/**
	 * @return the a1
	 */
	public double getA1() {
		return a1;
	}
	/**
	 * @return the a2
	 */
	public double getA2() {
		return a2;
	}
	/**
	 * @return the b
	 */
	public double getB() {
		return b;
	}
	/**
	 * @return the c1
	 */
	public double getC1() {
		return c1;
	}
	/**
	 * @return the c2
	 */
	public double getC2() {
		return c2;
	}
	/**
	 * @return the d
	 */
	public double getD() {
		return d;
	}
	/**
	 * @return the alignment
	 */
	public double getAlignment() {
		return alignment;
	}
//	/**
//	 * @return the minX
//	 */
//	public double getMinX() {
//		return minX;
//	}
//	/**
//	 * @return the minY
//	 */
//	public double getMinY() {
//		return minY;
//	}
//	/**
//	 * @return the maxX
//	 */
//	public double getMaxX() {
//		return maxX;
//	}
//	/**
//	 * @return the maxY
//	 */
//	public double getMaxY() {
//		return maxY;
//	}
	/**
	 * @param a1 the a1 to set
	 */
	public void setA1(double a1) {
		this.a1 = a1;
	}
	/**
	 * @param a2 the a2 to set
	 */
	public void setA2(double a2) {
		this.a2 = a2;
	}
	/**
	 * @param b the b to set
	 */
	public void setB(double b) {
		this.b = b;
	}
	/**
	 * @param c1 the c1 to set
	 */
	public void setC1(double c1) {
		this.c1 = c1;
	}
	/**
	 * @param c2 the c2 to set
	 */
	public void setC2(double c2) {
		this.c2 = c2;
	}
	/**
	 * @param d the d to set
	 */
	public void setD(double d) {
		this.d = d;
	}
	/**
	 * @param alignment the alignment to set
	 */
	public void setAlignment(double alignment) {
		this.alignment = alignment;
	}
//	/**
//	 * @param minX the minX to set
//	 */
//	public void setMinX(double minX) {
//		this.minX = minX;
//	}
//	/**
//	 * @param minY the minY to set
//	 */
//	public void setMinY(double minY) {
//		this.minY = minY;
//	}
//	/**
//	 * @param maxX the maxX to set
//	 */
//	public void setMaxX(double maxX) {
//		this.maxX = maxX;
//	}
//	/**
//	 * @param maxY the maxY to set
//	 */
//	public void setMaxY(double maxY) {
//		this.maxY = maxY;
//	}
}
