/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.treebank.swing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.tree.TreePath;

import net.ikarus_systems.icarus.language.treebank.Treebank;
import net.ikarus_systems.icarus.language.treebank.TreebankRegistry;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.ui.AbstractTreeModel;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.util.cache.LRUCache;

import org.java.plugin.registry.Extension;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankTreeModel extends AbstractTreeModel {
	
	protected static Extension[] extensions;
	protected static LRUCache<Extension, Treebank[]> cache = new LRUCache<>(20);
	
	protected static TreebankRegistryListener sharedListener;
	protected static WeakHashMap<TreebankTreeModel, Object> instances = new WeakHashMap<>();
	protected static final Object present = new Object();
	
	protected static final Object root = new Object(){
		@Override
		public String toString() {
			return "root"; //$NON-NLS-1$
		}
	};
	
	public TreebankTreeModel() {
		
		if(extensions==null) {
			List<Extension> availableExtensions = new ArrayList<>(
					TreebankRegistry.getInstance().availableTypes());
			Collections.sort(availableExtensions, PluginUtil.IDENTITY_COMPARATOR);
			extensions = availableExtensions.toArray(new Extension[availableExtensions.size()]);
		}
		
		if(sharedListener==null) {
			sharedListener = new TreebankRegistryListener();
			TreebankRegistry.getInstance().addListener(null, sharedListener);
		}
		
		instances.put(this, present);
	}
	
	protected static Treebank[] getTreebanks(Extension extension) {
		Treebank[] treebanks = null;
		treebanks = cache.get(extension);
		if(treebanks==null) {
			List<Treebank> instances = TreebankRegistry.getInstance().getInstances(extension);
			Collections.sort(instances, TreebankRegistry.TREEBANK_NAME_COMPARATOR);
			treebanks = instances.toArray(new Treebank[instances.size()]);
			cache.put(extension, treebanks);
		}
		
		return treebanks;
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
		Object child = null;
		if(parent==root) {
			child = extensions[index];
		} else if(parent instanceof Extension) {
			child = getTreebanks((Extension)parent)[index];
		}
		return child;
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object parent) {
		int childCount = 0; 
		if(parent==root) {
			childCount = extensions.length;
		} else if(parent instanceof Extension) {
			childCount = getTreebanks((Extension)parent).length;
		}
		return childCount;
	}

	/**
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node) {
		if(node==root) {
			return false;
		}
		return node instanceof Treebank;
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
		if(parent==null || child==null) {
			return -1;
		}
		
		Object[] children = null;
		
		if(parent==root) {
			children = extensions;
		} else if(parent instanceof Extension) {
			children = getTreebanks((Extension)parent);
		} else {
			return -1;
		}
		
		return indexOf(children, child);
	}
	
	protected static int indexOf(Object[] children, Object child) {
		
		for(int i = children.length-1; i>-1; i--) {
			if(children[i].equals(child)) {
				return i;
			}
		}
		
		return -1;
	}
	
	protected static class TreebankRegistryListener implements EventListener {

		/**
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			Treebank treebank = (Treebank) event.getProperty("treebank"); //$NON-NLS-1$
			if(treebank==null) {
				return;
			}
			Extension extension = (Extension) event.getProperty("extension"); //$NON-NLS-1$
			if(extension==null) {
				extension = TreebankRegistry.getInstance().getExtension(treebank);
			}
			Treebank[] children;
			TreePath parentPath = new TreePath(new Object[] {root, extension});
			
			List<TreebankTreeModel> models = new ArrayList<>(instances.keySet());
			
			switch (event.getName()) {
			case Events.ADDED:
				children = cache.get(extension);
				if(children==null) {
					// Load new children array, already containing our new treebank
					children = getTreebanks(extension);
				} else {
					// Add treebank to existing children array
					Treebank[] newChildren = new Treebank[children.length+1];
					System.arraycopy(children, 0, newChildren, 0, children.length);
					newChildren[children.length] = treebank;
					Arrays.sort(newChildren, TreebankRegistry.TREEBANK_NAME_COMPARATOR);
					children = newChildren;
				}
				cache.put(extension, children);
				
				for(TreebankTreeModel model : models) {
					model.fireTreeStructureChanged(parentPath);
				}
				break;
			
			case Events.REMOVED:
				children = cache.get(extension);
				if(children!=null && children.length>0) {
					int childIndex = indexOf(children, treebank);
					Treebank[] newChildren = new Treebank[children.length-1];
					System.arraycopy(children, 0, newChildren, 0, childIndex);
					System.arraycopy(children, childIndex+1, newChildren, 
							childIndex, newChildren.length-childIndex);
					cache.put(extension, newChildren);
				}
				
				for(TreebankTreeModel model : models) {
					model.fireTreeStructureChanged(parentPath);
				}
				break;
				
			case Events.CHANGED:
				children = getTreebanks(extension);
				int childIndex = indexOf(children, treebank);
				for(TreebankTreeModel model : models) {
					model.fireChildChanged(parentPath, childIndex, treebank);
				}
				break;
			}
		}
	}
	
}