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

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ColumnSelectionSynchronizer extends MouseAdapter implements ListSelectionListener, PropertyChangeListener {

	private final JTable table;

	public ColumnSelectionSynchronizer(JTable table) {
		if (table == null)
			throw new NullPointerException("Invalid table"); //$NON-NLS-1$

		this.table = table;

		//table.getColumnModel().getSelectionModel().addListSelectionListener(this);
		table.addMouseListener(this);
		table.getColumnModel().getSelectionModel().addListSelectionListener(this);
		table.getTableHeader().addMouseListener(this);

		table.addPropertyChangeListener("columnModel", this); //$NON-NLS-1$
		table.addPropertyChangeListener("tableHeader", this); //$NON-NLS-1$
	}

	/**
	 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		int column = -1;

		if(e.getSource()==table) {
			column = table.columnAtPoint(e.getPoint());
		} else {
			column = table.getTableHeader().columnAtPoint(e.getPoint());
		}

		ListSelectionModel selectionModel =
				table.getColumnModel().getSelectionModel();

		if(column==-1) {
			selectionModel.clearSelection();
		} else {
			selectionModel.setSelectionInterval(column, column);
		}
	}

	/**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
//		if(e.getValueIsAdjusting()) {
//			return;
//		}

		if(table.getRowCount()==0) {
			return;
		}

		Rectangle dirtyRegion = table.getCellRect(0, e.getFirstIndex(), true);
		dirtyRegion.add(table.getCellRect(table.getRowCount()-1, e.getLastIndex(), true));

		table.repaint(dirtyRegion);
		table.getTableHeader().resizeAndRepaint();
	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if("tableHeader".equals(evt.getPropertyName())) { //$NON-NLS-1$
			JTableHeader oldValue = (JTableHeader) evt.getOldValue();
			if(oldValue!=null) {
				oldValue.removeMouseListener(this);
			}

			JTableHeader newValue = (JTableHeader) evt.getNewValue();
			if(newValue!=null) {
				newValue.addMouseListener(this);
			}
		} else if("columnModel".equals(evt.getPropertyName())) { //$NON-NLS-1$
			TableColumnModel oldValue = (TableColumnModel) evt.getOldValue();
			if(oldValue!=null) {
				oldValue.getSelectionModel().removeListSelectionListener(this);
			}

			TableColumnModel newValue = (TableColumnModel) evt.getNewValue();
			if(newValue!=null) {
				newValue.getSelectionModel().addListSelectionListener(this);
			}
		}
	}
}
