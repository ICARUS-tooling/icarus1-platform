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

import de.ims.icarus.language.model.ContainerType;
import de.ims.icarus.language.model.manifest.ContainerManifest;
import de.ims.icarus.language.model.manifest.MarkableLayerManifest;
import de.ims.icarus.language.model.standard.manifest.ContainerManifestImpl;
import de.ims.icarus.language.model.standard.manifest.MarkableLayerManifestImpl;
import de.ims.icarus.language.model.xml.sax.ModelElementHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContainerElementHandler extends ManifestElementHandler<ContainerManifestImpl> {

	public ContainerElementHandler() {
		super("container"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.ManifestElementHandler#defaultReadAttributes(org.xml.sax.Attributes, de.ims.icarus.language.model.standard.manifest.AbstractManifest)
	 */
	@Override
	public void defaultReadAttributes(Attributes attributes,
			ContainerManifestImpl manifest) {
		super.defaultReadAttributes(attributes, manifest);

		String containerType = attributes.getValue("container-type"); //$NON-NLS-1$
		if(containerType!=null) {
			getElement().setContainerType(ContainerType.parseContainerType(containerType));
		}
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.handlers.ManifestElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelElementHandler<?> startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {
		switch (localName) {
		case "container": { //$NON-NLS-1$
			ContainerManifest template = defaultGetTemplate(attributes, ContainerManifest.class);
			element = template==null ? new ContainerManifestImpl() : new ContainerManifestImpl(template);

			defaultReadAttributes(attributes, element);

			if(!isTemplateMode()) {
				element.setLayerManifest((MarkableLayerManifest) getParent().getElement());
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
		case "container": { //$NON-NLS-1$

			ContainerManifestImpl manifest = getElement();
			manifest.setTemplate(isTemplateMode());

			if(isTemplateMode()) {
				registerTemplate(manifest);
			} else {
				register(manifest.getId(), manifest);

				MarkableLayerManifestImpl layerManifest = (MarkableLayerManifestImpl) getParent().getElement();
				int depth = layerManifest.getContainerDepth();
				if(depth>0) {
					ContainerManifest parentManifest = layerManifest.getContainerManifest(depth-1);
					if(parentManifest instanceof ContainerManifestImpl) {
						((ContainerManifestImpl)parentManifest).setElementManifest(manifest);
					}
					manifest.setParentManifest(parentManifest);
				}
				layerManifest.addContainerManifest(manifest);
			}
			return null;
		}

		default:
			return super.endElement(uri, localName, qName);
		}

//		return this;
	}

}
