package net.ikarus_systems.icarus.plugins.weblicht;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import org.java.plugin.registry.Extension;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.Location;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.opi.Commands;
import net.ikarus_systems.icarus.util.opi.Message;
import net.ikarus_systems.icarus.util.opi.ResultMessage;

public class WeblichtChainView extends View {

	private JTree weblichtTree;

	private JPopupMenu popupMenu;

	private Handler handler;
	private CallbackHandler callbackHandler;

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
			LoggerFactory.getLogger(WeblichtChainView.class).log(
					LoggerFactory.record(Level.SEVERE,
							"Failed to load actions from file", e)); //$NON-NLS-1$
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
		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel
				.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
		weblichtTree.setSelectionModel(selectionModel);
		weblichtTree.addTreeSelectionListener(handler);
		weblichtTree.addMouseListener(handler);
		weblichtTree.getModel().addTreeModelListener(handler);
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
		// TODO Auto-generated method stub

	}

	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		// TODO Auto-generated method stub
		return null;
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
				LoggerFactory.getLogger(WeblichtChainView.class).log(
						LoggerFactory.record(Level.SEVERE,
								"Unable to create popup menu")); //$NON-NLS-1$
			}
		}

		if (popupMenu != null) {
			popupMenu.show(weblichtTree, trigger.getX(), trigger.getY());
		}
	}

	private void refreshActions(Object selectedObject) {
		ActionManager actionManager = getDefaultActionManager();

		actionManager.setEnabled(selectedObject instanceof Webservice
				|| selectedObject instanceof Extension,
				"plugins.weblicht.weblichtChainView.newWebchainAction"); //$NON-NLS-1$

		actionManager.setEnabled(selectedObject instanceof Webservice,
				"plugins.weblicht.weblichtChainView.deleteWebchainAction", //$NON-NLS-1$
				"plugins.weblicht.weblichtChainView.cloneWebchainAction"); //$NON-NLS-1$
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
				callbackHandler.editWeblicht(null);
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
			if (path == null) {
				return;
			}
			weblichtTree.expandPath(path);
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
			if (path == null) {
				return;
			}
			weblichtTree.expandPath(path);
		}
	}

	public final class CallbackHandler {

		private CallbackHandler() {
			// no-op
		}

		public void editWeblicht(Object object) {
			// TODO Auto-generated method stub

		}
	}

}
