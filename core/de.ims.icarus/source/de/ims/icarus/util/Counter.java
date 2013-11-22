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

import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.collections.IntValueHashMap;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Counter {
	
	private IntValueHashMap counts = new IntValueHashMap();

	public Counter() {
		// no-op
	}

	public int increment(Object data) {
		int c = counts.get(data);
		if(c==-1) {
			c = 0;
		}
		
		c++;
		counts.put(data, c);
		
		return c;
	}

	public int decrement(Object data) {
		int c = counts.get(data);
		if(c<1)
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
	
	public int getCount(Object data) {
		int c = counts.get(data);
		return c==-1 ? 0 : c;
	}
	
	public boolean hasCount(Object data) {
		int c = counts.get(data);
		return c>0;
	}
	
	public Collection<Object> getItems() {
		return CollectionUtils.getCollectionProxy(counts.keySet());
	}
	
	public boolean isEmpty() {
		return counts.isEmpty();
	}
}
