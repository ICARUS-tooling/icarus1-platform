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

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.language.model.api.manifest.ContainerManifest;
import de.ims.icarus.language.model.api.manifest.ContextManifest;
import de.ims.icarus.language.model.api.manifest.ManifestType;
import de.ims.icarus.language.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.language.model.xml.XmlWriter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MarkableLayerManifestImpl extends AbstractLayerManifest<MarkableLayerManifest> implements MarkableLayerManifest {

	private List<ContainerManifest> containerManifests;

	private MarkableLayerManifest boundaryLayerManifest;

	private String boundaryLayer;
	private String boundaryContext;

	/**
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractLayerManifest#readTemplate(de.ims.icarus.language.model.api.manifest.LayerManifest)
	 */
	@Override
	protected void readTemplate(MarkableLayerManifest template) {
		super.readTemplate(template);

		ContainerManifestImpl lastAdded = null;

		for(int i=0; i<template.getContainerDepth(); i++) {
			ContainerManifest source = template.getContainerManifest(i);
			ContainerManifestImpl containerManifest =
					(ContainerManifestImpl) clone(source);

			containerManifest.setTemplate(source);

			if(lastAdded!=null) {
				containerManifest.setParentManifest(lastAdded);
				lastAdded.setElementManifest(containerManifest);
			}
			containerManifest.setLayerManifest(this);

			addContainerManifest(containerManifest);

			lastAdded = containerManifest;
		}
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.MARKABLE_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.MarkableLayerManifest#getContainerDepth()
	 */
	@Override
	public int getContainerDepth() {
//		if(containerManifests==null)
//			throw new IllegalStateException("Missing root container manifest"); //$NON-NLS-1$

		return containerManifests==null ? 0 : containerManifests.size();
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.MarkableLayerManifest#getRootContainerManifest()
	 */
	@Override
	public ContainerManifest getRootContainerManifest() {
		return getContainerManifest(0);
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.MarkableLayerManifest#getContainerManifest(int)
	 */
	@Override
	public ContainerManifest getContainerManifest(int level) {
		if(containerManifests==null)
			throw new IllegalStateException("Missing root container manifest"); //$NON-NLS-1$

		return containerManifests.get(level);
	}

	public void addContainerManifest(ContainerManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		if(containerManifests==null) {
			containerManifests = new ArrayList<>(3);
		}

		containerManifests.add(manifest);
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.StructureLayerManifest#getBoundaryLayerManifest()
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
	public String getBoundaryLayerManifest() {
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
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractLayerManifest#writeTemplateXmlElements(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlElements(serializer);

		for(int i=getTemplate().getContainerDepth(); i<getContainerDepth(); i++) {
			XmlWriter.writeContainerManifestElement(serializer, getContainerManifest(i));
		}
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractLayerManifest#writeFullXmlElements(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlElements(serializer);

		for(int i=0; i<getContainerDepth(); i++) {
			XmlWriter.writeContainerManifestElement(serializer, getContainerManifest(i));
		}
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractLayerManifest#writeTemplateXmlAttributes(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlAttributes(serializer);

		writeBoundaryXmlAttributes(serializer);
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractLayerManifest#writeFullXmlAttributes(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlAttributes(serializer);

		writeBoundaryXmlAttributes(serializer);
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.manifest.AbstractDerivable#getXmlTag()
	 */
	@Override
	protected String getXmlTag() {
		return "markable-layer"; //$NON-NLS-1$
	}

	protected ContainerManifest clone(ContainerManifest source) {
		return source.getManifestType()==ManifestType.STRUCTURE_MANIFEST ?
				new StructureManifestImpl() : new ContainerManifestImpl();
	}
}
