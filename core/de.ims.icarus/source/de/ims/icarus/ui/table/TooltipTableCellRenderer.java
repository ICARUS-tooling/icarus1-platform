/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.table;

import java.awt.Component;
import java.awt.FontMetrics;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TooltipTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 2505308201912712643L;

	public TooltipTableCellRenderer() {
		// no-op
	}

	/**
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,	row, column);
		
		String tooltip = getText();
		int columnWidth = table.getColumnModel().getColumn(column).getWidth();
		int textWidth = 0;
		
		if(tooltip!=null && !tooltip.isEmpty()) {
			FontMetrics fm = getFontMetrics(getFont());
			textWidth = fm.stringWidth(tooltip);
		}
		
		if(textWidth<=columnWidth) {
			tooltip = null;
		}

		setToolTipText(tooltip);
		
		return this;
	}	
}