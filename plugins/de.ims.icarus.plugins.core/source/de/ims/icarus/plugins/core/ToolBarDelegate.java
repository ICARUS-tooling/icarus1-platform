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

import java.awt.Component;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

import de.ims.icarus.ui.actions.ActionManager;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ToolBarDelegate {
	
	private JToolBar toolBar;
	
	private boolean separatorAllowed = true;
	
	public ToolBarDelegate() {
		// no-op
	}
	
	private void ensureToolBar() {
		if(toolBar==null) {
			toolBar = new JToolBar();
			toolBar.setFloatable(false);
			toolBar.setRollover(true);
		}
	}
	
	private void addImpl(Component comp) {
		if(comp==null) {
			return;
		}
		
		if(comp instanceof JSeparator && !separatorAllowed) {
			return;
		}
		
		ensureToolBar();
		
		toolBar.add(comp);
		comp.setFocusable(false);
		separatorAllowed = !(comp instanceof JSeparator);
	}
	
	JToolBar getToolbar() {
		ensureToolBar();
		return toolBar;
	}
	
	public boolean isEmpty() {
		return toolBar==null || toolBar.getComponentCount()==0;
	}
	
	void clear() {
		if(toolBar==null) {
			return;
		}
		
		toolBar.removeAll();
		separatorAllowed = true;
		
		toolBar.revalidate();
	}

	public void add(Component comp) {
		addImpl(comp);
	}
	
	public void addSeparator() {
		JToolBar toolBar = getToolbar();
		toolBar.addSeparator();
		separatorAllowed = false;
	}
	
	public void add(Action action) {
		if(action==null) {
			return;
		}
		JToolBar toolBar = getToolbar();
		JButton b = toolBar.add(action);
		b.setFocusable(false);
		separatorAllowed = true;
	}
	
	public void addAction(ActionManager actionManager, String id) {
		add(actionManager.getAction(id));
	}
	
	public void addActionList(ActionManager actionManager, String id, Map<String, Object> properties) {
		ensureToolBar();
		actionManager.feedToolBar(id, toolBar, properties);
		
		int count = toolBar.getComponentCount();
		Component lastAdded = count==0 ? null : toolBar.getComponent(count-1);
		separatorAllowed = !(lastAdded instanceof JSeparator);
	}
}
