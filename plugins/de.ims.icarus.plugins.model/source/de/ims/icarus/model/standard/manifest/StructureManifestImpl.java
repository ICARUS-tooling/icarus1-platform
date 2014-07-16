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
import de.ims.icarus.model.api.manifest.ManifestSource;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.StructureLayerManifest;
import de.ims.icarus.model.api.manifest.StructureManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class StructureManifestImpl extends ContainerManifestImpl implements StructureManifest {

	private StructureType structureType;
	private Boolean multiRootAllowed;

	/**
	 * @param manifestSource
	 * @param registry
	 * @param layerManifest
	 */
	public StructureManifestImpl(ManifestSource manifestSource,
			CorpusRegistry registry, StructureLayerManifest layerManifest) {
		super(manifestSource, registry, layerManifest);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#writeAttributes(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		// Write structure type
		if(structureType!=null) {
			serializer.writeAttribute(ATTR_STRUCTURE_TYPE, structureType.getXmlValue());
		}

		// Write flags
		writeFlag(serializer, ATTR_MULTI_ROOT, multiRootAllowed, DEFAULT_MULTI_ROOT_VALUE);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.ContainerManifestImpl#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_STRUCTURE;
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
		StructureType result = structureType;
		if(result==null && hasTemplate()) {
			result = getTemplate().getStructureType();
		}

		if(result==null) {
			result = StructureType.SET;
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.standard.manifest.ContainerManifestImpl#getResolvedLayerManifest()
	 */
	@Override
	public StructureLayerManifest getLayerManifest() {
		return (StructureLayerManifest) super.getLayerManifest();
	}

//	@Override
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
		if(multiRootAllowed==null) {
			return hasTemplate() ? getTemplate().isMultiRootAllowed() : DEFAULT_MULTI_ROOT_VALUE;
		} else {
			return multiRootAllowed.booleanValue();
		}
	}

//	@Override
	public void setMultiRootAllowed(boolean value) {
		this.multiRootAllowed = value;
	}
}
