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
import de.ims.icarus.language.model.manifest.CorpusManifest;
import de.ims.icarus.language.model.standard.manifest.ContextManifestImpl;
import de.ims.icarus.language.model.standard.manifest.CorpusManifestImpl;
import de.ims.icarus.language.model.xml.sax.ModelElementHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContextElementHandler extends ManifestElementHandler<ContextManifestImpl> {

	public ContextElementHandler() {
		super("context"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.ManifestElementHandler#defaultReadAttributes(org.xml.sax.Attributes, de.ims.icarus.language.model.standard.manifest.AbstractManifest)
	 */
	@Override
	public void defaultReadAttributes(Attributes attributes,
			ContextManifestImpl manifest) {
		super.defaultReadAttributes(attributes, manifest);

		String independent = attributes.getValue("independent"); //$NON-NLS-1$
		if(independent!=null) {
			manifest.setIndependent(booleanValue(independent));
		}
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.ManifestElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelElementHandler<?> startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {

		switch (localName) {

		case "documentation": //$NON-NLS-1$
			break;

		case "default-context": //$NON-NLS-1$
		case "context": { //$NON-NLS-1$
			ContextManifest template = defaultGetTemplate(attributes, ContextManifest.class);
			CorpusManifest corpusManifest = (CorpusManifest) (isTemplateMode() ? null : getParent().getElement());

			element = template==null ? new ContextManifestImpl(corpusManifest) :
					new ContextManifestImpl(corpusManifest, template);

			defaultReadAttributes(attributes, element);
		} break;

		case "location": { //$NON-NLS-1$
			return getPool().getHandler(LocationElementHandler.class);
		}

		case "context-reader": { //$NON-NLS-1$
			return getPool().getHandler(ContextReaderElementHandler.class);
		}

		case "context-writer": { //$NON-NLS-1$
			return getPool().getHandler(ContextWriterElementHandler.class);
		}

		case "markable-layer": { //$NON-NLS-1$
			return getPool().getHandler(MarkableLayerElementHandler.class);
		}

		case "structure-layer": { //$NON-NLS-1$
			return getPool().getHandler(StructureLayerElementHandler.class);
		}

		case "annotation-layer": { //$NON-NLS-1$
			return getPool().getHandler(AnnotationLayerElementHandler.class);
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

		boolean isDefault = false;

		switch (localName) {

		case "documentation": //$NON-NLS-1$
			break;

		case "default-context": //$NON-NLS-1$
			isDefault = true;
			//$FALL-THROUGH$
		case "context": { //$NON-NLS-1$
			ContextManifestImpl manifest = getElement();
			manifest.setTemplate(isTemplateMode());

			if(isTemplateMode()) {
				registerTemplate(manifest);
			} else {
				register(manifest.getId(), manifest);

				CorpusManifestImpl corpusManifest = (CorpusManifestImpl) getParent().getElement();

				if(isDefault) {
					corpusManifest.setDefaultContextManifest(manifest);
				} else {
					corpusManifest.addCustomContextManifest(manifest);
				}
			}
			return null;
		}

		case "location": //$NON-NLS-1$
			break;

		case "context-reader": //$NON-NLS-1$
			break;

		case "context-writer": //$NON-NLS-1$
			break;

		case "markable-layer": //$NON-NLS-1$
			break;

		case "structure-layer": //$NON-NLS-1$
			break;

		case "annotation-layer": //$NON-NLS-1$
			break;

		default:
			return super.endElement(uri, localName, qName);
		}

		return this;
	}

}
