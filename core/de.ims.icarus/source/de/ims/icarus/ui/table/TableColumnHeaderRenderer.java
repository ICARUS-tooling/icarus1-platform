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

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TableColumnHeaderRenderer implements TableCellRenderer {

	private final TableCellRenderer renderer;

	public static void attach(JTableHeader header) {
		if (header == null)
			throw new NullPointerException("Invalid header"); //$NON-NLS-1$

		TableCellRenderer oldRenderer = header.getDefaultRenderer();
		if(oldRenderer==null)
			throw new IllegalArgumentException("No default renderer present"); //$NON-NLS-1$

		TableCellRenderer newRenderer = new TableColumnHeaderRenderer(oldRenderer);
		header.setDefaultRenderer(newRenderer);
	}

	public TableColumnHeaderRenderer(TableCellRenderer renderer) {
		if (renderer == null)
			throw new NullPointerException("Invalid renderer"); //$NON-NLS-1$

		this.renderer = renderer;
	}

	/**
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if(table!=null) {
			isSelected |= table.getColumnModel().getSelectionModel().isSelectedIndex(column);
		}

		return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}

}
