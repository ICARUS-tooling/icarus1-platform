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
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.java.plugin.registry.Extension;

import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.ExtensionListModel;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.coref.CoreferencePlugin;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentView extends View {


	private AWTPresenter presenter;
	
	private Map<Extension, AWTPresenter> presenterInstances;
	
	private Handler handler;
	private CallbackHandler callbackHandler;
	
	private JPanel contentPanel;
	private JTextArea infoLabel;

	public CoreferenceDocumentView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// Load actions
		if(!defaultLoadActions(CoreferenceDocumentView.class, 
				"coreference-document-view-actions.xml")) { //$NON-NLS-1$
			return;
		}
		
		handler = new Handler();
		
		container.setLayout(new BorderLayout());
		
		contentPanel = new JPanel(new BorderLayout());
		
		infoLabel = UIUtil.defaultCreateInfoLabel(container);
		
		JComboBox<Extension> comboBox = new JComboBox<>(
				new ExtensionListModel(CoreferencePlugin.getCoreferencePresenterExtensions(), true));
		comboBox.setEditable(false);
		comboBox.setRenderer(ExtensionListCellRenderer.getSharedInstance());
		UIUtil.fitToContent(comboBox, 80, 150, 22);
		comboBox.addActionListener(handler);
		
		Options options = new Options();
		options.put("selectPresenter", comboBox); //$NON-NLS-1$
		
		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"plugins.coref.coreferenceDocumentView.toolBarList", options); //$NON-NLS-1$
		
		container.add(toolBar, BorderLayout.NORTH);
		container.add(contentPanel, BorderLayout.CENTER);
		
		registerActionCallbacks();
		refreshActions();
		
		showInfo(null);
	}
	
	private void showInfo(String text) {
		
	}
	
	private void registerActionCallbacks() {
		
	}
	
	private void refreshActions() {
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		super.close();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		super.reset();
	}
	
	private void displayDocument(CoreferenceDocumentData document, Options options) {
		
	}

	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.PRESENT.equals(message.getCommand())
				|| Commands.DISPLAY.equals(message.getCommand())) {
			Object data = message.getData();
			if(data instanceof CoreferenceDocumentData) {
				displayDocument((CoreferenceDocumentData) data, message.getOptions());
				return message.successResult(this, null);
			} else {
				return message.unsupportedDataResult(this);
			}
		} else if(Commands.CLEAR.equals(message.getCommand())) {
			reset();
			return message.successResult(this, null);
		} else {
			return message.unknownRequestResult(this);
		}
	}
	
	public class CallbackHandler {
		private CallbackHandler() {
			// no-op
		}
		
		public void clearView(ActionEvent e) {
			
		}
		
		public void refreshView(ActionEvent e) {
			
		}
	}
	
	private class Handler implements ActionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
}