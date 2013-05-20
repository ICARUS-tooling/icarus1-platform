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

import net.ikarus_systems.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchResult {

	int totalHitCount();
	
	int groupCount();
	
	int hitCountForGroup(int groupId);
	
	SearchConstraint groupConstraint(int groupId);
	
	ContentType getContentType();
}
