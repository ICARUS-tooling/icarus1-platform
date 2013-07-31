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
package de.ims.icarus.plugins.core.explorer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginElement;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class PluginExplorerUtil {

	private PluginExplorerUtil() {
		// no-op
	}

	
	public static Object[] getObjectPath(TreePath treePath) {
		if(treePath==null) {
			return null;
		}
		
		Object[] items = new Object[treePath.getPathCount()];
		for(int index = items.length-1; index>-1; index--) {
			Object item = treePath.getLastPathComponent();
			if(item instanceof DefaultMutableTreeNode) {
				item = ((DefaultMutableTreeNode)item).getUserObject();
			}
			if(item instanceof PluginElementProxy) {
				item = ((PluginElementProxy)item).get();
			}
			items[index] = item;
			treePath = treePath.getParentPath();
		}
		
		return items;
	}
	
	/**
	 * Returns the first occurring {@code PluginDescriptor} in 
	 * the given {@code objectPath}. Search is started at the last element.
	 */
	public static PluginDescriptor getPluginDescriptor(Object[] objectPath) {
		if(objectPath==null) {
			return null;
		}
		
		for(int i=objectPath.length-1; i>-1; i--) {
			Object node = objectPath[i];
			if(node instanceof PluginElementProxy) {
				node = ((PluginElementProxy)node).get();
			}
			if(node instanceof PluginDescriptor) {
				return (PluginDescriptor) node;
			}
		}
		
		return null;
	}

	/**
	 * Searches for a declaring {@code PluginDescriptor} in the given
	 * {@code objectPath}. Works similar as {@link #getPluginDescriptor(Object[])}
	 * but in the case that no element in the given path happened to be of type
	 * {@code PluginDescriptor} the first {@code PluginElement} encountered
	 * will be used to fetch a valid descriptor. 
	 */
	public static PluginDescriptor getDeclaringPluginDescriptor(Object[] objectPath) {
		if(objectPath==null) {
			return null;
		}
		
		PluginDescriptor descriptor = null;
		
		for(int i=objectPath.length-1; i>-1; i--) {
			Object node = objectPath[i];
			if(node instanceof PluginElementProxy) {
				node = ((PluginElementProxy)node).get();
			}
			if(node instanceof PluginDescriptor) {
				return (PluginDescriptor) node;
			} else if(descriptor==null && node instanceof PluginElement) {
				descriptor = ((PluginElement<?>)node).getDeclaringPluginDescriptor();
			}
		}
		
		return descriptor;
	}
}
