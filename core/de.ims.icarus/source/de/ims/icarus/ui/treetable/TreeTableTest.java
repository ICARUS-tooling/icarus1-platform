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

import java.awt.Container;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 * 
 */
public class TreeTableTest extends JFrame {

	private static final long serialVersionUID = -5874943290800612022L;

	public TreeTableTest() {
		super("Tree Table Demo"); //$NON-NLS-1$

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setLayout(new GridLayout(0, 1));

		AbstractTreeTableModel treeTableModel = new DataModel(
				createDataStructure());

		TreeTable TreeTable = new TreeTable(treeTableModel);

		Container cPane = getContentPane();

		cPane.add(new JScrollPane(TreeTable));

		setSize(1000, 800);
		setLocationRelativeTo(null);

	}

	private static DataNode createDataStructure() {
		List<DataNode> children1 = new ArrayList<DataNode>();
		children1.add(new DataNode("N12", "C12", new Date(), Integer //$NON-NLS-1$ //$NON-NLS-2$
				.valueOf(50), null));
		children1.add(new DataNode("N13", "C13", new Date(), Integer //$NON-NLS-1$ //$NON-NLS-2$
				.valueOf(60), null));
		children1.add(new DataNode("N14", "C14", new Date(), Integer //$NON-NLS-1$ //$NON-NLS-2$
				.valueOf(70), null));
		children1.add(new DataNode("N15", "C15", new Date(), Integer //$NON-NLS-1$ //$NON-NLS-2$
				.valueOf(80), null));

		List<DataNode> children2 = new ArrayList<DataNode>();
		children2.add(new DataNode("N12", "C12", new Date(), Integer //$NON-NLS-1$ //$NON-NLS-2$
				.valueOf(10), null));
		children2.add(new DataNode("N13", "C13", new Date(), Integer //$NON-NLS-1$ //$NON-NLS-2$
				.valueOf(20), children1));
		children2.add(new DataNode("N14", "C14", new Date(), Integer //$NON-NLS-1$ //$NON-NLS-2$
				.valueOf(30), null));
		children2.add(new DataNode("N15", "C15", new Date(), Integer //$NON-NLS-1$ //$NON-NLS-2$
				.valueOf(40), null));

		List<DataNode> rootNodes = new ArrayList<DataNode>();
		rootNodes.add(new DataNode("N1", "C1", new Date(), Integer.valueOf(10), //$NON-NLS-1$ //$NON-NLS-2$
				children2));
		rootNodes.add(new DataNode("N2", "C2", new Date(), Integer.valueOf(10), //$NON-NLS-1$ //$NON-NLS-2$
				children1));
		rootNodes.add(new DataNode("N3", "C3", new Date(), Integer.valueOf(10), //$NON-NLS-1$ //$NON-NLS-2$
				children2));
		rootNodes.add(new DataNode("N4", "C4", new Date(), Integer.valueOf(10), //$NON-NLS-1$ //$NON-NLS-2$
				children1));
		rootNodes.add(new DataNode("N5", "C5", new Date(), Integer.valueOf(10), //$NON-NLS-1$ //$NON-NLS-2$
				children1));
		rootNodes.add(new DataNode("N6", "C6", new Date(), Integer.valueOf(10), //$NON-NLS-1$ //$NON-NLS-2$
				children1));
		rootNodes.add(new DataNode("N7", "C7", new Date(), Integer.valueOf(10), //$NON-NLS-1$ //$NON-NLS-2$
				children1));
		rootNodes.add(new DataNode("N8", "C8", new Date(), Integer.valueOf(10), //$NON-NLS-1$ //$NON-NLS-2$
				children1));
		rootNodes.add(new DataNode("N9", "C9", new Date(), Integer.valueOf(10), //$NON-NLS-1$ //$NON-NLS-2$
				children1));
		rootNodes.add(new DataNode("N10", "C10", new Date(), Integer //$NON-NLS-1$ //$NON-NLS-2$
				.valueOf(10), children1));
		rootNodes.add(new DataNode("N11", "C11", new Date(), Integer //$NON-NLS-1$ //$NON-NLS-2$
				.valueOf(10), children1));
		rootNodes.add(new DataNode("N12", "C7", new Date(), //$NON-NLS-1$ //$NON-NLS-2$
				Integer.valueOf(10), children1));
		rootNodes.add(new DataNode("N13", "C8", new Date(), //$NON-NLS-1$ //$NON-NLS-2$
				Integer.valueOf(10), children1));
		rootNodes.add(new DataNode("N14", "C9", new Date(), //$NON-NLS-1$ //$NON-NLS-2$
				Integer.valueOf(10), children1));
		rootNodes.add(new DataNode("N15", "C10", new Date(), Integer //$NON-NLS-1$ //$NON-NLS-2$
				.valueOf(10), children1));
		rootNodes.add(new DataNode("N16", "C11", new Date(), Integer //$NON-NLS-1$ //$NON-NLS-2$
				.valueOf(10), children1));
		DataNode root = new DataNode("R1", "R1", new Date(), //$NON-NLS-1$ //$NON-NLS-2$
				Integer.valueOf(10), rootNodes);

		return root;
	}

	public static void main(final String[] args) {
		Runnable gui = new Runnable() {

			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				new TreeTableTest().setVisible(true);
			}
		};
		SwingUtilities.invokeLater(gui);
	}

	private static class DataModel extends AbstractTreeTableModel {
		// Spalten Name.
		static protected String[] columnNames = { "Knotentext", "String", //$NON-NLS-1$ //$NON-NLS-2$
				"Datum", "Integer" }; //$NON-NLS-1$ //$NON-NLS-2$

		// Spalten Typen.
		static protected Class<?>[] columnTypes = { TreeTableModel.class,
				String.class, Date.class, Integer.class };

		public DataModel(DataNode rootNode) {
			super(rootNode);
		}

		public Object getChild(Object parent, int index) {
			return ((DataNode) parent).getChildren().get(index);
		}

		public int getChildCount(Object parent) {
			return ((DataNode) parent).getChildren().size();
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public String getColumnName(int column) {
			return columnNames[column];
		}

		public Class<?> getColumnClass(int column) {
			return columnTypes[column];
		}

		public Object getValueAt(Object node, int column) {
			switch (column) {
			case 0:
				return ((DataNode) node).getName();
			case 1:
				return ((DataNode) node).getCapital();
			case 2:
				return ((DataNode) node).getDeclared();
			case 3:
				return ((DataNode) node).getArea();
			default:
				break;
			}
			return null;
		}

		public void setValueAt(Object aValue, Object node, int column) {
			// no-op
		}
	}

	private static class DataNode {
		private String name;
		private String capital;
		private Date declared;
		private Integer area;

		private List<DataNode> children;

		public DataNode(String name, String capital, Date declared,
				Integer area, List<DataNode> children) {
			this.name = name;
			this.capital = capital;
			this.declared = declared;
			this.area = area;
			this.children = children;

			if (this.children == null) {
				this.children = Collections.emptyList();
			}
		}

		public String getName() {
			return name;
		}

		public String getCapital() {
			return capital;
		}

		public Date getDeclared() {
			return declared;
		}

		public Integer getArea() {
			return area;
		}

		public List<DataNode> getChildren() {
			return children;
		}

		/**
		 * Knotentext vom JTree.
		 */
		public String toString() {
			return name;
		}
	}
}
