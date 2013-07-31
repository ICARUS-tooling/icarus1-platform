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

import java.awt.Component;
import java.awt.Cursor;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.ui.NumberDisplayMode;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ResultCountTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -4445133033791861253L;
	
	protected NumberDisplayMode displayMode = NumberDisplayMode.RAW;
	
	protected SearchResult searchResult;

	public ResultCountTableCellRenderer() {
		setHorizontalAlignment(CENTER);
	}

	/**
	 * @return the searchResult
	 */
	public SearchResult getSearchResult() {
		return searchResult;
	}

	/**
	 * @param searchResult the searchResult to set
	 */
	public void setSearchResult(SearchResult searchResult) {
		this.searchResult = searchResult;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if(value==null || (Integer) value == 0) {
			value = null;
		} else if(displayMode==NumberDisplayMode.PERCENTAGE 
				&& searchResult!=null) {
			double p = (Integer)value/(double)searchResult.getTotalMatchCount() * 100d;
			value = p<ResultCountUtils.getMinPercentage() ? 0 : String.format("%1.2f%%", p); //$NON-NLS-1$
			
			// TODO handle highlighting of p>highlightPercentage
		}
		
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);

		setCursor(getText().equals("") ? Cursor.getDefaultCursor() : Cursor //$NON-NLS-1$
				.getPredefinedCursor(Cursor.HAND_CURSOR));

		return this;
	}

	@Override
	protected void setValue(Object value) {
		setText(value == null ? "" : value.toString()); //$NON-NLS-1$
	}

	public NumberDisplayMode getDisplayMode() {
		return displayMode;
	}

	public void setDisplayMode(NumberDisplayMode displayMode) {
		this.displayMode = displayMode;
	}
}
