/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.weblicht.webservice;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.weblicht.WebserviceDialogs;
import de.ims.icarus.plugins.weblicht.WebserviceEditorExtension;
import de.ims.icarus.plugins.weblicht.WebserviceInputTableModel;
import de.ims.icarus.plugins.weblicht.WebserviceOutputTableModel;
import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.GridBagUtil;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.helper.Editor;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebserviceEditor implements WebserviceEditorExtension, Editor<Webservice> {
	
	protected boolean limitedAccess = true;

	protected JPanel contentPanel;
	
	protected Collection<JComponent> localizedComponents;
	
	protected JTextField nameInput;
	protected JTextArea description;
	protected JTextField creator;
	protected JTextField contact;
	protected JTextField url;
	protected JTextField serviceID;
	protected JTextField webresourceFormat;
	protected JTextField uniqueID;
	
	//protected JList<Object> inputList;
	protected JTable inputTable;
	protected WebserviceInputTableModel inputTableModel;
	protected JButton webserviceInputAddButton;
	protected JButton webserviceInputEditButton;
	protected JButton webserviceInputRemoveButton;
	
	
	//protected JList<Object> outputList;
	protected JTable outputTable;
	protected WebserviceOutputTableModel outputTableModel;
	protected JButton webserviceOutputAddButton;
	protected JButton webserviceOutputEditButton;
	protected JButton webserviceOutputRemoveButton;
	
	protected Webservice webservice;
	protected Handler handler;
	

	public WebserviceEditor() {
		//noop
	}
	
	

	protected int feedBasicComponents(JPanel panel) {
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		JLabel label;
		
		GridBagConstraints gbc = GridBagUtil.makeGbc(0, 0);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(1, 2, 1, 2);
		
		
		// Webservice uniqueID edit Field
		label = new JLabel();
		resourceDomain.prepareComponent(label, 
				"plugins.weblicht.labels.webservice.uniqueID", null); //$NON-NLS-1$
		resourceDomain.addComponent(label);
		localizedComponents.add(label);
		panel.add(label, gbc);
		gbc.gridx++;
		panel.add(uniqueID,gbc);
		
		// Webservice Name edit Field 
		gbc.gridx = 0;
		gbc.gridy++;
		label = new JLabel();
		resourceDomain.prepareComponent(label, 
				"plugins.weblicht.labels.webservice.name", null); //$NON-NLS-1$
		resourceDomain.addComponent(label);
		localizedComponents.add(label);
		panel.add(label, gbc);
		gbc.gridx++;
		panel.add(nameInput, gbc);
		
		panel.add(Box.createGlue(), GridBagUtil.makeGbcH(gbc.gridx+1, gbc.gridy, 
				GridBagConstraints.REMAINDER, 1));
		
		// Webservice contact edit Field
		gbc.gridx = 0;
		gbc.gridy++;
		label = new JLabel();
		resourceDomain.prepareComponent(label, 
				"plugins.weblicht.labels.webservice.contact", null); //$NON-NLS-1$
		resourceDomain.addComponent(label);
		localizedComponents.add(label);
		panel.add(label, gbc);
		gbc.gridx++;
		panel.add(contact,gbc);
		
		// Webservice creator edit Field
		gbc.gridx = 0;
		gbc.gridy++;
		label = new JLabel();
		resourceDomain.prepareComponent(label, 
				"plugins.weblicht.labels.webservice.creator", null); //$NON-NLS-1$
		resourceDomain.addComponent(label);
		localizedComponents.add(label);
		panel.add(label, gbc);
		gbc.gridx++;
		panel.add(creator,gbc);
		
		// Webservice url edit Field
		gbc.gridx = 0;
		gbc.gridy++;
		label = new JLabel();
		resourceDomain.prepareComponent(label, 
				"plugins.weblicht.labels.webservice.url", null); //$NON-NLS-1$
		resourceDomain.addComponent(label);
		localizedComponents.add(label);
		panel.add(label, gbc);
		gbc.gridx++;
		panel.add(url,gbc);
		
		// Webservice serviceID edit Field
		gbc.gridx = 0;
		gbc.gridy++;
		label = new JLabel();
		resourceDomain.prepareComponent(label, 
				"plugins.weblicht.labels.webservice.serviceID", null); //$NON-NLS-1$
		resourceDomain.addComponent(label);
		localizedComponents.add(label);
		panel.add(label, gbc);
		gbc.gridx++;
		panel.add(serviceID,gbc);
		
		// Webservice webresourceFormat Field
		gbc.gridx = 0;
		gbc.gridy++;
		label = new JLabel();
		resourceDomain.prepareComponent(label, 
				"plugins.weblicht.labels.webservice.webresourceFormat", null); //$NON-NLS-1$
		resourceDomain.addComponent(label);
		localizedComponents.add(label);
		panel.add(label, gbc);
		gbc.gridx++;
		panel.add(webresourceFormat,gbc);
		
		
		// Webservice description edit Field
		gbc.gridx = 0;
		gbc.gridy++;
		label = new JLabel();
		resourceDomain.prepareComponent(label, 
				"plugins.weblicht.labels.webservice.description", null); //$NON-NLS-1$
		resourceDomain.addComponent(label);
		localizedComponents.add(label);
		panel.add(label, gbc);
		gbc.gridx++;
		panel.add(description,gbc);
		gbc.gridy++;
		
		panel.add(Box.createGlue(), GridBagUtil.makeGbcH(gbc.gridx+1, gbc.gridy, 
				GridBagConstraints.REMAINDER, 1));
		
		return ++gbc.gridy;
	}
	
	
	protected void feedEditorComponent(JPanel panel) {
		
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		JLabel label;
		
		
		panel.setLayout(new GridBagLayout());
		JPanel inputPanel = new JPanel(new GridBagLayout());
		JPanel outputPanel = new JPanel(new GridBagLayout());
		
		int row = feedBasicComponents(panel);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = row;
		
		panel.add(Box.createVerticalStrut(10), gbc);
		gbc.gridy++;
		
		// Webservice Input Label
		label = new JLabel();
		resourceDomain.prepareComponent(label, 
				"plugins.weblicht.labels.webservice.Input", null); //$NON-NLS-1$
		resourceDomain.addComponent(label);
		localizedComponents.add(label);
		panel.add(label, gbc);
		gbc.gridy++;
		
		//input table		
		gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.NORTH;
		JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
		buttonPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
		buttonPanel.add(webserviceInputAddButton);
		buttonPanel.add(webserviceInputEditButton);
		buttonPanel.add(webserviceInputRemoveButton);
		inputPanel.add(buttonPanel, gbc);
		
				
		gbc.gridx++;
		gbc.gridheight = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;		
		inputPanel.add(inputTable.getTableHeader(), gbc);

		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 100;
		gbc.weightx = 100;
		inputPanel.add(inputTable, gbc);
		
		panel.add(inputPanel,gbc);

		
		//#############################################
		GridBagConstraints gbcOut = new GridBagConstraints();
		gbcOut.gridx = 0;
		gbcOut.gridy = gbc.gridy;
		
		
		gbcOut.gridx = 0;
		panel.add(Box.createVerticalStrut(10), gbcOut);
		gbcOut.gridy++;
		
		// Webservice Output Label
		label = new JLabel();
		resourceDomain.prepareComponent(label, 
				"plugins.weblicht.labels.webservice.Output", null); //$NON-NLS-1$
		resourceDomain.addComponent(label);
		localizedComponents.add(label);
		panel.add(label, gbcOut);
		gbcOut.gridy++;
		
		
		//Output table
		gbcOut.gridheight = GridBagConstraints.REMAINDER;
		gbcOut.anchor = GridBagConstraints.NORTH;
		
		JPanel buttonOutPanel = new JPanel(new GridLayout(3, 1));
		buttonOutPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
		buttonOutPanel.add(webserviceOutputAddButton);
		buttonOutPanel.add(webserviceOutputEditButton);
		buttonOutPanel.add(webserviceOutputRemoveButton);
		outputPanel.add(buttonOutPanel, gbcOut);		

		gbcOut.gridx++;
		gbcOut.gridheight = 1;
		gbcOut.gridwidth = GridBagConstraints.REMAINDER;
		gbcOut.fill = GridBagConstraints.HORIZONTAL;		
		outputPanel.add(outputTable.getTableHeader(), gbcOut);

		gbcOut.gridy++;
		gbcOut.fill = GridBagConstraints.BOTH;
		gbcOut.weighty = 100;
		gbcOut.weightx = 100;
		outputPanel.add(outputTable, gbcOut);
		
		panel.add(outputPanel,gbcOut);
	}
	
	protected Handler createHandler() {
		return new Handler();
	}
	
	
	private JTextField createTextField() {
		JTextField textField = new JTextField(65) {
			private static final long serialVersionUID = 1L;
			
		};
		
		UIUtil.createUndoSupport(textField, 30);
		UIUtil.addPopupMenu(textField, UIUtil.createDefaultTextMenu(textField, true));		
		
		return textField;
		
	}
	
	private JTextArea createTextArea() {
		JTextArea textArea = new JTextArea() {

			private static final long serialVersionUID = -3234388779826990121L;

			// force resizing according to the enclosing scroll pane's width
			@Override
			public boolean getScrollableTracksViewportWidth() {
				return true;
			}
		};
		textArea.setPreferredSize(new Dimension(500, 190));
		UIUtil.createUndoSupport(textArea, 30);
		UIUtil.addPopupMenu(textArea, UIUtil.createDefaultTextMenu(textArea, true));
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		JPanel container = new JPanel(new BorderLayout());
		container.add(scrollPane, BorderLayout.CENTER);
		
		textArea.putClientProperty("container", container);		 //$NON-NLS-1$
		textArea.setText(null);
		textArea.setToolTipText(null);		
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		return textArea;
	}
	
	protected void init() {
		
		localizedComponents = new ArrayList<>();
		
		handler = createHandler();
		
		//no undo needed (not editable at all)
		uniqueID = new JTextField(65);
		
		
		// generate fields with undo supports
		nameInput = createTextField();
		description = createTextArea();
		creator = createTextField();
		contact = createTextField();
		url = createTextField();
		serviceID = createTextField();
		webresourceFormat = createTextField();
		
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		
		//Add- /Remove- /Edit-Button
		webserviceInputAddButton = new JButton();
		webserviceInputAddButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("add_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceInputAddButton, "add", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceInputAddButton);
		localizedComponents.add(webserviceInputAddButton);
		webserviceInputAddButton.addActionListener(handler);
		
		webserviceInputRemoveButton = new JButton();
		webserviceInputRemoveButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("delete_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceInputRemoveButton, "delete", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceInputRemoveButton);
		localizedComponents.add(webserviceInputRemoveButton);
		webserviceInputRemoveButton.addActionListener(handler);
		
		webserviceInputEditButton = new JButton();
		webserviceInputEditButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("write_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceInputEditButton, "edit", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceInputEditButton);
		localizedComponents.add(webserviceInputEditButton);
		webserviceInputEditButton.addActionListener(handler);
		
		
		inputTableModel = new WebserviceInputTableModel();
		inputTable = new JTable(inputTableModel);
		inputTable.setBorder(UIUtil.defaultContentBorder);
		inputTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		inputTable.getTableHeader().setReorderingAllowed(false);
		inputTable.getSelectionModel().addListSelectionListener(handler);
		inputTable.addMouseListener(handler);
		inputTable.setIntercellSpacing(new Dimension(4, 4));
		
		
		//Add- /Remove- /Edit-Button		
		webserviceOutputAddButton = new JButton();
		webserviceOutputAddButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("add_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceOutputAddButton, "add", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceOutputAddButton);
		localizedComponents.add(webserviceOutputAddButton);
		webserviceOutputAddButton.addActionListener(handler);
		
		webserviceOutputRemoveButton = new JButton();
		webserviceOutputRemoveButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("delete_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceOutputRemoveButton, "delete", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceOutputRemoveButton);
		localizedComponents.add(webserviceOutputRemoveButton);
		webserviceOutputRemoveButton.addActionListener(handler);
		
		
		webserviceOutputEditButton = new JButton();
		webserviceOutputEditButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("write_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceOutputEditButton, "edit", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceOutputEditButton);
		localizedComponents.add(webserviceOutputEditButton);
		webserviceOutputEditButton.addActionListener(handler);		
		
		outputTableModel = new WebserviceOutputTableModel();
		outputTable = new JTable(outputTableModel);
		outputTable.setBorder(UIUtil.defaultContentBorder);
		outputTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		outputTable.getTableHeader().setReorderingAllowed(false);
		outputTable.getSelectionModel().addListSelectionListener(handler);
		outputTable.addMouseListener(handler);
		outputTable.setIntercellSpacing(new Dimension(4, 4));
		
		
		inputTableModel.reload(webservice);
		outputTableModel.reload(webservice);
		
		WebserviceRegistry.getInstance().addListener(Events.REMOVED, handler);
		WebserviceRegistry.getInstance().addListener(Events.CHANGED, handler);
	}
	
	protected String getWebserviceName() {
		return webservice==null ? "<undefined>" : webservice.getName(); //$NON-NLS-1$
	}
	
	
	//remove edit options from items when limited acces type is set
	protected void refreshItemAccess(){
		
		nameInput.setEditable(getAccessType());
		description.setEditable(getAccessType());
		//description.setEnabled(getAccessType());
		creator.setEditable(getAccessType());
		contact.setEditable(getAccessType());
		url.setEditable(getAccessType());
		serviceID.setEditable(getAccessType());
		webresourceFormat.setEditable(getAccessType());
		
		inputTable.setEnabled(getAccessType());
		outputTable.setEnabled(getAccessType());
		
		//set all disabled
		if (!(limitedAccess)){
		webserviceInputAddButton.setEnabled(getAccessType());
		webserviceInputEditButton.setEnabled(getAccessType());
		webserviceInputRemoveButton.setEnabled(getAccessType());
		
		webserviceOutputAddButton.setEnabled(getAccessType());
		webserviceOutputEditButton.setEnabled(getAccessType());
		webserviceOutputRemoveButton.setEnabled(getAccessType());
		}
		
		
	}
	
	
	/**
	 * @see de.ims.icarus.ui.helper.Editor#getEditorComponent()
	 */
	@Override
	public Component getEditorComponent() {
		if(contentPanel==null) {
			contentPanel = new JPanel();
			contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			init();
			feedEditorComponent(contentPanel);
			
			resetEdit();
		}
		return contentPanel;
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#setEditingItem(java.lang.Object)
	 */
	@Override
	public void setEditingItem(Webservice webservice) {
		
		//Complete removed to ensure access feature working correct
//		if(this.webservice==webservice) {
//			return;
//		}
		
		
		this.webservice = webservice;
		resetEdit();
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#getEditingItem()
	 */
	@Override
	public Webservice getEditingItem() {
		return webservice;
	}
	
	
	/**
	 * @see de.ims.icarus.plugins.weblicht.WebserviceEditorExtension#setAccessType(boolean)
	 */
	@Override
	public void setAccessType(boolean limitedAccess) {
		this.limitedAccess = limitedAccess;
		
	}


	/**
	 * @see de.ims.icarus.plugins.weblicht.WebserviceEditorExtension#getAccessType()
	 */
	@Override
	public boolean getAccessType() {
		return limitedAccess;
	}


	protected void refreshWebserviceActions() {
		boolean enabledInput = inputTable.getSelectedRow()!=-1;
		webserviceInputEditButton.setEnabled(enabledInput);
		webserviceInputRemoveButton.setEnabled(enabledInput);
		
		boolean enabledOutput = outputTable.getSelectedRow()!=-1;
		webserviceOutputEditButton.setEnabled(enabledOutput);
		webserviceOutputRemoveButton.setEnabled(enabledOutput);
		refreshItemAccess();
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#resetEdit()
	 */
	@Override
	public void resetEdit() {
		if(contentPanel==null) {
			return;
		}
		if(webservice==null) {
			nameInput.setText(null);
			description.setText(null);
			creator.setText(null);
			contact.setText(null);
			url.setText(null);
			serviceID.setText(null);
			webresourceFormat.setText(null);
			uniqueID.setText(null);
			return;
		}
		
		// Name
		nameInput.setText(webservice.getName());
		description.setText(webservice.getDescription());
		creator.setText(webservice.getCreator());
		contact.setText(webservice.getContact());
		url.setText(webservice.getURL());
		serviceID.setText(webservice.getServiceID());
		webresourceFormat.setText(webservice.getWebresourceFormat());
		uniqueID.setText(webservice.getUID());
		uniqueID.setEditable(false);
	
		/*
		List<WebserviceIOAttributes> wiol = new ArrayList<>();
		wiol = webservice.getInput();
		for (int i = 0; i < wiol.size(); i++){
			System.out.print("Name: " + wiol.get(i).getAttributename());
			System.out.println(" : " + wiol.get(i).getAttributevalues());
			
		}System.out.println("-----------");
		
		List<WebserviceIOAttributes> wool = new ArrayList<>();
		wool = webservice.getOutput();
		for (int i = 0; i < wool.size(); i++){
			System.out.print("Name: " + wool.get(i).getAttributename());
			System.out.println(" : " + wool.get(i).getAttributevalues());
	
		}System.out.println("-----------");
		*/
		
		// InputRefresh
		inputTableModel.reload(webservice);
		outputTableModel.reload(webservice);
		refreshWebserviceActions();	
	}


	/**
	 * @see de.ims.icarus.ui.helper.Editor#applyEdit()
	 */
	@Override
	public void applyEdit() {
		if(contentPanel==null) {
			return;
		}
		if(webservice==null) {
			return;
		}
		
		String newName = nameInput.getText();
		String newDesc = description.getText();
		String newCreator = creator.getText();
		String newContact = contact.getText();
		String newURL = url.getText();
		String newServiceID = serviceID.getText();
		String newWebresourceFormat = webresourceFormat.getText();
		List<WebserviceIOAttributes> newInput = fetchInputTable(inputTableModel);
		List<WebserviceIOAttributes> newOutput = fetchOutputTable(outputTableModel);;
		
		

		
		System.out.println(outputTable.toString());
		
		WebserviceRegistry.getInstance().setName(webservice, newName);
		WebserviceRegistry.getInstance().setDescription(webservice, newDesc);
		WebserviceRegistry.getInstance().setCreator(webservice, newCreator);
		WebserviceRegistry.getInstance().setContact(webservice, newContact);
		WebserviceRegistry.getInstance().setURL(webservice, newURL);
		WebserviceRegistry.getInstance().setServiceID(webservice, newServiceID);
		WebserviceRegistry.getInstance().setWebresourceFormat(webservice, newWebresourceFormat);
		WebserviceRegistry.getInstance().setInputAttributes(webservice, newInput);
		WebserviceRegistry.getInstance().setOutputAttributes(webservice, newOutput);
	}
	
	/**
	 * 
	 * @param itm
	 * @return
	 */
	private List<WebserviceIOAttributes> fetchInputTable(WebserviceInputTableModel itm){
		List<WebserviceIOAttributes> input = new ArrayList<>();
		WebserviceIOAttributes wAtt;;
		for (int i = 0; i < itm.getRowCount();i++) {
			wAtt = new WebserviceIOAttributes();
			wAtt.setAttributename((String) itm.getValueAt(i, 0));
			wAtt.setAttributevalues((String) itm.getValueAt(i, 1));
			input.add(wAtt);			
		}
		return input;
	}
	
	/**
	 * 
	 * @param outputTableModel2
	 * @return
	 */
	private List<WebserviceIOAttributes> fetchOutputTable(WebserviceOutputTableModel outputTableModel2){
		List<WebserviceIOAttributes> output = new ArrayList<>();
		WebserviceIOAttributes wAtt;;
		for (int i = 0; i < outputTableModel2.getRowCount();i++) {
			wAtt = new WebserviceIOAttributes();
			wAtt.setAttributename((String) outputTableModel2.getValueAt(i, 0));
			wAtt.setAttributevalues((String) outputTableModel2.getValueAt(i, 1));
			output.add(wAtt);			
		}
		return output;
	}


	/**
	 * @see de.ims.icarus.ui.helper.Editor#hasChanges()
	 */
	@Override
	public boolean hasChanges() {
		if(contentPanel==null) {
			return false;
		}
		if(webservice==null) {
			return false;
		}
		
		
		/* Compare uniqueID not needed uniqueID must not be changed!
		 * otherwise we could run into problems.
		 * */

		// Compare name
		if(!nameInput.getText().equals(webservice.getName())) {
			return true;
		}
		
		// Compare description
		if(!description.getText().equals(webservice.getDescription())) {
			return true;
		}
		// Compare creator
		if(!creator.getText().equals(webservice.getCreator())) {
			return true;
		}
		// Compare contact
		if(!contact.getText().equals(webservice.getContact())) {
			return true;
		}
		// Compare url
		if(!url.getText().equals(webservice.getURL())) {
			return true;
		}
		// Compare description
		if(!serviceID.getText().equals(webservice.getServiceID())) {
			return true;
		}
		
		// Compare webserviceFormat
		if(!webresourceFormat.getText().equals(webservice.getWebresourceFormat())) {
			return true;
		}
		
		// Compare list of input attributes
		
		
		//TODO stuff for attributes
		/*
		List<WebserviceIOAttributes> inputAttributes = WebserviceRegistry.getInstance().getWebserviceInput(webservice);
		System.out.println(inputTableModel.hasInputAttributeChanges(inputAttributes));
		*/

		return false;
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#close()
	 */
	@Override
	public void close() {
		WebserviceRegistry.getInstance().removeListener(handler);
		
		if(localizedComponents==null) {
			return;
		}
		for(JComponent component : localizedComponents) {
			ResourceManager.getInstance().removeLocalizableItem(component);
		}
		
		webservice = null;
	}
	
	
	//------Input Attributes

	protected void addWebserviceInput(String attribute) {

		WebserviceIOAttributes wio = WebserviceDialogs.getWebserviceDialogFactory().showWebserviceIOEditAttributes(null, 
				"plugins.weblicht.webserviceEditView.dialogs.addInput.title",  //$NON-NLS-1$
				"plugins.weblicht.webserviceEditView.dialogs.addInput.message",  //$NON-NLS-1$
				null, null);
		
		if (wio==null){
			return;
		}
		
		//empty attributenaim will fail later
		if (wio.getAttributename().equals("")){ //$NON-NLS-1$
			return;
		}
		
		//System.out.println(wio.getAttributename() + " " + wio.getAttributevalues());
		// -1 create item
		inputTableModel.setInputAttributes(wio, -1);
		
	}
	
	protected void editWebserviceInput(String attribute, String attributevalue, int index) {
		
		WebserviceIOAttributes wio = WebserviceDialogs.getWebserviceDialogFactory().showWebserviceIOEditAttributes(null, 
				"plugins.weblicht.webserviceEditView.dialogs.editInput.title",  //$NON-NLS-1$
				"plugins.weblicht.webserviceEditView.dialogs.editInput.message",  //$NON-NLS-1$
				attribute, attributevalue, attribute);
		
		//empty attributename will fail later / not allowed
		if (wio.getAttributename().equals("")){ //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showWarning(null,
					"plugins.weblicht.weblichtEditView.dialogs.emptyAttributename.title", //$NON-NLS-1$
					"plugins.weblicht.weblichtEditView.dialogs.emptyAttributename.message", //$NON-NLS-1$
					null,null);
			return;
		}
		
		//System.out.println(wio.getAttributename() + " " + wio.getAttributevalues());	
		inputTableModel.setInputAttributes(wio, index);
	}
	
	protected void deleteWebserviceInput(String attribute, int itemIndex) {
		if(DialogFactory.getGlobalFactory().showConfirm(null, 
				"plugins.weblicht.webserviceEditView.dialogs.deleteInput.title",  //$NON-NLS-1$
				"plugins.weblicht.webserviceEditView.dialogs.deleteInput.message",  //$NON-NLS-1$
				attribute)) {
			inputTableModel.deleteInputAttribute(itemIndex);
		}
	}
	
	
	//------- Output stuff
	protected void addWebserviceOutput(String attribute) {

		WebserviceIOAttributes wio = WebserviceDialogs.getWebserviceDialogFactory().showWebserviceIOEditAttributes(null, 
				"plugins.weblicht.webserviceEditView.dialogs.addOutput.title",  //$NON-NLS-1$
				"plugins.weblicht.webserviceEditView.dialogs.addOutput.message",  //$NON-NLS-1$
				null, null);
		
		if (wio==null){
			return;
		}
		
		//empty attributename will fail later / not allowed
		if (wio.getAttributename().equals("")){ //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showWarning(null,
					"plugins.weblicht.weblichtEditView.dialogs.emptyAttributename.title", //$NON-NLS-1$
					"plugins.weblicht.weblichtEditView.dialogs.emptyAttributename.message", //$NON-NLS-1$
					null,null);
			return;
		}
		
		//System.out.println(wio.getAttributename() + " " + wio.getAttributevalues());
		outputTableModel.setOutputAttributes(wio, -1);
		
	}
	
	protected void editWebserviceOutput(String attribute, String attributevalue,int index) {
		WebserviceIOAttributes wio = WebserviceDialogs.getWebserviceDialogFactory().showWebserviceIOEditAttributes(null, 
				"plugins.weblicht.webserviceEditView.dialogs.editOutput.title",  //$NON-NLS-1$
				"plugins.weblicht.webserviceEditView.dialogs.editOutput.message",  //$NON-NLS-1$
				attribute, attributevalue, attribute);
		
		//System.out.println(wio.getAttributename() + " " + wio.getAttributevalues());
		
		//empty attributename will fail later / not allowed
		if (wio.getAttributename().equals("")){ //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showWarning(null,
					"plugins.weblicht.weblichtEditView.dialogs.emptyAttributename.title", //$NON-NLS-1$
					"plugins.weblicht.weblichtEditView.dialogs.emptyAttributename.message", //$NON-NLS-1$
					null,null);
			return;
		}
		
		outputTableModel.setOutputAttributes(wio,index);

	}
	
	protected void deleteWebserviceOutput(String attribute, int itemIndex) {
		if(DialogFactory.getGlobalFactory().showConfirm(null, 
				"plugins.weblicht.webserviceEditView.dialogs.deleteOutput.title",  //$NON-NLS-1$
				"plugins.weblicht.webserviceEditView.dialogs.deleteOutput.message",  //$NON-NLS-1$
				attribute)) {
			outputTableModel.deleteOutputAttribute(itemIndex);
		}
	}
	
	
	
	protected class Handler extends MouseAdapter implements ActionListener, 
	EventListener, ListSelectionListener {

		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			resetEdit();
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			// Add Input / output
			if(e.getSource()==webserviceInputAddButton 
					||  e.getSource()==webserviceOutputAddButton) {
				
				try {
					if (e.getSource()==webserviceInputAddButton) {
						addWebserviceInput(getWebserviceName());
					}
					else if (e.getSource()==webserviceOutputAddButton) {
						addWebserviceOutput(getWebserviceName());
					}

				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to add attribute to webservice "+getWebserviceName(), ex); //$NON-NLS-1$
				}
				return;
			}
			
			// edit Input
			if(e.getSource()==webserviceInputEditButton) {
				
				int row = inputTable.getSelectedRow();
				if(row==-1) {
					return;
				}
				
				String attribute = (String) inputTable.getValueAt(row, 0);
				String attributevalue = (String) inputTable.getValueAt(row, 1);
				
				try {
					editWebserviceInput(attribute,attributevalue,row);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to edit input attribute from webservice "+getWebserviceName(), ex); //$NON-NLS-1$
				}
				return;
			}			
			
			// delete Input
			if(e.getSource()==webserviceInputRemoveButton) {
				
				int row = inputTable.getSelectedRow();
				if(row==-1) {
					return;
				}

				String attribute = (String) inputTable.getValueAt(row, 0);
				
				//System.out.println("DelRow " + inputTableModel.getRowCount() + " " + attribute);
				
				try {
					deleteWebserviceInput(attribute, row);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to delete attribute from webservice "+getWebserviceName(), ex); //$NON-NLS-1$
				}
				return;
			}
			
			
			// edit Output
			if(e.getSource()==webserviceOutputEditButton) {
				
				int row = outputTable.getSelectedRow();
				if(row==-1) {
					return;
				}
				
				String attribute = (String) outputTable.getValueAt(row, 0);
				String attributevalue = (String) outputTable.getValueAt(row, 1);
				
				try {
					editWebserviceOutput(attribute,attributevalue,row);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to edit output attribute from webservice "+getWebserviceName(), ex); //$NON-NLS-1$
				}
				return;
			}
			
			// delete Output
			if(e.getSource()==webserviceOutputRemoveButton) {
				
				int row = outputTable.getSelectedRow();
				if(row==-1) {
					return;
				}

				String attribute = (String) outputTable.getValueAt(row, 0);
				
				//System.out.println("DelRow " + outputTableModel.getRowCount() + " " + attribute);
				
				try {
					deleteWebserviceOutput(attribute, row);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to delete output attribute from webservice "+getWebserviceName(), ex); //$NON-NLS-1$
				}
				return;
			}
			
			
			
		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			
			boolean enabled = e.getFirstIndex()>-1;
			
			if (e.getSource().equals(inputTable.getSelectionModel())){
				webserviceInputAddButton.setEnabled(enabled);
				webserviceInputEditButton.setEnabled(enabled);
				webserviceInputRemoveButton.setEnabled(enabled);
			}
			
			if (e.getSource().equals(outputTable.getSelectionModel())){			
				webserviceOutputAddButton.setEnabled(enabled);
				webserviceOutputEditButton.setEnabled(enabled);
				webserviceOutputRemoveButton.setEnabled(enabled);
			}
			
		}
	}

}
