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
package de.ims.icarus.model.standard.manifest;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LayerGroupManifestImpl implements LayerGroupManifest {

	private final ContextManifest contextManifest;

	private final List<LayerManifest> layerManifests = new ArrayList<>();
	private MarkableLayerManifest primaryLayerManifest;
	private boolean independent = false;
	private final String name;

	public LayerGroupManifestImpl(ContextManifest contextManifest, String name) {
		if (contextManifest == null)
			throw new NullPointerException("Invalid contextManifest");  //$NON-NLS-1$
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		this.contextManifest = contextManifest;
		this.name = name;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerGroupManifest#getContextManifest()
	 */
	@Override
	public ContextManifest getContextManifest() {
		return contextManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerGroupManifest#getLayerManifests()
	 */
	@Override
	public List<LayerManifest> getLayerManifests() {
		return CollectionUtils.getListProxy(layerManifests);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerGroupManifest#getPrimaryLayerManifest()
	 */
	@Override
	public MarkableLayerManifest getPrimaryLayerManifest() {
		return primaryLayerManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerGroupManifest#isIndependent()
	 */
	@Override
	public boolean isIndependent() {
		return independent;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerGroupManifest#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param independent the independent to set
	 */
	public void setIndependent(boolean independent) {
		this.independent = independent;
	}

	public void addLayerManifest(LayerManifest layerManifest) {
		if (layerManifest == null)
			throw new NullPointerException("Invalid layerManifest"); //$NON-NLS-1$

		if(!layerManifests.add(layerManifest))
			throw new IllegalArgumentException("Layer manifest already present in group: "+layerManifest.getId()); //$NON-NLS-1$
	}

	/**
	 * @param primaryLayerManifest the primaryLayerManifest to set
	 */
	public void setPrimaryLayerManifest(MarkableLayerManifest primaryLayerManifest) {
		if (primaryLayerManifest == null)
			throw new NullPointerException("Invalid primaryLayerManifest"); //$NON-NLS-1$
		if(!layerManifests.contains(primaryLayerManifest))
			throw new IllegalArgumentException("Primary layer manifest not added as contained layer: "+primaryLayerManifest.getId()); //$NON-NLS-1$

		this.primaryLayerManifest = primaryLayerManifest;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof LayerGroupManifest) {
			return name.equals(((LayerGroupManifest)obj).getName());
		}

		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LayerGroup:"+name; //$NON-NLS-1$
	}
}
