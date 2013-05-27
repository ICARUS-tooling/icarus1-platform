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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import net.ikarus_systems.icarus.language.LanguageUtils;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.search_tools.SearchOperator;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="constraint")
public class DefaultConstraint implements SearchConstraint {
	
	private static final long serialVersionUID = 8086598627849516305L;

	@XmlAttribute(name="token")
	private String token;
	
	@XmlElements({
		@XmlElement(name="string", type=String.class),
		@XmlElement(name="integer", type=Integer.class),
		@XmlElement(name="float", type=Float.class),
		@XmlElement(name="double", type=Double.class),
		@XmlElement(name="boolean", type=Boolean.class),
	})
	private Object value;
	
	@XmlAttribute(name="operator")
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean matches(Object value) {
		
		value = prepareValue(value);
		Object constraint = this.value;
		
		switch (operator) {
		case CONTAINS:
			return ((String)value).contains((String)constraint);

		case CONTAINS_NOT:
			return !((String)value).contains((String)constraint);
			
		case EQUALS:
			return value.equals(constraint);
			
		case EQUALS_NOT:
			return !value.equals(constraint);
			
		case MATCHES: {
			Pattern pattern = SearchManager.getPattern((String)constraint);
			return pattern==null ? false : pattern.matcher((String)value).find();
		}
			
		case MATCHES_NOT: {
			Pattern pattern = SearchManager.getPattern((String)constraint);
			return pattern==null ? true : !pattern.matcher((String)value).find();
		}
		
		case LESS_THAN:
			return ((Comparable)value).compareTo((Comparable)constraint)<0;
		
		case GREATER_THAN:
			return ((Comparable)value).compareTo((Comparable)constraint)>0;
			
		case GREATER_OR_EQUAL:
			return ((Comparable)value).compareTo((Comparable)constraint)>=0;
			
		case LESS_OR_EQUAL:
			return ((Comparable)value).compareTo((Comparable)constraint)<=0;
			
		case GROUPING:
			return true;
		}
		
		return false;
	}
	
	protected Object prepareValue(Object value) {
		return value;
	}

	@Override
	public SearchConstraint clone() {
		return new DefaultConstraint(token, value, operator);
	}

	public void setValue(Object value) {
		if(value==null)
			throw new IllegalArgumentException("Invalid value"); //$NON-NLS-1$
		this.value = value;
	}

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
				&& (value==null || value.equals(LanguageUtils.DATA_UNDEFINED_LABEL)
				|| value.equals(LanguageUtils.DATA_UNDEFINED_VALUE)
				|| "".equals(value)); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchConstraint#getToken()
	 */
	@Override
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		if(token==null)
			throw new IllegalArgumentException("Invalid token"); //$NON-NLS-1$
		this.token = token;
	}

}
