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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchQuery {

	void parseQuery(String query);
	
	String getQuery();
}
