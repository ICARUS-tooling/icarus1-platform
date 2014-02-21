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
import de.ims.icarus.util.strings.StringUtil;

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
	    setHorizontalAlignment(SwingConstants.LEFT);
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
		
		int width = table.getColumnModel().getColumn(column).getWidth()-5;
		width = Math.max(width, StringUtil.MIN_WRAP_WIDTH);
		String[] lines = StringUtil.split(str, (Component) getCellRenderer(), width);
		
		//System.out.println("------------ "+width+"\n"+Arrays.toString(lines));
		
		MultilineListModel model = getModel();
		model.setLines(lines);
		
		setToolTipText(UIUtil.toSwingTooltip(str));
		
		return this;
	}

	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class MultilineListModel extends AbstractListModel<String> {

		private static final long serialVersionUID = 2182638905258539653L;
		
		private List<String> lines = new ArrayList<>();
		
		private int maxLineCount = 3;
		private int minLineCount = 0;
		
		public void clear() {
			lines.clear();
		}
		
		public void setLines(String...lines) {
			clear();
			
			for(int i=0; i<lines.length; i++) {
				addLine(lines[i]);
				
				// Check break condition AFTER adding the last line!
				// This way a header using up 3 lines will be shown properly
				// while only a header using more than 3 lines will get
				// its third line changed to reflect that
				if(i>=maxLineCount) {
					break;
				}
			}
			
			for(int i=getSize(); i<minLineCount; i++) {
//				System.out.println("adding dummy line");
				addLine(" "); //$NON-NLS-1$
			}
			
			fireContentChange();
		}
		
		public void addLine(String line) {
			if(lines.size()<maxLineCount) {
				lines.add(line);
			} else {
				lines.set(maxLineCount-1, StringUtil.TEXT_WILDCARD);
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

		/**
		 * @return the minLineCount
		 */
		public int getMinLineCount() {
			return minLineCount;
		}

		/**
		 * @param minLineCount the minLineCount to set
		 */
		public void setMinLineCount(int minLineCount) {
			this.minLineCount = minLineCount;
		}
		
	}
}
