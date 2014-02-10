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
package de.ims.icarus.language.model.standard.layer;

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.Context;
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.Layer;
import de.ims.icarus.language.model.LayerType;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.MarkableLayer;
import de.ims.icarus.language.model.MemberType;
import de.ims.icarus.language.model.manifest.LayerManifest;
import de.ims.icarus.language.model.util.CorpusUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AbstractLayer<M extends LayerManifest> implements Layer {

	private final long id;

	private final Context context;
	private final M manifest;
	private MarkableLayer baseLayer;
	private LayerType layerType;

	private final Markable markableProxy;

	public AbstractLayer(long id, Context context, M manifest) {
		if (context == null)
			throw new NullPointerException("Invalid context");  //$NON-NLS-1$
		if (manifest == null)
			throw new NullPointerException("Invalid manifest");  //$NON-NLS-1$

		this.id = id;
		this.context = context;
		this.manifest = manifest;

		markableProxy = new ProxyMarkable();
	}

	/**
	 * @param baseLayer the baseLayer to set
	 */
	public void setBaseLayer(MarkableLayer baseLayer) {
		this.baseLayer = baseLayer;
	}

	/**
	 * @param layerType the layerType to set
	 */
	public void setLayerType(LayerType layerType) {
		if (layerType == null)
			throw new NullPointerException("Invalid layer-type");  //$NON-NLS-1$

		this.layerType = layerType;
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
		return getContext().getCorpus();
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.LAYER;
	}

	/**
	 * @see de.ims.icarus.language.model.Layer#getName()
	 */
	@Override
	public String getName() {
		return getManifest().getName();
	}

	/**
	 * @see de.ims.icarus.language.model.Layer#getLayerType()
	 */
	@Override
	public LayerType getLayerType() {
		return layerType;
	}

	/**
	 * @see de.ims.icarus.language.model.Layer#getContext()
	 */
	@Override
	public Context getContext() {
		return context;
	}

	/**
	 * @see de.ims.icarus.language.model.Layer#getBaseLayer()
	 */
	@Override
	public MarkableLayer getBaseLayer() {
		return baseLayer;
	}

	/**
	 * @see de.ims.icarus.language.model.Layer#getManifest()
	 */
	@Override
	public M getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus.language.model.Layer#addNotify(de.ims.icarus.language.model.Corpus)
	 */
	@Override
	public void addNotify(Corpus corpus) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.model.Layer#removeNotify(de.ims.icarus.language.model.Corpus)
	 */
	@Override
	public void removeNotify(Corpus corpus) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.model.Layer#getMarkableProxy()
	 */
	@Override
	public Markable getMarkableProxy() {
		return markableProxy;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) id;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Layer) {
			Layer other = (Layer) obj;
			return other.getId()==id && other.getManifest()==manifest;
		}

		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	public class ProxyMarkable implements Markable {

		private final long id = getCorpus().getGlobalIdDomain().nextId();

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
			return AbstractLayer.this.getCorpus();
		}

		/**
		 * @see de.ims.icarus.language.model.CorpusMember#getMemberType()
		 */
		@Override
		public MemberType getMemberType() {
			return MemberType.MARKABLE;
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Markable o) {
			return CorpusUtils.compare(this, o);
		}

		/**
		 * @see de.ims.icarus.language.model.Markable#getContainer()
		 */
		@Override
		public Container getContainer() {
			return getCorpus().getOverlayLayer().getContainer();
		}

		/**
		 * @see de.ims.icarus.language.model.Markable#getLayer()
		 */
		@Override
		public MarkableLayer getLayer() {
			return getCorpus().getOverlayLayer();
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

	}
}
