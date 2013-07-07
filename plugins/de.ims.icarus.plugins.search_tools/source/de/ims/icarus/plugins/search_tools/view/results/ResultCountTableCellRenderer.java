/*
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
