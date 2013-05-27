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
import java.util.List;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.view.UnsupportedPresentationDataException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.mpi.Message;
import net.ikarus_systems.icarus.util.mpi.ResultMessage;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchResultView extends View {
	
	private SearchResultPresenter resultPresenter;
	private JTextArea infoLabel;

	public SearchResultView() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		container.setLayout(new BorderLayout());
		
		infoLabel = UIUtil.defaultCreateInfoLabel(container);
		
		showInfo(null);
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
		if(resultPresenter!=null) {
			resultPresenter.clear();
		}
	}
	
	public void displayData(SearchResult searchResult, Options options) {
		if(searchResult==null)
			throw new IllegalArgumentException("Invalid search-result"); //$NON-NLS-1$
		
		SearchResultPresenter resultPresenter = this.resultPresenter;
		
		// Try to find a suitable presenter if the current one is incapable
		if(resultPresenter==null || resultPresenter.getSupportedDimension()!=searchResult.getDimension()) {
			List<Extension> availablePresenters = SearchManager.getResultPresenterExtensions(
					searchResult.getDimension());
			
			Extension extension = null;
			
			if(availablePresenters.isEmpty()) {
				// Not available
				resultPresenter = null;
			} else if(availablePresenters.size()==1) {
				// Only one choice -> take it
				extension = availablePresenters.get(0);
			} else {
				// Let user decide
				// TODO maybe save user choice?
				extension = PluginUtil.showExtensionDialog(getFrame(), 
						"plugins.searchTools.searchResultView.selectPresenter",  //$NON-NLS-1$
						availablePresenters, true);
			}
			
			if (extension!=null) {
				try {
					resultPresenter = (SearchResultPresenter) PluginUtil.instantiate(extension);
				} catch (Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to instantiate presenter from extension: "+extension.getUniqueId(), e); //$NON-NLS-1$
				}
			}
		}
		
		// TODO present option to use fallback-presenter that does not care about dimensions?
		
		// Abort and clear view if something went wrong
		if(resultPresenter==null) {
			showInfo(ResourceManager.getInstance().get(
					"plugins.searchTools.searchResultView.unsupportedDimension", searchResult.getDimension())); //$NON-NLS-1$
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
			return;
		}
		
		// Finally switch visible components if required
		if(this.resultPresenter!=resultPresenter) {
			getContainer().remove(this.resultPresenter.getPresentingComponent());
			this.resultPresenter.close();
			
			this.resultPresenter = resultPresenter;
			getContainer().add(this.resultPresenter.getPresentingComponent(), BorderLayout.CENTER);
		}
	}
	
	private void showInfo(String text) {
		if(text==null) {
			text = ResourceManager.getInstance().get(
					"plugins.searchTools.searchResultView.notAvailable"); //$NON-NLS-1$
		}
		
		infoLabel.setText(text);
		
		getContainer().removeAll();
		getContainer().add(infoLabel, BorderLayout.NORTH);
		
		if(resultPresenter!=null) {
			resultPresenter.close();
			resultPresenter = null;
		}
	}

	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		// TODO Auto-generated method stub
		return super.handleRequest(message);
	}

}
