/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.treebank.swing;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.ikarus_systems.icarus.language.treebank.Treebank;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.UIUtil;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 466713405139267604L;

	public TreebankTreeCellRenderer() {
		setLeafIcon(null);
		setClosedIcon(null);
		setOpenIcon(null);
		UIUtil.disableHtml(this);
	}

	/**
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {	
		
		/*if(value instanceof DefaultMutableTreeNode) {
			value = ((DefaultMutableTreeNode)value).getUserObject();
		}*/
		
		Icon icon = null;
		
		if(value instanceof Treebank) {
			value = ((Treebank)value).getName();
		} else if(value instanceof Extension) {
			value = ((Extension)value).getId();
			icon = IconRegistry.getGlobalRegistry().getIcon("class_obj.gif"); //$NON-NLS-1$
		}
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		
		setIcon(icon);
		
		return this;
	}
}
