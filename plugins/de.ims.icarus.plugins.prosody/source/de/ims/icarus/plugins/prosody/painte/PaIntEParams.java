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
package de.ims.icarus.plugins.prosody.painte;

import java.io.Serializable;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import de.ims.icarus.plugins.prosody.ProsodicSentenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="painte")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaIntEParams implements Serializable {

	public static final double DEFAULT_ALIGNMENT = 3.6;

	private static final long serialVersionUID = 2000754079504418219L;

	@XmlAttribute
	double a1, a2, b, c1, c2, d, alignment = DEFAULT_ALIGNMENT;

	public static PaIntEParams parsePaIntEParams(String s) {
		if(s==null) {
			return null;
		}

		PaIntEParams params = new PaIntEParams();
		params.setParams(s);

		return params;
	}

	public void clear() {
		a1 = a2 = b = c1 = c2 = d = 0.0;
		alignment = DEFAULT_ALIGNMENT;
	}

	public PaIntEParams() {
		// no-op
	}

	public PaIntEParams(PaIntEParams params) {
		setParams(params);
	}

	public PaIntEParams(double[] params) {
		setParams(params);
	}

	public PaIntEParams(PaIntEConstraintParams constraints) {
		setParams(constraints);
	}

	public PaIntEParams(String encodedParams) {
		setParams(encodedParams);
	}

	public PaIntEParams(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
		setParams(sentence, wordIndex, sylIndex);
	}

	public void setParams(PaIntEParams params) {
		a1 = params.a1;
		a2 = params.a2;
		b = params.b;
		c1 = params.c1;
		c2 = params.c2;
		d = params.d;
		alignment = params.alignment;
	}

	public void setParams(PaIntEConstraintParams constraints) {
		a1 = constraints.a1;
		a2 = constraints.a2;
		b = constraints.b;
		c1 = constraints.c1;
		c2 = constraints.c2;
		d = constraints.d;
		alignment = constraints.alignment;
	}

	public void setParams(double[] params) {
		setA1(params[0]);
		setA2(params[1]);
		setB(params[2]);
		setC1(params[3]);
		setC2(params[4]);
		setD(params[5]);

		if(params.length>6) {
			setAlignment(params[6]);
		} else {
			setAlignment(DEFAULT_ALIGNMENT);
		}
	}

	public void setParams(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
		setA1(sentence.getPainteA1(wordIndex, sylIndex));
		setA2(sentence.getPainteA2(wordIndex, sylIndex));
		setB(sentence.getPainteB(wordIndex, sylIndex));
		setC1(sentence.getPainteC1(wordIndex, sylIndex));
		setC2(sentence.getPainteC2(wordIndex, sylIndex));
		setD(sentence.getPainteD(wordIndex, sylIndex));
	}

	public void setParams(String encodedParams) {
		if (encodedParams == null)
			throw new NullPointerException("Invalid encodedParams");  //$NON-NLS-1$

		String[] items = encodedParams.split("\\|"); //$NON-NLS-1$

		if(items.length<6 || items.length>7)
			throw new IllegalArgumentException("Invalid params string - wrong number of pipe separated items: "+encodedParams); //$NON-NLS-1$

		a1 = parse(items[0]);
		a2 = parse(items[1]);
		b = parse(items[2]);
		c1 = parse(items[3]);
		c2 = parse(items[4]);
		d = parse(items[5]);

		if(items.length==7) {
			alignment = parse(items[6]);
		} else {
			alignment = PaIntEParams.DEFAULT_ALIGNMENT;
		}
	}

	private double parse(String s) {
		return (s==null || s.isEmpty()) ? 0D : Double.parseDouble(s);
	}

	/**
	 * Returns the params as an array, not including the alignment value!
	 */
	public double[] getParams() {
		return getParams(new double[6]);
	}

	public double[] getParams(double[] params) {
		params[0] = a1;
		params[1] = a2;
		params[2] = b;
		params[3] = c1;
		params[4] = c2;
		params[5] = d;

		if(params.length>6) {
			params[6] = alignment;
		}

		return params;
	}

	public double calc(double x) {
		return PaIntEUtils.calcY(x, this);
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
	@Override
	public PaIntEParams clone() {
		PaIntEParams params = new PaIntEParams();
		params.setParams(this);
		return params;
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

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int)(a1*a2*b*c1*c2*d*alignment);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PaIntEParams) {
			PaIntEParams other = (PaIntEParams) obj;

			return a1==other.a1
					&& a2==other.a2
					&& b==other.b
					&& c1==other.c1
					&& c2==other.c2
					&& d==other.d
					&& alignment==other.alignment;
		}
		return false;
	}

	private static final char PIPE = '|';

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		// A1
		sb.append(a1);
		sb.append(PIPE);

		// A2
		sb.append(a2);
		sb.append(PIPE);

		// B
		sb.append(b);
		sb.append(PIPE);

		// C1
		sb.append(c1);
		sb.append(PIPE);

		// C2
		sb.append(c2);
		sb.append(PIPE);

		// D
		sb.append(d);

		// Alignment
		if(alignment!=DEFAULT_ALIGNMENT) {
			sb.append(PIPE);
			sb.append(alignment);
		}

		return sb.toString();
	}

	public String format() {
		return String.format(Locale.ENGLISH,
				"%f|%.02f b=%.02f c1=%.02f c2=%.02f d=%.02f alignment=%.02f", //$NON-NLS-1$
				a1, a2, b, c1, c2, d, alignment);
	}
}
