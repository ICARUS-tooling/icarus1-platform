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

import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.ManifestSource;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.StructureLayerManifest;
import de.ims.icarus.model.api.manifest.StructureManifest;
import de.ims.icarus.model.registry.CorpusRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class StructureLayerManifestImpl extends MarkableLayerManifestImpl implements StructureLayerManifest {

	/**
	 * @param manifestSource
	 * @param registry
	 * @param layerGroupManifest
	 */
	protected StructureLayerManifestImpl(ManifestSource manifestSource,
			CorpusRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestSource, registry, layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractDerivable#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_STRUCTURE_LAYER;
	}

	/**
	 * @see de.ims.icarus.model.api.standard.manifest.AbstractDerivable#getTemplate()
	 */
	@Override
	public synchronized StructureLayerManifest getTemplate() {
		return (StructureLayerManifest) super.getTemplate();
	}

	/**
	 * @see de.ims.icarus.model.api.standard.manifest.MarkableLayerManifestImpl#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.STRUCTURE_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.StructureLayerManifest#getStructureManifest()
	 */
	@Override
	public StructureManifest getStructureManifest() {
		return (StructureManifest) getContainerManifest(1);
	}

	public void addStructureManifest(StructureManifest manifest) {
		addContainerManifest(manifest);
	}
}
