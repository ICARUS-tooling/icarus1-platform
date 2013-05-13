/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.weblicht;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.Webservice;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebserviceEditor;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebserviceRegistry;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.ui.helper.Editor;
import net.ikarus_systems.icarus.ui.helper.UIHelperRegistry;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;
import net.ikarus_systems.icarus.util.mpi.ResultMessage;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebserviceEditView extends View {
	//private Editor<Webservice> editor;
	private WebserviceEditor editor;

	private JLabel header;
	private JLabel infoLabel;

	private JScrollPane scrollPane;

	private Handler handler;
	private CallbackHandler callbackHandler;
	
	boolean editable;
	
	/**
	 * 
	 */
	public WebserviceEditView() {
		// no-op
	}

	@Override
	public void init(JComponent container) {

		// Load actions
		URL actionLocation = WebserviceEditView.class
				.getResource("webservice-edit-view-actions.xml"); //$NON-NLS-1$
		if (actionLocation == null)
			throw new CorruptedStateException(
					"webservice-edit-view-actions.xml"); //$NON-NLS-1$

		ActionManager actionManager = getDefaultActionManager();
		try {
			actionManager.loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}
		
		handler = new Handler();

		// Header label
		header = new JLabel(""); //$NON-NLS-1$
		header.setBorder(new EmptyBorder(3, 5, 10, 20));
		header.setFont(header.getFont().deriveFont(
				header.getFont().getSize2D() + 2));

		// Info label
		infoLabel = new JLabel();
		infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		infoLabel.setVerticalAlignment(SwingConstants.TOP);
		ResourceManager
				.getInstance()
				.getGlobalDomain()
				.prepareComponent(infoLabel,
						"plugins.weblicht.webserviceEditView.notAvailable", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);

		// Footer area
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		footer.setBorder(new EmptyBorder(5, 20, 5, 20));
		footer.add(new JButton(actionManager.getAction(
				"plugins.weblicht.webserviceEditView.resetEditAction"))); //$NON-NLS-1$
		footer.add(new JButton(actionManager.getAction(
				"plugins.weblicht.webserviceEditView.applyEditAction"))); //$NON-NLS-1$		

		// Description Scrollpane
		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);

		container.setLayout(new BorderLayout());
		container.add(header, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		container.add(footer, BorderLayout.SOUTH);

		showDefaultInfo();

		WebserviceRegistry.getInstance().addListener(Events.REMOVED, handler);
		
		registerActionCallbacks();	
		refreshActions();
	}

	private void showDefaultInfo() {
		scrollPane.setViewportView(infoLabel);
		header.setText(""); //$NON-NLS-1$
		
		// Close any active editor and discard its reference
		// This is required to prevent "old" webservice to block
		// refresh operations
		if(editor!=null) {
			editor.close();
			editor = null;
		}
	}

	private void refreshActions() {
		ActionManager actionManager = getDefaultActionManager();
		// add webservice service check.. get webservice,

		actionManager.setEnabled(getWebservice() != null,
				"plugins.weblicht.webserviceEditView.resetEditAction", //$NON-NLS-1$
				"plugins.weblicht.webserviceEditView.applyEditAction"); //$NON-NLS-1$

	}

	private Webservice getWebservice() {
		return editor == null ? null : editor.getEditingItem();
	}

	private Editor<Webservice> getEditor() {
		return editor;
	}

	@Override
	public void close() {
		WebserviceRegistry.getInstance().removeListener(handler);
		
		Editor<Webservice> editor = this.editor;
		if(editor!=null) {
			editor.close();
		}		
		this.editor = null;

	}

	@Override
	public boolean isClosable() {
		if(editor==null) {
			return true;
		}
		if(!editor.hasChanges()) {
			return true;
		}
		
		Webservice webservice = getWebservice();
		if(webservice==null) {
			return true;
		}
		
		// Let user decide whether to discard unsaved changes
		return DialogFactory.getGlobalFactory().showConfirm(null, 
				"plugins.weblicht.webserviceEditView.dialogs.discardChanges.title",  //$NON-NLS-1$
				"plugins.weblicht.webserviceEditView.dialogs.discardChanges.message",  //$NON-NLS-1$
				webservice.getName());
	}

	@Override
	public void reset() {
		editWebservice(null);
		refreshActions();
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void editWebservice(Webservice webservice) {
		
		if(webservice!=null) {
			selectViewTab();
		}
		
		Webservice oldWebservice = getWebservice();
		// oldservice may be the same, accesstype may changed repaint to avoid inconsistency
		if(oldWebservice==webservice) {			
			editor.setAccessType(editable);
			editor.setEditingItem(webservice);
			return;
		}	
		
		
		// Offer chance to save changes 
		if(oldWebservice!=null && editor!=null && editor.hasChanges()) {
			// Let user decide whether to discard unsaved changes
			if(DialogFactory.getGlobalFactory().showConfirm(null, 
					"plugins.weblicht.webserviceEditView.dialogs.saveChanges.title",  //$NON-NLS-1$
					"plugins.weblicht.webserviceEditView.dialogs.saveChanges.message",  //$NON-NLS-1$
					oldWebservice.getName())) {
				
				try {
					editor.applyEdit();
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to apply edit: "+getWebservice(), ex); //$NON-NLS-1$
				}
			}
		}
		
		getContainer().remove(infoLabel);
		
		if(webservice==null) {
			showDefaultInfo();
			return;
		}
		
		// If the new Webservice is of the same type just use
		// the present editor!
		if(oldWebservice!=null && oldWebservice.getClass().equals(
				webservice.getClass()) && editor!=null) {
			editor.setAccessType(editable);
			editor.setEditingItem(webservice);
			header.setText(webservice.getName());			
			return;
		}

		// Try to fetch an editor for the supplied Webservice
		Editor<Webservice> editor = null;
		ContentType contentType = ContentTypeRegistry.getInstance().getEnclosingType(webservice);
		if(contentType!=null) {
			editor = UIHelperRegistry.globalRegistry().findHelper(
					Editor.class, contentType);
		}

		if(editor==null) {
			showDefaultInfo();
			return;
		}
		
		if(this.editor!=null) {
			this.editor.close();
		}

		this.editor = (WebserviceEditor) editor;
		this.editor.setAccessType(editable);
		editor.setEditingItem(webservice);

		
		scrollPane.setViewportView(editor.getEditorComponent());
		header.setText(webservice.getName());

	}
	

	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		
		if(Commands.EDIT.equals(message.getCommand())) {
			
			Object data = message.getData();
			//enable edit
			editable = true;			
			if (!(editor==null)) editor.setAccessType(editable);
			// We allow null values since this is a way to clear the editor view
			if(data==null || data instanceof Webservice) {
				editWebservice((Webservice) data);
				refreshActions();				
				return message.successResult(this, null);				
			} else {
				return message.unsupportedDataResult(this);
			}
			
		}
		if(Commands.DISPLAY.equals(message.getCommand())) {			
			
			Object data = message.getData();
			//only display
			editable = false;
			if (!(editor==null)) editor.setAccessType(editable);
			// We allow null values since this is a way to clear the editor view
			if(data==null || data instanceof Webservice) {
				editWebservice((Webservice) data);
				refreshActions();
				return message.successResult(this, null);				
			} else {
				return message.unsupportedDataResult(this);
			}
			
		}else {
			return message.unknownRequestResult(this);
		}
	}
	

	private void registerActionCallbacks() {
		if (callbackHandler == null) {
			callbackHandler = new CallbackHandler();
		}

		ActionManager actionManager = getDefaultActionManager();

		actionManager.addHandler(
				"plugins.weblicht.webserviceEditView.resetEditAction", //$NON-NLS-1$
				callbackHandler, "resetEdit"); //$NON-NLS-1$

		actionManager.addHandler(
				"plugins.weblicht.webserviceEditView.applyEditAction", //$NON-NLS-1$
				callbackHandler, "applyEdit"); //$NON-NLS-1$
	}
	
	
	private class Handler implements EventListener {

		/**
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			if(getWebservice()==null) {
				return;
			}
			
			if(sender==WebserviceRegistry.getInstance()) {
				Webservice webservice = (Webservice) event.getProperty("webservice"); //$NON-NLS-1$

				// Handle deleted Webservice							
				if(webservice==getWebservice()) {
					reset();
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
			Editor<Webservice> editor = getEditor();
			if (editor == null) {
				return;
			}

			try {
				editor.resetEdit();
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to reset editor: " + getWebservice(), ex); //$NON-NLS-1$
			}
		}

		public void applyEdit(ActionEvent e) {
			Editor<Webservice> editor = getEditor();
			if (editor == null) {
				return;
			}

			try {
				editor.applyEdit();
				//WebserviceRegistry.getInstance().webserviceChanged(editor.getEditingItem());
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to apply edit: " + getWebservice(), ex); //$NON-NLS-1$
			}
			
			header.setText(getWebservice().getName());
		}
	}

}
