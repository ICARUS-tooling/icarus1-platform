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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ValueRange;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;

public class ValueRangeImpl implements ValueRange, ModelXmlHandler {

	private ValueType valueType = ValueType.STRING;
	private Object lower, upper;
	private boolean lowerIncluded = DEFAULT_LOWER_INCLUSIVE_VALUE,
			upperIncluded = DEFAULT_UPPER_INCLUSIVE_VALUE;

	private transient short currentField = 0;
	private static final short MIN_FIELD = 1;
	private static final short MAX_FIELD = 2;

	public ValueRangeImpl() {
		// no-op
	}

	public ValueRangeImpl(ValueType valueType) {
		setValueType(valueType);
	}

	public ValueRangeImpl(boolean lowerIncluded,
			boolean upperIncluded) {
		this.lowerIncluded = lowerIncluded;
		this.upperIncluded = upperIncluded;
	}

	public ValueRangeImpl(Object lower, Object upper, boolean lowerIncluded,
			boolean upperIncluded) {
		this(lowerIncluded, upperIncluded);

		this.lower = lower;
		this.upper = upper;
	}

	/**
	 * @param attributes
	 */
	protected void readAttributes(Attributes attributes) {
		String lowerIncluded = ModelXmlUtils.normalize(attributes, ATTR_INCLUDE_MIN);
		if(lowerIncluded!=null) {
			setLowerBoundIncluded(Boolean.parseBoolean(lowerIncluded));
		}
		String upperIncluded = ModelXmlUtils.normalize(attributes, ATTR_INCLUDE_MAX);
		if(upperIncluded!=null) {
			setUpperBoundIncluded(Boolean.parseBoolean(upperIncluded));
		}

		valueType = ModelXmlUtils.typeValue(attributes);
	}

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_RANGE: {
			readAttributes(attributes);
		} break;

		case TAG_MIN : {
			currentField = MIN_FIELD;
		} break;

		case TAG_MAX : {
			currentField = MAX_FIELD;
		} break;

		case TAG_EVAL : {
			return new ExpressionXmlHandler();
		}

		default:
			throw new SAXException("Unrecognized opening tag  '"+qName+"' in "+TAG_RANGE+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return this;
	}

	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (qName) {
		case TAG_RANGE: {
			return null;
		}

		case TAG_MIN : {
			if(text!=null && lower==null) {
				setLowerBound(valueType.parse(text, manifestLocation.getClassLoader()));
			}
		} break;

		case TAG_MAX : {
			if(text!=null && upper==null) {
				setUpperBound(valueType.parse(text, manifestLocation.getClassLoader()));
			}
		} break;

		default:
			throw new SAXException("Unrecognized end tag  '"+qName+"' in "+TAG_RANGE+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

		case TAG_EVAL : {
			switch (currentField) {
			case MIN_FIELD:
				setLowerBound(((ExpressionXmlHandler) handler).createExpression());
				break;

			case MAX_FIELD:
				setUpperBound(((ExpressionXmlHandler) handler).createExpression());
				break;

			default:
				throw new IllegalStateException("Unable to assign expression to correct field"); //$NON-NLS-1$
			}

			currentField = 0;
		} break;

		default:
			throw new SAXException("Unrecognized nested tag  '"+qName+"' in "+TAG_RANGE+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
	 * @param valueType the valueType to set
	 */
	public void setValueType(ValueType valueType) {
		if (valueType == null)
			throw new NullPointerException("Invalid valueType"); //$NON-NLS-1$

		this.valueType = valueType;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ValueRange#getLowerBound()
	 */
	@Override
	public Object getLowerBound() {
		return lower;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ValueRange#getUpperBound()
	 */
	@Override
	public Object getUpperBound() {
		return upper;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ValueRange#isLowerBoundInclusive()
	 */
	@Override
	public boolean isLowerBoundInclusive() {
		return lowerIncluded;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ValueRange#isUpperBoundInclusive()
	 */
	@Override
	public boolean isUpperBoundInclusive() {
		return upperIncluded;
	}

	/**
	 * @param lower the lower to set
	 */
	public void setLowerBound(Object lower) {
		this.lower = lower;
	}

	/**
	 * @param upper the upper to set
	 */
	public void setUpperBound(Object upper) {
		this.upper = upper;
	}

	/**
	 * @param lowerIncluded the lowerIncluded to set
	 */
	public void setLowerBoundIncluded(boolean lowerIncluded) {
		this.lowerIncluded = lowerIncluded;
	}

	/**
	 * @param upperIncluded the upperIncluded to set
	 */
	public void setUpperBoundIncluded(boolean upperIncluded) {
		this.upperIncluded = upperIncluded;
	}

}