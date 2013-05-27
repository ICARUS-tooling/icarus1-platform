/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreePath;

import net.ikarus_systems.icarus.search_tools.SearchDescriptor;
import net.ikarus_systems.icarus.ui.helper.AbstractTreeModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchHistoryTreeModel extends AbstractTreeModel implements PropertyChangeListener {
	
	private List<SearchDescriptor> descriptors = new ArrayList<>();
	
	private final static Object root = new Object();

	public SearchHistoryTreeModel() {
		// no-op
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
			return descriptors.get(index);
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
			return descriptors.size();
		} else if(SearchDescriptor.class.equals(parent.getClass())) {
			return 3;
		} else {
			return 0;
		}
	}

	/**
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node) {
		return node!=root && !SearchDescriptor.class.equals(node.getClass());
	}

	/**
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if(parent==root) {
			return descriptors.indexOf(child);
		} else {
			// TODO
			return -1;
		}
	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}

	public void addSearch(SearchDescriptor descriptor) {
		if(descriptor==null)
			throw new IllegalArgumentException("Invalid descriptor"); //$NON-NLS-1$
		
		descriptors.add(descriptor);
		
		fireChildAdded(new TreePath(root), descriptors.size()-1, descriptor);
	}
	
	public void removeSearch(SearchDescriptor descriptor) {
		if(descriptor==null)
			throw new IllegalArgumentException("Invalid descriptor"); //$NON-NLS-1$
		
		int index = descriptors.indexOf(descriptor);
		if(index==-1) {
			return;
		}
		
		descriptors.remove(index);
		
		fireChildRemoved(new TreePath(root), index, descriptor);
	}
	
	public void clear() {
		descriptors.clear();
		
		fireStructureChanged();
	}
}
