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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Counter<E extends Object> {
	
	private Map<E, Integer> counts = new HashMap<>();

	public Counter() {
		// no-op
	}

	public int increment(E data) {
		Integer c = counts.get(data);
		if(c==null) {
			c = 0;
		}
		c++;
		
		counts.put(data, c);
		
		return c;
	}

	public int decrement(E data) {
		Integer c = counts.get(data);
		if(c==null || c==0) 
			throw new IllegalStateException("Cannot decrement count for data: "+data); //$NON-NLS-1$

		c--;
		
		if(c==0) {
			counts.remove(data);
		} else {
			counts.put(data, c);
		}
		
		return c;
	}
	
	public void clear() {
		counts.clear();
	}
	
	public int getCount(E data) {
		Integer c = counts.get(data);
		return c==null ? 0 : c;
	}
	
	public Collection<E> getItems() {
		// TODO make returned collection immutable?
		return counts.keySet();
	}
	
	public boolean isEmpty() {
		return counts.isEmpty();
	}
}
