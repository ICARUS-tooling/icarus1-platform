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
package de.ims.icarus.language.coref;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import de.ims.icarus.util.CompactProperties;
import de.ims.icarus.util.Counter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorefProperties extends CompactProperties {
	
	private static final long serialVersionUID = 4855184362661793838L;
	
	private static final char ASSIGNMENT_CHAR = ':';
	private static final char SEPARATOR_CHAR = ';';

	public CorefProperties() {
		// no-op
	}

	@Override
	public String toString() {
		if(table==null) {
			return ""; //$NON-NLS-1$
		} 
		
		StringBuilder sb = new StringBuilder();
		appendTo(sb);
		
		return sb.toString();
	}
	
	public void appendTo(StringBuilder sb) {
		
		if(table instanceof Object[]) {
			Object[] items = (Object[]) table;
			int maxI = items.length-1;
			for(int i=0; i<maxI; i+=2) {
				if(items[i]==null || items[i+1]==null) {
					continue;
				}
				sb.append(items[i]).append(ASSIGNMENT_CHAR)
					.append(items[i+1]).append(SEPARATOR_CHAR);
			}
		} else if(table!=null) {
			Map<?, ?> map = (Map<?, ?>) table;
			for(Entry<?, ?> entry : map.entrySet()) {
				if(entry.getValue()==null) {
					continue;
				}
				sb.append(entry.getKey()).append(ASSIGNMENT_CHAR)
					.append(entry.getValue()).append(SEPARATOR_CHAR);
			}
		}
	}
	
	public static CorefProperties parse(String s) {
		if(s==null || s.isEmpty()) {
			return null;
		}
		
		CorefProperties properties = new CorefProperties();
		int maxIndex = s.length()-1;
		int startIndex = 0;
		while(startIndex<maxIndex) {
			int offset0 = s.indexOf(ASSIGNMENT_CHAR, startIndex);
			if(offset0==-1)
				throw new IllegalArgumentException("Invalid properties source string: "+s); //$NON-NLS-1$
			int endIndex = s.indexOf(SEPARATOR_CHAR, offset0);
			if(endIndex==-1) {
				endIndex = s.length();
			}
			properties.put(s.substring(startIndex, offset0), 
					toValue(s.substring(offset0+1, endIndex)));
			
			startIndex = endIndex+1;
		}
		
		return properties;
	}
	
	private static Object toValue(String s) {
		try {
			return Integer.parseInt(s);
		} catch(NumberFormatException e) {
			// ignore
		}
		try {
			return Double.parseDouble(s);
		} catch(NumberFormatException e) {
			// ignore
		}
		
		return s;
	}

	@Override
	public CorefProperties clone() {
		return (CorefProperties) super.clone();
	}
	
	public static void countKeys(CorefProperties properties, 
			Counter<String> counter) {
		if(counter==null)
			throw new IllegalArgumentException("Invalid counter"); //$NON-NLS-1$
		
		if(properties==null || properties.size()==0) {
			return;
		}
		
		Object table = properties.table;
		
		if(table instanceof Object[]) {
			Object[] items = (Object[]) table;
			int maxI = items.length-1;
			for(int i=0; i<maxI; i+=2) {
				if(items[i]==null || items[i+1]==null) {
					continue;
				}
				counter.increment(getRawKey((String) items[i]));
			}
		} else {
			Map<?, ?> map = (Map<?, ?>) table;
			for(Entry<?, ?> entry : map.entrySet()) {
				if(entry.getValue()==null) {
					continue;
				}
				
				counter.increment(getRawKey((String) entry.getKey()));
			}
		}
	}
	
	public static void collectKeys(CorefProperties properties, 
			Collection<String> target) {
		if(target==null)
			throw new IllegalArgumentException("Invalid counter"); //$NON-NLS-1$
		
		if(properties==null || properties.size()==0) {
			return;
		}
		
		Object table = properties.table;
		
		if(table instanceof Object[]) {
			Object[] items = (Object[]) table;
			int maxI = items.length-1;
			for(int i=0; i<maxI; i+=2) {
				if(items[i]==null || items[i+1]==null) {
					continue;
				}
				target.add(getRawKey((String) items[i]));
			}
		} else {
			Map<?, ?> map = (Map<?, ?>) table;
			for(Entry<?, ?> entry : map.entrySet()) {
				if(entry.getValue()==null) {
					continue;
				}
				
				target.add(getRawKey((String) entry.getKey()));
			}
		}
	}
	
	private static String getRawKey(String key) {
		int idx = key.lastIndexOf('_');
		if(idx==-1) {
			return key;
		}
		
		int len = key.length();
		for(int i=idx+1; i<len; i++) {
			if(!Character.isDigit(key.charAt(i))) {
				return key;
			}
		}
		
		return key.substring(0, idx);
	}
}