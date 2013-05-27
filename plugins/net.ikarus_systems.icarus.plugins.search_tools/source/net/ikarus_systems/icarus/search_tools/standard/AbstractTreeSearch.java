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

import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchQuery;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AbstractTreeSearch extends Search {

	public AbstractTreeSearch(SearchQuery query) {
		super(query);
		// TODO Auto-generated constructor stub
	}
	
	public boolean validate() {
		// TODO check tree structure etc
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.Search#doSearch()
	 */
	@Override
	public void doSearch() throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.Search#getResult()
	 */
	@Override
	public SearchResult getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.Search#getProgress()
	 */
	@Override
	public int getProgress() {
		// TODO Auto-generated method stub
		return 0;
	}

}
