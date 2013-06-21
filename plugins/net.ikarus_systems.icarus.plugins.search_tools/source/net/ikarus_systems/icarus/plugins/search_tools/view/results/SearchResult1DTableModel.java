/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view.results;

import net.ikarus_systems.icarus.search_tools.result.SearchResult;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchResult1DTableModel extends SearchResultTableModel {

	private static final long serialVersionUID = 5078322583511186683L;

	public SearchResult1DTableModel(SearchResult resultData) {
		super(resultData);
	}

	@Override
	public int getSupportedDimensions() {
		return 1;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnName(int column) {
		return resultData.getGroupLabel(rowDimension).toString();
	}

	@Override
	public void flip() {
		// no flipping here
	}

	@Override
	public boolean isFlipped() {
		return false;
	}
	
	@Override
	public SearchResult getSubResultAt(int row, int column) {
		return resultData.getSubResult(getRowIndex(row));
	}
	
	@Override
	public void reset() {
		// nothing to do here
	}

	@Override
	public Integer getValueAt(int row, int column) {
		return resultData.getMatchCount(getRowIndex(row));
	}
}
