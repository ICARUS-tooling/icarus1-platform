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

import de.ims.icarus.language.model.Context;
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.Layer;
import de.ims.icarus.language.model.LayerType;
import de.ims.icarus.language.model.MarkableLayer;
import de.ims.icarus.language.model.MemberType;
import de.ims.icarus.language.model.manifest.LayerManifest;
import de.ims.icarus.language.model.registry.CorpusRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AbstractLayer implements Layer {

	private long id = CorpusRegistry.getInstance().newId();

	private final Context context;
	private final LayerManifest manifest;
	private MarkableLayer baseLayer;
	private LayerType layerType;

	public AbstractLayer(Context context, LayerManifest manifest) {
		if (context == null)
			throw new NullPointerException("Invalid context");  //$NON-NLS-1$
		if (manifest == null)
			throw new NullPointerException("Invalid manifest");  //$NON-NLS-1$

		this.context = context;
		this.manifest = manifest;
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
	 * @see de.ims.icarus.language.model.Layer#getType()
	 */
	@Override
	public LayerType getType() {
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
	public LayerManifest getManifest() {
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

}
