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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.weblicht.WebserviceDialogs;
import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.GridBagUtil;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.dialog.BasicDialogBuilder;
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
public class WebchainEditor implements Editor<Webchain> {
	
	protected JPanel contentPanel;
	
	protected Collection<JComponent> localizedComponents;
	
	protected JTextField nameInput;
	
	protected JButton webserviceAddButton;
	protected JButton webserviceRemoveButton;	
	protected JButton ioEditButton;
	
	// One file chooser should be enough for all editor instances
	protected static JFileChooser locationChooser;
	
	protected JButton addOutputButton;
	protected JButton changeOutputUsedStatusButton;
	
	protected JRadioButton webserviceStaticInput;
	protected JRadioButton webserviceLocationInput;
	protected JRadioButton webserviceDynamicInput;
	protected ButtonGroup  webserviceInputGroup;
	protected ButtonModel  latestGroupSelection;
	protected JTextArea	   webserviceInputArea;
	protected JPanel 	   inputPanel;
	
	
	protected JList<Object> webchainElementList;
	protected WebserviceListModel webchainElementListModel;
	
	protected Webchain webchain;
	protected Handler handler;
	
	public WebchainEditor() {
		//noop
	}
	
	
	protected int feedBasicComponents(JPanel panel) {
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		JLabel label;
		
		GridBagConstraints gbc = GridBagUtil.makeGbc(0, 0);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(1, 2, 1, 2);
		
		// Webchain Name edit Field 
		label = new JLabel();
		resourceDomain.prepareComponent(label, 
				"plugins.weblicht.labels.webchain.name", null); //$NON-NLS-1$
		resourceDomain.addComponent(label);
		localizedComponents.add(label);
		panel.add(label, gbc);
		gbc.gridx++;
		panel.add(nameInput, gbc);
		
		panel.add(Box.createGlue(), GridBagUtil.makeGbcH(gbc.gridx+1, gbc.gridy, 
				GridBagConstraints.REMAINDER, 1));
		
		return ++gbc.gridy;
	}
	
