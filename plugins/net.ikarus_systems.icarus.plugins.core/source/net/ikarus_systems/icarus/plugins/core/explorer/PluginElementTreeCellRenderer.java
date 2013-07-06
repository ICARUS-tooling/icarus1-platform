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

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.ikarus_systems.icarus.ui.LabelProxy;
import net.ikarus_systems.icarus.ui.UIUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PluginElementTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 2579251699095771899L;

	/**
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);		
		
		if(value instanceof DefaultMutableTreeNode) {
			value = ((DefaultMutableTreeNode)value).getUserObject();
		}
		
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
