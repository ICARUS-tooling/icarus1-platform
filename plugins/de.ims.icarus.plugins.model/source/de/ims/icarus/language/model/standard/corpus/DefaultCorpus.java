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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.ims.icarus.language.model.api.ChunkControl;
import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.ContainerType;
import de.ims.icarus.language.model.api.Context;
import de.ims.icarus.language.model.api.Corpus;
import de.ims.icarus.language.model.api.CorpusMember;
import de.ims.icarus.language.model.api.IdDomain;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.MemberType;
import de.ims.icarus.language.model.api.edit.CorpusEditModel;
import de.ims.icarus.language.model.api.edit.CorpusUndoManager;
import de.ims.icarus.language.model.api.events.CorpusListener;
import de.ims.icarus.language.model.api.events.EventManager;
import de.ims.icarus.language.model.api.layer.Layer;
import de.ims.icarus.language.model.api.layer.LayerType;
import de.ims.icarus.language.model.api.layer.MarkableLayer;
import de.ims.icarus.language.model.api.manifest.ContainerManifest;
import de.ims.icarus.language.model.api.manifest.CorpusManifest;
import de.ims.icarus.language.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.language.model.api.meta.MetaData;
import de.ims.icarus.language.model.registry.CorpusRegistry;
import de.ims.icarus.language.model.standard.context.DefaultContext;
import de.ims.icarus.language.model.standard.elements.AbstractContainer;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.collections.LongHashMap;
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

	private final List<Layer> layers = new ArrayList<>();

	private final OverlayLayer overlayLayer;
	private final OverlayContainer overlayContainer;

	// Id pools
	private final IdPool globalIdDomain;

	private final IdDomain internalIdDomain;

	private LongHashMap<CorpusMember> globalLookup = new LongHashMap<>();
	private boolean useGlobalCache = false;

	public DefaultCorpus(CorpusManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		this.manifest = manifest;
		this.defaultContext = new DefaultContext(this, manifest.getDefaultContextManifest());

		globalIdDomain = new IdPool();
		internalIdDomain = globalIdDomain.reserve(100);

		overlayLayer = new OverlayLayer(internalIdDomain.nextId());
		overlayContainer = new OverlayContainer(internalIdDomain.nextId());
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getChunkControl()
	 */
	@Override
	public ChunkControl getChunkControl() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getCustomContexts()
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
	 * @see de.ims.icarus.language.model.api.Corpus#getGlobalIdDomain()
	 */
	@Override
	public IdDomain getGlobalIdDomain() {
		return globalIdDomain;
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoaded()
	 */
	@Override
	public boolean isLoaded() {
		return defaultContext.isLoaded();
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoading()
	 */
	@Override
	public boolean isLoading() {
		return defaultContext.isLoading();
	}

	/**
	 * @see de.ims.icarus.io.Loadable#load()
	 */
	@Override
	public void load() throws Exception {
		defaultContext.load();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getLock()
	 */
	@Override
	public Lock getLock() {
		return lock;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getEditModel()
	 */
	@Override
	public CorpusEditModel getEditModel() {
		return editModel;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getUndoManager()
	 */
	@Override
	public CorpusUndoManager getUndoManager() {
		return undoManager;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getDefaultContext()
	 */
	@Override
	public Context getDefaultContext() {
		return defaultContext;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#addCorpusListener(de.ims.icarus.language.model.api.events.CorpusListener)
	 */
	@Override
	public void addCorpusListener(CorpusListener listener) {
		eventManager.addCorpusListener(listener);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#removeCorpusListener(de.ims.icarus.language.model.api.events.CorpusListener)
	 */
	@Override
	public void removeCorpusListener(CorpusListener listener) {
		eventManager.removeCorpusListener(listener);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getManifest()
	 */
	@Override
	public CorpusManifest getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getBaseLayer()
	 */
	@Override
	public MarkableLayer getBaseLayer() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getContexts()
	 */
	@Override
	public Set<Context> getContexts() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getOverlayLayer()
	 */
	@Override
	public MarkableLayer getOverlayLayer() {
		return overlayLayer;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getLayers()
	 */
	@Override
	public List<Layer> getLayers() {
		return CollectionUtils.getListProxy(layers);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getLayer(java.lang.String)
	 */
	@Override
	public Layer getLayer(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getLayers(de.ims.icarus.language.model.api.layer.LayerType)
	 */
	@Override
	public List<Layer> getLayers(LayerType type) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#addLayer(de.ims.icarus.language.model.api.layer.Layer)
	 */
	@Override
	public void addLayer(Layer layer) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#removeLayer(de.ims.icarus.language.model.api.layer.Layer)
	 */
	@Override
	public void removeLayer(Layer layer) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#removeContext(de.ims.icarus.language.model.api.Context)
	 */
	@Override
	public void removeContext(Context context) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#addMetaData(de.ims.icarus.util.data.ContentType, de.ims.icarus.language.model.api.layer.Layer, java.lang.Object)
	 */
	@Override
	public void addMetaData(ContentType type, Layer layer, Object data) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#removeMetaData(de.ims.icarus.util.data.ContentType, de.ims.icarus.language.model.api.layer.Layer, java.lang.Object)
	 */
	@Override
	public void removeMetaData(ContentType type, Layer layer, Object data) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#getMetaData(de.ims.icarus.util.data.ContentType, de.ims.icarus.language.model.api.layer.Layer)
	 */
	@Override
	public Set<MetaData> getMetaData(ContentType type, Layer layer) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Corpus#free()
	 */
	@Override
	public void free() {
		// TODO Auto-generated method stub

	}

	private class OverlayLayer implements MarkableLayer {

		private final long id;

		public OverlayLayer(long id) {
			this.id = id;
		}

		/**
		 * @see de.ims.icarus.language.model.api.layer.Layer#getName()
		 */
		@Override
		public String getName() {
			return DefaultCorpus.this.getManifest().getName()+" Overlay Layer"; //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.language.model.api.layer.Layer#getMarkableProxy()
		 */
		@Override
		public Markable getMarkableProxy() {
			// Not supported by this layer!
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.api.layer.Layer#getLayerType()
		 */
		@Override
		public LayerType getLayerType() {
			return CorpusRegistry.getInstance().getOverlayLayerType();
		}

		/**
		 * @see de.ims.icarus.language.model.api.layer.Layer#getContext()
		 */
		@Override
		public Context getContext() {
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.api.layer.Layer#getBaseLayer()
		 */
		@Override
		public MarkableLayer getBaseLayer() {
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.api.layer.Layer#addNotify(de.ims.icarus.language.model.api.Corpus)
		 */
		@Override
		public void addNotify(Corpus corpus) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.api.layer.Layer#removeNotify(de.ims.icarus.language.model.api.Corpus)
		 */
		@Override
		public void removeNotify(Corpus corpus) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.api.CorpusMember#getId()
		 */
		@Override
		public long getId() {
			return id;
		}

		/**
		 * @see de.ims.icarus.language.model.api.CorpusMember#getCorpus()
		 */
		@Override
		public Corpus getCorpus() {
			return DefaultCorpus.this;
		}

		/**
		 * @see de.ims.icarus.language.model.api.CorpusMember#getMemberType()
		 */
		@Override
		public MemberType getMemberType() {
			return MemberType.LAYER;
		}

		/**
		 * @see de.ims.icarus.language.model.api.layer.MarkableLayer#getManifest()
		 */
		@Override
		public MarkableLayerManifest getManifest() {
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.api.layer.MarkableLayer#getContainer()
		 */
		@Override
		public Container getContainer() {
			return overlayContainer;
		}

		/**
		 * No boundary on overlay layer!
		 * @see de.ims.icarus.language.model.api.layer.MarkableLayer#getBoundaryLayer()
		 */
		@Override
		public MarkableLayer getBoundaryLayer() {
			return null;
		}

	}


	private class OverlayContainer extends AbstractContainer {

		/**
		 * @param id
		 */
		public OverlayContainer(long id) {
			super(id);
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
			return DefaultCorpus.this.overlayLayer;
		}

		/**
		 * @see de.ims.icarus.language.model.api.Markable#getBeginOffset()
		 */
		@Override
		public int getBeginOffset() {
			return -1;
		}

		/**
		 * @see de.ims.icarus.language.model.api.Markable#getEndOffset()
		 */
		@Override
		public int getEndOffset() {
			return -1;
		}

		/**
		 * @see de.ims.icarus.language.model.api.CorpusMember#getCorpus()
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
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#getMarkableCount()
		 */
		@Override
		public int getMarkableCount() {
			return layers.size();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#getMarkableAt(int)
		 */
		@Override
		public Markable getMarkableAt(int index) {
			return layers.get(index).getMarkableProxy();
		}

		/**
		 * @see de.ims.icarus.language.model.api.Container#removeAllMarkables()
		 */
		@Override
		public void removeAllMarkables() {
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
		 * @see de.ims.icarus.language.model.api.Container#moveMarkable(int, int)
		 */
		@Override
		public void moveMarkable(int index0, int index1) {
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
