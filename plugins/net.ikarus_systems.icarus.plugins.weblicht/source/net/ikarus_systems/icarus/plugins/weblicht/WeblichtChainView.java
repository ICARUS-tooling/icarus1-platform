package net.ikarus_systems.icarus.plugins.weblicht;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import net.ikarus_systems.icarus.language.SentenceDataList;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.TCFDataList;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebExecutionService;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.Webchain;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebchainRegistry;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.Webservice;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebserviceProxy;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;
import de.tuebingen.uni.sfs.wlf1.io.TextCorpusStreamed;


/**
 * 
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WeblichtChainView extends View {

	private JTree weblichtTree;

	private JPopupMenu popupMenu;

	private Handler handler;
	private CallbackHandler callbackHandler;
	private SwingWorker<TextCorpusStreamed, Void> worker;

	
	@SuppressWarnings("static-access")
	@Override
	public void init(JComponent container) {

		// Load actions
		URL actionLocation = WeblichtChainView.class
				.getResource("weblicht-chain-view-actions.xml"); //$NON-NLS-1$
		if (actionLocation == null)
			throw new CorruptedStateException(
					"Missing resources: weblicht-chain-view-actions.xml"); //$NON-NLS-1$

		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}

		handler = new Handler();

		// Create and init tree
		weblichtTree = new JTree(new WeblichtTreeModel());
		UIUtil.enableRighClickTreeSelection(weblichtTree);
		weblichtTree.setCellRenderer(new WeblichtTreeCellRenderer());
		weblichtTree.setEditable(false);
		weblichtTree.setBorder(UIUtil.defaultContentBorder);
		weblichtTree.setLargeModel(true);
		weblichtTree.setRootVisible(false);
		weblichtTree.setShowsRootHandles(true);
		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
		weblichtTree.setSelectionModel(selectionModel);
		weblichtTree.addTreeSelectionListener(handler);
		weblichtTree.addMouseListener(handler);
		weblichtTree.getModel().addTreeModelListener(handler);
		
		//TODO UIUtil.enableTooltip
		ToolTipManager.sharedInstance().registerComponent(weblichtTree);
		UIUtil.expandAll(weblichtTree, true);

		// Scroll pane
		JScrollPane scrollPane = new JScrollPane(weblichtTree);
		scrollPane.setBorder(null);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		
		// Header
		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"plugins.weblicht.weblichtChainView.toolBarList", null); //$NON-NLS-1$
		toolBar.add(Box.createHorizontalGlue());
		
		// Add Stuff into container
		container.setLayout(new BorderLayout());
		container.add(toolBar, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		container.setPreferredSize(new Dimension(300, 500));
		container.setMinimumSize(new Dimension(250, 400));
		
		registerActionCallbacks();
		refreshActions(null);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isClosable() {
		return true;
	}

	@Override
	public void reset() {
		UIUtil.expandAll(weblichtTree, true);
		weblichtTree.expandPath(new TreePath(weblichtTree.getModel().getRoot()));

	}
	
    public void expandAllNodes() {
    	for (int i = 0; i < weblichtTree.getRowCount(); i++) weblichtTree.expandRow(i);
    }


	// #########

	private Object[] getSelectionPath() {
		TreePath path = weblichtTree.getSelectionPath();
		if (path == null) {
			return null;
		}
		Object[] items = path.getPath();
		for (int i = items.length - 1; i > -1; i--) {
			if (items[i] instanceof DefaultMutableTreeNode) {
				items[i] = ((DefaultMutableTreeNode) items[i]).getUserObject();
			}
		}

		return items;
	}

	private Object getSelectedObject() {
		Object[] path = getSelectionPath();
		if (path == null || path.length == 0) {
			return null;
		}
		return path[path.length - 1];
	}

	private void showPopup(MouseEvent trigger) {
		if (popupMenu == null) {
			// Create new popup menu

			Options options = new Options();
			popupMenu = getDefaultActionManager()
					.createPopupMenu(
							"plugins.weblicht.weblichtChainView.popupMenuList", options); //$NON-NLS-1$

			if (popupMenu != null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE,
						"Unable to create popup menu"); //$NON-NLS-1$
			}
		}

		if (popupMenu != null) {
			popupMenu.show(weblichtTree, trigger.getX(), trigger.getY());
		}
	}

	private void refreshActions(Object selectedObject) {
		ActionManager actionManager = getDefaultActionManager();

		actionManager.setEnabled(selectedObject instanceof Webchain,
				"plugins.weblicht.weblichtChainView.deleteWebchainAction", //$NON-NLS-1$
				"plugins.weblicht.weblichtChainView.cloneWebchainAction", //$NON-NLS-1$
				"plugins.weblicht.weblichtChainView.renameWebchainAction", //$NON-NLS-1$
				"plugins.weblicht.weblichtChainView.editWebchainAction", //$NON-NLS-1$
				"plugins.weblicht.weblichtChainView.runWebchainAction"); //$NON-NLS-1$);
		
		actionManager.setEnabled(false,
				"plugins.weblicht.weblichtChainView.stopWebchainAction"); //$NON-NLS-1$

		actionManager.setEnabled((selectedObject instanceof WebserviceProxy),
			"plugins.weblicht.weblichtChainView.webserviceInfo"); //$NON-NLS-1$
		
	}
	
	private String readInputFromFile(String filename) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String result;
	    try {			
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();	        

	        while (line != null) {
	            sb.append(line).append("\n"); //$NON-NLS-1$
	            line = br.readLine();
	        }
	        result = sb.toString();
	    } finally {	    	
	        br.close();
	    }
	    return result;
		
		
	}
	
	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		actionManager.addHandler("plugins.weblicht.weblichtChainView.saveWebchainAction",  //$NON-NLS-1$
				callbackHandler, "saveWebchain"); //$NON-NLS-1$
		actionManager.addHandler("plugins.weblicht.weblichtChainView.newWebchainAction",  //$NON-NLS-1$
				callbackHandler, "newWebchain"); //$NON-NLS-1$
		actionManager.addHandler("plugins.weblicht.weblichtChainView.deleteWebchainAction",  //$NON-NLS-1$
				callbackHandler, "deleteWebchain"); //$NON-NLS-1$
		actionManager.addHandler("plugins.weblicht.weblichtChainView.cloneWebchainAction",  //$NON-NLS-1$
				callbackHandler, "cloneWebchain"); //$NON-NLS-1$
		actionManager.addHandler("plugins.weblicht.weblichtChainView.renameWebchainAction",  //$NON-NLS-1$
				callbackHandler, "renameWebchain"); //$NON-NLS-1$
		actionManager.addHandler("plugins.weblicht.weblichtChainView.editWebchainAction",  //$NON-NLS-1$
				callbackHandler, "editWebchain"); //$NON-NLS-1$
		actionManager.addHandler("plugins.weblicht.weblichtChainView.runWebchainAction",  //$NON-NLS-1$
				callbackHandler, "runWebchain"); //$NON-NLS-1$
		actionManager.addHandler("plugins.weblicht.weblichtChainView.stopWebchainAction",  //$NON-NLS-1$
				callbackHandler, "stopWebchain"); //$NON-NLS-1$
		actionManager.addHandler("plugins.weblicht.weblichtChainView.webserviceInfo",  //$NON-NLS-1$
				callbackHandler, "webserviceInfo"); //$NON-NLS-1$
	}
	

	private class Handler extends MouseAdapter implements
			TreeSelectionListener, TreeModelListener {

		/**
		 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
		 */
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			Object[] path = getSelectionPath();
			Object selectedObject = (path == null || path.length == 0) ? null
					: path[path.length - 1];

			refreshActions(selectedObject);

			fireBroadcastEvent(new EventObject(
					WeblichtConstants.WEBLICHT_CHAIN_VIEW_CHANGED,
					"item", selectedObject, "path", path)); //$NON-NLS-1$ //$NON-NLS-2$
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
				// Note: we rely on the fact, that our callback handler
				// does not use the supplied ActionEvent object, so we pass
				// null.
				// If this ever changes we could run into some trouble!
				callbackHandler.editWebchain(null);
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
			UIUtil.expandAll(weblichtTree, true);
		}

		/**
		 * @see javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event.TreeModelEvent)
		 */
		@Override
		public void treeNodesInserted(TreeModelEvent e) {
			TreePath path = e.getTreePath();			
			if (path == null) {
				return;
			}
			//weblichtTree.expandPath(path);
			//System.out.println("Inserted " + path);
			UIUtil.expandAll(weblichtTree, true);
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

			System.out.println("Changed " + path);
			if (path == null) {
				return;
			}
			//weblichtTree.expandPath(path);
			weblichtTree.getComponentCount();
			UIUtil.expandAll(weblichtTree, true);
		}
		

	}

	public final class CallbackHandler {

		private CallbackHandler() {
			// no-op
		}
		
		/**
		 * 
		 * @param e
		 */
		public void newWebchain(ActionEvent e) {
			String name = null;
			/*
			 *
			name = DialogFactory.getGlobalFactory().showInputDialog(getFrame(),
					"plugins.weblicht.weblichtChainView.dialogs.addWebchain.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtChainView.dialogs.addWebchain.message", //$NON-NLS-1$
					name, null);*/
			
			Webchain webchain = WebserviceDialogs.getWebserviceDialogFactory().showNewWebchain(getFrame(),
					"plugins.weblicht.weblichtChainView.dialogs.addWebchain.title", //$NON-NLS-1$
					"plugins.weblicht.weblichtChainView.dialogs.addWebchain.message", //$NON-NLS-1$
					null, null);
			
			//canceled by user
			if (webchain == null){
				return;
			}
			//name = WebchainRegistry.getInstance().getUniqueName("New "+name); //$NON-NLS-1$
			
			try {
				WebchainRegistry.getInstance().addNewWebchain(webchain);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to create new webchain: "+name, ex); //$NON-NLS-1$
			}		
			
		}
		
		/**
		 * 
		 * @param e
		 */
		public void deleteWebchain(ActionEvent e) {
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Webchain)) {
				return;
			}
			
			Webchain webchain = (Webchain)selectedObject;
			boolean doDelete = false;
			
			doDelete = DialogFactory.getGlobalFactory().showConfirm(getFrame(), 
					"plugins.weblicht.weblichtChainView.dialogs.deleteWebchain.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtChainView.dialogs.deleteWebchain.message",  //$NON-NLS-1$
					webchain.getName());
			
			if(doDelete) {
				try {
					WebchainRegistry.getInstance().deleteWebchain(webchain);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Unable to delete webchain: "+webchain.getName(), ex); //$NON-NLS-1$
				}
			}
			
		}
		
		/**
		 * 
		 * @param e
		 */
		public void cloneWebchain(ActionEvent e) {
			
			Object[] path = getSelectionPath();
			if(path==null || path.length==0) {
				return;
			}
			Object selectedObject = path[path.length-1];
			if(selectedObject==null || !(selectedObject instanceof Webchain)) {
				return;
			}
			
			Webchain webchainToClone = (Webchain)selectedObject;
			
			//Dialog clone Yes/No ?!			
			if (!DialogFactory
					.getGlobalFactory()
					.showConfirm(
							null,
							"plugins.weblicht.weblichtChainView.dialogs.cloneWebchain.title", //$NON-NLS-1$
							"plugins.weblicht.weblichtChainView.dialogs.cloneWebchain.message", //$NON-NLS-1$
							webchainToClone.getName())) {
				return;
			};

			
			String name = WebchainRegistry.getInstance().getUniqueName(webchainToClone.getName());
			try {
				WebchainRegistry.getInstance().cloneWebchain(name, webchainToClone);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to clone webchain: "+name, ex); //$NON-NLS-1$
			}
			
		}
		
		/**
		 * 
		 * @param e
		 */
		public void editWebchain(ActionEvent e) {
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Webchain)) {
				return;
			}
			
			if(selectedObject instanceof Webchain){
			
				Webchain webchain = (Webchain)selectedObject;
			
				Message message = new Message(this, Commands.EDIT, webchain, null);
				sendRequest(WeblichtConstants.WEBLICHT_EDIT_VIEW_ID, message);
			}

		}
		
		/**
		 * 
		 * @param e
		 */
		public void webserviceInfo(ActionEvent e) {
			Object selectedObject = getSelectedObject();
			
			if(selectedObject==null || !(selectedObject instanceof WebserviceProxy)) {
				return;
			}
			
			WebserviceProxy webserviceproxy = (WebserviceProxy)selectedObject;
			Webservice webservice = webserviceproxy.get();
			//System.out.println(webservice.getName() + " " + webservice.getUID());
			
			Message message = new Message(this, Commands.DISPLAY, webservice, null);
			sendRequest(WeblichtConstants.WEBSERVICE_EDIT_VIEW_ID, message);
		}

		/**
		 * 
		 * @param e
		 */
		public void renameWebchain(ActionEvent e) {
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Webchain)) {
				return;
			}
			
			Webchain webchain = (Webchain)selectedObject;
			
			String currentName = webchain.getName();
			String newName = DialogFactory.getGlobalFactory().showInputDialog(getFrame(), 
					"plugins.weblicht.weblichtChainView.dialogs.renameWebchain.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtChainView.dialogs.renameWebchain.message",  //$NON-NLS-1$
					currentName, currentName);
			
			if(newName==null || newName.isEmpty()) {
				return;
			}
			
			if(currentName.equals(newName)) {
				return;
			}
			
			try {
				WebchainRegistry.getInstance().setName(webchain, newName);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to rename treebank "+currentName+" to "+newName, ex); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		}
		
		public void runWebchain(ActionEvent e){
			Object selectedObject = getSelectedObject();
			if(selectedObject==null || !(selectedObject instanceof Webchain)) {
				return;
			}
			
			final Webchain webchain = (Webchain)selectedObject;
			
			String inputType = webchain.getWebchainInputType().getInputType();
			String inputText = null;
			if (inputType.equals("static")) { //$NON-NLS-1$
				inputText = webchain.getWebchainInputType().getInputTypeValue();				
			}
			if (inputType.equals("location")) { //$NON-NLS-1$
				String filename = webchain.getWebchainInputType().getInputTypeValue();
				try {
					inputText = readInputFromFile(filename);
				} catch (Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed open File " + filename //$NON-NLS-1$
							+ " " + webchain.getName(), ex); //$NON-NLS-1$;
				}
							
			}
			if (inputType.equals("dynamic")) { //$NON-NLS-1$
				//TODO grab input from UI
				inputText = "Karin fliegt nach New York. Sie will dort Urlaub machen"; //$NON-NLS-1$				
			}

			
			/*
			String input = DialogFactory.getGlobalFactory().showTextInputDialog(getFrame(),
					"plugins.weblicht.weblichtChainView.dialogs.inputTextForWebchain.title", //$NON-NLS-1$
					"plugins.weblicht.weblichtChainView.dialogs.inputTextForWebchain.message", //$NON-NLS-1$
					webchain.getName(),test);
			*/
			
			//empty input will cause error in execusion
			if(inputText==null || inputText.isEmpty()) {
				DialogFactory.getGlobalFactory().showError(getFrame(),
						"plugins.weblicht.weblichtChainView.dialogs.inputTextEmpty.title", //$NON-NLS-1$
						"plugins.weblicht.weblichtChainView.dialogs.inputTextEmpty.message", //$NON-NLS-1$
						webchain.getName(),null);
				return;
			}

			
			try {
				// WebExecutionService.getInstance().runWebchain(webchain, inputText);
				final String input = inputText;

				// Construct a new SwingWorker
				worker = new SwingWorker<TextCorpusStreamed, Void>() {					

					@Override
					protected TextCorpusStreamed doInBackground() {
						return WebExecutionService.getInstance().runWebchain(
								webchain, input);
					}

					@Override
					protected void done() {
						try {

							ContentType contentType = ContentTypeRegistry
									.getInstance().getTypeForClass(
											SentenceDataList.class);

							Options options = new Options();
							options.put(Options.CONTENT_TYPE, contentType);
							// TODO send some kind of hint that we want the
							// presenter not to modify content?
							// -> Should be no problem since we only contain
							// immutable data objects?
							TCFDataList tcfList = new TCFDataList(get());

							Message message = new Message(this,
									Commands.DISPLAY, tcfList, options);
							sendRequest(null, message);
							
							System.out.println("Worker " + isCancelled() + isDone());
							
							if (isDone()) {
								//ActionManager.globalManager()
								
							}
							

							//System.out.println("Finished Executino / disable break option"); //$NON-NLS-1$
						} catch (InterruptedException e) {
							LoggerFactory
									.log(this,
											Level.SEVERE,
											"Execution Interrupted " + webchain.getName(), e); //$NON-NLS-1$
						} catch (ExecutionException e) {
							LoggerFactory
									.log(this,
											Level.SEVERE,
											"Execute Exception " + webchain.getName(), e); //$NON-NLS-1$
						}
					}
				};
				// Execute the SwingWorker; the GUI will not freeze
				worker.execute();

			} catch (Exception ex) {
				LoggerFactory
						.log(this,
								Level.SEVERE,
								"Failed to execute chain list " + webchain.getName(), ex); //$NON-NLS-1$

			}

		}
		
		public void stopWebchain(ActionEvent e) {
			//TODO nice stop feature close webservice
			worker.cancel(true);
		}
		
		public void saveWebchain(ActionEvent e) {
			try {
				WebchainRegistry.getInstance().saveWebchains();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Unable to save Webchains: ", ex); //$NON-NLS-1$
			}			
		}
		
	}

}
