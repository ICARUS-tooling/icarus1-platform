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
import net.ikarus_systems.icarus.search_tools.SearchOperator;
import net.ikarus_systems.icarus.search_tools.SearchUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="constraint")
public class DefaultConstraint implements SearchConstraint {
	
	@XmlAttribute(name="id")
	private String id;
	
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
	
	public DefaultConstraint(String id, Object value, SearchOperator operator) {
		setId(id);
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
			Pattern pattern = SearchUtils.getPattern((String)constraint);
			return pattern==null ? false : pattern.matcher((String)value).find();
		}
			
		case MATCHES_NOT: {
			Pattern pattern = SearchUtils.getPattern((String)constraint);
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

	@Override
	public SearchConstraint clone() {
		return new DefaultConstraint(id, value, operator);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.SearchConstraint#getId()
	 */
	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		if(id==null)
			throw new IllegalArgumentException("Invalid id"); //$NON-NLS-1$
		this.id = id;
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
		return value==null || value.equals(LanguageUtils.DATA_UNDEFINED_LABEL)
				|| value.equals(LanguageUtils.DATA_UNDEFINED_VALUE)
				|| "".equals(value); //$NON-NLS-1$
	}

}
