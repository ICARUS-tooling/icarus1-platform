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
package de.ims.icarus.ui.list;

import java.awt.Color;
import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;

import de.ims.icarus.ui.helper.AbstractRendererPanel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractListCellRendererPanel<E extends Object> extends AbstractRendererPanel implements ListCellRenderer<E> {

	private static final long serialVersionUID = 1024502321589715499L;

	public AbstractListCellRendererPanel() {
		super();
	}

	public AbstractListCellRendererPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public AbstractListCellRendererPanel(LayoutManager layout,
			boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public AbstractListCellRendererPanel(LayoutManager layout) {
		super(layout);
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends E> list,
			E value, int index, boolean isSelected, boolean cellHasFocus) {

        Color bg = null;
        Color fg = null;

        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsert()
                && dropLocation.getIndex() == index) {

            bg = UIManager.getColor("List.dropCellBackground"); //$NON-NLS-1$
            fg = UIManager.getColor("List.dropCellForeground"); //$NON-NLS-1$

            isSelected = true;
        }

        if (isSelected) {
            bg = (bg == null ? list.getSelectionBackground() : bg);
            fg = (fg == null ? list.getSelectionForeground() : fg);
        } else {
            bg = list.getBackground();
            fg = list.getForeground();
        }

        setBackground(bg);
        setForeground(fg);
        setEnabled(list.isEnabled());

		prepareRenderer(list, value, index, isSelected, cellHasFocus);
		
		prepareBorder(cellHasFocus, isSelected);
		
		return this;
	}
	
	protected void prepareBorder(boolean cellHasFocus, boolean isSelected) {
        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder"); //$NON-NLS-1$
            }
            if (border == null) {
                border = UIManager.getBorder("List.focusCellHighlightBorder"); //$NON-NLS-1$
            }
        } else {
            border = noFocusBorder;
        }
        setBorder(border);
	}
	
	protected abstract void prepareRenderer(JList<? extends E> list,
			E value, int index, boolean isSelected, boolean cellHasFocus);
}
