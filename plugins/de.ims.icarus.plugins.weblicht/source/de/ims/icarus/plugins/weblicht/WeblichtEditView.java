package de.ims.icarus.plugins.weblicht;

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

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.plugins.weblicht.webservice.Webchain;
import de.ims.icarus.plugins.weblicht.webservice.WebchainRegistry;
import de.ims.icarus.plugins.weblicht.webservice.WebserviceRegistry;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.ui.helper.UIHelperRegistry;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;


/**
 * 
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WeblichtEditView extends View {

	private Editor<Webchain> editor;

	private JLabel header;
	private JLabel infoLabel;

	private JScrollPane scrollPane;

	private Handler handler;
	private CallbackHandler callbackHandler;


	/**
	 * 
	 */
	public WeblichtEditView() {
		// no-op
	}

	@Override
	public void init(JComponent container) {

		// Load actions
		URL actionLocation = WeblichtEditView.class
				.getResource("weblicht-edit-view-actions.xml"); //$NON-NLS-1$
		if (actionLocation == null)
			throw new CorruptedStateException(
					"Missing resources: weblicht-edit-view-actions.xml"); //$NON-NLS-1$

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
						"plugins.weblicht.weblichtEditView.notAvailable", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);

		// Footer area
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		footer.setBorder(new EmptyBorder(5, 20, 5, 20));
		footer.add(new JButton(actionManager.getAction(
				"plugins.weblicht.weblichtEditView.resetEditAction"))); //$NON-NLS-1$
		footer.add(new JButton(actionManager.getAction(
				"plugins.weblicht.weblichtEditView.applyEditAction"))); //$NON-NLS-1$	

		// Description Scrollpane
		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);

		container.setLayout(new BorderLayout());
		container.add(header, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		container.add(footer, BorderLayout.SOUTH);

		showDefaultInfo();

		WebchainRegistry.getInstance().addListener(Events.REMOVED, handler);
		
		registerActionCallbacks();	
		refreshActions();
	}

	private void showDefaultInfo() {
		scrollPane.setViewportView(infoLabel);
		header.setText(""); //$NON-NLS-1$
	}

	private void refreshActions() {
		ActionManager actionManager = getDefaultActionManager();
		// add weblicht service check.. get weblicht,

		actionManager.setEnabled(getWebchain() != null,
				"plugins.weblicht.weblichtEditView.resetEditAction", //$NON-NLS-1$
				"plugins.weblicht.weblichtEditView.applyEditAction"); //$NON-NLS-1$

	}

	private Webchain getWebchain() {
		return editor == null ? null : editor.getEditingItem();
	}

	private Editor<Webchain> getEditor() {
		return editor;
	}

	@Override
	public void close() {
		WebchainRegistry.getInstance().removeListener(handler);
		
		Editor<Webchain> editor = this.editor;
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
		
		Webchain webchain = getWebchain();
		if(webchain==null) {
			return true;
		}
		
		// Let user decide whether to discard unsaved changes
		return DialogFactory.getGlobalFactory().showConfirm(null, 
				"plugins.weblicht.weblichtEditView.dialogs.discardChanges.title",  //$NON-NLS-1$
				"plugins.weblicht.weblichtEditView.dialogs.discardChanges.message",  //$NON-NLS-1$
				webchain.getName());
	}

	@Override
	public void reset() {
		editWebchain(null);
		refreshActions();
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void editWebchain(Webchain webchain) {
		if(webchain!=null) {
			selectViewTab();
		}
		
		Webchain oldWebchain = getWebchain();
		if(oldWebchain==webchain) {
			return;
		}
		
		// Offer chance to save changes 
		if(oldWebchain!=null && editor!=null && editor.hasChanges()) {
			// Let user decide whether to discard unsaved changes
			if(DialogFactory.getGlobalFactory().showConfirm(null, 
					"plugins.weblicht.weblichtEditView.dialogs.saveChanges.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtEditView.dialogs.saveChanges.message",  //$NON-NLS-1$
					oldWebchain.getName())) {
				
				try {
					editor.applyEdit();
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to apply edit: "+getWebchain(), ex); //$NON-NLS-1$
				}
			}
		}
		
		getContainer().remove(infoLabel);
		
		if(webchain==null) {
			showDefaultInfo();
			return;
		}
		
		// If the new Webchain is of the same type just use
		// the present editor!
		if(oldWebchain!=null && oldWebchain.getClass().equals(
				webchain.getClass()) && editor!=null) {
			editor.setEditingItem(webchain);
			header.setText(webchain.getName());
			return;
		}
		
		// Try to fetch an editor for the supplied webchain
		Editor<Webchain> editor = null;
		ContentType contentType = ContentTypeRegistry.getInstance().getEnclosingType(webchain);
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

		this.editor = editor;		
		
		editor.setEditingItem(webchain);
		scrollPane.setViewportView(editor.getEditorComponent());
		header.setText(webchain.getName());

	}
	

	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		
		if(Commands.EDIT.equals(message.getCommand())) {
			Object data = message.getData();
			// We allow null values since this is a way to clear the editor view
			if(data==null || data instanceof Webchain) {
				Webchain chain =(Webchain) data;
				editWebchain(chain);
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
				"plugins.weblicht.weblichtEditView.resetEditAction", //$NON-NLS-1$
				callbackHandler, "resetEdit"); //$NON-NLS-1$

		actionManager.addHandler(
				"plugins.weblicht.weblichtEditView.applyEditAction", //$NON-NLS-1$
				callbackHandler, "applyEdit"); //$NON-NLS-1$
	}
	
	
	private class Handler implements EventListener {

		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			if(getWebchain()==null) {
				return;
			}
			
			if(sender==WebserviceRegistry.getInstance()) {
				Webchain webchain = (Webchain) event.getProperty("webchain"); //$NON-NLS-1$

				// Handle deleted Webservice							
				if(webchain==getWebchain()) {
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
			Editor<Webchain> editor = getEditor();
			if (editor == null) {
				return;
			}

			try {
				editor.resetEdit();
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to reset editor: " + getWebchain(), ex); //$NON-NLS-1$
				UIUtil.beep();
				
				showError(ex);
			}
		}

		public void applyEdit(ActionEvent e) {			
			Editor<Webchain> editor = getEditor();

			if (editor == null) {
				return;
			}

			try {
				editor.applyEdit();				
				//TODO
				//WebserviceRegistry.getInstance().webserviceChanged(editor.getEditingItem());
			} catch (Exception ex) {
				LoggerFactory.log(this, Level.SEVERE,
						"Failed to apply edit: " + getWebchain(), ex); //$NON-NLS-1$
				UIUtil.beep();
				
				showError(ex);
			}
			
			header.setText(getWebchain().getName());
		}
	}

}
