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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;

import de.ims.icarus.language.model.manifest.ValueSet;
import de.ims.icarus.language.model.meta.ValueType;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.language.model.xml.XmlWriter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ValueSetImpl extends AbstractDerivable<ValueSet> implements ValueSet {

	private String name;
	private String description;
	private String id;
	private Icon icon;
	private ValueType valueType = ValueType.STRING;
	private List<Object> values = new ArrayList<>();

	public ValueSetImpl() {

	}

	public ValueSetImpl(Collection<?> items) {
		if (items == null)
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$

		values.addAll(items);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractDerivable#readTemplate(de.ims.icarus.language.model.manifest.Derivable)
	 */
	@Override
	protected void readTemplate(ValueSet template) {
		super.readTemplate(template);

		for(int i=0; i<template.valueCount(); i++) {
			values.add(i,template.getValueAt(i));
		}
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @return the icon
	 */
	@Override
	public Icon getIcon() {
		return icon;
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
	 * @param name the name to set
	 */
	public void setName(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		if (description == null)
			throw new NullPointerException("Invalid description"); //$NON-NLS-1$

		this.description = description;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

		this.id = id;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(Icon icon) {
		if (icon == null)
			throw new NullPointerException("Invalid icon"); //$NON-NLS-1$

		this.icon = icon;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ValueSet#valueCount()
	 */
	@Override
	public int valueCount() {
		return values.size();
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ValueSet#getValueAt(int)
	 */
	@Override
	public Object getValueAt(int index) {
		return values.get(0);
	}

	public void addValue(Object value) {
		if (value == null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$

		values.add(value);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractDerivable#getXmlTag()
	 */
	@Override
	protected String getXmlTag() {
		return "values"; //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractDerivable#writeTemplateXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlAttributes(serializer);

		writeXmlAttribute(serializer, "id", id, getTemplate().getId()); //$NON-NLS-1$
		writeXmlAttribute(serializer, "name", name, getTemplate().getName()); //$NON-NLS-1$
		writeXmlAttribute(serializer, "description", description, getTemplate().getDescription()); //$NON-NLS-1$
		writeXmlAttribute(serializer, "icon", icon, getTemplate().getIcon()); //$NON-NLS-1$

		serializer.writeAttribute("template-id", getTemplate().getId()); //$NON-NLS-1$

		if(valueType!=ValueType.STRING) {
			writeXmlAttribute(serializer, "type", valueType, getTemplate().getValueType()); //$NON-NLS-1$
		}
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractDerivable#writeFullXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlAttributes(serializer);

		serializer.writeAttribute("id", id); //$NON-NLS-1$
		serializer.writeAttribute("name", name); //$NON-NLS-1$
		serializer.writeAttribute("description", description); //$NON-NLS-1$
		writeXmlAttribute(serializer, "icon", icon); //$NON-NLS-1$
		if(valueType!=ValueType.STRING) {
			writeXmlAttribute(serializer, "type", valueType); //$NON-NLS-1$
		}
	}

	private Set<Object> valuesAsSet(ValueSet values) {
		if(values==null || values.valueCount()==0) {
			return Collections.emptySet();
		}

		Set<Object> set = new LinkedHashSet<>(values.valueCount());
		for(int i=0; i<values.valueCount(); i++) {
			set.add(values.getValueAt(i));
		}

		return set;
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractDerivable#writeTemplateXmlElements(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlElements(serializer);

		Set<Object> derived = valuesAsSet(getTemplate());

		for(Object value : values) {
			if(derived.contains(value)) {
				continue;
			}

			XmlWriter.writeValueElement(serializer, "value", value, valueType); //$NON-NLS-1$
		}
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractDerivable#writeFullXmlElements(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlElements(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlElements(serializer);

		for(Object value : values) {
			XmlWriter.writeValueElement(serializer, "value", value, valueType); //$NON-NLS-1$
		}
	}
}
