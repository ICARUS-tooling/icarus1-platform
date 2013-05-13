package net.ikarus_systems.icarus.plugins.weblicht;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.tree.TreePath;

import net.ikarus_systems.icarus.plugins.weblicht.webservice.Webchain;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebchainElements;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebchainRegistry;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebserviceProxy;

import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.ui.helper.AbstractTreeModel;

public class WeblichtTreeModel extends AbstractTreeModel {

	protected static WebchainLoaderListener sharedListener;
	protected static WeakHashMap<WeblichtTreeModel, Object> instances = new WeakHashMap<>();
	protected static final Object present = new Object();

	protected static final Object root = new Object() {
		@Override
		public String toString() {
			return "root"; //$NON-NLS-1$
		}
	};

	public WeblichtTreeModel() {

		if (sharedListener == null) {
			sharedListener = new WebchainLoaderListener();
			WebchainRegistry.getInstance().addListener(null, sharedListener);
		}

		instances.put(this, present);
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
		if (parent == root) {
			child = WebchainRegistry.getInstance().getWebchainAt(index);
		} else if (parent instanceof Webchain) {
			//child = ((Webchain) parent).getWebserviceAt(index);
			child = ((Webchain) parent).getElementAt(index);
		}
		// System.out.println("getChild@ parent: "+parent +" child: " +child +
		// " index: " +index);
		return child;
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object parent) {
		int childCount = 0;
		if (parent == root) {
			childCount = WebchainRegistry.getInstance().getWebchainCount();
		} else if (parent instanceof Webchain) {
			//childCount = ((Webchain) parent).getWebserviceCount();
			childCount = ((Webchain) parent).getElementsCount();
		}
		// System.out.println("getChildCount@ parent: "+parent +" childc: "
		// +childCount);
		return childCount;
	}

	/**
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node) {
		return node instanceof WebchainElements;
	}

	/**
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath,
	 *      java.lang.Object)
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// no-op
	}

	/**
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == null || child == null) {
			return -1;
		}
		int childIndex = -1;
		if (parent == root) {
			childIndex = WebchainRegistry.getInstance().indexOfWebchain(
					(Webchain) child);
		} else if (parent instanceof Webchain) {
			//childIndex = ((Webchain) parent).indexOfWebservice((WebserviceProxy) child);
			childIndex = ((Webchain) parent).indexOfElement((WebchainElements) child);
		}
		// System.out.println("getIndexOfChild@ parent: "+parent +" child: "
		// +child + " cindex: " +childIndex);
		return childIndex;

	}
	
	
	
	protected static Webchain[] getWebchains() {
		Webchain[] childs = null;
		
		if(childs==null) {
			List<Webchain> instances = new ArrayList<>();
			int chaincount = WebchainRegistry.getInstance().getWebchainCount();
			for (int i = 0; i < chaincount; i++){
				instances.add(WebchainRegistry.getInstance().getWebchainAt(i));				
			}
			childs = instances.toArray(new Webchain[instances.size()]);
		}

		return childs;
	}
	

	protected static class WebchainLoaderListener implements EventListener {

		/**
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object,
		 *      net.ikarus_systems.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			// System.out.println("Sender: " + sender);

			Webchain webchain = (Webchain) event.getProperty("webchain"); //$NON-NLS-1$
			if (webchain == null) {
				return;
			}
			
			int childIndex = (int) event.getProperty("index"); //$NON-NLS-1$

			//parent path always root, webchains will be added later in chaineditor
			TreePath parentPath = new TreePath(new Object[] {root});

			// parent + exakte element
			List<WeblichtTreeModel> models = new ArrayList<>(instances.keySet());

			switch (event.getName()) {
			case Events.ADDED:
				for (WeblichtTreeModel model : models) {
					model.fireChildAdded(parentPath, childIndex, webchain);
				}
				break;
				
			case Events.REMOVED:
				for (WeblichtTreeModel model : models) {
					model.fireChildRemoved(parentPath, childIndex, webchain);
				}
				break;

			case Events.CHANGED:
				childIndex = WebchainRegistry.getInstance().indexOfWebchain(webchain);
				for (WeblichtTreeModel model : models) {
					model.fireChildChanged(parentPath, childIndex, webchain);
				}
				break;
				
			case Events.CHANGE:
				for (WeblichtTreeModel model : models) {
					System.out.println(webchain.getName());
					System.out.println(parentPath);
					//model.fireTreeStructureChanged(parentPath);
					model.fireStructureChanged();
				}				
				break;
			}
			/*
			 * for(WeblichtTreeModel model : models) {
			 * model.fireStructureChanged(); }
			 */

		}
	}

}
