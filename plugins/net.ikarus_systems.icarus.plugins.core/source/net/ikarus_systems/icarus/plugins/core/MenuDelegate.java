/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core;

import javax.swing.JMenuBar;

import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class MenuDelegate {
	
	private ActionManager actionManager;
	private final String menuId;
	
	private Object fileMenuItems;
	private Object windowMenuItems;
	private Object helpMenuItems;
	private Object customMenus;
	private Object perspectives;

	MenuDelegate(String menuId) {
		if(menuId==null)
			throw new IllegalArgumentException("Invalid menuId"); //$NON-NLS-1$
		
		this.menuId = menuId;
	}

	void setActionManager(ActionManager actionManager) {
		this.actionManager = actionManager;
	}
	
	void clear() {
		fileMenuItems = null;
		windowMenuItems = null;
		helpMenuItems = null;
		customMenus = null;
		perspectives = null;
	}
	
	JMenuBar createMenuBar() {
		ActionManager actionManager = this.actionManager;
		if(actionManager==null) {
			actionManager = ActionManager.globalManager();
		}
		
		Options options = new Options();
		options.put("fileMenuItems", fileMenuItems); //$NON-NLS-1$
		options.put("windowMenuItems", windowMenuItems); //$NON-NLS-1$
		options.put("helpMenuItems", helpMenuItems); //$NON-NLS-1$
		options.put("customMenus", customMenus); //$NON-NLS-1$
		options.put("perspectives", perspectives); //$NON-NLS-1$
		
		return actionManager.createMenuBar(menuId, options);
	}

	public void setFileMenuItems(Object fileMenuItems) {
		this.fileMenuItems = fileMenuItems;
	}

	public void setWindowMenuItems(Object windowMenuItems) {
		this.windowMenuItems = windowMenuItems;
	}

	public void setHelpMenuItems(Object helpMenuItems) {
		this.helpMenuItems = helpMenuItems;
	}

	public void setCustomMenus(Object customMenus) {
		this.customMenus = customMenus;
	}

	void setPerspectives(Object perspectives) {
		this.perspectives = perspectives;
	}
}
