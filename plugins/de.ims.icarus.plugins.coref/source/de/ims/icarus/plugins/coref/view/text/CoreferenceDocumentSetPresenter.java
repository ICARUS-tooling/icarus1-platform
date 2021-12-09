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
package de.ims.icarus.plugins.coref.view.text;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.DocumentData;
import de.ims.icarus.language.coref.DocumentSet;
import de.ims.icarus.language.coref.text.CoreferenceDocument;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.coref.view.DocumentListCellRenderer;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionComponentBuilder;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.DataListModel;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentSetPresenter extends
		AbstractCoreferenceTextPresenter {

	protected DocumentSet data;
	protected JList<?> documentList;
	protected DataListModel<DocumentData> documentListModel = new DataListModel<>();
	
	private boolean showDocumentFilter = true;
	
	protected JSplitPane splitPane;
	
	protected JPopupMenu documentFilterPopupMenu;
		
	@Override
	protected void registerActionCallbacks() {
		super.registerActionCallbacks();
		
		ActionManager actionManager = getActionManager();
		
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.toggleDocumentFilterAction",  //$NON-NLS-1$
				callbackHandler, "toggleDocumentFilter"); //$NON-NLS-1$
	}

	@Override
	protected void refreshActions() {
		super.refreshActions();
		
		ActionManager actionManager = getActionManager();
		
		actionManager.setSelected(showDocumentFilter, 
				"plugins.coref.coreferenceDocumentPresenter.toggleDocumentFilterAction"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return data!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public DocumentSet getPresentedData() {
		return data;
	}

	/**
	 * @see de.ims.icarus.plugins.coref.view.text.AbstractCoreferenceTextPresenter#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceDocumentSetContentType();
	}

	/**
	 * @see de.ims.icarus.plugins.coref.view.text.AbstractCoreferenceTextPresenter#setData(java.lang.Object)
	 */
	@Override
	protected void setData(Object data) {
		this.data = (DocumentSet) data;
		
		documentListModel.setDataList(this.data);
		
		if(documentList!=null) {
			documentList.setSelectedIndex(0);
		}
	}

	/**
	 * @see de.ims.icarus.plugins.coref.view.text.AbstractCoreferenceTextPresenter#createToolBar()
	 */
	@Override
	protected ActionComponentBuilder createToolBar() {
		ActionComponentBuilder builder = super.createToolBar();
		
		builder.setActionListId("plugins.coref.coreferenceDocumentPresenter.extendedToolBarList"); //$NON-NLS-1$
		
		return builder;
	}

	@Override
	protected Handler createHandler() {
		return new CDSHandler();
	}
	
	@Override
	protected CDSHandler getHandler() {
		return (CDSHandler) super.getHandler();
	}

	@Override
	protected CallbackHandler createCallbackHandler() {
		return new CDSCallbackHandler();
	}

	@Override
	protected JComponent createContentPanel() {
		JPanel panel = new JPanel(new BorderLayout());
				
		textPane = createTextPane();
		
		JScrollPane spRight = new JScrollPane(textPane);
		spRight.getViewport().addChangeListener(getHandler());
		UIUtil.defaultSetUnitIncrement(spRight);
		spRight.setBorder(null);

		documentList = new JList<>(documentListModel);
		documentList.setCellRenderer(new DocumentListCellRenderer());
		documentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		documentList.setSelectedIndex(0);
		documentList.addListSelectionListener(getHandler());

		JScrollPane spLeft = new JScrollPane(documentList);
		UIUtil.defaultSetUnitIncrement(spLeft);
		spLeft.setBorder(null);
		spLeft.setVisible(showDocumentFilter);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, spLeft, spRight);
		splitPane.setResizeWeight(0);
		splitPane.setBorder(UIUtil.topLineBorder);
		splitPane.setDividerLocation(200);
		splitPane.setDividerSize(5);
		panel.add(splitPane, BorderLayout.CENTER);
		
		JToolBar toolBar = createToolBar().buildToolBar();
		if(toolBar!=null) {
			panel.add(toolBar, BorderLayout.NORTH);
		}
		
		return panel;
	}

	@Override
	protected boolean buildDocument(CoreferenceDocument doc) throws Exception {	
		if(data==null) {
			return false;
		}
		
		int index = documentList==null ? -1 : documentList.getSelectedIndex(); 
		if(index==-1) {
			return false;
		}
		
		DocumentData docData = documentListModel.getElementAt(index);
		doc.appendBatchDocumentData(docData, getAllocation(), getGoldAllocation());
		
		doc.applyBatchUpdates(0);
		
		return true;
	}
	
	protected void showDocumentFilterPopup(MouseEvent trigger) {
		if(contentPanel==null) {
			return;
		}
		
		if(documentFilterPopupMenu==null) {
			// Create new popup menu
			
			Options options = new Options();
			documentFilterPopupMenu = getActionManager().createPopupMenu(
					"plugins.coref.coreferenceDocumentPresenter.documentFilterPopupMenuList", options); //$NON-NLS-1$
			
			if(documentFilterPopupMenu!=null) {
				documentFilterPopupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create document-filter popup menu"); //$NON-NLS-1$
			}
		}
		
		if(documentFilterPopupMenu!=null) {
			refreshActions();
			
			documentFilterPopupMenu.show(documentList, trigger.getX(), trigger.getY());
		}
	}
	
	public boolean isShowDocumentFilter() {
		return showDocumentFilter;
	}

	public void setShowDocumentFilter(boolean showDocumentFilter) {
		if(this.showDocumentFilter==showDocumentFilter) {
			return;
		}
		
		this.showDocumentFilter = showDocumentFilter;
		
		if(documentList==null) {
			return;
		}

		JScrollPane container = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, textPane);
		if(showDocumentFilter) {
			container.setBorder(null);
			splitPane.setRightComponent(container);
			splitPane.setDividerLocation(200);
			contentPanel.add(splitPane, BorderLayout.CENTER);
		} else {
			container.setBorder(UIUtil.topLineBorder);
			contentPanel.remove(splitPane);
			contentPanel.add(splitPane.getRightComponent(), BorderLayout.CENTER);
		}
		
		contentPanel.revalidate();
		contentPanel.repaint();
	}

	protected class CDSHandler extends Handler implements ListSelectionListener {

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			refresh();
		}
	}
	
	public class CDSCallbackHandler extends CallbackHandler {
		protected CDSCallbackHandler() {
			// no-op
		}

		public void toggleDocumentFilter(ActionEvent e) {
			// ignore
		}

		public void toggleDocumentFilter(boolean b) {
			try {
				setShowDocumentFilter(b);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'filterDocuments' flag", e); //$NON-NLS-1$
			}
		}
	}
}
