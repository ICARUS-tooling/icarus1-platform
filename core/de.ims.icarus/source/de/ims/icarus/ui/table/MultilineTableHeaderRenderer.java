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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MultilineTableHeaderRenderer extends JList<String> implements
		TableCellRenderer {

	private static final long serialVersionUID = 6662546252664182405L;
	
	public MultilineTableHeaderRenderer() {
		super(new MultilineListModel());
		
		init();
	}
	
	protected void init() {
	    setOpaque(true);
	    setForeground(UIManager.getColor("TableHeader.foreground")); //$NON-NLS-1$
	    setBackground(UIManager.getColor("TableHeader.background")); //$NON-NLS-1$
	    setBorder(UIManager.getBorder("TableHeader.cellBorder")); //$NON-NLS-1$
	    
	    JComponent renderer = (JComponent) getCellRenderer();
	    UIUtil.disableHtml(renderer);
	    setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	public void setHorizontalAlignment(int alignment) {
	    ((JLabel) getCellRenderer()).setHorizontalAlignment(alignment);
	    
	    repaint();
	}
	
	public int getHorizontalAlignment() {
	    return ((JLabel)getCellRenderer()).getHorizontalAlignment();
	}

	@Override
	public MultilineListModel getModel() {
		return (MultilineListModel) super.getModel();
	}

	/**
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		String str = (value == null) ? "" : value.toString(); //$NON-NLS-1$
		
		int width = table.getColumnModel().getColumn(column).getWidth();
		width = Math.max(width, StringUtil.MIN_WRAP_WIDTH);
		String[] lines = StringUtil.split(str, (Component) getCellRenderer(), width);
		
		System.out.println("----------\n"+Arrays.toString(lines));
		
		MultilineListModel model = getModel();
		model.setLines(lines);
		
		setToolTipText(UIUtil.toSwingTooltip(str));
		
		return this;
	}

	public static class MultilineListModel extends AbstractListModel<String> {

		private static final long serialVersionUID = 2182638905258539653L;
		
		private List<String> lines = new ArrayList<>();
		
		private int maxLineCount = 3;
		
		public void clear() {
			lines.clear();
		}
		
		public void setLines(String...lines) {
			clear();
			
			int max = Math.min(lines.length, maxLineCount);
			for(int i=0; i<max; i++) {
				this.lines.add(lines[i]);
			}
			
			fireContentChange();
		}
		
		public void addLine(String line) {
			if(lines.size()<maxLineCount) {
				lines.add(line);
			}
		}
		
		public void fireContentChange() {
			fireContentsChanged(this, 0, Math.max(0, getSize()-1));
		}
		
		/**
		 * @see javax.swing.ListModel#getSize()
		 */
		@Override
		public int getSize() {
			return lines.size();
		}

		/**
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		@Override
		public String getElementAt(int index) {
			return lines.get(index);
		}

		public int getMaxLineCount() {
			return maxLineCount;
		}

		public void setMaxLineCount(int maxLineCount) {
			this.maxLineCount = maxLineCount;
		}
		
	}
}
