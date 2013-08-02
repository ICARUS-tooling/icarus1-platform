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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.event.MouseInputListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import com.sun.java.swing.plaf.windows.WindowsTableHeaderUI;

import de.ims.icarus.ui.UIUtil;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TableRowHeaderRenderer extends JTableHeader implements
		ListCellRenderer<String>, MouseInputListener, PropertyChangeListener {

	private static final long serialVersionUID = -3612996232153942208L;

	protected TableColumn proxyColumn;

	protected final JList<String> list;

	protected int verticalOffset;

	protected int rolloverRow = -1;

	protected boolean alwaysContains = false;

	public TableRowHeaderRenderer(JList<String> list, JTable table) {
		if(list==null)
			throw new IllegalArgumentException("Invalid list"); //$NON-NLS-1$

		proxyColumn = new TableColumn(0);

		verticalOffset = getOffsetForUI();

		getColumnModel().addColumn(proxyColumn);
		this.list = list;
		list.addMouseMotionListener(this);
		list.addMouseListener(this);
		
		UIUtil.disableHtml(this);

		setTable(table);
	}

	@Override
	public void setTable(JTable table) {
		if (getTable() != null) {
			getTable().removePropertyChangeListener(this);
		}

		super.setTable(table);

		if (table != null) {
			table.addPropertyChangeListener("tableHeader", this); //$NON-NLS-1$
			copyStyle(table.getTableHeader());
		}
	}

	protected void copyStyle(JTableHeader source) {
		if (source != null) {
			// setBackground(source.getBackground());
		}
	}

	protected int getOffsetForUI() {
		if (getUI() instanceof WindowsTableHeaderUI) {
			return UIUtil.isWindowsClassicLAF() ? 1 : 2;
		}

		// TODO test with more LaF implementations?

		return 0;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof JTable)
			copyStyle(((JTable) evt.getSource()).getTableHeader());
	}

	protected void updateRolloverRow(MouseEvent e) {
		int row = list.locationToIndex(e.getPoint());

		Rectangle oldBounds = rolloverRow == -1 ? null : list.getCellBounds(
				rolloverRow, rolloverRow);

		Rectangle newBounds = row == -1 ? null : list.getCellBounds(row, row);
		MouseListener[] listeners = getMouseListeners();

		if (rolloverRow != -1) {
			/*
			 * force cleanup since we always are in the same "column" -> let UI
			 * free state for ols cell
			 */
			for (int i = listeners.length - 1; i >= 0; i--) {
				listeners[i].mouseExited(e);
			}
		}

		// repaint old cell
		if (oldBounds != null && rolloverRow != row) {
			list.paintImmediately(oldBounds);
		}

		if (row != -1) {
			/*
			 * force the component to always contain every point for the
			 * duration of dispatching
			 */
			alwaysContains = true;
			e = new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiers(),
					e.getX(), e.getY() - newBounds.y, e.getClickCount(), e
							.isPopupTrigger());

			// trigger mouseEntered event for proper setup
			for (int i = listeners.length - 1; i >= 0; i--) {
				listeners[i].mouseEntered(e);
			}

			rolloverRow = row;
			alwaysContains = false;
		}

		if (newBounds != null) {
			list.paintImmediately(newBounds);
		} else {
			rolloverRow = -1;
		}

		/*
		 * TableHeaderUI may store the current rollover state, so scrolling
		 * might corrupt proper painting. The current workaround is to clear the
		 * UI's rollover state immediately after painting.
		 */
		for (int i = listeners.length - 1; i >= 0; i--) {
			listeners[i].mouseExited(e);
		}
	}

	@Override
	public boolean contains(int x, int y) {
		return alwaysContains ? true : super.contains(x, y);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends String> list,
			String value, int index, boolean selected, boolean hasFocus) {

		proxyColumn.setHeaderValue(value);
		proxyColumn.setWidth(list.getWidth());

		// setSize(list.getWidth(), list.getFixedCellHeight());

		return this;
	}

	@Override
	public void updateUI() {
		super.updateUI();

		verticalOffset = getOffsetForUI();
	}

	@Override
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h + verticalOffset);
	}

	@Override
	public boolean isOpaque() {
		return true;
	}

	@Override
	public void validate() {
		// no-op
	}

	@Override
	public void invalidate() {
		// no-op
	}

	@Override
	public void revalidate() {
		// no-op
	}

	@Override
	public void repaint() {
		// no-op
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
		// no-op
	}

	@Override
	public void repaint(Rectangle r) {
		// no-op
	}

	@Override
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		super.firePropertyChange(propertyName, oldValue, newValue);
	}

	@Override
	public void firePropertyChange(String propertyName, byte oldValue,
			byte newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, int oldValue,
			int newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, short oldValue,
			short newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, long oldValue,
			long newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, float oldValue,
			float newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, double oldValue,
			double newValue) {
		// no-op
	}

	@Override
	public void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) {
		// no-op
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// no-op
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		updateRolloverRow(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// no-op
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		updateRolloverRow(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (rolloverRow != -1) {
			// clean up any rollover states
			MouseListener[] listeners = getMouseListeners();
			for (int i = listeners.length - 1; i >= 0; i--) {
				listeners[i].mouseExited(e);
			}

			list.repaint(list.getCellBounds(rolloverRow, rolloverRow));
		}
		rolloverRow = -1;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// no-op
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// no-op
	}
}