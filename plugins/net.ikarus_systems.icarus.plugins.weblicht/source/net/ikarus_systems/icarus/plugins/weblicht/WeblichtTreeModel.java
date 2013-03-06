package net.ikarus_systems.icarus.plugins.weblicht;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.tree.TreePath;

import org.java.plugin.registry.Extension;

import net.ikarus_systems.icarus.ui.AbstractTreeModel;

import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;


public class WeblichtTreeModel extends AbstractTreeModel {
	
	//protected static Extension[] extensions;
	//protected static LRUCache<Extension, Corpus[]> cache = new LRUCache<>(20);
	
	protected static WeblichtRegistryListener sharedListener;
	protected static WeakHashMap<WeblichtTreeModel, Object> instances = new WeakHashMap<>();
	protected static final Object present = new Object();
	
	protected static final Object root = new Object(){
		@Override
		public String toString() {
			return "root"; //$NON-NLS-1$
		}
	};

	@Override
	public Object getChild(Object arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getChildCount(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getIndexOfChild(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public boolean isLeaf(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void valueForPathChanged(TreePath arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
	
	protected static class WeblichtRegistryListener implements EventListener {

		@Override
		public void invoke(Object sender, EventObject event) {
			// TODO Auto-generated method stub
			
		}
	}
	

}
