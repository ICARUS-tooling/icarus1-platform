/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools.corpus;

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

import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.language.corpus.CorpusRegistry;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.core.View;
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
import net.ikarus_systems.icarus.util.opi.Commands;
import net.ikarus_systems.icarus.util.opi.Message;
import net.ikarus_systems.icarus.util.opi.ResultMessage;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusEditView extends View {
	
	private Editor<Corpus> editor;
	private JLabel header;
	private JLabel infoLabel;
	
	private JScrollPane scrollPane;
	
	private Handler handler;
	private CallbackHandler callbackHandler;

	/**
	 * 
	 */
	public CorpusEditView() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// Load actions
		URL actionLocation = CorpusEditView.class.getResource("corpus-edit-view-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: corpus-edit-view-actions.xml"); //$NON-NLS-1$
		
		ActionManager actionManager = getDefaultActionManager();
		try {
			actionManager.loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.getLogger(CorpusEditView.class).log(LoggerFactory.record(Level.SEVERE, 
					"Failed to load actions from file", e)); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}
		
		handler = new Handler();
		
		// Header label
		header = new JLabel(""); //$NON-NLS-1$
		header.setBorder(new EmptyBorder(3, 5, 10, 20));
		header.setFont(header.getFont().deriveFont(header.getFont().getSize2D()+2));
		
		// Info label
		infoLabel = new JLabel();
		infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		infoLabel.setVerticalAlignment(SwingConstants.TOP);
		ResourceManager.getInstance().getGlobalDomain().prepareComponent(
				infoLabel, "plugins.languageTools.corpusEditView.notAvailable", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);
		
		// Footer area
		JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		footer.setBorder(new EmptyBorder(5, 20, 5, 20));
		footer.add(new JButton(actionManager.getAction(
				"plugins.languageTools.corpusEditView.resetEditAction"))); //$NON-NLS-1$
		footer.add(new JButton(actionManager.getAction(
				"plugins.languageTools.corpusEditView.applyEditAction"))); //$NON-NLS-1$
		
		// Content area
		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		
		container.setLayout(new BorderLayout());
		container.add(header, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		container.add(footer, BorderLayout.SOUTH);
		
		showDefaultInfo();
		
		CorpusRegistry.getInstance().addListener(Events.REMOVED, handler);

		registerActionCallbacks();
		refreshActions();
	}
	
	private Corpus getCorpus() {
		return editor==null ? null : editor.getEditingItem();
	}
	
	private Editor<Corpus> getEditor() {
		return editor;
	}
	
	private void showDefaultInfo() {
		scrollPane.setViewportView(infoLabel);
		header.setText(""); //$NON-NLS-1$
		
		// Close any active editor and discard its reference
		// This is required to prevent "old" corpora to block
		// refresh operations
		if(editor!=null) {
			editor.close();
			editor = null;
		}
	}
	
	private void refreshActions() {
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.setEnabled(getCorpus()!=null, 
				"plugins.languageTools.corpusEditView.resetEditAction",  //$NON-NLS-1$
				"plugins.languageTools.corpusEditView.applyEditAction"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#close()
	 */
	@Override
	public void close() {
		
		CorpusRegistry.getInstance().removeListener(handler);
		
		Editor<Corpus> editor = this.editor;
		if(editor!=null) {
			editor.close();
		}

		this.editor = null;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#isClosable()
	 */
	@Override
	public boolean isClosable() {
		if(editor==null) {
			return true;
		}
		if(!editor.hasChanges()) {
			return true;
		}
		
		Corpus corpus = getCorpus();
		if(corpus==null) {
			return true;
		}
		
		// Let user decide whether to discard unsaved changes
		return DialogFactory.getGlobalFactory().showConfirm(null, 
				"plugins.languageTools.corpusEditView.dialogs.discardChanges.title",  //$NON-NLS-1$
				"plugins.languageTools.corpusEditView.dialogs.discardChanges.message",  //$NON-NLS-1$
				corpus.getName());
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		editCorpus(null);
		refreshActions();
	}
	
	private void editCorpus(Corpus corpus) {
		if(corpus!=null) {
			requestFocusInPerspective();
		}
		
		Corpus oldCorpus = getCorpus();
		if(oldCorpus==corpus) {
			return;
		}
		
		// Offer chance to save changes 
		if(oldCorpus!=null && editor!=null && editor.hasChanges()) {
			// Let user decide whether to discard unsaved changes
			if(DialogFactory.getGlobalFactory().showConfirm(null, 
					"plugins.languageTools.corpusEditView.dialogs.saveChanges.title",  //$NON-NLS-1$
					"plugins.languageTools.corpusEditView.dialogs.saveChanges.message",  //$NON-NLS-1$
					oldCorpus.getName())) {
				
				try {
					editor.applyEdit();
				} catch(Exception ex) {
					LoggerFactory.getLogger(CorpusEditView.class).log(LoggerFactory.record(Level.SEVERE, 
							"Failed to apply edit: "+getCorpus(), ex)); //$NON-NLS-1$
				}
			}
		}
		
		getContainer().remove(infoLabel);
		
		if(corpus==null) {
			showDefaultInfo();
			return;
		}
		
		// If the new corpus is of the same type just use
		// the present editor!
		if(oldCorpus!=null && oldCorpus.getClass().equals(
				corpus.getClass()) && editor!=null) {
			editor.setEditingItem(corpus);
			header.setText(corpus.getName());
			return;
		}
		
		// Try to fetch an editor for the supplied corpus
		@SuppressWarnings("unchecked")
		Editor<Corpus> editor = UIHelperRegistry.globalRegistry().findHelper(
				Editor.class, corpus);
		
		if(editor==null) {
			showDefaultInfo();
			return;
		}
		
		if(this.editor!=null) {
			this.editor.close();
		}

		this.editor = editor;
		
		editor.setEditingItem(corpus);
		scrollPane.setViewportView(editor.getEditorComponent());
		header.setText(corpus.getName());
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#handleRequest(net.ikarus_systems.icarus.util.opi.Message)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.EDIT.equals(message.getCommand())) {
			Object data = message.getData();
			
			// We allow null values since this is a way to clear the editor view
			if(data==null || data instanceof Corpus) {
				editCorpus((Corpus) data);
				refreshActions();
				return message.successResult(null);				
			} else {
				return message.unsupportedDataResult();
			}
		} else {
			return message.unknownRequestResult();
		}
	}

	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.addHandler("plugins.languageTools.corpusEditView.resetEditAction",  //$NON-NLS-1$
				callbackHandler, "resetEdit"); //$NON-NLS-1$
		
		actionManager.addHandler("plugins.languageTools.corpusEditView.applyEditAction",  //$NON-NLS-1$
				callbackHandler, "applyEdit"); //$NON-NLS-1$
	}

	private class Handler implements EventListener {

		/**
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			if(getCorpus()==null) {
				return;
			}
			
			if(sender==CorpusRegistry.getInstance()) {
				Corpus corpus = (Corpus) event.getProperty("corpus"); //$NON-NLS-1$

				// Handle deleted corpora								
				if(corpus==getCorpus()) {
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
			Editor<Corpus> editor = getEditor();
			if(editor==null) {
				return;
			}
			
			try {
				editor.resetEdit();
			} catch(Exception ex) {
				LoggerFactory.getLogger(CorpusEditView.class).log(LoggerFactory.record(Level.SEVERE, 
						"Failed to reset editor: "+getCorpus(), ex)); //$NON-NLS-1$
			}
		}
		
		public void applyEdit(ActionEvent e) {
			Editor<Corpus> editor = getEditor();
			if(editor==null) {
				return;
			}
			
			try {
				editor.applyEdit();
				
				CorpusRegistry.getInstance().corpusChanged(editor.getEditingItem());
			} catch(Exception ex) {
				LoggerFactory.getLogger(CorpusEditView.class).log(LoggerFactory.record(Level.SEVERE, 
						"Failed to apply edit: "+getCorpus(), ex)); //$NON-NLS-1$
			}
			
			header.setText(getCorpus().getName());
		}
	}
}
