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
public class PaIntEConstraintParams implements Serializable {

	private static final long serialVersionUID = 5146300722493790738L;

	@XmlAttribute
	double a1, a2, b, c1, c2, d, alignment;

	private int activeMask = 0;

	public static final int MASK_A1 = (1<<0);
	public static final int MASK_A2 = (1<<1);
	public static final int MASK_D = (1<<2);
	public static final int MASK_C1 = (1<<3);
	public static final int MASK_C2 = (1<<4);
	public static final int MASK_B = (1<<5);
	public static final int MASK_ALIGNMENT = (1<<6);

	public static final int ALL_SET = MASK_A1 | MASK_A2 | MASK_B | MASK_C1 | MASK_C2 | MASK_D | MASK_ALIGNMENT;

	public PaIntEConstraintParams() {
		// no-op
	}

	public PaIntEConstraintParams(PaIntEConstraintParams constraints) {
		setParams(constraints);
	}

	public PaIntEConstraintParams(PaIntEParams params) {
		setParams(params);
	}

	public PaIntEConstraintParams(String s) {
		setParams(s);
	}

	public boolean isUndefined() {
		return activeMask==0;
	}

	public void setParams(PaIntEParams params) {
		if (params == null)
			throw new NullPointerException("Invalid params"); //$NON-NLS-1$

		a1 = params.getA1();
		a2 = params.getA2();
		b = params.getB();
		c1 = params.getC1();
		c2 = params.getC2();
		d = params.getD();
		alignment = params.getAlignment();

		activeMask = ALL_SET;
	}

	public void setParams(PaIntEConstraintParams constraints) {
		if (constraints == null)
			throw new NullPointerException("Invalid constraints");  //$NON-NLS-1$

		a1 = constraints.a1;
		a2 = constraints.a2;
		b = constraints.b;
		c1 = constraints.c1;
		c2 = constraints.c2;
		d = constraints.d;
		alignment = constraints.alignment;

		activeMask = constraints.activeMask;
	}

	public void setParams(double[] params) {
		if (params == null)
			throw new NullPointerException("Invalid params");  //$NON-NLS-1$

		setA1(params[0]);
		setA2(params[1]);
		setB(params[2]);
		setC1(params[3]);
		setC2(params[4]);
		setD(params[5]);

		if(params.length>6) {
			setAlignment(params[6]);
		} else {
			setAlignment(PaIntEParams.DEFAULT_ALIGNMENT);
		}

		activeMask = ALL_SET;
	}

	public void setParams(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
		if (sentence == null)
			throw new NullPointerException("Invalid sentence");  //$NON-NLS-1$

		setA1(sentence.getPainteA1(wordIndex, sylIndex));
		setA2(sentence.getPainteA2(wordIndex, sylIndex));
		setB(sentence.getPainteB(wordIndex, sylIndex));
		setC1(sentence.getPainteC1(wordIndex, sylIndex));
		setC2(sentence.getPainteC2(wordIndex, sylIndex));
		setD(sentence.getPainteD(wordIndex, sylIndex));

		activeMask = ALL_SET;
	}

	public void setParams(String encodedParams) {
		if (encodedParams == null)
			throw new NullPointerException("Invalid encodedParams");  //$NON-NLS-1$

		String[] items = encodedParams.split("\\|"); //$NON-NLS-1$

		if(items.length<6 || items.length>7)
			throw new IllegalArgumentException("Invalid params string - wrong number of pipe separated items: "+encodedParams); //$NON-NLS-1$

		activeMask = 0;

		a1 = parse(items[0], MASK_A1);
		a2 = parse(items[1], MASK_A2);
		b = parse(items[2], MASK_B);
		c1 = parse(items[3], MASK_C1);
		c2 = parse(items[4], MASK_C2);
		d = parse(items[5], MASK_D);

		if(items.length==7) {
			alignment = parse(items[6], MASK_ALIGNMENT);
		} else {
			alignment = PaIntEParams.DEFAULT_ALIGNMENT;
			setAlignmentActive(true);
		}
	}

