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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.ItemLayerManifest;
import de.ims.icarus.model.api.members.ContainerType;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContainerManifestImpl extends AbstractMemberManifest<ContainerManifest> implements ContainerManifest {

//	private ContainerManifest parentManifest;
	private final ItemLayerManifest layerManifest;

//	private ContainerManifest elementManifest;
	private ContainerType containerType;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public ContainerManifestImpl(ManifestLocation manifestLocation,
			CorpusRegistry registry, ItemLayerManifest layerManifest) {
		super(manifestLocation, registry);

		verifyEnvironment(manifestLocation, layerManifest, ItemLayerManifest.class);

		this.layerManifest = layerManifest;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#writeAttributes(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		// Write container type
		if(containerType!=null) {
			serializer.writeAttribute(ATTR_CONTAINER_TYPE, containerType.getXmlValue());
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		String containerType = ModelXmlUtils.normalize(attributes, ATTR_CONTAINER_TYPE);
		if(containerType!=null) {
			setContainerType(ContainerType.parseContainerType(containerType));
		}
	}

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_CONTAINER: {
			readAttributes(attributes);
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return this;
	}

	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (qName) {
		case TAG_CONTAINER: {
			return null;
		}

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_CONTAINER;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.CONTAINER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContainerManifest#getLayerManifest()
	 */
	@Override
	public ItemLayerManifest getLayerManifest() {
		return layerManifest;
	}
//
//	/**
//	 * @see de.ims.icarus.model.api.manifest.ContainerManifest#getParentManifest()
//	 */
//	@Override
//	public ContainerManifest getParentManifest() {
//		return parentManifest;
//	}
//
//	/**
//	 * @see de.ims.icarus.model.api.manifest.ContainerManifest#getElementManifest()
//	 */
//	@Override
//	public ContainerManifest getElementManifest() {
//		return elementManifest;
//	}
//
//	/**
//	 * @param elementManifest the elementManifest to set
//	 */
//	public void setElementManifest(ContainerManifest elementManifest) {
//		if (elementManifest == null)
//			throw new NullPointerException("Invalid elementManifest"); //$NON-NLS-1$
//
//		this.elementManifest = elementManifest;
//	}
//
//	/**
//	 * @param parentManifest the parentManifest to set
//	 */
//	public void setParentManifest(ContainerManifest parentManifest) {
//		if (parentManifest == null)
//			throw new NullPointerException("Invalid parentManifest"); //$NON-NLS-1$
//
//		this.parentManifest = parentManifest;
//	}

//	/**
//	 * @param layerManifest the layerManifest to set
//	 */
//	public void setLayerManifest(ItemLayerManifest layerManifest) {
//		if (layerManifest == null)
//			throw new NullPointerException("Invalid layerManifest"); //$NON-NLS-1$
//
//		this.layerManifest = layerManifest;
//	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContainerManifest#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		ContainerType result = containerType;
		if(result==null && hasTemplate()) {
			result = getTemplate().getContainerType();
		}

		if(result==null) {
			result = ContainerType.LIST;
		}

		return result;
	}

	public void setContainerType(ContainerType containerType) {
		if (containerType == null)
			throw new NullPointerException("Invalid containerType"); //$NON-NLS-1$

		this.containerType = containerType;
	}

//	/**
//	 * @see de.ims.icarus.model.api.standard.manifest.AbstractMemberManifest#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if(!super.equals(obj)) {
//			return false;
//		}
//
//		if(obj instanceof ContainerManifest) {
//			ContainerManifest other = (ContainerManifest) obj;
//			return containerType==other.getContainerType()
//					&& (ClassUtils.equals(elementManifest, other.getElementManifest()));
//		}
//		return false;
//	}

}
