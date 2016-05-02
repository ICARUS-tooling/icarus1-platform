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

import java.awt.Color;
import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import de.ims.icarus.ui.helper.AbstractRendererPanel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractTableCellRendererPanel extends AbstractRendererPanel implements TableCellRenderer {

	private static final long serialVersionUID = -2947740793791812040L;

	public AbstractTableCellRendererPanel() {
		super();
	}

	public AbstractTableCellRendererPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public AbstractTableCellRendererPanel(LayoutManager layout,
			boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public AbstractTableCellRendererPanel(LayoutManager layout) {
		super(layout);
	}

	/**
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
        if (table == null) {
            return this;
        }

		isSelected = prepareColor(table, value, isSelected, hasFocus, row, column);

        prepareBorder(table, value, isSelected, hasFocus, row, column);

        setEnabled(table.isEnabled());

        prepareRenderer(table, value, isSelected, hasFocus, row, column);

//        System.out.printf("refreshing: value=%s bg=%s fg=%s\n", value, getBackground(), getForeground());

		return this;
	}

	protected boolean prepareColor(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {


        Color fg = null;
        Color bg = null;

        JTable.DropLocation dropLocation = table.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsertRow()
                && !dropLocation.isInsertColumn()
                && dropLocation.getRow() == row
                && dropLocation.getColumn() == column) {

            fg = UIManager.getColor("Table.dropCellForeground"); //$NON-NLS-1$
            bg = UIManager.getColor("Table.dropCellBackground"); //$NON-NLS-1$

            isSelected = true;
        }

        if (isSelected) {
            fg = table.getSelectionForeground();
            bg = table.getSelectionBackground();

        } else {
        	fg = table.getForeground();
        	bg = table.getBackground();

            if (table.isCellEditable(row, column)) {
                fg = UIManager.getColor("Table.focusCellForeground"); //$NON-NLS-1$
                bg = UIManager.getColor("Table.focusCellBackground"); //$NON-NLS-1$
            } else if (bg == null || bg instanceof javax.swing.plaf.UIResource) {
                Color alternateColor = UIManager.getColor("Table.alternateRowColor"); //$NON-NLS-1$
                if (alternateColor != null && row % 2 != 0) {
                    bg = alternateColor;
                }
            }
        }


        setBackground(bg);
        setForeground(fg);

       return isSelected;
	}

	protected void prepareBorder(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

        Border border = noFocusBorder;

        if (hasFocus) {
            if (isSelected) {
                border = UIManager.getBorder("Table.focusSelectedCellHighlightBorder"); //$NON-NLS-1$
            }
            if (border == null) {
                border = UIManager.getBorder("Table.focusCellHighlightBorder"); //$NON-NLS-1$
            }
        }

        setBorder(border);
	}


	protected abstract void prepareRenderer(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column);

}
