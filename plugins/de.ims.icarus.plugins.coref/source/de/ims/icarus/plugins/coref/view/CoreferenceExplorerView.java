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
package de.ims.icarus.plugins.coref.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.language.coref.registry.AllocationListWrapper;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry;
import de.ims.icarus.language.coref.registry.DefaultAllocationDescriptor;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.coref.CorefConstants;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.Updatable;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.ChoiceFormEntry;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.list.ComboBoxListWrapper;
import de.ims.icarus.ui.list.ListUtils;
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
public class CoreferenceExplorerView extends View implements Updatable {
	
	private JList<CoreferenceDocumentData> list;
	private DataListModel<CoreferenceDocumentData> listModel;
	
	private JLabel loadingLabel;
	
	private ComboBoxListWrapper<DocumentSetDescriptor> documentSetModel;
	private AllocationListWrapper allocationModel;
	private AllocationListWrapper goldAllocationModel;
	
	
	private DocumentSetDescriptor descriptor;
//	private AllocationDescriptor allocation;
//	private AllocationDescriptor goldAllocation;
		
	private Handler handler;
	private CallbackHandler callbackHandler;
	private JPopupMenu popupMenu;
	
	public CoreferenceExplorerView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// Load actions
		if(!defaultLoadActions(CoreferenceExplorerView.class, 
				"coreference-explorer-view-actions.xml")) { //$NON-NLS-1$
			return;
		}

		container.setLayout(new BorderLayout());
		
		FormBuilder formBuilder = FormBuilder.newLocalizingBuilder();
		ChoiceFormEntry entry;
		
		// Document-set selection
		documentSetModel = new ComboBoxListWrapper<>(
				CoreferenceRegistry.getInstance().getDocumentSetListModel());
		entry = new ChoiceFormEntry( 
				"plugins.coref.labels.documentSet", documentSetModel, false); //$NON-NLS-1$
		entry.getComboBox().addActionListener(getHandler());
		formBuilder.addEntry("documentSet", entry); //$NON-NLS-1$
		// Allocation selection
		allocationModel = new AllocationListWrapper(false);
		entry = new ChoiceFormEntry( 
				"plugins.coref.labels.allocation", allocationModel, false); //$NON-NLS-1$
		entry.getComboBox().addActionListener(getHandler());
		formBuilder.addEntry("allocation", entry); //$NON-NLS-1$
		// Gold allocation selection
		goldAllocationModel = new AllocationListWrapper(true);
		entry = new ChoiceFormEntry( 
				"plugins.coref.labels.goldAllocation", goldAllocationModel, false); //$NON-NLS-1$
		entry.getComboBox().addActionListener(getHandler());
		formBuilder.addEntry("goldAllocation", entry); //$NON-NLS-1$
		
		formBuilder.buildForm();
		JComponent header = (JComponent) formBuilder.getContainer();
		header.setBorder(UIUtil.defaultContentBorder);
		
		loadingLabel = UIUtil.defaultCreateLoadingLabel(container);
		loadingLabel.setVisible(false);
		
		JPanel headerPanel = new JPanel(new BorderLayout());
		// TODO create tool-bar
		headerPanel.add(header, BorderLayout.CENTER);
		headerPanel.add(loadingLabel, BorderLayout.SOUTH);
		
		listModel = new DataListModel<>();
		listModel.addListDataListener(getHandler());
		list = new JList<>(listModel);
		list.setCellRenderer(new DocumentListCellRenderer());
		list.setBorder(UIUtil.defaultContentBorder);
		UIUtil.enableRighClickListSelection(list);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addMouseListener(getHandler());
		list.addListSelectionListener(getHandler());
		
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(UIUtil.topLineBorder);
		
		container.add(headerPanel, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		
		registerActionCallbacks();
		refreshActions();
	}
	
	private Handler getHandler() {
		if(handler==null) {
			handler = new Handler();
		}
		
		return handler;
	}

	private void displayData(DocumentSetDescriptor descriptor, Options options) {
		
		this.descriptor = descriptor;
		
		if(listModel==null) {
			return;
		}
		
		// Request tab focus only when actually displaying data!
		if(descriptor!=null) {
			selectViewTab();
		}
		
		// Reset user selection
		documentSetModel.setSelectedItem(descriptor);
		allocationModel.setDescriptor(descriptor);
		goldAllocationModel.setDescriptor(descriptor);
		allocationModel.setSelectedItem(CoreferenceRegistry.getInstance()
				.getDefaultAllocationDescriptor(descriptor));
		goldAllocationModel.setSelectedItem(CoreferenceRegistry.dummyEntry);
		
		if(descriptor!=null && !descriptor.isLoaded()) {
			listModel.clear();
			loadingLabel.setVisible(true);
			
			CoreferenceRegistry.loadDocumentSet(descriptor, new Runnable() {
				
				@Override
				public void run() {
					update();
					loadingLabel.setVisible(false);
				}
			});
		} else {
			update();
		}
	}
	
	@Override
	public boolean update() {
		if(descriptor==null) {
			return false;
		}
		loadingLabel.setVisible(descriptor.isLoading());
		if(!descriptor.isLoaded()) {
			return false;
		}
		
		listModel.setDataList(descriptor.getDocumentSet());
		
		allocationModel.update();
		goldAllocationModel.update();
		
		return true;
	}
	
