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
package de.ims.icarus.model.standard.manifest;

import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ValueManifest;
import de.ims.icarus.model.types.UnsupportedValueTypeException;
import de.ims.icarus.model.types.ValueType;
import de.ims.icarus.model.xml.ModelXmlElement;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ValueManifestImpl implements ValueManifest, ModelXmlHandler, ModelXmlElement {

	private Object value;
	private String name;
	private String description;
	private final ValueType valueType;

	private static final Set<ValueType> supportedValueTypes = ValueType.filterWithout(
			ValueType.UNKNOWN,
			ValueType.CUSTOM,
			ValueType.IMAGE_RESOURCE,
			ValueType.URL_RESOURCE);

	public ValueManifestImpl(ValueType valueType) {
		if (valueType == null)
			throw new NullPointerException("Invalid valueType"); //$NON-NLS-1$

		if(!supportedValueTypes.contains(valueType))
			throw new UnsupportedValueTypeException(valueType);

		this.valueType = valueType;
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlElement#writeXml(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		ModelXmlUtils.writeValueManifestElement(serializer, this, valueType);
	}

	protected void readAttributes(Attributes attributes) {
		// Name must be specified
		setName(ModelXmlUtils.normalize(attributes, ATTR_NAME));

		// Description is optional
		description = ModelXmlUtils.normalize(attributes, ATTR_DESCRIPTION);
	}

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_VALUE: {
			readAttributes(attributes);
		} break;

		default:
			throw new SAXException("Unrecognized opening tag  '"+qName+"' in "+TAG_VALUE+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return this;
	}

	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (qName) {
		case TAG_VALUE: {
			setValue(valueType.parse(text, manifestLocation.getClassLoader()));

			return null;
		}

		default:
			throw new SAXException("Unrecognized end tag  '"+qName+"' in "+TAG_VALUE+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ModelXmlHandler handler)
			throws SAXException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ValueManifest#getValueType()
	 */
	@Override
	public ValueType getValueType() {
		return valueType;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ValueManifest#getValue()
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ValueManifest#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ValueManifest#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		if (value == null)
			throw new NullPointerException("Invalid value");  //$NON-NLS-1$

		this.value = value;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name");  //$NON-NLS-1$

		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		//FIXME really allow null for description?
//		if (description == null)
//			throw new NullPointerException("Invalid description");  //$NON-NLS-1$

		this.description = description;
	}

}
