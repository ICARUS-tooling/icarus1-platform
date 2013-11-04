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

import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface TreeTableModel extends TreeModel {
	
	/**
     * Returns the number of available columns.
     * @return Number of Columns
     */
    public int getColumnCount();
 
    /**
     * Returns the column name.
     * @param column Column number
     * @return Column name
     */
    public String getColumnName(int column);
 
 
    /**
     * Returns the type (class) of a column.
     * @param column Column number
     * @return Class
     */
    public Class<?> getColumnClass(int column);
 
    /**
     * Returns the value of a node in a column.
     * @param node Node
     * @param column Column number
     * @return Value of the node in the column
     */
    public Object getValueAt(Object node, int column);
 
 
    /**
     * Check if a cell of a node in one column is editable.
     * @param node Node
     * @param column Column number
     * @return true/false
     */
    public boolean isCellEditable(Object node, int column);
 
    /**
     * Sets a value for a node in one column.
     * @param aValue New value
     * @param node Node
     * @param column Column number
     */
    public void setValueAt(Object aValue, Object node, int column);

	/**
	 * Used to signal structural changes in the table part of the model
	 * @param listener
	 */
	void addChangeListener(ChangeListener listener);

	/**
	 * @param listener
	 */
	void removeChangeListener(ChangeListener listener);
}
