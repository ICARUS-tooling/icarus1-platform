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
package de.ims.icarus.plugins.coref.view.grid;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.plugins.coref.view.CoreferenceCellRenderer;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.list.RowHeaderList;
import de.ims.icarus.ui.table.TableIndexListModel;
import de.ims.icarus.ui.table.TablePresenter;
import de.ims.icarus.ui.table.TableRowHeaderRenderer;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EntityGridPresenter extends TablePresenter {
	
	protected CoreferenceDocumentData document;
	protected CoreferenceAllocation allocation;
	protected CoreferenceAllocation goldAllocation;
	
	protected boolean showGoldNodes = true;
	protected boolean markFalseNodes = true;
	protected boolean filterSingletons = true;
	
	protected EntityGridTableModel gridModel = new EntityGridTableModel();
	protected CoreferenceCellRenderer outline;
	protected EntityGridCellRenderer cellRenderer;
	
	protected static final int DEFAULT_CELL_HEIGHT = 20;
	protected static final int DEFAULT_CELL_WIDTH = 75;

	public EntityGridPresenter() {
		// no-op
	}

	@Override
	protected void init() {
		cellRenderer = new EntityGridCellRenderer();
	}

	@Override
	protected JToolBar createToolBar() {
		// TODO
		return null;
	}

	@Override
	protected void buildPanel() {
		contentPanel = new JPanel(new BorderLayout());
		
		JTable table = createTable();
		TableIndexListModel indexModel = new TableIndexListModel(gridModel);
		RowHeaderList rowHeader = createRowHeader(indexModel, table, contentPanel);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setRowHeaderView(rowHeader);
		scrollPane.setBorder(UIUtil.emptyBorder);
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		
		JToolBar toolBar = createToolBar();
		if(toolBar!=null) {
			contentPanel.add(toolBar, BorderLayout.NORTH);
		}
		
		outline = new CoreferenceCellRenderer();
		outline.setBorder(UIUtil.defaultContentBorder);
		
		JPanel footer = new JPanel(new BorderLayout());
		footer.add(outline, BorderLayout.CENTER);
		footer.setBorder(UIUtil.topLineBorder);
		contentPanel.add(BorderLayout.SOUTH, footer);
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		setData(null, null);
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return document!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public CoreferenceDocumentData getPresentedData() {
		return document;
	}

	/**
	 * @see de.ims.icarus.ui.table.TablePresenter#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceDocumentContentType();
	}

	/**
	 * @see de.ims.icarus.ui.table.TablePresenter#createTable()
	 */
	@Override
	protected JTable createTable() {
		JTable table = new JTable(gridModel, gridModel.getColumnModel());
		
		table.setDefaultRenderer(EntityGridNode.class, cellRenderer);
		table.setFillsViewportHeight(true);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setRowHeight(DEFAULT_CELL_HEIGHT);
		table.setIntercellSpacing(new Dimension(4, 4));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		EntityGridTableHeaderRenderer renderer = new EntityGridTableHeaderRenderer();
		header.setDefaultRenderer(renderer);
		renderer.setPreferredSize(new Dimension(0, DEFAULT_CELL_HEIGHT));
		
		return table;
	}

	protected RowHeaderList createRowHeader(ListModel<String> model, 
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
	
	/**
	 * @see de.ims.icarus.ui.table.TablePresenter#setData(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	protected void setData(Object data, Options options) {
		document = (CoreferenceDocumentData) data;
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		allocation = (CoreferenceAllocation) options.get("allocation"); //$NON-NLS-1$
		goldAllocation = (CoreferenceAllocation) options.get("goldAllocation"); //$NON-NLS-1$
		
		gridModel.setDocument(document);
		gridModel.reload(allocation, goldAllocation, filterSingletons, showGoldNodes);
	}

}
