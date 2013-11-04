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
package de.ims.icarus.plugins.search_tools.view.results;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.search_tools.Grouping;
import de.ims.icarus.search_tools.result.ResultDummies;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.ui.CompoundMenuButton;
import de.ims.icarus.ui.NumberDisplayMode;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionList.EntryType;
import de.ims.icarus.ui.list.RowHeaderList;
import de.ims.icarus.ui.table.TableRowHeaderRenderer;
import de.ims.icarus.ui.table.TableSortMode;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Default2DResultPresenter extends SearchResultTabbedPresenter {
	
	public static final int SUPPORTED_DIMENSIONS = 2;
	
	// TODO allow sub-result creation for cells, rows and columns (0D and 1D respectively)
	
	protected JTable table;
	
	protected RowHeaderList rowHeader;
	protected TableRowHeaderRenderer rowHeaderRenderer;
	protected SearchResultTableModel tableModel;
	protected ResultCountTableCellRenderer cellRenderer;

	public Default2DResultPresenter() {
		buildContentPanel();
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
		int id1 = SearchUtils.getGroupId(getSearchResult(), 0);
		int id2 = SearchUtils.getGroupId(getSearchResult(), 1);
		
		if(tableModel.isFlipped()) {
			Grouping.setGroupIds(table, id2, id1);
		} else {
			Grouping.setGroupIds(table, id1, id2);
		}
 	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#displayResult()
	 */
	@Override
	protected void displayResult() {
		SearchResult searchResult = this.searchResult;
		if(searchResult==null) {
			searchResult = ResultDummies.dummyResult2D;
		}
		
		tableModel.setResultData(searchResult);
		cellRenderer.setSearchResult(searchResult);
	}

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#refresh()
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
			throw new NullPointerException("Invalid display mode"); //$NON-NLS-1$
		
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

	/**
	 * @see de.ims.icarus.plugins.search_tools.view.results.SearchResultPresenter#buildContentPanel()
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
}
