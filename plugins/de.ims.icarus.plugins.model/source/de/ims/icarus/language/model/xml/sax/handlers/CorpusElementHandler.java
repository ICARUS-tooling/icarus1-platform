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

import de.ims.icarus.language.model.manifest.CorpusManifest;
import de.ims.icarus.language.model.standard.manifest.CorpusManifestImpl;
import de.ims.icarus.language.model.xml.sax.ModelElementHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusElementHandler extends ManifestElementHandler<CorpusManifestImpl> {

	public CorpusElementHandler() {
		super("corpus"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.ManifestElementHandler#defaultReadAttributes(org.xml.sax.Attributes, de.ims.icarus.language.model.standard.manifest.AbstractManifest)
	 */
	@Override
	public void defaultReadAttributes(Attributes attributes,
			CorpusManifestImpl manifest) {
		super.defaultReadAttributes(attributes, manifest);

		String editable = attributes.getValue("editable"); //$NON-NLS-1$
		if(editable!=null) {
			manifest.setEditable(booleanValue(editable));
		}
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.ManifestElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelElementHandler<?> startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {

		switch (localName) {
		case "corpus": { //$NON-NLS-1$
			CorpusManifest template = defaultGetTemplate(attributes, CorpusManifest.class);
			element = template==null ? new CorpusManifestImpl() : new CorpusManifestImpl(template);

			defaultReadAttributes(attributes, element);
		} break;

		case "context": { //$NON-NLS-1$
			return getPool().getHandler(ContextElementHandler.class);
		}

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
		case "corpus": { //$NON-NLS-1$
			CorpusManifestImpl manifest = getElement();
			manifest.setTemplate(isTemplateMode());

			if(isTemplateMode()) {
				registerTemplate(manifest);
			} else {
				register(manifest.getId(), manifest);
			}

			return null;
		}

		case "context": { //$NON-NLS-1$
			return getPool().getHandler(ContextElementHandler.class);
		}

		default:
			return super.endElement(uri, localName, qName);
		}

//		return this;
	}

}
