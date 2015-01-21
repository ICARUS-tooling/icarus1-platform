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
package de.ims.icarus.model.standard.corpus;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.Scope;
import de.ims.icarus.model.api.CorpusView;
import de.ims.icarus.model.api.CorpusOwner;
import de.ims.icarus.model.api.driver.ChunkStorage;
import de.ims.icarus.model.api.driver.Driver;
import de.ims.icarus.model.api.driver.IndexSet;
import de.ims.icarus.model.api.driver.IndexUtils;
import de.ims.icarus.model.api.layer.Layer;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.ContainerType;
import de.ims.icarus.model.api.members.Item;
import de.ims.icarus.model.api.members.MemberSet;
import de.ims.icarus.model.api.members.MemberType;
import de.ims.icarus.model.iql.Query;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.collections.IdentityHashSet;
import de.ims.icarus.util.collections.LookupList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
abstract class AbstractSegment implements CorpusView {

	private final DefaultCorpus corpus;
	private final Query query;

	private final Map<MarkableLayer, ProxyContainer> containers;

	private final Set<CorpusOwner> owners = new IdentityHashSet<>();
	private volatile boolean closed = false;
	private volatile boolean closing = false;


	// When a markable gets added to a proxy container, make sure its host container
	// is a legal base container for the target
	private boolean checkContainersOnAdd = true;

	private boolean checkIndicesOnAdd = true;