	private double parse(String s, int mask) {
		if(s==null || s.isEmpty()) {
			activeMask &= ~mask;
			return 0D;
		} else {
			activeMask |= mask;
			return Double.parseDouble(s);
		}
	}

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

		return params;
	}

	public PaIntEParams toPaIntEParams() {
		PaIntEParams params = new PaIntEParams();
		params.setParams(this);
		return params;
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
	public double getAlignment() {
		return alignment;
	}

	public void setAlignment(double alignment) {
		this.alignment = alignment;
	}

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

	public boolean isA1Active() {
		return (activeMask & MASK_A1) == MASK_A1;
	}
	public boolean isA2Active() {
		return (activeMask & MASK_A2) == MASK_A2;
	}
	public boolean isBActive() {
		return (activeMask & MASK_B) == MASK_B;
	}
	public boolean isC1Active() {
		return (activeMask & MASK_C1) == MASK_C1;
	}
	public boolean isC2Active() {
		return (activeMask & MASK_C2) == MASK_C2;
	}
	public boolean isDActive() {
		return (activeMask & MASK_D) == MASK_D;
	}
	public boolean isAlignmentActive() {
		return (activeMask & MASK_ALIGNMENT) == MASK_ALIGNMENT;
	}

	public void setA1Active(boolean active) {
		if(active) {
			activeMask |= MASK_A1;
		} else {
			activeMask &= ~MASK_A1;
		}
	}
	public void setA2Active(boolean active) {
		if(active) {
			activeMask |= MASK_A2;
		} else {
			activeMask &= ~MASK_A2;
		}
	}
	public void setBActive(boolean active) {
		if(active) {
			activeMask |= MASK_B;
		} else {
			activeMask &= ~MASK_B;
		}
	}
	public void setC1Active(boolean active) {
		if(active) {
			activeMask |= MASK_C1;
		} else {
			activeMask &= ~MASK_C1;
		}
	}
	public void setC2Active(boolean active) {
		if(active) {
			activeMask |= MASK_C2;
		} else {
			activeMask &= ~MASK_C2;
		}
	}
	public void setDActive(boolean active) {
		if(active) {
			activeMask |= MASK_D;
		} else {
			activeMask &= ~MASK_D;
		}
	}
	public void setAlignmentActive(boolean active) {
		if(active) {
			activeMask |= MASK_ALIGNMENT;
		} else {
			activeMask &= ~MASK_ALIGNMENT;
		}
	}

	public void setActiveMask(int mask) {
		if(mask<0 || mask>ALL_SET)
			throw new IllegalArgumentException("Invalid mask (0 to "+ALL_SET+" allowed): "+mask); //$NON-NLS-1$ //$NON-NLS-2$

		activeMask = mask;
	}

	public int getActiveMask() {
		return activeMask;
	}

	@Override
	public PaIntEConstraintParams clone() {
		return new PaIntEConstraintParams(this);
	}

	@Override
	public int hashCode() {
		return (int) (a1*a2*b*c1*c2*d*activeMask);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PaIntEConstraintParams) {
			PaIntEConstraintParams other = (PaIntEConstraintParams) obj;
			return a1==other.a1
					&& a2==other.a2
					&& b==other.b
					&& c1==other.c1
					&& c2==other.c2
					&& d==other.d
					&& alignment==other.alignment
					&& activeMask==other.activeMask;
		}

		return false;
	}

	private static final char PIPE = '|';

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		// A1
		if(isA1Active()) {
			sb.append(a1);
		}
		sb.append(PIPE);

		// A2
		if(isA2Active()) {
			sb.append(a2);
		}
		sb.append(PIPE);

		// B
		if(isBActive()) {
			sb.append(b);
		}
		sb.append(PIPE);

		// C1
		if(isC1Active()) {
			sb.append(c1);
		}
		sb.append(PIPE);

		// C2
		if(isC2Active()) {
			sb.append(c2);
		}
		sb.append(PIPE);

		// D
		if(isDActive()) {
			sb.append(d);
		}

		// Alignment
		if(isAlignmentActive()) {
			sb.append(PIPE);
			sb.append(alignment);
		}

		return sb.toString();
	}
}
