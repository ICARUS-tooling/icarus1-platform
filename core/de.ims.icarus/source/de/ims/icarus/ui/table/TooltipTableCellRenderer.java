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
package de.ims.icarus.ui.table;

import java.awt.Component;
import java.awt.FontMetrics;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.ims.icarus.util.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TooltipTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 2505308201912712643L;

//	private static TooltipTableCellRenderer sharedInstance;
//
//	/**
//	 * @return the sharedInstance
//	 */
//	public static TooltipTableCellRenderer getSharedInstance() {
//		if(sharedInstance==null) {
//			sharedInstance = new TooltipTableCellRenderer();
//		}
//		return sharedInstance;
//	}

	public TooltipTableCellRenderer() {
		// no-op
	}

	/**
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if(value instanceof Integer) {
			value = StringUtil.formatDecimal((int) value);
		}

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