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

 * $Revision: 244 $
 * $Date: 2014-04-10 14:09:12 +0200 (Do, 10 Apr 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/source/de/ims/icarus/language/model/standard/context/DefaultContext.java $
 *
 * $LastChangedDate: 2014-04-10 14:09:12 +0200 (Do, 10 Apr 2014) $
 * $LastChangedRevision: 244 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.model.standard.corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.ims.icarus.model.api.Context;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.driver.Driver;
import de.ims.icarus.model.api.layer.Layer;
import de.ims.icarus.model.api.layer.LayerGroup;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id: DefaultContext.java 244 2014-04-10 12:09:12Z mcgaerty $
 *
 */
public class DefaultContext implements Context {

	private final Corpus corpus;
	private final ContextManifest manifest;
	private final Driver driver;

	private MarkableLayer primaryLayer;

	private final List<Layer> layers = new ArrayList<>(5);
	private final List<LayerGroup> layerGroups = new ArrayList<>(5);

	private final HashMap<String, Layer> layerLut = new HashMap<>();

	public DefaultContext(Corpus corpus, ContextManifest manifest, Driver driver) {
		if(corpus==null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$
		if(manifest==null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$
		if (driver == null)
			throw new NullPointerException("Invalid driver"); //$NON-NLS-1$

		this.corpus = corpus;
		this.manifest = manifest;
		this.driver = driver;
	}

	/**
	 * @see de.ims.icarus.model.api.Context#getPrimaryLayer()
	 */
	@Override
	public MarkableLayer getPrimaryLayer() {
		return primaryLayer;
	}

	/**
	 * Sets the primary layer for this context. Note that the primary layer must be
	 * added as regular layer prior to calling this method!
	 *
	 * @param primaryLayer the primaryLayer to set
	 */
	public void setPrimaryLayer(MarkableLayer primaryLayer) {

		if(primaryLayer!=null) {
			if(!layers.contains(primaryLayer))
				throw new IllegalArgumentException("Primary layer is unknown to this context: "+primaryLayer); //$NON-NLS-1$
			if(primaryLayer.getLayerGroup().getPrimaryLayer()!=primaryLayer)
				throw new IllegalArgumentException("Context primary layer must be the primary layer of the hosting group: "+primaryLayer); //$NON-NLS-1$
		}

		this.primaryLayer = primaryLayer;
	}

	/**
	 * @see de.ims.icarus.model.api.Context#getLayerGroups()
	 */
	@Override
	public List<LayerGroup> getLayerGroups() {
		return CollectionUtils.getListProxy(layerGroups);
	}

	public void addLayerGroup(LayerGroup group) {
		if (group == null)
			throw new NullPointerException("Invalid group"); //$NON-NLS-1$

		if(layerGroups.contains(group))
			throw new IllegalArgumentException("Layer group already added: "+CorpusUtils.getName(group)); //$NON-NLS-1$

		layerGroups.add(group);
	}

	/**
	 * @see de.ims.icarus.model.api.Context#getDriver()
	 */
	@Override
	public Driver getDriver() {
		return driver;
	}

	/**
	 * @see de.ims.icarus.model.api.Context#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @see de.ims.icarus.model.api.Context#getManifest()
	 */
	@Override
	public ContextManifest getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus.model.api.Context#addNotify(de.ims.icarus.model.api.Corpus)
	 */
	@Override
	public void addNotify(Corpus corpus) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.model.api.Context#removeNotify(de.ims.icarus.model.api.Corpus)
	 */
	@Override
	public void removeNotify(Corpus corpus) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.model.api.Context#getLayers()
	 */
	@Override
	public List<Layer> getLayers() {
		return CollectionUtils.getListProxy(layers);
	}

	/**
	 * @see de.ims.icarus.model.api.Context#getLayer(java.lang.String)
	 */
	@Override
	public Layer getLayer(String id) {
		return layerLut.get(id);
	}

	public void addLayer(Layer layer) {
		if(layer==null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$
		if(layer.getContext()!=this)
			throw new IllegalArgumentException("Foreign layer: "+CorpusUtils.getName(layer)); //$NON-NLS-1$

		String id = layer.getManifest().getId();

		if(layerLut.containsKey(id))
			throw new IllegalArgumentException("Layer id already mapped to different layer: "+id); //$NON-NLS-1$

		layers.add(layer);
		layerLut.put(id, layer);

		getCorpus().addLayer(layer);
	}

	public void removeLayer(Layer layer) {
		if(layer==null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$
		if(layer.getContext()!=this)
			throw new IllegalArgumentException("Foreign layer: "+CorpusUtils.getName(layer)); //$NON-NLS-1$

		String id = layer.getManifest().getId();

		if(!layers.remove(layer))
			throw new IllegalArgumentException("Unknown layer: "+CorpusUtils.getName(layer)); //$NON-NLS-1$
		layerLut.remove(id);

		getCorpus().removeLayer(layer);
	}
}
