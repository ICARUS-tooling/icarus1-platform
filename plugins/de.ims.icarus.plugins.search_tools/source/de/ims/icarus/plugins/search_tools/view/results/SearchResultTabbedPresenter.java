/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.search_tools.view.results;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.tab.ButtonTabComponent;
import de.ims.icarus.ui.tab.TabController;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.cache.LRUCache;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class SearchResultTabbedPresenter extends SearchResultTablePresenter {

	protected JTabbedPane tabbedPane;

	protected JPanel overviewPanel;
	
	protected Map<Object, Reference<SearchResult>> subResults;
	
	protected SearchResultTabbedPresenter() {
		// TODO Auto-generated constructor stub
	}
	
	protected void expandView() {
		if(tabbedPane!=null) {
			return;
		}

		tabbedPane = createTabbedPane();
		String title = ResourceManager.getInstance().get(
				"plugins.searchTools.searchResultPresenter.labels.overview"); //$NON-NLS-1$

		contentPanel.remove(overviewPanel);
		tabbedPane.insertTab(title, null, overviewPanel, null, 0);
		
		contentPanel.add(tabbedPane, BorderLayout.CENTER);
	}
	
	protected void shrinkView() {
		if(tabbedPane==null) {
			return;
		}
		
		for(int i=1; i<tabbedPane.getTabCount(); i++) {
			SubResultContainer container = (SubResultContainer)tabbedPane.getComponentAt(i);
			container.close();
		}
		tabbedPane.removeAll();
		contentPanel.remove(tabbedPane);
		
		contentPanel.add(overviewPanel, BorderLayout.CENTER);
		tabbedPane = null;
	}

	@Override
	public void clear() {
		super.clear();
		
		shrinkView();
	}

	protected void checkViewMode(boolean instertionPending) {
		int tabCount = tabbedPane==null ? 0 : tabbedPane.getTabCount();
		
		if(tabCount==0 && instertionPending) {
			// Expan
			expandView();
		} else if(tabCount==1 && !instertionPending) {
			// Shrink
			shrinkView();
		}
	}
	
	protected JTabbedPane createTabbedPane() {
		return new ClosableTabbedPane();
	}
	
	protected SearchResult getCachedSubResult(int...indices) {
		if(subResults==null) {
			return null;
		}
		Reference<SearchResult> ref = subResults.get(Arrays.toString(indices));
		return ref==null ? null : ref.get();
	}
	
	protected void cacheSubResult(SearchResult subResult, int...indices) {
		if(subResult==null)
			throw new IllegalArgumentException("invalid sub result"); //$NON-NLS-1$
		
		if(subResults==null) {
			subResults = new LRUCache<>();
		}
		
		subResults.put(Arrays.toString(indices), new WeakReference<>(subResult));
	}
	
	protected int getSubResultIndex(SearchResult subResult) {
		if(tabbedPane==null) {
			return -1;
		}
		
		for(int i=1; i<tabbedPane.getTabCount(); i++) {
			SubResultContainer container = (SubResultContainer) tabbedPane.getComponentAt(i);
			if(subResult==container.getSubResult()) {
				return i;
			}
		}
				
		return -1;
	}
	
	protected SearchResult getMainResult() {
		return getSearchResult();
	}
	
	protected void displaySelectedSubResult(int[] indices, String label) {
		if(indices==null)
			throw new IllegalArgumentException("Invalid indices"); //$NON-NLS-1$
		
		SearchResult subResult = getCachedSubResult(indices);
		if(subResult==null) {
			TaskManager.getInstance().schedule(new SubResultDisplayJob(
					indices, label), TaskPriority.DEFAULT, true);
			return;
		}
		
		checkViewMode(true);
		
		int index = getSubResultIndex(subResult);
		if(index==-1) {
			index = tabbedPane.getTabCount();
			
			SubResultContainer container = new SubResultContainer(label, subResult);
			
			container.init();
			
			tabbedPane.insertTab(label, null, container, null, index);
			tabbedPane.setTabComponentAt(index, new ButtonTabComponent(tabbedPane));
		}

		tabbedPane.setSelectedIndex(index);
	}
	
	protected class ClosableTabbedPane extends JTabbedPane implements TabController {

		private static final long serialVersionUID = -8989316268794923006L;

		/**
		 * @see de.ims.icarus.ui.tab.TabController#closeTab(java.awt.Component)
		 */
		@Override
		public boolean closeTab(Component comp) {
			if(comp instanceof SubResultContainer) {
				SubResultContainer container = (SubResultContainer) comp;
				container.close();
			}
			
			remove(comp);
			checkViewMode(false);
			
			return true;
		}

		/**
		 * @see de.ims.icarus.ui.tab.TabController#closeChildren(java.awt.Component)
		 */
		@Override
		public boolean closeChildren(Component comp) {
			// Not supported
			return false;
		}
	}
	
	protected class SubResultContainer extends JPanel {

		private static final long serialVersionUID = -124096642718184615L;
		
		private final SearchResult subResult;
		private SearchResultPresenter resultPresenter;
		private final String title;

		public SubResultContainer(String title, SearchResult subResult) {
			super(new BorderLayout());
			if(title==null) 
				throw new IllegalArgumentException("Invalid title"); //$NON-NLS-1$
			if(subResult==null) 
				throw new IllegalArgumentException("Invalid sub-result"); //$NON-NLS-1$
			
			this.title = title;
			this.subResult = subResult;
		}
		
		public SearchResult getSubResult() {
			return subResult;
		}
		
		public SearchResultPresenter getResultPresenter() {
			return resultPresenter;
		}
		
		public String getTitle() {
			return title;
		}
		
		public void init() {
			resultPresenter = SearchResultView.getPresenter(getSubResult());
			if(resultPresenter==null) {
				resultPresenter = SearchResultView.getFallbackPresenter(getSubResult());
			}
			
			try {
				String title = ResourceManager.getInstance().get(
						"plugins.searchTools.default2DResultPresenter.instancesTitle", //$NON-NLS-1$
						getTitle());
				
				Options options = new Options();
				options.put(Options.TITLE, title);
				resultPresenter.present(getSubResult(), options);
				
				add(resultPresenter.getPresentingComponent(), BorderLayout.CENTER);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to init presenter for sub-result", e); //$NON-NLS-1$
				
				UIDummies.createDefaultErrorOutput(this, e);
			}
		}
		
		public void close() {
			try {
				getResultPresenter().close();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to close presenter tab for sub-result: "+getTitle(), e); //$NON-NLS-1$
			}
		}
	}
	
	protected class SubResultDisplayJob extends AbstractResultJob {
		
		protected final int[] indices;
		protected final String label;
		
		public SubResultDisplayJob(int[] indices, String label) {
			super("subResultJob"); //$NON-NLS-1$
			
			this.indices = indices;
			this.label = label;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected SearchResult doInBackground() throws Exception {
			firePropertyChange("indeterminate", false, true); //$NON-NLS-1$
			SearchResult searchResult = getMainResult();
			return searchResult==null ? null : searchResult.getSubResult(indices);
		}

		@Override
		protected void done() {
			try {
				SearchResult subResult = (SearchResult) get();
				if(subResult!=null) {
					cacheSubResult(subResult, indices);
					displaySelectedSubResult(indices, label);
				}
			} catch(InterruptedException | CancellationException e) {
				// ignore
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to display sub-result: "+label, e); //$NON-NLS-1$
			} finally {
				firePropertyChange("indeterminate", true, false); //$NON-NLS-1$
			}
		}

		@Override
		protected Object[] getDescriptionParams() {
			return new Object[]{Arrays.toString(indices)};
		}
	}
}
