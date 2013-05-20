/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.helper;

import java.awt.Component;
import java.awt.FontMetrics;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TooltipListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 4283938142282983275L;

	public TooltipListCellRenderer() {
		// no-op
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {

		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		String tooltip = getText();
		int columnWidth = list.getWidth();
		int textWidth = 0;
		
		if(tooltip!=null && !tooltip.isEmpty()) {
			FontMetrics fm = getFontMetrics(getFont());
			textWidth = fm.stringWidth(tooltip);
		}
		
		if(textWidth<=columnWidth) {
			tooltip = null;
		}

		setToolTipText(tooltip);
		
		return this;
	}

}
