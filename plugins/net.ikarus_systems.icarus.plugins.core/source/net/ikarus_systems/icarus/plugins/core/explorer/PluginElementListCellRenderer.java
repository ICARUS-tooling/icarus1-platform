/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core.explorer;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.tree.DefaultMutableTreeNode;

import net.ikarus_systems.icarus.ui.LabelProxy;
import net.ikarus_systems.icarus.ui.UIUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PluginElementListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 4151574420306526966L;
	
	public PluginElementListCellRenderer() {
		//setBorder(new EmptyBorder(0, 2, 0, 5));
	}

	/**
	 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		
		if(value instanceof DefaultMutableTreeNode)
			value = ((DefaultMutableTreeNode)value).getUserObject();
		
		if(value instanceof PluginElementProxy) {
			PluginElementProxy proxy = (PluginElementProxy)value; 
			setToolTipText(UIUtil.toSwingTooltip(proxy.getElementDescription()));
			setIcon(proxy.getIcon());
		} else if(value instanceof LabelProxy) {
			setIcon(((LabelProxy)value).getIcon());
		}
		
		return this;
	}

}
