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

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.io.Loadable;
import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.language.coref.registry.AllocationEditor;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.language.coref.registry.DocumentSetEditor;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.coref.CorefConstants;
import de.ims.icarus.plugins.coref.view.properties.PropertyInfoDialog;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.mem.AssessmentWorker;
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

		if(!defaultLoadActions(CoreferenceManagerView.class, "coreference-manager-view-actions.xml")) //$NON-NLS-1$
			return;

		container.setLayout(new BorderLayout());

		treeModel = new CoreferenceRegistryTreeModel();
		tree = new JTree(treeModel);
		tree.setBorder(UIUtil.defaultContentBorder);
		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		selectionModel.addTreeSelectionListener(getHandler());
		tree.setSelectionModel(selectionModel);
		tree.setCellRenderer(new CoreferenceTreeCellRenderer());
		UIUtil.enableRighClickTreeSelection(tree);
		tree.addMouseListener(getHandler());
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setLargeModel(true);

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

	private Handler getHandler() {
		if (handler==null) {
			handler = new Handler();
		}

		return handler;
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

		boolean canLoadDocumentSet = isDocumentSet && CoreferenceRegistry.canLoad((DocumentSetDescriptor) selectedObject);
		boolean canFreeDocumentSet = isDocumentSet && IOUtil.canFree((Loadable) selectedObject);

		boolean canLoadAllocation = isAllocation && CoreferenceRegistry.canLoad((AllocationDescriptor) selectedObject);
		boolean canFreeAllocation = isAllocation && IOUtil.canFree((Loadable) selectedObject);

		ActionManager actionManager = getDefaultActionManager();
		actionManager.setEnabled(isDocumentSet,
				"plugins.coref.coreferenceManagerView.deleteDocumentSetAction", //$NON-NLS-1$
				"plugins.coref.coreferenceManagerView.editDocumentSetAction", //$NON-NLS-1$
				"plugins.coref.coreferenceManagerView.inspectDocumentSetAction"); //$NON-NLS-1$
		actionManager.setEnabled(canLoadDocumentSet,
				"plugins.coref.coreferenceManagerView.loadDocumentSetAction"); //$NON-NLS-1$
		actionManager.setEnabled(canFreeDocumentSet,
				"plugins.coref.coreferenceManagerView.assessDocumentSetAction", //$NON-NLS-1$
				"plugins.coref.coreferenceManagerView.freeDocumentSetAction"); //$NON-NLS-1$
		actionManager.setEnabled(isDocumentSetPath,
				"plugins.coref.coreferenceManagerView.addAllocationAction"); //$NON-NLS-1$
		actionManager.setEnabled(isAllocation,
				"plugins.coref.coreferenceManagerView.deleteAllocationAction", //$NON-NLS-1$
				"plugins.coref.coreferenceManagerView.editAllocationAction"); //$NON-NLS-1$
		actionManager.setEnabled(canLoadAllocation,
				"plugins.coref.coreferenceManagerView.loadAllocationAction"); //$NON-NLS-1$
		actionManager.setEnabled(canFreeAllocation,
				"plugins.coref.coreferenceManagerView.assessAllocationAction", //$NON-NLS-1$
				"plugins.coref.coreferenceManagerView.freeAllocationAction"); //$NON-NLS-1$
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
		actionManager.addHandler("plugins.coref.coreferenceManagerView.loadDocumentSetAction",  //$NON-NLS-1$
				callbackHandler, "loadDocumentSet"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceManagerView.freeDocumentSetAction",  //$NON-NLS-1$
				callbackHandler, "freeDocumentSet"); //$NON-NLS-1$

		actionManager.addHandler("plugins.coref.coreferenceManagerView.addAllocationAction",  //$NON-NLS-1$
				callbackHandler, "addAllocation"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceManagerView.deleteAllocationAction",  //$NON-NLS-1$
				callbackHandler, "deleteAllocation"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceManagerView.editAllocationAction",  //$NON-NLS-1$
				callbackHandler, "editAllocation"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceManagerView.loadAllocationAction",  //$NON-NLS-1$
				callbackHandler, "loadAllocation"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceManagerView.freeAllocationAction",  //$NON-NLS-1$
				callbackHandler, "freeAllocation"); //$NON-NLS-1$

		actionManager.addHandler("plugins.coref.coreferenceManagerView.showPropertyDialogAction",  //$NON-NLS-1$
				callbackHandler, "showPropertyDialog"); //$NON-NLS-1$

		actionManager.addHandler("plugins.coref.coreferenceManagerView.assessDocumentSetAction",  //$NON-NLS-1$
				callbackHandler, "assessDocumentSet"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceManagerView.assessAllocationAction",  //$NON-NLS-1$
				callbackHandler, "assessAllocation"); //$NON-NLS-1$
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
			refreshActions();
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
		if(treePath==null)
			return null;
		Object[] path = treePath.getPath();
		if(path==null)
			return null;
		for(Object element : path) {
			if(element instanceof DocumentSetDescriptor)
				return (DocumentSetDescriptor)element;
		}
		return null;
	}

	private AllocationDescriptor getSelectedAllocation() {
		TreePath treePath = tree.getSelectionPath();
		if(treePath==null)
			return null;
		Object[] path = treePath.getPath();
		if(path==null)
			return null;
		for(Object element : path) {
			if(element instanceof AllocationDescriptor)
				return (AllocationDescriptor)element;
		}
		return null;
	}

	private void selectPath(Object... items) {
		TreePath path = new TreePath(treeModel.getRoot());
		for(Object item : items) {
			path = path.pathByAddingChild(item);
		}

		tree.setSelectionPath(path);
	}

	private class Handler extends MouseAdapter implements TreeSelectionListener, Runnable {

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
				Object selectedItem = getSelectedObject();
				if(selectedItem instanceof DocumentSetDescriptor) {
					callbackHandler.editDocumentSet(null);
				} else if(selectedItem instanceof AllocationDescriptor) {
					callbackHandler.editAllocation(null);
				}
			}
		}

		/**
		 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
		 */
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			refreshActions();
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			tree.repaint();
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
			DocumentSetDescriptor descriptor = null;

			try {
				String name = "New Document-Set"; //$NON-NLS-1$
				name = CoreferenceRegistry.getInstance().getUniqueDocumentSetName(name);

				descriptor = CoreferenceRegistry.getInstance().newDocumentSet(name);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to add document set", ex); //$NON-NLS-1$

				UIUtil.beep();
				showError(ex);
			}

			if(descriptor==null)
				return;

			selectPath(descriptor);

			editDocumentSet(null);;
		}

		public void deleteDocumentSet(ActionEvent e) {
			try {
				Object selectedObject = getSelectedObject();
				if(selectedObject==null
						|| !(selectedObject instanceof DocumentSetDescriptor))
					return;

				DocumentSetDescriptor descriptor = (DocumentSetDescriptor)selectedObject;

				if(!DialogFactory.getGlobalFactory().showConfirm(
						getFrame(),
						"plugins.coref.coreferenceManagerView.dialogs.documentSet.deleteTitle",  //$NON-NLS-1$
						"plugins.coref.coreferenceManagerView.dialogs.documentSet.confirmDelete",  //$NON-NLS-1$
						descriptor.getName()))
					return;

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
						|| !(selectedObject instanceof DocumentSetDescriptor))
					return;

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
						|| !(selectedObject instanceof DocumentSetDescriptor))
					return;

				DocumentSetDescriptor descriptor = (DocumentSetDescriptor) selectedObject;

				if(descriptor.getLocation()==null) {
					DialogFactory.getGlobalFactory().showError(getFrame(),
							"plugins.coref.dialogs.errorTitle",  //$NON-NLS-1$
							"plugins.coref.dialogs.missingLocation"); //$NON-NLS-1$
					return;
				}
				if(descriptor.getReaderExtension()==null) {
					DialogFactory.getGlobalFactory().showError(getFrame(),
							"plugins.coref.dialogs.errorTitle",  //$NON-NLS-1$
							"plugins.coref.dialogs.missingReader"); //$NON-NLS-1$
					return;
				}

				Message message = new Message(this, Commands.DISPLAY, descriptor, null);

				sendRequest(CorefConstants.COREFERENCE_EXPLORER_VIEW_ID, message);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to inspect document set", ex); //$NON-NLS-1$

				UIUtil.beep();
				showError(ex);
			}
		}

		public void loadDocumentSet(ActionEvent e) {
			try {

				DocumentSetDescriptor descriptor = getSelectedDocumentSet();

				if(descriptor==null)
					return;

				CoreferenceRegistry.loadDocumentSet(descriptor, getHandler());
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to load document set", ex); //$NON-NLS-1$

				UIUtil.beep();
				showError(ex);
			}
		}

		public void freeDocumentSet(ActionEvent e) {
			try {

				DocumentSetDescriptor descriptor = getSelectedDocumentSet();

				if(descriptor==null)
					return;

				if (!descriptor.isLoaded() || descriptor.isLoading())
					return;

				descriptor.free();

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to free document set", ex); //$NON-NLS-1$

				UIUtil.beep();
				showError(ex);
			}
		}

		public void addAllocation(ActionEvent e) {
			DocumentSetDescriptor descriptor = getSelectedDocumentSet();
			if(descriptor==null)
				return;

			AllocationDescriptor alloc = null;

			try {
				String name = "New Allocation"; //$NON-NLS-1$
				name = CoreferenceRegistry.getInstance().getUniqueAllocationName(
						descriptor, name);

				alloc = CoreferenceRegistry.getInstance().newAllocation(name, descriptor);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to add allocation", ex); //$NON-NLS-1$

				UIUtil.beep();
				showError(ex);
			}

			if(alloc==null)
				return;

			selectPath(descriptor, alloc);

			editAllocation(null);
		}

		public void deleteAllocation(ActionEvent e) {
			try {
				Object selectedObject = getSelectedObject();
				if(selectedObject==null
						|| !(selectedObject instanceof AllocationDescriptor))
					return;

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
						|| !(selectedObject instanceof AllocationDescriptor))
					return;

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

		public void loadAllocation(ActionEvent e) {
			try {

				AllocationDescriptor descriptor = getSelectedAllocation();

				if(descriptor==null)
					return;

				CoreferenceRegistry.loadAllocation(descriptor, getHandler());
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to load allocation", ex); //$NON-NLS-1$

				UIUtil.beep();
				showError(ex);
			}
		}

		public void freeAllocation(ActionEvent e) {
			try {

				AllocationDescriptor descriptor = getSelectedAllocation();

				if(descriptor==null)
					return;

				if (!descriptor.isLoaded() || descriptor.isLoading())
					return;

				descriptor.free();

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to free allocation", ex); //$NON-NLS-1$

				UIUtil.beep();
				showError(ex);
			}
		}

		public void showPropertyDialog(ActionEvent e) {
			try {
				PropertyInfoDialog.showDialog();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to show property dialog", ex); //$NON-NLS-1$

				UIUtil.beep();
				showError(ex);
			}
		}

		public void assessAllocation(ActionEvent e) {

			AllocationDescriptor descriptor = getSelectedAllocation();

			if(descriptor==null || !descriptor.isLoaded()) {
				return;
			}

			TaskManager.getInstance().schedule(new AssessmentWorker(descriptor.get()), TaskPriority.DEFAULT, true);
		}

		public void assessDocumentSet(ActionEvent e) {

			DocumentSetDescriptor descriptor = getSelectedDocumentSet();

			if(descriptor==null || !descriptor.isLoaded()) {
				return;
			}

			TaskManager.getInstance().schedule(new AssessmentWorker(descriptor.get()), TaskPriority.DEFAULT, true);
		}
	}
}
