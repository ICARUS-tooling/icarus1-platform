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

import de.ims.icarus.language.model.manifest.ContextReaderManifest;
import de.ims.icarus.language.model.standard.manifest.ContextManifestImpl;
import de.ims.icarus.language.model.standard.manifest.ContextReaderManifestImpl;
import de.ims.icarus.language.model.xml.sax.ModelElementHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContextReaderElementHandler extends ManifestElementHandler<ContextReaderManifestImpl> {

	public ContextReaderElementHandler() {
		super("context-reader"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.ManifestElementHandler#defaultReadAttributes(org.xml.sax.Attributes, de.ims.icarus.language.model.standard.manifest.AbstractManifest)
	 */
	@Override
	public void defaultReadAttributes(Attributes attributes,
			ContextReaderManifestImpl manifest) {
		super.defaultReadAttributes(attributes, manifest);

		manifest.setFormatId(attributes.getValue("format")); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.ManifestElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelElementHandler<?> startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {

		switch (localName) {
		case "context-reader": { //$NON-NLS-1$
			ContextReaderManifest template = defaultGetTemplate(attributes, ContextReaderManifest.class);
			element = template==null ? new ContextReaderManifestImpl() : new ContextReaderManifestImpl(template);

			defaultReadAttributes(attributes, element);
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
		case "context-reader": { //$NON-NLS-1$
			ContextReaderManifestImpl manifest = getElement();
			manifest.setTemplate(isTemplateMode());

			if(isTemplateMode()) {
				registerTemplate(manifest);
			} else {
				register(manifest.getId(), manifest);

				ContextManifestImpl contextManifest = (ContextManifestImpl) getParent().getElement();

				contextManifest.setReaderManifest(manifest);
			}

			return null;
		}

		default:
			return super.endElement(uri, localName, qName);
		}

//		return this;
	}

}
