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
package de.ims.icarus.ui.list;

import java.util.BitSet;

import javax.swing.AbstractListModel;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FilterListModel extends AbstractListModel<Boolean> {
	
	private static final long serialVersionUID = 1484917444108124343L;
	
	protected BitSet filter;
	protected int size = 0;
	
	public FilterListModel() {
		this(-1);
	}
	
	public FilterListModel(int size) {
		if(size>0) {
			filter = new BitSet(size);
		}
	}
	
	public void setFilter(BitSet filter) {
		if(filter==null)
			throw new NullPointerException("Invalid filter"); //$NON-NLS-1$
		if(size==0) 
			throw new IllegalStateException("Cannot apply new filter to empty model"); //$NON-NLS-1$
		
		this.filter = new BitSet(filter.size());
		this.filter.or(filter);
		
		fireContentsChanged(this, 0, size-1);
	}
	
	public void setSize(int newSize) {
		if(newSize<=0) {
			filter = null;
		} else {
			filter = new BitSet(newSize);
			filter.set(0, newSize);
		}
		
		size = Math.max(newSize, 0);
		
		
		fireContentsChanged(this, 0, Math.max(0, size-1));
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Boolean getElementAt(int index) {
		return filter==null ? false : filter.get(index);
	}
	
	public boolean isSet(int index) {
		return filter==null ? false : filter.get(index);
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return size;
	}	
	
	public void setElementAt(int index, boolean value) {
		if(filter==null) {
			return;
		}
		
		boolean oldValue = filter.get(index);
		if(oldValue==value) {
			return;
		}
		
		filter.set(index, value);
		fireContentsChanged(this, index, index);
	}
	
	public void flipElementAt(int index) {
		if(filter==null) {
			return;
		}
		
		filter.flip(index);
		fireContentsChanged(this, index, index);
	}
	
	public void flip() {
		if(filter==null) {
			return;
		}
		
		filter.flip(0, size);
		fireContentsChanged(this, 0, size-1);
	}
	
	public void clear() {
		if(filter==null || size==0) {
			return;
		}
		
		filter.clear();
		fireContentsChanged(this, 0, size-1);
	}
	
	public void fill() {
		if(filter==null || size==0) {
			return;
		}
		
		filter.set(0, size);
		fireContentsChanged(this, 0, size-1);
	}
	
	public void fill(boolean value) {
		if(filter==null || size==0) {
			return;
		}
		
		filter.set(0, size, value);
		fireContentsChanged(this, 0, size-1);
	}
	
	public void fill(int limit) {
		if(filter==null || size==0) {
			return;
		}
		
		if(size<limit) {
			filter.set(0, size);
		} else {
			filter.set(0, limit);
			filter.set(limit, size, false);
		}
		
		fireContentsChanged(this, 0, size-1);
	}
	
	public int filteredElementsCount() {
		return filter==null ? 0 : filter.cardinality();
	}
}