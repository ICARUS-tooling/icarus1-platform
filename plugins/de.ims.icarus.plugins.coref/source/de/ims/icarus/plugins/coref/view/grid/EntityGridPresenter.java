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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;

import org.java.plugin.registry.Extension;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.DocumentData;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.annotation.AnnotatedCoreferenceDocumentData;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentAnnotationManager;
import de.ims.icarus.language.coref.helper.SpanFilters;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.coref.view.CoreferenceDocumentDataPresenter;
import de.ims.icarus.plugins.coref.view.PatternExample;
import de.ims.icarus.plugins.coref.view.grid.labels.GridLabelBuilder;
import de.ims.icarus.plugins.coref.view.grid.labels.PatternLabelBuilder;
import de.ims.icarus.resources.Localizable;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.TooltipFreezer;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.list.ListUtils;
import de.ims.icarus.ui.list.RowHeaderList;
import de.ims.icarus.ui.table.ColumnSelectionSynchronizer;
import de.ims.icarus.ui.table.TableColumnAdjuster;
import de.ims.icarus.ui.table.TableIndexListModel;
import de.ims.icarus.ui.table.TablePresenter;
import de.ims.icarus.ui.table.TableRowHeaderRenderer;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.HtmlUtils;
import de.ims.icarus.util.Installable;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.annotation.Annotation;
import de.ims.icarus.util.annotation.AnnotationControl;
import de.ims.icarus.util.annotation.AnnotationController;
import de.ims.icarus.util.annotation.AnnotationManager;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EntityGridPresenter extends TablePresenter implements AnnotationController, Installable {

	protected DocumentData document;
	protected CoreferenceAllocation allocation;
	protected CoreferenceAllocation goldAllocation;

	protected EntityGridTableModel gridModel;
	protected EntityGridCellRenderer cellRenderer;
	protected EntityGridTableHeaderRenderer headerRenderer;

	protected AnnotationManager annotationManager;

	protected JComboBox<Object> patternSelect;
	protected JLabel patternSelectInfo;

	protected CoreferenceDocumentDataPresenter.PresenterMenu presenterMenu;

	protected CoreferenceDocumentDataPresenter parent;

	protected TableColumnAdjuster columnAdjuster;

	protected boolean adjustColumnWidth = true;
	protected boolean patternActive = true;

	public static final int DEFAULT_CELL_HEIGHT = 20;
	public static final int DEFAULT_CELL_WIDTH = 95;

	protected ActionManager actionManager;
	protected CallbackHandler callbackHandler;
	protected Handler handler;

	protected JPopupMenu popupMenu;

	public EntityGridPresenter() {
		// no-op
	}

	protected Handler getHandler() {
		if(handler==null) {
			handler = new Handler();
		}
		return handler;
	}

	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandler();
	}

	@Override
	protected void init() {
		cellRenderer = new EntityGridCellRenderer();
		//cellRenderer.setLabelBuilder(new PatternLabelBuilder("(b-e)"));
		gridModel = new EntityGridTableModel();

		presenterMenu = new CoreferenceDocumentDataPresenter.PresenterMenu(this, getHandler());
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

		actionManager.setSelected(adjustColumnWidth,
				"plugins.coref.entityGridPresenter.toggleAdjustColumnWidthAction"); //$NON-NLS-1$
		actionManager.setSelected(patternActive,
				"plugins.coref.entityGridPresenter.toggleLabelModeAction"); //$NON-NLS-1$

		actionManager.addHandler("plugins.coref.entityGridPresenter.refreshAction",  //$NON-NLS-1$
				callbackHandler, "refresh"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.entityGridPresenter.openPreferencesAction",  //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.entityGridPresenter.toggleMarkFalseMentionsAction",  //$NON-NLS-1$
				callbackHandler, "toggleMarkFalseMentions"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.entityGridPresenter.toggleIncludeGoldMentionsAction",  //$NON-NLS-1$
				callbackHandler, "toggleIncludeGoldMentions"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.entityGridPresenter.toggleFilterSingletonsAction",  //$NON-NLS-1$
				callbackHandler, "toggleFilterSingletons"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.entityGridPresenter.toggleLabelModeAction",  //$NON-NLS-1$
				callbackHandler, "toggleLabelMode"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.entityGridPresenter.toggleAdjustColumnWidthAction",  //$NON-NLS-1$
				callbackHandler, "toggleAdjustColumnWidth"); //$NON-NLS-1$
	}

	protected void refreshActions() {
		if(contentPanel==null) {
			return;
		}

		ActionManager actionManager = getActionManager();

		EntityGridTableModel model = getGridModel();

		actionManager.setSelected(model.isMarkFalseMentions(), "plugins.coref.entityGridPresenter.toggleMarkFalseMentionsAction"); //$NON-NLS-1$
		actionManager.setSelected(model.isIncludeGoldMentions(), "plugins.coref.entityGridPresenter.toggleIncludeGoldMentionsAction"); //$NON-NLS-1$
		actionManager.setSelected(model.isFilterSingletons(), "plugins.coref.entityGridPresenter.toggleFilterSingletonsAction"); //$NON-NLS-1$
		actionManager.setSelected(patternSelect.isEnabled(), "plugins.coref.entityGridPresenter.toggleLabelModeAction"); //$NON-NLS-1$

		boolean hasGold = goldAllocation!=null && goldAllocation!=allocation;
		actionManager.setEnabled(hasGold,
				"plugins.coref.entityGridPresenter.toggleMarkFalseMentionsAction",  //$NON-NLS-1$
				"plugins.coref.entityGridPresenter.toggleIncludeGoldMentionsAction"); //$NON-NLS-1$
	}

	@Override
	public AnnotationManager getAnnotationManager() {
		if(annotationManager==null) {
			annotationManager = new CoreferenceDocumentAnnotationManager();
		}
		return annotationManager;
	}

	public void setAnnotationManager(AnnotationManager annotationManager) {
		this.annotationManager = annotationManager;
	}

	@Override
	protected ActionComponentBuilder createToolBar() {
		ActionComponentBuilder builder = new ActionComponentBuilder(getActionManager());
		builder.setActionListId("plugins.coref.entityGridPresenter.toolBarList"); //$NON-NLS-1$

		if(patternSelect==null) {
			String pattern = ConfigRegistry.getGlobalRegistry().getString(
					"plugins.coref.appearance.grid.defaultLabelPattern"); //$NON-NLS-1$
			if(pattern!=null && pattern.trim().isEmpty()) {
				pattern = null;
			}

			patternActive = ConfigRegistry.getGlobalRegistry().getBoolean(
					"plugins.coref.appearance.grid.usePatternLabel"); //$NON-NLS-1$

			patternSelect = new JComboBox<>();
			patternSelect.setEditable(true);
			if(pattern!=null) {
				MutableComboBoxModel<Object> model = (MutableComboBoxModel<Object>) patternSelect.getModel();
				model.addElement(pattern);
			}
			patternSelect.setSelectedItem(pattern);
			patternSelect.setEnabled(patternActive);
			patternSelect.addActionListener(getHandler());
			UIUtil.resizeComponent(patternSelect, 300, 24);
		}

		if(patternSelectInfo==null) {
			final JLabel label = new JLabel();
			label.addMouseListener(new TooltipFreezer());label.setIcon(UIUtil.getInfoIcon());

			Localizable localizable = new Localizable() {

				@Override
				public void localize() {
					label.setToolTipText(createPatternSelectTooltip());
				}
			};

			localizable.localize();
			ResourceManager.getInstance().getGlobalDomain().addItem(localizable);

			patternSelectInfo = label;
		}

		builder.addOption("errorInfoLabel", CoreferenceUtils.createErrorInfoLabel()); //$NON-NLS-1$
		builder.addOption("patternSelect", patternSelect); //$NON-NLS-1$
		builder.addOption("patternSelectInfo", patternSelectInfo); //$NON-NLS-1$
		AnnotationControl annotationControl = createAnnotationControl();
		if(annotationControl!=null) {
			builder.addOption("annotationControl", annotationControl.getComponents()); //$NON-NLS-1$
		}

		return builder;
	}

	protected String createPatternSelectTooltip() {
		StringBuilder sb = new StringBuilder(300);
		ResourceManager rm = ResourceManager.getInstance();

		sb.append("<html>"); //$NON-NLS-1$
		sb.append("<h3>").append(rm.get("plugins.coref.labelPattern.title")).append("</h3>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sb.append("<table>"); //$NON-NLS-1$
		sb.append("<tr><th>") //$NON-NLS-1$
			.append(rm.get("plugins.coref.labelPattern.character")).append("</th><th>") //$NON-NLS-1$ //$NON-NLS-2$
			.append(rm.get("plugins.coref.labelPattern.description")).append("</th></tr>"); //$NON-NLS-1$ //$NON-NLS-2$

		Map<Object, Object> mc = PatternLabelBuilder.magicCharacters;
		for(Entry<Object, Object> entry : mc.entrySet()) {
			String c = entry.getKey().toString();
			String key = entry.getValue().toString();

			sb.append("<tr><td>").append(HtmlUtils.escapeHTML(c)) //$NON-NLS-1$
			.append("</td><td>").append(rm.get(key)).append("</td></tr>"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		sb.append("</table>"); //$NON-NLS-1$

		return sb.toString();
	}

	protected AnnotationControl createAnnotationControl() {
		AnnotationControl annotationControl = new AnnotationControl(true);
		annotationControl.setAnnotationManager(getAnnotationManager());
		return annotationControl;
	}

	@Override
	protected void buildPanel() {
		contentPanel = new JPanel(new BorderLayout());

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

		columnAdjuster = new TableColumnAdjuster(table);
		columnAdjuster.setColumnDataIncluded(true);
		columnAdjuster.setColumnHeaderIncluded(false);
		columnAdjuster.setOnlyAdjustLarger(false);
		columnAdjuster.setDynamicAdjustment(false);

		ActionComponentBuilder builder = createToolBar();
		if(builder!=null) {
			contentPanel.add(builder.buildToolBar(), BorderLayout.NORTH);
		}

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
	public DocumentData getPresentedData() {
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

		UIUtil.enableRighClickTableSelection(table);
		table.setDefaultRenderer(EntityGridNode.class, cellRenderer);
		table.setFillsViewportHeight(true);
		//table.setRowSelectionAllowed(false);
		//table.setColumnSelectionAllowed(false);
		table.setRowHeight(DEFAULT_CELL_HEIGHT);
		//table.setIntercellSpacing(new Dimension(4, 4));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.addMouseListener(getHandler());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(getHandler());
		table.getColumnModel().getSelectionModel().addListSelectionListener(getHandler());
		new ColumnSelectionSynchronizer(table);

		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		headerRenderer = new EntityGridTableHeaderRenderer(header.getDefaultRenderer());
		headerRenderer.setAutoAdjustEnabled(adjustColumnWidth);
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
		document = (DocumentData) data;

		if(options==null) {
			options = Options.emptyOptions;
		}
		allocation = (CoreferenceAllocation) options.get("allocation"); //$NON-NLS-1$
		goldAllocation = (CoreferenceAllocation) options.get("goldAllocation"); //$NON-NLS-1$

		refresh();
	}

	public void refresh() {
		Annotation annotation = null;
		if(document instanceof AnnotatedCoreferenceDocumentData) {
			annotation = ((AnnotatedCoreferenceDocumentData)document).getAnnotation();
		}
		getAnnotationManager().setAnnotation(annotation);

		gridModel.setDocument(document);
		gridModel.reload(allocation, goldAllocation);

		refreshGridHeader();

		tryAdjustColumnWidth();

		refreshActions();
	}

	protected void refreshGridHeader() {
		if(headerRenderer==null) {
			return;
		}

		boolean hasValidGold = goldAllocation!=null && goldAllocation!=allocation;

		headerRenderer.setShowErrorLabels(hasValidGold);

		JTableHeader header = table.getTableHeader();

		headerRenderer.setPrototypeMode(true);
		try {
			header.resizeAndRepaint();
		} finally {
			headerRenderer.setPrototypeMode(false);
		}

//		JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(
//				JScrollPane.class, table);
//		JViewport viewport = scrollPane.getColumnHeader();
//		viewport.revalidate();
//		viewport.repaint();
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

					// Legal pattern
					MutableComboBoxModel<Object> model =
							(MutableComboBoxModel<Object>) patternSelect.getModel();

					int index = ListUtils.indexOf(pattern, model);

					if(index==-1) {
						model.addElement(pattern);
					}
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE,
							"Invalid pattern: "+pattern, e); //$NON-NLS-1$

					UIUtil.beep();
					DialogFactory.getGlobalFactory().showError(null,
							"plugins.coref.entityGridPresenter.dialogs.invalidPattern.title",  //$NON-NLS-1$
							"plugins.coref.entityGridPresenter.dialogs.invalidPattern.message",  //$NON-NLS-1$
							pattern);
				}
			}
		}

		cellRenderer.setLabelBuilder(builder);
		if(table!=null) {
			tryAdjustColumnWidth();
			table.repaint();
		}
	}

	protected EntityGridTableModel getGridModel() {
		return gridModel;
	}

	/**
	 * @see de.ims.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		parent = null;
		if(target instanceof CoreferenceDocumentDataPresenter) {
			parent = (CoreferenceDocumentDataPresenter) target;
		}
	}

	/**
	 * @see de.ims.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		parent = null;
	}

	protected void tryAdjustColumnWidth() {
		if(columnAdjuster==null) {
			return;
		}

		if(headerRenderer!=null) {
			headerRenderer.setAutoAdjustEnabled(adjustColumnWidth);
		}

		if(adjustColumnWidth) {
			columnAdjuster.adjustColumns();
		} else {
			gridModel.getColumnModel().resetColumnSize();
		}
	}

	protected void showPopup(MouseEvent e) {
		if(popupMenu==null) {
			// Create new popup menu

			Options options = new Options();
			options.put("showInMenu", presenterMenu); //$NON-NLS-1$
			popupMenu = getActionManager().createPopupMenu(
					"plugins.coref.entityGridPresenter.popupMenuList", options); //$NON-NLS-1$

			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create popup menu"); //$NON-NLS-1$
			}
		}

		if(popupMenu!=null) {

			refreshActions();
			presenterMenu.refresh();
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	protected void outlineProperties(EntityGridNode node) {
		if(parent==null) {
			return;
		}

		try {
			parent.outlineMembers(node.getSpans(), null);
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to outline properties: "+String.valueOf(node), e); //$NON-NLS-1$
		}
	}

	protected void togglePresenter(Extension extension) {
		if(extension==null)
			throw new NullPointerException("Invalid extension"); //$NON-NLS-1$

		if(parent==null) {
			return;
		}

		try {
			Options options = new Options();

			int row = table.getSelectedRow();
			int column = table.getSelectedColumn();
			if(row!=-1 && column!=-1) {
				EntityGridNode node = (EntityGridNode) table.getValueAt(row, column);
				if(node!=null) {
					options.put("filter", createFilterForNode(node)); //$NON-NLS-1$
				}
			}

			if(!options.isEmpty()) {
				parent.togglePresenter(extension, options);
			}
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to switch to presennter: "+extension.getUniqueId(), e); //$NON-NLS-1$
		}
	}

	protected Filter createFilterForNode(EntityGridNode node) {
		if(node==null) {
			return null;
		}

		Collection<Span> spans = new ArrayList<>();
		for(int i=0; i<node.getSpanCount(); i++) {
			spans.add(node.getSpan(i));
		}

		return new SpanFilters.SpanFilter(spans);
	}

	protected class Handler extends MouseAdapter implements ActionListener, ListSelectionListener {

		protected void maybeShowPopup(MouseEvent e) {
			if(e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() instanceof JMenuItem) {
				String uid = e.getActionCommand();
				Extension extension = PluginUtil.getExtension(uid);
				if(extension!=null) {
					togglePresenter(extension);
				}
			} else {
				refreshLabelBuilder();
			}
		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getValueIsAdjusting()) {
				return;
			}

			int row = table.getSelectedRow();
			int column = table.getSelectedColumn();

			if(row==-1 || column==-1) {
				return;
			}

			Object value = table.getValueAt(row, column);
			if(value==null) {
				return;
			}

			outlineProperties((EntityGridNode) value);
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

		public void toggleMarkFalseMentions(ActionEvent e) {
			// ignore
		}

		public void toggleMarkFalseMentions(boolean b) {
			EntityGridTableModel model = getGridModel();

			if(model.isMarkFalseMentions()==b) {
				return;
			}

			try {
				model.setMarkFalseMentions(b);
				EntityGridPresenter.this.refresh();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to toggle 'markFalseMentions' flag", e); //$NON-NLS-1$
			}
		}

		public void toggleIncludeGoldMentions(ActionEvent e) {
			// ignore
		}

		public void toggleIncludeGoldMentions(boolean b) {
			EntityGridTableModel model = getGridModel();

			if(model.isIncludeGoldMentions()==b) {
				return;
			}

			try {
				model.setIncludeGoldMentions(b);
				EntityGridPresenter.this.refresh();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to toggle 'includeGoldMentions' flag", e); //$NON-NLS-1$
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

			if(patternActive==b) {
				return;
			}

			try {
				patternActive = b;
				patternSelect.setEnabled(b);
				refreshLabelBuilder();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to toggle label mode", e); //$NON-NLS-1$
			}
		}

		public void toggleAdjustColumnWidth(ActionEvent e) {
			// ignore
		}

		public void toggleAdjustColumnWidth(boolean b) {

			if(adjustColumnWidth==b) {
				return;
			}

			try {
				adjustColumnWidth = b;

				tryAdjustColumnWidth();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to adjust table columns", e); //$NON-NLS-1$
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
}
