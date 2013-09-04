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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.coref.view.CoreferenceCellRenderer;
import de.ims.icarus.plugins.coref.view.grid.labels.GridLabelBuilder;
import de.ims.icarus.plugins.coref.view.grid.labels.PatternLabelBuilder;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.list.RowHeaderList;
import de.ims.icarus.ui.table.TableIndexListModel;
import de.ims.icarus.ui.table.TablePresenter;
import de.ims.icarus.ui.table.TableRowHeaderRenderer;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.NamedObject;
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
		
	protected EntityGridTableModel gridModel;
	protected CoreferenceCellRenderer outline;
	protected EntityGridCellRenderer cellRenderer;
	
	protected JComboBox<Object> patternSelect;
	
	public static final int DEFAULT_CELL_HEIGHT = 20;
	public static final int DEFAULT_CELL_WIDTH = 70;
	
	protected ActionManager actionManager;
	protected CallbackHandler callbackHandler;
	protected Handler handler;

	public EntityGridPresenter() {
		// no-op
	}
	
	protected Handler createHandler() {
		return new Handler();
	}
	
	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandler();
	}

	@Override
	protected void init() {
		cellRenderer = new EntityGridCellRenderer();
		//cellRenderer.setLabelBuilder(new PatternLabelBuilder("(b-e)"));
		gridModel = new EntityGridTableModel();
	}
	
	public ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = ActionManager.globalManager().derive();
		}
		
		return actionManager;
	}
	
	protected void registerActionCallbacks() {
		ActionManager actionManager = getActionManager();
		
		if(callbackHandler==null) {
			callbackHandler = createCallbackHandler();
		}
		
		actionManager.addHandler("plugins.coref.entityGridPresenter.refreshAction",  //$NON-NLS-1$
				callbackHandler, "refresh"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.entityGridPresenter.openPreferencesAction",  //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.entityGridPresenter.toggleMarkFalseSpansAction",  //$NON-NLS-1$
				callbackHandler, "toggleMarkFalseSpans"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.entityGridPresenter.toggleShowGoldSpansAction",  //$NON-NLS-1$
				callbackHandler, "toggleShowGoldSpans"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.entityGridPresenter.toggleFilterSingletonsAction",  //$NON-NLS-1$
				callbackHandler, "toggleFilterSingletons"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.entityGridPresenter.toggleLabelModeAction",  //$NON-NLS-1$
				callbackHandler, "toggleLabelMode"); //$NON-NLS-1$
	}
	
	protected void refreshActions() {
		
		ActionManager actionManager = getActionManager();
		
		EntityGridTableModel model = getGridModel();

		actionManager.setSelected(model.isMarkFalseSpans(), "plugins.coref.entityGridPresenter.toggleMarkFalseSpansAction"); //$NON-NLS-1$
		actionManager.setSelected(model.isShowGoldSpans(), "plugins.coref.entityGridPresenter.toggleShowGoldSpansAction"); //$NON-NLS-1$
		actionManager.setSelected(model.isFilterSingletons(), "plugins.coref.entityGridPresenter.toggleFilterSingletonsAction"); //$NON-NLS-1$
		actionManager.setSelected(patternSelect.isEnabled(), "plugins.coref.entityGridPresenter.toggleLabelModeAction"); //$NON-NLS-1$
	}

	@Override
	protected JToolBar createToolBar() {
		if(patternSelect==null) {
			patternSelect = new JComboBox<>();
			patternSelect.setEditable(true);
			patternSelect.addActionListener(handler);
			UIUtil.resizeComponent(patternSelect, 150, 24);
			patternSelect.setEnabled(false);
		}
		
		Options options = new Options();
		options.put("patternSelect", patternSelect); //$NON-NLS-1$
		
		return getActionManager().createToolBar(
				"plugins.coref.entityGridPresenter.toolBarList", options); //$NON-NLS-1$
	}

	@Override
	protected void buildPanel() {
		contentPanel = new JPanel(new BorderLayout());
		handler = createHandler();
		
		ActionManager actionManager = getActionManager();
		URL actionLocation = EntityGridPresenter.class.getResource("entity-grid-presenter-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: entity-grid-presenter-actions.xml"); //$NON-NLS-1$
		
		try {
			actionManager.loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to load actions from file: "+actionLocation, e); //$NON-NLS-1$
			
			UIDummies.createDefaultErrorOutput(contentPanel, e);
			return;
		}
		
		table = createTable();
		TableIndexListModel indexModel = new TableIndexListModel(gridModel);
		RowHeaderList rowHeader = createRowHeader(indexModel, table, contentPanel);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setRowHeaderView(rowHeader);
		scrollPane.setBorder(UIUtil.topLineBorder);
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
		
		registerActionCallbacks();		
		refreshActions();
		refreshLabelBuilder();
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
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		EntityGridTableHeaderRenderer headerRenderer = new EntityGridTableHeaderRenderer();
		header.setDefaultRenderer(headerRenderer);
		//renderer.setPreferredSize(new Dimension(0, DEFAULT_CELL_HEIGHT));
		gridModel.getColumnModel().setHeaderRenderer(headerRenderer);
		
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
		
		refresh();
	}
	
	public void refresh() {
		gridModel.setDocument(document);
		gridModel.reload(allocation, goldAllocation);
	}
	
	protected void refreshLabelBuilder() {
		GridLabelBuilder builder = null;
		if(patternSelect!=null && patternSelect.isEnabled()) {
			Object value = patternSelect.getSelectedItem();
			String pattern = null;
			
			if(value instanceof String) {
				pattern = (String) value;
			} else if(value instanceof PatternExample) {
				pattern = ((PatternExample)value).getPattern();
			}
			
			if(pattern!=null && !pattern.isEmpty()) {
				try {
					builder = new PatternLabelBuilder(pattern);
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Invalid pattern: "+pattern, e); //$NON-NLS-1$
					
					UIUtil.beep();
					DialogFactory.getGlobalFactory().showError(null, 
							"plugins.coref.entityGridPresenterdialogs.invalidPattern.title",  //$NON-NLS-1$
							"plugins.coref.entityGridPresenterdialogs.invalidPattern.message",  //$NON-NLS-1$
							pattern);
				}
			}
		}
		
		cellRenderer.setLabelBuilder(builder);
		if(table!=null) {
			table.repaint();
		}
	}
	
	protected EntityGridTableModel getGridModel() {
		return gridModel;
	}
	
	protected class Handler implements ActionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			refreshLabelBuilder();
		}
	}

	public class CallbackHandler {
		protected CallbackHandler() {
			// no-op
		}

		public void openPreferences(ActionEvent e) {
			try {
				UIUtil.openConfigDialog("plugins.coref.appearance"); //$NON-NLS-1$
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to open preferences", ex); //$NON-NLS-1$
			}
		}

		public void toggleMarkFalseSpans(ActionEvent e) {
			// ignore
		}

		public void toggleMarkFalseSpans(boolean b) {
			EntityGridTableModel model = getGridModel();
			
			if(model.isMarkFalseSpans()==b) {
				return;
			}
			
			try {
				model.setMarkFalseSpans(b);
				EntityGridPresenter.this.refresh();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'markFalseSpans' flag", e); //$NON-NLS-1$
			}
		}

		public void toggleShowGoldSpans(ActionEvent e) {
			// ignore
		}

		public void toggleShowGoldSpans(boolean b) {
			EntityGridTableModel model = getGridModel();
			
			if(model.isShowGoldSpans()==b) {
				return;
			}
			
			try {
				model.setShowGoldSpans(b);
				EntityGridPresenter.this.refresh();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'showGoldSpans' flag", e); //$NON-NLS-1$
			}
		}

		public void toggleFilterSingletons(ActionEvent e) {
			// ignore
		}

		public void toggleFilterSingletons(boolean b) {
			EntityGridTableModel model = getGridModel();
			
			if(model.isFilterSingletons()==b) {
				return;
			}
			
			try {
				model.setFilterSingletons(b);
				EntityGridPresenter.this.refresh();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'filterSingletons' flag", e); //$NON-NLS-1$
			}
		}

		public void toggleLabelMode(ActionEvent e) {
			// ignore
		}

		public void toggleLabelMode(boolean b) {
			
			if(patternSelect.isEnabled()==b) {
				return;
			}
			
			try {
				patternSelect.setEnabled(b);
				refreshLabelBuilder();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle label mode", e); //$NON-NLS-1$
			}
		}

		public void refresh(ActionEvent e) {
			try {
				EntityGridPresenter.this.refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to refresh grid", ex); //$NON-NLS-1$
			}
		}
	}
	
	public static class PatternExample implements NamedObject {
		private String pattern;
		private String key;
		
		public PatternExample(String pattern, String key) {
			setPattern(pattern);
			setKey(key);
		}
		
		public String getPattern() {
			return pattern;
		}
		
		public String getKey() {
			return key;
		}
		
		public void setPattern(String pattern) {
			if(pattern==null || pattern.isEmpty())
				throw new IllegalArgumentException("Invalid pattern"); //$NON-NLS-1$
			
			this.pattern = pattern;
		}
		
		public void setKey(String key) {
			if(key==null || key.isEmpty())
				throw new IllegalArgumentException("Invalid key"); //$NON-NLS-1$
			
			this.key = key;
		}

		@Override
		public String toString() {
			return getKey()+": "+getPattern(); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.util.NamedObject#getName()
		 */
		@Override
		public String getName() {
			return ResourceManager.getInstance().get(key);
		}
	}
}
