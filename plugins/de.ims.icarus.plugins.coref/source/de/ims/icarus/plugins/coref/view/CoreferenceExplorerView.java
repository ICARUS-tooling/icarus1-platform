/*
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.ui.UIUtil;
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
public class CoreferenceExplorerView extends View {
	
	protected JList<CoreferenceDocumentData> list;
	protected DataListModel<CoreferenceDocumentData> listModel;
	
	protected CoreferenceDocumentSet documentSet;
	
	protected Handler handler;

	public CoreferenceExplorerView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		handler = new Handler();
		
		listModel = new DataListModel<>();		
		list = new JList<>(listModel);
		list.setCellRenderer(new DocumentListCellRenderer());
		list.setBorder(UIUtil.defaultContentBorder);
		
		// TODO create tool-bar
		
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(UIUtil.emptyBorder);
		
		container.setLayout(new BorderLayout());
		container.add(scrollPane, BorderLayout.CENTER);
	}

	protected void displayData(CoreferenceDocumentSet documentSet, Options options) {
		listModel.setDataList(documentSet);
	}
	
	protected void displaySelectedValue() {
		
	}
	
	@Override
	public void reset() {
		listModel.clear();
	}

	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.SELECT.equals(message.getCommand())) {
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

	protected class Handler extends MouseAdapter implements ListSelectionListener {

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			displaySelectedValue();
		}
		
		protected void maybeShowPopup(MouseEvent e) {
			if(e.isPopupTrigger()) {
				// TODO show popup
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
	}
}
