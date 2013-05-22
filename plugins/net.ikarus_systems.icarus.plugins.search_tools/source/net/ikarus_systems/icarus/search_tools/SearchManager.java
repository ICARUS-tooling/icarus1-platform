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


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class SearchManager {
	
	private static SearchManager instance;
	
	public static SearchManager getInstance() {
		if(instance==null) {
			synchronized (SearchManager.class) {
				if(instance==null) {
					instance = new SearchManager();
				}
			}
		}
		
		return instance;
	}

	private SearchManager() {
		// no-op
	}

	public void executeSearch(Search search) {
		
	}
}
