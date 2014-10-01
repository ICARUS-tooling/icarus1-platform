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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.Context;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.SubCorpus;
import de.ims.icarus.model.api.edit.CorpusEditModel;
import de.ims.icarus.model.api.edit.CorpusUndoManager;
import de.ims.icarus.model.api.events.CorpusListener;
import de.ims.icarus.model.api.events.EventManager;
import de.ims.icarus.model.api.layer.Layer;
import de.ims.icarus.model.api.layer.LayerGroup;
import de.ims.icarus.model.api.layer.LayerType;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.ContainerType;
import de.ims.icarus.model.api.members.Markable;
import de.ims.icarus.model.api.members.MemberSet;
import de.ims.icarus.model.api.members.MemberType;
import de.ims.icarus.model.api.meta.MetaData;
import de.ims.icarus.model.iql.Query;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultCorpus implements Corpus {

	private final CorpusManifest manifest;

	private final EventManager eventManager = new EventManager(this);
	private final CorpusEditModel editModel = new CorpusEditModel(this);
	private final CorpusUndoManager undoManager = new CorpusUndoManager(this);

	private final DefaultContext defaultContext;

	private final Lock lock = new ReentrantLock();

	// All contained layers, not including the overlay layer!
	private final List<Layer> layers = new ArrayList<>();

	private final OverlayLayer overlayLayer;
	private final OverlayContainer overlayContainer;

	public DefaultCorpus(CorpusManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		this.manifest = manifest;
		this.defaultContext = new DefaultContext(this, manifest.getRootContextManifest());

		overlayLayer = new OverlayLayer();
		overlayContainer = new OverlayContainer();
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getCustomContexts()
	 */
	@Override
	public List<Context> getCustomContexts() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Layer> iterator() {
		return new LayerIterator();
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getLock()
	 */
	@Override
	public Lock getLock() {
		return lock;
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getEditModel()
	 */
	@Override
	public CorpusEditModel getEditModel() {
		return editModel;
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getUndoManager()
	 */
	@Override
	public CorpusUndoManager getUndoManager() {
		return undoManager;
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getDefaultContext()
	 */
	@Override
	public Context getDefaultContext() {
		return defaultContext;
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#addCorpusListener(de.ims.icarus.model.api.events.CorpusListener)
	 */
	@Override
	public void addCorpusListener(CorpusListener listener) {
		eventManager.addCorpusListener(listener);
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#removeCorpusListener(de.ims.icarus.model.api.events.CorpusListener)
	 */
	@Override
	public void removeCorpusListener(CorpusListener listener) {
		eventManager.removeCorpusListener(listener);
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getManifest()
	 */
	@Override
	public CorpusManifest getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getBaseLayer()
	 */
	@Override
	public MarkableLayer getBaseLayer() {
		//FIXME need more elegant solution!
		return (MarkableLayer) getDefaultContext().getLayers().get(0);
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getContexts()
	 */
	@Override
	public Set<Context> getContexts() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getOverlayLayer()
	 */
	@Override
	public MarkableLayer getOverlayLayer() {
		return overlayLayer;
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getLayers()
	 */
	@Override
	public List<Layer> getLayers() {
		return CollectionUtils.getListProxy(layers);
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getLayers(de.ims.icarus.model.api.layer.LayerType)
	 */
	@Override
	public List<Layer> getLayers(LayerType type) {
		if (type == null)
			throw new NullPointerException("Invalid type");

		List<Layer> result = new ArrayList<>();

		for(Layer layer : layers) {
			if(layer.getManifest().getLayerType()==type) {
				result.add(layer);
			}
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#addLayer(de.ims.icarus.model.api.layer.Layer)
	 */
	@Override
	public void addLayer(Layer layer) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#removeLayer(de.ims.icarus.model.api.layer.Layer)
	 */
	@Override
	public void removeLayer(Layer layer) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#removeContext(de.ims.icarus.model.api.Context)
	 */
	@Override
	public void removeContext(Context context) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#addMetaData(de.ims.icarus.util.data.ContentType, de.ims.icarus.model.api.layer.Layer, java.lang.Object)
	 */
	@Override
	public void addMetaData(ContentType type, Layer layer, Object data) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#removeMetaData(de.ims.icarus.util.data.ContentType, de.ims.icarus.model.api.layer.Layer, java.lang.Object)
	 */
	@Override
	public void removeMetaData(ContentType type, Layer layer, Object data) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getMetaData(de.ims.icarus.util.data.ContentType, de.ims.icarus.model.api.layer.Layer)
	 */
	@Override
	public Set<MetaData> getMetaData(ContentType type, Layer layer) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getSegment(de.ims.icarus.model.iql.Query)
	 */
	@Override
	public SubCorpus createSubCorpus(Query query) throws ModelException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.Corpus#getOverlayContainer()
	 */
	@Override
	public Container getOverlayContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	private class OverlayLayer implements MarkableLayer {

		/**
		 * @see de.ims.icarus.model.api.layer.Layer#getName()
		 */
		@Override
		public String getName() {
			return DefaultCorpus.this.getManifest().getName()+" Overlay Layer"; //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.model.api.layer.Layer#getMarkableProxy()
		 */
		@Override
		public Markable getMarkableProxy() {
			// Not supported by this layer!
			return null;
		}

		/**
		 * @see de.ims.icarus.model.api.layer.Layer#getContext()
		 */
		@Override
		public Context getContext() {
			return null;
		}

		/**
		 * @see de.ims.icarus.model.api.layer.Layer#getBaseLayer()
		 */
		@Override
		public MemberSet<MarkableLayer> getBaseLayers() {
			return null;
		}

		/**
		 * @see de.ims.icarus.model.api.layer.Layer#addNotify(de.ims.icarus.model.api.Corpus)
		 */
		@Override
		public void addNotify(Corpus corpus) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.layer.Layer#removeNotify(de.ims.icarus.model.api.Corpus)
		 */
		@Override
		public void removeNotify(Corpus corpus) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.CorpusMember#getCorpus()
		 */
		@Override
		public Corpus getCorpus() {
			return DefaultCorpus.this;
		}

		/**
		 * @see de.ims.icarus.model.api.members.CorpusMember#getMemberType()
		 */
		@Override
		public MemberType getMemberType() {
			return MemberType.LAYER;
		}

		/**
		 * @see de.ims.icarus.model.api.layer.MarkableLayer#getManifest()
		 */
		@Override
		public MarkableLayerManifest getManifest() {
			return null;
		}

		/**
		 * No boundary on overlay layer!
		 * @see de.ims.icarus.model.api.layer.MarkableLayer#getBoundaryLayer()
		 */
		@Override
		public MarkableLayer getBoundaryLayer() {
			return null;
		}

		/**
		 * The artificial overlay layer is so far the only layer that is allowed to be
		 * without a hosting layer group!
		 *
		 * FIXME: re-evaluate that situation and maybe introduce a dummy group
		 *
		 * @see de.ims.icarus.model.api.layer.Layer#getLayerGroup()
		 */
		@Override
		public LayerGroup getLayerGroup() {
			return null;
		}

	}


	private class OverlayContainer implements Container {

		/**
		 * @see de.ims.icarus.model.api.members.Markable#getContainer()
		 */
		@Override
		public Container getContainer() {
			return null;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Markable#getLayer()
		 */
		@Override
		public MarkableLayer getLayer() {
			return DefaultCorpus.this.overlayLayer;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Markable#getBeginOffset()
		 */
		@Override
		public long getBeginOffset() {
			return -1;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Markable#getEndOffset()
		 */
		@Override
		public long getEndOffset() {
			return -1;
		}

		/**
		 * @see de.ims.icarus.model.api.members.CorpusMember#getCorpus()
		 */
		@Override
		public Corpus getCorpus() {
			return DefaultCorpus.this;
		}

		/**
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<Markable> iterator() {
			return new OverlayIterator();
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
			return null;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#getMarkableCount()
		 */
		@Override
		public int getMarkableCount() {
			return layers.size();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#getMarkableAt(int)
		 */
		@Override
		public Markable getMarkableAt(int index) {
			return layers.get(index).getMarkableProxy();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#removeAllMarkables()
		 */
		@Override
		public void removeAllMarkables() {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#addMarkable(int, de.ims.icarus.model.api.members.Markable)
		 */
		@Override
		public void addMarkable(int index, Markable markable) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#removeMarkable(int)
		 */
		@Override
		public Markable removeMarkable(int index) {
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
		 * @see de.ims.icarus.model.api.members.Markable#getIndex()
		 */
		@Override
		public long getIndex() {
			return -1;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Markable#setIndex(long)
		 */
		@Override
		public void setIndex(long newIndex) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.CorpusMember#getMemberType()
		 */
		@Override
		public MemberType getMemberType() {
			return MemberType.CONTAINER;
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Markable o) {
			return CorpusUtils.compare(this, o);
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#getBaseContainers()
		 */
		@Override
		public MemberSet<Container> getBaseContainers() {
			return null;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#getBoundaryContainer()
		 */
		@Override
		public Container getBoundaryContainer() {
			return null;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#indexOfMarkable(de.ims.icarus.model.api.members.Markable)
		 */
		@Override
		public int indexOfMarkable(Markable markable) {
			for(int i=layers.size(); --i>=0;) {
				if(layers.get(i).getMarkableProxy()==markable) {
					return i;
				}
			}

			return -1;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#containsMarkable(de.ims.icarus.model.api.members.Markable)
		 */
		@Override
		public boolean containsMarkable(Markable markable) {
			return indexOfMarkable(markable)!=-1;
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#addMarkable(de.ims.icarus.model.api.members.Markable)
		 */
		@Override
		public void addMarkable(Markable markable) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#removeMarkable(de.ims.icarus.model.api.members.Markable)
		 */
		@Override
		public Markable removeMarkable(Markable markable) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Container#moveMarkable(de.ims.icarus.model.api.members.Markable, int)
		 */
		@Override
		public void moveMarkable(Markable markable, int index) {
			throw new UnsupportedOperationException();
		}

	}

	private class OverlayIterator implements Iterator<Markable> {
		private final Iterator<Layer> delegate = layers.iterator();

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Markable next() {
			return delegate.next().getMarkableProxy();
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private class LayerIterator implements Iterator<Layer> {
		private final Iterator<Layer> delegate = layers.iterator();

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Layer next() {
			return delegate.next();
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}
}
