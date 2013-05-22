/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools;

import java.io.Serializable;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchConstraint extends Serializable {
	
	String getId();
	
	Object getValue();

	SearchOperator getOperator();
	
	SearchConstraint clone();
	
	boolean matches(Object value);
	
	boolean isUndefined();
}
