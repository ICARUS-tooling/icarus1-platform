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
package de.ims.icarus.ui.treetable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import de.ims.icarus.ui.tree.AbstractTreeModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractTreeTableModel extends AbstractTreeModel implements TreeTableModel {

	public AbstractTreeTableModel() {
		super();
	}

	public AbstractTreeTableModel(Object root) {
		super(root);
	}

	/**
	 * @see de.ims.icarus.ui.treetable.TreeTableModel#isCellEditable(java.lang.Object, int)
	 */
	@Override
	public boolean isCellEditable(Object node, int column) {
		// Important to activate TreeExpandListener
		return column==0;
	}

	@Override
	public void setValueAt(Object aValue, Object node, int column) {
		throw new UnsupportedOperationException("Model is immutable!"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.ui.treetable.TreeTableModel#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener listener) {
		if (listener == null)
			throw new NullPointerException("Invalid listener"); //$NON-NLS-1$

		if (listeners == null) {
			listeners = new EventListenerList();
		}
        listeners.add(ChangeListener.class, listener);
	}

	/**
	 * @see de.ims.icarus.ui.treetable.TreeTableModel#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener listener) {
		if (listener == null)
			throw new NullPointerException("Invalid listener"); //$NON-NLS-1$

		if (listeners == null) {
			return;
		}
        listeners.remove(ChangeListener.class, listener);
	}
	
	protected void fireTableStructureChanged() {
		if(listeners==null) {
			return;
		}

		Object[] pairs = listeners.getListenerList();

		ChangeEvent event = null;

		for (int i = pairs.length - 2; i >= 0; i -= 2) {
			if (pairs[i] == ChangeListener.class) {
				if (event == null) {
					event = new ChangeEvent(this);
				}

				((ChangeListener) pairs[i + 1]).stateChanged(event);
			}
		}
	}
}
