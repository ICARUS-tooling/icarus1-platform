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
package de.ims.icarus.language.coref.helper;

import java.util.Arrays;

import de.ims.icarus.language.coref.Span;
import de.ims.icarus.util.CorruptedStateException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SpanBuffer {
	
	/**
	 * Token is not part of any mention
	 */
	private static final int SPANTYPE_NONE = 0;
	
	/**
	 * A spans starts at this token
	 */
	private static final int SPANTYPE_BEGIN = (1 << 0);
	
	/**
	 * A mention ends at this token
	 */
	private static final int SPANTYPE_END = (1 << 1);
	
	/**
	 * The token is part of an arbitrary number of nested
	 * spans but is neither start nor end of any. 
	 */
	private static final int SPANTYPE_INTERMEDIATE = (1 << 2);
	
	/**
	 * Span of size {@code 1}
	 */
	private static final int SPANTYPE_SINGLE = (1 << 3);
	
	// Type of token in sentence
	private int[] types;
	// Number of spans starting or ending at a certain token
	// (not including singletons!)
	private int[] starting;
	private int[] ending;
	
	// Mapping from token index to list of spans
	// ending and/or starting at that index
	private int[][] ids;
	
	private Span[] spans;
		
	// Number of tokens covered by spans
	private int coveredSize;
	
	public static final int DEFAULT_BUFFER_SIZE = 100;

	public SpanBuffer() {
		this(DEFAULT_BUFFER_SIZE);
	}

	public SpanBuffer(int bufferSize) {
		refreshBuffer(bufferSize);
	}

	private void refreshBuffer(int bufferSize) {
		types = new int[bufferSize];
		ids = new int[bufferSize][];
		starting = new int[bufferSize];
		ending = new int[bufferSize];
	}
	
	public void rebuild(Span[] spans) {
		if(spans==null)
			throw new IllegalArgumentException("Invalid span array"); //$NON-NLS-1$
		
		this.spans = spans;
		
		int numSpans = spans.length;
		
		coveredSize = 0;
		for(int i=0; i<numSpans; i++) {
			coveredSize = Math.max(coveredSize, spans[i].getEndIndex());
		}
		coveredSize++;
		
		if(coveredSize>=types.length) {
			refreshBuffer(coveredSize*2);
		}
		
		// Clean up
		for(int i=0; i<coveredSize; i++) {
			types[i] = SPANTYPE_NONE;
			starting[i] = 0;
			ending[i] = 0;
			int[] list = ids[i];
			if(list!=null) {
				list[0] = 0;
			}
		}
		
		if(spans==null || numSpans==0) {
			return;
		}
		
		// Cache type informations for tokens
		for(int i=0; i<numSpans; i++) {
			Span span = spans[i];
						
			int start = span.getBeginIndex();
			int end = span.getEndIndex();
			
			boolean single = start==end;
			
			// Allow at most one singleton per token
			if(single && (types[start] & SPANTYPE_SINGLE) != 0)
				throw new IllegalArgumentException("Duplicate singleton at index "+start+", span: "+span); //$NON-NLS-1$ //$NON-NLS-2$
			
			// Allow at most one span to cover a given range
			if(!single && (types[start] & SPANTYPE_END) != 0
					&& (types[end] & SPANTYPE_BEGIN) != 0)
				throw new IllegalArgumentException("Concurrent start and end for span: "+span); //$NON-NLS-1$
			
			if(single) {
				types[start] |= SPANTYPE_SINGLE;
				add(start, i);
			} else {
				types[start] |= SPANTYPE_BEGIN;
				types[end] |= SPANTYPE_END;
				
				add(start, i);
				add(end, i);

				starting[start]++;
				ending[end]++;
			}
		}
		
		// Sanity check and marking of intermediate tokens
		int spanCount = 0;
		for(int i=0; i<coveredSize; i++) {
			if(spanCount>0 && types[i]==SPANTYPE_NONE) {
				types[i] = SPANTYPE_INTERMEDIATE;
			}
			
			if((types[i] & SPANTYPE_BEGIN) != 0) {
				spanCount += starting[i];
			} 
			if((types[i] & SPANTYPE_END) != 0) {
				spanCount -= ending[i];
			}
		}
		if(spanCount!=0)
			throw new CorruptedStateException("Unclosed span count: "+spanCount); //$NON-NLS-1$
	}
	
	private int[] add(int index, int id) {
		int[] list = ids[index];
		if(list==null) {
			list = new int[3];
			ids[index] = list;
		}
		
		if(list[0] >= list.length-2) {
			list = Arrays.copyOf(list, list.length*2);
			ids[index] = list;
		}
		
		list[0]++;
		list[list[0]] = id;
		
		return list;
	}
	
	public boolean isStart(int index) {
		if(index>=coveredSize) {
			return false;
		}
		return (types[index] & SPANTYPE_BEGIN)!=0 || (types[index] & SPANTYPE_SINGLE)!=0;
	}
	
	public boolean isEnd(int index) {
		if(index>=coveredSize) {
			return false;
		}
		return (types[index] & SPANTYPE_END)!=0 || (types[index] & SPANTYPE_SINGLE)!=0;
	}
	
	public boolean isStartOrEnd(int index) {
		if(index>=coveredSize) {
			return false;
		}
		return (types[index] & SPANTYPE_BEGIN)!=0 
				|| (types[index] & SPANTYPE_END)!=0 
				|| (types[index] & SPANTYPE_SINGLE)!=0;
	}
	
	public boolean isImportant(int index) {
		return index<coveredSize && types[index]!= SPANTYPE_NONE;
	}
	
	public int getSpanCount(int index) {
		if(index>=coveredSize) {
			return 0;
		}
		int[] list = ids[index];
		return list==null ? 0 : list[0];
	}
	
	public Span getSpan(int index, int i) {
		if(index>=coveredSize) {
			return null;
		}
		return spans==null ? null : spans[ids[index][1+i]];
	}
	
	public void clear() {
		spans = null;
		coveredSize = 0;
	}
}
