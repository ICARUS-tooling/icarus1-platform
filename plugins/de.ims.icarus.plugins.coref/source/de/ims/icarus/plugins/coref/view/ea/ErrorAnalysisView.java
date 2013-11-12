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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.ea;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry.LoadJob;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.list.FilterList;
import de.ims.icarus.ui.list.FilterListModel;
import de.ims.icarus.ui.list.RowHeaderList;
import de.ims.icarus.ui.table.TableRowHeaderRenderer;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.DataListModel;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */


/**
 * @author Markus Gärtner
 * @version $Id$
 */
public class ErrorAnalysisView extends View {

	private CallbackHandler callbackHandler;
	private Handler handler;
	private JPopupMenu popupMenu;
	
	private AbstractErrorAnalysisTableModel tableModel;
	private JTable table;
	private RowHeaderList rowHeader;
	private JLabel loadingLabel;
	private JSplitPane splitPane;
	private JPanel contentPanel;

	private FilterList list;
	private DataListModel<CoreferenceDocumentData> listModel;
	private FilterListModel filterModel;
	private DocumentFilterListCellRenderer listRenderer;
	
	private CoreferenceDocumentData document;
	private Options options;
	
	private boolean analyzeSubSet = false;

	public ErrorAnalysisView() {
		// no-op
	}

	@Override
	public void init(JComponent container) {

		// Load actions
		if (!defaultLoadActions(ErrorAnalysisView.class,
				"error-analysis-view-actions.xml")) { //$NON-NLS-1$
			return;
		}

		// Init ui
		container.setLayout(new BorderLayout());
		
		contentPanel = new JPanel(new BorderLayout());

		loadingLabel = UIUtil.defaultCreateLoadingLabel(container);
		
		table = new JTable();
		table.getTableHeader().setReorderingAllowed(false);
		table.setRowHeight(24);
		table.addMouseListener(getHandler());
		table.setIntercellSpacing(new Dimension(4, 4));
		
		rowHeader = new RowHeaderList();
		rowHeader.setFixedCellWidth(120);
		rowHeader.setMinimumCellWidth(60);
		rowHeader.setResizingAllowed(true);
		rowHeader.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowHeader.setFixedCellHeight(table.getRowHeight());
		rowHeader.setBackground(container.getBackground());
		rowHeader.setForeground(table.getForeground());		
		TableRowHeaderRenderer rowHeaderRenderer = new TableRowHeaderRenderer(rowHeader, table);
		rowHeader.setCellRenderer(rowHeaderRenderer);
		
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setRowHeaderView(rowHeader);
		scrollPane.setBorder(UIUtil.topLineBorder);
		
		listModel = new DataListModel<>();
		filterModel = new FilterListModel();
		listRenderer = new DocumentFilterListCellRenderer();
		list = new FilterList(filterModel, listRenderer);
		list.setBorder(UIUtil.defaultContentBorder);
		UIUtil.enableRighClickListSelection(list);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addMouseListener(getHandler());
		
		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setBorder(UIUtil.topLineBorder);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.setDividerSize(5);
		splitPane.setBorder(UIUtil.emptyBorder);
		splitPane.setLeftComponent(listScrollPane);
		splitPane.setDividerLocation(250);
		
		JToolBar toolBar = createToolBar().buildToolBar();
		
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		
		container.add(contentPanel, BorderLayout.CENTER);
		container.add(toolBar, BorderLayout.NORTH);

		registerActionCallbacks();

		refreshActions();
		
		refreshUIState();
	}

	private Handler getHandler() {
		if (handler == null) {
			handler = new Handler();
		}
		return handler;
	}
	
	private void refreshUIState() {
		JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(
				JScrollPane.class, table);
		
		contentPanel.removeAll();
		
		if(analyzeSubSet) {
			splitPane.setRightComponent(scrollPane);
			contentPanel.add(splitPane, BorderLayout.CENTER);
		} else {
			contentPanel.add(scrollPane, BorderLayout.CENTER);
		}
		
		contentPanel.revalidate();
		contentPanel.repaint();
	}

