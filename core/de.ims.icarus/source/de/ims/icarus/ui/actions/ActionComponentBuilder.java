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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.actions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ActionComponentBuilder {

	private ActionManager actionManager;
	private String actionListId;
	private Options options;
	private Set<String> lockedOptions;
	
	public ActionComponentBuilder() {
		// no-op
	}

	public ActionComponentBuilder(ActionManager actionManager) {
		setActionManager(actionManager);
	}

	public ActionComponentBuilder(ActionManager actionManager, String actionListId) {
		setActionManager(actionManager);
		setActionListId(actionListId);
	}
	
	/**
	 * @return the actionManager
	 */
	public ActionManager getActionManager() {
		if(actionManager==null)
			throw new IllegalStateException("No action manager defined"); //$NON-NLS-1$
		
		return actionManager;
	}
	
	/**
	 * @return the actionListId
	 */
	public String getActionListId() {
		if(actionListId==null)
			throw new IllegalStateException("No action list id defined"); //$NON-NLS-1$
		
		return actionListId;
	}
	
	/**
	 * @param actionManager the actionManager to set
	 */
	public void setActionManager(ActionManager actionManager) {
		if(actionManager==null)
			throw new NullPointerException("Invalid action manager"); //$NON-NLS-1$
		
		this.actionManager = actionManager;
	}
	
	/**
	 * @param actionListId the actionListId to set
	 */
	public void setActionListId(String actionListId) {
		if(actionListId==null)
			throw new NullPointerException("Invalid action list id"); //$NON-NLS-1$
		
		this.actionListId = actionListId;
	}
	
	public Options getOptions() {
		if(options==null) {
			options = new Options();
		}
		return options;
	}
	
	private boolean isIgnored(String key) {
		return lockedOptions!=null && lockedOptions.contains(key);
	}
	
	public boolean addOption(String key, Object value) {
		if(key==null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$
		
		if(isIgnored(key)) {
			LoggerFactory.debug(this, "Ignoring options key: "+key); //$NON-NLS-1$
			return false;
		}
		
		getOptions().put(key, value);
		
		return true;
	}
	
	public Set<String> addOptions(Map<String, Object> map) {
		if(map==null)
			throw new NullPointerException("Invalid options map"); //$NON-NLS-1$

		Set<String> result = null;
		
		for(Map.Entry<String, Object> entry : map.entrySet()) {
			if(!addOption(entry.getKey(), entry.getValue())) {
				if(result==null) {
					result = new HashSet<>();
				}
				result.add(entry.getKey());
			}
		}
		
		if(result==null) {
			result = Collections.emptySet();
		}
		
		return result;
	}
	
	public void lockOption(String key) {
		if(key==null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$
		
		if(lockedOptions==null) {
			lockedOptions = new HashSet<>();
		}
		lockedOptions.add(key);
	}
	
	public JToolBar buildToolBar() {
		return getActionManager().createToolBar(getActionListId(), getOptions());
	}
	
	public JPopupMenu buildPopupMenu() {
		return getActionManager().createPopupMenu(getActionListId(), getOptions());
	}
	
	public JMenu buildMenu() {
		return getActionManager().createMenu(getActionListId(), getOptions());
	}
}
