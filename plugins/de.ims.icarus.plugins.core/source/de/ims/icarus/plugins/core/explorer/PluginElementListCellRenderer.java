/*
 * $Revision: 33 $
 * $Date: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.core/source/net/ikarus_systems/icarus/plugins/core/explorer/PluginElementListCellRenderer.java $
 *
 * $LastChangedDate: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $ 
 * $LastChangedRevision: 33 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.core.explorer;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.tree.DefaultMutableTreeNode;

import de.ims.icarus.ui.LabelProxy;
import de.ims.icarus.ui.UIUtil;


/**
 * @author Markus Gärtner
 * @version $Id: PluginElementListCellRenderer.java 33 2013-05-13 12:33:31Z mcgaerty $
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
