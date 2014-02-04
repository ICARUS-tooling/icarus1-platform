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

import de.ims.icarus.language.model.manifest.ContainerManifest;
import de.ims.icarus.language.model.manifest.ManifestType;
import de.ims.icarus.language.model.manifest.MarkableLayerManifest;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.language.model.xml.XmlWriter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MarkableLayerManifestImpl extends AbstractLayerManifest<MarkableLayerManifest> implements MarkableLayerManifest {

	private List<ContainerManifest> containerManifests;

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractLayerManifest#readTemplate(de.ims.icarus.language.model.manifest.LayerManifest)
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
	 * @see de.ims.icarus.language.model.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.MARKABLE_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.MarkableLayerManifest#getContainerDepth()
	 */
	@Override
	public int getContainerDepth() {
//		if(containerManifests==null)
//			throw new IllegalStateException("Missing root container manifest"); //$NON-NLS-1$

		return containerManifests==null ? 0 : containerManifests.size();
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.MarkableLayerManifest#getRootContainerManifest()
	 */
	@Override
	public ContainerManifest getRootContainerManifest() {
		return getContainerManifest(0);
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.MarkableLayerManifest#getContainerManifest(int)
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
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractLayerManifest#writeTemplateXmlElements(de.ims.icarus.language.model.xml.XmlSerializer)
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
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractLayerManifest#writeFullXmlElements(de.ims.icarus.language.model.xml.XmlSerializer)
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
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractDerivable#getXmlTag()
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
