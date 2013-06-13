/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import net.ikarus_systems.icarus.Core;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.plugins.search_tools.SearchToolsConstants;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.SearchDescriptor;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.search_tools.SearchQuery;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.dialog.FormBuilder;
import net.ikarus_systems.icarus.ui.dialog.SelectFormEntry;
import net.ikarus_systems.icarus.ui.helper.Editor;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.StringUtil;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchManagerView extends View {
	
	protected JTree searchHistoryTree;
	protected SearchHistoryTreeModel searchHistoryTreeModel;
	
	protected SearchEditor currentSearchEditor;
	
	protected Handler handler;
	
	protected CallbackHandler callbackHandler;

	public SearchManagerView() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
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
		URL actionLocation = SearchManagerView.class.getResource("search-manager-view-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: search-manager-view-actions.xml"); //$NON-NLS-1$
		
		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}
		
		handler = createHandler();
		
		// Current search editor
		currentSearchEditor = new SearchEditor();
		// Set initially empty search
		SearchDescriptor descriptor = new SearchDescriptor();
		descriptor.setFactoryExtension(factoryExtensions.iterator().next());
		currentSearchEditor.setEditingItem(descriptor);
		
		// History
		searchHistoryTreeModel = new SearchHistoryTreeModel();
		searchHistoryTree = new JTree(searchHistoryTreeModel);
		searchHistoryTree.setEditable(false);
		searchHistoryTree.setRootVisible(false);
		searchHistoryTree.setShowsRootHandles(true);
		searchHistoryTree.addTreeSelectionListener(handler);
		searchHistoryTree.setCellRenderer(new SearchHistoryTreeCellRenderer());
		JScrollPane scrollPane = new JScrollPane(searchHistoryTree);
		scrollPane.setBorder(UIUtil.topLineBorder);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(currentSearchEditor.getEditorComponent(), BorderLayout.NORTH);
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		
		container.setLayout(new BorderLayout());
		container.add(createToolBar(), BorderLayout.NORTH);
		container.add(contentPanel, BorderLayout.CENTER);
		
		registerActionCallbacks();

		refreshActions();
	}
	
	protected void refreshActions() {
		// no-op
	}
	
	protected void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = createCallbackHandler();
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
		actionManager.addHandler("plugins.searchTools.searchManagerView.viewResultAction",  //$NON-NLS-1$
				callbackHandler, "viewResult"); //$NON-NLS-1$
	}
	
	protected JToolBar createToolBar() {
		return getDefaultActionManager().createToolBar(
				"plugins.searchTools.searchManagerView.toolBarList", null); //$NON-NLS-1$
	}
	
	protected Handler createHandler() {
		return new Handler();
	}
	
	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandler();
	}

	protected class Handler implements TreeSelectionListener {
		protected Handler() {
			// no-op
		}

		/**
		 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
		 */
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public class CallbackHandler {
		protected CallbackHandler() {
			// no-op
		}
		
		public void newSearch(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		public void executeSearch(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		public void cancelSearch(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		public void removeSearch(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		public void clearHistory(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		public void openPreferences(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
		
		public void editQuery(ActionEvent e) {
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
			}
			
		}
		
		public void selectFactory(ActionEvent e) {
			if(currentSearchEditor.getEditingItem()==null) {
				return;
			}
			
			try {
				Collection<Extension> extensions = SearchManager.getInstance().availableSearchFactories();
				Extension extension = PluginUtil.showExtensionDialog(getFrame(), 
						"plugins.searchTools.searchManagerView.dialogs.selectFactory.title",  //$NON-NLS-1$
						extensions, true);

				if(extension==null) {
					return;
				}
				
				currentSearchEditor.getEditingItem().setFactoryExtension(extension);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to select new search factory", ex); //$NON-NLS-1$
			}
		}
		
		public void viewResult(ActionEvent e) {
			// TODO
			Core.showNotice();
		}
	}
	
	protected class SearchEditor implements Editor<SearchDescriptor> {
		protected SearchDescriptor descriptor;
		
		protected FormBuilder formBuilder;
		
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
			// Result
			a = getDefaultActionManager().getAction("plugins.searchTools.searchManagerView.viewResultAction"); //$NON-NLS-1$
			formBuilder.addEntry("result", new SelectFormEntry( //$NON-NLS-1$
					"plugins.searchTools.searchManagerView.searchEditor.labels.result", null, a)); //$NON-NLS-1$
			
			formBuilder.buildForm();
			((JComponent)formBuilder.getContainer()).setBorder(UIUtil.topLineBorder);
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#getEditorComponent()
		 */
		@Override
		public Component getEditorComponent() {
			return formBuilder.getContainer();
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#setEditingItem(java.lang.Object)
		 */
		@Override
		public void setEditingItem(SearchDescriptor item) {
			if(item==null)
				throw new IllegalArgumentException("Invalid search-descriptor"); //$NON-NLS-1$
			
			descriptor = item;
			
			// Factory
			formBuilder.setValue("factory", PluginUtil.getIdentity(descriptor.getFactoryExtension())); //$NON-NLS-1$
			
			// Target
			String name = StringUtil.getName(descriptor.getTarget());
			if(name==null || name.isEmpty()) {
				name = ResourceManager.getInstance().get("plugins.searchTools.undefinedStats"); //$NON-NLS-1$
			}
			//TODO DUMMY
			//name = "Treebank [english_conll08]"; //$NON-NLS-1$
			formBuilder.setValue("target", name); //$NON-NLS-1$
			
			// Query
			String query = SearchUtils.getQueryStats(descriptor.getQuery());
			if(query==null || query.isEmpty()) {
				query = ResourceManager.getInstance().get("plugins.searchTools.emptyStats"); //$NON-NLS-1$
			}
			// TODO DUMMY
			//query = "2 nodes, 1 edge, 1 root"; //$NON-NLS-1$
			formBuilder.setValue("query", query); //$NON-NLS-1$
			
			// Result
			String result = SearchUtils.getResultStats(descriptor.getSearchResult());
			if(result==null || result.isEmpty()) {
				result = ResourceManager.getInstance().get("plugins.searchTools.emptyStats"); //$NON-NLS-1$
			}
			formBuilder.setValue("result", result); //$NON-NLS-1$
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#getEditingItem()
		 */
		@Override
		public SearchDescriptor getEditingItem() {
			return descriptor;
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#resetEdit()
		 */
		@Override
		public void resetEdit() {
			// TODO Auto-generated method stub
			
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#applyEdit()
		 */
		@Override
		public void applyEdit() {
			// TODO Auto-generated method stub
			
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#hasChanges()
		 */
		@Override
		public boolean hasChanges() {
			// TODO Auto-generated method stub
			return false;
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#close()
		 */
		@Override
		public void close() {
			// no-op
		}
	}
}
