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
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.Grouping;
import net.ikarus_systems.icarus.search_tools.result.ResultDummies;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;
import net.ikarus_systems.icarus.ui.CompoundMenuButton;
import net.ikarus_systems.icarus.ui.NumberDisplayMode;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionList.EntryType;
import net.ikarus_systems.icarus.ui.list.RowHeaderList;
import net.ikarus_systems.icarus.ui.table.TableRowHeaderRenderer;
import net.ikarus_systems.icarus.ui.table.TableSortMode;
import net.ikarus_systems.icarus.ui.tasks.TaskManager;
import net.ikarus_systems.icarus.ui.tasks.TaskPriority;
import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Default1DResultPresenter extends SearchResultTablePresenter {
	
	public static final int SUPPORTED_DIMENSIONS = 1;
	
	protected Default0DResultPresenter subResultPresenter;
	
	protected SearchResult1DTableModel tableModel;
	protected ResultCountTableCellRenderer cellRenderer;
	protected TableRowHeaderRenderer rowHeaderRenderer;
	protected RowHeaderList rowHeader;
	protected JTable table;

	public Default1DResultPresenter() {
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
		int groupId = SearchUtils.getGroupId(getSearchResult(), 0);
		Grouping.setGroupId(table, groupId);
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
		displaySelectedSubResult(tableModel.getRowCount()==0 ? -1 : 0);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#refresh()
	 */
	@Override
	public void refresh() {
		tableModel.update();
		subResultPresenter.refresh();
	}

	@Override
	protected Handler createHandler() {
		return new Handler1D();
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.search_tools.view.results.SearchResultPresenter#buildContentPanel()
	 */
	@Override
	protected void buildContentPanel() {
		contentPanel = new JPanel(new BorderLayout());
		
		subResultPresenter = new Default0DResultPresenter();

		cellRenderer = new ResultCountTableCellRenderer();
		
		tableModel = new SearchResult1DTableModel(ResultDummies.dummyResult1D);
		table = createTable(tableModel, cellRenderer, true);
		table.addMouseListener(getHandler());

		rowHeader = createRowHeader(tableModel.getRowHeaderModel(), table, contentPanel);
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setRowHeaderView(rowHeader);
		scrollPane.setBorder(UIUtil.topLineBorder);		
		Grouping.decorate(scrollPane, false);
		
		JPanel leftPanel = new JPanel(new BorderLayout());
		
		CompoundMenuButton menuButton = createCompoundButton(SORT_ROWS_BUTTON);
		
		Options options = new Options();
		options.put("sortButtons", new Object[]{ //$NON-NLS-1$
				EntryType.SEPARATOR,
				menuButton, 
				menuButton.getOpenButton(),
		});
		options.put("multiline", true); //$NON-NLS-1$
		JToolBar toolBar = getActionManager().createToolBar(
				"plugins.searchTools.searchResultPresenter.toolBarList1D", options); //$NON-NLS-1$
		leftPanel.add(toolBar, BorderLayout.NORTH);
		leftPanel.add(scrollPane, BorderLayout.CENTER);
		
		Dimension minSize = new Dimension(100, 100);
		leftPanel.setMinimumSize(minSize);
		subResultPresenter.getPresentingComponent().setMinimumSize(minSize);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(subResultPresenter.getPresentingComponent());
		splitPane.setResizeWeight(0);
		splitPane.setDividerSize(5);
		splitPane.setDividerLocation(200);
		splitPane.setBorder(null);
		
		contentPanel.add(splitPane, BorderLayout.CENTER);
	}
	
	protected void displaySelectedSubResult(int index) {
		if(searchResult==null) {
			return;
		}
		
		if(index==-1) {
			subResultPresenter.clear();
		} else {		
			TaskManager.getInstance().schedule(
					new SubResultDisplayJob(index), TaskPriority.DEFAULT, true);
		}
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
	}

	@Override
	protected void resetTable() {
		tableModel.clear(true, true);
	}

	protected class Handler1D extends Handler {

		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getSource()!=table) {
				super.mouseClicked(e);
				return;
			}
			
			if(e.getClickCount()!=2 || !SwingUtilities.isLeftMouseButton(e)) {
				return;
			}
			
			try {
				int index = table.rowAtPoint(e.getPoint());
				if(index>=-1) {
					index = tableModel.translateRowIndex(index);
					displaySelectedSubResult(index);
				}
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to handle mouse-click on table: "+e, ex); //$NON-NLS-1$
			}
		}		
	}
	
	protected class SubResultDisplayJob extends AbstractResultJob {
		
		protected final int index;
		
		public SubResultDisplayJob(int index) {
			super("subResultJob"); //$NON-NLS-1$
			this.index = index;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected SearchResult doInBackground() throws Exception {
			firePropertyChange("indeterminate", false, true); //$NON-NLS-1$
			return searchResult.getSubResult(index);
		}

		@Override
		protected void done() {
			try {
				SearchResult subResult = (SearchResult) get();
				if(subResult!=null) {
					
					Object label = searchResult.getInstanceLabel(0, index);
					String title = ResourceManager.getInstance().get(
							"plugins.searchTools.default1DResultPresenter.instanceTitle", //$NON-NLS-1$
							label);
					
					Options options = new Options();
					options.put(Options.TITLE, title);
					
					subResultPresenter.present(subResult, options);
				}
			} catch(InterruptedException | CancellationException e) {
				// ignore
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to display sub-result for index: "+index, e); //$NON-NLS-1$
			} finally {
				firePropertyChange("indeterminate", true, false); //$NON-NLS-1$
			}
		}

		@Override
		protected Object[] getDescriptionParams() {
			return new Object[]{index};
		}
	}
}
