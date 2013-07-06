/*
 * $Revision: 33 $
 * $Date: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.core/source/net/ikarus_systems/icarus/plugins/core/explorer/PluginElementTreeCellRenderer.java $
 *
 * $LastChangedDate: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $ 
 * $LastChangedRevision: 33 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.core.explorer;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.ims.icarus.ui.LabelProxy;
import de.ims.icarus.ui.UIUtil;


/**
 * @author Markus Gärtner
 * @version $Id: PluginElementTreeCellRenderer.java 33 2013-05-13 12:33:31Z mcgaerty $
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
