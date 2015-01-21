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
package de.ims.icarus.model.api.driver.indexing;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import de.ims.icarus.model.api.layer.Layer;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.collections.IdentityHashSet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class IndexStorage {

	public static String getLabel(Index index) {
		return index.getSourceLayer().getName()+"->"+index.getTargetLayer().getName(); //$NON-NLS-1$
	}

	private AtomicBoolean locked = new AtomicBoolean(false);

	private TIntObjectMap<Index> indexMap = new TIntObjectHashMap<>();
	private Set<Index> indexSet = new IdentityHashSet<>();

	public void lock() {
		if(!locked.compareAndSet(false, true))
			throw new IllegalStateException("Storage already locked!"); //$NON-NLS-1$
	}

	public boolean isLocked() {
		return locked.get();
	}

	public Set<Index> getLayers() {
		return CollectionUtils.getSetProxy(indexSet);
	}

	private int getKey(Layer source, Layer target) {
		return source.getUID() & (target.getUID()<<11);
	}

	public Index getIndex(Layer source, Layer target) {
		return indexMap.get(getKey(source, target));
	}

	public boolean hasIndex(Layer source, Layer target) {
		return indexMap.containsKey(getKey(source, target));
	}

	public void addIndex(Index index) {
		if(isLocked())
			throw new IllegalStateException("Storage is locked!"); //$NON-NLS-1$
		if (index == null)
			throw new NullPointerException("Invalid index"); //$NON-NLS-1$

		if(indexSet.contains(index))
			throw new IllegalArgumentException("Duplicate index: "+getLabel(index)); //$NON-NLS-1$

		indexMap.put(getKey(index.getSourceLayer(), index.getTargetLayer()), index);
	}

	public List<Index> getOutgoingIndices(Layer source) {
		List<Index> result = null;

		for(Index index : indexSet) {
			if(index.getSourceLayer()==source) {
				if(result==null) {
					result = new ArrayList<>();
				}
				result.add(index);
			}
		}

		if(result==null) {
			result = Collections.emptyList();
		}

		return result;
	}

	public List<Index> getIncomingIndices(Layer target) {
		List<Index> result = null;

		for(Index index : indexSet) {
			if(index.getTargetLayer()==target) {
				if(result==null) {
					result = new ArrayList<>();
				}
				result.add(index);
			}
		}

		if(result==null) {
			result = Collections.emptyList();
		}

		return result;
	}
}
