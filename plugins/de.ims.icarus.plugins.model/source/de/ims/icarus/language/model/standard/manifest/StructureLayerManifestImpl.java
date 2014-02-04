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

import de.ims.icarus.language.model.manifest.ContextManifest;
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
public class StructureLayerManifestImpl extends MarkableLayerManifestImpl implements StructureLayerManifest {

	private MarkableLayerManifest boundaryLayerManifest;

	private String boundaryLayer;
	private String boundaryContext;

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractDerivable#getTemplate()
	 */
	@Override
	public synchronized StructureLayerManifest getTemplate() {
		return (StructureLayerManifest) super.getTemplate();
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.MarkableLayerManifestImpl#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.STRUCTURE_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.StructureLayerManifest#getStructureManifest()
	 */
	@Override
	public StructureManifest getStructureManifest() {
		return (StructureManifest) getContainerManifest(2);
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.StructureLayerManifest#getBoundaryLayerManifest()
	 */
	@Override
	public MarkableLayerManifest getBoundaryLayerManifest() {
		return boundaryLayerManifest;
	}

	/**
	 * @param boundaryLayerManifest the boundaryLayerManifest to set
	 */
	public void setBoundaryLayerManifest(MarkableLayerManifest boundaryLayerManifest) {
		if (boundaryLayerManifest == null)
			throw new NullPointerException("Invalid boundaryLayerManifest"); //$NON-NLS-1$

		this.boundaryLayerManifest = boundaryLayerManifest;
	}

	/**
	 * @return the boundaryLayer
	 */
	public String getBoundaryLayer() {
		return boundaryLayer;
	}

	/**
	 * @return the boundaryContext
	 */
	public String getBoundaryContext() {
		return boundaryContext;
	}

	/**
	 * @param boundaryLayer the boundaryLayer to set
	 */
	public void setBoundaryLayer(String boundaryLayer) {
		if (boundaryLayer == null)
			throw new NullPointerException("Invalid boundaryLayer"); //$NON-NLS-1$
		if(!isTemplate())
			throw new UnsupportedOperationException("Cannot define lazy boundary layer link"); //$NON-NLS-1$
		this.boundaryLayer = boundaryLayer;
	}

	public void addStructureManifest(StructureManifest manifest) {
		addContainerManifest(manifest);
	}

	/**
	 * @param boundaryContext the boundaryContext to set
	 */
	public void setBoundaryContext(String boundaryContext) {
		if (boundaryContext == null)
			throw new NullPointerException("Invalid boundaryContext"); //$NON-NLS-1$
		if(!isTemplate())
			throw new UnsupportedOperationException("Cannot define lazy boundary context link"); //$NON-NLS-1$
		this.boundaryContext = boundaryContext;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.MarkableLayerManifestImpl#getXmlTag()
	 */
	@Override
	protected String getXmlTag() {
		return "structure-layer"; //$NON-NLS-1$
	}

	private void writeBoundaryXmlAttributes(XmlSerializer serializer) throws Exception {
		if(isTemplate()) {
			serializer.writeAttribute("boundary-layer", boundaryLayer); //$NON-NLS-1$
			serializer.writeAttribute("boundary-context", boundaryContext); //$NON-NLS-1$
		} else if(boundaryLayerManifest!=null) {
			ContextManifest boundaryContext = boundaryLayerManifest.getContextManifest();

			serializer.writeAttribute("boundary-layer", boundaryLayerManifest.getId()); //$NON-NLS-1$

			if(boundaryContext!=getContextManifest()) {
				serializer.writeAttribute("boundary-context", boundaryContext.getId()); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractLayerManifest#writeTemplateXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlAttributes(serializer);

		writeBoundaryXmlAttributes(serializer);
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractLayerManifest#writeFullXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlAttributes(serializer);

		writeBoundaryXmlAttributes(serializer);
	}

}
