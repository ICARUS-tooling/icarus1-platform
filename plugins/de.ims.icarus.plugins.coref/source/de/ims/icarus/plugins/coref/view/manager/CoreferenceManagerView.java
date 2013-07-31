/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.manager;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.language.coref.registry.AllocationEditor;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.language.coref.registry.DocumentSetEditor;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.coref.CorefConstants;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceManagerView extends View {
	
	private JTree tree;
	private CoreferenceRegistryTreeModel treeModel;
	
	private Handler handler;
	private CallbackHandler callbackHandler;
	
	private JPopupMenu popupMenu;

	public CoreferenceManagerView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		if(!defaultLoadActions(CoreferenceManagerView.class, "coreference-manager-view-actions.xml")) { //$NON-NLS-1$
			return;
		}
		
		handler = new Handler();

		container.setLayout(new BorderLayout());
		
		treeModel = new CoreferenceRegistryTreeModel();
		tree = new JTree(treeModel);
		tree.setBorder(UIUtil.defaultContentBorder);
		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		selectionModel.addTreeSelectionListener(handler);
		tree.setSelectionModel(selectionModel);
		tree.setCellRenderer(new CoreferenceTreeCellRenderer());
		UIUtil.enableRighClickTreeSelection(tree);
		tree.addMouseListener(handler);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setBorder(UIUtil.topLineBorder);
		container.add(scrollPane, BorderLayout.CENTER);

		Options options = new Options("multiline", true); //$NON-NLS-1$
		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"plugins.coref.coreferenceManagerView.toolBarList", options); //$NON-NLS-1$
		container.add(toolBar, BorderLayout.NORTH);
		
		registerActionCallbacks();
		refreshActions();
	}
	
	@Override
	public void reset() {
		UIUtil.expandAll(tree, false);
	}

	@Override
	public void close() {
		super.close();
		
		treeModel.close();
	}
	
	@Override
	public boolean isClosable() {
		return true;
	}

	private void refreshActions() {
		Object selectedObject = getSelectedObject();
		
		boolean hasSelection = selectedObject!=null;
		boolean isDocumentSet = hasSelection && selectedObject instanceof DocumentSetDescriptor;
		boolean isAllocation = hasSelection && selectedObject instanceof AllocationDescriptor;
		boolean isDocumentSetPath = getSelectedDocumentSet()!=null;
		
		ActionManager actionManager = getDefaultActionManager();
		actionManager.setEnabled(isDocumentSet, 
				"plugins.coref.coreferenceManagerView.deleteDocumentSetAction", //$NON-NLS-1$
				"plugins.coref.coreferenceManagerView.editDocumentSetAction", //$NON-NLS-1$
				"plugins.coref.coreferenceManagerView.inspectDocumentSetAction"); //$NON-NLS-1$
		actionManager.setEnabled(isDocumentSetPath, 
				"plugins.coref.coreferenceManagerView.addAllocationAction"); //$NON-NLS-1$
		actionManager.setEnabled(isAllocation, 
				"plugins.coref.coreferenceManagerView.deleteAllocationAction", //$NON-NLS-1$
				"plugins.coref.coreferenceManagerView.editAllocationAction"); //$NON-NLS-1$
	}
	
	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.addHandler("plugins.coref.coreferenceManagerView.expandAllAction",  //$NON-NLS-1$
				callbackHandler, "expandAll"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceManagerView.collapseAllAction",  //$NON-NLS-1$
				callbackHandler, "collapseAll"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.coref.coreferenceManagerView.addDocumentSetAction",  //$NON-NLS-1$
				callbackHandler, "addDocumentSet"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceManagerView.deleteDocumentSetAction",  //$NON-NLS-1$
				callbackHandler, "deleteDocumentSet"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceManagerView.editDocumentSetAction",  //$NON-NLS-1$
				callbackHandler, "editDocumentSet"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceManagerView.inspectDocumentSetAction",  //$NON-NLS-1$
				callbackHandler, "inspectDocumentSet"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.coref.coreferenceManagerView.addAllocationAction",  //$NON-NLS-1$
				callbackHandler, "addAllocation"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceManagerView.deleteAllocationAction",  //$NON-NLS-1$
				callbackHandler, "deleteAllocation"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceManagerView.editAllocationAction",  //$NON-NLS-1$
				callbackHandler, "editAllocation"); //$NON-NLS-1$
	}
	
	private void showPopup(MouseEvent trigger) {
		if(popupMenu==null) {
			// Create new popup menu
			
			popupMenu = getDefaultActionManager().createPopupMenu(
					"plugins.coref.coreferenceManagerView.popupMenuList", null); //$NON-NLS-1$
			
			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create popup menu"); //$NON-NLS-1$
			}
		}
		
		if(popupMenu!=null) {			
			popupMenu.show(tree, trigger.getX(), trigger.getY());
		}
	}
	
	private Object getSelectedObject() {
		TreePath treePath = tree.getSelectionPath();
		return treePath==null ? null : treePath.getLastPathComponent();
	}
	
	@SuppressWarnings("unused")
	private Object[] getSelectionPath() {
		TreePath treePath = tree.getSelectionPath();
		return treePath==null ? null : treePath.getPath();
	}
	
	private DocumentSetDescriptor getSelectedDocumentSet() {
		TreePath treePath = tree.getSelectionPath();
		if(treePath==null) {
			return null;
		}
		Object[] path = treePath.getPath();
		if(path==null) {
			return null;
		}
		for(Object element : path) {
			if(element instanceof DocumentSetDescriptor) {
				return (DocumentSetDescriptor)element;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private AllocationDescriptor getSelectedAllocation() {
		TreePath treePath = tree.getSelectionPath();
		if(treePath==null) {
			return null;
		}
		Object[] path = treePath.getPath();
		if(path==null) {
			return null;
		}
		for(Object element : path) {
			if(element instanceof AllocationDescriptor) {
				return (AllocationDescriptor)element;
			}
		}
		return null;
	}

	private class Handler extends MouseAdapter implements TreeSelectionListener {
		
		private void maybeShowPopup(MouseEvent e) {
			if(e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
				callbackHandler.inspectDocumentSet(null);
			}
		}

		/**
		 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
		 */
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			refreshActions();
		}
	}
	
	public class CallbackHandler {
		
		protected CallbackHandler() {
			// no-op
		}
		
		public void expandAll(ActionEvent e) {
			try {
				UIUtil.expandAll(tree, true);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to expand entire registry tree", ex); //$NON-NLS-1$
				
				UIUtil.beep();
				showError(ex);
			}
		}
		
		public void collapseAll(ActionEvent e) {
			try {
				UIUtil.expandAll(tree, false);
				tree.expandPath(new TreePath(tree.getModel().getRoot()));
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to collapse entire registry tree", ex); //$NON-NLS-1$
				
				UIUtil.beep();
				showError(ex);
			}
		}
		
		public void addDocumentSet(ActionEvent e) {
			try {
				String name = "New Document-Set"; //$NON-NLS-1$
				name = CoreferenceRegistry.getInstance().getUniqueDocumentSetName(name);
				
				CoreferenceRegistry.getInstance().newDocumentSet(name);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to add document set", ex); //$NON-NLS-1$
				
				UIUtil.beep();
				showError(ex);
			}
		}
		
		public void deleteDocumentSet(ActionEvent e) {
			try {
				Object selectedObject = getSelectedObject();
				if(selectedObject==null 
						|| !(selectedObject instanceof DocumentSetDescriptor)) {
					return;
				}
				
				DocumentSetDescriptor descriptor = (DocumentSetDescriptor)selectedObject;
				
				if(!DialogFactory.getGlobalFactory().showConfirm(
						getFrame(), 
						"plugins.coref.coreferenceManagerView.dialogs.documentSet.deleteTitle",  //$NON-NLS-1$
						"plugins.coref.coreferenceManagerView.dialogs.documentSet.confirmDelete",  //$NON-NLS-1$
						descriptor.getName())) {
					return;
				}
				
				CoreferenceRegistry.getInstance().deleteDocumentSet(descriptor);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to delete document set", ex); //$NON-NLS-1$
				
				UIUtil.beep();
				showError(ex);
			}
		}
		
		public void editDocumentSet(ActionEvent e) {
			DocumentSetEditor editor = null;
			try {
				Object selectedObject = getSelectedObject();
				if(selectedObject==null 
						|| !(selectedObject instanceof DocumentSetDescriptor)) {
					return;
				}
				
				DocumentSetDescriptor descriptor = (DocumentSetDescriptor) selectedObject;
				editor = new DocumentSetEditor();
				
				DialogFactory.getGlobalFactory().showEditorDialog(
						getFrame(), descriptor, editor, 
						"plugins.coref.coreferenceManagerView.dialogs.documentSet.title"); //$NON-NLS-1$
				
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to edit document set", ex); //$NON-NLS-1$
				
				UIUtil.beep();
				showError(ex);
			} finally {
				if(editor!=null) {
					editor.close();
				}
			}
		}
		
		public void inspectDocumentSet(ActionEvent e) {
			try {
				Object selectedObject = getSelectedObject();
				if(selectedObject==null 
						|| !(selectedObject instanceof DocumentSetDescriptor)) {
					return;
				}
				
				DocumentSetDescriptor descriptor = (DocumentSetDescriptor) selectedObject;
				Message message = new Message(this, Commands.DISPLAY, descriptor, null);
				
				sendRequest(CorefConstants.COREFERENCE_EXPLORER_VIEW_ID, message);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to inspect document set", ex); //$NON-NLS-1$
				
				UIUtil.beep();
				showError(ex);
			}
		}
		
		public void addAllocation(ActionEvent e) {
			try {
				DocumentSetDescriptor descriptor = getSelectedDocumentSet();
				if(descriptor==null) {
					return;
				}

				String name = "New Allocation"; //$NON-NLS-1$
				name = CoreferenceRegistry.getInstance().getUniqueAllocationName(name);
				
				CoreferenceRegistry.getInstance().newAllocation(name, descriptor);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to add allocation", ex); //$NON-NLS-1$
				
				UIUtil.beep();
				showError(ex);
			}
		}
		
		public void deleteAllocation(ActionEvent e) {
			try {
				Object selectedObject = getSelectedObject();
				if(selectedObject==null 
						|| !(selectedObject instanceof AllocationDescriptor)) {
					return;
				}
				
				CoreferenceRegistry.getInstance().deleteAllocation(
						(AllocationDescriptor) selectedObject);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to delete allocation", ex); //$NON-NLS-1$
				
				UIUtil.beep();
				showError(ex);
			}
		}
		
		public void editAllocation(ActionEvent e) {
			AllocationEditor editor = null;
			try {
				Object selectedObject = getSelectedObject();
				if(selectedObject==null 
						|| !(selectedObject instanceof AllocationDescriptor)) {
					return;
				}
				
				AllocationDescriptor descriptor = (AllocationDescriptor) selectedObject;
				editor = new AllocationEditor();
				
				DialogFactory.getGlobalFactory().showEditorDialog(
						getFrame(), descriptor, editor, 
						"plugins.coref.coreferenceManagerView.dialogs.allocation.title"); //$NON-NLS-1$
				
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to edit allocation", ex); //$NON-NLS-1$
				
				UIUtil.beep();
				showError(ex);
			} finally {
				if(editor!=null) {
					editor.close();
				}
			}
		}
	}
}
