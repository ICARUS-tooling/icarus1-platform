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

import de.ims.icarus.model.api.StructureType;
import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.StructureLayerManifest;
import de.ims.icarus.model.api.manifest.StructureManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class StructureManifestImpl extends ContainerManifestImpl implements StructureManifest {

	private StructureType structureType = StructureType.SET;
	private boolean multiRootAllowed = false;

	/**
	 * @see de.ims.icarus.model.api.standard.manifest.ContainerManifestImpl#readTemplate(de.ims.icarus.model.api.manifest.ContainerManifest)
	 */
	@Override
	protected void readTemplate(ContainerManifest template) {
		super.readTemplate(template);

		if(structureType==null) {
			structureType = ((StructureManifest)template).getStructureType();
		}
	}

	/**
	 * @see de.ims.icarus.model.api.standard.manifest.AbstractDerivable#getTemplate()
	 */
	@Override
	public synchronized StructureManifest getTemplate() {
		return (StructureManifest) super.getTemplate();
	}

	/**
	 * @see de.ims.icarus.model.api.standard.manifest.ContainerManifestImpl#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.STRUCTURE_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.StructureManifest#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return structureType;
	}

	/**
	 * @see de.ims.icarus.model.api.standard.manifest.ContainerManifestImpl#getResolvedLayerManifest()
	 */
	@Override
	public StructureLayerManifest getLayerManifest() {
		return (StructureLayerManifest) super.getLayerManifest();
	}

	public void setStructureType(StructureType structureType) {
		if (structureType == null)
			throw new NullPointerException("Invalid structureType"); //$NON-NLS-1$

		this.structureType = structureType;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.StructureManifest#isMultiRootAllowed()
	 */
	@Override
	public boolean isMultiRootAllowed() {
		return multiRootAllowed;
	}

	public void setMultiRootAllowed(boolean value) {
		this.multiRootAllowed = value;
	}
}
