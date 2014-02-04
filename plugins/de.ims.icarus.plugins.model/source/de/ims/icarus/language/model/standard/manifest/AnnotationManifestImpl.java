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
package de.ims.icarus.language.model.standard.manifest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.ims.icarus.language.model.manifest.AnnotationManifest;
import de.ims.icarus.language.model.manifest.ManifestType;
import de.ims.icarus.language.model.manifest.ValueRange;
import de.ims.icarus.language.model.manifest.ValueSet;
import de.ims.icarus.language.model.meta.ValueType;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.language.model.xml.XmlWriter;
import de.ims.icarus.util.ClassUtils;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotationManifestImpl extends AbstractManifest<AnnotationManifest> implements AnnotationManifest {

	private String key;
	private List<String> aliases;
	private ValueType valueType = ValueType.UNKNOWN;
	private ValueSet values;
	private ValueRange valueRange;

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#readTemplate(de.ims.icarus.language.model.manifest.MemberManifest)
	 */
	@Override
	protected void readTemplate(AnnotationManifest template) {
		super.readTemplate(template);

		for(String alias : template.getAliases()) {
			if(aliases==null) {
				aliases = new ArrayList<>();
			}

			if(!aliases.contains(alias)) {
				aliases.add(alias);
			}
		}

		if(key==null) {
			key = template.getKey();
		}
		if(valueType==null) {
			valueType = template.getValueType();
		}
		if(valueRange==null) {
			valueRange = template.getSupportedRange();
		}

		ValueSet values = template.getSupportedValues();
		if(values!=null) {
			if(this.values==null) {
				this.values = new ValueSetImpl();
			}

			this.values.setTemplate(values);
		}
	}

	/**
	 * @return the key
	 */
	@Override
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$

		this.key = key;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.ANNOTATION_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationManifest#getAliases()
	 */
	@Override
	public List<String> getAliases() {
		List<String> result = aliases;

		if(result==null) {
			result = Collections.emptyList();
		} else {
			result = CollectionUtils.getListProxy(result);
		}

		return result;
	}

	public void addAlias(String alias) {
		if (alias == null)
			throw new NullPointerException("Invalid alias"); //$NON-NLS-1$

		if(aliases==null) {
			aliases = new ArrayList<>(3);
		}

		if(aliases.contains(alias))
			throw new IllegalArgumentException("Alias already registered: "+alias); //$NON-NLS-1$

		aliases.add(alias);
	}

	/**
	 * This implementation returns {@code true} when at least one of
	 * {@link ValueRange} or {@link Values.ValueIteratorFactory} previously
	 * assigned to this manifest is non-null.
	 *
	 * @see de.ims.icarus.language.model.manifest.AnnotationManifest#isBounded()
	 */
	@Override
	public boolean isBounded() {
		return valueRange!=null || values!=null;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationManifest#getSupportedRange()
	 */
	@Override
	public ValueRange getSupportedRange() {
		return valueRange;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationManifest#getSupportedValues()
	 */
	@Override
	public ValueSet getSupportedValues() {
		return values;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.AnnotationManifest#getValueType()
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

	public void setValues(ValueSet values) {
		if (values == null)
			throw new NullPointerException("Invalid values"); //$NON-NLS-1$

		this.values = values;
	}

	/**
	 * @param valueRange the valueRange to set
	 */
	public void setValueRange(ValueRange valueRange) {
		if (valueRange == null)
			throw new NullPointerException("Invalid valueRange"); //$NON-NLS-1$

		this.valueRange = valueRange;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractDerivable#getXmlTag()
	 */
	@Override
	protected String getXmlTag() {
		return "annotation"; //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeTemplateXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlAttributes(serializer);

		writeXmlAttribute(serializer, "key", key, getTemplate().getKey()); //$NON-NLS-1$
		writeXmlAttribute(serializer, "type", valueType, getTemplate().getValueType()); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeFullXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlAttributes(serializer);

		serializer.writeAttribute("key", key); //$NON-NLS-1$
		serializer.writeAttribute("type", valueType.getValue()); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeTemplateXmlElements(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlElements(serializer);

		Set<String> derived = new HashSet<>(getTemplate().getAliases());

		for(String alias : getAliases()) {
			if(derived.contains(alias)) {
				continue;
			}

			XmlWriter.writeAliasElement(serializer, alias);
		}

		XmlWriter.writeValuesElement(serializer, values, valueType);

		if(!ClassUtils.equals(valueRange, getTemplate().getSupportedRange())) {
			XmlWriter.writeValueRangeElement(serializer, valueRange, valueType);
		}
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeFullXmlElements(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlElements(serializer);

		for(String alias : getAliases()) {
			XmlWriter.writeAliasElement(serializer, alias);
		}

		XmlWriter.writeValuesElement(serializer, values, valueType);
		XmlWriter.writeValueRangeElement(serializer, valueRange, valueType);
	}

}
