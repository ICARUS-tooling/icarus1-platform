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

import java.util.regex.Matcher;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchOperator;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="constraint")
public class DefaultConstraint implements SearchConstraint {

	private static final long serialVersionUID = 8086598627849516305L;

	private String token;

	private Object value;

	private Object specifier;

	private boolean active = true;

	private SearchOperator operator;

	public DefaultConstraint(String token, Object value, SearchOperator operator) {
		init();

		setToken(token);
		setValue(value);
		setOperator(operator);
	}

	public DefaultConstraint(String token, Object value, SearchOperator operator, Object specifier) {
		this(token, value, operator);

		setSpecifier(specifier);
	}

	public DefaultConstraint(SearchConstraint source) {
		init();

		setToken(source.getToken());
		setValue(source.getValue());
		setOperator(source.getOperator());
		setSpecifier(source.getSpecifier());
		setActive(source.isActive());
	}

	protected void init() {
		// for subclasses
	}

	@SuppressWarnings("unused")
	private DefaultConstraint() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getValue()
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getOperator()
	 */
	@Override
	public SearchOperator getOperator() {
		return operator;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#matches(java.lang.Object)
	 */
	@Override
	public boolean matches(Object value) {
		return operator.apply(getInstance(value), getConstraint());
	}

	protected Object getConstraint() {
		return value;
	}

	protected boolean equals(Object value, Object constraint) {
		return value.equals(constraint);
	}

	protected boolean contains(Object value, Object constraint) {
		return ((String)value).contains((String)constraint);
	}

	protected boolean matches(Object value, Object constraint) {
		Matcher matcher = SearchManager.getMatcher((String)constraint, (String)value);
		boolean result = matcher==null ? false : matcher.find();
		SearchManager.recycleMatcher(matcher);

		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected int compare(Object value, Object constraint) {
		return ((Comparable)value).compareTo((Comparable)constraint);
	}

	@Override
	public Object getInstance(Object value) {
		return value;
	}

	@Override
	public SearchConstraint clone() {
		// Note that the 'active' state is not cloned!
		return new DefaultConstraint(token, value, operator);
	}

	@Override
	public String toString() {
		return String.format("[%s: %s%s%s]", getClass().getSimpleName(),  //$NON-NLS-1$
				token, operator.getSymbol(), value);
	}

	@XmlElements({
		@XmlElement(name="string", type=String.class),
		@XmlElement(name="integer", type=Integer.class),
		@XmlElement(name="float", type=Float.class),
		@XmlElement(name="double", type=Double.class),
		@XmlElement(name="long", type=Long.class),
		@XmlElement(name="boolean", type=Boolean.class),
	})
	public void setValue(Object value) {
		if(value==null)
			throw new NullPointerException("Invalid value"); //$NON-NLS-1$
		this.value = value;
	}

	@XmlJavaTypeAdapter(value=OperatorAdapter.class)
	@XmlAttribute(name="operator")
	public void setOperator(SearchOperator operator) {
		if(operator==null)
			throw new NullPointerException("Invalid operator"); //$NON-NLS-1$
		this.operator = operator;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#isUndefined()
	 */
	@Override
	public boolean isUndefined() {
		return !SearchManager.isGroupingOperator(operator)
				&& (value==null || value.equals(LanguageConstants.DATA_UNDEFINED_LABEL)
				|| value.equals(LanguageConstants.DATA_UNDEFINED_VALUE)
				|| value.equals(LanguageConstants.DATA_UNDEFINED_FLOAT_VALUE)
				|| value.equals(LanguageConstants.DATA_UNDEFINED_DOUBLE_VALUE)
				|| "".equals(value)); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getToken()
	 */
	@Override
	public String getToken() {
		return token;
	}

	@XmlAttribute(name="token")
	public void setToken(String token) {
		if(token==null)
			throw new NullPointerException("Invalid token"); //$NON-NLS-1$
		this.token = token;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#setActive(boolean)
	 */
	@XmlAttribute(name="active",required=false)
	@Override
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#isActive()
	 */
	@Override
	public boolean isActive() {
		return active;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getSpecifier()
	 */
	@Override
	public Object getSpecifier() {
		return specifier;
	}

	@XmlElement(name="specifier", required=false)
	public void setSpecifier(Object specifier) {
		this.specifier = specifier;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#getLabel(java.lang.Object)
	 */
	@Override
	public Object getLabel(Object value) {
		return value;
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#isMultiplexing()
	 */
	@Override
	public boolean isMultiplexing() {
		return false;
	}

	@Override
	public void group(GroupCache cache, int groupId, Object value) {
		throw new UnsupportedOperationException();
	}

}
