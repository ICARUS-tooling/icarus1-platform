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
package de.ims.icarus.ui.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.ui.tree.AbstractTreeModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConfigTreeModel extends AbstractTreeModel {

	private static final Handle[] EMPTY_CHILDREN = {};

	private Map<Handle, Handle[]> childMap = new HashMap<>();

	public ConfigTreeModel(Handle root) {
		super(root);
	}

	public ConfigTreeModel(ConfigRegistry registry) {
		super(registry.ROOT_HANDLE);
	}

	/**
	 * @see de.ims.icarus.ui.tree.AbstractTreeModel#getRoot()
	 */
	@Override
	public Handle getRoot() {
		return (Handle) super.getRoot();
	}

	private Handle[] getChildren(Handle parent) {
		Handle[] result = childMap.get(parent);

		if(result==null) {
			ConfigRegistry registry = parent.getSource();
			int numChildren = registry.getItemCount(parent);

			if(numChildren > 0) {
				List<Handle> tmp = new ArrayList<>(numChildren);
				for(int i=0; i < numChildren; i++) {
					Handle child = registry.getChildHandle(parent, i);

					if(registry.isGroup(child) && !registry.isVirtual(child)
							&& !registry.isHidden(child)) {
						tmp.add(child);
					}
				}

				if(tmp.isEmpty()) {
					result = EMPTY_CHILDREN;
				} else {
					result = new Handle[tmp.size()];
					tmp.toArray(result);
				}

				childMap.put(parent, result);
			}
		}

		return result==EMPTY_CHILDREN ? null : result;
	}

	/**
	 * @see de.ims.icarus.ui.tree.AbstractTreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node) {
		Handle handle = (Handle)node;
		return handle.getSource().getItemCount(handle)==0
				|| getChildren(handle)==null;
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(Object parent, int index) {
		Handle[] children = getChildren((Handle) parent);
		return children==null ? null : children[index];
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object parent) {
		Handle[] children = getChildren((Handle) parent);
		return children==null ? 0 : children.length;
	}

}
