/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools.corpus;

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

import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.language.corpus.CorpusDescriptor;
import net.ikarus_systems.icarus.language.corpus.CorpusImportResult;
import net.ikarus_systems.icarus.language.corpus.CorpusInfo;
import net.ikarus_systems.icarus.language.corpus.CorpusRegistry;
import net.ikarus_systems.icarus.language.corpus.DerivedCorpus;
import net.ikarus_systems.icarus.language.corpus.swing.CorpusListCellRenderer;
import net.ikarus_systems.icarus.language.corpus.swing.CorpusTreeCellRenderer;
import net.ikarus_systems.icarus.language.corpus.swing.CorpusTreeModel;
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
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.NamingUtil;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusExplorerView extends View {
	
	private JTree corporaTree;
	
	private JPopupMenu popupMenu;
	
	private Handler handler;
	private CallbackHandler callbackHandler;

	public CorpusExplorerView() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// Load actions
		URL actionLocation = CorpusExplorerView.class.getResource("corpus-explorer-view-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: corpus-explorer-view-actions.xml"); //$NON-NLS-1$
		
		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.getLogger(CorpusExplorerView.class).log(LoggerFactory.record(Level.SEVERE, 
					"Failed to load actions from file", e)); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}
		
		handler = new Handler();		

		// Create and init tree
		corporaTree = new JTree(new CorpusTreeModel());
		UIUtil.enableRighClickTreeSelection(corporaTree);
		corporaTree.setCellRenderer(new CorpusTreeCellRenderer());
		corporaTree.setEditable(false);
		corporaTree.setBorder(UIUtil.defaultContentBorder);
		corporaTree.setLargeModel(true);
		corporaTree.setRootVisible(false);
		corporaTree.setShowsRootHandles(true);	
		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
		corporaTree.setSelectionModel(selectionModel);
		corporaTree.addTreeSelectionListener(handler);		
		corporaTree.addMouseListener(handler);
		corporaTree.getModel().addTreeModelListener(handler);
		UIUtil.expandAll(corporaTree, true);
		
		// Scroll pane
		JScrollPane scrollPane = new JScrollPane(corporaTree);
		scrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		
		// Header
		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"plugins.languageTools.corpusExplorerView.toolBarList", null); //$NON-NLS-1$
		
		container.setLayout(new BorderLayout());
		container.add(toolBar, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		container.setPreferredSize(new Dimension(300, 500));
		container.setMinimumSize(new Dimension(250, 400));
		
		CorpusRegistry.getInstance().addListener(Events.ADDED, handler);
		CorpusRegistry.getInstance().addListener(Events.REMOVED, handler);
		
		registerActionCallbacks();
		refreshActions(null);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		UIUtil.expandAll(corporaTree, false);
		corporaTree.expandPath(new TreePath(corporaTree.getModel().getRoot()));
	}
	
	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#close()
	 */
	@Override
	public void close() {
		CorpusRegistry.getInstance().removeListener(handler);
	}

	@Override
	protected void refreshInfoPanel(InfoPanel infoPanel) {
		infoPanel.addLabel("selectedItem"); //$NON-NLS-1$
		infoPanel.addSeparator();
		infoPanel.addLabel("totalTypes", 70); //$NON-NLS-1$
		infoPanel.addSeparator();
		infoPanel.addLabel("totalCorpora", 70); //$NON-NLS-1$
		infoPanel.addGap(100);
		
		showCorpusInfo();
	}
	
	@Override
	protected void buildToolBar(ToolBarDelegate delegate) {
		delegate.addAction(getDefaultActionManager(), 
				"plugins.languageTools.corpusExplorerView.exportCorporaAction"); //$NON-NLS-1$
		delegate.addAction(getDefaultActionManager(), 
				"plugins.languageTools.corpusExplorerView.importCorporaAction"); //$NON-NLS-1$
	}
	
	private void showCorpusInfo() {
		InfoPanel infoPanel = getInfoPanel();
		if(infoPanel==null) {
			return;
		}
		
		Object selectedItem = getSelectedObject();
		if(selectedItem instanceof Corpus) {
			Corpus corpus = (Corpus) selectedItem;
			CorpusDescriptor descriptor = CorpusRegistry.getInstance().getDescriptor(corpus);
			String text = corpus.getName()+" - "+descriptor.getExtension().getUniqueId(); //$NON-NLS-1$

			infoPanel.displayText("selectedItem", text); //$NON-NLS-1$
		} else {
			infoPanel.displayText("selectedItem", null); //$NON-NLS-1$
		}
		
		// Total corpus types
		String text = ResourceManager.getInstance().get(
				"plugins.languageTools.corpusExplorerView.labels.totalTypes",  //$NON-NLS-1$
				CorpusRegistry.getInstance().availableTypeCount()); 
		infoPanel.displayText("totalTypes", text); //$NON-NLS-1$
		
		// Total corpora
		text = ResourceManager.getInstance().get(
				"plugins.languageTools.corpusExplorerView.labels.totalCorpora",  //$NON-NLS-1$
				CorpusRegistry.getInstance().availableCorporaCount()); 
		infoPanel.displayText("totalCorpora", text); //$NON-NLS-1$
	}
	
	private Object[] getSelectionPath() {
		TreePath path = corporaTree.getSelectionPath();
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
					"plugins.languageTools.corpusExplorerView.popupMenuList", options); //$NON-NLS-1$
			
			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.getLogger(CorpusExplorerView.class).log(LoggerFactory.record(
						Level.SEVERE, "Unable to create popup menu")); //$NON-NLS-1$
			}
		}
		
		if(popupMenu!=null) {			
			popupMenu.show(corporaTree, trigger.getX(), trigger.getY());
		}
	}
	
	private void refreshActions(Object selectedObject) {
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.setEnabled(
				selectedObject instanceof Corpus || selectedObject instanceof Extension, 
				"plugins.languageTools.corpusExplorerView.newCorpusAction");  //$NON-NLS-1$
		
		actionManager.setEnabled(selectedObject instanceof Corpus, 
				"plugins.languageTools.corpusExplorerView.deleteCorpusAction",  //$NON-NLS-1$
				"plugins.languageTools.corpusExplorerView.cloneCorpusAction",  //$NON-NLS-1$
				"plugins.languageTools.corpusExplorerView.renameCorpusAction",  //$NON-NLS-1$
				"plugins.languageTools.corpusExplorerView.openLocationAction",  //$NON-NLS-1$
				"plugins.languageTools.corpusExplorerView.inspectCorpusAction",  //$NON-NLS-1$
				"plugins.languageTools.corpusExplorerView.editCorpusAction",  //$NON-NLS-1$
				"plugins.languageTools.corpusExplorerView.exportCorpusAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(CorpusRegistry.getInstance().availableCorporaCount()>0, 
				"plugins.languageTools.corpusExplorerView.exportCorporaAction"); //$NON-NLS-1$
	}
	
	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		actionManager.addHandler("plugins.languageTools.corpusExplorerView.newCorpusAction",  //$NON-NLS-1$
				callbackHandler, "newCorpus"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.corpusExplorerView.deleteCorpusAction",  //$NON-NLS-1$
				callbackHandler, "deleteCorpus"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.corpusExplorerView.cloneCorpusAction",  //$NON-NLS-1$
				callbackHandler, "cloneCorpus"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.corpusExplorerView.renameCorpusAction",  //$NON-NLS-1$
				callbackHandler, "renameCorpus"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.corpusExplorerView.openLocationAction",  //$NON-NLS-1$
				callbackHandler, "openLocation"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.corpusExplorerView.inspectCorpusAction",  //$NON-NLS-1$
				callbackHandler, "inspectCorpus"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.corpusExplorerView.editCorpusAction",  //$NON-NLS-1$
				callbackHandler, "editCorpus"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.corpusExplorerView.exportCorpusAction",  //$NON-NLS-1$
				callbackHandler, "exportCorpus"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.corpusExplorerView.exportCorporaAction",  //$NON-NLS-1$
				callbackHandler, "exportCorpora"); //$NON-NLS-1$
		actionManager.addHandler("plugins.languageTools.corpusExplorerView.importCorporaAction",  //$NON-NLS-1$
				callbackHandler, "importCorpora"); //$NON-NLS-1$
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
			
			showCorpusInfo();
			
			fireBroadcastEvent(new EventObject(LanguageToolsConstants.CORPUS_EXPLORER_SELECTION_CHANGED, 
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
				callbackHandler.editCorpus(null);
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
			corporaTree.expandPath(path);
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
			corporaTree.expandPath(path);
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			showCorpusInfo();
			refreshActions(getSelectedObject());
		}
	}
	
	public final class CallbackHandler {
		
		private CallbackHandler() {
			// no-op
		}
		
		public void newCorpus(ActionEvent e) {
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
			
			String name = CorpusRegistry.getInstance().getUniqueName("New "+extension.getId()); //$NON-NLS-1$
			try {
				CorpusRegistry.getInstance().newCorpus(extension, name);
			} catch (Exception ex) {
				LoggerFactory.getLogger(CorpusExplorerView.class).log(LoggerFactory.record(Level.SEVERE, 
						"Unable to create new corpus: "+name, ex)); //$NON-NLS-1$
			}
		}
		
		public void deleteCorpus(ActionEvent e) {
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Corpus)) {
				return;
			}
			
			Corpus corpus = (Corpus)selectedObject;
			boolean doDelete = false;
			
			// Special handling for corpora with other corpora derived from them 
			List<DerivedCorpus> derivedCorpora = CorpusRegistry.getInstance().getDerivedCorpora(corpus);
			if(!derivedCorpora.isEmpty()) {
				Collections.sort(derivedCorpora, CorpusRegistry.CORPUS_NAME_COMPARATOR);
				StringBuilder sb = new StringBuilder();
				int count = Math.min(5, derivedCorpora.size());
				for(int i=0; i< count; i++) {
					sb.append(derivedCorpora.get(i).getName()).append("\n"); //$NON-NLS-1$
				}
				if(derivedCorpora.size()>5) {
					sb.append(ResourceManager.getInstance().get(
							"plugins.languageTools.corpusExplorerView.dialogs.hint", //$NON-NLS-1$
							derivedCorpora.size()-5)).append("\n"); //$NON-NLS-1$
				}

				doDelete = DialogFactory.getGlobalFactory().showConfirm(getFrame(), 
						"plugins.languageTools.corpusExplorerView.dialogs.deleteBaseCorpus.title",  //$NON-NLS-1$
						"plugins.languageTools.corpusExplorerView.dialogs.deleteBaseCorpus.message",  //$NON-NLS-1$
						corpus.getName(), sb.toString());
			} else {
				// Handling for regular corpora
				doDelete = DialogFactory.getGlobalFactory().showConfirm(getFrame(), 
						"plugins.languageTools.corpusExplorerView.dialogs.deleteCorpus.title",  //$NON-NLS-1$
						"plugins.languageTools.corpusExplorerView.dialogs.deleteCorpus.message",  //$NON-NLS-1$
						corpus.getName());
			}
			
			if(doDelete) {
				try {
					CorpusRegistry.getInstance().deleteCorpus(corpus);
				} catch(Exception ex) {
					LoggerFactory.getLogger(CorpusExplorerView.class).log(LoggerFactory.record(Level.SEVERE, 
							"Unable to delete corpus: "+corpus.getName(), ex)); //$NON-NLS-1$
				}
			}
		}
		
		public void cloneCorpus(ActionEvent e) {
			Object[] path = getSelectionPath();
			if(path==null || path.length==0) {
				return;
			}
			Object selectedObject = path[path.length-1];
			if(selectedObject==null || !(selectedObject instanceof Corpus)) {
				return;
			}
			
			Corpus corpus = (Corpus)selectedObject;
			
			Extension extension = null;
			for(int i=path.length-1; i>-1; i--) {
				if(path[i] instanceof Extension) {
					extension = (Extension) path[i];
				}
			}
			
			if(extension==null) {
				return;
			}
			
			String name = CorpusRegistry.getInstance().getUniqueName(corpus.getName());
			try {
				CorpusRegistry.getInstance().newCorpus(extension, name);
			} catch (Exception ex) {
				LoggerFactory.getLogger(CorpusExplorerView.class).log(LoggerFactory.record(Level.SEVERE, 
						"Unable to create new corpus: "+name, ex)); //$NON-NLS-1$
			}
		}
		
		public void renameCorpus(ActionEvent e) {
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Corpus)) {
				return;
			}
			
			Corpus corpus = (Corpus)selectedObject;
			
			String currentName = corpus.getName();
			String newName = DialogFactory.getGlobalFactory().showInputDialog(getFrame(), 
					"plugins.languageTools.corpusExplorerView.dialogs.renameCorpus.title",  //$NON-NLS-1$
					"plugins.languageTools.corpusExplorerView.dialogs.renameCorpus.message",  //$NON-NLS-1$
					currentName, currentName);
			
			if(newName==null || newName.isEmpty()) {
				return;
			}
			
			// No changes
			if(currentName.equals(newName)) {
				return;
			}
			
			// Let corpus registry manage naming checks
			String uniqueName = CorpusRegistry.getInstance().getUniqueName(newName);
			if(!uniqueName.equals(newName)) {
				DialogFactory.getGlobalFactory().showInfo(getFrame(), 
						"plugins.languageTools.corpusExplorerView.dialogs.title",  //$NON-NLS-1$
						"plugins.languageTools.corpusExplorerView.dialogs.duplicateName",  //$NON-NLS-1$
						newName, uniqueName);
			}
			
			try {
				CorpusRegistry.getInstance().setName(corpus, uniqueName);
			} catch (Exception ex) {
				LoggerFactory.getLogger(CorpusExplorerView.class).log(LoggerFactory.record(Level.SEVERE, 
						"Unable to rename corpus "+currentName+" to "+uniqueName, ex)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		public void openLocation(ActionEvent e) {
			if(!Desktop.isDesktopSupported()) {
				return;
			}
			
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Corpus)) {
				return;
			}
			
			Corpus corpus = (Corpus)selectedObject;
			Location location = corpus.getLocation();
			if(location==null) {
				return;
			}
			
			try {
				Desktop desktop = Desktop.getDesktop();
				if(location.isLocal()) {
					// Open local corpora in the default explorer
					desktop.open(location.getFile().getParentFile());
				} else {
					// Use the systems browser for remote corpora
					desktop.browse(location.getURL().toURI());
				}
			} catch(Exception ex) {
				LoggerFactory.getLogger(CorpusExplorerView.class).log(LoggerFactory.record(Level.SEVERE, 
						"Unable to open corpus location: "+corpus.getName(), ex)); //$NON-NLS-1$
			}
		}
		
		public void inspectCorpus(ActionEvent e) {	
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Corpus)) {
				return;
			}
			
			Corpus corpus = (Corpus)selectedObject;
			
			Message message = new Message(Commands.DISPLAY, corpus, null);
			sendRequest(LanguageToolsConstants.CORPUS_INSPECT_VIEW_ID, message);
		}
		
		public void editCorpus(ActionEvent e) {			
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Corpus)) {
				return;
			}
			
			Corpus corpus = (Corpus)selectedObject;
			
			Message message = new Message(Commands.EDIT, corpus, null);
			sendRequest(LanguageToolsConstants.CORPUS_EDIT_VIEW_ID, message);
		}
		
		public void exportCorpus(ActionEvent e) {
			// TODO allow user to select location and format
		}

		// TODO switch execution to background task?
		public void exportCorpora(ActionEvent e) {
			
			// Obtain a sorted collection of all available corpora
			List<Corpus> corpora = new ArrayList<>(
					CorpusRegistry.getInstance().availableCorpora());
			Collections.sort(corpora, CorpusRegistry.CORPUS_NAME_COMPARATOR);

			// Create a corpus list with multi-selection enabled
			DefaultListModel<Corpus> model = new DefaultListModel<>();
			for(Corpus corpus : corpora) {
				model.addElement(corpus);
			}
			JList<Corpus> list = new JList<>(model);
			list.setCellRenderer(new CorpusListCellRenderer());
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			
			JScrollPane scrollPane = new JScrollPane(list);
			scrollPane.setPreferredSize(new Dimension(250, 200));
			
			if(!DialogFactory.getGlobalFactory().showGenericDialog(
					getFrame(), 
					"plugins.languageTools.corpusExplorerView.dialogs.exportCorpora.title",  //$NON-NLS-1$
					"plugins.languageTools.corpusExplorerView.dialogs.exportCorpora.selectInfo",  //$NON-NLS-1$
					scrollPane, "ok", "cancel")) { //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			
			int[] indices = list.getSelectedIndices();
			if(indices.length==0) {
				return;
			}
			
			// Collect selected corpora
			corpora.clear();
			for(int index : indices) {
				corpora.add(list.getModel().getElementAt(index));
			}
			
			// Obtain destination file (factory handles the 'overwrite' dialog)
			File file = DialogFactory.getGlobalFactory().showDestinationFileDialog(
					getFrame(), 
					"plugins.languageTools.corpusExplorerView.dialogs.exportCorpora.title",  //$NON-NLS-1$
					null);
			
			if(file==null) {
				return;
			}
			
			try {
				// Perform export
				CorpusRegistry.getInstance().exportCorpora(file, corpora);
			} catch (Exception ex) {
				getLogger().log(LoggerFactory.record(Level.SEVERE, 
						"Failed to export corpora to file: "+file.getAbsolutePath(), ex)); //$NON-NLS-1$
				DialogFactory.getGlobalFactory().showError(getFrame(), 
						"plugins.languageTools.corpusExplorerView.dialogs.exportCorpora.title",  //$NON-NLS-1$
						"plugins.languageTools.corpusExplorerView.dialogs.exportCorpora.ioException",  //$NON-NLS-1$
						NamingUtil.fit(file.getAbsolutePath(), 100),
						NamingUtil.fit(ex.getMessage(), 100));
				return;
			}
			
			// Feedback to user
			DialogFactory.getGlobalFactory().showInfo(getFrame(), 
					"plugins.languageTools.corpusExplorerView.dialogs.exportCorpora.title",  //$NON-NLS-1$
					"plugins.languageTools.corpusExplorerView.dialogs.exportCorpora.result",  //$NON-NLS-1$
					corpora.size(), NamingUtil.fit(file.getAbsolutePath(), 100));
		}
		
		// TODO switch execution to background task?
		public void importCorpora(ActionEvent e) {
			
			// Obtain source file
			File file = DialogFactory.getGlobalFactory().showSourceFileDialog(
					getFrame(), 
					"plugins.languageTools.corpusExplorerView.dialogs.importCorpora.title",  //$NON-NLS-1$
					null);
			
			if(file==null || !file.exists()) {
				return;
			}
			
			CorpusImportResult importResult = null;
			
			// Perform import
			try {
				importResult = CorpusRegistry.getInstance().importCorpora(file);
			} catch (IOException ex) {
				getLogger().log(LoggerFactory.record(Level.SEVERE, 
						"Failed to import corpora from file: "+file.getAbsolutePath(), ex)); //$NON-NLS-1$
				DialogFactory.getGlobalFactory().showError(getFrame(), 
						"plugins.languageTools.corpusExplorerView.dialogs.importCorpora.title",  //$NON-NLS-1$
						"plugins.languageTools.corpusExplorerView.dialogs.importCorpora.ioException",  //$NON-NLS-1$
						NamingUtil.fit(file.getAbsolutePath(), 100),
						NamingUtil.fit(ex.toString(), 100));
				return;
			} catch (Exception ex) {
				getLogger().log(LoggerFactory.record(Level.SEVERE, 
						"Cannot import corpus data from file: "+file.getAbsolutePath(), ex)); //$NON-NLS-1$
				DialogFactory.getGlobalFactory().showError(getFrame(), 
						"plugins.languageTools.corpusExplorerView.dialogs.importCorpora.title",  //$NON-NLS-1$
						"plugins.languageTools.corpusExplorerView.dialogs.importCorpora.invalidContent",  //$NON-NLS-1$
						NamingUtil.fit(file.getAbsolutePath(), 100),
						NamingUtil.fit(ex.toString(), 100));
				return;
			}
			
			// No big deal if we didn't find something to import
			if(importResult==null || importResult.isEmpty()) {
				DialogFactory.getGlobalFactory().showInfo(getFrame(), 
						"plugins.languageTools.corpusExplorerView.dialogs.importCorpora.title",  //$NON-NLS-1$
						"plugins.languageTools.corpusExplorerView.dialogs.importCorpora.noItems");  //$NON-NLS-1$
				return;
			}
			
			// Abort if all corpora are unavailable
			// (no available corpora means all the remaining are unavailable)
			if(importResult.getAvailableCorporaCount()==0) {
				StringBuilder sb = new StringBuilder(300);
				List<CorpusInfo> items = importResult.getUnavailableCorpora();
				int count = Math.min(5, items.size());
				for(int i=0; i<count; i++) {
					sb.append(items.get(i).getCorpusName())
					.append(" - ") //$NON-NLS-1$
					.append(items.get(i).getPluginId())
					.append(" (v") //$NON-NLS-1$
					.append(items.get(i).getPluginVersion())
					.append(")") //$NON-NLS-1$
					.append("\n"); //$NON-NLS-1$
				}
				if(items.size()>5) {
					sb.append(ResourceManager.getInstance().get(
							"plugins.languageTools.corpusExplorerView.dialogs.hint",  //$NON-NLS-1$
							items.size()-5));
				}
				
				DialogFactory.getGlobalFactory().showWarning(getFrame(), 
						"plugins.languageTools.corpusExplorerView.dialogs.importCorpora.title",  //$NON-NLS-1$
						"plugins.languageTools.corpusExplorerView.dialogs.importCorpora.noAvailableItems", //$NON-NLS-1$
						sb.toString());
				return;
			}
			
			// Scan for duplicate names (expensive!!)
			Set<CorpusInfo> duplicates = new LinkedHashSet<>();
			for(CorpusInfo info : importResult.getAvailabeCorpora().keySet()) {
				if(CorpusRegistry.getInstance().getDescriptorByName(info.getCorpusName())!=null) {
					duplicates.add(info);
				}
			}
			
			// Allow user to keep duplicates
			if(!duplicates.isEmpty()) {
				CorpusInfo[] items = duplicates.toArray(
						new CorpusInfo[duplicates.size()]);
				JList<CorpusInfo> list = new JList<>(items);
				list.setFocusable(false);
				list.setCellRenderer(new CorpusListCellRenderer());
				JScrollPane scrollPane = new JScrollPane(list);
				scrollPane.setPreferredSize(new Dimension(250, 200));
				
				BasicDialogBuilder builder = DialogFactory.getGlobalFactory().newBuilder();
				builder.setTitle("plugins.languageTools.corpusExplorerView.dialogs.importCorpora.title"); //$NON-NLS-1$
				builder.setPlainType();
				builder.setMessage("plugins.languageTools.corpusExplorerView.dialogs.importCorpora.duplicatesInfo", duplicates.size()); //$NON-NLS-1$
				builder.addMessage(scrollPane);
				builder.addMessage("plugins.languageTools.corpusExplorerView.dialogs.importCorpora.keepNamesInfo"); //$NON-NLS-1$
				builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
				
				builder.showDialog(getFrame());
				
				// Disable automatic renaming if user cancels dialog
				if(builder.isCancelValue()) {
					duplicates.clear();
				}
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append("Result of import from file (").append(file.getAbsolutePath()).append("):\n"); //$NON-NLS-1$ //$NON-NLS-2$
			if(importResult.getUnavailableCorporaCount()>0) {
				for(CorpusInfo info : importResult.getUnavailableCorpora()) {
					sb.append("  -skipped ").append(info.fullInfo()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			
			int errorCount = 0;
			
			// Add new corpus descriptors
			Map<CorpusInfo, CorpusDescriptor> newCorpora = importResult.getAvailabeCorpora();
			for(Entry<CorpusInfo, CorpusDescriptor> entry : newCorpora.entrySet()) {
				CorpusInfo info = entry.getKey();
				CorpusDescriptor descriptor = entry.getValue();
				
				sb.append(" -importing ").append(info.fullInfo()); //$NON-NLS-1$
				
				// Rename if required
				if(duplicates.contains(info)) {
					descriptor.setName(CorpusRegistry.getInstance()
							.getUniqueName(info.getCorpusName()));
					sb.append(" (renamed to '").append(descriptor.getName()); //$NON-NLS-1$
				}
				
				sb.append("\n"); //$NON-NLS-1$
				
				try {
					CorpusRegistry.getInstance().addCorpus(descriptor);
					sb.append("  ok"); //$NON-NLS-1$
				} catch (Exception ex) {
					errorCount++;
					sb.append("  error"); //$NON-NLS-1$
					getLogger().log(LoggerFactory.record(Level.SEVERE, 
							"Failed to add new corpus: "+info.fullInfo(), ex)); //$NON-NLS-1$
				}
			}
			
			getLogger().log(LoggerFactory.record(Level.INFO, sb.toString()));
			
			// Present result of import operation
			DialogFactory.getGlobalFactory().showInfo(getFrame(), 
					"plugins.languageTools.corpusExplorerView.dialogs.importCorpora.title", //$NON-NLS-1$
					"plugins.languageTools.corpusExplorerView.dialogs.importCorpora.result", //$NON-NLS-1$
					importResult.getAvailableCorporaCount()+errorCount,
					duplicates.size(),
					importResult.getUnavailableCorporaCount(),
					errorCount);
		}
	}
}
