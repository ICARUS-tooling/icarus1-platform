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
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 * 
 */
public class TreeTableCellRenderer extends JTree implements TableCellRenderer {

	private static final long serialVersionUID = -1750976014189553400L;

	/** Die letzte Zeile, die gerendert wurde. */
	protected int visibleRow;

	private TreeTable treeTable;

	public TreeTableCellRenderer(TreeTable treeTable, TreeModel model) {
		super(model);
		this.treeTable = treeTable;

		// Setzen der Zeilenhoehe fuer die JTable
		// Muss explizit aufgerufen werden, weil treeTable noch
		// null ist, wenn super(model) setRowHeight aufruft!
		setRowHeight(getRowHeight());
	}

	/**
	 * Tree und Table muessen die gleiche Hoehe haben.
	 */
	@Override
	public void setRowHeight(int rowHeight) {
		if (rowHeight > 0) {
			super.setRowHeight(rowHeight);
			if (treeTable != null && treeTable.getRowHeight() != rowHeight) {
				treeTable.setRowHeight(getRowHeight());
			}
		}
	}

	/**
	 * Tree muss die gleiche Hoehe haben wie Table.
	 */
	@Override
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, 0, w, treeTable.getHeight());
	}

	/**
	 * Sorgt fuer die Einrueckung der Ordner.
	 */
	public void paint(Graphics g) {
		g.translate(0, -visibleRow * getRowHeight());

		super.paint(g);
	}

	/**
	 * Liefert den Renderer mit der passenden Hintergrundfarbe zurueck.
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (isSelected)
			setBackground(table.getSelectionBackground());
		else
			setBackground(table.getBackground());

		visibleRow = row;
		return this;
	}
}
