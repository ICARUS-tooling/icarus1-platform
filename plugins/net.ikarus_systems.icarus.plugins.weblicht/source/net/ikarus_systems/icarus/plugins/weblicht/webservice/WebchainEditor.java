/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.weblicht.webservice;

import java.awt.Component;
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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.weblicht.WebserviceDialogs;
import net.ikarus_systems.icarus.resources.ResourceDomain;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.GridBagUtil;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.ui.helper.Editor;

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
	
	// One file chooser should be enough for all editor instances
	protected static JFileChooser locationChooser;
	
	protected JRadioButton webserviceStaticInput;
	protected JRadioButton webserviceLocationInput;
	protected JRadioButton webserviceDynamicInput;
	protected ButtonGroup webserviceInputGroup;
	protected ButtonModel latestGroupSelection;
	protected JTextArea webserviceInputArea;
	
	
	protected JList<Object> webservicesList;
	protected WebserviceListModel webserviceListModel;
	//protected JTable webserviceTable;
	//protected WebserviceTableModel webserviceTableModel;
	
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
		JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
		buttonPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
		buttonPanel.add(webserviceAddButton);
		buttonPanel.add(webserviceRemoveButton);
		panel.add(buttonPanel, gbc);
		
		
		gbc.gridx++;
		gbc.gridheight = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 100;
		gbc.weightx = 100;
		panel.add(webservicesList,gbc);
		
		GridBagConstraints gbcInput = new GridBagConstraints();
		gbcInput.gridx = 0;
		gbcInput.gridy = gbc.gridy+1;
		panel.add(Box.createVerticalStrut(10), gbcInput);

		gbcInput.gridy++;
		gbcInput.gridheight = GridBagConstraints.REMAINDER;
		gbcInput.anchor = GridBagConstraints.NORTH;
		JPanel inputPanel = new JPanel(new GridLayout(3, 2));
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
		panel.add(inputPanel,gbcInput);
		
		gbcInput.gridx++;
		gbcInput.gridy++;
		gbcInput.gridheight = 1;
		gbcInput.fill = GridBagConstraints.BOTH;
		gbcInput.weighty = 100;
		gbcInput.weightx = 100;
	
		panel.add(webserviceInputArea,gbcInput);

		
		
		
	}
	
	protected Handler createHandler() {
		return new Handler();
	}
	

	protected void init() {
		localizedComponents = new ArrayList<>();
		
		handler = createHandler();
		
		nameInput = new JTextField(30);
		
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		
		//Add- /Remove- /Edit-Button
		webserviceAddButton = new JButton();
		webserviceAddButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("add_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceAddButton, "add", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceAddButton);
		localizedComponents.add(webserviceAddButton);
		webserviceAddButton.addActionListener(handler);
		
		webserviceRemoveButton = new JButton();
		webserviceRemoveButton.setIcon(IconRegistry.getGlobalRegistry().getIcon("delete_obj.gif")); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceRemoveButton, "delete", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceRemoveButton);
		localizedComponents.add(webserviceRemoveButton);
		webserviceRemoveButton.addActionListener(handler);	
		
		
		webserviceListModel = new WebserviceListModel();
		webservicesList = new JList<Object>(webserviceListModel);
		webservicesList.setBorder(UIUtil.defaultContentBorder);
		webservicesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		webservicesList.getSelectionModel().addListSelectionListener(handler);
		webservicesList.addMouseListener(handler);		
		
		
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
		webserviceInputArea = new JTextArea();
		webserviceInputArea.setBorder(UIUtil.defaultContentBorder);
		
		WebchainRegistry.getInstance().addListener(Events.REMOVED, handler);
		WebchainRegistry.getInstance().addListener(Events.CHANGED, handler);
	}
	
	protected String getWebchainName() {
		return webchain==null ? "<undefined>" : webchain.getName(); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#getEditorComponent()
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
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#setEditingItem(java.lang.Object)
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
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#getEditingItem()
	 */
	@Override
	public Webchain getEditingItem() {
		return webchain;
	}

	protected void refreshWebchainActions() {
		boolean enabled = webservicesList.getSelectedIndex()!=-1;
		webserviceRemoveButton.setEnabled(enabled);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#resetEdit()
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
		webserviceListModel.reload();
		refreshWebchainActions();
		
		latestGroupSelection = getSelectedInputType();
		
		webserviceInputGroup.setSelected(latestGroupSelection, true);
		
		webserviceInputArea.setEnabled(true);

		
	}
	
	private ButtonModel getSelectedInputType(){
		ButtonModel bm = null;
		if (webchain.getWebchainInputType().getInputType().equals("static")){ //$NON-NLS-1$
			bm = webserviceStaticInput.getModel();
		}
		if (webchain.getWebchainInputType().getInputType().equals("dynamic")){ //$NON-NLS-1$
			bm = webserviceDynamicInput.getModel();
		}
		if (webchain.getWebchainInputType().getInputType().equals("location")){ //$NON-NLS-1$
			bm = webserviceLocationInput.getModel();
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
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#applyEdit()
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
		

		// Replace the old set of webchains
		WebchainRegistry.getInstance().setWebservices(webchain, webserviceListModel.webservices);
		
		
		// Save InputType
		WebchainRegistry.getInstance().setWebserviceInput(webchain,
					getNameFromSelectedButton(),
					webserviceInputArea.getText());
	}
	

	/**
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#hasChanges()
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
		List<Webservice> webservices= webserviceListModel.webservices;
		if(webservices.size() != webchain.getWebserviceCount()) {
			return true;
		}
		

		for(int i = 0; i < webservices.size(); i++) {				
			if ((webservices.get(i)).equals(webchain.getWebserviceAt(i))){
				return true;
			}
		}
		
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
		
		
		return false;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.helper.Editor#close()
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
		 * @see net.ikarus_systems.icarus.ui.events.EventListener#invoke(java.lang.Object, net.ikarus_systems.icarus.ui.events.EventObject)
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
			webserviceRemoveButton.setEnabled(enabled);			
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			
			// Add webservice
			if(e.getSource()==webserviceAddButton) {
				
				try {
					addWebservice();
				} catch(Exception ex) {
					LoggerFactory.getLogger(WebchainEditor.class).log(LoggerFactory.record(Level.SEVERE, 
							"Failed to add webservice to webchain "+getWebchainName(), ex)); //$NON-NLS-1$
				}
				return;
			}
			
			// Remove Webservice
			if(e.getSource()==webserviceRemoveButton) {
				int index = webservicesList.getSelectedIndex();
				if(index==-1) {
					return;
				}
				Webservice key = webserviceListModel.getKey(index);
				if(key==null) {
					return;
				}
				
				try {
					removeWebservice(key);
				} catch(Exception ex) {
					LoggerFactory.getLogger(WebchainEditor.class).log(LoggerFactory.record(Level.SEVERE, 
							"Failed to remove webservice from webchain "+getWebchainName(), ex)); //$NON-NLS-1$
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
		protected void addWebservice() {
			
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
					webserviceListModel.webservices, null);
			
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

			webserviceListModel.addWebservice(webservice);		
			 
		}
		
		protected void removeWebservice(Webservice webservice) {
			if(DialogFactory.getGlobalFactory().showConfirm(null, 
					"plugins.weblicht.weblichtEditView.dialogs.deleteWebservice.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtEditView.dialogs.deleteWebservice.message",  //$NON-NLS-1$
					webservice)) {
				webserviceListModel.removeWebservice(webservice);				
			}			
		}
		
		
		protected class WebserviceListModel extends AbstractListModel<Object>{
				
			private static final long serialVersionUID = -8873909135748882977L;
			
			protected List<Webservice> webservices;
			
			//TODO change to webchainelements
			
			protected List<Webservice> getWebelementsFromChain(Webchain webchain){
				List<Webservice> services = new ArrayList<>();
				
				for (int i = 0; i< webchain.getWebserviceCount();i++){
						services.add(webchain.getWebserviceAt(i).get());					
				}
				return services;
			}
			
			protected void reload() {
				if(webchain==null) {
					webservices = null;
				} else {
					webservices = new ArrayList<>(getWebelementsFromChain(webchain));
				}
				
				/*
				System.out.println("WebchainEditorReload: "
						+ webchain.getName()
						+ getWebservicesFromChain(webchain) + " " + 
						webservices.size());
				*/
				fireContentsChanged(webchain, 0, webservices.size());
			}
			
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
		
			/**
			 * @see javax.swing.ListModel#getElementAt(int)
			 */
			@Override
			public Object getElementAt(int index) {
				return webservices==null ? null : buildListViewString(index);
			}
		
			/**
			 * @param serviceID
			 * @return
			 */
			protected String getValue(String serviceID) {
				for(int i = 0; i < webservices.size();i++){
					if(webservices.get(i).getServiceID().equals(serviceID)){
						return String.valueOf(webservices.get(i));
					}
				}
				return serviceID;
			}
		
			
			/**
			 * @param key
			 */
			protected void removeWebservice(Webservice webservice) {
				int removedindex = webservices.indexOf(webservice);				
				
				//System.out.println(removedindex + " "+ webservices.size());
				
				//Check if we are removing the last item.
				if (removedindex == webservices.size()-1){
					webservices.remove(webservice);
					fireIntervalRemoved(webservice, removedindex, removedindex);
				}
				
				//remove multiwebservices
				else {
					int removedtil = webservices.size()-1;
					for (int i = removedtil; i >= removedindex; i--) {
						webservices.remove(webservices.get(i));
					}	
					//System.out.println("Removed From " +  removedindex + " " + removedtil);
					fireIntervalRemoved(webservice, removedindex, removedtil);
				}

			}
			
			
			/**
			 * @param key
			 */
			protected void addWebservice(Webservice webservice) {
				webservices.add(webservice);
				int newindex = webservices.indexOf(webservice);
				fireIntervalAdded(webservice, newindex, newindex);
				//System.out.println("NewVal set " + webservice);
			}
		
			/**
			 * @param index
			 * @return
			 */
			protected Webservice getKey(int index) {
				return webservices==null ? null : webservices.get(index);
			}
		
			/**
			 * @see javax.swing.ListModel#getSize()
			 */
			@Override
			public int getSize() {
				return webservices.size();
			}
		
		}
	}
