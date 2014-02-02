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
package de.ims.icarus.language.model.xml.sax.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.language.model.manifest.Prerequisite;
import de.ims.icarus.language.model.standard.manifest.AbstractLayerManifest;
import de.ims.icarus.language.model.standard.manifest.PrerequisiteImpl;
import de.ims.icarus.language.model.xml.sax.ModelElementHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@SuppressWarnings("rawtypes")
public class LayerElementHandler<L extends AbstractLayerManifest> extends ManifestElementHandler<L> {

	protected LayerElementHandler(String tag) {
		super(tag);
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.ManifestElementHandler#defaultReadAttributes(org.xml.sax.Attributes, de.ims.icarus.language.model.standard.manifest.AbstractManifest)
	 */
	@Override
	public void defaultReadAttributes(Attributes attributes, L manifest) {
		super.defaultReadAttributes(attributes, manifest);

		String indexable = attributes.getValue("index"); //$NON-NLS-1$
		if(indexable!=null) {
			manifest.setIndexable(booleanValue(indexable));
		}

		String searchable = attributes.getValue("search"); //$NON-NLS-1$
		if(searchable!=null) {
			manifest.setSearchable(booleanValue(searchable));
		}
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.ManifestElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelElementHandler<?> startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {
		switch (localName) {
		case "prerequisites": //$NON-NLS-1$
			break;

		case "prerequisite": { //$NON-NLS-1$
			String layerId = attributes.getValue("layer-id"); //$NON-NLS-1$
			String contextId = attributes.getValue("context-id"); //$NON-NLS-1$
			String typeId = attributes.getValue("type-id"); //$NON-NLS-1$
			String alias = attributes.getValue("alias"); //$NON-NLS-1$

			Prerequisite prerequisite = null;

			if(alias==null) {
				// report error
			} else if(layerId!=null) {
				prerequisite = new PrerequisiteImpl(layerId, contextId, alias);
			} else if(typeId!=null) {
				prerequisite = new PrerequisiteImpl(typeId, alias);
			} else {
				// report error
			}

			if(prerequisite!=null) {
				getElement().addPrerequisite(prerequisite);
			}
		} break;

		default:
			return super.startElement(uri, localName, qName, attributes);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.ManifestElementHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ModelElementHandler<?> endElement(String uri, String localName,
			String qName) throws SAXException {
		switch (localName) {
		case "prerequisites": //$NON-NLS-1$
			break;

		case "prerequisite": //$NON-NLS-1$
			break;

		default:
			return super.endElement(uri, localName, qName);
		}

		return this;
	}

}
