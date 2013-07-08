/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.ims.icarus.Core;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.View;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.dialog.BasicDialogBuilder;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.util.CorruptedStateException;

/**
 * @author Gregor Thiele
 * @version $Id$
 * 
 */
public class NGramQueryView extends View {

	protected JPanel contentPanel;

	protected Collection<JComponent> localizedComponents;

	private JScrollPane scrollPane;
	
	//Tablestuff
	protected NGramQTableModel qtm;
	protected JTable qt;
	protected JButton qtAddButton;
	protected JButton qtEditButton;
	protected JButton qtRemoveButton;
	
	private JLabel header;
	private JLabel infoLabel;

	private Handler handler;
	private CallbackHandler callbackHandler;
	
	/**
	 * Constructor
	 */
	public NGramQueryView(){
		//noop
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {

		// Load actions
		URL actionLocation = ErrorMiningView.class
				.getResource("query-view-actions.xml"); //$NON-NLS-1$
		if (actionLocation == null)
			throw new CorruptedStateException(
					"Missing resources: query-view-actions.xml"); //$NON-NLS-1$

		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.log(this, Level.SEVERE,
					"Failed to load actions from file", e); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}

		handler = createHandler();
		
		// Header label
		header = new JLabel(""); //$NON-NLS-1$
		header.setBorder(new EmptyBorder(3, 5, 10, 20));
		header.setFont(header.getFont().deriveFont(header.getFont().getSize2D() + 2));

		// Info label
		infoLabel = new JLabel();
		infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		infoLabel.setVerticalAlignment(SwingConstants.TOP);
		ResourceManager.getInstance().getGlobalDomain()
				.prepareComponent(infoLabel,"plugins.errormining.nGramQueryView.notAvailable", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);
		
		
		// Description Scrollpane
		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);	
		UIUtil.defaultSetUnitIncrement(scrollPane);
		scrollPane.setPreferredSize(new Dimension(400, 400));

		
		container.add(new JLabel("FUCKCK"));
		//put all on viewcontainer
		container.setLayout(new BorderLayout());
		container.add(header, BorderLayout.NORTH);
		container.add(scrollPane, BorderLayout.CENTER);
		
		showDefaultInfo();
		
		registerActionCallbacks();
		refreshActions();
		
		
		
