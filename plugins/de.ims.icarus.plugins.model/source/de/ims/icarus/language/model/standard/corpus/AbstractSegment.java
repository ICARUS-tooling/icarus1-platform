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
package de.ims.icarus.language.model.standard.corpus;

import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.ContainerType;
import de.ims.icarus.language.model.api.Corpus;
import de.ims.icarus.language.model.api.CorpusException;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.MemberSet;
import de.ims.icarus.language.model.api.MemberType;
import de.ims.icarus.language.model.api.driver.ChunkStorage;
import de.ims.icarus.language.model.api.driver.Driver;
import de.ims.icarus.language.model.api.driver.IndexSet;
import de.ims.icarus.language.model.api.layer.Layer;
import de.ims.icarus.language.model.api.layer.MarkableLayer;
import de.ims.icarus.language.model.api.manifest.ContainerManifest;
import de.ims.icarus.language.model.api.seg.Scope;
import de.ims.icarus.language.model.api.seg.Segment;
import de.ims.icarus.language.model.api.seg.SegmentOwner;
import de.ims.icarus.language.model.iql.Query;
import de.ims.icarus.language.model.util.CorpusUtils;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.collections.IdentityHashSet;
import de.ims.icarus.util.collections.LookupList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
abstract class AbstractSegment implements Segment {

	private final DefaultCorpus corpus;
	private final Query query;

	private final Map<MarkableLayer, ProxyContainer> containers;

	private final Set<SegmentOwner> owners = new IdentityHashSet<>();
	private volatile boolean closed = false;
	private volatile boolean closing = false;


	// When a markable gets added to a proxy container, make sure its host container
	// is a legal base container for the target
	private boolean checkContainersOnAdd = true;

	private boolean checkIndicesOnAdd = true;

	protected AbstractSegment(DefaultCorpus corpus, Query query) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$
		if (query == null)
			throw new NullPointerException("Invalid query"); //$NON-NLS-1$
		if (query.getResultScope() == null)
			throw new IllegalArgumentException("Query does not declare a valid scope"); //$NON-NLS-1$

		this.corpus = corpus;
		this.query = query;

		Scope scope = query.getResultScope();

		Map<MarkableLayer, ProxyContainer> containers = new IdentityHashMap<>();

		// Add primary layer
		containers.put(scope.getPrimaryLayer(), createProxyContainer(scope.getPrimaryLayer(), query));

		// Now process all other layers that can hold markables
		// Remember that annotations are handled by the driver!
		for(Layer layer : scope.getSecondaryLayers()) {
			if(CorpusUtils.isMarkableLayer(layer)) {
				MarkableLayer markableLayer = (MarkableLayer) layer;
				containers.put(markableLayer, createProxyContainer(markableLayer, query));
			}
		}

		//TODO create container sets for proxy containers!

