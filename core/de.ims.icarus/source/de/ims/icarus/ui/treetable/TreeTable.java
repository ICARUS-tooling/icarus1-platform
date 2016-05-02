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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreeTable extends JTable {

	private static final long serialVersionUID = 7196942148627696377L;

	private TreeTableCellRenderer tree;

	private Set<Class<?>> useCustomTooltipComponent = new HashSet<>();

	public TreeTable(TreeTableModel treeTableModel) {
		this(treeTableModel, null);
	}

	public TreeTable(TreeTableModel treeTableModel, TableColumnModel columnModel) {
		//super(null, columnModel);

		setAutoCreateColumnsFromModel(false);

		if(columnModel!=null) {
			setColumnModel(columnModel);
		}
		tree = new TreeTableCellRenderer(this, treeTableModel);
		setModel(new TreeTableModelAdapter(treeTableModel, tree));

		if(columnModel==null) {
			setAutoCreateColumnsFromModel(true);
		}

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

	public void setUseCustomTooltipComponent(Class<?> clazz, boolean doUse) {
		if (doUse) {
			useCustomTooltipComponent.add(clazz);
		} else {
			useCustomTooltipComponent.remove(clazz);
		}
	}

	public boolean isUseCustomTooltipComponent(Class<?> clazz) {
		return useCustomTooltipComponent.contains(clazz);
	}

	/**
	 * @see javax.swing.JComponent#createToolTip()
	 */
	@Override
	public JToolTip createToolTip() {

		JToolTip result = null;

		// Only try to get custom component if there are any custom flags at all
		if(!useCustomTooltipComponent.isEmpty()) {
			Point p = getMousePosition();
			if(p!=null) {
				int column = columnAtPoint(p);
				if(column!=-1) {
					Class<?> clazz = getColumnClass(column);
					if(clazz!=null && isUseCustomTooltipComponent(clazz)) {
						int row = rowAtPoint(p);

			            TableCellRenderer renderer = getCellRenderer(row, column);
			            Component component = prepareRenderer(renderer, row, column);

			            if(component instanceof JComponent) {
			            	result = ((JComponent)renderer).createToolTip();
			            	if(result!=null) {
			            		result.setComponent(this);
			            	}
			            }
					}
				}
			}
		}

		if(result==null) {
			result = super.createToolTip();
		}

		return result;
	}

	/**
	 * @see javax.swing.JComponent#getToolTipLocation(java.awt.event.MouseEvent)
	 */
	@Override
	public Point getToolTipLocation(MouseEvent event) {
		Point p = null;

		if(!useCustomTooltipComponent.isEmpty()) {
			Point pos = getMousePosition();
			if(pos!=null) {
				int column = columnAtPoint(pos);
				if(column!=-1) {
					Class<?> clazz = getColumnClass(column);
					if(clazz!=null && isUseCustomTooltipComponent(clazz)) {
						int row = rowAtPoint(pos);

			            Rectangle bounds = getCellRect(row, column, true);

			            p = new Point(bounds.x, bounds.y+bounds.height);
					}
				}
			}
		}

		if(p==null) {
			p = super.getToolTipLocation(event);
		}

		return p;
	}

	public TreeTableCellRenderer getTreeTableCellRenderer() {
		return tree;
	}

	public TreeTableSelectionModel getTreeSelectionModel() {
		return (TreeTableSelectionModel) tree.getSelectionModel();
	}
}
