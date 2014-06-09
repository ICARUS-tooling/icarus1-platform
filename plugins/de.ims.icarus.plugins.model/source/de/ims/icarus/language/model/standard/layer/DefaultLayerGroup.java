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

import java.util.Set;

import de.ims.icarus.language.model.api.Context;
import de.ims.icarus.language.model.api.layer.Dependency;
import de.ims.icarus.language.model.api.layer.Layer;
import de.ims.icarus.language.model.api.layer.LayerGroup;
import de.ims.icarus.language.model.api.layer.MarkableLayer;
import de.ims.icarus.language.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.language.model.util.CorpusUtils;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.collections.IdentityHashSet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultLayerGroup implements LayerGroup {

	private final Set<Layer> layers = new IdentityHashSet<>();
	private final Set<Dependency<LayerGroup>> dependencies = new IdentityHashSet<>();
	private final Context context;
	private final LayerGroupManifest manifest;
	private MarkableLayer primaryLayer;

	public DefaultLayerGroup(Context context, LayerGroupManifest manifest) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		this.context = context;
		this.manifest = manifest;
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.LayerGroup#getContext()
	 */
	@Override
	public Context getContext() {
		return context;
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.LayerGroup#getLayers()
	 */
	@Override
	public Set<Layer> getLayers() {
		return CollectionUtils.getSetProxy(layers);
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.LayerGroup#getManifest()
	 */
	@Override
	public LayerGroupManifest getManifest() {
		return manifest;
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.LayerGroup#getPrimaryLayer()
	 */
	@Override
	public MarkableLayer getPrimaryLayer() {
		return primaryLayer;
	}

	/**
	 * @see de.ims.icarus.language.model.api.layer.LayerGroup#getDependencies()
	 */
	@Override
	public Set<Dependency<LayerGroup>> getDependencies() {
		return CollectionUtils.getSetProxy(dependencies);
	}

	/**
	 * Changes the primary layer of this group. Note that the new primary layer must
	 * be added as regular layer to this group prior to making this method call!
	 *
	 * @param primaryLayer
	 */
	public void setPrimaryLayer(MarkableLayer primaryLayer) {
		if (primaryLayer == null)
			throw new NullPointerException("Invalid primaryLayer"); //$NON-NLS-1$
		if(!layers.contains(primaryLayer))
			throw new IllegalArgumentException("Layer is unknown to this group: "+CorpusUtils.getName(primaryLayer)); //$NON-NLS-1$

		this.primaryLayer = primaryLayer;
	}

	/**
	 * Adds the layer to the internal set of layers, discarding potential duplicates.
	 * @param layer
	 */
	public void addLayer(Layer layer) {
		if (layer == null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$
//		if(layers.contains(layer))
//			throw new IllegalArgumentException("Layer already added: "+CorpusUtils.getName(layer)); //$NON-NLS-1$

		layers.add(layer);
	}

	/**
	 * Adds the specified dependency to the internal set of dependencies, discarding potential
	 * duplicates.
	 * @param dependency
	 */
	public void addDependency(Dependency<LayerGroup> dependency) {
		if (dependency == null)
			throw new NullPointerException("Invalid dependency"); //$NON-NLS-1$
//		if(dependencies.contains(dependency))
//			throw new IllegalArgumentException("Dependency already added: "+dependency); //$NON-NLS-1$

		dependencies.add(dependency);
	}
}