		this.containers = Collections.unmodifiableMap(containers);
	}

	protected ProxyContainer createProxyContainer(MarkableLayer layer, Query query) {
		return new ProxyContainer(layer, estimateContainerCapacity(layer, query));
	}

	protected int estimateContainerCapacity(MarkableLayer layer, Query query) {
		Driver driver = layer.getContext().getDriver();
		long memberCount = -1;

		try {
			memberCount = driver.getMemberCount(layer);
		} catch (CorpusException e) {
			LoggerFactory.error(this, "Failed to fetch cached number of members in markable layer: "+layer, e); //$NON-NLS-1$
		}

		if(memberCount==-1) {
			memberCount = 10000;
		}

		return memberCount>Integer.MAX_VALUE ? 10000 : (int)memberCount;
	}

	/**
	 * @see de.ims.icarus.language.model.api.seg.Segment#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @see de.ims.icarus.language.model.api.seg.Segment#getScope()
	 */
	@Override
	public Scope getScope() {
		return query.getResultScope();
	}

	/**
	 * @see de.ims.icarus.language.model.api.seg.Segment#getQuery()
	 */
	@Override
	public Query getQuery() {
		return query;
	}

	private ProxyContainer getProxyContainer(MarkableLayer layer) {
		return containers.get(layer);
	}

	/**
	 * @see de.ims.icarus.language.model.api.seg.Segment#getContainer(de.ims.icarus.language.model.api.layer.MarkableLayer)
	 */
	@Override
	public Container getContainer(MarkableLayer layer) {
		if (layer == null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$

		return getProxyContainer(layer);
	}

	// Only to be called under 'owners' lock
	protected final void checkOpen() {
		if(closed)
			throw new IllegalStateException("Segment already closed"); //$NON-NLS-1$
		if(closing)
			throw new IllegalStateException("Segment already closing"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.api.seg.Segment#closable()
	 */
	@Override
	public boolean closable() {
		synchronized (owners) {
			checkOpen();

			return owners.isEmpty();
		}
	}

	/**
	 * @see de.ims.icarus.language.model.api.seg.Segment#close()
	 */
	@Override
	public void close() throws CorpusException {
		synchronized (owners) {
			checkOpen();

			for(Iterator<SegmentOwner> it = owners.iterator(); it.hasNext();) {
				SegmentOwner owner = it.next();

				if(owner.release()) {
					it.remove();
				} else
					throw new IllegalStateException("Unable to close segment - could not release ownership of "+owner.getName()); //$NON-NLS-1$
			}

			closing = true;
		}

		// At this point no owners may prevent the segment from closing.
		// Therefore simply free the content of the current page
		freePage();
	}

	/**
	 * Releases all the data stored in the current page.
	 *
	 * @throws CorpusException
	 */
	protected void freePage() throws CorpusException {

		for(ProxyContainer container : containers.values()) {
			Driver driver = container.getLayer().getContext().getDriver();
			driver.releaseContainer(container, this);

			container.clear();
		}
	}

	protected void loadPage(IndexSet[] indices) throws CorpusException {
		if (indices == null)
			throw new NullPointerException("Invalid indices"); //$NON-NLS-1$
		if(indices.length==0)
			throw new IllegalArgumentException("Empty indices array"); //$NON-NLS-1$


	}

	/**
	 * @see de.ims.icarus.language.model.api.seg.Segment#getOwners()
	 */
	@Override
	public Set<SegmentOwner> getOwners() {
		synchronized (owners) {
			return CollectionUtils.getSetProxy(owners);
		}
	}

	/**
	 * @see de.ims.icarus.language.model.api.seg.Segment#acquire(de.ims.icarus.language.model.api.seg.SegmentOwner)
	 */
	@Override
	public void acquire(SegmentOwner owner) throws CorpusException {
		if (owner == null)
			throw new NullPointerException("Invalid owner"); //$NON-NLS-1$

		synchronized (owners) {
			checkOpen();

//			if(owners.contains(owner)) {
//				return;
//			}
//
			owners.add(owner);
		}
	}

	/**
	 * @see de.ims.icarus.language.model.api.seg.Segment#release(de.ims.icarus.language.model.api.seg.SegmentOwner)
	 */
	@Override
	public void release(SegmentOwner owner) throws CorpusException {
		if (owner == null)
			throw new NullPointerException("Invalid owner"); //$NON-NLS-1$

		synchronized (owners) {
			checkOpen();

			if(!owners.remove(owner))
				throw new IllegalArgumentException("Owner does not hold ownership of this segment: "+owner.getName()); //$NON-NLS-1$


		}
	}

	private static final Comparator<Markable> idSorter = new Comparator<Markable>() {

		@Override
		public int compare(Markable o1, Markable o2) {
			return (int)(o1.getIndex()-o2.getIndex());
		}
	};

	private class ProxyContainer implements Container, ChunkStorage {

		private long maxId = -1;
		private boolean sorted = true;

		private final LookupList<Markable> items;
		private final MarkableLayer layer;

		private MemberSet<Container> bases;

		private ProxyContainer(MarkableLayer layer, int capacity) {
			if (layer == null)
				throw new NullPointerException("Invalid layer"); //$NON-NLS-1$

			this.layer = layer;
			items = new LookupList<>(capacity);
		}

		/**
		 * @param bases the bases to set
		 */
		public void setBaseContainers(MemberSet<Container> bases) {
			this.bases = bases;
		}

		/**
		 * @see de.ims.icarus.language.model.api.driver.ChunkStorage#add(de.ims.icarus.language.model.api.Markable,long)
		 */
		@Override
		public void add(Markable member, long index) {

			items.add(member);

			if(index>=maxId) {
				maxId = index;
			} else {
				sorted = false;
			}

			//FIXME remember to run a sorting on the internal list once the loading is done if required!!


//			if(items.isEmpty()) {
//				items.add(member);
//				minId = maxId = index;
//			} if(index>maxId) {
//				items.add(member);
//				maxId = index;
//			} else if(index<minId) {
//				items.add(0, member);
//				minId = index;
//			} else {
//				// Here comes the expensive part, we need to run a binary search
//				// in order to find the position for a sorted insertion
//		        int low = 0;
//		        int high = items.size()-1;
//
//		        while (low <= high) {
//		            int mid = (low + high) >>> 1;
//		            long midVal = items.get(mid).getIndex();
//
//		            if (midVal < index)
//		                low = mid + 1;
//		            else if(midVal > index)
//		                high = mid - 1;
//		            else
//		            	throw new CorruptedStateException("Member "+member+" already present at index "+mid); //$NON-NLS-2$
//		        }
//
//		        items.add(low, member);
//			}
		}

		public void clear() {
			items.clear();
			maxId = -1;
			sorted = true;
		}

		public void sort() {
			if(!sorted) {
				items.sort(idSorter);
				sorted = true;
			}
		}

		public void ensureCapacity(int capacity) {
			items.ensureCapacity(capacity);
		}

		/**
		 * @see de.ims.icarus.language.model.api.Markable#getContainer()
		 */
		@Override
		public Container getContainer() {
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.api.Markable#getLayer()
		 */
		@Override
		public MarkableLayer getLayer() {
			return layer;
		}

		/**
		 * @see de.ims.icarus.language.model.api.Markable#getIndex()
		 */
		@Override
		public long getIndex() {
			return -1L;
		}

		/**
		 * @see de.ims.icarus.language.model.api.Markable#setIndex(long)
		 */
		@Override
		public void setIndex(long newIndex) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Markable#getBeginOffset()
		 */
		@Override
		public long getBeginOffset() {
			return items.first().getBeginOffset();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Markable#getEndOffset()
		 */
		@Override
		public long getEndOffset() {
			return items.last().getEndOffset();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Markable#compareTo(de.ims.icarus.language.model.api.Markable)
		 */
		@Override
		public int compareTo(Markable o) {
			return CorpusUtils.compare(this, o);
		}

		/**
		 * @see de.ims.icarus.language.model.api.CorpusMember#getCorpus()
		 */
		@Override
		public Corpus getCorpus() {
			return corpus;
		}

		/**
		 * @see de.ims.icarus.language.model.api.CorpusMember#getMemberType()
		 */
		@Override
		public MemberType getMemberType() {
			return MemberType.CONTAINER;
		}

		/**
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<Markable> iterator() {
			return items.iterator();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#getContainerType()
		 */
		@Override
		public ContainerType getContainerType() {
			return ContainerType.LIST;
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#getManifest()
		 */
		@Override
		public ContainerManifest getManifest() {
			return getLayer().getManifest().getRootContainerManifest();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#getBaseContainers()
		 */
		@Override
		public MemberSet<Container> getBaseContainers() {
			return bases;
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#getBoundaryContainer()
		 */
		@Override
		public Container getBoundaryContainer() {
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#getMarkableCount()
		 */
		@Override
		public int getMarkableCount() {
			return items.size();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#getMarkableAt(int)
		 */
		@Override
		public Markable getMarkableAt(int index) {
			return items.get(index);
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#indexOfMarkable(de.ims.icarus.language.model.api.Markable)
		 */
		@Override
		public int indexOfMarkable(Markable markable) {
			return items.indexOf(markable);
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#containsMarkable(de.ims.icarus.language.model.api.Markable)
		 */
		@Override
		public boolean containsMarkable(Markable markable) {
			return items.contains(markable);
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#removeAllMarkables()
		 */
		@Override
		public void removeAllMarkables() {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#addMarkable(de.ims.icarus.language.model.api.Markable)
		 */
		@Override
		public void addMarkable(Markable markable) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#addMarkable(int, de.ims.icarus.language.model.api.Markable)
		 */
		@Override
		public void addMarkable(int index, Markable markable) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#removeMarkable(int)
		 */
		@Override
		public Markable removeMarkable(int index) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#removeMarkable(de.ims.icarus.language.model.api.Markable)
		 */
		@Override
		public Markable removeMarkable(Markable markable) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#moveMarkable(int, int)
		 */
		@Override
		public void moveMarkable(int index0, int index1) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#moveMarkable(de.ims.icarus.language.model.api.Markable, int)
		 */
		@Override
		public void moveMarkable(Markable markable, int index) {
			throw new UnsupportedOperationException();
		}

	}
}