	private void registerActionCallbacks() {
		if (callbackHandler == null) {
			callbackHandler = new CallbackHandler();
		}

		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.setSelected(analyzeSubSet, "plugins.coref.errorAnalysisView.toggleAnalyzeSubSetAction"); //$NON-NLS-1$

		actionManager.addHandler("plugins.coref.errorAnalysisView.refreshAction",  //$NON-NLS-1$
				callbackHandler, "refresh"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.errorAnalysisView.toggleAnalyzeSubSetAction",  //$NON-NLS-1$
				callbackHandler, "toggleAnalyzeSubSet"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.coref.errorAnalysisView.selectAllDocumentsAction",  //$NON-NLS-1$
				callbackHandler, "selectAllDocuments"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.errorAnalysisView.unselectAllDocumentsAction",  //$NON-NLS-1$
				callbackHandler, "unselectAllDocuments"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.errorAnalysisView.invertDocumentsSelectionAction",  //$NON-NLS-1$
				callbackHandler, "invertDocumentsSelection"); //$NON-NLS-1$
	}

	private void refreshActions() {
		// TODO refresh actions
	}
	
	private ActionComponentBuilder createToolBar() {
		ActionComponentBuilder builder = new ActionComponentBuilder(getDefaultActionManager());
		
		builder.setActionListId("plugins.coref.errorAnalysisView.toolBarList"); //$NON-NLS-1$
		
		return builder;
	}
	
	private boolean checkAllocation(final Options options, final String key) {
		Object value = options.get(key);
		if(value instanceof AllocationDescriptor) {
			final AllocationDescriptor descriptor = (AllocationDescriptor) value;

			options.put(key, descriptor.getAllocation());
			
			if(!descriptor.isLoaded() && !descriptor.isLoading()) {				
				final String name = descriptor.getName();
				String title = ResourceManager.getInstance().get(
						"plugins.coref.labels.loadingAllocation"); //$NON-NLS-1$
				Object task = new LoadJob(descriptor) {
					@Override
					protected void done() {
						try {
							get();
						} catch(CancellationException | InterruptedException e) {
							// ignore
						} catch(Exception e) {
							LoggerFactory.log(this, Level.SEVERE, 
									"Failed to load allocation: "+name, e); //$NON-NLS-1$
							
							UIUtil.beep();
						} finally {
							getContainer().remove(loadingLabel);
							getContainer().revalidate();
							getContainer().repaint();
									
							refresh();
						}
					}				
				};
				TaskManager.getInstance().schedule(task, title, null, null, 
						TaskPriority.DEFAULT, true);
				
				return true;
			}
		}
		
		return false;
	}
	
	private Options getPresenterOptions() {
		Options options = new Options(this.options);
		
		if(checkAllocation(options, "allocation")) { //$NON-NLS-1$
			return null;
		}
		if(checkAllocation(options, "goldAllocation")) { //$NON-NLS-1$
			return null;
		}
		
		return options;
	}
	
	private void refresh() {
		
		CoreferenceDocumentData document = this.document;
		Options options = getPresenterOptions();
		
		if(document==null) {
			return;
		}
		
		// Check if some allocations need to be loaded
		if(options==null) {
			contentPanel.removeAll();
			contentPanel.add(loadingLabel, BorderLayout.CENTER);
			return;
		}
		
		Set<CoreferenceDocumentData> documents = collectDocuments();
		if(documents.isEmpty()) {
			// Nothing to do here
			return;
		}
		
		if(tableModel==null) {
			tableModel = new DefaultErrorAnalysisTableModel();
		}
		
		try {
			tableModel.setDocuments(documents);
			tableModel.setAllocation((CoreferenceAllocation) options.get("allocation")); //$NON-NLS-1$
			tableModel.setGoldAllocation((CoreferenceAllocation) options.get("goldAllocation")); //$NON-NLS-1$
			tableModel.rebuild();
		} catch(Exception e) {
			LoggerFactory.error(this, "Failed to perform error analysis on document: "+document.getId(), e); //$NON-NLS-1$
			UIUtil.beep();
			
			showError(e);
			return;
		}
		
		table.setModel(tableModel);
		rowHeader.setModel(tableModel.getRowHeaderModel());
		
		refreshActions();
	}
	