	protected void feedEditorComponent(JPanel panel) {
		panel.setLayout(new GridBagLayout());
		
		int row = feedBasicComponents(panel);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = row;
		panel.add(Box.createVerticalStrut(10), gbc);
		
		

		gbc.gridy++;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.NORTH;
		
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbcButton = new GridBagConstraints();
		gbcButton.gridx=0;
		gbcButton.gridy=0;
		gbcButton.fill = GridBagConstraints.HORIZONTAL;
		gbcButton.anchor = GridBagConstraints.CENTER;
		gbcButton.insets = new Insets(0,2,2,2);
		
		buttonPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
		buttonPanel.add(webserviceAddButton, gbcButton);
		gbcButton.gridy++;
		buttonPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbcButton);
		gbcButton.gridy++;
		buttonPanel.add(addOutputButton, gbcButton);
		gbcButton.gridy++;
		buttonPanel.add(ioEditButton, gbcButton);
		gbcButton.gridy++;
		buttonPanel.add(changeOutputUsedStatusButton, gbcButton);
		gbcButton.gridy++;
		buttonPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbcButton);
		gbcButton.gridy++;
		buttonPanel.add(webserviceRemoveButton, gbcButton);
		

		panel.add(buttonPanel, gbc);		
		
		gbc.gridx++;
		gbc.gridheight = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 100;
		gbc.weightx = 100;
		panel.add(webchainElementList,gbc);
		
		//TODO move to init section
		//Input Type Stuff
		
		GridBagConstraints gbcInput = new GridBagConstraints();
		gbcInput.gridx = 0;
		gbcInput.gridy = gbc.gridy+1;
		panel.add(Box.createVerticalStrut(10), gbcInput);

				
		gbcInput.gridy++;
		gbcInput.gridheight = GridBagConstraints.REMAINDER;
		gbcInput.anchor = GridBagConstraints.NORTH;
		inputPanel = new JPanel(new GridLayout(3, 2));
		inputPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
	    		
		webserviceInputGroup.add(webserviceStaticInput);
		webserviceInputGroup.add(webserviceLocationInput);
		webserviceInputGroup.add(webserviceDynamicInput);
		JLabel staticImage = new JLabel(IconRegistry.getGlobalRegistry().getIcon("addrepo_rep.gif")); //$NON-NLS-1$
	    inputPanel.add(staticImage);
		inputPanel.add(webserviceStaticInput);
		
		JLabel locationImage = new JLabel(IconRegistry.getGlobalRegistry().getIcon("history_rep.gif")); //$NON-NLS-1$
	    inputPanel.add(locationImage);
		inputPanel.add(webserviceLocationInput);
		
		JLabel dynamicImage = new JLabel(IconRegistry.getGlobalRegistry().getIcon("newconnect_wiz.gif")); //$NON-NLS-1$
	    inputPanel.add(dynamicImage);
		inputPanel.add(webserviceDynamicInput);
		//panel.add(inputPanel,gbcInput);
		
		gbcInput.gridx++;
		gbcInput.gridy++;
		gbcInput.gridheight = 1;
		gbcInput.fill = GridBagConstraints.BOTH;
		gbcInput.weighty = 100;
		gbcInput.weightx = 100;
	
		//panel.add(webserviceInputArea,gbcInput);

		
		
		
	}
	
	protected Handler createHandler() {
		return new Handler();
	}
	

	protected void init() {
		localizedComponents = new ArrayList<>();
		
		handler = createHandler();
		
		nameInput = new JTextField(30);
		
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		
		//Add- /Remove-
		webserviceAddButton = new JButton();
		webserviceAddButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("add_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceAddButton, "addWebservice", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceAddButton);
		localizedComponents.add(webserviceAddButton);
		webserviceAddButton.addActionListener(handler);
		
		webserviceRemoveButton = new JButton();
		webserviceRemoveButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("delete_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceRemoveButton, "delete", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceRemoveButton);
		localizedComponents.add(webserviceRemoveButton);
		webserviceRemoveButton.addActionListener(handler);	
		
		
		//AddIO- /Activate /Edit-Button
		addOutputButton = new JButton();
		addOutputButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("add_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(addOutputButton, "addOutput", null); //$NON-NLS-1$
		resourceDomain.addComponent(addOutputButton);
		localizedComponents.add(addOutputButton);
		addOutputButton.addActionListener(handler);
				
		
		changeOutputUsedStatusButton = new JButton();
		changeOutputUsedStatusButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("outrepo_rep.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(changeOutputUsedStatusButton, "changeStatus", null); //$NON-NLS-1$
		resourceDomain.addComponent(changeOutputUsedStatusButton);
		localizedComponents.add(changeOutputUsedStatusButton);
		changeOutputUsedStatusButton.addActionListener(handler);
		
		
		ioEditButton = new JButton();
		ioEditButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("newconnect_wiz.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(ioEditButton, "editIO", null); //$NON-NLS-1$
		resourceDomain.addComponent(ioEditButton);
		localizedComponents.add(ioEditButton);
		ioEditButton.addActionListener(handler);	
		
		
		webchainElementListModel = new WebserviceListModel();
		webchainElementList = new JList<Object>(webchainElementListModel);
		webchainElementList.setBorder(UIUtil.defaultContentBorder);
		webchainElementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		webchainElementList.getSelectionModel().addListSelectionListener(handler);
		webchainElementList.addMouseListener(handler);		
		
		
		//input Buttons
		webserviceStaticInput = new JRadioButton();
		webserviceStaticInput.setName("static"); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceStaticInput, "static", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceStaticInput);
		localizedComponents.add(webserviceStaticInput);
		webserviceStaticInput.addActionListener(handler);	
		
		webserviceLocationInput = new JRadioButton();
		webserviceLocationInput.setName("location"); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceLocationInput, "location", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceLocationInput);
		localizedComponents.add(webserviceLocationInput);
		webserviceLocationInput.addActionListener(handler);	
		
		webserviceDynamicInput = new JRadioButton();
		webserviceDynamicInput.setName("dynamic"); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceDynamicInput, "dynamic", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceDynamicInput);
		localizedComponents.add(webserviceDynamicInput);
		webserviceDynamicInput.addActionListener(handler);
		
		
		webserviceInputGroup = new ButtonGroup();
		webserviceInputArea = new JTextArea() {

			private static final long serialVersionUID = 24354678798765432L;

			/**
			 * @see javax.swing.JTextArea#getScrollableTracksViewportWidth()
			 */
			@Override
			public boolean getScrollableTracksViewportWidth() {
				return true;
			}
			
		};
		webserviceInputArea.setLineWrap(true);
		webserviceInputArea.setWrapStyleWord(true);
		webserviceInputArea.setBorder(UIUtil.defaultContentBorder);
		JScrollPane scrollPane = new JScrollPane(webserviceInputArea);
		scrollPane.setBorder(UIUtil.defaultAreaBorder);
		scrollPane.setPreferredSize(new Dimension(400, 150));
		
		WebchainRegistry.getInstance().addListener(Events.REMOVED, handler);
		WebchainRegistry.getInstance().addListener(Events.CHANGED, handler);
	}
	
	protected String getWebchainName() {
		return webchain==null ? "<undefined>" : webchain.getName(); //$NON-NLS-1$
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
	public void setEditingItem(Webchain webchain) {
		if(this.webchain==webchain) {
			return;
		}
		
		this.webchain = webchain;
		
		resetEdit();		
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#getEditingItem()
	 */
	@Override
	public Webchain getEditingItem() {
		return webchain;
	}

	protected void refreshWebchainActions() {
		boolean enabled = webchainElementList.getSelectedIndex()!=-1;
		webserviceRemoveButton.setEnabled(enabled);
		ioEditButton.setEnabled(enabled);		
		changeOutputUsedStatusButton.setEnabled(enabled);
		
		
		int lastitem = webchainElementListModel.getSize();
		//TODO fixme at least input + one webservice needed to create output
		//System.out.println(lastitem);
		if (lastitem > 1) {
			WebchainElements element = webchainElementListModel.getKey(lastitem-1);	
			if(element instanceof WebchainOutputType) {
				addOutputButton.setEnabled(false);
			}else {
				addOutputButton.setEnabled(true);
			}
		}else {
				addOutputButton.setEnabled(false);
		}

	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#resetEdit()
	 */
	@Override
	public void resetEdit() {
		if(contentPanel==null) {
			return;
		}
		if(webchain==null) {
			nameInput.setText(null);			
			return;
		}
		
		// Name
		nameInput.setText(webchain.getName());
		
		//System.out.println("reset" + webchain.getName());
		
		// Webchains
		webchainElementListModel.reload();
		refreshWebchainActions();
		
//		//remove no longer needed!		
//		latestGroupSelection = getSelectedInputType();		
//		webserviceInputGroup.setSelected(latestGroupSelection, true);		
//		webserviceInputArea.setEnabled(true);
		
		
	}
	
	private ButtonModel getSelectedIOType(WebchainElements element){
		ButtonModel bm = null;
		if (element instanceof WebchainInputType) {
			WebchainInputType wi = (WebchainInputType) element;
			if (wi.getInputType().equals("static")){ //$NON-NLS-1$
				bm = webserviceStaticInput.getModel();
			}
			if (wi.getInputType().equals("dynamic")){ //$NON-NLS-1$
				bm = webserviceDynamicInput.getModel();
			}
			if (wi.getInputType().equals("location")){ //$NON-NLS-1$
				bm = webserviceLocationInput.getModel();
			}
		} else {
			WebchainOutputType wo = (WebchainOutputType) element;
			if (wo.getOutputType().equals("static")){ //$NON-NLS-1$
				bm = webserviceStaticInput.getModel();
			}
			if (wo.getOutputType().equals("dynamic")){ //$NON-NLS-1$
				bm = webserviceDynamicInput.getModel();
			}
			if (wo.getOutputType().equals("location")){ //$NON-NLS-1$
				bm = webserviceLocationInput.getModel();
			}
		}

		return bm;
	}
	
	private String getNameFromSelectedButton(){
		  Enumeration<AbstractButton> allRadioButton = webserviceInputGroup.getElements();  
		   
		  while(allRadioButton.hasMoreElements()) {  
		   JRadioButton temp=(JRadioButton)allRadioButton.nextElement();  
		   if(temp.isSelected()) {
		    return temp.getName();
		   } 
		  }
		  return null;		   
	}
	
	
	protected static JFileChooser getLocationChooser() {
		if(locationChooser==null) {
			locationChooser = new JFileChooser();
			locationChooser.setMultiSelectionEnabled(false);
			// TODO configure file chooser
		}
		
		return locationChooser;
	}
	
	protected boolean openLocationChooser() {
		File file = new File(webserviceInputArea.getText());
		JFileChooser fileChooser = getLocationChooser();
		fileChooser.setSelectedFile(file);
		int result = fileChooser.showDialog(contentPanel, ResourceManager.getInstance().get(
				"select")); //$NON-NLS-1$
		
		if(result==JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			webserviceInputArea.setText(file.getAbsolutePath());
			return true;
		}
		
		return false;
	}


	/**
	 * @see de.ims.icarus.ui.helper.Editor#applyEdit()
	 */
	@Override
	public void applyEdit() {
		if(contentPanel==null) {
			return;
		}
		if(webchain==null) {
			return;
		}
	
		// Name - no unique needed?
		String newName = nameInput.getText();
		WebchainRegistry.getInstance().setName(webchain, newName);
		
		// Replace the old set of Webchainelements with new ones
		WebchainRegistry.getInstance().setWebchainElements(webchain,
								webchainElementListModel.webchainElements);
		

		// Save InputType
//		WebchainRegistry.getInstance().setWebserviceInput(webchain,
//					getNameFromSelectedButton(),
//					webserviceInputArea.getText());		
		
	}
	

	/**
	 * @param webchainElements
	 */
	private void printelement(List<WebchainElements> webchainElements) {
		for ( int i = 0 ; i < webchainElements.size(); i++){
			if (webchainElements.get(i) instanceof WebchainOutputType){
				WebchainOutputType wo = (WebchainOutputType) webchainElements.get(i);
				System.out.print(wo.getOutputType() + " " + wo.getOutputUsed()); //$NON-NLS-1$
			}
		}		
	}


	/**
	 * @see de.ims.icarus.ui.helper.Editor#hasChanges()
	 */
	@Override
	public boolean hasChanges() {
		if(contentPanel==null) {
			return false;
		}
		if(webchain==null) {
			return false;
		}
		
		// Compare name
		if(!nameInput.getText().equals(webchain.getName())) {
			return true;
		}

		
		//Compare Webservices in Webchain		
		List<WebchainElements> webchainElementList = webchainElementListModel.webchainElements;
		if(webchainElementList.size() != webchain.getElementsCount()) {
			return true;
		}

		/*
		for(int i = 0; i < webchainElementList.size(); i++) {				
			if ((webchainElementList.get(i)).equals(webchain.getElementAt(i))){
				return true;
			}
		}
		*/

		if (!(WebchainRegistry.getInstance().equalElements(webchainElementList,
				webchain.webchainElementsList))){
			return true;
			
		}
		
		
		/*
		//compare input 
		if(!webchain.getWebchainInputType().getInputType()
				.equals(getNameFromSelectedButton()) ){
			return true;			
		} 
		//inputtype equal valueschanged
		else {
			if(!webchain.getWebchainInputType().getInputTypeValue()
					.equals(webserviceInputArea.getText())){
				return true;
			}
		}
		*/
		
		
		return false;
	}

	/**
	 * @see de.ims.icarus.ui.helper.Editor#close()
	 */
	@Override
	public void close() {
		WebchainRegistry.getInstance().removeListener(handler);
		
		if(localizedComponents==null) {
			return;
		}
		for(JComponent component : localizedComponents) {
			ResourceManager.getInstance().removeLocalizableItem(component);
		}
		
		webchain = null;
		
	}
	
	
	protected class Handler extends MouseAdapter implements ActionListener, 
	ListSelectionListener, EventListener {

		/**
		 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
		 */
		@Override
		public void invoke(Object sender, EventObject event) {
			resetEdit();			
		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			boolean enabled = e.getFirstIndex()>-1;
			
			WebchainElements we = null;			
			DefaultListSelectionModel dm = (DefaultListSelectionModel) e.getSource();
			
			// -1 when deleted (no selection atm)
			if (dm.getMinSelectionIndex() != -1) we = webchainElementListModel.getKey(dm.getMinSelectionIndex());
			
			//enable edit button for input/output types
			if(we instanceof WebchainInputType || we instanceof WebchainOutputType){
				ioEditButton.setEnabled(enabled);
			} else {
				ioEditButton.setEnabled(!enabled);
			}
			
			if(we instanceof WebchainOutputType){
				changeOutputUsedStatusButton.setEnabled(enabled);
			} else {
				changeOutputUsedStatusButton.setEnabled(!enabled);
			}
			
			//removebutton only allowed for services and outputtypes
			if (!(we instanceof WebchainInputType)){
				webserviceRemoveButton.setEnabled(enabled);				
			} else {
				webserviceRemoveButton.setEnabled(!enabled);		
			}
			
			
			//Add output
			int lastitem = webchainElementListModel.getSize();
			WebchainElements element = webchainElementListModel.getKey(lastitem-1);

			if(element instanceof WebchainOutputType) {
				addOutputButton.setEnabled(false);
			} else {
				addOutputButton.setEnabled(true);
			}
			
			
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			
			// Add webservice
			if(e.getSource()==webserviceAddButton) {
				
				try {
					addWebchainElement();
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to add webservice to webchain "+getWebchainName(), ex); //$NON-NLS-1$
				}
				return;
			}
			
			// Remove Webservice
			if(e.getSource()==webserviceRemoveButton) {
				int index = webchainElementList.getSelectedIndex();
				if(index==-1) {
					return;
				}
				WebchainElements key = webchainElementListModel.getKey(index);
				if(key==null) {
					return;
				}
				
				try {
					removeWebchainElement(key);
				} catch(Exception ex) {
					LoggerFactory.log(this, Level.SEVERE, 
							"Failed to remove webservice from webchain "+getWebchainName(), ex); //$NON-NLS-1$
				}
				return;
			}
			
			
			// Add Output
			if(e.getSource()==addOutputButton) {
				
				try {
					addOutputElement();
				} catch(Exception ex) {
					LoggerFactory.getLogger(WebchainEditor.class).log(LoggerFactory.record(Level.SEVERE, 
							"Failed to add output to webchain "+getWebchainName(), ex)); //$NON-NLS-1$
				}
				return;
			}
			
			if(e.getSource()==changeOutputUsedStatusButton){
				int index = webchainElementList.getSelectedIndex();
				if(index==-1) {
					return;
				}
				WebchainElements key = webchainElementListModel.getKey(index);
				if(key==null) {
					return;
				}
				
				try {
					changeOutputStatus(key, index);
				} catch(Exception ex) {
					LoggerFactory.getLogger(WebchainEditor.class).log(LoggerFactory.record(Level.SEVERE, 
							"Failed to change Output Status from webchain "+getWebchainName(), ex)); //$NON-NLS-1$
				}
				return;
				
			}
			
			
			// Edit IO Types
			if(e.getSource()==ioEditButton) {
				int index = webchainElementList.getSelectedIndex();
				if(index==-1) {
					return;
				}
				WebchainElements key = webchainElementListModel.getKey(index);
				if(key==null) {
					return;
				}
				
				try {
					editIOElement(key, index);
				} catch(Exception ex) {
					LoggerFactory.getLogger(WebchainEditor.class).log(LoggerFactory.record(Level.SEVERE, 
							"Failed to edit IO Element from webchain "+getWebchainName(), ex)); //$NON-NLS-1$
				}
				return;
			}
			
			
			// Add webserviceStaticInput
			if(e.getSource()==webserviceStaticInput) {
				String staticInput = DialogFactory.getGlobalFactory()
						.showTextInputDialog(contentPanel,
						"plugins.weblicht.webchainEditor.staticInput.title", //$NON-NLS-1$
						"plugins.weblicht.webchainEditor.staticInput.message", //$NON-NLS-1$
						webserviceInputArea.getText(),
						webchain.getName());
				
				//user canceled
				if (staticInput == null){
					webserviceInputGroup.setSelected(latestGroupSelection, true);
					return;
				}
				
				latestGroupSelection = webserviceInputGroup.getSelection();
				webserviceInputArea.setText(staticInput);
				webserviceInputArea.setEnabled(true);

			}
			
			// Add webserviceLocationInput
			if(e.getSource()==webserviceLocationInput) {
				if (openLocationChooser()){
					webserviceInputArea.setEnabled(true);
					latestGroupSelection = webserviceInputGroup.getSelection();
				}
				else {
					//back to latest selection
					webserviceInputGroup.setSelected(latestGroupSelection, true);
				}
				


			}
			
			// Add webserviceDynamicInput
			if(e.getSource()==webserviceDynamicInput) {
				//switch to dynamic mode only if accepted
				if (DialogFactory.getGlobalFactory().showConfirm(contentPanel,
						"plugins.weblicht.webchainEditor.dynamicInput.title", //$NON-NLS-1$
						"plugins.weblicht.webchainEditor.dynamicInput.message", //$NON-NLS-1$
						webchain.getName())) {
					/*
					webserviceInputArea.setText(
							ResourceManager.getInstance().get(
									"plugins.weblicht.webchainEditor.dynamicInput.mode")); //$NON-NLS-1$
					*/
					webserviceInputArea.setEnabled(false);
					latestGroupSelection = webserviceInputGroup.getSelection();
				} else {
					//back to latest selection
					webserviceInputGroup.setSelected(latestGroupSelection, true);
				}

			}

		}
	}
	
	//Action Operation
		protected void addWebchainElement() {
			
			//List<String> wsQuery = WebchainRegistry.getInstance().getQueryFromWebchain(webchain);
			
			/*
			String uniqueWS = WebserviceDialogs.getWebserviceDialogFactory().showWebserviceChooserDialog(null, 
					"plugins.weblicht.weblichtChainView.dialogs.addWebservice.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtChainView.dialogs.addWebservice.message",  //$NON-NLS-1$
					ws, wsQuery, null);
			
			*/
			
			String uniqueWS = WebserviceDialogs.getWebserviceDialogFactory().showWebserviceChooserDialogReworked(null, 
					"plugins.weblicht.weblichtChainView.dialogs.addWebservice.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtChainView.dialogs.addWebservice.message",  //$NON-NLS-1$
					webchainElementListModel.webchainElements, null);
			
			//System.out.println(webserviceListModel.webservices);
			
			// Cancelled by user
			if(uniqueWS==null) {
				return;
			}			
			
			/* no more needed filter before webservice can be added implemented
			//Cancelled service already in chain
			for (int i = 0; i < ws.size(); i++){
				if (ws.get(i).getUID().equals(uniqueWS)){
					DialogFactory.getGlobalFactory().showError(null,
							"plugins.weblicht.weblichtChainView.dialogs.serviceAlreadyInChain.title", //$NON-NLS-1$
							"plugins.weblicht.weblichtChainView.dialogs.serviceAlreadyInChain.message", //$NON-NLS-1$
							WebserviceRegistry.getInstance().getNameFromUniqueID(uniqueWS),webchain);
					return;
				}
			}	
			*/		
			
			Webservice webservice = WebserviceRegistry.getInstance().getWebserviceFromUniqueID(uniqueWS);
			WebserviceProxy wsp = new WebserviceProxy(webservice.getUID());
			webchainElementListModel.addWebservice((WebchainElements) wsp);		
			 
		}
		
		protected void addOutputElement(){
			
			WebchainElements element = showAddOutputElements(null, 
					"plugins.weblicht.weblichtChainView.dialogs.editIO.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtChainView.dialogs.editIO.message",  //$NON-NLS-1$
					inputPanel, webserviceInputArea, null);

			// Cancelled by user
			if(element==null) {
				return;
			}
			webchainElementListModel.addWebservice(element);
			
			
		}
		
		/**
		 * Remove Elements from List.
		 * @param chainelement
		 */
		protected void removeWebchainElement(WebchainElements chainelement) {
			if (chainelement instanceof WebserviceProxy){
				if(DialogFactory.getGlobalFactory().showConfirm(null, 
						"plugins.weblicht.weblichtEditView.dialogs.deleteWebservice.title",  //$NON-NLS-1$
						"plugins.weblicht.weblichtEditView.dialogs.deleteWebservice.message",  //$NON-NLS-1$
						chainelement)) {
					webchainElementListModel.removeWebservice(chainelement);				
				}
			} else
				if(DialogFactory.getGlobalFactory().showConfirm(null, 
						"plugins.weblicht.weblichtEditView.dialogs.deleteWebservice.title",  //$NON-NLS-1$
						"plugins.weblicht.weblichtEditView.dialogs.deleteOutput.message",  //$NON-NLS-1$
						((WebchainOutputType)chainelement).getOutputType())) {
					webchainElementListModel.removeWebservice(chainelement);
			}
		}
		
		protected void changeOutputStatus(WebchainElements chainelement, int index){			
			
			WebchainOutputType wo = (WebchainOutputType) chainelement;
			boolean changOutputStatus = wo.getIsOutputUsed();
			wo.setOutputUsed(!changOutputStatus);
			
			webchainElementListModel.editElement(chainelement, index);	
			printelement(webchainElementListModel.webchainElements);
		}
		
		
		/**
		 * edit Input/Output Elements
		 * @param chainelement
		 * @param index 
		 */
		protected void editIOElement(WebchainElements chainelement, int index){
			
			//System.out.println("Selectedindex: " +  index);
			
			WebchainElements element = showEditIOElements(null, 
					"plugins.weblicht.weblichtChainView.dialogs.editIO.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtChainView.dialogs.editIO.message",  //$NON-NLS-1$
					chainelement, inputPanel, webserviceInputArea, null);

			// Cancelled by user
			if(element==null) {
				return;
			}
			
			
			webchainElementListModel.editElement(element, index);
		}
		
		
		private WebchainElements showEditIOElements(Component parent, String title, 
				String message, WebchainElements chainelement, Object...params) {		

			BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory.getGlobalFactory()
																	.getResourceDomain());
						
			latestGroupSelection = getSelectedIOType(chainelement);
			
			webserviceInputGroup.setSelected(latestGroupSelection, true);
			
			if(chainelement instanceof WebchainInputType){
				builder.addMessage("plugins.weblicht.labels.webservice.WebchainInput"); //$NON-NLS-1$
				WebchainInputType wi = (WebchainInputType) chainelement;
				webserviceInputArea.setText(wi.getInputTypeValue());
			}
			
			if(chainelement instanceof WebchainOutputType){
				WebchainOutputType wo = (WebchainOutputType) chainelement;
				builder.addMessage("plugins.weblicht.labels.webservice.WebchainOutput"); //$NON-NLS-1$
				webserviceInputArea.setText(wo.getOutputTypeValue());
			}
			
			builder.setTitle(title);
			builder.addMessage(inputPanel);
			builder.addMessage(SwingUtilities.getAncestorOfClass(JScrollPane.class, webserviceInputArea));

			builder.setPlainType();
			builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
			
			builder.showDialog(parent);
			
			return builder.isYesValue() ? editedIOField(chainelement) : null;
		}
		
		
		
		private WebchainElements showAddOutputElements(Component parent, String title, 
				String message, Object...params) {		

			BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory.getGlobalFactory()
																	.getResourceDomain());
			
			WebchainElements element = (WebchainElements) new WebchainOutputType();
			
			builder.setTitle(title);
			builder.addMessage(inputPanel);
			builder.addMessage(SwingUtilities.getAncestorOfClass(JScrollPane.class, webserviceInputArea));

			builder.setPlainType();
			builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
			
			builder.showDialog(parent);
			
			return builder.isYesValue() ? editedIOField(element) : null;
		}
		
		
		/**
		 * @return
		 */
		private WebchainElements editedIOField(WebchainElements chainelement) {
			WebchainElements wes = null;
			if(chainelement instanceof WebchainInputType){				
				WebchainInputType wi = new WebchainInputType();
				wi.setInputType(getNameFromSelectedButton());
				wi.setInputTypeValue(webserviceInputArea.getText());
				wes = (WebchainElements) wi;
				
			}
			
			if(chainelement instanceof WebchainOutputType){
				WebchainOutputType wo = new WebchainOutputType();
				wo.setOutputType(getNameFromSelectedButton());
				wo.setOutputTypeValue(webserviceInputArea.getText());
				wes = (WebchainElements) wo;
			}
			return wes;
		}


		protected class WebserviceListModel extends AbstractListModel<Object>{
				
			private static final long serialVersionUID = -8873909135748882977L;
			
			protected List<WebchainElements> webchainElements;			
			
			protected List<WebchainElements> getWebelementsFromChain(Webchain webchain){
				List<WebchainElements> elements = new ArrayList<>();
				
				for (int i = 0; i< webchain.getElementsCount();i++){
						elements.add(webchain.getElementAt(i));					
				}
				return elements;
			}
			
			protected void reload() {
				if(webchain==null) {
					webchainElements = null;
				} else {
					webchainElements = new ArrayList<>(getWebelementsFromChain(webchain));
				}
				
				/*
				System.out.println("WebchainEditorReload: "
						+ webchain.getName()
						+ getWebservicesFromChain(webchain) + " " + 
						webservices.size());
				*/
				fireContentsChanged(webchain, 0, webchainElements.size());
			}
			
			/*
			private String buildListViewString(int index){
				StringBuilder sb = new StringBuilder();
				sb.append(ResourceManager.getInstance().get(
						"plugins.weblicht.webchainEditor.serviceName")) //$NON-NLS-1$
					.append(" ") //$NON-NLS-1$
					.append(webservices.get(index).getName())
					.append(" ") //$NON-NLS-1$
					.append(ResourceManager.getInstance().get(
							"plugins.weblicht.webchainEditor.outFormat")) //$NON-NLS-1$
					.append(" ") //$NON-NLS-1$
					.append(webservices.get(index).getOutputText());
				return sb.toString();
			}
			*/
		
			/**
			 * @see javax.swing.ListModel#getElementAt(int)
			 */
			@Override
			public Object getElementAt(int index) {
				//return webservices==null ? null : buildListViewString(index);
				if (webchainElements.get(index) instanceof WebchainInputType){
					return webchainElements==null ? null 
							: ResourceManager.getInstance().get("input")  //$NON-NLS-1$
								+ ((WebchainInputType)webchainElements.get(index))
								.getInputType();
				}
				
				if (webchainElements.get(index) instanceof WebchainOutputType){
					return webchainElements==null ? null 
							: ResourceManager.getInstance().get("output")  //$NON-NLS-1$
								+ ((WebchainOutputType)webchainElements.get(index))
								.getOutputType()
								+" " //$NON-NLS-1$
								+ ResourceManager.getInstance().get(
										((WebchainOutputType)webchainElements.get(index))
										.getOutputUsed());
				}
				
				return webchainElements==null ? null : webchainElements.get(index);
			}
				
			
			/**
			 * @param key
			 */
			protected void removeWebservice(WebchainElements chainelement) {
				int removedindex = webchainElements.indexOf(chainelement);				
				
				//We have to check if we should remove multi items (only for Webserviceproxy
				if (chainelement instanceof WebserviceProxy) {
					//System.out.println(removedindex + " "+ webchainElements.size());
					
					//Check if we are removing the last item.
					if (removedindex == webchainElements.size()-1){
						webchainElements.remove(chainelement);
						fireIntervalRemoved(chainelement, removedindex, removedindex);
					}
					
					//remove multiwebservices
					else {
						int removedtil = webchainElements.size()-1;
						for (int i = removedtil; i >= removedindex; i--) {
							webchainElements.remove(webchainElements.get(i));
						}	
						//System.out.println("Removed From " +  removedindex + " " + removedtil);
						fireIntervalRemoved(chainelement, removedindex, removedtil);
					}
				} else {
					// remove used for webchainoutputtypes, no multi remove needed
					webchainElements.remove(chainelement);
					fireIntervalRemoved(chainelement, removedindex, removedindex);
				}
			
				

			}
			
			
			/**
			 * @param key
			 */
			protected void addWebservice(WebchainElements chainElement) {
				//WebserviceProxy wsp = new WebserviceProxy(chainElement.getUID());
				webchainElements.add(chainElement);
				int newindex = webchainElements.indexOf(chainElement);
				fireIntervalAdded(chainElement, newindex, newindex);
				refreshWebchainActions();
				//System.out.println("NewVal set " + webservice);				
			}
			
			
			/**
			 * @param key
			 */
			protected void editElement(WebchainElements chainElement, int changedIndex) {
				webchainElements.set(changedIndex, chainElement);
				fireContentsChanged(chainElement, changedIndex, changedIndex);
				refreshWebchainActions();
			}
		
			/**
			 * @param index
			 * @return
			 */
			protected WebchainElements getKey(int index) {
				return webchainElements==null ? null : webchainElements.get(index);
			}
		
			/**
			 * @see javax.swing.ListModel#getSize()
			 */
			@Override
			public int getSize() {
				return webchainElements.size();
			}
		
		}
	}
