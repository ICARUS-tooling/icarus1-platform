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
package de.ims.icarus.search_tools.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EntryBuilder {
	protected List<Hit> hits = new ArrayList<>();
	protected int index;
	
	protected static final Hit[] EMPTY_HITS = new Hit[0];
	
	protected final int[] allocation;
	
	public EntryBuilder(int size) {
		allocation = new int[size];
		Arrays.fill(allocation, -1);
	}
	
	public void allocate(int index, int alloc) {
		allocation[index] = alloc;
	}
	
	public void deallocate(int index) {
		allocation[index] = -1;
	}
	
	public void commitAllocation() {
		addHit(new Hit(allocation.clone()));
	}
	
	public void addHit(Hit hit) {
		hits.add(hit);
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}

	public ResultEntry toEntry() {
		if(hits.size()==0) {
			return new ResultEntry(index, EMPTY_HITS);
		}
		
		ResultEntry entry = new ResultEntry(index, hits.toArray(new Hit[hits.size()]));
		
		hits.clear();
		//Arrays.fill(allocation, -1);
		
		return entry;
	}
}