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
package de.ims.icarus.language.model.standard.manifest;

import de.ims.icarus.language.model.StructureType;
import de.ims.icarus.language.model.manifest.ContainerManifest;
import de.ims.icarus.language.model.manifest.ManifestType;
import de.ims.icarus.language.model.manifest.MarkableLayerManifest;
import de.ims.icarus.language.model.manifest.StructureLayerManifest;
import de.ims.icarus.language.model.manifest.StructureManifest;
import de.ims.icarus.language.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class StructureManifestImpl extends ContainerManifestImpl implements StructureManifest {

	private StructureType structureType;
//	private ContainerManifest boundaryContainerManifest;

	public StructureManifestImpl() {

	}

	public StructureManifestImpl(StructureManifest template) {
		super(template);

		structureType = template.getStructureType();
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.DerivedObject#getTemplate()
	 */
	@Override
	public synchronized StructureManifest getTemplate() {
		return (StructureManifest) super.getTemplate();
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.ContainerManifestImpl#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.STRUCTURE_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.StructureManifest#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return structureType;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.ContainerManifestImpl#getLayerManifest()
	 */
	@Override
	public StructureLayerManifest getLayerManifest() {
		return (StructureLayerManifest) super.getLayerManifest();
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.StructureManifest#setStructureType(de.ims.icarus.language.model.StructureType)
	 */
	@Override
	public void setStructureType(StructureType structureType) {
		if (structureType == null)
			throw new NullPointerException("Invalid structureType"); //$NON-NLS-1$

		this.structureType = structureType;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.StructureManifest#getBoundaryContainerManifest()
	 */
	@Override
	public ContainerManifest getBoundaryContainerManifest() {
//		return boundaryContainerManifest;
		MarkableLayerManifest boundaryLayerManifest = getLayerManifest().getBoundaryLayerManifest();
		return boundaryLayerManifest==null ? null : boundaryLayerManifest.getRootContainerManifest();
	}

//	/**
//	 * @param boundaryContainerManifest the boundaryContainerManifest to set
//	 */
//	public void setBoundaryContainerManifest(
//			ContainerManifest boundaryContainerManifest) {
//		if (boundaryContainerManifest == null)
//			throw new NullPointerException("Invalid boundaryContainerManifest"); //$NON-NLS-1$
//
//		this.boundaryContainerManifest = boundaryContainerManifest;
//	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.ContainerManifestImpl#writeTemplateXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlAttributes(serializer);

		writeXmlAttribute(serializer, "structure-type", structureType, getTemplate().getStructureType()); //$NON-NLS-1$

//		if(boundaryContainerManifest!=null) {
//			serializer.writeAttribute("boundary", boundaryContainerManifest.getId()); //$NON-NLS-1$
//		}
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.ContainerManifestImpl#writeFullXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlAttributes(serializer);

		serializer.writeAttribute("structure-type", structureType.getValue()); //$NON-NLS-1$

//		if(boundaryContainerManifest!=null) {
//			serializer.writeAttribute("boundary", boundaryContainerManifest.getId()); //$NON-NLS-1$
//		}
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.ContainerManifestImpl#getXmlTag()
	 */
	@Override
	protected String getXmlTag() {
		return "structure"; //$NON-NLS-1$
	}
}
