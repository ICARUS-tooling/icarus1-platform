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

import de.ims.icarus.language.model.manifest.Implementation;
import de.ims.icarus.language.model.manifest.OptionsManifest;
import de.ims.icarus.language.model.meta.ValueType;
import de.ims.icarus.language.model.standard.manifest.AbstractManifest;
import de.ims.icarus.language.model.xml.sax.IconWrapper;
import de.ims.icarus.language.model.xml.sax.ModelElementHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@SuppressWarnings("rawtypes")
public abstract class ManifestElementHandler<M extends AbstractManifest> extends ModelElementHandler<M> {

	private String name;
	private ValueType valueType;

	protected ManifestElementHandler(String tag) {
		super(tag);
	}

	public void defaultReadAttributes(Attributes attributes, M manifest) {
		manifest.setId(attributes.getValue("id")); //$NON-NLS-1$
		manifest.setName(attributes.getValue("name")); //$NON-NLS-1$
		manifest.setDescription(attributes.getValue("description")); //$NON-NLS-1$

		String iconId = attributes.getValue("icon"); //$NON-NLS-1$
		if(iconId!=null) {
			manifest.setIcon(new IconWrapper(iconId));
		}
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#release()
	 */
	@Override
	protected void release() {
		super.release();

		name = null;
		valueType = null;
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelElementHandler<?> startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {

		switch (localName) {
		case "property": { //$NON-NLS-1$
			name = stringValue(attributes, "name"); //$NON-NLS-1$
			valueType = typeVal(attributes);

			if(valueType==null) {
				OptionsManifest optionsManifest = getElement().getOptionsManifest();
				if(optionsManifest!=null) {
					valueType = optionsManifest.getValueType(name);
				} else {
					getReport().error("Unable to resolve type of property '"+name+"': No options manifest defined"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			String valueString = attributes.getValue("value"); //$NON-NLS-1$
			if(valueString!=null) {
				Object value = value(valueString, valueType);

				getElement().setProperty(name, value);

				name = null;
				valueType = null;
			}
		} break;

		case "options": { //$NON-NLS-1$
			return getPool().getHandler(OptionsElementHandler.class);
		}

		case "implementation": { //$NON-NLS-1$
			Implementation implementation = createImplementation(attributes);
			if(implementation==null) {
				getReport().warning("EMpty implementation declaration in manifest: "+getElement().getId()); //$NON-NLS-1$
			} else {
				getElement().setImplementation(implementation);
			}
		} break;

		default:
			return super.startElement(uri, localName, qName, attributes);
		}

		return this;
	}

	private static ValueType typeVal(Attributes attr) {
		String s = attr.getValue("type"); //$NON-NLS-1$
		return s==null ? null : ValueType.parseValueType(s);
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ModelElementHandler<?> endElement(String uri, String localName,
			String qName) throws SAXException {

		switch (localName) {
		case "property": { //$NON-NLS-1$
			if(name!=null) {
				Object value = value(getText(), valueType);
				getElement().setProperty(name, value);
			}
		} break;

		case "options": //$NON-NLS-1$
			break;

		case "implementation": //$NON-NLS-1$
			break;

		default:
			return super.endElement(uri, localName, qName);
		}

		return this;
	}
}
