/*
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
import java.util.StringTokenizer;

import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
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
		str = StringUtil.wrap(str, (Component) getCellRenderer(), width);
		StringTokenizer tokenizer = new StringTokenizer(str, "\n"); //$NON-NLS-1$
		
		MultilineListModel model = getModel();
		model.clear();
		while(tokenizer.hasMoreTokens()) {
			model.addLine(tokenizer.nextToken());
		}
		//model.fireContentChange();
		
		setToolTipText(str);
		
		return this;
	}

	public static class MultilineListModel extends AbstractListModel<String> {

		private static final long serialVersionUID = 2182638905258539653L;
		
		private List<String> lines = new ArrayList<>();
		
		public void clear() {
			lines.clear();
		}
		
		public void setLines(String...lines) {
			clear();
			CollectionUtils.feedItems(this.lines, lines);
			
			fireContentChange();
		}
		
		public void addLine(String line) {
			lines.add(line);
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
		
	}
}
