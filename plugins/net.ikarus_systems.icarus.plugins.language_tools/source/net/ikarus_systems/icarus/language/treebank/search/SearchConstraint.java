/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.treebank.search;

import net.ikarus_systems.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchConstraint extends Identity {
	
	String getToken();

	SearchOperator getOperator();
	
	void setOperator(SearchOperator operator);
	
	SearchConstraint clone();
	
	boolean matches(Object value);
}