		// buildDialog();
	}
	
	
	
	private void showDefaultInfo() {
		scrollPane.setViewportView(infoLabel);
		header.setText(""); //$NON-NLS-1$
	
	}
	

	protected void refreshActions() {
		// no-op
	}

	public void buildDialog() {
		qtm = new NGramQTableModel();
		qt = new JTable(qtm);
		qt.setBorder(UIUtil.defaultContentBorder);
		qt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		qt.getTableHeader().setReorderingAllowed(false);
		qt.getSelectionModel().addListSelectionListener(handler);
		qt.addMouseListener(handler);
		qt.setIntercellSpacing(new Dimension(4, 4));
		scrollPane.setViewportView(qt);
	}
	

	protected void addQueryInput() {

		NGramQAttributes att = showEditQAttributes(null,
				"plugins.errormining.dialogs.addInput.title", //$NON-NLS-1$
				"plugins.errormining.dialogs.addInput.message", //$NON-NLS-1$
				null, null);

		if (att == null) {
			return;
		}

		// empty attributenaim will fail later
		if (att.getKey().equals("")) { //$NON-NLS-1$
			return;
		}

		// System.out.println(wio.getAttributename() + " " +
		// wio.getAttributevalues());
		// -1 create item
		qtm.setQueryAttributes(att, -1);

	}

	protected void editQueryInput(String attribute, String attributevalue,
			int index) {

		NGramQAttributes att = showEditQAttributes(null,
				"plugins.errormining.dialogs.editInput.title", //$NON-NLS-1$
				"plugins.errormining.dialogs.editInput.message", //$NON-NLS-1$
				null, null);

		// empty attributename will fail later / not allowed
		if (att.getKey().equals("")) { //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showWarning(null,
					"plugins.errormining.dialogs.emptyAttributename.title", //$NON-NLS-1$
					"plugins.errormining.dialogs.emptyAttributename.message", //$NON-NLS-1$
					null, null);
			return;
		}

		// System.out.println(wio.getAttributename() + " " +
		// wio.getAttributevalues());
		qtm.setQueryAttributes(att, index);
	}

	protected void deleteQueryInput(String attribute, int itemIndex) {
		if (DialogFactory.getGlobalFactory().showConfirm(null,
				"plugins.errormining.dialogs.deleteInput.title", //$NON-NLS-1$
				"plugins.errormining.dialogs.deleteInput.message", //$NON-NLS-1$
				attribute)) {
			qtm.deleteQueryAttribute(itemIndex);
		}
	}

	protected void registerActionCallbacks() {
		if (callbackHandler == null) {
			callbackHandler = createCallbackHandler();
		}

		ActionManager actionManager = getDefaultActionManager();

		actionManager.addHandler(
				"plugins.errorMining.nGramQueryView.newQueryAction", //$NON-NLS-1$
				callbackHandler, "addQuery"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.errorMining.nGramQueryView.editQueryAction", //$NON-NLS-1$
				callbackHandler, "editQuery"); //$NON-NLS-1$
		actionManager.addHandler(
				"plugins.errorMining.nGramQueryView.deleteQueryAction", //$NON-NLS-1$
				callbackHandler, "deleteQuery"); //$NON-NLS-1$

	}

	protected Handler createHandler() {
		return new Handler();
	}

	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandler();
	}

	protected class Handler extends MouseAdapter implements ActionListener,
			ListSelectionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			// Add Input / output
			if (e.getSource() == qtAddButton) {

				try {
					if (e.getSource() == qtAddButton) {
						// addQueryInput(getWebserviceName());
						addQueryInput();
					}

				} catch (Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Failed to add key ", ex); //$NON-NLS-1$
				}
				return;
			}

			// edit Input
			if (e.getSource() == qtEditButton) {

				int row = qt.getSelectedRow();
				if (row == -1) {
					return;
				}

				String attribute = (String) qt.getValueAt(row, 0);
				String attributevalue = (String) qt.getValueAt(row, 1);

				try {
					editQueryInput(attribute, attributevalue, row);
				} catch (Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Failed to edit input attribute", ex); //$NON-NLS-1$
				}
				return;
			}

			// delete Input
			if (e.getSource() == qtRemoveButton) {

				int row = qt.getSelectedRow();
				if (row == -1) {
					return;
				}

				String attribute = (String) qt.getValueAt(row, 0);

				// System.out.println("DelRow " + inputTableModel.getRowCount()
				// + " " + attribute);

				try {
					deleteQueryInput(attribute, row);
				} catch (Exception ex) {
					LoggerFactory.log(this, Level.SEVERE,
							"Failed to delete attribute", ex); //$NON-NLS-1$
				}
				return;
			}

		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			boolean enabled = e.getFirstIndex() > -1;

			if (e.getSource().equals(qt.getSelectionModel())) {
				qtAddButton.setEnabled(enabled);
				qtEditButton.setEnabled(enabled);
				qtRemoveButton.setEnabled(enabled);
			}

		}
	}

	public NGramQAttributes showEditQAttributes(Component parent, String title,
			String message, String attributename, String attributevalues,
			Object... params) {
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory
				.getGlobalFactory().getResourceDomain());

		NGramQAttributes att = new NGramQAttributes();

		JTextField attributenameField = new JTextField(30);
		attributenameField.setText(attributename);

		JTextField attributevaluesField = new JTextField(30);
		attributevaluesField.setText(attributevalues);

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage("plugins.errormining.labels.Key"); //$NON-NLS-1$
		builder.addMessage(attributenameField);
		builder.addMessage("plugins.errormining.labels.Value"); //$NON-NLS-1$
		builder.addMessage(attributevaluesField);
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$

		builder.showDialog(parent);

		if (!builder.isYesValue()) {
			return null;
		}

		att.setKey((attributenameField.getText()));
		att.setValue((attributevaluesField.getText()));

		return att;
	}

	public final class CallbackHandler {

		private CallbackHandler() {
			// no-op
		}

		public void addQuery() {
			// TODO
			Core.showNotice();
		}

		public void editQuery() {
			// TODO
			Core.showNotice();
		}

		public void deleteQuery() {
			// TODO
			Core.showNotice();
		}

	}

}
