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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.io.Loadable;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.ExtensionListCellRenderer;
import net.ikarus_systems.icarus.plugins.ExtensionListModel;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.plugins.search_tools.SearchToolsConstants;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchDescriptor;
import net.ikarus_systems.icarus.search_tools.SearchFactory;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.search_tools.SearchQuery;
import net.ikarus_systems.icarus.search_tools.SearchTargetSelector;
import net.ikarus_systems.icarus.search_tools.corpus.CorpusSearchResultND;
import net.ikarus_systems.icarus.search_tools.result.Hit;
import net.ikarus_systems.icarus.search_tools.result.ResultEntry;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.search_tools.standard.DefaultConstraint;
import net.ikarus_systems.icarus.search_tools.standard.DefaultSearchOperator;
import net.ikarus_systems.icarus.search_tools.standard.GroupCache;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.config.ConfigDialog;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.dialog.FormBuilder;
import net.ikarus_systems.icarus.ui.dialog.SelectFormEntry;
import net.ikarus_systems.icarus.ui.helper.Editor;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.StringUtil;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;
import net.ikarus_systems.icarus.util.data.DataContainer;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;
import net.ikarus_systems.icarus.util.mpi.ResultMessage;

import org.java.plugin.registry.Extension;

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
		searchHistory = new SearchHistory();
		searchHistoryList = new JList<>(searchHistory);
		searchHistoryList.setBorder(UIUtil.defaultContentBorder);
		searchHistoryList.setCellRenderer(new SearchHistoryListCellRenderer());
		searchHistoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchHistoryList.addListSelectionListener(handler);
		UIUtil.enableToolTip(searchHistoryList);
		JScrollPane scrollPane = new JScrollPane(searchHistoryList);
		scrollPane.setBorder(UIUtil.topLineBorder);
		UIUtil.defaultSetUnitIncrement(scrollPane);
		
		JPanel historyPanel = new JPanel(new BorderLayout());
		historyPanel.setBorder(UIUtil.topLineBorder);
		historyPanel.add(toolBar, BorderLayout.NORTH);
		historyPanel.add(scrollPane, BorderLayout.CENTER);
		
		container.setLayout(new BorderLayout());
		container.add(editorPanel, BorderLayout.NORTH);
		container.add(historyPanel, BorderLayout.CENTER);
		
		registerActionCallbacks();

		refreshActions();
	}
	
	protected void refreshActions() {
		ActionManager actionManager = getDefaultActionManager();
		
		// Refresh editor actions
		SearchDescriptor descriptor = currentSearchEditor.getEditingItem();
		Search search = descriptor==null ? null : descriptor.getSearch();

		boolean canRun = descriptor!=null && descriptor.getTarget()!=null;
		
		actionManager.setEnabled(canRun, 
				"plugins.searchTools.searchManagerView.executeSearchAction"); //$NON-NLS-1$
		
		// Refresh history actions
		descriptor = searchHistoryList.getSelectedValue();
		boolean selected = descriptor!=null;
		search = selected ? descriptor.getSearch() : null;
		boolean canCancel = search!=null && search.isRunning();
		boolean hasResult = search!=null && search.getResult()!=null;
		
		actionManager.setEnabled(hasResult,
				"plugins.searchTools.searchManagerView.viewResultAction"); //$NON-NLS-1$		
		actionManager.setEnabled(canCancel,
				"plugins.searchTools.searchManagerView.cancelSearchAction"); //$NON-NLS-1$		
		actionManager.setEnabled(searchHistory.getSize()>0, 
				"plugins.searchTools.searchManagerView.clearHistoryAction"); //$NON-NLS-1$
		actionManager.setEnabled(selected, 
				"plugins.searchTools.searchManagerView.viewSearchAction",  //$NON-NLS-1$
				"plugins.searchTools.searchManagerView.removeSearchAction"); //$NON-NLS-1$
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
	}
	
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.SELECT.equals(message.getCommand())) {
			Object data = message.getData();
			if(data instanceof SearchDescriptor) {
				SearchDescriptor descriptor = (SearchDescriptor)data;
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
		}
		
		refreshActions();
	}
	
	protected SearchDescriptor newDescriptor() {
		Collection<Extension> factoryExtensions = SearchManager.getSearchFactoryExtensions();
		if(factoryExtensions==null || factoryExtensions.isEmpty())
			throw new IllegalStateException("Cannot create search descriptor - no search factories available"); //$NON-NLS-1$
		
		SearchDescriptor descriptor = new SearchDescriptor();
		descriptor.setFactoryExtension(factoryExtensions.iterator().next());
		
		return descriptor;
	}

	private void debugShowResult() throws Exception {
		SearchDescriptor descriptor = currentSearchEditor.getEditingItem();
		descriptor.createSearch();
		
		Object target = descriptor.getTarget();
		if(target instanceof Loadable && !((Loadable)target).isLoaded()) {
			try {
				((Loadable)target).load();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		SearchConstraint[] groupConstraints = {
				new DefaultConstraint("form", 1, DefaultSearchOperator.GROUPING),
				new DefaultConstraint("pos", 3, DefaultSearchOperator.GROUPING),
		};
		CorpusSearchResultND result = new CorpusSearchResultND(descriptor.getSearch(), groupConstraints);
		GroupCache cache = result.createCache();
		Hit[] hits;
		
		hits = new Hit[]{
			new Hit(new int[]{0, 3, 7}),
		};
		cache.cacheGroupInstance(0, "test");
		cache.cacheGroupInstance(1, "x");
		cache.commit(new ResultEntry(12, hits));
		
		cache.cacheGroupInstance(0, "bla");
		cache.cacheGroupInstance(1, "x");
		cache.commit(new ResultEntry(11, hits));
		
		cache.cacheGroupInstance(0, "test2");
		cache.cacheGroupInstance(1, "y");
		cache.commit(new ResultEntry(7, hits));
		
		cache.cacheGroupInstance(0, "test2");
		cache.cacheGroupInstance(1, "xy");
		cache.commit(new ResultEntry(14, hits));
		
		//result.finish();
		
		descriptor.setSearchResult(result);
		
		currentSearchEditor.setEditingItem(descriptor);
		
		Options options = new Options();
		Message message = new Message(this, Commands.PRESENT, result, options);
		
		sendRequest(SearchToolsConstants.SEARCH_RESULT_VIEW_ID, message);
	}

	protected class Handler implements ListSelectionListener, PropertyChangeListener {
		
		protected Search observedSearch;
		
		protected Handler() {
			// no-op
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
			}
		}
		
		public void executeSearch(ActionEvent e) {
			if(currentSearchEditor.getEditingItem()==null) {
				return;
			}
			
			try {
				syncEditorViews();
				
				SearchDescriptor descriptor = currentSearchEditor.getEditingItem(); 
				Search search = descriptor.getSearch();
				if(search!=null && search.isRunning()) {
					UIUtil.beep();
					return;
				}
				
				// TODO allow for execution of already running searches
				
				// Create a new descriptor without a search object
				SearchDescriptor clone = descriptor.cloneFlat();
				// Let factory create a blank new search object
				clone.createSearch();
				//currentSearchEditor.setEditingItem(clone);
				
				searchHistory.addSearch(clone);
				
				SearchManager.getInstance().executeSearch(clone.getSearch());
				
				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to execute search", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void openPreferences(ActionEvent e) {
			try {
				new ConfigDialog(ConfigRegistry.getGlobalRegistry(), 
						"plugins.searchTools").setVisible(true); //$NON-NLS-1$
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to open preferences", ex); //$NON-NLS-1$
				UIUtil.beep();
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
				currentSearchEditor.refresh();

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to select new search factory", ex); //$NON-NLS-1$
				UIUtil.beep();
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
			}
		}
		
		public void editParameters(ActionEvent e) {
			if(currentSearchEditor.getEditingItem()==null) {
				return;
			}
			
			try {
				SearchDescriptor descriptor = currentSearchEditor.getEditingItem();
				
				Editor<Options> editor = descriptor.getSearchFactory().createParameterEditor();
				
				if(DialogFactory.getGlobalFactory().showEditorDialog(
						getFrame(), descriptor.getParameters(), editor, 
						"plugins.searchTools.searchManagerView.dialogs.editParameters.title")) { //$NON-NLS-1$
					descriptor.setParameters(editor.getEditingItem());
				}
				
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to edit paramters", ex); //$NON-NLS-1$
				UIUtil.beep();
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
			}
		}
		
		public void viewSearch(ActionEvent e) {
			SearchDescriptor descriptor = searchHistoryList.getSelectedValue();
			if(descriptor==null) {
				return;
			}
			
			try {
				SearchDescriptor clone = descriptor.cloneFlat();
				currentSearchEditor.setEditingItem(clone);

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to edit selected search", ex); //$NON-NLS-1$
				UIUtil.beep();
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
				
				int index = searchHistoryList.getSelectedIndex();
				searchHistory.removeSearch(descriptor);
				
				Search search = descriptor.getSearch();
				if(search!=null) {
					SearchManager.getInstance().cancelSearch(search);
				}
				
				// Maintain selected index to allow for more fluent
				// use of selection related actions
				if(index!=-1 && index<searchHistory.getSize()) {
					searchHistoryList.setSelectedIndex(index);
				} else {
					refreshActions();
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to remove selected search from history", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void clearHistory(ActionEvent e) {
			try {
				// Abort if there is a search still active and running
				for(int i=0; i<searchHistory.getSize(); i++) {
					if(searchHistory.getElementAt(i).isActive()) {
						DialogFactory.getGlobalFactory().showWarning(getFrame(), 
								"plugins.searchTools.searchManagerView.dialogs.removeSearch.title",  //$NON-NLS-1$
								"plugins.searchTools.searchManagerView.dialogs.removeSearch.message"); //$NON-NLS-1$
						return;
					}
				}
				
				searchHistory.clear();

				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to clear search history", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
	}
	
	protected static class SearchTargetDialog implements ActionListener {
		protected final ContentType contentType;
		
		protected Object target;
		protected JPanel panel;
		
		protected Map<Extension, SearchTargetSelector> selectorInstances;
		
		public SearchTargetDialog(ContentType contentType) {
			if(contentType==null)
				throw new IllegalArgumentException("Invalid content-type"); //$NON-NLS-1$
			
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
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#getEditorComponent()
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
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#setEditingItem(java.lang.Object)
		 */
		@Override
		public void setEditingItem(SearchDescriptor item) {
			if(item==null)
				throw new IllegalArgumentException("Invalid search-descriptor"); //$NON-NLS-1$
						
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
			String query = SearchUtils.getQueryStats(descriptor.getQuery());
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
			// no-op
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#applyEdit()
		 */
		@Override
		public void applyEdit() {
			// no-op
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#hasChanges()
		 */
		@Override
		public boolean hasChanges() {
			return false;
		}

		/**
		 * @see net.ikarus_systems.icarus.ui.helper.Editor#close()
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
