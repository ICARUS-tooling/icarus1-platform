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
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.Grouping;
import net.ikarus_systems.icarus.search_tools.result.ResultDummies;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;
import net.ikarus_systems.icarus.ui.CompoundMenuButton;
import net.ikarus_systems.icarus.ui.NumberDisplayMode;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionList.EntryType;
import net.ikarus_systems.icarus.ui.list.RowHeaderList;
import net.ikarus_systems.icarus.ui.tab.ButtonTabComponent;
import net.ikarus_systems.icarus.ui.tab.TabController;
import net.ikarus_systems.icarus.ui.table.TableRowHeaderRenderer;
import net.ikarus_systems.icarus.ui.table.TableSortMode;
import net.ikarus_systems.icarus.ui.tasks.TaskManager;
import net.ikarus_systems.icarus.ui.tasks.TaskPriority;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.cache.LRUCache;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Default2DResultPresenter extends SearchResultTablePresenter {
	
	public static final int SUPPORTED_DIMENSIONS = 2;
	
	// TODO allow sub-result creation for cells, rows and columns (0D and 1D respectively)
	
	protected JTabbedPane tabbedPane;
	
	protected JPanel overviewPanel;
	
	protected JTable table;
	
	protected RowHeaderList rowHeader;
	protected TableRowHeaderRenderer rowHeaderRenderer;
	protected SearchResultTableModel tableModel;
	protected ResultCountTableCellRenderer cellRenderer;
	
	protected Map<Object, Reference<SearchResult>> subResults;

	public Default2DResultPresenter() {
		buildContentPanel();
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#getSupportedDimensions()
	 */
	@Override
	public int getSupportedDimensions() {
		return SUPPORTED_DIMENSIONS;
	}

	@Override
	protected void updateGroupPainters() {
		int id1 = SearchUtils.getGroupId(getSearchResult(), 0);
		int id2 = SearchUtils.getGroupId(getSearchResult(), 1);
		
		if(tableModel.isFlipped()) {
			Grouping.setGroupIds(table, id2, id1);
		} else {
			Grouping.setGroupIds(table, id1, id2);
		}
 	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#displayResult()
	 */
	@Override
	protected void displayResult(Options options) {
		SearchResult searchResult = this.searchResult;
		if(searchResult==null) {
			searchResult = ResultDummies.dummyResult1D;
		}
		
		tableModel.setResultData(searchResult);
		cellRenderer.setSearchResult(searchResult);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#refresh()
	 */
	@Override
	public void refresh() {
		tableModel.update();
		
		// TODO refresh sub-result tabs as well?
	}

	@Override
	protected Handler2D getHandler() {
		return (Handler2D) super.getHandler();
	}

	@Override
	protected Handler createHandler() {
		return new Handler2D();
	}

	@Override
	protected void setNumberDisplayMode(NumberDisplayMode mode) {
		if(mode==null)
			throw new IllegalArgumentException("Invalid display mode"); //$NON-NLS-1$
		
		cellRenderer.setDisplayMode(mode);
		tableModel.setDisplayMode(mode);
		
		// TODO ensure that the row header is still readable (adjust width?)
	}

	@Override
	protected void sortTable(TableSortMode sortMode) {
		if(hasCurrentTask()) {
			return;
		}
		
		SortTableJob job = new SortTableJob(sortMode){
			@Override
			protected Object doInBackground() throws Exception {
				tableModel.sort(getSortMode());
				return null;
			}
		};
		setCurrentTask(job);
		TaskManager.getInstance().schedule(job, TaskPriority.DEFAULT, true);
		TaskManager.getInstance().setIndeterminate(job, true);
	}

	@Override
	protected void flipTable() {
		tableModel.flip();
		
		updateGroupPainters();
	}

	@Override
	protected void resetTable() {
		tableModel.clear(true, true);
	}

	protected void checkViewMode(boolean instertionPending) {
		int tabCount = tabbedPane==null ? 0 : tabbedPane.getTabCount();
		
		if(tabCount==0 && instertionPending) {
			// Expand
			if(tabbedPane==null) {
				tabbedPane = createTabbedPane();
			}
			String title = ResourceManager.getInstance().get(
					"plugins.searchTools.searchResultPresenter.labels.overview"); //$NON-NLS-1$

			contentPanel.remove(overviewPanel);
			tabbedPane.insertTab(title, null, overviewPanel, null, 0);
			
			contentPanel.add(tabbedPane, BorderLayout.CENTER);
		} else if(tabCount==1 && !instertionPending) {
			// Shrink
			for(int i=1; i<tabbedPane.getTabCount(); i++) {
				SubResultContainer container = (SubResultContainer)tabbedPane.getComponentAt(i);
				container.close();
			}
			tabbedPane.removeAll();
			contentPanel.remove(tabbedPane);
			
			contentPanel.add(overviewPanel, BorderLayout.CENTER);
			tabbedPane = null;
		}
	}
	
	protected JTabbedPane createTabbedPane() {
		return new ClosableTabbedPane();
	}
	
	protected SearchResult getCachedSubResult(int[] indices) {
		if(subResults==null) {
			return null;
		}
		Reference<SearchResult> ref = subResults.get(Arrays.toString(indices));
		return ref==null ? null : ref.get();
	}
	
	protected void cacheSubResult(int[] indices, SearchResult subResult) {
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

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#buildContentPanel()
	 */
	@Override
	protected void buildContentPanel() {
		contentPanel = new JPanel(new BorderLayout());
		
		cellRenderer = new ResultCountTableCellRenderer();
		
		tableModel = new SearchResultTableModel(ResultDummies.dummyResult2D);
		table = createTable(tableModel, cellRenderer, false);
		table.addMouseListener(getHandler());
		
		rowHeader = createRowHeader(tableModel.getRowHeaderModel(), table, contentPanel);
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setRowHeaderView(rowHeader);
		scrollPane.setBorder(UIUtil.topLineBorder);
		Grouping.decorate(scrollPane, true);
		
		CompoundMenuButton menuButtonRows = createCompoundButton(SORT_ROWS_BUTTON);
		CompoundMenuButton menuButtonCols = createCompoundButton(SORT_COLUMNS_BUTTON);
		
		Options options = new Options();
		options.put("sortButtons", new Object[]{ //$NON-NLS-1$
				EntryType.SEPARATOR,
				menuButtonRows, 
				menuButtonRows.getOpenButton(),
				menuButtonCols, 
				menuButtonCols.getOpenButton(),
		});
		options.put("multiline", true); //$NON-NLS-1$
		JToolBar toolBar = getActionManager().createToolBar(
				"plugins.searchTools.searchResultPresenter.toolBarList2D", options); //$NON-NLS-1$
		
		overviewPanel = new JPanel(new BorderLayout());
		overviewPanel.add(toolBar, BorderLayout.NORTH);
		overviewPanel.add(scrollPane, BorderLayout.CENTER);
		
		contentPanel.add(overviewPanel, BorderLayout.CENTER);
	}
	
	protected class ClosableTabbedPane extends JTabbedPane implements TabController {

		private static final long serialVersionUID = -8989316268794923006L;

		/**
		 * @see net.ikarus_systems.icarus.ui.tab.TabController#closeTab(java.awt.Component)
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
		 * @see net.ikarus_systems.icarus.ui.tab.TabController#closeChildren(java.awt.Component)
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
				resultPresenter = SearchResultView.getFalbackPresenter(getSubResult());
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

	protected class Handler2D extends Handler implements ChangeListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getSource()!=table) {
				super.mouseClicked(e);
				return;
			}
			
			if(e.getClickCount()!=2 || !SwingUtilities.isLeftMouseButton(e)) {
				return;
			}
			
			int row = table.rowAtPoint(e.getPoint());
			int col = table.columnAtPoint(e.getPoint());
			
			if(row==-1 || col==-1) {
				return;
			}
			
			int count = tableModel.getValueAt(row, col);
			if(count==0) {
				return;
			}
			
			int[] indices = new int[2];
			
			try {
				indices[0] = tableModel.translateRowIndex(row, col);
				indices[1] = tableModel.translateColumnIndex(row, col);
				
				String label = tableModel.getRowName(row)+"/"+tableModel.getColumnName(col); //$NON-NLS-1$
				
				displaySelectedSubResult(indices, label);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to handle sub-result selection: "+e, ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			// TODO Auto-generated method stub
			
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
			return searchResult.getSubResult(indices);
		}

		@Override
		protected void done() {
			try {
				SearchResult subResult = (SearchResult) get();
				if(subResult!=null) {
					cacheSubResult(indices, subResult);
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
