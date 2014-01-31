/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your key) any later version.
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

import de.ims.icarus.language.model.manifest.OptionsManifest;
import de.ims.icarus.language.model.meta.ValueType;
import de.ims.icarus.language.model.standard.manifest.AbstractManifest;
import de.ims.icarus.language.model.standard.manifest.OptionsManifestImpl;
import de.ims.icarus.language.model.standard.manifest.ValueRangeImpl;
import de.ims.icarus.language.model.xml.sax.ModelElementHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class OptionsElementHandler extends ModelElementHandler<OptionsManifestImpl> {

	private String key;
	private ValueType valueType;
	private ValueRangeImpl range;

	public OptionsElementHandler() {
		super("options"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#release()
	 */
	@Override
	protected void release() {
		super.release();

		key = null;
		valueType = null;
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelElementHandler<?> startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {

		switch (localName) {
		case "options": { //$NON-NLS-1$
			OptionsManifest template = defaultGetTemplate(attributes, OptionsManifest.class);
			element = template==null ? new OptionsManifestImpl() : new OptionsManifestImpl(template);

			String id = attributes.getValue("id"); //$NON-NLS-1$
			if(id!=null) {
				getElement().setId(id);
			}
		} break;

		case "option": { //$NON-NLS-1$
			key = attributes.getValue("id"); //$NON-NLS-1$
			valueType = typeValue(attributes);
			OptionsManifestImpl manifest = getElement();

			manifest.addOption(key);
			manifest.setName(key, attributes.getValue("name")); //$NON-NLS-1$
			manifest.setDescription(key, attributes.getValue("description")); //$NON-NLS-1$
			manifest.setPublished(key, boolValue(attributes, "published", true)); //$NON-NLS-1$

			manifest.setValueType(key, valueType);
		} break;

		case "default-value": //$NON-NLS-1$
			break;

		case "values": { //$NON-NLS-1$
			ValuesElementHandler handler = getPool().getHandler(ValuesElementHandler.class);
			handler.setKey(key);
			handler.setValueType(valueType);
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
		case "options": { //$NON-NLS-1$
			OptionsManifestImpl manifest = getElement();
			manifest.setTemplate(isTemplateMode());

			if(isTemplateMode()) {
				registerTemplate(manifest);
			} else {
				AbstractManifest<?> target = (AbstractManifest<?>) getParent().getElement();
				target.setOptionsManifest(getElement());
			}
			return null;
		}

		case "option": { //$NON-NLS-1$
			key = null;
		} break;

		case "default-value": { //$NON-NLS-1$
			getElement().setDefaultValue(key, value(getText(), valueType));
		} break;

		case "values": //$NON-NLS-1$
			break;

		case "min": { //$NON-NLS-1$)
			Object val = value(getText(), valueType);
			range.setLowerBound(val);
		} break;

		case "max": { //$NON-NLS-1$)
			Object val = value(getText(), valueType);
			range.setUpperBound(val);
		} break;

		case "range": { //$NON-NLS-1$)
			getElement().setRange(key, range);
		} break;

		default:
			return super.endElement(uri, localName, qName);
		}

		return this;
	}
}
