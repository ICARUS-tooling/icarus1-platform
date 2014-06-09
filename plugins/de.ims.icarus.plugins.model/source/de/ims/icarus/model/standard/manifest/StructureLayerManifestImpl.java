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

import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.StructureLayerManifest;
import de.ims.icarus.model.api.manifest.StructureManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class StructureLayerManifestImpl extends MarkableLayerManifestImpl implements StructureLayerManifest {

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
		return (StructureManifest) getContainerManifest(2);
	}

	public void addStructureManifest(StructureManifest manifest) {
		addContainerManifest(manifest);
	}

	/**
	 * @see de.ims.icarus.model.api.standard.manifest.MarkableLayerManifestImpl#getXmlTag()
	 */
	@Override
	protected String getXmlTag() {
		return "structure-layer"; //$NON-NLS-1$
	}

}
