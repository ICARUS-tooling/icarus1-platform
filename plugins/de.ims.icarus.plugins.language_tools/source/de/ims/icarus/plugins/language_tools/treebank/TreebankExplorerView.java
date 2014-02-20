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
package de.ims.icarus.plugins.language_tools.treebank;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.java.plugin.registry.Extension;

import de.ims.icarus.language.treebank.DerivedTreebank;
import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankDescriptor;
import de.ims.icarus.language.treebank.TreebankEvents;
import de.ims.icarus.language.treebank.TreebankImportResult;
import de.ims.icarus.language.treebank.TreebankInfo;
import de.ims.icarus.language.treebank.TreebankListDelegate;
import de.ims.icarus.language.treebank.TreebankRegistry;
import de.ims.icarus.language.treebank.swing.TreebankListCellRenderer;
import de.ims.icarus.language.treebank.swing.TreebankListModel;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.InfoPanel;
import de.ims.icarus.plugins.core.ToolBarDelegate;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.language_tools.LanguageToolsConstants;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.BasicDialogBuilder;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.ui.list.ListUtils;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.mem.AssessmentWorker;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankExplorerView extends View {

	private JList<Treebank> treebanksList;

	private JPopupMenu popupMenu;

	private Handler handler;
	private LoadTracker loadTracker;
	private CallbackHandler callbackHandler;

	public TreebankExplorerView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {

		// Load actions
		if(!defaultLoadActions(TreebankExplorerView.class, "treebank-explorer-view-actions.xml")) //$NON-NLS-1$
			return;

		handler = new Handler();
		loadTracker = new LoadTracker();

		// Create and init tree
		TreebankListModel model = new TreebankListModel();
		model.setDummyTreebankAllowed(false);
		treebanksList = new JList<>(model);
		UIUtil.enableToolTip(treebanksList);
		UIUtil.enableRighClickListSelection(treebanksList);
		treebanksList.setCellRenderer(new TreebankListCellRenderer());
		treebanksList.setBorder(UIUtil.defaultContentBorder);
		treebanksList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		treebanksList.addListSelectionListener(handler);
		treebanksList.addMouseListener(handler);

		// Scroll pane
		JScrollPane scrollPane = new JScrollPane(treebanksList);
		scrollPane.setBorder(UIUtil.topLineBorder);
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
		refreshActions();
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#close()
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
		if(infoPanel==null)
			return;

		Treebank treebank = treebanksList.getSelectedValue();
		if(treebank!=null) {
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
			popupMenu.show(treebanksList, trigger.getX(), trigger.getY());
		}
	}

	private void refreshActions() {
		Treebank treebank = treebanksList.getSelectedValue();
		ActionManager actionManager = getDefaultActionManager();

		boolean isTreebank = treebank!=null;
		boolean isLoading = isTreebank && treebank.isLoading();
		boolean isLoaded = isTreebank && treebank.isLoaded() && !isLoading;

		actionManager.setEnabled(isTreebank,
				"plugins.languageTools.treebankExplorerView.deleteTreebankAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankExplorerView.cloneTreebankAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankExplorerView.renameTreebankAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankExplorerView.openLocationAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankExplorerView.editTreebankAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankExplorerView.exportTreebankAction"); //$NON-NLS-1$

		actionManager.setEnabled(isTreebank && isLoaded,
				"plugins.languageTools.treebankExplorerView.inspectTreebankAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankExplorerView.assessTreebankAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankExplorerView.freeTreebankAction"); //$NON-NLS-1$

		actionManager.setEnabled(isTreebank && !isLoaded && !isLoading,
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
		actionManager.addHandler("plugins.languageTools.treebankExplorerView.assessTreebankAction",  //$NON-NLS-1$
				callbackHandler, "assessTreebank"); //$NON-NLS-1$
	}

	private class LoadTracker implements EventListener, Runnable {

		private Treebank treebank;

		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			SwingUtilities.invokeLater(this);
		}

		void unregister() {
			if(treebank!=null) {
				treebank.removeListener(this);
			}
		}

		void register(Treebank treebank) {
			if(treebank==this.treebank)
				return;

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

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			refreshActions();
		}
	}

	private class Handler extends MouseAdapter implements ListSelectionListener, EventListener {

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
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			showTreebankInfo();
			refreshActions();
		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			Treebank treebank = ListUtils.getSelectedItem(treebanksList);

			refreshActions();

			if(treebank!=null) {
				loadTracker.register(treebank);
			} else {
				loadTracker.unregister();
			}

			showTreebankInfo();

			fireBroadcastEvent(new EventObject(LanguageToolsConstants.TREEBANK_EXPLORER_SELECTION_CHANGED,
					"item", treebank)); //$NON-NLS-1$
		}
	}

	public final class CallbackHandler {

		private CallbackHandler() {
			// no-op
		}

		public void newTreebank(ActionEvent e) {
			Extension extension = TreebankRegistry.getInstance().availableTypes().iterator().next();

			// Create new name and treebank
			String name = TreebankRegistry.getInstance().getUniqueName("New Treebank"); //$NON-NLS-1$
			TreebankDescriptor descriptor = null;
			try {
				descriptor = TreebankRegistry.getInstance().newTreebank(extension, name);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Unable to create new treebank: "+name, ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}

			if(descriptor==null) {
				return;
			}

			Treebank treebank = descriptor.getTreebank();
			treebanksList.setSelectedValue(treebank, true);

			// Edit treebank and check for user cancellation
			boolean cancelled = false;
			Editor<Treebank> editor = null;
			try {

				editor = new DefaultSimpleTreebankEditor();

				if(!DialogFactory.getGlobalFactory().showEditorDialog(
						getFrame(), treebank, editor,
						"plugins.languageTools.treebankExplorerView.dialogs.editTreebank.title")) { //$NON-NLS-1$
					cancelled = true;
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to edit new treebank", ex); //$NON-NLS-1$

				UIUtil.beep();
				showError(ex);
			} finally {
				if(editor!=null) {
					editor.close();
				}
			}

			// If initial edit dialog was cancelled, delete treebank
			if(cancelled) {
				TreebankRegistry.getInstance().deleteTreebank(treebank);
			}
		}

		public void deleteTreebank(ActionEvent e) {
			Treebank treebank = treebanksList.getSelectedValue();
			if(treebank==null)
				return;
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
					treebanksList.clearSelection();
					TreebankRegistry.getInstance().deleteTreebank(treebank);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Unable to delete treebank: "+treebank.getName(), ex); //$NON-NLS-1$
					UIUtil.beep();

					showError(ex);
				}
			}
		}

		public void cloneTreebank(ActionEvent e) {
			Treebank treebank = treebanksList.getSelectedValue();
			if(treebank==null)
				return;

			Extension extension = TreebankRegistry.getInstance().getExtension(treebank);

			String name = TreebankRegistry.getInstance().getUniqueName(treebank.getName());
			try {
				TreebankRegistry.getInstance().newTreebank(extension, name);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Unable to create new treebank: "+name, ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void renameTreebank(ActionEvent e) {
			Treebank treebank = treebanksList.getSelectedValue();
			if(treebank==null)
				return;

			String currentName = treebank.getName();
			String newName = DialogFactory.getGlobalFactory().showInputDialog(getFrame(),
					"plugins.languageTools.treebankExplorerView.dialogs.renameTreebank.title",  //$NON-NLS-1$
					"plugins.languageTools.treebankExplorerView.dialogs.renameTreebank.message",  //$NON-NLS-1$
					currentName, currentName);

			if(newName==null || newName.isEmpty())
				return;

			// No changes
			if(currentName.equals(newName))
				return;

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
				UIUtil.beep();

				showError(ex);
			}
		}

		public void openLocation(ActionEvent e) {
			if(!Desktop.isDesktopSupported())
				return;
			Treebank treebank = treebanksList.getSelectedValue();
			if(treebank==null)
				return;
			Location location = treebank.getLocation();
			if(location==null)
				return;

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
				UIUtil.beep();

				showError(ex);
			}
		}

		public void inspectTreebank(ActionEvent e) {
			Treebank treebank = treebanksList.getSelectedValue();
			if(treebank==null)
				return;
			if(!treebank.isLoaded())
				return;

			ContentType contentType = ContentTypeRegistry.getInstance().getTypeForClass(Treebank.class);

			Options options = new Options();
			options.put(Options.CONTENT_TYPE, contentType);
			// TODO send some kind of hint that we want the presenter not to modify content?
			// -> Should be no problem since we only contain immutable data objects?
			TreebankListDelegate delegate = TreebankRegistry.getInstance().getListDelegate(treebank);

			Message message = new Message(this, Commands.DISPLAY, delegate, options);

			try {
				sendRequest(null, message);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to inspect treebank: "+treebank.getName(), ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void loadTreebank(ActionEvent e) {
			Treebank treebank = treebanksList.getSelectedValue();
			if(treebank==null)
				return;
			if(treebank.isLoaded())
				return;

			try {
				TreebankJob task = new TreebankJob(treebank, true);
				TaskManager.getInstance().schedule(task, TaskPriority.DEFAULT, true);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to schedule load-task for treebank: "+treebank.getName(), ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void freeTreebank(ActionEvent e) {
			Treebank treebank = treebanksList.getSelectedValue();
			if(treebank==null)
				return;
			if(!treebank.isLoaded())
				return;

			try {
				TreebankJob task = new TreebankJob(treebank, false);
				TaskManager.getInstance().schedule(task, TaskPriority.DEFAULT, true);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to schedule free-task for treebank: "+treebank.getName(), ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void editTreebank(ActionEvent e) {
//
//			Message message = new Message(this, Commands.EDIT, treebank, null);
//
//			try {
//				sendRequest(LanguageToolsConstants.TREEBANK_EDIT_VIEW_ID, message);
//			} catch(Exception ex) {
//				LoggerFactory.log(this, Level.SEVERE,
//						"Failed to edit treebank: "+treebank.getName(), ex); //$NON-NLS-1$
//				UIUtil.beep();
//
//				showError(ex);
//			}
			Editor<Treebank> editor = null;
			try {
				Treebank treebank = treebanksList.getSelectedValue();
				if(treebank==null)
					return;

				editor = new DefaultSimpleTreebankEditor();

				DialogFactory.getGlobalFactory().showEditorDialog(
						getFrame(), treebank, editor,
						"plugins.languageTools.treebankExplorerView.dialogs.editTreebank.title"); //$NON-NLS-1$

			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to edit treebank", ex); //$NON-NLS-1$

				UIUtil.beep();
				showError(ex);
			} finally {
				if(editor!=null) {
					editor.close();
				}
			}
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
					scrollPane, false, "ok", "cancel")) //$NON-NLS-1$ //$NON-NLS-2$
				return;

			int[] indices = list.getSelectedIndices();
			if(indices.length==0)
				return;

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

			if(file==null)
				return;

			try {
				// Perform export
				TreebankRegistry.getInstance().exportTreebanks(file, treebanks);
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to export treebanks to file: "+file.getAbsolutePath(), ex); //$NON-NLS-1$
				UIUtil.beep();

				DialogFactory.getGlobalFactory().showError(getFrame(),
						"plugins.languageTools.treebankExplorerView.dialogs.exportTreebanks.title",  //$NON-NLS-1$
						"plugins.languageTools.treebankExplorerView.dialogs.exportTreebanks.ioException",  //$NON-NLS-1$
						StringUtil.fit(file.getAbsolutePath(), 100),
						StringUtil.fit(ex.getMessage(), 100));
				return;
			}

			// Feedback to user
			DialogFactory.getGlobalFactory().showInfo(getFrame(),
					"plugins.languageTools.treebankExplorerView.dialogs.exportTreebanks.title",  //$NON-NLS-1$
					"plugins.languageTools.treebankExplorerView.dialogs.exportTreebanks.result",  //$NON-NLS-1$
					treebanks.size(), StringUtil.fit(file.getAbsolutePath(), 100));
		}

		// TODO switch execution to background task?
		public void importTreebanks(ActionEvent e) {

			// Obtain source file
			File file = DialogFactory.getGlobalFactory().showSourceFileDialog(
					getFrame(),
					"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.title",  //$NON-NLS-1$
					null);

			if(file==null || !file.exists())
				return;

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
						StringUtil.fit(file.getAbsolutePath(), 100),
						StringUtil.fit(ex.toString(), 100));
				return;
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Cannot import treebank data from file: "+file.getAbsolutePath(), ex); //$NON-NLS-1$
				UIUtil.beep();

				DialogFactory.getGlobalFactory().showError(getFrame(),
						"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.title",  //$NON-NLS-1$
						"plugins.languageTools.treebankExplorerView.dialogs.importTreebanks.invalidContent",  //$NON-NLS-1$
						StringUtil.fit(file.getAbsolutePath(), 100),
						StringUtil.fit(ex.toString(), 100));
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

		public void assessTreebank(ActionEvent e) {
			Treebank treebank = treebanksList.getSelectedValue();
			if(treebank==null) {
				return;
			}

			TaskManager.getInstance().schedule(new AssessmentWorker(treebank), TaskPriority.DEFAULT, true);
		}
	}
}
