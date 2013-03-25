/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.corpus.swing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.tree.TreePath;

import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.language.corpus.CorpusRegistry;
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
public class CorpusTreeModel extends AbstractTreeModel {
	
	protected static Extension[] extensions;
	protected static LRUCache<Extension, Corpus[]> cache = new LRUCache<>(20);
	
	protected static CorpusRegistryListener sharedListener;
	protected static WeakHashMap<CorpusTreeModel, Object> instances = new WeakHashMap<>();
	protected static final Object present = new Object();
	
	protected static final Object root = new Object(){
		@Override
		public String toString() {
			return "root"; //$NON-NLS-1$
		}
	};
	
	public CorpusTreeModel() {
		
		if(extensions==null) {
			List<Extension> availableExtensions = new ArrayList<>(
					CorpusRegistry.getInstance().availableCorpusTypes());
			Collections.sort(availableExtensions, PluginUtil.IDENTITY_COMPARATOR);
			extensions = availableExtensions.toArray(new Extension[availableExtensions.size()]);
		}
		
		if(sharedListener==null) {
			sharedListener = new CorpusRegistryListener();
			CorpusRegistry.getInstance().addListener(null, sharedListener);
		}
		
		instances.put(this, present);
	}
	
	protected static Corpus[] getCorpora(Extension extension) {
		Corpus[] corpora = null;
		corpora = cache.get(extension);
		if(corpora==null) {
			List<Corpus> instances = CorpusRegistry.getInstance().getCorporaForType(extension);
			Collections.sort(instances, CorpusRegistry.CORPUS_NAME_COMPARATOR);
			corpora = instances.toArray(new Corpus[instances.size()]);
			cache.put(extension, corpora);
		}
		
		return corpora;
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
			child = getCorpora((Extension)parent)[index];
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
			childCount = getCorpora((Extension)parent).length;
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
		return node instanceof Corpus;
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
			children = getCorpora((Extension)parent);
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
	
	protected static class CorpusRegistryListener implements EventListener {

		/**
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			Corpus corpus = (Corpus) event.getProperty("corpus"); //$NON-NLS-1$
			if(corpus==null) {
				return;
			}
			Extension extension = (Extension) event.getProperty("extension"); //$NON-NLS-1$
			if(extension==null) {
				extension = CorpusRegistry.getInstance().getExtension(corpus);
			}
			Corpus[] children;
			TreePath parentPath = new TreePath(new Object[] {root, extension});
			
			List<CorpusTreeModel> models = new ArrayList<>(instances.keySet());
			
			switch (event.getName()) {
			case Events.ADDED:
				children = cache.get(extension);
				if(children==null) {
					// Load new children array, already containing our new corpus
					children = getCorpora(extension);
				} else {
					// Add corpus to existing children array
					Corpus[] newChildren = new Corpus[children.length+1];
					System.arraycopy(children, 0, newChildren, 0, children.length);
					newChildren[children.length] = corpus;
					Arrays.sort(newChildren, CorpusRegistry.CORPUS_NAME_COMPARATOR);
					children = newChildren;
				}
				cache.put(extension, children);
				
				for(CorpusTreeModel model : models) {
					model.fireTreeStructureChanged(parentPath);
				}
				break;
			
			case Events.REMOVED:
				children = cache.get(extension);
				if(children!=null && children.length>0) {
					int childIndex = indexOf(children, corpus);
					Corpus[] newChildren = new Corpus[children.length-1];
					System.arraycopy(children, 0, newChildren, 0, childIndex);
					System.arraycopy(children, childIndex+1, newChildren, 
							childIndex, newChildren.length-childIndex);
					cache.put(extension, newChildren);
				}
				
				for(CorpusTreeModel model : models) {
					model.fireTreeStructureChanged(parentPath);
				}
				break;
				
			case Events.CHANGED:
				children = getCorpora(extension);
				int childIndex = indexOf(children, corpus);
				for(CorpusTreeModel model : models) {
					model.fireChildChanged(parentPath, childIndex, corpus);
				}
				break;
			}
		}
	}
	
}