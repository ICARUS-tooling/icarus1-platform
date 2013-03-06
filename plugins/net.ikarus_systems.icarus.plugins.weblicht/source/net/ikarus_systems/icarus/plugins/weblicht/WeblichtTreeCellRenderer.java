package net.ikarus_systems.icarus.plugins.weblicht;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.ikarus_systems.icarus.ui.IconRegistry;

import org.java.plugin.registry.Extension;

public class WeblichtTreeCellRenderer extends DefaultTreeCellRenderer{


	private static final long serialVersionUID = -2589454462089491253L;

	public WeblichtTreeCellRenderer() {
		setLeafIcon(null);
		setClosedIcon(null);
		setOpenIcon(null);
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
		
		if(value instanceof Webservice) {
			value = ((Webservice)value).getName();
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
