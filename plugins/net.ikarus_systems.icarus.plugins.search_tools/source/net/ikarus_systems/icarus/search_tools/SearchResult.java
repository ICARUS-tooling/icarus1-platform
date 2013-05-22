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

import net.ikarus_systems.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchResult {
	
	int getDimension();
	
	SearchQuery getSource();

	int totalHitCount();
	
	int groupCount();
	
	int hitCountForGroup(int groupId);
	
	SearchConstraint groupConstraint(int groupId);
	
	ContentType getContentType();
	
	Object groupLabel(int groupId, int index);
}
