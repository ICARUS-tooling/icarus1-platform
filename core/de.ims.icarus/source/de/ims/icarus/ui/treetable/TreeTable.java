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

import java.awt.Dimension;

import javax.swing.JTable;

/**
 * @author Markus Gärtner
 * @version $Id$
 * 
 */
public class TreeTable extends JTable {

	private static final long serialVersionUID = 7196942148627696377L;

	private TreeTableCellRenderer tree;

	public TreeTable(TreeTableModel treeTableModel) {
		tree = new TreeTableCellRenderer(this, treeTableModel);

		setModel(new TreeTableModelAdapter(treeTableModel, tree));

		// Link selections
		TreeTableSelectionModel selectionModel = new TreeTableSelectionModel();
		tree.setSelectionModel(selectionModel);
		setSelectionModel(selectionModel.getListSelectionModel());

		// Use special editor to handle double clicks for expansion
		setDefaultRenderer(TreeTableModel.class, tree);
		setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor(tree, this));

		// For style reasons disable grid and spacing
		setShowGrid(false);
		setIntercellSpacing(new Dimension(0, 0));
	}
}
