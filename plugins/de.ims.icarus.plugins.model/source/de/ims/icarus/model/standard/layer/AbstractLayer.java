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
package de.ims.icarus.model.standard.layer;

import de.ims.icarus.model.api.Context;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.layer.Layer;
import de.ims.icarus.model.api.layer.LayerGroup;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.Markable;
import de.ims.icarus.model.api.members.MemberSet;
import de.ims.icarus.model.api.members.MemberType;
import de.ims.icarus.model.util.CorpusUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AbstractLayer<M extends LayerManifest> implements Layer {

	private final Context context;
	private final M manifest;
	private MemberSet<MarkableLayer> baseLayers = EMPTY_BASE_SET;
	private final LayerGroup group;

	private final int uid = CorpusUtils.getNewUID();

	private final Markable markableProxy;

	public AbstractLayer(M manifest, LayerGroup group) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest");  //$NON-NLS-1$
		if (group == null)
			throw new NullPointerException("Invalid group"); //$NON-NLS-1$

		this.context = group.getContext();
		this.manifest = manifest;
		this.group = group;

		markableProxy = new ProxyMarkable();
	}

	/**
	 * @param baseLayer the baseLayer to set
	 */
	public void setBaseLayers(MemberSet<MarkableLayer> baseLayers) {
		if (baseLayers == null)
			throw new NullPointerException("Invalid baseLayers"); //$NON-NLS-1$

		this.baseLayers = baseLayers;
	}

	/**
	 * @see de.ims.icarus.model.api.members.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return getContext().getCorpus();
	}

	/**
	 * @see de.ims.icarus.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.LAYER;
	}

	/**
	 * @see de.ims.icarus.model.api.layer.Layer#getName()
	 */
	@Override
	public String getName() {
		return getManifest().getName();
	}

	/**
	 * @see de.ims.icarus.model.api.layer.Layer#getContext()
	 */
	@Override
	public Context getContext() {
		return context;
	}

	/**
	 * @see de.ims.icarus.model.api.layer.Layer#getBaseLayer()
	 */
	@Override
	public MemberSet<MarkableLayer> getBaseLayers() {
		return baseLayers;
	}

	/**
	 * @see de.ims.icarus.model.api.layer.Layer#getManifest()
	 */
	@Override
	public M getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus.model.api.layer.Layer#addNotify(de.ims.icarus.model.api.Corpus)
	 */
	@Override
	public void addNotify(Corpus corpus) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.model.api.layer.Layer#removeNotify(de.ims.icarus.model.api.Corpus)
	 */
	@Override
	public void removeNotify(Corpus corpus) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.model.api.layer.Layer#getMarkableProxy()
	 */
	@Override
	public Markable getMarkableProxy() {
		return markableProxy;
	}

	/**
	 * @see de.ims.icarus.model.api.layer.Layer#getLayerGroup()
	 */
	@Override
	public LayerGroup getLayerGroup() {
		return group;
	}

	/**
	 * @see de.ims.icarus.model.api.layer.Layer#getUID()
	 */
	@Override
	public int getUID() {
		return uid;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	public class ProxyMarkable implements Markable {

		/**
		 * @see de.ims.icarus.model.api.members.CorpusMember#getCorpus()
		 */
		@Override
		public Corpus getCorpus() {
			return AbstractLayer.this.getCorpus();
		}

		/**
		 * @see de.ims.icarus.model.api.members.CorpusMember#getMemberType()
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
		 * @see de.ims.icarus.model.api.members.Markable#getContainer()
		 */
		@Override
		public Container getContainer() {
			return getCorpus().getOverlayContainer();
		}

		/**
		 * @see de.ims.icarus.model.api.members.Markable#getLayer()
		 */
		@Override
		public MarkableLayer getLayer() {
			return getCorpus().getOverlayLayer();
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
			throw new UnsupportedOperationException("Proxy markables cannot have index values assigned"); //$NON-NLS-1$
		}

	}
}
