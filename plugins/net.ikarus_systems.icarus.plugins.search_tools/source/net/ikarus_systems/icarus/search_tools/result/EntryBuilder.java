/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.result;

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
		addHit(new Hit(allocation));
	}
	
	public void addHit(Hit hit) {
		hits.add(hit);
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public ResultEntry toEntry() {
		if(hits.size()==0) {
			return null;
		}
		
		ResultEntry entry = new ResultEntry(index, hits.toArray(new Hit[hits.size()]));
		
		hits.clear();
		Arrays.fill(allocation, -1);
		
		return entry;
	}
}