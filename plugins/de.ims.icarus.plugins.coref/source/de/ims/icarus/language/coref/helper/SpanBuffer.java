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

import de.ims.icarus.language.coref.CoreferenceData;
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
	private static final int SPANTYPE_SINGLETON = (1 << 3);
	
	private int[] types;
	private int[][] ids;
	
	private Span[] spans;
	
	private int size;
	
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
	}
	
	public void rebuild(CoreferenceData data) {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		
		if(data.length()>=types.length) {
			refreshBuffer(data.length()*2);
		}
		
		size = data.length();
		
		for(int i=0; i<size; i++) {
			types[i] = SPANTYPE_NONE;
			int[] list = ids[i];
			if(list!=null) {
				list[0] = 0;
			}
		}
		
		spans = data.getSpans();
		
		if(spans==null || spans.length==0) {
			return;
		}
		
		for(int i=0; i<spans.length; i++) {
			Span span = spans[i];
			int start = span.getBeginIndex();
			int end = span.getEndIndex();
			
			boolean singleton = start==end;
			
			if(singleton && (types[start] & SPANTYPE_SINGLETON) != 0)
				throw new IllegalArgumentException("Duplicate singleton at index "+start); //$NON-NLS-1$
			
			if(!singleton && (types[start] & SPANTYPE_END) != 0)
				throw new IllegalArgumentException("Concurrent start and end at index "+start); //$NON-NLS-1$
			
			if(!singleton && (types[end] & SPANTYPE_BEGIN) != 0)
				throw new IllegalArgumentException("Concurrent start and end at index "+end); //$NON-NLS-1$
			
			if(singleton) {
				types[start] |= SPANTYPE_SINGLETON;
				add(start, i);
			} else {
				types[start] |= SPANTYPE_BEGIN;
				types[end] |= SPANTYPE_END;
				
				add(start, i);
				add(end, i);
			}
		}
		
		int spanCount = 0;
		for(int i=0; i<size; i++) {
			if(spanCount>0 && types[i]==SPANTYPE_NONE) {
				types[i] = SPANTYPE_INTERMEDIATE;
			}
			
			boolean singleton = (types[i] & SPANTYPE_SINGLETON) != 0;
			
			if((types[i] & SPANTYPE_BEGIN) != 0) {
				spanCount += ids[i][0];
				if(singleton) {
					spanCount--;
				}
			} else if((types[i] & SPANTYPE_END) != 0) {
				spanCount -= ids[i][0];
				if(singleton) {
					spanCount++;
				}
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
		return (types[index] & SPANTYPE_BEGIN)!=0 || (types[index] & SPANTYPE_SINGLETON)!=0;
	}
	
	public boolean isEnd(int index) {
		return (types[index] & SPANTYPE_END)!=0 || (types[index] & SPANTYPE_SINGLETON)!=0;
	}
	
	public boolean isStartOrEnd(int index) {
		return (types[index] & SPANTYPE_BEGIN)!=0 
				|| (types[index] & SPANTYPE_END)!=0 
				|| (types[index] & SPANTYPE_SINGLETON)!=0;
	}
	
	public boolean isImportant(int index) {
		return types[index]!= SPANTYPE_NONE;
	}
	
	public int getSpanCount(int index) {
		int[] list = ids[index];
		return list==null ? 0 : list[0];
	}
	
	public Span getSpan(int index, int i) {
		return spans==null ? null : spans[ids[index][1+i]];
	}
	
	public void clear() {
		spans = null;
	}
}
