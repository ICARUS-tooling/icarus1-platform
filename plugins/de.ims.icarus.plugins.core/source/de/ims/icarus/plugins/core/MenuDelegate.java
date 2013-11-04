/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.core;

import javax.swing.JMenuBar;

import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.util.Options;


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
			throw new NullPointerException("Invalid menuId"); //$NON-NLS-1$
		
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
