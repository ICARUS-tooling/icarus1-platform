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

import de.ims.icarus.language.model.manifest.ContextManifest;
import de.ims.icarus.language.model.manifest.MarkableLayerManifest;
import de.ims.icarus.language.model.standard.manifest.ContextManifestImpl;
import de.ims.icarus.language.model.standard.manifest.MarkableLayerManifestImpl;
import de.ims.icarus.language.model.xml.sax.ModelElementHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MarkableLayerElementHandler extends LayerElementHandler<MarkableLayerManifestImpl> {

	public MarkableLayerElementHandler() {
		super("markable-layer"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.ManifestElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelElementHandler<?> startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {

		switch (localName) {
		case "markable-layer": { //$NON-NLS-1$
			ContextManifest contextManifest = (ContextManifest) (isTemplateMode() ? null : getParent().getElement());
			MarkableLayerManifest template = defaultGetTemplate(attributes, MarkableLayerManifest.class);
			element = template==null ? new MarkableLayerManifestImpl(contextManifest)
					: new MarkableLayerManifestImpl(contextManifest, template);

			defaultReadAttributes(attributes, element);
		} break;

		case "container": { //$NON-NLS-1$
			return getPool().getHandler(ContainerElementHandler.class);
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
		case "markable-layer": { //$NON-NLS-1$
			MarkableLayerManifestImpl manifest = getElement();
			manifest.setTemplate(isTemplateMode());

			if(isTemplateMode()) {
				registerTemplate(manifest);
			} else {
				register(manifest.getId(), manifest);

				ContextManifestImpl contextManifest = (ContextManifestImpl) getParent().getElement();
				contextManifest.addLayerManifest(manifest);
			}
			return null;
		}

		case "container": //$NON-NLS-1$
			break;

		default:
			return super.endElement(uri, localName, qName);
		}

		return this;
	}

}
