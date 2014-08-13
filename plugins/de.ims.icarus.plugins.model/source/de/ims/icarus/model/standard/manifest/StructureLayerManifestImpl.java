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

import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.StructureLayerManifest;
import de.ims.icarus.model.api.manifest.StructureManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.xml.ModelXmlHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class StructureLayerManifestImpl extends MarkableLayerManifestImpl implements StructureLayerManifest {

	/**
	 * @param manifestLocation
	 * @param registry
	 * @param layerGroupManifest
	 */
	public StructureLayerManifestImpl(ManifestLocation manifestLocation,
			CorpusRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry, layerGroupManifest);
	}

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_STRUCTURE_LAYER: {
			readAttributes(attributes);
		} break;

		case TAG_STRUCTURE: {
			return new StructureManifestImpl(manifestLocation, getRegistry(), this);
		}

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
		case TAG_STRUCTURE_LAYER: {
			return null;
		}

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ModelXmlHandler handler)
			throws SAXException {
		switch (qName) {

		case TAG_STRUCTURE: {
			addStructureManifest((StructureManifest) handler);
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_STRUCTURE_LAYER;
	}

	/**
	 * @see de.ims.icarus.model.api.standard.manifest.AbstractManifest#getTemplate()
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
	 * @see de.ims.icarus.model.api.manifest.StructureLayerManifest#getRootStructureManifest()
	 */
	@Override
	public StructureManifest getRootStructureManifest() {
		return (StructureManifest) getContainerManifest(1);
	}

	public void addStructureManifest(StructureManifest manifest) {
		if(getContainerDepth()==0)
			throw new IllegalStateException("Cannot add structure manifest as root container manifest"); //$NON-NLS-1$
		addContainerManifest(manifest);
	}
}
