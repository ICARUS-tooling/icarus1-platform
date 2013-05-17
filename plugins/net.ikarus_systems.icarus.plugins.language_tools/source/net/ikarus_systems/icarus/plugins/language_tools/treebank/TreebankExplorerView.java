/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools.treebank;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import net.ikarus_systems.icarus.language.treebank.DerivedTreebank;
import net.ikarus_systems.icarus.language.treebank.Treebank;
import net.ikarus_systems.icarus.language.treebank.TreebankDescriptor;
import net.ikarus_systems.icarus.language.treebank.TreebankEvents;
import net.ikarus_systems.icarus.language.treebank.TreebankImportResult;
import net.ikarus_systems.icarus.language.treebank.TreebankInfo;
import net.ikarus_systems.icarus.language.treebank.TreebankListDelegate;
import net.ikarus_systems.icarus.language.treebank.TreebankRegistry;
import net.ikarus_systems.icarus.language.treebank.swing.TreebankListCellRenderer;
import net.ikarus_systems.icarus.language.treebank.swing.TreebankTreeCellRenderer;
import net.ikarus_systems.icarus.language.treebank.swing.TreebankTreeModel;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.core.InfoPanel;
import net.ikarus_systems.icarus.plugins.core.ToolBarDelegate;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.plugins.language_tools.LanguageToolsConstants;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.dialog.BasicDialogBuilder;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.ui.tasks.TaskManager;
import net.ikarus_systems.icarus.ui.tasks.TaskPriority;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.NamingUtil;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankExplorerView extends View {
	
	private JTree treebanksTree;
	
	private JPopupMenu popupMenu;
	
	private Handler handler;
	private LoadTracker loadTracker;
	private CallbackHandler callbackHandler;

	public TreebankExplorerView() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// Load actions
		URL actionLocation = TreebankExplorerView.class.getResource("treebank-explorer-view-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: treebank-explorer-view-actions.xml"); //$NON-NLS-1$
		
		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}
		
		handler = new Handler();
		loadTracker = new LoadTracker();

		// Create and init tree
		treebanksTree = new JTree(new TreebankTreeModel());
		UIUtil.enableToolTip(treebanksTree);
		UIUtil.enableRighClickTreeSelection(treebanksTree);
		treebanksTree.setCellRenderer(new TreebankTreeCellRenderer());
		treebanksTree.setEditable(false);
		treebanksTree.setBorder(UIUtil.defaultContentBorder);
		treebanksTree.setLargeModel(true);
		treebanksTree.setRootVisible(false);
		treebanksTree.setShowsRootHandles(true);	
		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
		treebanksTree.setSelectionModel(selectionModel);
		treebanksTree.addTreeSelectionListener(handler);		
		treebanksTree.addMouseListener(handler);
		treebanksTree.getModel().addTreeModelListener(handler);
		UIUtil.expandAll(treebanksTree, true);
		
		// Scroll pane
		JScrollPane scrollPane = new JScrollPane(treebanksTree);
		scrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		
		// Header
		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"plugins.languageTools.treebankExplorerView.toolBarList", null); //$NON-NLS-1$
		
		container.setLayout(new BorderLayout());
		container.add(toolBar, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		container.setPreferredSize(new Dimension(300, 500));
		container.setMinimumSize(new Dimension(250, 400));
		
		TreebankRegistry.getInstance().addListener(Events.ADDED, handler);
		TreebankRegistry.getInstance().addListener(Events.REMOVED, handler);
		
		registerActionCallbacks();
		refreshActions(null);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		UIUtil.expandAll(treebanksTree, false);
		treebanksTree.expandPath(new TreePath(treebanksTree.getModel().getRoot()));
	}
	
	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#close()
	 */
	@Override
	public void close() {
		TreebankRegistry.getInstance().removeListener(handler);
	}

	@Override
	protected void refreshInfoPanel(InfoPanel infoPanel) {
		infoPanel.addLabel("selectedItem"); //$NON-NLS-1$
		infoPanel.addSeparator();
		infoPanel.addLabel("totalTypes", 70); //$NON-NLS-1$
		infoPanel.addSeparator();
		infoPanel.addLabel("totalTreebanks", 100); //$NON-NLS-1$
		infoPanel.addGap(100);
		
		showTreebankInfo();
	}
	
	@Override
	protected void buildToolBar(ToolBarDelegate delegate) {
		delegate.addAction(getDefaultActionManager(), 
				"plugins.languageTools.treebankExplorerView.exportTreebanksAction"); //$NON-NLS-1$
		delegate.addAction(getDefaultActionManager(), 
				"plugins.languageTools.treebankExplorerView.importTreebanksAction"); //$NON-NLS-1$
	}
	
	private void showTreebankInfo() {
		InfoPanel infoPanel = getInfoPanel();
		if(infoPanel==null) {
			return;
		}
		
		Object selectedItem = getSelectedObject();
		if(selectedItem instanceof Treebank) {
			Treebank treebank = (Treebank) selectedItem;
			TreebankDescriptor descriptor = TreebankRegistry.getInstance().getDescriptor(treebank);
			String text = treebank.getName()+" - "+descriptor.getExtension().getUniqueId(); //$NON-NLS-1$

			infoPanel.displayText("selectedItem", text); //$NON-NLS-1$
		} else {
			infoPanel.displayText("selectedItem", null); //$NON-NLS-1$
		}
		
		// Total treebank types
		String text = ResourceManager.getInstance().get(
				"plugins.languageTools.treebankExplorerView.labels.totalTypes",  //$NON-NLS-1$
				TreebankRegistry.getInstance().availableTypeCount()); 
		infoPanel.displayText("totalTypes", text); //$NON-NLS-1$
		
		// Total treebanks
		text = ResourceManager.getInstance().get(
				"plugins.languageTools.treebankExplorerView.labels.totalTreebanks",  //$NON-NLS-1$
				TreebankRegistry.getInstance().availableTreebankCount()); 
		infoPanel.displayText("totalTreebanks", text); //$NON-NLS-1$
	}
	
	private Object[] getSelectionPath() {
		TreePath path = treebanksTree.getSelectionPath();
		if(path==null) {
			return null;
		}
		Object[] items = path.getPath();
		for(int i=items.length-1; i>-1; i--) {
			if(items[i] instanceof DefaultMutableTreeNode) {
				items[i] = ((DefaultMutableTreeNode)items[i]).getUserObject();
			}
		}
		
		return items;
	}
	
	private Object getSelectedObject() {
		Object[] path = getSelectionPath();
		if(path==null || path.length==0) {
			return null;
		}
		return path[path.length-1];
	}
	
	private void showPopup(MouseEvent trigger) {
		if(popupMenu==null) {
			// Create new popup menu
			
			Options options = new Options();
			popupMenu = getDefaultActionManager().createPopupMenu(
					"plugins.languageTools.treebankExplorerView.popupMenuList", options); //$NON-NLS-1$
			
			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create popup menu"); //$NON-NLS-1$
			}
		}
		
		if(popupMenu!=null) {			
			popupMenu.show(treebanksTree, trigger.getX(), trigger.getY());
		}
	}
	
	private void refreshActions(Object selectedObject) {
		ActionManager actionManager = getDefaultActionManager();
		
		boolean isTreebank = selectedObject instanceof Treebank;
		boolean isExtension = selectedObject instanceof Extension;
		boolean isLoaded = isTreebank && ((Treebank)selectedObject).isLoaded();
		
		actionManager.setEnabled(isTreebank || isExtension, 
				"plugins.languageTools.treebankExplorerView.newTreebankAction");  //$NON-NLS-1$
		
		actionManager.setEnabled(isTreebank, 
				"plugins.languageTools.treebankExplorerView.deleteTreebankAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankExplorerView.cloneTreebankAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankExplorerView.renameTreebankAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankExplorerView.openLocationAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankExplorerView.editTreebankAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankExplorerView.exportTreebankAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(isTreebank && isLoaded, 
				"plugins.languageTools.treebankExplorerView.inspectTreebankAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankExplorerView.freeTreebankAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(isTreebank && !isLoaded, 
				"plugins.languageTools.treebankExplorerView.loadTreebankAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(TreebankRegistry.getInstance().availableTreebankCount()>0, 
				"plugins.languageTools.treebankExplorerView.exportTreebanksAction"); //$NON-NLS-1$
	}
	
	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		actionManager.addHandler("plugins.languageTools.treebankExplorerView.newTreebankAction",  //$NON-NLS-1$
				callbackHandler, "newTreebank"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.treebankExplorerView.deleteTreebankAction",  //$NON-NLS-1$
				callbackHandler, "deleteTreebank"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.treebankExplorerView.cloneTreebankAction",  //$NON-NLS-1$
				callbackHandler, "cloneTreebank"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.treebankExplorerView.renameTreebankAction",  //$NON-NLS-1$
				callbackHandler, "renameTreebank"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.treebankExplorerView.openLocationAction",  //$NON-NLS-1$
				callbackHandler, "openLocation"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.treebankExplorerView.inspectTreebankAction",  //$NON-NLS-1$
				callbackHandler, "inspectTreebank"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.treebankExplorerView.loadTreebankAction",  //$NON-NLS-1$
				callbackHandler, "loadTreebank"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.treebankExplorerView.freeTreebankAction",  //$NON-NLS-1$
				callbackHandler, "freeTreebank"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.treebankExplorerView.editTreebankAction",  //$NON-NLS-1$
				callbackHandler, "editTreebank"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.treebankExplorerView.exportTreebankAction",  //$NON-NLS-1$
				callbackHandler, "exportTreebank"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.treebankExplorerView.exportTreebanksAction",  //$NON-NLS-1$
				callbackHandler, "exportTreebanks"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.treebankExplorerView.importTreebanksAction",  //$NON-NLS-1$
				callbackHandler, "importTreebanks"); //$NON-NLS-1$
	}
	
	private class LoadTracker implements EventListener {
		
		private Treebank treebank;

		/**
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			if(TreebankEvents.LOADED.equals(event.getName())
					|| TreebankEvents.FREED.equals(event.getName())) {
				refreshActions(getSelectedObject());
			}
		}
		
		void unregister() {
			if(treebank!=null) {
				treebank.removeListener(this);
			}
		}
		
		void register(Treebank treebank) {
			if(treebank==this.treebank) {
				return;
			}
			
			if(this.treebank!=null) {
				this.treebank.removeListener(this);
			}
			
			this.treebank = treebank;
			
			if(this.treebank!=null) {
				this.treebank.addListener(TreebankEvents.LOADING, this);
				this.treebank.addListener(TreebankEvents.LOADED, this);
				this.treebank.addListener(TreebankEvents.FREEING, this);
				this.treebank.addListener(TreebankEvents.FREED, this);
			}
		}
	}
	
	private class Handler extends MouseAdapter implements TreeSelectionListener, 
		TreeModelListener, EventListener {

		/**
		 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
		 */
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			Object[] path = getSelectionPath();
			Object selectedObject = (path==null || path.length==0) ? null : path[path.length-1];
	
			refreshActions(selectedObject);
			
			if(selectedObject instanceof Treebank) {
				loadTracker.register((Treebank) selectedObject);
			} else {
				loadTracker.unregister();
			}
			
			showTreebankInfo();
			
			fireBroadcastEvent(new EventObject(LanguageToolsConstants.TREEBANK_EXPLORER_SELECTION_CHANGED, 
					"item", selectedObject, "path", path)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		private void maybeShowPopup(MouseEvent e) {
			if(e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
				// Note: we rely on the fact, that our callback handler
				// does not use the supplied ActionEvent object, so we pass null.
				// If this ever changes we could run into some trouble!
				callbackHandler.editTreebank(null);
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * @see javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event.TreeModelEvent)
		 */
		@Override
		public void treeNodesChanged(TreeModelEvent e) {
			// no-op
		}

		/**
		 * @see javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event.TreeModelEvent)
		 */
		@Override
		public void treeNodesInserted(TreeModelEvent e) {
			TreePath path = e.getTreePath();
			if(path==null) {
				return;
			}
			treebanksTree.expandPath(path);
		}

		/**
		 * @see javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event.TreeModelEvent)
		 */
		@Override
		public void treeNodesRemoved(TreeModelEvent e) {
			// no-op
		}

		/**
		 * @see javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.event.TreeModelEvent)
		 */
		@Override
		public void treeStructureChanged(TreeModelEvent e) {
			TreePath path = e.getTreePath();
			if(path==null) {
				return;
			}
			treebanksTree.expandPath(path);
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			showTreebankInfo();
			refreshActions(getSelectedObject());
		}
	}
	
	public final class CallbackHandler {
		
		private CallbackHandler() {
			// no-op
		}
		
		public void newTreebank(ActionEvent e) {
			Object[] path = getSelectionPath();
			if(path==null) {
				return;
			}
			
			Extension extension = null;
			for(int i=path.length-1; i>-1; i--) {
				if(path[i] instanceof Extension) {
					extension = (Extension) path[i];
				}
			}
			
			if(extension==null) {
				return;
			}
			
			String name = TreebankRegistry.getInstance().getUniqueName("New "+extension.getId()); //$NON-NLS-1$
			try {
				TreebankRegistry.getInstance().newTreebank(extension, name);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to create new treebank: "+name, ex); //$NON-NLS-1$
			}
		}
		
		public void deleteTreebank(ActionEvent e) {
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Treebank)) {
				return;
			}
			
			Treebank treebank = (Treebank)selectedObject;
			boolean doDelete = false;
			
			// Special handling for treebanks with other treebanks derived from them 
			List<DerivedTreebank> derivedTreebanks = TreebankRegistry.getInstance().getDerived(treebank);
			if(!derivedTreebanks.isEmpty()) {
				Collections.sort(derivedTreebanks, TreebankRegistry.TREEBANK_NAME_COMPARATOR);
				StringBuilder sb = new StringBuilder();
				int count = Math.min(5, derivedTreebanks.size());
				for(int i=0; i< count; i++) {
					sb.append(derivedTreebanks.get(i).getName()).append("\n"); //$NON-NLS-1$
				}
				if(derivedTreebanks.size()>5) {
					sb.append(ResourceManager.getInstance().get(
							"plugins.languageTools.treebankExplorerView.dialogs.hint", //$NON-NLS-1$
							derivedTreebanks.size()-5)).append("\n"); //$NON-NLS-1$
				}

				doDelete = DialogFactory.getGlobalFactory().showConfirm(getFrame(), 
						"plugins.languageTools.treebankExplorerView.dialogs.deleteBaseTreebank.title",  //$NON-NLS-1$
						"plugins.languageTools.treebankExplorerView.dialogs.deleteBaseTreebank.message",  //$NON-NLS-1$
						treebank.getName(), sb.toString());
			} else {
				// Handling for regular treebanks
				doDelete = DialogFactory.getGlobalFactory().showConfirm(getFrame(), 
						"plugins.languageTools.treebankExplorerView.dialogs.deleteTreebank.title",  //$NON-NLS-1$
						"plugins.languageTools.treebankExplorerView.dialogs.deleteTreebank.message",  //$NON-NLS-1$
						treebank.getName());
			}
			
			if(doDelete) {
				try {
					TreebankRegistry.getInstance().deleteTreebank(treebank);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Unable to delete treebank: "+treebank.getName(), ex); //$NON-NLS-1$
				}
			}
		}
		
		public void cloneTreebank(ActionEvent e) {
			Object[] path = getSelectionPath();
			if(path==null || path.length==0) {
				return;
			}
			Object selectedObject = path[path.length-1];
			if(selectedObject==null || !(selectedObject instanceof Treebank)) {
				return;
			}
			
			Treebank treebank = (Treebank)selectedObject;
			
			Extension extension = null;
			for(int i=path.length-1; i>-1; i--) {
				if(path[i] instanceof Extension) {
					extension = (Extension) path[i];
				}
			}
			
			if(extension==null) {
				return;
			}
			
			String name = TreebankRegistry.getInstance().getUniqueName(treebank.getName());
			try {
				TreebankRegistry.getInstance().newTreebank(extension, name);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to create new treebank: "+name, ex); //$NON-NLS-1$
			}
		}
		
		public void renameTreebank(ActionEvent e) {
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Treebank)) {
				return;
			}
			
			Treebank treebank = (Treebank)selectedObject;
			
			String currentName = treebank.getName();
			String newName = DialogFactory.getGlobalFactory().showInputDialog(getFrame(), 
					"plugins.languageTools.treebankExplorerView.dialogs.renameTreebank.title",  //$NON-NLS-1$
					"plugins.languageTools.treebankExplorerView.dialogs.renameTreebank.message",  //$NON-NLS-1$
					currentName, currentName);
			
			if(newName==null || newName.isEmpty()) {
				return;
			}
			
			// No changes
			if(currentName.equals(newName)) {
				return;
			}
			
			// Let treebank registry manage naming checks
			String uniqueName = TreebankRegistry.getInstance().getUniqueName(newName);
			if(!uniqueName.equals(newName)) {
				DialogFactory.getGlobalFactory().showInfo(getFrame(), 
						"plugins.languageTools.treebankExplorerView.dialogs.title",  //$NON-NLS-1$
						"plugins.languageTools.treebankExplorerView.dialogs.duplicateName",  //$NON-NLS-1$
						newName, uniqueName);
			}
			
			try {
				TreebankRegistry.getInstance().setName(treebank, uniqueName);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to rename treebank "+currentName+" to "+uniqueName, ex); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		public void openLocation(ActionEvent e) {
			if(!Desktop.isDesktopSupported()) {
				return;
			}
			
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Treebank)) {
				return;
			}
			
			Treebank treebank = (Treebank)selectedObject;
			Location location = treebank.getLocation();
			if(location==null) {
				return;
			}
			
			try {
				Desktop desktop = Desktop.getDesktop();
				if(location.isLocal()) {
					// Open local treebanks in the default explorer
					desktop.open(location.getFile().getParentFile());
				} else {
					// Use the systems browser for remote treebanks
					desktop.browse(location.getURL().toURI());
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to open treebank location: "+treebank.getName(), ex); //$NON-NLS-1$
			}
		}
		
		public void inspectTreebank(ActionEvent e) {	
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Treebank)) {
				return;
			}
			
			Treebank treebank = (Treebank)selectedObject;
			if(!treebank.isLoaded()) {
				return;
			}
			
			ContentType contentType = ContentTypeRegistry.getInstance().getTypeForClass(Treebank.class);
			
			Options options = new Options();
			options.put(Options.CONTENT_TYPE, contentType);
			// TODO send some kind of hint that we want the presenter not to modify content?
			// -> Should be no problem since we only contain immutable data objects?
			TreebankListDelegate delegate = TreebankRegistry.getInstance().getListDelegate(treebank);
			
			Message message = new Message(this, Commands.DISPLAY, delegate, options);
			sendRequest(null, message);
		}
		
		public void loadTreebank(ActionEvent e) {	
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Treebank)) {
				return;
			}
			
			Treebank treebank = (Treebank)selectedObject;
			if(treebank.isLoaded()) {
				return;
			}
			
			try {
				TreebankJob task = new TreebankJob(treebank, true);
				TaskManager.getInstance().schedule(task, TaskPriority.DEFAULT, true);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to schedule load-task for treebank: "+treebank.getName(), ex); //$NON-NLS-1$
			}
		}
		
		public void freeTreebank(ActionEvent e) {	
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Treebank)) {
				return;
			}
			
			Treebank treebank = (Treebank)selectedObject;
			if(!treebank.isLoaded()) {
				return;
			}
			
			try {
				TreebankJob task = new TreebankJob(treebank, false);
				TaskManager.getInstance().schedule(task, TaskPriority.DEFAULT, true);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to schedule free-task for treebank: "+treebank.getName(), ex); //$NON-NLS-1$
			}
		}
		
		public void editTreebank(ActionEvent e) {			
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Treebank)) {
				return;
			}
			
			Treebank treebank = (Treebank)selectedObject;
			
			Message message = new Message(this, Commands.EDIT, treebank, null);
			sendRequest(LanguageToolsConstants.TREEBANK_EDIT_VIEW_ID, message);
		}
		
		public void exportTreebank(ActionEvent e) {
			// TODO allow user to select location and format
		}

		// TODO switch execution to background task?
		public void exportTreebanks(ActionEvent e) {
			
			// Obtain a sorted collection of all available treebanks
			List<Treebank> treebanks = new ArrayList<>(
					TreebankRegistry.getInstance().availableTreebanks());
			Collections.sort(treebanks, TreebankRegistry.TREEBANK_NAME_COMPARATOR);

			// Create a treebank list with multi-selection enabled
			DefaultListModel<Treebank> model = new DefaultListModel<>();
			for(Treebank treebank : treebanks) {
				model.addElement(treebank);
			}
			JList<Treebank> list = new JList<>(model);
			list.setCellRenderer(new TreebankListCellRenderer());
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			
			JScrollPane scrollPane = new JScrollPane(list);
			scrollPane.setPreferredSize(new Dimension(250, 200));
			
			if(!DialogFactory.getGlobalFactory().showGenericDialog(
					getFrame(), 
					"plugins.languageTools.treebankExplorerView.dialogs.exportTreebanks.title",  //$NON-NLS-1$
					"plugins.languageTools.treebankExplorerView.dialogs.exportTreebanks.selectInfo",  //$NON-NLS-1$
					scrollPane, false, "ok", "cancel")) { //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			
			int[] indices = list.getSelectedIndices();
			if(indices.length==0) {
				return;
			}
			
			// Collect selected treebanks
			treebanks.clear();
			for(int index : indices) {
				treebanks.add(list.getModel().getElementAt(index));
			}
			
			// Obtain destination file (factory handles the 'overwrite' dialog)
			File file = DialogFactory.getGlobalFactory().showDestinationFileDialog(
					getFrame(), 
					"plugins.languageTools.treebankExplorerView.dialogs.exportTreebanks.title",  //$NON-NLS-1$
					null);
			
			if(file==null) {
				return;
			}
			
			try {
				// Perform export
				TreebankRegistry.getInstance().exportTreebanks(file, treebanks);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to export treebanks to file: "+file.getAbsolutePath(), ex); //$NON-NLS-1$
				DialogFactory.getGlobalFactory().showError(getFrame(), 
						"plugins.languageTools.treebankExplorerView.dialogs.exportTreebanks.title",  //$NON-NLS-1$
						"plugins.languageTools.treebankExplorerView.dialogs.exportTreebanks.ioException",  //$NON-NLS-1$
						NamingUtil.fit(file.getAbsolutePath(), 100),
						NamingUtil.fit(ex.getMessage(), 100));
				return;
			}
			
			// Feedback to user
			DialogFactory.getGlobalFactory().showInfo(getFrame(), 
					"plugins.languageTools.treebankExplorerView.dialogs.exportTreebanks.title",  //$NON-NLS-1$
					"plugins.languageTools.treebankExplorerView.dialogs.exportTreebanks.result",  //$NON-NLS-1$
					treebanks.size(), NamingUtil.fit(file.getAbsolutePath(), 100));
		}
		
		// TODO switch execution to background task?
		public void importTreebanks(ActionEvent e) {
			
			// Obtain source file
			File file = DialogFactory.getGlobalFactory().showSourceFileDialog(
					getFrame(), 
					"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.title",  //$NON-NLS-1$
					null);
			
			if(file==null || !file.exists()) {
				return;
			}
			
			TreebankImportResult importResult = null;
			
			// Perform import
			try {
				importResult = TreebankRegistry.getInstance().importTreebanks(file);
			} catch (IOException ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to import treebanks from file: "+file.getAbsolutePath(), ex); //$NON-NLS-1$
				DialogFactory.getGlobalFactory().showError(getFrame(), 
						"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.title",  //$NON-NLS-1$
						"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.ioException",  //$NON-NLS-1$
						NamingUtil.fit(file.getAbsolutePath(), 100),
						NamingUtil.fit(ex.toString(), 100));
				return;
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Cannot import treebank data from file: "+file.getAbsolutePath(), ex); //$NON-NLS-1$
				DialogFactory.getGlobalFactory().showError(getFrame(), 
						"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.title",  //$NON-NLS-1$
						"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.invalidContent",  //$NON-NLS-1$
						NamingUtil.fit(file.getAbsolutePath(), 100),
						NamingUtil.fit(ex.toString(), 100));
				return;
			}
			
			// No big deal if we didn't find something to import
			if(importResult==null || importResult.isEmpty()) {
				DialogFactory.getGlobalFactory().showInfo(getFrame(), 
						"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.title",  //$NON-NLS-1$
						"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.noItems");  //$NON-NLS-1$
				return;
			}
			
			// Abort if all treebanks are unavailable
			// (no available treebanks means all the remaining are unavailable)
			if(importResult.getAvailableTreebankCount()==0) {
				StringBuilder sb = new StringBuilder(300);
				List<TreebankInfo> items = importResult.getUnavailableTreebanks();
				int count = Math.min(5, items.size());
				for(int i=0; i<count; i++) {
					sb.append(items.get(i).getTreebankName())
					.append(" - ") //$NON-NLS-1$
					.append(items.get(i).getPluginId())
					.append(" (v") //$NON-NLS-1$
					.append(items.get(i).getPluginVersion())
					.append(")") //$NON-NLS-1$
					.append("\n"); //$NON-NLS-1$
				}
				if(items.size()>5) {
					sb.append(ResourceManager.getInstance().get(
							"plugins.languageTools.treebankExplorerView.dialogs.hint",  //$NON-NLS-1$
							items.size()-5));
				}
				
				DialogFactory.getGlobalFactory().showWarning(getFrame(), 
						"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.title",  //$NON-NLS-1$
						"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.noAvailableItems", //$NON-NLS-1$
						sb.toString());
				return;
			}
			
			// Scan for duplicate names (expensive!!)
			Set<TreebankInfo> duplicates = new LinkedHashSet<>();
			for(TreebankInfo info : importResult.getAvailabeTreebanks().keySet()) {
				if(TreebankRegistry.getInstance().getDescriptorByName(info.getTreebankName())!=null) {
					duplicates.add(info);
				}
			}
			
			// Allow user to keep duplicates
			if(!duplicates.isEmpty()) {
				TreebankInfo[] items = duplicates.toArray(
						new TreebankInfo[duplicates.size()]);
				JList<TreebankInfo> list = new JList<>(items);
				list.setFocusable(false);
				list.setCellRenderer(new TreebankListCellRenderer());
				JScrollPane scrollPane = new JScrollPane(list);
				scrollPane.setPreferredSize(new Dimension(250, 200));
				
				BasicDialogBuilder builder = DialogFactory.getGlobalFactory().newBuilder();
				builder.setTitle("plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.title"); //$NON-NLS-1$
				builder.setPlainType();
				builder.setMessage("plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.duplicatesInfo", duplicates.size()); //$NON-NLS-1$
				builder.addMessage(scrollPane);
				builder.addMessage("plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.keepNamesInfo"); //$NON-NLS-1$
				builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
				
				builder.showDialog(getFrame());
				
				// Disable automatic renaming if user cancels dialog
				if(builder.isCancelValue()) {
					duplicates.clear();
				}
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append("Result of import from file (").append(file.getAbsolutePath()).append("):\n"); //$NON-NLS-1$ //$NON-NLS-2$
			if(importResult.getUnavailableTreebankCount()>0) {
				for(TreebankInfo info : importResult.getUnavailableTreebanks()) {
					sb.append("  -skipped ").append(info.fullInfo()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			
			int errorCount = 0;
			
			// Add new treebank descriptors
			Map<TreebankInfo, TreebankDescriptor> newTreebanks = importResult.getAvailabeTreebanks();
			for(Entry<TreebankInfo, TreebankDescriptor> entry : newTreebanks.entrySet()) {
				TreebankInfo info = entry.getKey();
				TreebankDescriptor descriptor = entry.getValue();
				
				sb.append(" -importing ").append(info.fullInfo()); //$NON-NLS-1$
				
				// Rename if required
				if(duplicates.contains(info)) {
					descriptor.setName(TreebankRegistry.getInstance()
							.getUniqueName(info.getTreebankName()));
					sb.append(" (renamed to '").append(descriptor.getName()); //$NON-NLS-1$
				}
				
				sb.append("\n"); //$NON-NLS-1$
				
				try {
					TreebankRegistry.getInstance().addTreebank(descriptor);
					sb.append("  ok"); //$NON-NLS-1$
				} catch (Exception ex) {
					errorCount++;
					sb.append("  error"); //$NON-NLS-1$
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to add new treebank: "+info.fullInfo(), ex); //$NON-NLS-1$
				}
			}
			
			LoggerFactory.log(this, Level.INFO, sb.toString());
			
			// Present result of import operation
			DialogFactory.getGlobalFactory().showInfo(getFrame(), 
					"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.title", //$NON-NLS-1$
					"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.result", //$NON-NLS-1$
					importResult.getAvailableTreebankCount()+errorCount,
					duplicates.size(),
					importResult.getUnavailableTreebankCount(),
					errorCount);
		}
	}
}
