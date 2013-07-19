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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.Grouping;
import de.ims.icarus.search_tools.result.ResultDummies;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.search_tools.standard.DefaultGroupOrderEditor;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.ui.CompoundMenuButton;
import de.ims.icarus.ui.NumberDisplayMode;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionList.EntryType;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.list.RowHeaderList;
import de.ims.icarus.ui.table.TableSortMode;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Default3DResultPresenter extends SearchResultTabbedPresenter {

	public static final int SUPPORTED_DIMENSIONS = 2;
	
	protected JPanel subResultPanel;
	protected JTextArea infoLabel;
	
	// left panel
	protected SearchResult1DTableModel fixedModel;
	protected JTable fixedTable;
	protected RowHeaderList fixedHeader;
	protected ResultCountTableCellRenderer fixedCellRenderer;
	
	// right panel
	protected SearchResultTableModel dynModel;
	protected JTable dynTable;
	protected RowHeaderList dynHeader;
	protected ResultCountTableCellRenderer dynCellRenderer;
	
	protected SearchResult dynResult;

	protected static final String[] SORT_FIXED_ACTIONS = {
		"plugins.searchTools.searchResultPresenter.sortFixedDimensionAscAlphaAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortFixedDimensionDescAlphaAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortFixedDimensionAscNumAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortFixedDimensionDescNumAction", //$NON-NLS-1$
	};

	public Default3DResultPresenter() {
		buildContentPanel();
	}

	@Override
	protected void registerActionCallbacks() {
		super.registerActionCallbacks();
		
		ActionManager actionManager = getActionManager();
		
		for(String id : SORT_FIXED_ACTIONS) {
			actionManager.addHandler(id, callbackHandler, "sortFixedTable"); //$NON-NLS-1$
		}
		
		actionManager.addHandler("plugins.searchTools.searchResultPresenter.resetFixedTableAction",  //$NON-NLS-1$
				callbackHandler, "resetFixedTable"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchResultPresenter.reorderResultAction",  //$NON-NLS-1$
				callbackHandler, "reorderResult"); //$NON-NLS-1$
	}

	@Override
	protected void refreshActions() {
		super.refreshActions();
		
		ActionManager actionManager = getActionManager();
		
		boolean canExecute = !hasCurrentTask();
		boolean canReorder = canExecute && searchResult!=null && searchResult.canReorder();
		
		actionManager.setEnabled(canExecute, SORT_FIXED_ACTIONS);
		
		actionManager.setEnabled(canExecute,
				"plugins.searchTools.searchResultPresenter.resetFixedTableAction"); //$NON-NLS-1$
		actionManager.setEnabled(canReorder,
				"plugins.searchTools.searchResultPresenter.reorderResultAction"); //$NON-NLS-1$
	}

	@Override
	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandler3D();
	}

	@Override
	protected Handler3D getHandler() {
		return (Handler3D) super.getHandler();
	}

	@Override
	protected Handler createHandler() {
		return new Handler3D();
	}

	@Override
	protected void setNumberDisplayMode(NumberDisplayMode mode) {
		if(mode==null)
			throw new IllegalArgumentException("Invalid display mode"); //$NON-NLS-1$
		
		fixedCellRenderer.setDisplayMode(mode);
		fixedModel.setDisplayMode(mode);

		dynCellRenderer.setDisplayMode(mode);
		dynModel.setDisplayMode(mode);
	}

	@Override
	protected void sortTable(TableSortMode sortMode) {
		sortTable(dynModel, sortMode);
	}
	
	protected void sortFixedTable(TableSortMode sortMode) {
		sortTable(fixedModel, sortMode);
	}

	protected void sortTable(final SearchResultTableModel model, TableSortMode sortMode) {
		if(hasCurrentTask()) {
			return;
		}
		
		SortTableJob job = new SortTableJob(sortMode){
			@Override
			protected Object doInBackground() throws Exception {
				model.sort(getSortMode());
				return null;
			}
		};
		setCurrentTask(job);
		TaskManager.getInstance().schedule(job, TaskPriority.DEFAULT, true);
		TaskManager.getInstance().setIndeterminate(job, true);
	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#getSupportedDimensions()
	 */
	@Override
	public int getSupportedDimensions() {
		return SUPPORTED_DIMENSIONS;
	}

	@Override
	protected void updateGroupPainters() {
		int fixedId = SearchUtils.getGroupId(getSearchResult(), 0);
		int dynId1 = SearchUtils.getGroupId(getSearchResult(), 1);
		int dynId2 = SearchUtils.getGroupId(getSearchResult(), 2);
		
		Grouping.setGroupId(fixedTable, fixedId);
		
		if(dynModel.isFlipped()) {
			Grouping.setGroupIds(dynTable, dynId2, dynId1);
		} else {
			Grouping.setGroupIds(dynTable, dynId1, dynId2);
		}
	}
	
	public SearchResult getDynSearchResult() {
		return dynResult;
	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#displayResult(de.ims.icarus.util.Options)
	 */
	@Override
	protected void displayResult(Options options) {
		SearchResult searchResult = this.searchResult;
		if(searchResult==null) {
			searchResult = ResultDummies.dummyResult3D;
		}
		
		fixedCellRenderer.setSearchResult(searchResult);
		fixedModel.setResultData(searchResult);
		
		dynCellRenderer.setSearchResult(ResultDummies.dummyResult2D);
		dynModel.setResultData(ResultDummies.dummyResult2D);
	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#refresh()
	 */
	@Override
	public void refresh() {
		fixedModel.update();
		dynModel.update();
	}

	@Override
	protected void flipTable() {
		dynModel.flip();
		
		updateGroupPainters();
	}

	@Override
	protected void resetTable() {
		dynModel.clear(true, true);
	}
	
	protected void resetFixedTable() {
		fixedModel.clear(true, true);
	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#buildContentPanel()
	 */
	@Override
	protected void buildContentPanel() {
		contentPanel = new JPanel(new BorderLayout());
		
		// Left panel
		fixedModel = new FixedDimensionModel(ResultDummies.dummyResult3D);
		fixedCellRenderer = new ResultCountTableCellRenderer();
		fixedTable = createTable(fixedModel, fixedCellRenderer, true);
		fixedTable.addMouseListener(getHandler());
		
		fixedHeader = createRowHeader(fixedModel.getRowHeaderModel(), fixedTable, contentPanel);
		
		JScrollPane spLeft = new JScrollPane(fixedTable);
		spLeft.setRowHeaderView(fixedHeader);
		spLeft.setBorder(UIUtil.emptyBorder);
		spLeft.setMinimumSize(new Dimension(100, 100));
		Grouping.decorate(spLeft, false);
		
		// Right panel
		dynModel = new SearchResultTableModel(ResultDummies.dummyResult2D);
		dynCellRenderer = new ResultCountTableCellRenderer();
		dynTable = createTable(dynModel, dynCellRenderer, false);
		dynTable.addMouseListener(getHandler());
		
		dynHeader = createRowHeader(dynModel.getRowHeaderModel(), dynTable, contentPanel);
		
		infoLabel = UIUtil.defaultCreateInfoLabel(contentPanel);
		infoLabel.setText(ResourceManager.getInstance().get(
				"plugins.searchTools.default3DResultPresenter.noSubResultAvailable")); //$NON-NLS-1$
		
		JScrollPane spRight = new JScrollPane(dynTable);
		spRight.setRowHeaderView(dynHeader);
		spRight.setBorder(UIUtil.emptyBorder);
		spRight.setMinimumSize(new Dimension(100, 100));
		Grouping.decorate(spRight, true);
		
		subResultPanel = new JPanel(new BorderLayout());
		subResultPanel.add(infoLabel, BorderLayout.NORTH);
		subResultPanel.add(spRight, BorderLayout.CENTER);
		
		// General stuff
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setLeftComponent(spLeft);
		splitPane.setRightComponent(subResultPanel);
		splitPane.setResizeWeight(0);
		splitPane.setDividerSize(5);
		splitPane.setDividerLocation(250);
		splitPane.setBorder(UIUtil.topLineBorder);
		
		CompoundMenuButton menuButtonRows = createCompoundButton(SORT_ROWS_BUTTON);
		CompoundMenuButton menuButtonCols = createCompoundButton(SORT_COLUMNS_BUTTON);
		CompoundMenuButton menuButtonFixed = createCompoundButton(SORT_FIXED_DIMENSION_BUTTON);
		
		Options options = new Options();
		options.put("sortButtons", new Object[]{ //$NON-NLS-1$
				EntryType.SEPARATOR,
				menuButtonRows, 
				menuButtonRows.getOpenButton(),
				menuButtonCols, 
				menuButtonCols.getOpenButton(),
		});
		options.put("fixedDimensionSortButtons", new Object[]{ //$NON-NLS-1$
				EntryType.SEPARATOR,
				menuButtonFixed, 
				menuButtonFixed.getOpenButton(),
		});
		options.put("multiline", true); //$NON-NLS-1$
		JToolBar toolBar = getActionManager().createToolBar(
				"plugins.searchTools.searchResultPresenter.toolBarList3D", options); //$NON-NLS-1$
		
		overviewPanel = new JPanel(new BorderLayout());
		overviewPanel.add(toolBar, BorderLayout.NORTH);
		overviewPanel.add(splitPane, BorderLayout.CENTER);
		
		contentPanel.add(overviewPanel, BorderLayout.CENTER);
	}
	
	protected void displaySelectedDynResult(int index) {
		JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, dynTable);
		scrollPane.setVisible(index!=-1);
		infoLabel.setVisible(index==-1);
		
		if(index!=-1) {
			SearchResult subResult = getCachedSubResult(index);
			if(subResult==null) {
				TaskManager.getInstance().schedule(new DynResultDisplayJob(index), TaskPriority.DEFAULT, true);
				return;
			}
			
			dynResult = subResult;
		} else {
			dynResult = ResultDummies.dummyResult2D;
		}
		
		dynCellRenderer.setSearchResult(dynResult);
		dynModel.setResultData(dynResult);
	}
	
	@Override
	protected SearchResult getMainResult() {
		return getDynSearchResult();
	}
	
	public void reorderResult() {
		SearchResult searchResult = getSearchResult();
		if(searchResult==null || !searchResult.canReorder()) {
			return;
		}
		if(hasCurrentTask()) {
			return;
		}
		
		DefaultGroupOrderEditor editor = new DefaultGroupOrderEditor(searchResult);
		
		if(!DialogFactory.getGlobalFactory().showGenericDialog(
				null,
				DialogFactory.OK_CANCEL_OPTION,
				"plugins.searchTools.default3DResultPresenter.reorderDialog.title",  //$NON-NLS-1$
				"plugins.searchTools.default3DResultPresenter.reorderDialog.message",  //$NON-NLS-1$
				editor.getContentPanel(), true)) {
			return;
		}
		
		int[] permutation = editor.getPermutation();
		if(CollectionUtils.isAscending(permutation)) {
			return;
		}
		
		TaskManager.getInstance().schedule(new ReorderResultJob(
				permutation), TaskPriority.DEFAULT, true);
	}
	
	protected class Handler3D extends Handler implements ChangeListener {
		
		protected void fixedTableClicked(MouseEvent e) {
			if(e.getClickCount()!=2 || !SwingUtilities.isLeftMouseButton(e)) {
				return;
			}

			int row = fixedTable.rowAtPoint(e.getPoint());
			
			if(row==-1) {
				return;
			}
			
			try {
				int index = fixedModel.translateRowIndex(row);
				
				displaySelectedDynResult(index);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to handle dyn-result selection: "+e, ex); //$NON-NLS-1$
			}
		}

		protected void dynTableClicked(MouseEvent e) {
			if(e.getClickCount()!=2 || !SwingUtilities.isLeftMouseButton(e)) {
				return;
			}
			
			int row = dynTable.rowAtPoint(e.getPoint());
			int col = dynTable.columnAtPoint(e.getPoint());
			
			if(row==-1 || col==-1) {
				return;
			}
			
			int count = dynModel.getValueAt(row, col);
			if(count==0) {
				return;
			}
			
			int[] indices = new int[2];
			
			try {
				indices[0] = dynModel.translateRowIndex(row, col);
				indices[1] = dynModel.translateColumnIndex(row, col);
				
				int fixedIndex = fixedTable.getSelectedRow();
				fixedIndex = fixedModel.translateRowIndex(fixedIndex);
				
				String label = fixedModel.getRowName(fixedIndex)
						+"/"+dynModel.getRowName(row) //$NON-NLS-1$
						+"/"+dynModel.getColumnName(col); //$NON-NLS-1$
				
				displaySelectedSubResult(indices, label);
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to handle sub-result selection: "+e, ex); //$NON-NLS-1$
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getSource()==fixedTable) {
				fixedTableClicked(e);
			} else if(e.getSource()==dynTable) {
				dynTableClicked(e);
			} else {
				super.mouseClicked(e);
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
	
	public class CallbackHandler3D extends TableCallbackHandler {
		
		protected CallbackHandler3D() {
			// no-op
		}
		
		public void sortFixedTable(ActionEvent e) {
			try {
				TableSortMode sortMode = TableSortMode.parseMode(e.getActionCommand());
				Default3DResultPresenter.this.sortFixedTable(sortMode);
				
				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to sort fixed table", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void resetFixedTable(ActionEvent e) {
			try {
				Default3DResultPresenter.this.resetFixedTable();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to reset fixed table", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void reorderResult(ActionEvent e) {
			try {
				Default3DResultPresenter.this.reorderResult();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to reorder result", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
	}
	
	protected static class FixedDimensionModel extends SearchResult1DTableModel {

		private static final long serialVersionUID = -5274289009611796068L;

		public FixedDimensionModel(SearchResult resultData) {
			super(resultData, true);
		}

		@Override
		public Integer getValueAt(int row, int column) {
			return resultData.getGroupMatchCount(rowDimension, translateRowIndex(row));
		}
	}
	
	protected class DynResultDisplayJob extends AbstractResultJob {
		
		protected final int[] indices;
		protected final String label;
		
		public DynResultDisplayJob(int index) {
			super("subResultJob"); //$NON-NLS-1$
			
			this.indices = new int[]{index};
			this.label = String.valueOf(index);
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected SearchResult doInBackground() throws Exception {
			firePropertyChange("indeterminate", false, true); //$NON-NLS-1$
			
			return getSearchResult().getSubResult(indices);
		}

		@Override
		protected void done() {
			try {
				SearchResult subResult = (SearchResult) get();
				if(subResult!=null) {
					cacheSubResult(subResult, indices);
					
					displaySelectedDynResult(indices[0]);
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
	
	protected class ReorderResultJob extends AbstractResultJob {
		
		protected final int[] permutation;
		
		public ReorderResultJob(int[] permutation) {
			super("reorderResultJob"); //$NON-NLS-1$
			
			this.permutation = permutation;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected SearchResult doInBackground() throws Exception {
			firePropertyChange("indeterminate", false, true); //$NON-NLS-1$
			
			getSearchResult().reorder(permutation);
			
			return null;
		}

		@Override
		protected void done() {
			try {
				dynCellRenderer.setSearchResult(ResultDummies.dummyResult2D);
				dynModel.setResultData(ResultDummies.dummyResult2D);
				
				shrinkView();
				
				refresh();
				
				updateGroupPainters();
			} catch(CancellationException e) {
				// ignore
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to reorder result: "+Arrays.toString(permutation), e); //$NON-NLS-1$
			} finally {
				firePropertyChange("indeterminate", true, false); //$NON-NLS-1$
			}
		}

		@Override
		protected Object[] getDescriptionParams() {
			return new Object[]{Arrays.toString(permutation)};
		}
	}
}
