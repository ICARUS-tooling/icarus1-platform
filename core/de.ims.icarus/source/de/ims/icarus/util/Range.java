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
package de.ims.icarus.util;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Range {
	
	private int start = 0, end = 1;

	public Range() {
		// no-op
	}

	public Range(int start, int end) {
		setRange(start, end);
	}

	public Range(int end) {
		setRange(0, end);
	}

	public Range(Range source) {
		setRange(source);
	}
	
	public void setRange(int start, int end) {
		if(start>end)
			throw new IllegalArgumentException(String.format("Invalid range arguments: %d - %d", start, end)); //$NON-NLS-1$
		this.start = start;
		this.end = end;
	}
	
	public void setRange(Range source) {
		if(source==null)
			throw new NullPointerException("Invalid range"); //$NON-NLS-1$
		
		start = source.getStart();
		end = source.getEnd();
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		if(start>end)
			throw new IllegalArgumentException("Start value out of bounds: "+start); //$NON-NLS-1$
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		if(end<start)
			throw new IllegalArgumentException("End value out of bounds: "+end); //$NON-NLS-1$
		this.end = end;
	}

	public int getRange() {
		return end-start+1;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Range) {
			Range other = (Range) obj;
			return other.start==start && other.end==end;
		}
		return false;
	}

	@Override
	public Range clone() {
		return new Range(this);
	}

	@Override
	public String toString() {
		return String.format("[%d-%d]", start, end); //$NON-NLS-1$
	}
}
