/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.standard;

import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.search_tools.SearchOperator;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;

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
	
	private SearchOperator operator;
	
	public DefaultConstraint(String token, Object value, SearchOperator operator) {
		setToken(token);
		setValue(value);
		setOperator(operator);
	}
	
	@SuppressWarnings("unused")
	private DefaultConstraint() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchConstraint#getValue()
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchConstraint#getOperator()
	 */
	@Override
	public SearchOperator getOperator() {
		return operator;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchConstraint#matches(java.lang.Object)
	 */
	@Override
	public boolean matches(Object value) {
		return operator.apply(prepareValue(value), getConstraint());
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
		Pattern pattern = SearchManager.getPattern((String)constraint);
		return pattern==null ? false : pattern.matcher((String)value).find();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected int compare(Object value, Object constraint) {
		return ((Comparable)value).compareTo((Comparable)constraint);
	}
	
	protected Object prepareValue(Object value) {
		return value;
	}

	@Override
	public SearchConstraint clone() {
		return new DefaultConstraint(token, value, operator);
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
			throw new IllegalArgumentException("Invalid value"); //$NON-NLS-1$
		this.value = value;
	}

	@XmlJavaTypeAdapter(value=OperatorAdapter.class)
	@XmlAttribute(name="operator")
	public void setOperator(SearchOperator operator) {
		if(operator==null)
			throw new IllegalArgumentException("Invalid operator"); //$NON-NLS-1$
		this.operator = operator;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchConstraint#isUndefined()
	 */
	@Override
	public boolean isUndefined() {
		return operator!=SearchOperator.GROUPING
				&& (value==null || value.equals(SearchUtils.DATA_UNDEFINED_LABEL)
				|| value.equals(SearchUtils.DATA_UNDEFINED_VALUE)
				|| "".equals(value)); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchConstraint#getToken()
	 */
	@Override
	public String getToken() {
		return token;
	}

	@XmlAttribute(name="token")
	public void setToken(String token) {
		if(token==null)
			throw new IllegalArgumentException("Invalid token"); //$NON-NLS-1$
		this.token = token;
	}

}
