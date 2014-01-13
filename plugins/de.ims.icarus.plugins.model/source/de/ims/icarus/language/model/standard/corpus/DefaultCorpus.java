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

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.ContainerType;
import de.ims.icarus.language.model.Context;
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.CorpusMember;
import de.ims.icarus.language.model.IdDomain;
import de.ims.icarus.language.model.Layer;
import de.ims.icarus.language.model.LayerType;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.MarkableLayer;
import de.ims.icarus.language.model.MemberType;
import de.ims.icarus.language.model.edit.CorpusEditModel;
import de.ims.icarus.language.model.edit.CorpusUndoManager;
import de.ims.icarus.language.model.events.CorpusListener;
import de.ims.icarus.language.model.events.EventManager;
import de.ims.icarus.language.model.manifest.ContainerManifest;
import de.ims.icarus.language.model.manifest.CorpusManifest;
import de.ims.icarus.language.model.manifest.MarkableLayerManifest;
import de.ims.icarus.language.model.meta.MetaData;
import de.ims.icarus.language.model.registry.CorpusRegistry;
import de.ims.icarus.language.model.standard.container.AbstractContainer;
import de.ims.icarus.language.model.standard.context.DefaultContext;
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

	private LongHashMap<CorpusMember> globalLookup = new LongHashMap<>();
	private boolean useGlobalCache = false;

	public DefaultCorpus(CorpusManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		this.manifest = manifest;
		this.defaultContext = new DefaultContext(this, manifest.getDefaultContextManifest());
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Layer> iterator() {
		return new LayerIterator();
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getGlobalIdDomain()
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
	 * @see de.ims.icarus.language.model.Corpus#getLock()
	 */
	@Override
	public Lock getLock() {
		return lock;
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getEditModel()
	 */
	@Override
	public CorpusEditModel getEditModel() {
		return editModel;
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getUndoManager()
	 */
	@Override
	public CorpusUndoManager getUndoManager() {
		return undoManager;
	}

//	/**
//	 * @see de.ims.icarus.language.model.Corpus#addMember(de.ims.icarus.language.model.CorpusMember)
//	 */
//	@Override
//	public void addMember(CorpusMember member) {
//		// TODO Auto-generated method stub
//
//	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getUniqueName(java.lang.String)
	 */
	@Override
	public String getUniqueName(String baseName) {
		if (baseName == null)
			throw new NullPointerException("Invalid baseName"); //$NON-NLS-1$

		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getUniqueId(java.lang.String)
	 */
	@Override
	public String getUniqueId(String baseId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getDefaultContext()
	 */
	@Override
	public Context getDefaultContext() {
		return defaultContext;
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#addCorpusListener(de.ims.icarus.language.model.events.CorpusListener)
	 */
	@Override
	public void addCorpusListener(CorpusListener listener) {
		eventManager.addCorpusListener(listener);
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#removeCorpusListener(de.ims.icarus.language.model.events.CorpusListener)
	 */
	@Override
	public void removeCorpusListener(CorpusListener listener) {
		eventManager.removeCorpusListener(listener);
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getManifest()
	 */
	@Override
	public CorpusManifest getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getBaseLayer()
	 */
	@Override
	public MarkableLayer getBaseLayer() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getContexts()
	 */
	@Override
	public Set<Context> getContexts() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getOverlayLayer()
	 */
	@Override
	public MarkableLayer getOverlayLayer() {
		return overlayLayer;
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getLayers()
	 */
	@Override
	public List<Layer> getLayers() {
		return CollectionUtils.getListProxy(layers);
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getLayer(java.lang.String)
	 */
	@Override
	public Layer getLayer(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getLayers(de.ims.icarus.language.model.LayerType)
	 */
	@Override
	public List<Layer> getLayers(LayerType type) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#addLayer(de.ims.icarus.language.model.Layer)
	 */
	@Override
	public void addLayer(Layer layer) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#removeLayer(de.ims.icarus.language.model.Layer)
	 */
	@Override
	public void removeLayer(Layer layer) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#removeContext(de.ims.icarus.language.model.Context)
	 */
	@Override
	public void removeContext(Context context) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#addMetaData(de.ims.icarus.util.data.ContentType, de.ims.icarus.language.model.Layer, java.lang.Object)
	 */
	@Override
	public void addMetaData(ContentType type, Layer layer, Object data) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#removeMetaData(de.ims.icarus.util.data.ContentType, de.ims.icarus.language.model.Layer, java.lang.Object)
	 */
	@Override
	public void removeMetaData(ContentType type, Layer layer, Object data) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#getMetaData(de.ims.icarus.util.data.ContentType, de.ims.icarus.language.model.Layer)
	 */
	@Override
	public Set<MetaData> getMetaData(ContentType type, Layer layer) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Corpus#free()
	 */
	@Override
	public void free() {
		// TODO Auto-generated method stub

	}

	private class OverlayLayer implements MarkableLayer {

		private final long id = CorpusRegistry.getInstance().newId();

		/**
		 * @see de.ims.icarus.language.model.Layer#getName()
		 */
		@Override
		public String getName() {
			return DefaultCorpus.this.getManifest().getName()+" Overlay Layer"; //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.language.model.Layer#getMarkableProxy()
		 */
		@Override
		public Markable getMarkableProxy() {
			// Not supported by this layer!
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.Layer#getLayerType()
		 */
		@Override
		public LayerType getLayerType() {
		}

		/**
		 * @see de.ims.icarus.language.model.Layer#getContext()
		 */
		@Override
		public Context getContext() {
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.Layer#getBaseLayer()
		 */
		@Override
		public MarkableLayer getBaseLayer() {
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.Layer#addNotify(de.ims.icarus.language.model.Corpus)
		 */
		@Override
		public void addNotify(Corpus corpus) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.Layer#removeNotify(de.ims.icarus.language.model.Corpus)
		 */
		@Override
		public void removeNotify(Corpus corpus) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.CorpusMember#getId()
		 */
		@Override
		public long getId() {
			return id;
		}

		/**
		 * @see de.ims.icarus.language.model.CorpusMember#getCorpus()
		 */
		@Override
		public Corpus getCorpus() {
			return DefaultCorpus.this;
		}

		/**
		 * @see de.ims.icarus.language.model.CorpusMember#getMemberType()
		 */
		@Override
		public MemberType getMemberType() {
			return MemberType.LAYER;
		}

		/**
		 * @see de.ims.icarus.language.model.MarkableLayer#getManifest()
		 */
		@Override
		public MarkableLayerManifest getManifest() {
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.MarkableLayer#getContainer()
		 */
		@Override
		public Container getContainer() {
			return overlayContainer;
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
		 * @see de.ims.icarus.language.model.Markable#getContainer()
		 */
		@Override
		public Container getContainer() {
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.Markable#getLayer()
		 */
		@Override
		public MarkableLayer getLayer() {
			return DefaultCorpus.this.overlayLayer;
		}

		/**
		 * @see de.ims.icarus.language.model.Markable#getBeginOffset()
		 */
		@Override
		public int getBeginOffset() {
			return -1;
		}

		/**
		 * @see de.ims.icarus.language.model.Markable#getEndOffset()
		 */
		@Override
		public int getEndOffset() {
			return -1;
		}

		/**
		 * @see de.ims.icarus.language.model.CorpusMember#getCorpus()
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
		 * @see de.ims.icarus.language.model.Container#getContainerType()
		 */
		@Override
		public ContainerType getContainerType() {
			return ContainerType.LIST;
		}

		/**
		 * @see de.ims.icarus.language.model.Container#getManifest()
		 */
		@Override
		public ContainerManifest getManifest() {
			return null;
		}

		/**
		 * @see de.ims.icarus.language.model.Container#getMarkableCount()
		 */
		@Override
		public int getMarkableCount() {
			return layers.size();
		}

		/**
		 * @see de.ims.icarus.language.model.Container#getMarkableAt(int)
		 */
		@Override
		public Markable getMarkableAt(int index) {
			return layers.get(index).getMarkableProxy();
		}

		/**
		 * @see de.ims.icarus.language.model.Container#removeAllMarkables()
		 */
		@Override
		public void removeAllMarkables() {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.Container#addMarkable(int, de.ims.icarus.language.model.Markable)
		 */
		@Override
		public void addMarkable(int index, Markable markable) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.Container#removeMarkable(int)
		 */
		@Override
		public Markable removeMarkable(int index) {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.language.model.Container#moveMarkable(int, int)
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