	private Set<CoreferenceDocumentData> collectDocuments() {
		if(document==null)
			throw new IllegalStateException("No document available"); //$NON-NLS-1$
		
		Set<CoreferenceDocumentData> result = new HashSet<>();
		
		if(analyzeSubSet) {
			for(int i=0; i<filterModel.getSize(); i++) {
				if(filterModel.isSet(i)) {
					result.add(listModel.getElementAt(i));
				}
			}
		} else {
			result.addAll(document.getDocumentSet().getDocuments());
		}
		
		return result;
	}
	
	private void setDocument(CoreferenceDocumentData document) {
		if(this.document==document) {
			return;
		}
		
		this.document = document;
		
		if(document==null) {
			list.clearSelection();
			listModel.clear();
		} else if(listModel.getDataList()!=document.getDocumentSet()) {
			listModel.setDataList(document.getDocumentSet());
			filterModel.setSize(listModel.getSize());
		}
		
		listRenderer.setDocumentSet(document==null ? null : document.getDocumentSet());
		
		refresh();
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#handleRequest(de.ims.icarus.util.mpi.Message)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.DISPLAY.equals(message.getCommand())
				|| Commands.PRESENT.equals(message.getCommand())) {
			Object data = message.getData();
			CoreferenceDocumentData document = null;
			if(data instanceof CoreferenceDocumentData) {
				document = (CoreferenceDocumentData) data;
			} else if(data instanceof CoreferenceDocumentSet) {
				CoreferenceDocumentSet documentSet = (CoreferenceDocumentSet) data;
				if(documentSet.size()>0) {
					document = documentSet.get(0);
				}
			}
			
			if(document!=null) {
				selectViewTab();
				
				options = message.getOptions();
				setDocument(document);
				
				return message.successResult(this, null);
			} else {
				return message.unsupportedDataResult(this);
			}
		} else {
			return message.unknownRequestResult(this);
		}
	}

	private void showPopup(MouseEvent e) {
		if(popupMenu==null) {
			// Create new popup menu
			
			popupMenu = getDefaultActionManager().createPopupMenu(
					"plugins.coref.errorAnalysisView.popupMenuList", null); //$NON-NLS-1$
			
			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create popup menu"); //$NON-NLS-1$
			}
		}
		
		if(popupMenu!=null) {
			refreshActions();
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private class Handler extends MouseAdapter {

		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount()!=2 || !SwingUtilities.isLeftMouseButton(e)) {
				return;
			}
			
			int row = list.locationToIndex(e.getPoint());
			if(row==-1) {
				return;
			}
			
			filterModel.flipElementAt(row);
		}
		
		private void maybeShowPopup(MouseEvent e) {
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

	}

	public class CallbackHandler {

		private CallbackHandler() {
			// no-op
		}

		public void refresh(ActionEvent e) {
			try {
				ErrorAnalysisView.this.refresh();
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to refresh", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void selectAllDocuments(ActionEvent e) {
			try {
				filterModel.fill(true);
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to select all documents", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void unselectAllDocuments(ActionEvent e) {
			try {
				filterModel.fill(false);
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to unselect all documents", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void invertDocumentsSelection(ActionEvent e) {
			try {
				filterModel.flip();
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to invert documents selection", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}

		public void toggleAnalyzeSubSet(ActionEvent e) {
			// no-op
		}

		public void toggleAnalyzeSubSet(boolean b) {
			if(analyzeSubSet==b) {
				return;
			}

			analyzeSubSet = b;
			
			try {
				refreshUIState();
			} catch (Exception ex) {
				LoggerFactory.error(this, "Failed to toggle 'analyzeSubSet' flag", ex); //$NON-NLS-1$
				UIUtil.beep();

				showError(ex);
			}
		}
	}
}