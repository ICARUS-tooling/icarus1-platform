/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.standard;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;

import de.ims.icarus.search_tools.SearchOperator;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultCaseInsensitiveConstraint extends DefaultConstraint {

	private static final long serialVersionUID = -7648734660494017554L;
	
	@XmlTransient
	protected Object lowercaseValue;

	public DefaultCaseInsensitiveConstraint(String token, Object value,
			SearchOperator operator) {
		super(token, value, operator);
	}
	
	@Override
	protected Object getConstraint() {
		return lowercaseValue;
	}

	@XmlElements({ @XmlElement(name = "string", type = String.class),
			@XmlElement(name = "integer", type = Integer.class),
			@XmlElement(name = "float", type = Float.class),
			@XmlElement(name = "double", type = Double.class),
			@XmlElement(name = "long", type = Long.class),
			@XmlElement(name = "boolean", type = Boolean.class) })
	@Override
	public void setValue(Object value) {
		super.setValue(value);
		lowercaseValue = ((String)value).toLowerCase();
	}

	public Object getLowercaseValue() {
		return lowercaseValue;
	}

}