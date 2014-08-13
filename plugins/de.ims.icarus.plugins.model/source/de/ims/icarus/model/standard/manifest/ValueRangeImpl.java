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

import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ValueRange;
import de.ims.icarus.model.util.types.UnsupportedValueTypeException;
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.model.xml.ModelXmlElement;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.classes.ClassUtils;

public class ValueRangeImpl implements ValueRange, ModelXmlHandler, ModelXmlElement {

	private final ValueType valueType;
	private Object lower, upper, stepSize;
	private boolean lowerIncluded = DEFAULT_LOWER_INCLUSIVE_VALUE,
			upperIncluded = DEFAULT_UPPER_INCLUSIVE_VALUE;

	// Stuff used for parsing
	private transient short currentField = 0;
	private static final short MIN_FIELD = 1;
	private static final short MAX_FIELD = 2;
	private static final short STEP_SIZE_FIELD = 3;

	private static final Set<ValueType> supportedValueTypes = ValueType.filterIncluding(
			ValueType.STRING,
			ValueType.INTEGER,
			ValueType.LONG,
			ValueType.FLOAT,
			ValueType.DOUBLE,
			ValueType.ENUM);

	public ValueRangeImpl(ValueType valueType) {
		if (valueType == null)
			throw new NullPointerException("Invalid valueType"); //$NON-NLS-1$

		if(!supportedValueTypes.contains(valueType))
			throw new UnsupportedValueTypeException(valueType);

		this.valueType = valueType;
	}

	public ValueRangeImpl(ValueType valueType, boolean lowerIncluded,
			boolean upperIncluded) {
		this(valueType);

		this.lowerIncluded = lowerIncluded;
		this.upperIncluded = upperIncluded;
	}

	public ValueRangeImpl(ValueType valueType, Object lower, Object upper, boolean lowerIncluded,
			boolean upperIncluded) {
		this(valueType, lowerIncluded, upperIncluded);

		setLowerBound(lower);
		setUpperBound(upper);
	}

	public ValueRangeImpl(ValueType valueType, Object lower, Object upper) {
		this(valueType, lower, upper, true, true);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = valueType.hashCode();

		if(lowerIncluded!=DEFAULT_LOWER_INCLUSIVE_VALUE) {
			hash *= -1;
		}

		if(upperIncluded!=DEFAULT_UPPER_INCLUSIVE_VALUE) {
			hash *= -2;
		}

		if(lower!=null) {
			hash *= lower.hashCode();
		}

		if(upper!=null) {
			hash *= upper.hashCode();
		}

		return hash;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ValueRange) {
			ValueRange other = (ValueRange)obj;

			return valueType.equals(other.getValueType())
					&& lowerIncluded==other.isLowerBoundInclusive()
					&& upperIncluded==other.isUpperBoundInclusive()
					&& ClassUtils.equals(lower, other.getLowerBound())
					&& ClassUtils.equals(upper, other.getUpperBound());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ValueRange@").append(valueType.getXmlValue()); //$NON-NLS-1$

		if(lowerIncluded) {
			sb.append('[');
		} else {
			sb.append('(');
		}

		if(lower==null && upper==null) {
			sb.append('-');
		} else {
			sb.append(lower).append(',').append(upper);
		}

		if(upperIncluded) {
			sb.append('[');
		} else {
			sb.append(')');
		}


		return sb.toString();
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlElement#writeXml(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		ModelXmlUtils.writeValueRangeElement(serializer, this, valueType);
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

		case TAG_STEP_SIZE : {
			currentField = STEP_SIZE_FIELD;
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

		case TAG_STEP_SIZE : {
			if(text!=null && stepSize==null) {
				setStepSize(valueType.parse(text, manifestLocation.getClassLoader()));
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
	 * @see de.ims.icarus.model.api.manifest.ValueRange#getStepSize()
	 */
	@Override
	public Object getStepSize() {
		return stepSize;
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

	protected void checkValue(Object value) {
		Class<?> type = valueType.checkValue(value);

		if(!Comparable.class.isAssignableFrom(type))
			throw new ModelException(ModelError.MANIFEST_TYPE_CAST,
					"Provided value for value range does not implement java.lang.Comparable: "+type.getName()); //$NON-NLS-1$
	}

	/**
	 * @param lower the lower to set
	 */
	public void setLowerBound(Object lower) throws ModelException {
		if (lower == null)
			throw new NullPointerException("Invalid lower bound"); //$NON-NLS-1$

		checkValue(lower);

		this.lower = lower;
	}

	/**
	 * @param upper the upper to set
	 */
	public void setUpperBound(Object upper) throws ModelException {
		if (upper == null)
			throw new NullPointerException("Invalid upper bound"); //$NON-NLS-1$

		checkValue(upper);

		this.upper = upper;
	}

	/**
	 * @param upper the upper to set
	 */
	public void setStepSize(Object stepSize) throws ModelException {
		if (stepSize == null)
			throw new NullPointerException("Invalid stepSize"); //$NON-NLS-1$

		checkValue(stepSize);

		this.stepSize = stepSize;
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