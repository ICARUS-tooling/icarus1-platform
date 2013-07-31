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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.standard;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.ConstraintFactory;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.SearchParameters;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractConstraintFactory implements ConstraintFactory, SearchParameters {

	private String nameKey, descriptionKey;
	
	private String token;
	
	private int type;
	
	protected static final Object[] DEFAULT_UNDEFINED_VALUESET = {
		LanguageUtils.DATA_UNDEFINED_LABEL
	};

	public AbstractConstraintFactory(String token, int type, String nameKey, String descriptionKey) {
		this.token = token;
		this.nameKey = nameKey;
		this.descriptionKey = descriptionKey;
		this.type = type;
	}
	
	protected boolean isFlagSet(int flags, int mask) {
		return (flags & mask) == mask;
	}
	
	/**
	 * 
	 * @see de.ims.icarus.search_tools.ConstraintFactory#getConstraintType()
	 */
	@Override
	public int getConstraintType() {
		return type;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getToken() {
		return token;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return ResourceManager.getInstance().get(nameKey);
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(descriptionKey);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#getSupportedOperators()
	 */
	@Override
	public SearchOperator[] getSupportedOperators() {
		return DefaultSearchOperator.values();
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#getValueClass()
	 */
	@Override
	public Class<?> getValueClass() {
		return String.class;
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#getDefaultValue()
	 */
	@Override
	public Object getDefaultValue() {
		return LanguageUtils.DATA_UNDEFINED_LABEL;
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#getValueSet()
	 */
	@Override
	public Object[] getLabelSet() {
		return DEFAULT_UNDEFINED_VALUESET;
	}

	@Override
	public Object labelToValue(Object label) {
		return label;
	}

	@Override
	public Object valueToLabel(Object value) {
		return value;
	}
}
