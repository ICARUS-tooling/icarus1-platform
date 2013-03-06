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
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.helper.Editor;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.opi.Message;
import net.ikarus_systems.icarus.util.opi.ResultMessage;

public class WeblichtEditView extends View {

	private Editor<Webservice> editor;

	private JLabel header;
	private JLabel infoLabel;

	private JScrollPane contentSP;

	// private Handler handler;
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
			LoggerFactory.getLogger(WeblichtEditView.class).log(
					LoggerFactory.record(Level.SEVERE,
							"Failed to load actions from file", e)); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}

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
		footer.add(new JButton("Apply")); //$NON-NLS-1$
		footer.add(new JButton("Reset")); //$NON-NLS-1$		

		// Description Scrollpane
		contentSP = new JScrollPane();
		contentSP.setBorder(null);

		container.setLayout(new BorderLayout());
		container.add(header, BorderLayout.NORTH);
		container.add(contentSP, BorderLayout.CENTER);
		container.add(footer, BorderLayout.SOUTH);

		showDefaultInfo();

		registerActionCallbacks();
		;
		refreshActions();
	}

	private void showDefaultInfo() {
		contentSP.setViewportView(infoLabel);
		header.setText(""); //$NON-NLS-1$
	}

	private void refreshActions() {
		ActionManager actionManager = getDefaultActionManager();
		// add weblicht service check.. get weblicht,

		actionManager.setEnabled(getWebservice() != null,
				"plugins.weblicht.weblichtEditView.resetEditAction", //$NON-NLS-1$
				"plugins.weblicht.weblichtEditView.applyEditAction"); //$NON-NLS-1$

	}

	private Webservice getWebservice() {
		return editor == null ? null : editor.getEditingItem();
	}

	private Editor<Webservice> getEditor() {
		return editor;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isClosable() {
		return true;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		// TODO Auto-generated method stub
		return null;
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
				LoggerFactory.getLogger(WeblichtEditView.class).log(
						LoggerFactory.record(Level.SEVERE,
								"Failed to reset editor: " + getWebservice(), ex)); //$NON-NLS-1$
			}
		}

		public void applyEdit(ActionEvent e) {
			Editor<Webservice> editor = getEditor();
			if (editor == null) {
				return;
			}

			try {
				editor.applyEdit();
				//CorpusRegistry.getInstance().corpusChanged(editor.getEditingItem());
			} catch (Exception ex) {
				LoggerFactory.getLogger(WeblichtEditView.class).log(
						LoggerFactory.record(Level.SEVERE,
								"Failed to apply edit: " + getWebservice(), ex)); //$NON-NLS-1$
			}

			header.setText(getWebservice().getName());
		}
	}

}
