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

import java.awt.Component;
import java.awt.Cursor;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.ui.NumberDisplayMode;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ResultCountListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 5422553217008739942L;

	protected NumberDisplayMode displayMode = NumberDisplayMode.RAW;
	
	
	protected SearchResult searchResult;

	public ResultCountListCellRenderer() {
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
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		if(value==null || (Integer) value == 0) {
			value = null;
		} else if(displayMode==NumberDisplayMode.PERCENTAGE 
				&& searchResult!=null) {
			double p = (Integer)value/(double)searchResult.getTotalMatchCount() * 100d;
			value = p<ResultCountUtils.getMinPercentage() ? 0 : String.format("%1.2f%%", p); //$NON-NLS-1$
			
			// TODO handle highlighting of p>highlightPercentage
		}
	
		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);

		setCursor(getText().equals("") ? Cursor.getDefaultCursor() : Cursor //$NON-NLS-1$
				.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		return this;
	}

	public NumberDisplayMode getDisplayMode() {
		return displayMode;
	}

	public void setDisplayMode(NumberDisplayMode displayMode) {
		this.displayMode = displayMode;
	}
}
