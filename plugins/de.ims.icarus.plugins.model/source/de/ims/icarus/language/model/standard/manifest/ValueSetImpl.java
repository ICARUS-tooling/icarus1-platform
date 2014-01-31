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
import java.util.List;

import de.ims.icarus.language.model.manifest.ValueSet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ValueSetImpl extends DerivedObject<ValueSet> implements ValueSet {

	private String id;
	private List<Object> values = new ArrayList<>();

	public ValueSetImpl() {

	}

	public ValueSetImpl(Collection<?> items) {
		if (items == null)
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$

		values.addAll(items);
	}

	public ValueSetImpl(ValueSet template) {
		super(template);

		for(int i=0; i<template.valueCount(); i++) {
			values.add(template.getValueAt(i));
		}
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Template#getId()
	 */
	@Override
	public String getId() {
		return id;
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
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.DerivedObject#getXmlTag()
	 */
	@Override
	protected String getXmlTag() {
		return "values"; //$NON-NLS-1$
	}

}
