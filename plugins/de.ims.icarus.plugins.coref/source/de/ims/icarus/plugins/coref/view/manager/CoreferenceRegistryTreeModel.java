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
package de.ims.icarus.plugins.coref.view.manager;

import javax.swing.tree.TreePath;

import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.helper.AbstractTreeModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceRegistryTreeModel extends AbstractTreeModel implements EventListener {
	
	private static final Object root = new Object();

	public CoreferenceRegistryTreeModel() {
		CoreferenceRegistry.getInstance().addListener(null, this);
	}

	/**
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	@Override
	public Object getRoot() {
		return root;
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(Object parent, int index) {
		if(parent==root) {
			return CoreferenceRegistry.getInstance().getDocumentSet(index);
		} else if(parent instanceof DocumentSetDescriptor) {
			return ((DocumentSetDescriptor)parent).get(index);
		} else {
			return null;
		}
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object parent) {
		if(parent==root) {
			return CoreferenceRegistry.getInstance().getDocumentSetCount();
		} else if(parent instanceof DocumentSetDescriptor) {
			return ((DocumentSetDescriptor)parent).size();
		} else {
			return 0;
		}
	}

	/**
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node) {
		return node instanceof AllocationDescriptor;
	}

	/**
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// no-op
	}

	/**
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if(parent==root) {
			return CoreferenceRegistry.getInstance().indexOfDocumentSet((DocumentSetDescriptor) child);
		} else if(parent instanceof DocumentSetDescriptor) {
			return ((DocumentSetDescriptor)parent).indexOfAllocation((AllocationDescriptor) child);
		} else {
			return -1;
		}
	}

	/**
	 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
	 */
	@Override
	public void invoke(Object sender, EventObject event) {
		if(Events.CHANGED.equals(event.getName())) {
			TreePath path = new TreePath(root);
			int index;
			Object child;
			if(event.getProperties().containsKey("documentSet")) { //$NON-NLS-1$
				child = event.getProperty("documentSet"); //$NON-NLS-1$
				index = getIndexOfChild(root, child);
			} else {
				child = event.getProperty("allocation"); //$NON-NLS-1$
				Object parent = ((AllocationDescriptor)child).getParent();
				path = path.pathByAddingChild(parent);
				index = getIndexOfChild(parent, child);
			}
			fireChildChanged(path, index, child);
			
		} else if(Events.ADDED.equals(event.getName())) {
			TreePath path = new TreePath(root);
			int index;
			Object child;
			if(event.getProperties().containsKey("documentSet")) { //$NON-NLS-1$
				child = event.getProperty("documentSet"); //$NON-NLS-1$
				index = getIndexOfChild(root, child);
			} else {
				child = event.getProperty("allocation"); //$NON-NLS-1$
				Object parent = ((AllocationDescriptor)child).getParent();
				path = path.pathByAddingChild(parent);
				index = getIndexOfChild(parent, child);
			}
			
			fireChildAdded(path, index, child);

		} else if(Events.REMOVED.equals(event.getName())) {
			TreePath path = new TreePath(root);
			int index = (int) event.getProperty("index"); //$NON-NLS-1$
			Object child;
			if(event.getProperties().containsKey("documentSet")) { //$NON-NLS-1$
				child = event.getProperty("documentSet"); //$NON-NLS-1$
			} else {
				child = event.getProperty("allocation"); //$NON-NLS-1$
				Object parent = (DocumentSetDescriptor) event.getProperty("parent"); //$NON-NLS-1$
				if(parent==null) {
					parent = ((AllocationDescriptor)child).getParent();
				}
				path = path.pathByAddingChild(parent);
			}
			
			fireChildRemoved(path, index, child);
		}
	}
	
	public void close() {
		CoreferenceRegistry.getInstance().removeListener(this);
	}
}
