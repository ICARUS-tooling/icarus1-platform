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
import de.ims.icarus.language.model.manifest.StructureLayerManifest;
import de.ims.icarus.language.model.standard.manifest.ContextManifestImpl;
import de.ims.icarus.language.model.standard.manifest.StructureLayerManifestImpl;
import de.ims.icarus.language.model.xml.sax.ModelElementHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class StructureLayerElementHandler extends LayerElementHandler<StructureLayerManifestImpl> {

	public StructureLayerElementHandler() {
		super("structure-layer"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.LayerElementHandler#defaultReadAttributes(org.xml.sax.Attributes, de.ims.icarus.language.model.standard.manifest.AbstractLayerManifest)
	 */
	@Override
	public void defaultReadAttributes(Attributes attributes,
			StructureLayerManifestImpl manifest) {
		super.defaultReadAttributes(attributes, manifest);

		String boundary = attributes.getValue("boundary"); //$NON-NLS-1$
		if(boundary!=null) {
			if(isTemplateMode()) {
				getReport().error("Boundary declarations not supported in template section"); //$NON-NLS-1$
				return;
			}
//			MarkableLayerManifest boundaryManifest = lookup(boundary, MarkableLayerManifest.class);
//			if(boundaryManifest!=null) {
//				manifest.setBoundaryLayerManifest(boundaryManifest);
//			} else {
//				getReport().warning("Missing boundary layer manifest: "+boundary); //$NON-NLS-1$
//			}
			manifest.setBoundaryId(boundary);
		}
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.ManifestElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelElementHandler<?> startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {

		switch (localName) {
		case "structure-layer": { //$NON-NLS-1$
			ContextManifest contextManifest = (ContextManifest) (isTemplateMode() ? null : getParent().getElement());
			StructureLayerManifest template = defaultGetTemplate(attributes, StructureLayerManifest.class);
			element = template==null ? new StructureLayerManifestImpl(contextManifest)
					: new StructureLayerManifestImpl(contextManifest, template);

			defaultReadAttributes(attributes, element);
		} break;

		case "structure": { //$NON-NLS-1$
			return getPool().getHandler(StructureElementHandler.class);
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
		case "structure-layer": { //$NON-NLS-1$
			StructureLayerManifestImpl manifest = getElement();
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

		case "structure": //$NON-NLS-1$
			break;

		default:
			return super.endElement(uri, localName, qName);
		}

		return this;
	}

}
