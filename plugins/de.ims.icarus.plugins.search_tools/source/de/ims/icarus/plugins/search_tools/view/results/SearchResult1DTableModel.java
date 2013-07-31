/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.search_tools.view.results;

import de.ims.icarus.search_tools.result.SearchResult;

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

	public SearchResult1DTableModel(SearchResult resultData,
			boolean ommitDimensionCheck) {
		super(resultData, ommitDimensionCheck);
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
		return resultData.getSubResult(translateRowIndex(row));
	}
	
	@Override
	public void reset() {
		// nothing to do here
	}

	@Override
	public Integer getValueAt(int row, int column) {
		return resultData.getMatchCount(translateRowIndex(row));
	}
}
