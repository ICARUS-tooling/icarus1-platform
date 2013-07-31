/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.manager;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.location.Locations;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 3033361195693809708L;

	public CoreferenceTreeCellRenderer() {
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
		
		if(value instanceof DocumentSetDescriptor) {
			DocumentSetDescriptor descriptor = (DocumentSetDescriptor)value;
			value = descriptor.getName();
			
			StringBuilder sb = new StringBuilder(200);
			sb.append(ResourceManager.getInstance().get("plugins.languageTools.labels.name")) //$NON-NLS-1$
				.append(": ") //$NON-NLS-1$
				.append(descriptor.getName())
				.append("\n"); //$NON-NLS-1$
			String path = Locations.getPath(descriptor.getLocation());
			sb.append(ResourceManager.getInstance().get("plugins.languageTools.labels.location")) //$NON-NLS-1$
			.append(": ") //$NON-NLS-1$
			.append(path==null ? "?" : path); //$NON-NLS-1$
			
			tooltip = UIUtil.toSwingTooltip(sb.toString());
			icon = IconRegistry.getGlobalRegistry().getIcon("file_obj.gif"); //$NON-NLS-1$
		} else if(value instanceof AllocationDescriptor) {
			AllocationDescriptor descriptor = (AllocationDescriptor)value;
			value = descriptor.getName();
			
			StringBuilder sb = new StringBuilder(200);
			sb.append(ResourceManager.getInstance().get("plugins.languageTools.labels.name")) //$NON-NLS-1$
				.append(": ") //$NON-NLS-1$
				.append(descriptor.getName())
				.append("\n"); //$NON-NLS-1$
			String path = Locations.getPath(descriptor.getLocation());
			sb.append(ResourceManager.getInstance().get("plugins.languageTools.labels.location")) //$NON-NLS-1$
			.append(": ") //$NON-NLS-1$
			.append(path==null ? "?" : path); //$NON-NLS-1$
			
			tooltip = UIUtil.toSwingTooltip(sb.toString());
		}
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		setIcon(icon);
		setToolTipText(tooltip);
		
		return this;
	}
}
