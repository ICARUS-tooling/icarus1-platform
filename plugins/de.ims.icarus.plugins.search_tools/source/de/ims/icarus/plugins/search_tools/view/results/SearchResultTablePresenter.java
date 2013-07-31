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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.CompoundMenuButton;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.list.RowHeaderList;
import de.ims.icarus.ui.table.TableRowHeaderRenderer;
import de.ims.icarus.ui.table.TableSortMode;
import de.ims.icarus.ui.tasks.TaskManager;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class SearchResultTablePresenter extends SearchResultPresenter {
	
	protected static final String[] SORT_ACTIONS = { 
		"plugins.searchTools.searchResultPresenter.sortColsAscAlphaAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortColsDescAlphaAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortColsAscNumAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortColsDescNumAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortRowsAscAlphaAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortRowsDescAlphaAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortRowsAscNumAction",  //$NON-NLS-1$
		"plugins.searchTools.searchResultPresenter.sortRowsDescNumAction", //$NON-NLS-1$
	};
	
	protected SearchResultTablePresenter() {
		// no-op
	}
	
	@Override
	protected CallbackHandler createCallbackHandler() {
		return new TableCallbackHandler();
	}

	@Override
	protected void registerActionCallbacks() {
		super.registerActionCallbacks();
		
		ActionManager actionManager = getActionManager();
		
		for(String id : SORT_ACTIONS) {
			actionManager.addHandler(id, callbackHandler, "sortTable"); //$NON-NLS-1$
		}
		
		actionManager.addHandler("plugins.searchTools.searchResultPresenter.flipTableAction",  //$NON-NLS-1$
				callbackHandler, "flipTable"); //$NON-NLS-1$
		actionManager.addHandler("plugins.searchTools.searchResultPresenter.resetTableAction",  //$NON-NLS-1$
				callbackHandler, "resetTable"); //$NON-NLS-1$
	}

	@Override
	protected void refreshActions() {
		super.refreshActions();
		
		ActionManager actionManager = getActionManager();
		
		boolean canExecute = !hasCurrentTask();
		
		actionManager.setEnabled(canExecute, SORT_ACTIONS);
		
		actionManager.setEnabled(canExecute, 
				"plugins.searchTools.searchResultPresenter.flipTableAction", //$NON-NLS-1$
				"plugins.searchTools.searchResultPresenter.resetTableAction"); //$NON-NLS-1$
	}

	protected void sortTable(TableSortMode sortMode) {
		// for subclasses
	}
	
	protected void flipTable() {
		// for subclasses
	}
	
	protected void resetTable() {
		// for subclasses
	}
	
	public JTable createTable(SearchResultTableModel model, 
			TableCellRenderer cellRenderer, boolean resize) {
		JTable table = new JTable(model, model.getColumnModel());
		table.setDefaultRenderer(Integer.class, cellRenderer);
		table.setFillsViewportHeight(true);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setRowHeight(DEFAULT_CELL_HEIGHT);
		table.setIntercellSpacing(new Dimension(4, 4));
		table.setAutoResizeMode(resize ? JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF);

		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		//header.setResizingAllowed(false);
		DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
		renderer.setPreferredSize(new Dimension(0, DEFAULT_CELL_HEIGHT));
		UIUtil.disableHtml(renderer);
		
		return table;
	}
	
	public RowHeaderList createRowHeader(ListModel<String> model, 
			JTable table, JComponent container) {

		RowHeaderList rowHeader = new RowHeaderList(model, table.getSelectionModel());
		rowHeader.setFixedCellWidth(DEFAULT_CELL_WIDTH);
		rowHeader.setMinimumCellWidth(DEFAULT_CELL_WIDTH/2);
		rowHeader.setResizingAllowed(true);
		rowHeader.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowHeader.setFixedCellHeight(table.getRowHeight());
		rowHeader.setBackground(container.getBackground());
		rowHeader.setForeground(table.getForeground());		
		TableRowHeaderRenderer rowHeaderRenderer = new TableRowHeaderRenderer(rowHeader, table);
		rowHeader.setCellRenderer(rowHeaderRenderer);
		
		return rowHeader;
	}
	
	public static final int SORT_COLUMNS_BUTTON = 0;
	public static final int SORT_FIXED_DIMENSION_BUTTON = 1;
	public static final int SORT_ROWS_BUTTON = 2;
	
	public CompoundMenuButton createCompoundButton(int type) {
		ActionManager actionManager = getActionManager();
		switch (type) {
		case SORT_COLUMNS_BUTTON:
			return new CompoundMenuButton(
					0, CompoundMenuButton.HORIZONTAL,
					actionManager.getAction("plugins.searchTools.searchResultPresenter.sortColsAscAlphaAction"), //$NON-NLS-1$
					actionManager.getAction("plugins.searchTools.searchResultPresenter.sortColsDescAlphaAction"), //$NON-NLS-1$
					actionManager.getAction("plugins.searchTools.searchResultPresenter.sortColsAscNumAction"), //$NON-NLS-1$
					actionManager.getAction("plugins.searchTools.searchResultPresenter.sortColsDescNumAction")); //$NON-NLS-1$

		case SORT_ROWS_BUTTON:
			return new CompoundMenuButton(
					0, CompoundMenuButton.HORIZONTAL,
					actionManager.getAction("plugins.searchTools.searchResultPresenter.sortRowsAscAlphaAction"), //$NON-NLS-1$
					actionManager.getAction("plugins.searchTools.searchResultPresenter.sortRowsDescAlphaAction"), //$NON-NLS-1$
					actionManager.getAction("plugins.searchTools.searchResultPresenter.sortRowsAscNumAction"), //$NON-NLS-1$
					actionManager.getAction("plugins.searchTools.searchResultPresenter.sortRowsDescNumAction")); //$NON-NLS-1$

		case SORT_FIXED_DIMENSION_BUTTON:
			return new CompoundMenuButton(
					0, CompoundMenuButton.HORIZONTAL,
					actionManager.getAction("plugins.searchTools.searchResultPresenter.sortFixedDimensionAscAlphaAction"), //$NON-NLS-1$
					actionManager.getAction("plugins.searchTools.searchResultPresenter.sortFixedDimensionDescAlphaAction"), //$NON-NLS-1$
					actionManager.getAction("plugins.searchTools.searchResultPresenter.sortFixedDimensionAscNumAction"), //$NON-NLS-1$
					actionManager.getAction("plugins.searchTools.searchResultPresenter.sortFixedDimensionDescNumAction")); //$NON-NLS-1$
		}
		
		return null;
	}

	public class TableCallbackHandler extends CallbackHandler {
		
		protected TableCallbackHandler() {
			// no-op
		}
		
		public void sortTable(ActionEvent e) {
			try {
				TableSortMode sortMode = TableSortMode.parseMode(e.getActionCommand());
				SearchResultTablePresenter.this.sortTable(sortMode);
				
				refreshActions();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to sort table", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void flipTable(ActionEvent e) {
			try {
				SearchResultTablePresenter.this.flipTable();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to flip table", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
		
		public void resetTable(ActionEvent e) {
			try {
				SearchResultTablePresenter.this.resetTable();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to reset table", ex); //$NON-NLS-1$
				UIUtil.beep();
			}
		}
	}
	
	protected abstract class SortTableJob extends AbstractResultJob {

		private final TableSortMode sortMode;
		
		public SortTableJob(TableSortMode sortMode) {
			super("sortTableJob"); //$NON-NLS-1$
			
			if(sortMode==null)
				throw new IllegalArgumentException("Invalid sort mode"); //$NON-NLS-1$
			
			this.sortMode = sortMode;
		}
		
		protected TableSortMode getSortMode() {
			return sortMode;
		}

		@Override
		protected Object[] getDescriptionParams() {
			return new Object[]{sortMode.getName()};
		}

		@Override
		protected void done() {
			if(isCancelled()) {
				return;
			}
			
			TaskManager.getInstance().setIndeterminate(this, false);
			setCurrentTask(null);
			refreshActions();
		}
	}
}