	/**
	 * Maximum initial capacity used for proxy containers
	 */
	//TODO needs adjustments!
	public static final int MAX_INITIAL_CAPACITY = 100_000;

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
		} catch (ModelException e) {
			LoggerFactory.error(this, "Failed to fetch cached number of members in markable layer: "+layer, e); //$NON-NLS-1$
		}

		if(memberCount==-1) {
			memberCount = 10000;
		}

		return (int)Math.min(memberCount, MAX_INITIAL_CAPACITY);
	}

	protected int estimateContainerCapacity(IndexSet[] indices) {
		long count = IndexUtils.count(indices);
		return (int)Math.min(count, MAX_INITIAL_CAPACITY);
	}

	/**
	 * @see de.ims.icarus.model.api.CorpusView#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @see de.ims.icarus.model.api.CorpusView#getScope()
	 */
	@Override
	public Scope getScope() {
		return query.getResultScope();
	}

	/**
	 * @see de.ims.icarus.model.api.CorpusView#getQuery()
	 */
	@Override
	public Query getQuery() {
		return query;
	}

	private ProxyContainer getProxyContainer(MarkableLayer layer) {
		return containers.get(layer);
	}

	/**
	 * @see de.ims.icarus.model.api.CorpusView#getContainer(de.ims.icarus.model.api.layer.MarkableLayer)
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
	 * @see de.ims.icarus.model.api.CorpusView#closable()
	 */
	@Override
	public boolean closable() {
		synchronized (owners) {
			checkOpen();

			return owners.isEmpty();
		}
	}

	/**
	 * @throws InterruptedException
	 * @see de.ims.icarus.model.api.CorpusView#close()
	 */
	@Override
	public void close() throws ModelException, InterruptedException {
		synchronized (owners) {
			checkOpen();

			for(Iterator<CorpusOwner> it = owners.iterator(); it.hasNext();) {
				CorpusOwner owner = it.next();

				if(owner.release()) {
					it.remove();
				} else
					throw new ModelException(ModelError.VIEW_UNCLOSABLE,
							"Unable to close segment - could not release ownership of "+owner.getName()); //$NON-NLS-1$
			}

			closing = true;
		}

		// At this point no owners may prevent the segment from closing.
		// Therefore simply free the content of the current page
		try {
			freePage();
		} finally {
			closed = true;
		}
	}

	/**
	 * Releases all the data stored in the current page.
	 *
	 * @throws ModelException
	 */
	protected void freePage() throws ModelException, InterruptedException {

		Set<Driver> drivers = new HashSet<>();

		// Collect affected drivers
		for(ProxyContainer container : containers.values()) {
			drivers.add(container.getLayer().getContext().getDriver());
		}

		for(Driver driver : drivers) {
			// Delegate data clean up to the appropriate driver
			driver.release(this);

		}

		for(ProxyContainer container : containers.values()) {
			// Finally empty all proxy containers
			container.clear();
		}
	}

	protected void loadPage(IndexSet[] indices) throws ModelException, InterruptedException {
		IndexUtils.check(indices);

		MarkableLayer primaryLayer = getPrimaryLayer();
		ProxyContainer container = getProxyContainer(primaryLayer);
		Driver driver = primaryLayer.getContext().getDriver();

		// Make some preparations
		container.ensureCapacity(estimateContainerCapacity(indices));

		// Delegate actual work to driver
		driver.load(indices, primaryLayer, container);

		// Finalize container
		container.sort();
	}

	public MarkableLayer getPrimaryLayer() {
		return getScope().getPrimaryLayer();
	}

	public Container getPrimaryContainer() {
		return getProxyContainer(getPrimaryLayer());
	}

	/**
	 * @see de.ims.icarus.model.api.CorpusView#getOwners()
	 */
	@Override
	public Set<CorpusOwner> getOwners() {
		synchronized (owners) {
			return CollectionUtils.getSetProxy(owners);
		}
	}

	/**
	 * @see de.ims.icarus.model.api.CorpusView#acquire(de.ims.icarus.model.api.CorpusOwner)
	 */
	@Override
	public void acquire(CorpusOwner owner) throws ModelException {
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
	 * @see de.ims.icarus.model.api.CorpusView#release(de.ims.icarus.model.api.CorpusOwner)
	 */
	@Override
	public void release(CorpusOwner owner) throws ModelException {
		if (owner == null)
			throw new NullPointerException("Invalid owner"); //$NON-NLS-1$

		synchronized (owners) {
			checkOpen();

			if(!owners.remove(owner))
				throw new IllegalArgumentException("Owner does not hold ownership of this segment: "+owner.getName()); //$NON-NLS-1$


		}
	}

	private static final Comparator<Item> idSorter = new Comparator<Item>() {

		@Override
		public int compare(Item o1, Item o2) {
			return (int)(o1.getIndex()-o2.getIndex());
		}
	};

	private class ChunkDelegate implements ChunkStorage {

		/**
		 * @see de.ims.icarus.model.api.driver.ChunkStorage#add(de.ims.icarus.model.api.members.Item, long)
		 */
		@Override
		public void add(Item member, long index) {
			ProxyContainer container = getProxyContainer(member.getLayer());
			if(container!=null) {
				container.add(member, index);
			}
		}

	}

	private class ProxyContainer implements Container, ChunkStorage {

		private long maxId = -1;
		private boolean sorted = true;

		private final LookupList<Item> items;
		private final MarkableLayer layer;

		private ProxyContainer(MarkableLayer layer, int capacity) {
			if (layer == null)
				throw new NullPointerException("Invalid layer"); //$NON-NLS-1$

			this.layer = layer;
			items = new LookupList<>(capacity);


		}

		/**
		 * @see de.ims.icarus.model.api.driver.ChunkStorage#add(de.ims.icarus.model.api.members.Item,long)
		 */
		@Override
		public void add(Item member, long index) {

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
		 * @see de.ims.icarus.model.api.members.Item#getContainer()
		 */
		@Override
		public Container getContainer() {
			return null;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Item#getLayer()
		 */
		@Override
		public MarkableLayer getLayer() {
			return layer;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Item#getIndex()
		 */
		@Override
		public long getIndex() {
			return -1L;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Item#setIndex(long)
		 */
		@Override
		public void setIndex(long newIndex) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Item#getBeginOffset()
		 */
		@Override
		public long getBeginOffset() {
			return items.first().getBeginOffset();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Item#getEndOffset()
		 */
		@Override
		public long getEndOffset() {
			return items.last().getEndOffset();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Item#compareTo(de.ims.icarus.model.api.members.Item)
		 */
		@Override
		public int compareTo(Item o) {
			return CorpusUtils.compare(this, o);
		}

		/**
		 * @see de.ims.icarus.model.api.members.CorpusMember#getCorpus()
		 */
		@Override
		public Corpus getCorpus() {
			return corpus;
		}

		/**
		 * @see de.ims.icarus.model.api.members.CorpusMember#getMemberType()
		 */
		@Override
		public MemberType getMemberType() {
			return MemberType.CONTAINER;
		}

		/**
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<Item> iterator() {
			return items.iterator();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#getContainerType()
		 */
		@Override
		public ContainerType getContainerType() {
			return ContainerType.LIST;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#getManifest()
		 */
		@Override
		public ContainerManifest getManifest() {
			return getLayer().getManifest().getRootContainerManifest();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#getBaseContainers()
		 */
		@Override
		public MemberSet<Container> getBaseContainers() {
			return Container.EMPTY_BASE_SET;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#getBoundaryContainer()
		 */
		@Override
		public Container getBoundaryContainer() {
			return null;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#getMarkableCount()
		 */
		@Override
		public int getMarkableCount() {
			return items.size();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#getItemAt(int)
		 */
		@Override
		public Item getItemAt(int index) {
			return items.get(index);
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#indexOfItem(de.ims.icarus.model.api.members.Item)
		 */
		@Override
		public int indexOfItem(Item item) {
			return items.indexOf(item);
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#containsItem(de.ims.icarus.model.api.members.Item)
		 */
		@Override
		public boolean containsItem(Item item) {
			return items.contains(item);
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#removeAllMarkables()
		 */
		@Override
		public void removeAllMarkables() {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#addItem(de.ims.icarus.model.api.members.Item)
		 */
		@Override
		public void addItem(Item item) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#addItem(int, de.ims.icarus.model.api.members.Item)
		 */
		@Override
		public void addItem(int index, Item item) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#removeItem(int)
		 */
		@Override
		public Item removeItem(int index) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#removeItem(de.ims.icarus.model.api.members.Item)
		 */
		@Override
		public Item removeItem(Item item) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#moveMarkable(int, int)
		 */
		@Override
		public void moveMarkable(int index0, int index1) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#moveItem(de.ims.icarus.model.api.members.Item, int)
		 */
		@Override
		public void moveItem(Item item, int index) {
			throw new UnsupportedOperationException();
		}

	}
}
