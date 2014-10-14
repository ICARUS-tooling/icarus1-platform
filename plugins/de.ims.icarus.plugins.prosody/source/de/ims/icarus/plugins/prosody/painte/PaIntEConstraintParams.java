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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import de.ims.icarus.plugins.prosody.ProsodicSentenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="painte")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaIntEConstraintParams extends PaIntEParams {

	private static final long serialVersionUID = 5146300722493790738L;

	private int activeMask = 0;

	public static final int MASK_A1 = (1<<0);
	public static final int MASK_A2 = (1<<1);
	public static final int MASK_D = (1<<2);
	public static final int MASK_C1 = (1<<3);
	public static final int MASK_C2 = (1<<4);
	public static final int MASK_B = (1<<5);
	public static final int MASK_ALIGNMENT = (1<<6);

	public static final int ALL_SET = MASK_A1 | MASK_A2 | MASK_B | MASK_C1 | MASK_C2 | MASK_D | MASK_ALIGNMENT;

	public static final int DEFAULT_REQUIRED_FIELDS = MASK_A1 | MASK_A2 | MASK_B | MASK_C1 | MASK_C2 | MASK_D;

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

	public void checkNonEmpty() {
		checkNonEmpty(null);
	}

	public void checkNonEmpty(String msg) {
		if(activeMask!=0) {
			return;
		}

		String message = "PaIntE-constraint is empty - must provide at least one valid field"; //$NON-NLS-1$
		if(msg!=null) {
			message = msg+": "+message; //$NON-NLS-1$
		}

		throw new PaIntEConstraintException(message, this);
	}

	public void checkParams() {
		checkParams(null, DEFAULT_REQUIRED_FIELDS);
	}

	public void checkParams(String msg) {
		checkParams(msg, DEFAULT_REQUIRED_FIELDS);
	}

	public void checkParams(String msg, int mask) {
		if(activeMask==mask) {
			return;
		}

		int dif = (mask & ~activeMask);

		if(dif==0) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		if(msg!=null) {
			sb.append(msg).append(": "); //$NON-NLS-1$
		}

		sb.append("Missing PaIntE fields ("); //$NON-NLS-1$

		String[] fields = new String[Integer.bitCount(dif)];
		int idx = 0;

		if((dif & MASK_A1) == MASK_A1) {
			fields[idx++] = "A1"; //$NON-NLS-1$
		}

		if((dif & MASK_A2) == MASK_A2) {
			fields[idx++] = "A2"; //$NON-NLS-1$
		}

		if((dif & MASK_B) == MASK_B) {
			fields[idx++] = "B"; //$NON-NLS-1$
		}

		if((dif & MASK_C1) == MASK_C1) {
			fields[idx++] = "C1"; //$NON-NLS-1$
		}

		if((dif & MASK_C2) == MASK_C2) {
			fields[idx++] = "C2"; //$NON-NLS-1$
		}

		if((dif & MASK_D) == MASK_D) {
			fields[idx++] = "D"; //$NON-NLS-1$
		}

		if((dif & MASK_ALIGNMENT) == MASK_ALIGNMENT) {
			fields[idx++] = "Alignment"; //$NON-NLS-1$
		}

		for(int i=0; i<fields.length; i++) {
			if(i>0) {
				sb.append(',');
			}
			sb.append(fields[i]);
		}

		sb.append(')');

		throw new PaIntEConstraintException(sb.toString(), this);
	}

	public boolean isUndefined() {
		return activeMask==0;
	}

	@Override
	public void setParams(PaIntEParams params) {
		super.setParams(params);

		activeMask = ALL_SET;
	}

	@Override
	public void setParams(PaIntEConstraintParams constraints) {
		super.setParams(constraints);

		activeMask = constraints.activeMask;
	}

	@Override
	public void setParams(double[] params) {
		super.setParams(params);

		activeMask = ALL_SET;
	}

	@Override
	public void setParams(ProsodicSentenceData sentence, int wordIndex, int sylIndex) {
		super.setParams(sentence, wordIndex, sylIndex);

		activeMask = ALL_SET;
	}

	@Override
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
		return super.hashCode()*activeMask;
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
