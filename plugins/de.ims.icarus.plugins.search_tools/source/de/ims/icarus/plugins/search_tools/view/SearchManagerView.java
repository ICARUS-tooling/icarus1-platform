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
package de.ims.icarus.plugins.search_tools.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.java.plugin.registry.Extension;

import de.ims.icarus.Core;
import de.ims.icarus.io.IOUtil;
import de.ims.icarus.io.Loadable;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.ExtensionListModel;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.IcarusCorePlugin;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.search_tools.SearchToolsConstants;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchDescriptor;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.SearchTargetSelector;
import de.ims.icarus.search_tools.io.SearchReader;
import de.ims.icarus.search_tools.io.SearchResolver;
import de.ims.icarus.search_tools.io.SearchWriter;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.ui.ComponentUpdater;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.dialog.SelectFormEntry;
import de.ims.icarus.ui.helper.DefaultFileFilter;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.data.DataContainer;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;
import de.ims.icarus.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchManagerView extends View {

	protected JList<SearchDescriptor> searchHistoryList;
	protected SearchHistory searchHistory;

	protected SearchEditor currentSearchEditor;

	protected Handler handler;

	protected CallbackHandler callbackHandler;

	protected JPopupMenu popupMenu;

	public SearchManagerView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {

		Collection<Extension> factoryExtensions = SearchManager.getSearchFactoryExtensions();
		if(factoryExtensions==null || factoryExtensions.isEmpty()) {
			container.setLayout(new BorderLayout());
			JTextArea infoLabel = UIUtil.defaultCreateInfoLabel(container);
			ResourceManager.getInstance().getGlobalDomain().prepareComponent(infoLabel,
					"plugins.searchTools.searchManagerView.notAvailable", null); //$NON-NLS-1$
			ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);
			container.add(infoLabel, BorderLayout.NORTH);

			return;
		}

		// Load actions
		if(!defaultLoadActions(SearchManagerView.class, "search-manager-view-actions.xml")) { //$NON-NLS-1$
			return;
		}

		handler = new Handler();

		// Header tool-bar
		Options options = new Options("multiline", true); //$NON-NLS-1$
		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"plugins.searchTools.searchManagerView.toolBarList", options); //$NON-NLS-1$

		// Current search editor
		currentSearchEditor = new SearchEditor();
		// Set initially empty search
		currentSearchEditor.setEditingItem(newDescriptor());

		JPanel editorPanel = new JPanel(new BorderLayout());
		editorPanel.add(toolBar, BorderLayout.NORTH);
		editorPanel.add(currentSearchEditor.getEditorComponent(), BorderLayout.CENTER);

		// History tool-bar
		options = new Options("multiline", true); //$NON-NLS-1$
		toolBar = getDefaultActionManager().createToolBar(
				"plugins.searchTools.searchManagerView.historyToolBarList", options); //$NON-NLS-1$

		// History
		searchHistory = SearchHistory.getSharedInstance();
		searchHistoryList = new JList<>(searchHistory);
		searchHistoryList.setBorder(UIUtil.defaultContentBorder);
		searchHistoryList.setCellRenderer(new SearchHistoryListCellRenderer());
		searchHistoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchHistoryList.addListSelectionListener(handler);
		searchHistoryList.addMouseListener(handler);
		UIUtil.enableToolTip(searchHistoryList);
		JScrollPane scrollPane = new JScrollPane(searchHistoryList);
		scrollPane.setBorder(UIUtil.topLineBorder);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		UIUtil.enableRighClickListSelection(searchHistoryList);

		JPanel historyPanel = new JPanel(new BorderLayout());
		historyPanel.setBorder(UIUtil.topLineBorder);
		historyPanel.add(toolBar, BorderLayout.NORTH);
		historyPanel.add(scrollPane, BorderLayout.CENTER);

		container.setLayout(new BorderLayout());
		container.add(editorPanel, BorderLayout.NORTH);
		container.add(historyPanel, BorderLayout.CENTER);

		registerActionCallbacks();

		// Show example if required
		if(IcarusCorePlugin.isShowExampleData()) {
			try {
				SearchDescriptor descriptor = new SearchDescriptor();
				descriptor.setFactoryExtension(factoryExtensions.iterator().next());

				descriptor.createExampleSearch();

				currentSearchEditor.setEditingItem(descriptor);

				callbackHandler.editQuery(null);
			} catch (Exception e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to generate example search", e); //$NON-NLS-1$
			}
		}

		refreshActions();

		ComponentUpdater.getInstance().addComponent(searchHistoryList);
	}

	protected void refreshActions() {
		ActionManager actionManager = getDefaultActionManager();

		// Refresh editor actions
		SearchDescriptor descriptor = currentSearchEditor.getEditingItem();
		Search search = descriptor==null ? null : descriptor.getSearch();
		Object target = descriptor==null ? null : descriptor.getTarget();

		boolean canRun = descriptor!=null && descriptor.getTarget()!=null;

		actionManager.setEnabled(canRun,
				"plugins.searchTools.searchManagerView.executeSearchAction"); //$NON-NLS-1$

		// Refresh history actions
		descriptor = searchHistoryList.getSelectedValue();
		boolean selected = descriptor!=null;
		search = selected ? descriptor.getSearch() : null;
		boolean canCancel = search!=null && search.isRunning();
		boolean hasResult = search!=null && search.getResult()!=null;
		boolean isLoadable = target instanceof Loadable;
		boolean isLoading = isLoadable && ((Loadable)target).isLoading();
		boolean canLoad = isLoadable && !isLoading && !((Loadable)target).isLoaded();
		boolean canFree = isLoadable && !isLoading && ((Loadable)target).isLoaded();
		boolean canSave = hasResult && search.isDone() && search.isSerializable();

		actionManager.setEnabled(hasResult,
				"plugins.searchTools.searchManagerView.viewResultAction"); //$NON-NLS-1$
		actionManager.setEnabled(canCancel,
				"plugins.searchTools.searchManagerView.cancelSearchAction"); //$NON-NLS-1$
		actionManager.setEnabled(searchHistory.getSize()>0,
				"plugins.searchTools.searchManagerView.clearHistoryAction"); //$NON-NLS-1$
		actionManager.setEnabled(selected,
				"plugins.searchTools.searchManagerView.viewSearchAction",  //$NON-NLS-1$
				"plugins.searchTools.searchManagerView.removeSearchAction"); //$NON-NLS-1$
		actionManager.setEnabled(canLoad,
				"plugins.searchTools.searchManagerView.loadSearchTargetAction"); //$NON-NLS-1$
		actionManager.setEnabled(canFree,
				"plugins.searchTools.searchManagerView.freeSearchTargetAction"); //$NON-NLS-1$
		actionManager.setEnabled(canSave,
				"plugins.searchTools.searchManagerView.saveSearchAction"); //$NON-NLS-1$
	}

	protected void showPopup(MouseEvent trigger) {
		if(popupMenu==null) {
			// Create new popup menu

			Options options = new Options();
			popupMenu = getDefaultActionManager().createPopupMenu(
					"plugins.searchTools.searchManagerView.historyPopupMenuList", options); //$NON-NLS-1$

			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create popup menu"); //$NON-NLS-1$
			}
		}

		if(popupMenu!=null) {
			refreshActions();

			popupMenu.show(searchHistoryList, trigger.getX(), trigger.getY());
		}
	}

	protected void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}

		ActionManager actionManager = getDefaultActionManager();

		actionManager.addHandler("plugins.searchTools.searchManagerView.newSearchAction",  //$NON-NLS-1$
				callbackHandler, "newSearch"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.executeSearchAction",  //$NON-NLS-1$
				callbackHandler, "executeSearch"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.cancelSearchAction",  //$NON-NLS-1$
				callbackHandler, "cancelSearch"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.removeSearchAction",  //$NON-NLS-1$
				callbackHandler, "removeSearch"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.clearHistoryAction",  //$NON-NLS-1$
				callbackHandler, "clearHistory"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.openPreferencesAction",  //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.editQueryAction",  //$NON-NLS-1$
				callbackHandler, "editQuery"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.selectFactoryAction",  //$NON-NLS-1$
				callbackHandler, "selectFactory"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.selectTargetAction",  //$NON-NLS-1$
				callbackHandler, "selectTarget"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.viewResultAction",  //$NON-NLS-1$
				callbackHandler, "viewResult"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.editParametersAction",  //$NON-NLS-1$
				callbackHandler, "editParameters"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.viewSearchAction",  //$NON-NLS-1$
				callbackHandler, "viewSearch"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.loadSearchTargetAction",  //$NON-NLS-1$
				callbackHandler, "loadSearchTarget"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.freeSearchTargetAction",  //$NON-NLS-1$
				callbackHandler, "freeSearchTarget"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.saveSearchAction",  //$NON-NLS-1$
				callbackHandler, "saveSearch"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchManagerView.openSearchAction",  //$NON-NLS-1$
				callbackHandler, "openSearch"); //$NON-NLS-1$
	}

	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.SELECT.equals(message.getCommand())) {
			Object data = message.getData();
			if(data instanceof SearchDescriptor) {
				SearchDescriptor descriptor = (SearchDescriptor)data;
				SearchDescriptor currentDescriptor = currentSearchEditor.getEditingItem();

				if(currentDescriptor!=null && currentDescriptor!=descriptor) {
					currentDescriptor.setQuery(descriptor.getQuery());
					descriptor = currentDescriptor;
				}

				currentSearchEditor.setEditingItem(descriptor);
				searchHistoryList.setSelectedValue(descriptor, true);

				return message.successResult(this, data);
			} else {
				return message.unsupportedDataResult(this);
			}
		} else {
			return message.unknownRequestResult(this);
		}
	}

	protected void syncEditorViews() {
		if(currentSearchEditor.getEditingItem()==null) {
			return;
		}

		Message message = new Message(this, Commands.COMMIT, null, null);
		try {
			sendRequest(null, message);
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to synchronize editor views", e); //$NON-NLS-1$

			UIUtil.beep();
			showError(e);
		}

		refreshActions();
	}

	protected void clearEditorViews() {
		Message message = new Message(this, Commands.CLEAR, null, null);
		try {
			sendRequest(SearchToolsConstants.QUERY_EDITOR_VIEW_ID, message);
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to clear editor views", e); //$NON-NLS-1$
		}
	}

	protected SearchDescriptor newDescriptor() {
		Collection<Extension> factoryExtensions = SearchManager.getSearchFactoryExtensions();
		if(factoryExtensions==null || factoryExtensions.isEmpty())
			throw new IllegalStateException("Cannot create search descriptor - no search factories available"); //$NON-NLS-1$

		SearchDescriptor descriptor = new SearchDescriptor();
		descriptor.setFactoryExtension(factoryExtensions.iterator().next());

		return descriptor;
	}

	protected class Handler extends MouseAdapter implements ListSelectionListener, PropertyChangeListener {

		protected Search observedSearch;

		protected Handler() {
			// no-op
		}

		protected void maybeShowPopup(MouseEvent e) {
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
			if(e.getClickCount()!=2 || !SwingUtilities.isLeftMouseButton(e)) {
				return;
			}

			int index = searchHistoryList.locationToIndex(e.getPoint());
			if(index==-1) {
				return;
			}
			Rectangle bounds = searchHistoryList.getCellBounds(index, index);
			if(!bounds.contains(e.getPoint())) {
				return;
			}

			SearchDescriptor descriptor = searchHistoryList.getModel().getElementAt(index);

			try {
				// Display search
				SearchDescriptor clone = descriptor.clone();
				currentSearchEditor.setEditingItem(clone);
				refreshActions();

				// Display result
				SearchResult result = descriptor.getSearchResult();
				Search search = descriptor.getSearch();

				if(result==null || (!search.isDone() && !search.isRunning())) {
					return;
				}

				if(result.getTotalMatchCount()<1) {
					DialogFactory.getGlobalFactory().showWarning(getFrame(),
							"plugins.searchTools.searchManagerView.dialogs.emptyResult.title",  //$NON-NLS-1$
							"plugins.searchTools.searchManagerView.dialogs.emptyResult.message"); //$NON-NLS-1$

					return;
				}

				Options options = new Options();
				Message message = new Message(this, Commands.PRESENT, result, options);

				sendRequest(SearchToolsConstants.SEARCH_RESULT_VIEW_ID, message);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to view result for search at index: "+index, ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(observedSearch!=null) {
				observedSearch.removePropertyChangeListener("state", this); //$NON-NLS-1$
				observedSearch = null;
			}

			SearchDescriptor descriptor = searchHistoryList.getSelectedValue();
			if(descriptor!=null) {
				observedSearch = descriptor.getSearch();
			}

			if(observedSearch!=null) {
				observedSearch.addPropertyChangeListener("state", this); //$NON-NLS-1$
			}

			refreshActions();
		}

		/**
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			refreshActions();
		}
	}

	public class CallbackHandler {
		protected CallbackHandler() {
			// no-op
		}

		public void newSearch(ActionEvent e) {
			try {
				syncEditorViews();

				currentSearchEditor.setEditingItem(newDescriptor());

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to create new search", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void executeSearch(ActionEvent e) {
			if(currentSearchEditor.getEditingItem()==null) {
				return;
			}

			try {

				syncEditorViews();
				SearchDescriptor descriptor = currentSearchEditor.getEditingItem();

				/*SearchDescriptor updatedDescriptor = currentSearchEditor.getEditingItem();
				if(updatedDescriptor!=null && updatedDescriptor!=descriptor) {
					descriptor.setQuery(updatedDescriptor.getQuery());
				}*/

				Search search = descriptor.getSearch();
				if(search!=null && search.isRunning()) {
					UIUtil.beep();
					return;
				}

				// TODO allow for execution of already running searches

				// Create a new descriptor without a search object
				SearchDescriptor clone = descriptor.cloneShallow();
				// Let factory create a blank new search object
				if(!clone.createSearch()) {
					return;
				}
				currentSearchEditor.setEditingItem(descriptor.clone());

				searchHistory.addSearch(clone);

				SearchManager.getInstance().executeSearch(clone.getSearch());

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to execute search", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void openPreferences(ActionEvent e) {
			try {
				UIUtil.openConfigDialog("plugins.searchTools"); //$NON-NLS-1$
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to open preferences", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void editQuery(ActionEvent e) {
			if(currentSearchEditor.getEditingItem()==null) {
				return;
			}
			SearchDescriptor descriptor = currentSearchEditor.getEditingItem();
			if(descriptor==null) {
				return;
			}

			SearchQuery searchQuery = descriptor.getQuery();
			if(searchQuery==null) {
				LoggerFactory.log(this, Level.WARNING,
						"No search-query present on search-descriptor"); //$NON-NLS-1$
				return;
			}

			try {
				Message message = new Message(SearchManagerView.this,
						Commands.PRESENT, descriptor, null);

				sendRequest(SearchToolsConstants.QUERY_EDITOR_VIEW_ID, message);
			}  catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to forward editing of query", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}

		}

		public void selectFactory(ActionEvent e) {
			try {
				Collection<Extension> extensions = SearchManager.getInstance().availableSearchFactories();
				Extension extension = PluginUtil.showExtensionDialog(getFrame(),
						"plugins.searchTools.searchManagerView.dialogs.selectFactory.title",  //$NON-NLS-1$
						extensions, true);

				if(extension==null) {
					return;
				}

				SearchDescriptor descriptor = new SearchDescriptor();
				descriptor.setFactoryExtension(extension);
				currentSearchEditor.setEditingItem(descriptor);

				clearEditorViews();

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to select new search factory", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void selectTarget(ActionEvent e) {
			if(currentSearchEditor.getEditingItem()==null) {
				return;
			}

			SearchFactory factory = currentSearchEditor.getEditingItem().getSearchFactory();
			if(factory==null) {
				UIUtil.beep();
				return;
			}

			try {
				ContentType contentType = factory.getConstraintContext().getContentType();
				SearchTargetDialog dialog = new SearchTargetDialog(contentType);
				dialog.showDialog();

				Object target = dialog.getTarget();
				if(target==null) {
					return;
				}

				boolean compatible = false;
				if(target instanceof DataContainer) {
					// If target is a container allow checking against its internal content type
					compatible = ContentTypeRegistry.isCompatible(contentType,
							((DataContainer)target).getContentType());
				} else {
					// No information about content type accessible, so
					// just do a plain check (will fail often?)
					compatible = ContentTypeRegistry.isCompatible(contentType, target);
				}

				if(!compatible) {
					DialogFactory.getGlobalFactory().showError(null,
							"plugins.searchTools.searchManagerView.dialogs.selectTarget.title",  //$NON-NLS-1$
							"plugins.searchTools.searchManagerView.dialogs.selectTarget.incompatible",  //$NON-NLS-1$
							target.getClass().getName(), contentType.getName());
					return;
				}

				currentSearchEditor.getEditingItem().setTarget(target);
				currentSearchEditor.refresh();

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to select target for current search", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void editParameters(ActionEvent e) {
			if(currentSearchEditor.getEditingItem()==null) {
				return;
			}

			try {
				SearchDescriptor descriptor = currentSearchEditor.getEditingItem();

				Editor<Options> editor = descriptor.getSearchFactory().createParameterEditor();
				Options parameters = descriptor.getParameters();
				if(parameters==null) {
					parameters = new Options();
				}

				if(DialogFactory.getGlobalFactory().showEditorDialog(
						getFrame(), parameters, editor,
						"plugins.searchTools.searchManagerView.dialogs.editParameters.title")) { //$NON-NLS-1$
					descriptor.setParameters(editor.getEditingItem());
				}

			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to edit paramters", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void viewResult(ActionEvent e) {
			SearchDescriptor descriptor = searchHistoryList.getSelectedValue();
			if(descriptor==null) {
				return;
			}

			try {
				// TODO DEBUG
				/*if(descriptor.getSearch()==null) {
					debugShowResult();
					return;
				}*/

				SearchResult result = descriptor.getSearchResult();

				if(result==null) {
					return;
				}

				Options options = new Options();
				Message message = new Message(this, Commands.PRESENT, result, options);

				sendRequest(SearchToolsConstants.SEARCH_RESULT_VIEW_ID, message);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to view result of selected search", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void viewSearch(ActionEvent e) {
			SearchDescriptor descriptor = searchHistoryList.getSelectedValue();
			if(descriptor==null) {
				return;
			}

			try {
				SearchDescriptor clone = descriptor.cloneShallow();
				currentSearchEditor.setEditingItem(clone);

				refreshActions();

				// Forward to query editor directly
				editQuery(e);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to edit selected search", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void cancelSearch(ActionEvent e) {
			SearchDescriptor descriptor = searchHistoryList.getSelectedValue();
			if(descriptor==null) {
				return;
			}

			try {
				Search search = descriptor.getSearch();
				if(search!=null) {
					SearchManager.getInstance().cancelSearch(search);
				}

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to cancel selected search", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void loadSearchTarget(ActionEvent e) {
			SearchDescriptor descriptor = searchHistoryList.getSelectedValue();
			if(descriptor==null) {
				return;
			}

			try {
				Object target = descriptor.getTarget();

				if(!(target instanceof Loadable)) {
					return;
				}

				Loadable loadable = (Loadable) target;

				if(loadable.isLoaded() || loadable.isLoading()) {
					return;
				}

				String title = ResourceManager.getInstance().get(
						"plugins.searchTools.searchManager.loadTargetJob.name"); //$NON-NLS-1$
				String info = ResourceManager.getInstance().get(
						"plugins.searchTools.searchManager.loadTargetJob.description", //$NON-NLS-1$
						StringUtil.getName(loadable));

				Object task = new IOUtil.LoadJob(loadable) {

					/**
					 * @see javax.swing.SwingWorker#done()
					 */
					@Override
					protected void done() {
						Loadable loadable = null;
						try {
							loadable = get();

						} catch(InterruptedException | CancellationException e) {
							// ignore
						} catch(Exception e) {
							LoggerFactory.log(this, Level.SEVERE,
									"Failed to load search target: "+String.valueOf(loadable), e); //$NON-NLS-1$
							UIUtil.beep();

							if(!Core.getCore().handleThrowable(e)) {
								showError(e);
							}

						} finally {
							searchHistoryList.repaint();
							refreshActions();
						}
					}
				};

				TaskManager.getInstance().schedule(task, title, info, null, TaskPriority.HIGH, true);

				searchHistoryList.repaint();
				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to load search target", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void freeSearchTarget(ActionEvent e) {
			SearchDescriptor descriptor = searchHistoryList.getSelectedValue();
			if(descriptor==null) {
				return;
			}

			try {
				Object target = descriptor.getTarget();

				if(!(target instanceof Loadable)) {
					return;
				}

				Loadable loadable = (Loadable) target;

				if(!loadable.isLoaded() || loadable.isLoading()) {
					return;
				}

				loadable.free();

				searchHistoryList.repaint();
				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to free search target", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void removeSearch(ActionEvent e) {
			SearchDescriptor descriptor = searchHistoryList.getSelectedValue();
			if(descriptor==null) {
				return;
			}

			try {

				if(descriptor.isActive()) {
					DialogFactory.getGlobalFactory().showWarning(getFrame(),
							"plugins.searchTools.searchManagerView.dialogs.removeSearch.title",  //$NON-NLS-1$
							"plugins.searchTools.searchManagerView.dialogs.removeSearch.message"); //$NON-NLS-1$
					return;
				}

				if(!DialogFactory.getGlobalFactory().showConfirm(getFrame(),
						"plugins.searchTools.searchManagerView.dialogs.removeSearch.title",  //$NON-NLS-1$
						"plugins.searchTools.searchManagerView.dialogs.removeSearch.confirm")) { //$NON-NLS-1$
					return;
				}

				int index = searchHistoryList.getSelectedIndex();
				searchHistory.removeSearch(descriptor);

				Search search = descriptor.getSearch();
				if(search!=null) {
					SearchManager.getInstance().cancelSearch(search);
				}

				// Maintain selected index to allow for more fluent
				// use of selection related actions
				if(index==-1 || index>=searchHistory.getSize()) {
					index = searchHistory.getSize()-1;
				}
				if(index!=-1) {
					searchHistoryList.setSelectedIndex(index);
				} else {
					refreshActions();
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to remove selected search from history", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void clearHistory(ActionEvent e) {
			try {
				// Abort if there is a search still active and running
				for(int i=0; i<searchHistory.getSize(); i++) {
					if(searchHistory.getElementAt(i).isActive()) {
						DialogFactory.getGlobalFactory().showWarning(getFrame(),
								"plugins.searchTools.searchManagerView.dialogs.clearHistory.title",  //$NON-NLS-1$
								"plugins.searchTools.searchManagerView.dialogs.clearHistory.message"); //$NON-NLS-1$
						return;
					}
				}

				if(!DialogFactory.getGlobalFactory().showConfirm(getFrame(),
						"plugins.searchTools.searchManagerView.dialogs.clearHistory.title",  //$NON-NLS-1$
						"plugins.searchTools.searchManagerView.dialogs.clearHistory.confirm")) { //$NON-NLS-1$
					return;
				}

				searchHistory.clear();

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to clear search history", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		private JFileChooser fileChooser;

		private JFileChooser getFileChooser() {
			if(fileChooser==null) {
				fileChooser = new JFileChooser();

				FileFilter defaultFilter = new DefaultFileFilter(".xml", "*.xml Files"); //$NON-NLS-1$ //$NON-NLS-2$

				fileChooser.addChoosableFileFilter(defaultFilter);
				fileChooser.setFileFilter(defaultFilter);

				fileChooser.setCurrentDirectory(Core.getCore().getDataFolder().toFile());
			}

			return fileChooser;
		}

		public void saveSearch(ActionEvent e) {
			SearchDescriptor descriptor = searchHistoryList.getSelectedValue();
			if(descriptor==null) {
				return;
			}

			try {
				Search search = descriptor.getSearch();

				if(!search.isDone() || search.getResult()==null && !search.isSerializable()) {
					return;
				}

				SearchResolver resolver = search.getSearchResolver();

				if(resolver==null) {
					return;
				}

				SearchQuery query = search.getQuery();
				List<SearchConstraint> inactive = SearchUtils.collectInactive(query.getSearchGraph());
				if(!inactive.isEmpty()) {
					UIUtil.beep();
					DialogFactory.getGlobalFactory().showWarning(getFrame(),
							"plugins.searchTools.searchManagerView.dialogs.saveSearch.title", //$NON-NLS-1$
							"plugins.searchTools.searchManagerView.dialogs.saveSearch.inactiveConstraints", //$NON-NLS-1$
							inactive.size());
					return;
				}

				JFileChooser fileChooser = getFileChooser();

				fileChooser.setApproveButtonText(ResourceManager.getInstance().get("save")); //$NON-NLS-1$
				fileChooser.setDialogTitle(ResourceManager.getInstance().get(
						"plugins.searchTools.searchManagerView.dialogs.saveSearch.title")); //$NON-NLS-1$

				String filename = null;

				if (fileChooser.showDialog(null, null) != JFileChooser.APPROVE_OPTION) {
					return;
				}

				filename = fileChooser.getSelectedFile().getAbsolutePath();

				// Append file extension if missing in name
				if (!filename.toLowerCase().endsWith(".xml")) { //$NON-NLS-1$
					filename += ".xml"; //$NON-NLS-1$
				}

				Path path = Paths.get(filename);

				// Overwrite if already existing?
				if (Files.exists(path)
						&& !DialogFactory.getGlobalFactory().showConfirm(null,
								"plugins.searchTools.searchManagerView.dialogs.saveSearch.title",  //$NON-NLS-1$
								"plugins.searchTools.searchManagerView.dialogs.saveSearch.overwriteExisting", //$NON-NLS-1$
								StringUtil.fit(filename, 80))) {
					return;
				}

				SearchWriter writer = new SearchWriter(search);

				String title = ResourceManager.getInstance().get("plugins.searchTools.searchManagerView.saveSearchTask.title"); //$NON-NLS-1$

				TaskManager.getInstance().schedule(
						new SearchWriterTask(writer, path),
						title, null, null, TaskPriority.DEFAULT, true);

			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to save search", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void openSearch(ActionEvent e) {
			try {

				JFileChooser fileChooser = getFileChooser();

				fileChooser.setApproveButtonText(ResourceManager.getInstance().get("load")); //$NON-NLS-1$
				fileChooser.setDialogTitle(ResourceManager.getInstance().get(
						"plugins.searchTools.searchManagerView.dialogs.openSearch.title")); //$NON-NLS-1$

				if (fileChooser.showDialog(null, null) != JFileChooser.APPROVE_OPTION) {
					return;
				}

				Path path = fileChooser.getSelectedFile().toPath();

				SearchReader reader = new SearchReader(path);

				String title = ResourceManager.getInstance().get("plugins.searchTools.searchManagerView.openSearchTask.title"); //$NON-NLS-1$

				TaskManager.getInstance().schedule(
						new SearchReaderTask(reader),
						title, null, null, TaskPriority.DEFAULT, true);

			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to open search", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}
	}

	protected class SearchWriterTask extends SwingWorker<SearchWriter, Object> {

		private final SearchWriter writer;
		private final Path path;

		public SearchWriterTask(SearchWriter writer, Path path) {
			if (writer == null)
				throw new NullPointerException("Invalid reader"); //$NON-NLS-1$
			if (path == null)
				throw new NullPointerException("Invalid path"); //$NON-NLS-1$

			this.writer = writer;
			this.path = path;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected SearchWriter doInBackground() throws Exception {

			TaskManager.getInstance().setIndeterminate(this, true);

			try (OutputStream out = Files.newOutputStream(path,
					StandardOpenOption.CREATE,
					StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING)) {
				writer.write(out);

				LoggerFactory.info(this, "Search saved to file: "+path); //$NON-NLS-1$
			}

			return writer;
		}

		/**
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		protected void done() {
			try {
				get();
			} catch (InterruptedException e) {
				try {
					Files.deleteIfExists(path);
				} catch (IOException ex) {
					LoggerFactory.error(this,
							"Failed to delete file '"+path+"' after cancelled save task", ex); //$NON-NLS-1$ //$NON-NLS-2$
				}

			} catch (ExecutionException e) {
				Exception ex = (Exception) e.getCause();

				LoggerFactory.error(this, "Saving search to file '"+path+"' failed", ex); //$NON-NLS-1$ //$NON-NLS-2$

				DialogFactory.getGlobalFactory().showError(null,
						"plugins.searchTools.searchManagerView.dialogs.saveSearch.title", //$NON-NLS-1$
						"plugins.searchTools.searchManagerView.dialogs.saveSearch.failed", //$NON-NLS-1$
						path);
			}
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof SearchWriterTask) {
				return writer.equals(((SearchWriterTask)obj).writer);
			}

			return false;
		}

	}

	protected class SearchReaderTask extends SwingWorker<SearchDescriptor, Object> {

		private final SearchReader reader;

		public SearchReaderTask(SearchReader reader) {
			if (reader == null)
				throw new NullPointerException("Invalid reader"); //$NON-NLS-1$

			this.reader = reader;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected SearchDescriptor doInBackground() throws Exception {

			TaskManager.getInstance().setIndeterminate(this, true);

			SearchDescriptor descriptor = reader.load();

			LoggerFactory.info(this, "Loaded search from file: "+reader.getPath()); //$NON-NLS-1$

			return descriptor;
		}

		/**
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		protected void done() {
			try {
				SearchDescriptor descriptor = get();

				searchHistory.addSearch(descriptor);

				refreshActions();

				Object target = descriptor.getTarget();

				if(!(target instanceof Loadable)) {
					return;
				}

				final Loadable loadable = (Loadable) target;

				if(!loadable.isLoaded() && DialogFactory.getGlobalFactory().showConfirm(getFrame(),
						"plugins.searchTools.searchManagerView.dialogs.openSearch.title", //$NON-NLS-1$
						"plugins.searchTools.searchManagerView.dialogs.openSearch.loadTarget")) { //$NON-NLS-1$

					String title = ResourceManager.getInstance().get(
							"plugins.searchTools.searchManager.loadTargetJob.name"); //$NON-NLS-1$
					String info = ResourceManager.getInstance().get(
							"plugins.searchTools.searchManager.loadTargetJob.description", //$NON-NLS-1$
							StringUtil.getName(loadable));

					Object task = new IOUtil.LoadJob(loadable) {

						/**
						 * @see javax.swing.SwingWorker#done()
						 */
						@Override
						protected void done() {
							Loadable loadable = null;
							try {
								loadable = get();

							} catch(InterruptedException | CancellationException e) {
								// ignore
							} catch(Exception e) {
								LoggerFactory.log(this, Level.SEVERE,
										"Failed to load search target: "+String.valueOf(loadable), e); //$NON-NLS-1$
								UIUtil.beep();

								if(!Core.getCore().handleThrowable(e)) {
									showError(e);
								}

							} finally {
								searchHistoryList.repaint();
								refreshActions();
							}
						}
					};

					TaskManager.getInstance().schedule(task, title, info, null, TaskPriority.HIGH, true);

					searchHistoryList.repaint();
				}

			} catch (InterruptedException e) {
				// no-op
			} catch (ExecutionException e) {
				Exception ex = (Exception) e.getCause();

				LoggerFactory.error(this, "Loading search from file '"+reader.getPath()+"' failed", ex); //$NON-NLS-1$ //$NON-NLS-2$

				DialogFactory.getGlobalFactory().showError(null,
						"plugins.searchTools.searchManagerView.dialogs.openSearch.title", //$NON-NLS-1$
						"plugins.searchTools.searchManagerView.dialogs.openSearch.failed", //$NON-NLS-1$
						reader.getPath());
			}
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof SearchReaderTask) {
				return reader.equals(((SearchReaderTask)obj).reader);
			}

			return false;
		}

	}

	protected static class SearchTargetDialog implements ActionListener {
		protected final ContentType contentType;

		protected Object target;
		protected JPanel panel;

		protected Map<Extension, SearchTargetSelector> selectorInstances;

		public SearchTargetDialog(ContentType contentType) {
			if(contentType==null)
				throw new NullPointerException("Invalid content-type"); //$NON-NLS-1$

			this.contentType = contentType;
		}

		public void showDialog() {
			Collection<Extension> extensions = SearchManager.getInstance().availableTargetSelectors();
			if(extensions==null || extensions.isEmpty())
				throw new IllegalStateException("No target selectors available"); //$NON-NLS-1$

			panel = new JPanel(new BorderLayout());

			JToolBar toolBar = ActionManager.globalManager().createEmptyToolBar();;

			JLabel label = new JLabel();
			label.setText(ResourceManager.getInstance().get(
					"plugins.searchTools.searchManagerView.dialogs.selectTarget.label")); //$NON-NLS-1$
			label.setBorder(new EmptyBorder(1, 5, 1, 10));
			toolBar.add(label);

			JComboBox<Extension> cb = new JComboBox<>(
					new ExtensionListModel(extensions, true));
			cb.setEditable(false);
			cb.setRenderer(new ExtensionListCellRenderer());
			UIUtil.fitToContent(cb, 150, 250, 22);
			cb.addActionListener(this);
			toolBar.add(cb);

			panel.add(toolBar, BorderLayout.NORTH);
			panel.setPreferredSize(new Dimension(400, 300));

			cb.setSelectedIndex(0);

			target = null;
			if(DialogFactory.getGlobalFactory().showGenericDialog(null,
					"plugins.searchTools.searchManagerView.dialogs.selectTarget.title",  //$NON-NLS-1$
					null, panel, true, "ok", "cancel")) { //$NON-NLS-1$ //$NON-NLS-2$
				Extension extension = (Extension) cb.getSelectedItem();
				if(extension!=null) {
					target = getSelector(extension).getSelectedItem();
				}
			}
		}

		public Object getTarget() {
			return target;
		}

		protected SearchTargetSelector getSelector(Extension extension) {
			if(extension==null)
				throw new NullPointerException();

			if(selectorInstances==null) {
				selectorInstances = new HashMap<>();
			}

			SearchTargetSelector selector = selectorInstances.get(extension);
			if(selector==null) {
				try {
					selector = (SearchTargetSelector) PluginUtil.instantiate(extension);
					selector.setAllowedContentType(contentType);
					selectorInstances.put(extension, selector);
				} catch (Exception e) {
					LoggerFactory.log(this, Level.SEVERE,
							"Failed to instantiate target selector: "+extension.getUniqueId(), e); //$NON-NLS-1$
				}
			}

			return selector;
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox<?> cb = (JComboBox<?>) e.getSource();

			Extension extension = (Extension) cb.getSelectedItem();
			if(extension==null) {
				return;
			}

			SearchTargetSelector selector = getSelector(extension);
			if(selector==null) {
				UIUtil.beep();
				return;
			}

			if(panel.getComponentCount()>1) {
				panel.remove(1);
			}
			panel.add(selector.getSelectorComponent(), BorderLayout.CENTER);
			panel.revalidate();
			panel.repaint();
			selector.getSelectorComponent().requestFocusInWindow();
		}
	}

	protected class SearchEditor implements Editor<SearchDescriptor>, ActionListener {
		protected SearchDescriptor descriptor;

		protected FormBuilder formBuilder;

		protected Timer timer;

		protected SearchEditor() {
			Action a;

			formBuilder = FormBuilder.newLocalizingBuilder(new JPanel());
			// Factory
			a = getDefaultActionManager().getAction("plugins.searchTools.searchManagerView.selectFactoryAction"); //$NON-NLS-1$
			formBuilder.addEntry("factory", new SelectFormEntry( //$NON-NLS-1$
					"plugins.searchTools.searchManagerView.searchEditor.labels.factory", null, a)); //$NON-NLS-1$
			// Target
			a = getDefaultActionManager().getAction("plugins.searchTools.searchManagerView.selectTargetAction"); //$NON-NLS-1$
			formBuilder.addEntry("target", new SelectFormEntry( //$NON-NLS-1$
					"plugins.searchTools.searchManagerView.searchEditor.labels.target", null, a)); //$NON-NLS-1$
			// Query
			a = getDefaultActionManager().getAction("plugins.searchTools.searchManagerView.editQueryAction"); //$NON-NLS-1$
			formBuilder.addEntry("query", new SelectFormEntry( //$NON-NLS-1$
					"plugins.searchTools.searchManagerView.searchEditor.labels.query", null, a)); //$NON-NLS-1$
			// Parameters
			a = getDefaultActionManager().getAction("plugins.searchTools.searchManagerView.editParametersAction"); //$NON-NLS-1$
			formBuilder.addEntry("parameters", new SelectFormEntry( //$NON-NLS-1$
					"plugins.searchTools.searchManagerView.searchEditor.labels.parameters", null, a)); //$NON-NLS-1$

			formBuilder.buildForm();
			((JComponent)formBuilder.getContainer()).setBorder(UIUtil.topLineBorder);
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#getEditorComponent()
		 */
		@Override
		public Component getEditorComponent() {
			return formBuilder.getContainer();
		}

		public void refresh() {
			SearchDescriptor descriptor = getEditingItem();
			if(descriptor!=null) {
				setEditingItem(descriptor);
			}
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#setEditingItem(java.lang.Object)
		 */
		@Override
		public void setEditingItem(SearchDescriptor item) {
			if(item==null)
				throw new NullPointerException("Invalid search-descriptor"); //$NON-NLS-1$

			descriptor = item;

			Search search = descriptor.getSearch();
			if(search!=null && search.isRunning()) {
				if(timer==null) {
					timer = new Timer(1000, this);
				}
				timer.start();
			} else if(timer!=null) {
				timer.stop();
			}

			// Factory
			formBuilder.setValue("factory", PluginUtil.getIdentity(descriptor.getFactoryExtension())); //$NON-NLS-1$

			// Target
			String name = StringUtil.getName(descriptor.getTarget());
			if(name==null || name.isEmpty()) {
				name = ResourceManager.getInstance().get("plugins.searchTools.undefinedStats"); //$NON-NLS-1$
			}
			formBuilder.setValue("target", name); //$NON-NLS-1$

			// Query
			String query = descriptor.getSearchFactory().getQueryLabel(descriptor.getQuery());
			// Generate default label if factory does not require a specialized label
			if(query==null) {
				query = SearchUtils.getQueryStats(descriptor.getQuery());
			}
			if(query==null || query.isEmpty()) {
				query = ResourceManager.getInstance().get("plugins.searchTools.emptyStats"); //$NON-NLS-1$
			}
			formBuilder.setValue("query", query); //$NON-NLS-1$

			// Result
			/*String result = SearchUtils.getResultStats(descriptor.getSearchResult());
			if(result==null || result.isEmpty()) {
				result = ResourceManager.getInstance().get("plugins.searchTools.emptyStats"); //$NON-NLS-1$
			}
			formBuilder.setValue("result", result); //$NON-NLS-1$*/

			// Parameters
			// TODO provide any kind of textual representation?
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#getEditingItem()
		 */
		@Override
		public SearchDescriptor getEditingItem() {
			return descriptor;
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#resetEdit()
		 */
		@Override
		public void resetEdit() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#applyEdit()
		 */
		@Override
		public void applyEdit() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#hasChanges()
		 */
		@Override
		public boolean hasChanges() {
			return false;
		}

		/**
		 * @see de.ims.icarus.ui.helper.Editor#close()
		 */
		@Override
		public void close() {
			// no-op
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			SearchDescriptor descriptor = getEditingItem();
			if(descriptor!=null) {
				setEditingItem(descriptor);
			} else if(timer!=null) {
				timer.stop();
			}
		}
	}
}
