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
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.result.ResultDummies;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.ui.CompoundMenuButton;
import net.ikarus_systems.icarus.ui.NumberDisplayMode;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionList.EntryType;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.list.RowHeaderList;
import net.ikarus_systems.icarus.ui.tab.ButtonTabComponent;
import net.ikarus_systems.icarus.ui.tab.TabController;
import net.ikarus_systems.icarus.ui.table.TableRowHeaderRenderer;
import net.ikarus_systems.icarus.ui.table.TableSortMode;
import net.ikarus_systems.icarus.ui.tasks.TaskManager;
import net.ikarus_systems.icarus.ui.tasks.TaskPriority;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Default2DResultPresenter extends SearchResultTablePresenter {
	
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
		return 2;
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
		if(sortTableJob!=null) {
			return;
		}
		
		sortTableJob = new SortTableJob(sortMode){
			@Override
			protected Object doInBackground() throws Exception {
				tableModel.sort(getSortMode());
				return null;
			}
		};
		TaskManager.getInstance().schedule(sortTableJob, 
				TaskPriority.DEFAULT, true);
		TaskManager.getInstance().setIndeterminate(sortTableJob, true);
	}

	@Override
	protected void flipTable() {
		tableModel.flip();
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
			subResults = new HashMap<>();
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
		table = new JTable(tableModel, tableModel.getColumnModel());
		table.setDefaultRenderer(Integer.class, cellRenderer);
		table.setFillsViewportHeight(true);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setRowHeight(DEFAULT_CELL_HEIGHT);
		table.setIntercellSpacing(new Dimension(4, 4));
		table.addMouseListener(getHandler());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		//header.setResizingAllowed(false);
		DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
		renderer.setPreferredSize(new Dimension(0, DEFAULT_CELL_HEIGHT));
		UIUtil.disableHtml(renderer);
		
		rowHeader = new RowHeaderList(tableModel.getRowHeaderModel());
		rowHeader.setFixedCellWidth(DEFAULT_CELL_WIDTH);
		rowHeader.setMinimumCellWidth(DEFAULT_CELL_WIDTH/2);
		rowHeader.setResizingAllowed(true);
		rowHeader.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowHeader.setFixedCellHeight(table.getRowHeight());
		rowHeader.setBackground(contentPanel.getBackground());
		rowHeader.setForeground(table.getForeground());		
		rowHeaderRenderer = new TableRowHeaderRenderer(rowHeader, table);
		rowHeader.setCellRenderer(rowHeaderRenderer);
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setRowHeaderView(rowHeader);
		scrollPane.setBorder(UIUtil.topLineBorder);
		
		ActionManager actionManager = getActionManager();
		CompoundMenuButton menuButtonRows = new CompoundMenuButton(
				0, CompoundMenuButton.HORIZONTAL,
				actionManager.getAction("plugins.searchTools.searchResultPresenter.sortRowsAscAlphaAction"), //$NON-NLS-1$
				actionManager.getAction("plugins.searchTools.searchResultPresenter.sortRowsDescAlphaAction"), //$NON-NLS-1$
				actionManager.getAction("plugins.searchTools.searchResultPresenter.sortRowsAscNumAction"), //$NON-NLS-1$
				actionManager.getAction("plugins.searchTools.searchResultPresenter.sortRowsDescNumAction")); //$NON-NLS-1$
		CompoundMenuButton menuButtonCols = new CompoundMenuButton(
				0, CompoundMenuButton.HORIZONTAL,
				actionManager.getAction("plugins.searchTools.searchResultPresenter.sortColsAscAlphaAction"), //$NON-NLS-1$
				actionManager.getAction("plugins.searchTools.searchResultPresenter.sortColsDescAlphaAction"), //$NON-NLS-1$
				actionManager.getAction("plugins.searchTools.searchResultPresenter.sortColsAscNumAction"), //$NON-NLS-1$
				actionManager.getAction("plugins.searchTools.searchResultPresenter.sortColsDescNumAction")); //$NON-NLS-1$
		
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
			if(tabbedPane==null) {
				return false;
			}
			
			if(comp instanceof SubResultContainer) {
				SubResultContainer container = (SubResultContainer) comp;
				container.close();
			}
			
			tabbedPane.remove(comp);
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
	
	protected class SubResultDisplayJob extends SwingWorker<SearchResult, Object>
			implements Identity {
		
		protected final int[] indices;
		protected final String label;
		
		public SubResultDisplayJob(int[] indices, String label) {
			this.indices = indices;
			this.label = label;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof SubResultDisplayJob) {
				return owner()==((SubResultDisplayJob)obj).owner();
			}
			
			return false;
		}
		
		private Object owner() {
			return Default2DResultPresenter.this;
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
				SearchResult subResult = get();
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

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return getClass().getSimpleName();
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return ResourceManager.getInstance().get(
					"plugins.searchTools.searchResultPresenter.subResultJob.name"); //$NON-NLS-1$
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			return ResourceManager.getInstance().get(
					"plugins.searchTools.searchResultPresenter.subResultJob.description", indices); //$NON-NLS-1$
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return null;
		}

		/**
		 * @see net.ikarus_systems.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}
	}
}
