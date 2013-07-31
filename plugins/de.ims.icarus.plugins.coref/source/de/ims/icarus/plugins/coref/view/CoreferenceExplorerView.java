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
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.java.plugin.registry.Extension;

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.registry.AllocationDescriptor;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry.LoadJob;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.coref.CorefConstants;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.Updatable;
import de.ims.icarus.ui.dialog.ChoiceFormEntry;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.list.ListUtils;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.DataListModel;
import de.ims.icarus.util.location.Location;
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
	
	private DocumentSetListWrapper documentSetModel;
	private AllocationListWrapper allocationModel;
	private AllocationListWrapper goldAllocationModel;
	
	private static final Object dummyEntry = "-"; //$NON-NLS-1$
	
	private DocumentSetDescriptor descriptor;
	private AllocationDescriptor allocation;
	private AllocationDescriptor goldAllocation;
		
	private Handler handler;
	private CallbackHandler callbackHandler;
	
	private JPopupMenu popupMenu;
	
	private DefaultAllocationDescriptor defaultAllocationDescriptor;
	
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
		
		handler = new Handler();

		container.setLayout(new BorderLayout());
		
		FormBuilder formBuilder = FormBuilder.newLocalizingBuilder();
		ChoiceFormEntry entry;
		
		// Document-set selection
		documentSetModel = new DocumentSetListWrapper();
		entry = new ChoiceFormEntry( 
				"plugins.coref.labels.documentSet", documentSetModel, false); //$NON-NLS-1$
		entry.getComboBox().addActionListener(handler);
		formBuilder.addEntry("documentSet", entry); //$NON-NLS-1$
		// Allocation selection
		entry = new ChoiceFormEntry( 
				"plugins.coref.labels.allocation", allocationModel, false); //$NON-NLS-1$
		entry.getComboBox().addActionListener(handler);
		allocationModel = new AllocationListWrapper(false);
		formBuilder.addEntry("allocation", entry); //$NON-NLS-1$
		// Gold allocation selection
		entry = new ChoiceFormEntry( 
				"plugins.coref.labels.goldAllocation", goldAllocationModel, false); //$NON-NLS-1$
		entry.getComboBox().addActionListener(handler);
		goldAllocationModel = new AllocationListWrapper(true);
		formBuilder.addEntry("goldAllocation", entry); //$NON-NLS-1$
		
		formBuilder.buildForm();
		JComponent header = (JComponent) formBuilder.getContainer();
		header.setBorder(UIUtil.defaultContentBorder);
		
		JPanel headerPanel = new JPanel(new BorderLayout());
		// TODO create tool-bar
		headerPanel.add(header, BorderLayout.CENTER);
		
		listModel = new DataListModel<>();		
		list = new JList<>(listModel);
		list.setCellRenderer(new DocumentListCellRenderer());
		list.setBorder(UIUtil.defaultContentBorder);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(UIUtil.topLineBorder);
		
		container.add(headerPanel, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		
		registerActionCallbacks();
		refreshActions();
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
		allocationModel.setSelectedItem(getDefautlAllocationDescriptor());
		goldAllocationModel.setSelectedItem(dummyEntry);
		
		if(descriptor!=null && !descriptor.isLoaded()) {
			listModel.clear();
			
			final String name = descriptor.getName();
			
			String title = ResourceManager.getInstance().get(
					"plugins.coref.labels.loadingDocumentSet"); //$NON-NLS-1$
			Object task = new LoadJob(descriptor) {
				@Override
				protected void done() {
					try {
						get();
					} catch(CancellationException | InterruptedException e) {
						// ignore
					} catch(Exception e) {
						LoggerFactory.log(this, Level.SEVERE, 
								"Failed to load document-set: "+name, e); //$NON-NLS-1$
						
						UIUtil.beep();
						showError(e);
					} finally {
						update();
					}
				}				
			};
			TaskManager.getInstance().schedule(task, title, null, null, 
					TaskPriority.DEFAULT, true);
		} else {
			update();
		}
	}
	
	@Override
	public boolean update() {
		if(descriptor==null) {
			return false;
		}
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
			if(allocation!=null) {
				options.put("allocation", allocation.getAllocation());
			}
			
			Message message = new Message(this, Commands.PRESENT, document, options);
			sendRequest(CorefConstants.COREFERENCE_DOCUMENT_VIEW_ID, message);
		} catch(Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to forward presentation of document at index "+index, e); //$NON-NLS-1$
			
			UIUtil.beep();
			showError(e);
		}
	}
	
	private void forwardDocument(CoreferenceDocumentData document, Options options) {
		
	}
	
	private DefaultAllocationDescriptor getDefautlAllocationDescriptor() {
		if(defaultAllocationDescriptor==null) {
			synchronized (this) {
				if(defaultAllocationDescriptor==null) {
					defaultAllocationDescriptor = new DefaultAllocationDescriptor();
				}
			}
		}
		return defaultAllocationDescriptor;
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
		
	}
	
	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
	}
	
	private void showPopup(MouseEvent trigger) {
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
			popupMenu.show(list, trigger.getX(), trigger.getY());
		}
	}
	
	private void updateModels() {
		documentSetModel.update();
		allocationModel.update();
		goldAllocationModel.update();
	}

	private class Handler extends MouseAdapter 
			implements ListSelectionListener, ActionListener, EventListener {

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			displaySelectedValue();
		}
		
		private void maybeShowPopup(MouseEvent e) {
			if(e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			super.mouseClicked(e);
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
			displaySelectedValue();
		}

		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			updateModels();
		}
	}
	
	public class CallbackHandler {
		
		protected CallbackHandler() {
			// no-op
		}
	}
	
	private class DocumentSetListWrapper extends AbstractListModel<Object> 
			implements ComboBoxModel<Object>, Updatable {

		private static final long serialVersionUID = -8374082995792875575L;
		
		private final ListModel<DocumentSetDescriptor> base =
				CoreferenceRegistry.getInstance().getDocumentSetListModel();
		
		/**
		 * @see javax.swing.ListModel#getSize()
		 */
		@Override
		public int getSize() {
			return base.getSize()+1;
		}

		/**
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		@Override
		public Object getElementAt(int index) {
			return index==0 ? dummyEntry : base.getElementAt(index-1);
		}

		/**
		 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
		 */
		@Override
		public void setSelectedItem(Object anItem) {
			if(!(anItem instanceof DocumentSetDescriptor)) {
				anItem = null;
			}
			displayData((DocumentSetDescriptor) anItem, null);
			
			fireContentsChanged(this, -1, -1);
		}

		/**
		 * @see javax.swing.ComboBoxModel#getSelectedItem()
		 */
		@Override
		public Object getSelectedItem() {
			return descriptor;
		}
		
		/**
		 * @see de.ims.icarus.ui.Updatable#update()
		 */
		@Override
		public boolean update() {
			fireContentsChanged(this, 0, Math.max(0, getSize()-1));
			return true;
		}
	}
	
	private class AllocationListWrapper extends AbstractListModel<Object> 
			implements ComboBoxModel<Object>, Updatable {

		private static final long serialVersionUID = 5697820922840356053L;
		
		private final boolean gold;
		
		public AllocationListWrapper(boolean gold) {
			this.gold = gold;
		}
		
		private boolean isEmpty() {
			return descriptor==null || descriptor.size()==0;
		}
		
		/**
		 * @see javax.swing.ListModel#getSize()
		 */
		@Override
		public int getSize() {
			return isEmpty() ? 0 : descriptor.size() + 2;
		}

		/**
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		@Override
		public Object getElementAt(int index) {
			if(isEmpty()) {
				return null;
			}
			if(index==0) {
				return dummyEntry;
			} else if(index==1) {
				return getDefautlAllocationDescriptor();
			}
			return descriptor.get(index-1);
		}

		/**
		 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
		 */
		@Override
		public void setSelectedItem(Object anItem) {
			Object selectedObject = gold ? goldAllocation : allocation;
			
			if(anItem==dummyEntry) {
				anItem = null;
			}
			
			if((selectedObject!=null && !selectedObject.equals(anItem))
					|| (selectedObject==null && anItem!=null)) {
				
				if(gold) {
					goldAllocation = (AllocationDescriptor) anItem;
				} else {
					allocation = (AllocationDescriptor) anItem;
				}
				
				fireContentsChanged(this, -1, -1);
			}
		}

		/**
		 * @see javax.swing.ComboBoxModel#getSelectedItem()
		 */
		@Override
		public Object getSelectedItem() {
			return gold ? goldAllocation : allocation;
		}
		
		/**
		 * @see de.ims.icarus.ui.Updatable#update()
		 */
		@Override
		public boolean update() {
			fireContentsChanged(this, 0, Math.max(0, getSize()-1));
			return true;
		}
	}
	
	private class DefaultAllocationDescriptor extends AllocationDescriptor {
		
		private DefaultAllocationDescriptor() {
			super();
		}

		@Override
		public String getName() {
			String name = "Default Allocation"; //$NON-NLS-1$
			CoreferenceAllocation allocation = getAllocation();
			if(allocation==null || allocation.size()==0) {
				name += " (empty)"; //$NON-NLS-1$
			}
			return name;
		}

		@Override
		public CoreferenceAllocation getAllocation() {
			if(descriptor==null) {
				return null;
			}
			CoreferenceDocumentSet documentSet = descriptor.getDocumentSet();
			if(documentSet==null) {
				return null;
			}
			return documentSet.getDefaultAllocation();
		}

		@Override
		public boolean isLoaded() {
			return true;
		}

		@Override
		public boolean isLoading() {
			return false;
		}

		@Override
		public void load() throws Exception {
			// no-op
		}

		@Override
		public Location getLocation() {
			return null;
		}

		@Override
		public Extension getReaderExtension() {
			return null;
		}

		@Override
		public void free() {
			// no-op
		}
	}
}
