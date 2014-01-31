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

import de.ims.icarus.language.model.manifest.ValueSet;
import de.ims.icarus.language.model.meta.ValueType;
import de.ims.icarus.language.model.standard.manifest.AnnotationManifestImpl;
import de.ims.icarus.language.model.standard.manifest.OptionsManifestImpl;
import de.ims.icarus.language.model.standard.manifest.ValueManifestImpl;
import de.ims.icarus.language.model.standard.manifest.ValueSetImpl;
import de.ims.icarus.language.model.xml.sax.ModelElementHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ValuesElementHandler extends ModelElementHandler<ValueSetImpl> {

	private ValueType valueType;
	private ValueManifestImpl value;
	private String key;

	protected ValuesElementHandler() {
		super("values"); //$NON-NLS-1$
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#release()
	 */
	@Override
	protected void release() {
		super.release();

		valueType = null;
		value = null;
		key = null;
	}


	/**
	 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelElementHandler<?> startElement(String uri, String localName,
			String qName, Attributes attributes) throws SAXException {

		switch (localName) {
		case "values": { //$NON-NLS-1$
			ValueSet template = defaultGetTemplate(attributes, ValueSet.class);

			element = template==null ? new ValueSetImpl() : new ValueSetImpl(template);

			String valueType = attributes.getValue("type"); //$NON-NLS-1$
			if(valueType!=null); {
				this.valueType = typeValue(valueType);
			}
		} break;

		case "value": { //$NON-NLS-1$
			String name = attributes.getValue("name"); //$NON-NLS-1$
			String description = attributes.getValue("description"); //$NON-NLS-1$

			if(name!=null || description!=null) {
				value = new ValueManifestImpl();

				if(name!=null) {
					value.setName(name);
				}
				if(description!=null) {
					value.setDescription(description);
				}
			}
		} break;

		default:
			return super.startElement(uri, localName, qName, attributes);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus.language.model.xml.sax.ModelElementHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ModelElementHandler<?> endElement(String uri, String localName,
			String qName) throws SAXException {

		switch (localName) {
		case "values": { //$NON-NLS-1$
			ValueSetImpl manifest = getElement();

			if(isTemplateMode()) {
				registerTemplate(manifest);
			} else {
				Object parent = getParent().getElement();
				if(parent instanceof OptionsManifestImpl) {
					((OptionsManifestImpl)parent).setValues(key, manifest);
				} else if(parent instanceof AnnotationManifestImpl) {
					((AnnotationManifestImpl)parent).setValues(manifest);
				}
				//TODO throw exception when parent is illegal?
			}

			return null;
		}

		case "value": { //$NON-NLS-1$

			Object val = value(getText(), valueType);

			if(value!=null) {
				value.setValue(val);
				getElement().addValue(value);
			} else {
				getElement().addValue(val);
			}
			value = null;
		} break;

		default:
			return super.endElement(uri, localName, qName);
		}

		return this;
	}
}
