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

import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

/**
 * @author Markus Gärtner
 * @version $Id$
 * 
 */
public class TreeTableModelAdapter extends AbstractTableModel implements TreeModelListener, ChangeListener {

	private static final long serialVersionUID = -7028911094217019439L;

	private JTree tree;
	
	private TreeTableModel treeTableModel;

	public TreeTableModelAdapter(TreeTableModel treeTableModel, JTree tree) {
		this.tree = tree;
		this.treeTableModel = treeTableModel;

		tree.addTreeExpansionListener(new TreeExpansionListener() {
			
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				fireTableDataChanged();
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				fireTableDataChanged();
			}
		});
		
		treeTableModel.addChangeListener(this);
		treeTableModel.addTreeModelListener(this);
	}

	@Override
	public int getColumnCount() {
		return treeTableModel.getColumnCount();
	}

	@Override
	public String getColumnName(int column) {
		return treeTableModel.getColumnName(column);
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return treeTableModel.getColumnClass(column);
	}

	@Override
	public int getRowCount() {
		return tree.getRowCount();
	}

	protected Object nodeForRow(int row) {
		TreePath treePath = tree.getPathForRow(row);
		return treePath.getLastPathComponent();
	}

	@Override
	public Object getValueAt(int row, int column) {
		return treeTableModel.getValueAt(nodeForRow(row), column);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return treeTableModel.isCellEditable(nodeForRow(row), column);
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		treeTableModel.setValueAt(value, nodeForRow(row), column);
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		fireTableStructureChanged();
	}
	
	protected int[] getRowBounds(TreeModelEvent e) {
		if(e.getChildIndices()==null) {
			return null;
		} else {
			TreePath parent = e.getTreePath();
			int index0 = tree.getRowForPath(parent)+1;
			int[] indices = e.getChildIndices();
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			for(int index : indices) {
				min = Math.min(min, index);
				max = Math.max(max, index);
			}
			return new int[]{index0+min, index0+max};
		}
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesChanged(TreeModelEvent e) {
		int[] bounds = getRowBounds(e);
		if(bounds==null) {
			fireTableDataChanged();
		} else {
			fireTableRowsUpdated(bounds[0], bounds[1]);
		}
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesInserted(TreeModelEvent e) {
		int[] bounds = getRowBounds(e);
		if(bounds==null) {
			fireTableDataChanged();
		} else {
			fireTableRowsInserted(bounds[0], bounds[1]);
		}
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesRemoved(TreeModelEvent e) {
		int[] bounds = getRowBounds(e);
		if(bounds==null) {
			fireTableDataChanged();
		} else {
			fireTableRowsDeleted(bounds[0], bounds[1]);
		}
	}

	/**
	 * @see javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeStructureChanged(TreeModelEvent e) {
		fireTableDataChanged();
	}
}
