/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.treebank.swing;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;


import org.java.plugin.registry.Extension;

import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.id.Identity;
import de.ims.icarus.util.location.Locations;

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
		String tooltip = null;
		
		if(value instanceof Treebank) {
			Treebank treebank = (Treebank) value;
			value = treebank.getName();
			
			StringBuilder sb = new StringBuilder(200);
			sb.append(ResourceManager.getInstance().get("plugins.languageTools.labels.name")) //$NON-NLS-1$
				.append(": ") //$NON-NLS-1$
				.append(treebank.getName())
				.append("\n"); //$NON-NLS-1$
			String path = Locations.getPath(treebank.getLocation());
			sb.append(ResourceManager.getInstance().get("plugins.languageTools.labels.location")) //$NON-NLS-1$
			.append(": ") //$NON-NLS-1$
			.append(path==null ? "?" : path); //$NON-NLS-1$
			
			tooltip = UIUtil.toSwingTooltip(sb.toString());
		} else if(value instanceof Extension) {
			Identity identity = PluginUtil.getIdentity((Extension)value);
			value = identity.getName();
			icon = identity.getIcon();
			if(icon==null) {
				icon = IconRegistry.getGlobalRegistry().getIcon("treebank_co.png"); //$NON-NLS-1$
			}
		}
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		
		setIcon(icon);
		setToolTipText(tooltip);
		
		return this;
	}
}
