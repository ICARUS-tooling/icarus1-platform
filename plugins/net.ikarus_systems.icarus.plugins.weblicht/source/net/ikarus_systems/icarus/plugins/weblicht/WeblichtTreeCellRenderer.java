package net.ikarus_systems.icarus.plugins.weblicht;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.ikarus_systems.icarus.plugins.weblicht.webservice.Webchain;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebchainInputType;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebchainOutputType;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebchainRegistry;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.Webservice;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.CompoundIcon;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.util.Wrapper;

public class WeblichtTreeCellRenderer extends DefaultTreeCellRenderer{


	private static final long serialVersionUID = -2589454462089491253L;
	private static CompoundIcon outputIcon;
	private static CompoundIcon webchainIcon;

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
		
		if (value instanceof Wrapper) {
			value = ((Wrapper<?>)value).get();
		}
		
		Icon icon = null;
		if(value instanceof Webchain) {
			Webchain chain = (Webchain)value;
			value = chain.getName();
			
			if (webchainIcon == null){
				webchainIcon = new CompoundIcon(IconRegistry.getGlobalRegistry().getIcon("link_obj_dark.gif")); //$NON-NLS-1$
			}
			if (WebchainRegistry.getInstance().hasChainOutput(chain)) {
				webchainIcon.setBottomLeftOverlay(null);
			} else {
				webchainIcon.setBottomLeftOverlay(IconRegistry.getGlobalRegistry().getIcon("warning_co.gif")); //$NON-NLS-1$
			}
			icon = webchainIcon;
			
		} else if(value instanceof Webservice) {
			value = ((Webservice)value).getName();			
			icon = IconRegistry.getGlobalRegistry().getIcon("repository_rep.gif"); //$NON-NLS-1$
		
		} else if(value instanceof WebchainOutputType) {
			WebchainOutputType wo = (WebchainOutputType)value;
			value = ResourceManager.getInstance().get("output") //$NON-NLS-1$
					+ wo.getOutputType();			
			if (outputIcon == null){
				outputIcon = new CompoundIcon(IconRegistry.getGlobalRegistry().getIcon("outrepo_rep.gif")); //$NON-NLS-1$
			}
			if (wo.getIsOutputUsed()) {
				outputIcon.setBottomLeftOverlay(IconRegistry.getGlobalRegistry().getIcon("task_complete")); //$NON-NLS-1$
			} else {
				outputIcon.setBottomLeftOverlay(IconRegistry.getGlobalRegistry().getIcon("unconfigured_co.gif")); //$NON-NLS-1$
			}
			icon = outputIcon;
			
		} else if(value instanceof WebchainInputType) {
			value = ResourceManager.getInstance().get("input") //$NON-NLS-1$
					+ ((WebchainInputType)value).getInputType();
			icon = IconRegistry.getGlobalRegistry().getIcon("addrepo_rep.gif"); //$NON-NLS-1$
		}
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		
		setIcon(icon);
		
		
		return this;
	}

}
