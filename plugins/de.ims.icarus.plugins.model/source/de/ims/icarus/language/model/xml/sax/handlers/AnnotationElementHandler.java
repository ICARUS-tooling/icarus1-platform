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

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.language.model.manifest.AnnotationManifest;
import de.ims.icarus.language.model.standard.manifest.AnnotationLayerManifestImpl;
import de.ims.icarus.language.model.standard.manifest.AnnotationManifestImpl;
import de.ims.icarus.language.model.standard.manifest.ValueRangeImpl;
import de.ims.icarus.language.model.xml.sax.ModelElementHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotationElementHandler extends ManifestElementHandler<AnnotationManifestImpl> {

	private ValueRangeImpl range;

	private Set<String> aliases = new HashSet<>();

	public AnnotationElementHandler() {
		super("annotation"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#release()
	 */
	@Override
	protected void release() {
		super.release();

		range = null;
		aliases.clear();
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelElementHandler<?> startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {

		switch (localName) {
		case "annotation": { //$NON-NLS-1$
			AnnotationManifest template = defaultGetTemplate(attributes, AnnotationManifest.class);
			element = template==null ? new AnnotationManifestImpl() : new AnnotationManifestImpl(template);

			defaultReadAttributes(attributes, element);

			String key = attributes.getValue("key"); //$NON-NLS-1$
			if(key!=null) {
				element.setKey(key);
			}
		} break;

		case "alias": { //$NON-NLS-1$
			String alias = attributes.getValue("name"); //$NON-NLS-1$
			if(alias!=null) {
				if(alias!=null && !alias.isEmpty()) {
					getElement().addAlias(alias);
					aliases.add(alias);
				} else {
					getReport().warning("Duplicate alias '"+alias+"' for annotation "+getElement().getId()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		} break;

		case "values": { //$NON-NLS-1$
			ValuesElementHandler handler = getPool().getHandler(ValuesElementHandler.class);
			handler.setValueType(getElement().getValueType());
			return handler;
		}

		case "min": //$NON-NLS-1$
			break;

		case "max": //$NON-NLS-1$
			break;

		case "range": { //$NON-NLS-1$
			boolean includeMin = boolValue(attributes, "include-min", true); //$NON-NLS-1$
			boolean includeMax = boolValue(attributes, "include-max", true); //$NON-NLS-1$
			range = new ValueRangeImpl(includeMin, includeMax);
		} break;

		default:
			return super.startElement(uri, localName, qName, attributes);
		}

		return this;
	}

	private static boolean boolValue(Attributes attr, String key, boolean defaultValue) {
		String s = attr.getValue(key);
		return s==null ? defaultValue : booleanValue(s);
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ModelElementHandler<?> endElement(String uri, String localName,
			String qName) throws SAXException {

		switch (localName) {
		case "annotation": { //$NON-NLS-1$
			AnnotationManifestImpl manifest = getElement();
			manifest.setTemplate(isTemplateMode());

			if(isTemplateMode()) {
				registerTemplate(manifest);
			} else {
				String key = manifest.getKey();
				AnnotationLayerManifestImpl layerManifest = (AnnotationLayerManifestImpl) getParent().getElement();
				if(key!=null) {
					layerManifest.addAnnotationManifest(key, manifest);
				} else {
					layerManifest.setDefaultAnnotationManifest(manifest);
				}
			}

			return null;
		}

		case "alias": { //$NON-NLS-1$
			String alias = getText();
			if(alias!=null && !alias.isEmpty()) {
				if(!aliases.contains(alias)) {
					getElement().addAlias(alias);
					aliases.add(alias);
				} else {
					getReport().warning("Duplicate alias '"+alias+"' for annotation "+getElement().getId()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		} break;

		case "values":  //$NON-NLS-1$
			break;

		case "min": { //$NON-NLS-1$)
			Object val = value(getText(), getElement().getValueType());
			range.setLowerBound(val);
		} break;

		case "max": { //$NON-NLS-1$)
			Object val = value(getText(), getElement().getValueType());
			range.setUpperBound(val);
		} break;

		case "range": { //$NON-NLS-1$)
			getElement().setValueRange(range);
		} break;

		default:
			return super.endElement(uri, localName, qName);
		}

		return this;
	}

}
