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
package de.ims.icarus.plugins.language_tools.treebank;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.ims.icarus.language.treebank.Treebank;
import de.ims.icarus.language.treebank.TreebankRegistry;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.ui.helper.UIHelperRegistry;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankEditView extends View {
	
	private Editor<Treebank> editor;
	private JLabel header;
	private JLabel infoLabel;
	
	private JScrollPane scrollPane;
	
	private Handler handler;
	private CallbackHandler callbackHandler;

	/**
	 * 
	 */
	public TreebankEditView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// Load actions
		if(!defaultLoadActions(TreebankEditView.class, "treebank-edit-view-actions.xml")) { //$NON-NLS-1$
			return;
		}
		
		handler = new Handler();
		
		// Header label
		header = new JLabel(""); //$NON-NLS-1$
		UIUtil.disableHtml(header);
		header.setBorder(new EmptyBorder(3, 5, 10, 20));
		header.setFont(header.getFont().deriveFont(header.getFont().getSize2D()+2));
		
		// Info label
		infoLabel = new JLabel();
		infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		infoLabel.setVerticalAlignment(SwingConstants.TOP);
		ResourceManager.getInstance().getGlobalDomain().prepareComponent(
				infoLabel, "plugins.languageTools.treebankEditView.notAvailable", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);
		
		// Footer area
		ActionManager actionManager = getDefaultActionManager();
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		footer.setBorder(new EmptyBorder(5, 20, 5, 20));
		footer.add(new JButton(actionManager.getAction(
				"plugins.languageTools.treebankEditView.resetEditAction"))); //$NON-NLS-1$
		footer.add(new JButton(actionManager.getAction(
				"plugins.languageTools.treebankEditView.applyEditAction"))); //$NON-NLS-1$
		
		// Content area
		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		
		container.setLayout(new BorderLayout());
		container.add(header, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		container.add(footer, BorderLayout.SOUTH);
		
		showDefaultInfo();
		
		TreebankRegistry.getInstance().addListener(Events.REMOVED, handler);
		TreebankRegistry.getInstance().addListener(Events.CHANGED, handler);

		registerActionCallbacks();
		refreshActions();
	}
	
	private Treebank getTreebank() {
		return editor==null ? null : editor.getEditingItem();
	}
	
	private Editor<Treebank> getEditor() {
		return editor;
	}
	
	private void showDefaultInfo() {
		scrollPane.setViewportView(infoLabel);
		header.setText(""); //$NON-NLS-1$
		
		// Close any active editor and discard its reference
		// This is required to prevent "old" treebanks to block
		// refresh operations
		if(editor!=null) {
			editor.close();
			editor = null;
		}
	}
	
	private void refreshActions() {
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.setEnabled(getTreebank()!=null, 
				"plugins.languageTools.treebankEditView.resetEditAction",  //$NON-NLS-1$
				"plugins.languageTools.treebankEditView.applyEditAction"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#close()
	 */
	@Override
	public void close() {
		
		TreebankRegistry.getInstance().removeListener(handler);
		
		Editor<Treebank> editor = this.editor;
		if(editor!=null) {
			editor.close();
		}

		this.editor = null;
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#isClosable()
	 */
	@Override
	public boolean isClosable() {
		if(editor==null) {
			return true;
		}
		if(!editor.hasChanges()) {
			return true;
		}
		
		Treebank treebank = getTreebank();
		if(treebank==null) {
			return true;
		}
		
		// Let user decide whether to discard unsaved changes
		return DialogFactory.getGlobalFactory().showConfirm(null, 
				"plugins.languageTools.treebankEditView.dialogs.discardChanges.title",  //$NON-NLS-1$
				"plugins.languageTools.treebankEditView.dialogs.discardChanges.message",  //$NON-NLS-1$
				treebank.getName());
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		editTreebank(null);
		refreshActions();
	}
	
	@SuppressWarnings("unchecked")
	private void editTreebank(Treebank treebank) {
		if(treebank!=null) {
			selectViewTab();
		}
		
		Treebank oldTreebank = getTreebank();
		if(oldTreebank==treebank) {
			return;
		}
		
		// Offer chance to save changes 
		if(oldTreebank!=null && editor!=null && editor.hasChanges()) {
			// Let user decide whether to discard unsaved changes
			if(DialogFactory.getGlobalFactory().showConfirm(null, 
					"plugins.languageTools.treebankEditView.dialogs.saveChanges.title",  //$NON-NLS-1$
					"plugins.languageTools.treebankEditView.dialogs.saveChanges.message",  //$NON-NLS-1$
					oldTreebank.getName())) {
				
				try {
					editor.applyEdit();
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to apply edit: "+getTreebank(), ex); //$NON-NLS-1$
				}
			}
		}
		
		getContainer().remove(infoLabel);
		
		if(treebank==null) {
			showDefaultInfo();
			return;
		}
		
		// If the new treebank is of the same type just use
		// the present editor!
		if(oldTreebank!=null && oldTreebank.getClass().equals(
				treebank.getClass()) && editor!=null) {
			editor.setEditingItem(treebank);
			header.setText(treebank.getName());
			return;
		}
		
		// Try to fetch an editor for the supplied treebank
		Editor<Treebank> editor = UIHelperRegistry.globalRegistry().findHelper(
				Editor.class, treebank);
		
		if(editor==null) {
			showDefaultInfo();
			return;
		}
		
		if(this.editor!=null) {
			this.editor.close();
		}

		this.editor = editor;
		
		editor.setEditingItem(treebank);
		scrollPane.setViewportView(editor.getEditorComponent());
		header.setText(treebank.getName());
	}

	/**
	 * Accepted commands:
	 * <ul>
	 * <li>{@link Commands#EDIT}</li>
	 * <li>{@link Commands#CLEAR}</li>
	 * </ul>
	 * 
	 * @see de.ims.icarus.plugins.core.View#handleRequest(de.ims.icarus.util.mpi.Message)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.EDIT.equals(message.getCommand())) {
			Object data = message.getData();
			
			// We allow null values since this is a way to clear the editor view
			if(data==null || data instanceof Treebank) {
				editTreebank((Treebank) data);
				refreshActions();
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

	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.addHandler("plugins.languageTools.treebankEditView.resetEditAction",  //$NON-NLS-1$
				callbackHandler, "resetEdit"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.treebankEditView.applyEditAction",  //$NON-NLS-1$
				callbackHandler, "applyEdit"); //$NON-NLS-1$
	}

	private class Handler implements EventListener {

		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			if(getTreebank()==null) {
				return;
			}
			
			if(sender==TreebankRegistry.getInstance()) {
				Treebank treebank = (Treebank) event.getProperty("treebank"); //$NON-NLS-1$

				if(treebank!=getTreebank()) {
					return;
				}
				
				// Handle deleted treebanks								
				if(Events.REMOVED.equals(event.getName())) {
					reset();
					return;
				}
				
				// Handle changes so we can display latest name								
				if(Events.CHANGED.equals(event.getName())) {
					header.setText(treebank.getName());
					return;
				}
			}
		}
	}
	
	public final class CallbackHandler {
		
		private CallbackHandler() {
			// no-op
		}
		
		public void resetEdit(ActionEvent e) {
			Editor<Treebank> editor = getEditor();
			if(editor==null) {
				return;
			}
			
			try {
				editor.resetEdit();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to reset editor: "+getTreebank(), ex); //$NON-NLS-1$
				UIUtil.beep();
				
				showError(ex);
			}
		}
		
		public void applyEdit(ActionEvent e) {
			Editor<Treebank> editor = getEditor();
			if(editor==null) {
				return;
			}
			
			try {
				editor.applyEdit();
				
				TreebankRegistry.getInstance().treebankChanged(editor.getEditingItem());
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to apply edit: "+getTreebank(), ex); //$NON-NLS-1$
				UIUtil.beep();
				
				showError(ex);
			}
			
			header.setText(getTreebank().getName());
		}
	}
}
