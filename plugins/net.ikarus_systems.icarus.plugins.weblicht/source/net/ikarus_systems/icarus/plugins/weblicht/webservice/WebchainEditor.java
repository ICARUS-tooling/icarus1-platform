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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
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
		gbc.fill = GridBagConstraints.HORIZONTAL;
		//panel.add(webserviceTable.getTableHeader(), gbc);

		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 100;
		gbc.weightx = 100;
		//panel.add(webserviceTable, gbc);
		panel.add(webservicesList,gbc);
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

		}
	}
	
	//Action Operation
		protected void addWebservice() {
			
			List<Webservice> ws = new ArrayList<>(webserviceListModel.getWebservicesFromChain(webchain));
			List<String> wsQuery = WebchainRegistry.getInstance().getQueryFromWebchain(webchain);
			
			String uniqueWS = WebserviceDialogs.getWebserviceDialogFactory().showWebserviceCooserDialog(null, 
					"plugins.weblicht.weblichtChainView.dialogs.addWebservice.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtChainView.dialogs.addWebservice.message",  //$NON-NLS-1$
					ws, wsQuery, null);
			
			// Cancelled by user
			if(uniqueWS==null) {
				return;
			}			
			
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
			
			protected List<Webservice> getWebservicesFromChain(Webchain webchain){
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
					webservices = new ArrayList<>(getWebservicesFromChain(webchain));
				}
				
				/*
				System.out.println("WebchainEditorReload: "
						+ webchain.getName()
						+ getWebservicesFromChain(webchain) + " " + 
						webservices.size());
				*/
				fireContentsChanged(webchain, 0, webservices.size());
			}
		
			/**
			 * @see javax.swing.ListModel#getElementAt(int)
			 */
			@Override
			public Object getElementAt(int index) {
				return webservices==null ? null : "UniqueID: " + webservices.get(index).getUID() //$NON-NLS-1$
											+ " Service Name: " + webservices.get(index).getName(); //$NON-NLS-1$
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