	private void displaySelectedValue() {
		int index = list.getSelectedIndex();
		
		if(index==-1) {
			return;
		}
		
		try {
			// Fetch document
			CoreferenceDocumentData document = listModel.getElementAt(index);
			if(document==null) {
				return;
			}
			
			Options options = new Options();
			
			// Fetch allocations	
			options.put("allocation", getAllocation(allocationModel, false)); //$NON-NLS-1$
			options.put("goldAllocation", getAllocation(goldAllocationModel, true)); //$NON-NLS-1$
			
			Message message = new Message(this, Commands.PRESENT, document, options);
			sendRequest(CorefConstants.COREFERENCE_DOCUMENT_VIEW_ID, message);
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to forward presentation of document at index "+index, e); //$NON-NLS-1$
			
			UIUtil.beep();
			showError(e);
		}
	}
	
	private void clearOutline() {
		try {
			Message message = new Message(this, Commands.CLEAR);
			sendRequest(CorefConstants.COREFERENCE_DOCUMENT_VIEW_ID, message);
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to forward clear command", e); //$NON-NLS-1$
			
			UIUtil.beep();
			showError(e);
		}
	}
	
	private void analyzeSelectedValue() {
		int index = list.getSelectedIndex();
		
		if(index==-1) {
			return;
		}
		
		try {
			// Fetch document
			CoreferenceDocumentData document = listModel.getElementAt(index);
			if(document==null) {
				return;
			}
			
			Options options = new Options();
			
			// Fetch allocations	
			options.put("allocation", getAllocation(allocationModel, false)); //$NON-NLS-1$
			options.put("goldAllocation", getAllocation(goldAllocationModel, true)); //$NON-NLS-1$
			
			Message message = new Message(this, Commands.PRESENT, document, options);
			sendRequest(CorefConstants.ERROR_ANALYSIS_VIEW_ID, message);
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to forward analysis of document at index "+index, e); //$NON-NLS-1$
			
			UIUtil.beep();
			showError(e);
		}
	}
	
	private AllocationDescriptor getAllocation(AllocationListWrapper model, boolean allowDefault) {
		Object selectedItem = model.getSelectedItem();
		if(selectedItem==CoreferenceRegistry.dummyEntry || 
				(!allowDefault && selectedItem instanceof DefaultAllocationDescriptor)) {
			selectedItem = null;
		}
		
		return (AllocationDescriptor) selectedItem;
	}
	
	@Override
	public void reset() {
		listModel.clear();
	}

	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.DISPLAY.equals(message.getCommand())
				|| Commands.PRESENT.equals(message.getCommand())) {
			Object data = message.getData();
			if(data instanceof DocumentSetDescriptor) {
				displayData((DocumentSetDescriptor) data, message.getOptions());
				
				return message.successResult(this, null);
			} else {
				return message.unsupportedDataResult(this);
			}
		} else if(Commands.CLEAR.equals(message.getCommand())) {
			reset();
			return message.successResult(this, null);
		} else if(Commands.SELECT.equals(message.getCommand())) {
			Object data = message.getData();
			int index = -1;
			if(data instanceof Integer) {
				index = (int) data;
			} else if(data instanceof CoreferenceDocumentData) {
				CoreferenceDocumentData document = (CoreferenceDocumentData)data;
				index = ListUtils.indexOf(document, listModel);
			} else {
				return message.unsupportedDataResult(this);
			}
			
			if(index==-1) {
				list.clearSelection();
			} else {
				list.setSelectedIndex(index);
			}
			
			return message.successResult(this, null);
		} else {
			return message.unknownRequestResult(this);
		}
	}
	
	private void refreshActions() {
		boolean hasSelection = list.getSelectedIndex()!=-1;
		
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.setEnabled(hasSelection, 
				"plugins.coref.coreferenceExplorerView.inspectDocumentAction", //$NON-NLS-1$
				"plugins.coref.coreferenceExplorerView.analyzeDocumentAction"); //$NON-NLS-1$
	}
	
	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.addHandler("plugins.coref.coreferenceExplorerView.inspectDocumentAction",  //$NON-NLS-1$
				callbackHandler, "inspectDocument"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceExplorerView.analyzeDocumentAction",  //$NON-NLS-1$
				callbackHandler, "analyzeDocument"); //$NON-NLS-1$
	}
	
	private void updateModels() {
		documentSetModel.update();
		allocationModel.update();
		goldAllocationModel.update();
	}

	private void showPopup(MouseEvent e) {
		if(popupMenu==null) {
			// Create new popup menu
			
			popupMenu = getDefaultActionManager().createPopupMenu(
					"plugins.coref.coreferenceExplorerView.popupMenuList", null); //$NON-NLS-1$
			
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

	private class Handler extends MouseAdapter 
			implements ListSelectionListener, ActionListener, EventListener, ListDataListener {

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getValueIsAdjusting()) {
				return;
			}
			
			displaySelectedValue();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount()!=2 || !SwingUtilities.isLeftMouseButton(e)) {
				return;
			}
			
			displaySelectedValue();
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			
			JComboBox<?> cb = (JComboBox<?>) e.getSource();			
			Object selectedItem = cb.getSelectedItem();
			
			if(selectedItem instanceof DocumentSetDescriptor) {
				displayData((DocumentSetDescriptor) selectedItem, null);
			} else {
				displaySelectedValue();
			}
		}

		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			updateModels();
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
		
		private void maybeClearOutline() {
			if(list.getModel().getSize()==0) {
				clearOutline();
			}
		}

		/**
		 * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void intervalAdded(ListDataEvent e) {
			// no-op
		}

		/**
		 * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void intervalRemoved(ListDataEvent e) {
			maybeClearOutline();
		}

		/**
		 * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
		 */
		@Override
		public void contentsChanged(ListDataEvent e) {
			maybeClearOutline();
		}

	}
	
	public class CallbackHandler {
		
		protected CallbackHandler() {
			// no-op
		}
		
		public void inspectDocument(ActionEvent e) {
			displaySelectedValue();
		}
		
		public void analyzeDocument(ActionEvent e) {
			analyzeSelectedValue();
		}
	}
}
