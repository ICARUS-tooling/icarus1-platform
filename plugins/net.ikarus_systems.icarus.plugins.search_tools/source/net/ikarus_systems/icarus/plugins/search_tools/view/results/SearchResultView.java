/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view.results;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.Search;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.view.UnsupportedPresentationDataException;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.MutablePrimitives.MutableBoolean;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;
import net.ikarus_systems.icarus.util.mpi.ResultMessage;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchResultView extends View {
	
	// TODO add general toolbar with details about match count and groups (maybe hideable header panel?)
	
	private SearchResultPresenter resultPresenter;
	private JTextArea infoLabel;
	
	private JPanel contentPanel;
	private JToolBar toolBar;
	private boolean toolBarExpanded;
	
	private JLabel hitCountLabel;
	
	private Timer refreshTimer;
	
	private Handler handler;
	private CallbackHandler callbackHandler;

	public SearchResultView() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// Load actions
		URL actionLocation = SearchResultView.class.getResource("search-result-view-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: search-result-view-actions.xml"); //$NON-NLS-1$
		
		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}
		
		handler = new Handler();
		
		container.setLayout(new BorderLayout());
		
		infoLabel = UIUtil.defaultCreateInfoLabel(container);
		
		hitCountLabel = new JLabel();
		hitCountLabel.setBorder(UIUtil.defaultContentBorder);
		hitCountLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		hitCountLabel.setIconTextGap(5);
		
		contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBorder(UIUtil.topLineBorder);
		
		toolBar = getDefaultActionManager().createEmptyToolBar();
		feedToolBar();
		
		container.add(toolBar, BorderLayout.NORTH);
		container.add(contentPanel, BorderLayout.CENTER);
		
		registerActionCallbacks();
		
		showInfo(null);
	}
	
	private void feedToolBar() {		
		Options options = new Options();
		options.put("countLabel", hitCountLabel); //$NON-NLS-1$
		options.put("multiline", true); //$NON-NLS-1$
		getDefaultActionManager().feedToolBar("plugins.searchTools.searchResultView.toolBarList",  //$NON-NLS-1$
				toolBar, options);
	}
	
	private void refreshActions() {
		ActionManager actionManager = getDefaultActionManager();
		
		boolean hasResult = resultPresenter!=null;
		
		actionManager.setEnabled(hasResult, 
				"plugins.searchTools.searchResultView.refreshAction",  //$NON-NLS-1$
				"plugins.searchTools.searchResultView.viewSearchAction",  //$NON-NLS-1$
				"plugins.searchTools.searchResultView.saveResultAction",  //$NON-NLS-1$
				"plugins.searchTools.searchResultView.clearViewAction"); //$NON-NLS-1$
	}
	
	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();

		actionManager.addHandler("plugins.searchTools.searchResultView.refreshAction",  //$NON-NLS-1$
				callbackHandler, "refresh"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchResultView.openPreferencesAction",  //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchResultView.viewSearchAction",  //$NON-NLS-1$
				callbackHandler, "viewSearch"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchResultView.saveResultAction",  //$NON-NLS-1$
				callbackHandler, "saveResult"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchResultView.openResultAction",  //$NON-NLS-1$
				callbackHandler, "openResult"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchResultView.clearViewAction",  //$NON-NLS-1$
				callbackHandler, "clearView"); //$NON-NLS-1$
	}

	@Override
	public void close() {
		if(resultPresenter!=null) {
			resultPresenter.close();
			resultPresenter = null;
		}
	}

	@Override
	public void reset() {
		showInfo(null);
	}
	
	public static String getTitle(SearchResult searchResult, int[] indices) {
		if(indices==null)
			throw new IllegalArgumentException("Invalid indices"); //$NON-NLS-1$
		
		if(indices.length==0) {
			return "..."; //$NON-NLS-1$
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i<indices.length; i++) {
			if(i>0) {
				sb.append("/"); //$NON-NLS-1$
			}
			sb.append(searchResult.getInstanceLabel(i, indices[i]));
		}
		
		return sb.toString();
	}
	
	public static SearchResultPresenter getFalbackPresenter(SearchResult searchResult) {
		return new DefaultFallbackResultPresenter();
	}
	
	public static SearchResultPresenter getPresenter(SearchResult searchResult) {
		
		// Check if result is to be displayed in plain form first
		if(Boolean.TRUE.equals(searchResult.getProperty(
				SearchResult.FORCE_SIMPLE_OUTLINE_PROPERTY))) {
			return getFalbackPresenter(searchResult);
		}
		
		// Result requires regular visualization, so find a presenter
		List<Extension> availablePresenters = SearchManager.getResultPresenterExtensions(
				searchResult.getContentType(), searchResult.getDimension());
		
		Extension extension = null;
		SearchResultPresenter resultPresenter = null;
		
		if(availablePresenters.isEmpty()) {
			// Not available
			extension = null;
		} else if(availablePresenters.size()==1) {
			// Only one choice -> take it
			extension = availablePresenters.get(0);
		} else {
			// Let user decide
			// TODO maybe save user choice?
			extension = PluginUtil.showExtensionDialog(null, 
					"plugins.searchTools.searchResultView.selectPresenter",  //$NON-NLS-1$
					availablePresenters, true);
		}
		
		if (extension!=null) {
			try {
				resultPresenter = (SearchResultPresenter) PluginUtil.instantiate(extension);
			} catch (Exception e) {
				LoggerFactory.log(SearchResultView.class, Level.SEVERE, 
						"Failed to instantiate presenter from extension: "+extension.getUniqueId(), e); //$NON-NLS-1$
			}
		}
		
		return resultPresenter;
	}
	
	public void displayResult(SearchResult searchResult, Options options) {
		if(searchResult==null)
			throw new IllegalArgumentException("Invalid search-result"); //$NON-NLS-1$
		
		selectViewTab();
		
		if(refreshTimer!=null) {
			refreshTimer.stop();
		}
		
		if(toolBarExpanded) {
			toolBar.removeAll();
			feedToolBar();
		}
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		SearchResultPresenter resultPresenter = this.resultPresenter;
		
		// Try to find a suitable presenter if the current one is incapable
		if(resultPresenter==null 
				|| !resultPresenter.supportsEntryType(searchResult.getContentType()) 
				|| resultPresenter.getSupportedDimensions()!=searchResult.getDimension()) {
			resultPresenter = getPresenter(searchResult);
		}
		
		// Check permission to use fallback presenter
		if(resultPresenter==null) {
			boolean useFallback = options.get(SearchResult.FORCE_SIMPLE_OUTLINE_PROPERTY, false);
			if(!useFallback) {
				useFallback = ConfigRegistry.getGlobalRegistry().getBoolean(
						"plugins.searchTools.alwaysUseFallbackPresenter"); //$NON-NLS-1$
			}
			if(!useFallback) {
				MutableBoolean check = new MutableBoolean(false);
				useFallback = DialogFactory.getGlobalFactory().showCheckedConfirm(
						getFrame(), check, 
						"plugins.searchTools.searchResultView.dialogs.title",  //$NON-NLS-1$
						null,
						"plugins.searchTools.searchResultView.dialogs.useFallbackPresenter", //$NON-NLS-1$
						searchResult.getDimension());
				
				if(check.getValue()) {
					ConfigRegistry.getGlobalRegistry().setValue(
							"plugins.searchTools.alwaysUseFallbackPresenter",  //$NON-NLS-1$
							true);
				}
			}
			
			if(useFallback) {
				resultPresenter = getFalbackPresenter(searchResult);
			}
		}
		
		// Abort and clear view if something went wrong
		if(resultPresenter==null) {
			showInfo(ResourceManager.getInstance().get(
					"plugins.searchTools.searchResultView.unsupportedDimension", searchResult.getDimension())); //$NON-NLS-1$
			UIUtil.beep();
			return;
		}
		
		// Attempt to present result and abort if it failed
		try {
			resultPresenter.present(searchResult, options);
		} catch (UnsupportedPresentationDataException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to present search result", e); //$NON-NLS-1$
			showInfo(ResourceManager.getInstance().get(
					"plugins.searchTools.searchResultView.presentationFailed")); //$NON-NLS-1$
			UIUtil.beep();
			return;
		}
		
		if(!searchResult.isFinal()) {
			if(refreshTimer==null) {
				refreshTimer = new Timer(1000, handler);
			}
			refreshTimer.start();
		}
		
		int toolBarItemCount = toolBar.getComponentCount();
		resultPresenter.exportToolBarItems(toolBar);
		toolBarExpanded = toolBarItemCount!=toolBar.getComponentCount();
		
		if(!searchResult.isFinal()) {
			Search search = searchResult.getSource();
			if(search!=null) {
				search.removePropertyChangeListener(handler);
				search.addPropertyChangeListener("state", handler); //$NON-NLS-1$
			}
		}
		
		if(this.resultPresenter==resultPresenter) {
			refreshHitCount();
			return;
		}
		
		contentPanel.remove(infoLabel);
		
		// Switch visible components if required
		if(this.resultPresenter!=null) {
			contentPanel.remove(this.resultPresenter.getPresentingComponent());
			this.resultPresenter.close();
		}
		
		this.resultPresenter = resultPresenter;
	
		contentPanel.add(this.resultPresenter.getPresentingComponent(), BorderLayout.CENTER);
		contentPanel.revalidate();
		contentPanel.repaint();
		
		refreshHitCount();
		refreshActions();
	}
	
	private void showInfo(String text) {
		if(text==null) {
			text = ResourceManager.getInstance().get(
					"plugins.searchTools.searchResultView.notAvailable"); //$NON-NLS-1$
		}
		
		infoLabel.setText(text);
		
		contentPanel.removeAll();
		contentPanel.add(infoLabel, BorderLayout.NORTH);
		contentPanel.revalidate();
		contentPanel.repaint();
		
		if(resultPresenter!=null) {
			resultPresenter.close();
			resultPresenter = null;
		}
		
		refreshHitCount();
		refreshActions();
	}
	
	private void refreshHitCount() {
		String s = "-"; //$NON-NLS-1$
		Icon icon = null;
		if(resultPresenter!=null) {
			SearchResult searchResult = resultPresenter.getSearchResult();
			
			s = SearchResultPresenter.getHitCountString(searchResult);

			// Display 'loading' icon only when the result is backed up by an active search
			Search search = searchResult.getSource();
			if(!searchResult.isFinal() && search!=null && search.isRunning()) {
				icon = IconRegistry.getGlobalRegistry().getIcon("ajax-loader_16.gif"); //$NON-NLS-1$
			}
		}
		
		hitCountLabel.setIcon(icon);
		
		hitCountLabel.setText(s);
	}

	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.DISPLAY.equals(message.getCommand())
				|| Commands.PRESENT.equals(message.getCommand())) {
			
			Object data = message.getData();
			if(data instanceof SearchResult) {
				displayResult((SearchResult) data, message.getOptions());
				return message.successResult(this, null);
			} else {
				return message.unsupportedDataResult(this);
			}
			
		} else if(Commands.CLEAR.equals(message.getCommand())) {
			reset();
			return message.successResult(this, null);
		} else {
			return message.unknownRequestResult(this);
		}
	}
	
	protected class Handler implements ActionListener, PropertyChangeListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {			
			try {
				if(resultPresenter!=null) {
					resultPresenter.refresh();
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to perform cyclic refresh", ex); //$NON-NLS-1$
			} finally {
				SearchResult searchResult = resultPresenter==null ? null : resultPresenter.getSearchResult();
				if(searchResult==null || searchResult.isFinal()) {
					refreshTimer.stop();
				}
				refreshHitCount();
			}
		}

		/**
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			refreshHitCount();
		}
		
	}

	public class CallbackHandler {
		
		protected CallbackHandler() {
			// no-op
		}
		
		public void refresh(ActionEvent e) {
			if(resultPresenter==null) {
				return;
			}
			
			try {
				resultPresenter.refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to refresh result presenter", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void openPreferences(ActionEvent e) {
			if(resultPresenter==null) {
				return;
			}
			
			try {
				resultPresenter.openPreferences();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to open preferences", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void viewSearch(ActionEvent e) {
			if(resultPresenter==null) {
				return;
			}
			
			try {
				// TODO fetch search descriptor and send to manager view!
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to view search", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void saveResult(ActionEvent e) {
			if(resultPresenter==null) {
				return;
			}
			
			try {
				// TODO 
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to save result", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void openResult(ActionEvent e) {			
			try {
				// TODO 
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to open result", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void clearView(ActionEvent e) {			
			try {
				reset();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to clear view", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
	}
}
