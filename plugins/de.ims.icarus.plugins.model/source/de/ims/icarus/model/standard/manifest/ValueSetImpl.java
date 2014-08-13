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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.model.xml.ModelXmlElement;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ValueSetImpl implements ValueSet, ModelXmlHandler, ModelXmlElement {

	private final ValueType valueType;
	private final List<Object> values = new ArrayList<>();

	public ValueSetImpl(ValueType valueType) {
		if (valueType == null)
			throw new NullPointerException("Invalid valueType"); //$NON-NLS-1$

		this.valueType = valueType;
	}

	public ValueSetImpl(ValueType valueType, Collection<?> items) {
		this(valueType);

		if (items == null)
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$

		values.addAll(items);
	}

	public ValueSetImpl(ValueType valueType, Object...items) {
		this(valueType);

		if (items == null)
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$

		CollectionUtils.feedItems(values, items);
	}

	public ValueSetImpl(ValueType valueType, Class<?> enumClass) {
		this(valueType);

		if(!ValueType.ENUM.equals(valueType))
			throw new IllegalArgumentException("Cannot use the enum based constructor for other value types than "+ValueType.ENUM); //$NON-NLS-1$

		if (enumClass == null)
			throw new NullPointerException("Invalid enumClass"); //$NON-NLS-1$

		CollectionUtils.feedItems(values, (Object[]) enumClass.getEnumConstants());
	}


	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = valueType.hashCode() * (1+values.size());

		for(int i=0; i<values.size(); i++) {
			hash *= (1+values.get(i).hashCode());
		}

		return hash;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ValueSet) {
			ValueSet other = (ValueSet) obj;

			if(!valueType.equals(other.getValueType())) {
				return false;
			}

			if(values.size()!=other.valueCount()) {
				return false;
			}

			for(int i=0; i<values.size(); i++) {
				if(!values.get(i).equals(other.getValueAt(i))) {
					return false;
				}
			}

			return true;
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ValueSet@"+valueType.getXmlValue()+"["+values.size()+" items]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlElement#writeXml(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		ModelXmlUtils.writeValueSetElement(serializer, this, valueType);
	}

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_VALUES: {
			// no-op
		} break;

		case TAG_VALUE : {
			if(attributes.getLength()>0) {
				return new ValueManifestImpl(valueType);
			}
		} break;

		default:
			throw new SAXException("Unrecognized opening tag  '"+qName+"' in "+TAG_VALUES+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return this;
	}

	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (qName) {
		case TAG_VALUES: {
			return null;
		}

		case TAG_VALUE : {
			Object value = valueType.parse(text, manifestLocation.getClassLoader());

			addValue(value);
		} break;

		default:
			throw new SAXException("Unrecognized end tag  '"+qName+"' in "+TAG_VALUES+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return this;
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ModelXmlHandler handler)
			throws SAXException {
		switch (qName) {

		case TAG_VALUE : {
			Object value = (ValueManifestImpl) handler;

			addValue(value);
		} break;

		default:
			throw new SAXException("Unexpected nested element "+qName+" in "+TAG_VALUES+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	/**
	 * @return the valueType
	 */
	@Override
	public ValueType getValueType() {
		return valueType;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ValueSet#valueCount()
	 */
	@Override
	public int valueCount() {
		return values.size();
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ValueSet#getValueAt(int)
	 */
	@Override
	public Object getValueAt(int index) {
		return values.get(index);
	}

	public void addValue(Object value) throws ModelException {
		if (value == null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$

		valueType.checkValue(value);

		values.add(value);
	}
}
