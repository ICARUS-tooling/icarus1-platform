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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.ui.NumberDisplayMode;
import net.ikarus_systems.icarus.util.Exceptions;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ResultCountTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -4445133033791861253L;
	
	protected NumberDisplayMode displayMode = NumberDisplayMode.RAW;
	
	protected static double minPercentage = 0.01;
	protected static double highlightPercentage = 10.0;
	protected static Color highlightColor = Color.blue;
	
	protected SearchResult searchResult;

	public ResultCountTableCellRenderer() {
		setHorizontalAlignment(CENTER);
	}
	
	public void install(JTable table, JComponent owner) {
		Exceptions.testNullArgument(table, "table"); //$NON-NLS-1$

		table.setDefaultRenderer(Integer.class, this);		
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
		
		if(value==null || (Integer) value == 0)
			value = null;
		else if(displayMode==NumberDisplayMode.PERCENTAGE 
				&& searchResult!=null) {
			double p = (Integer)value/(double)searchResult.getTotalMatchCount() * 100d;
			value = p<minPercentage ? 0 : String.format("%1.2f%%", p); //$NON-NLS-1$
			
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

	/**
	 * @return the minPercentage
	 */
	public static double getMinPercentage() {
		return minPercentage;
	}

	/**
	 * @param minPercentage the minPercentage to set
	 */
	public static void setMinPercentage(double value) {
		minPercentage = value;
	}

	/**
	 * @return the highlightPercentage
	 */
	public static double getHighlightPercentage() {
		return highlightPercentage;
	}

	/**
	 * @param highlightPercentage the highlightPercentage to set
	 */
	public static void setHighlightPercentage(double value) {
		highlightPercentage = value;
	}

	/**
	 * @return the highlightColor
	 */
	public static Color getHighlightColor() {
		return highlightColor;
	}

	/**
	 * @param highlightColor the highlightColor to set
	 */
	public static void setHighlightColor(Color value) {
		Exceptions.testNullArgument(value, "value"); //$NON-NLS-1$
		
		highlightColor = value;
	}
}