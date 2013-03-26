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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.ikarus_systems.icarus.plugins.weblicht.webservice.Webchain;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.Webservice;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebserviceIOAttributes;
import net.ikarus_systems.icarus.plugins.weblicht.webservice.WebserviceRegistry;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.dialog.BasicDialogBuilder;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebserviceDialogs {
	


	private static WebserviceDialogs webserviceDialogFactory;
	
	public static WebserviceDialogs getWebserviceDialogFactory() {
		if(webserviceDialogFactory==null) {
			synchronized (DialogFactory.class) {
				if(webserviceDialogFactory==null)
					webserviceDialogFactory= new WebserviceDialogs();
			}
		}
		
		return webserviceDialogFactory;
	}
	
	private Component getContainer(JComponent comp) {
		Object container = comp.getClientProperty("container"); //$NON-NLS-1$
		return container instanceof Component ? (Component)container : comp;
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
		textArea.setPreferredSize(new Dimension(320, 190));
		UIUtil.createUndoSupport(textArea, 75);		
		UIUtil.addPopupMenu(textArea, UIUtil.createDefaultTextMenu(textArea, true));
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		JPanel container = new JPanel(new BorderLayout());
		container.add(scrollPane, BorderLayout.CENTER);
		
		textArea.putClientProperty("container", container);		 //$NON-NLS-1$
		textArea.setText(null);
		textArea.setToolTipText(null);
		
		return textArea;
	}
	
	private void fillRequiredFields(JTextField creator, String input){
		if (input == null || input.equals("")) { //$NON-NLS-1$
			creator.setBorder(BorderFactory.createLineBorder(Color.red));
		} else {
			creator.setText(input);
		}
	}
	
	
	private List<Webservice> filterWebservice(List<Webservice> webservices){
		List<Webservice> filtered = new ArrayList<>();
		int webservicesCount = WebserviceRegistry.getInstance().getWebserviceCount();

		for (int i = 0; i < webservicesCount; i++) {
			Webservice webservice = WebserviceRegistry.getInstance().getWebserviceAt(i);
			if (!(webservices.contains(WebserviceRegistry.getInstance().getWebserviceAt(i)))){
				filtered.add(webservice);				
			};
		}
		return filtered;
	}
	
	private boolean validateRequiredWebserviceFields(JTextField name, JTextArea description,
			JTextField creator, JTextField contact, JTextField url, JTextField serviceID,
			JTextField webresourceFormat) {
		
		if (name.getText().equals("")){ //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showError(null,
					"plugins.weblicht.weblichtWebserviceView.dialogs.noEmptyName.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtWebserviceView.dialogs.noEmptyName.message"); //$NON-NLS-1$
			return false;
		}
		
		if (creator.getText().equals("")){ //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showError(null,
					"plugins.weblicht.weblichtWebserviceView.dialogs.noEmptyCreator.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtWebserviceView.dialogs.noEmptyCreator.message"); //$NON-NLS-1$
			return false;
		}
		
		if (contact.getText().equals("")){ //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showError(null,
					"plugins.weblicht.weblichtWebserviceView.dialogs.noEmptyContact.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtWebserviceView.dialogs.noEmptyContact.message");//$NON-NLS-1$
			return false;
		}
		
		if (url.getText().equals("")){ //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showError(null,
					"plugins.weblicht.weblichtWebserviceView.dialogs.noEmptyURL.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtWebserviceView.dialogs.noEmptyURL.message"); //$NON-NLS-1$
			return false;
		}

		if (serviceID.getText().equals("")) { //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showError(null,
				"plugins.weblicht.weblichtWebserviceView.dialogs.noEmptyServiceID.title", //$NON-NLS-1$
				"plugins.weblicht.weblichtWebserviceView.dialogs.noEmptyServiceID.message"); //$NON-NLS-1$
			return false;
		}

		if (webresourceFormat.getText().equals("")) { //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showError(	null,
				"plugins.weblicht.weblichtWebserviceView.dialogs.noEmptyWebserviceFormat.title", //$NON-NLS-1$
				"plugins.weblicht.weblichtWebserviceView.dialogs.noEmptyWebserviceFormat.message"); //$NON-NLS-1$
			return false;
		}		
		return true;			
	}
	
	
	public WebserviceIOAttributes showWebserviceIOEditAttributes(Component parent, String title, 
		String message, String attributename, String attributevalues, Object...params) {
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory.getGlobalFactory().getResourceDomain());
		
		WebserviceIOAttributes wio = new WebserviceIOAttributes();

		JTextField attributenameField = new JTextField(30);
		attributenameField.setText(attributename);
		
		JTextField attributevaluesField = new JTextField(30);
		attributevaluesField.setText(attributevalues);
		
		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage("plugins.weblicht.labels.webservice.AttributeName"); //$NON-NLS-1$
		builder.addMessage(attributenameField);
		builder.addMessage("plugins.weblicht.labels.webservice.AttributeValue"); //$NON-NLS-1$
		builder.addMessage(attributevaluesField);
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		if(!builder.isYesValue()) {
			return null;
		}
		
		wio.setAttributename(attributenameField.getText());
		wio.setAttributevalues(attributevaluesField.getText());
		
		return wio;
	}
	
	
	@SuppressWarnings("static-access")
	public String showWebserviceChooserDialog(Component parent, String title, 
			String message, List<Webservice> ws, List<String> wsQuery, String query, Object...params) {
		
		JList<Object> webserviceList;
		JList<Object> queryList;
		
		WebserviceViewListModel webserviceViewListModel;
		
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory.getGlobalFactory().getResourceDomain());
		
		builder.setTitle(title);
		
		webserviceViewListModel = new WebserviceViewListModel();
		webserviceList = new JList<Object>(webserviceViewListModel);
		webserviceList.setBorder(UIUtil.defaultContentBorder);
		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		selectionModel.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		
		
		//TODO nicer gui
		queryList = new JList<Object>(wsQuery.toArray());
		queryList.setBorder(UIUtil.defaultContentBorder);

		
		builder.setMessage(message, params);
		builder.addMessage(webserviceList);
		builder.addMessage("plugins.weblicht.labels.webservice.WebchainOutformat"); //$NON-NLS-1$
		builder.addMessage(queryList);
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		Webservice webservice = (Webservice) webserviceList.getSelectedValue();
		
		return builder.isYesValue() ? webservice.getUID() : null;
	}
	
	
	//reworked
	@SuppressWarnings("static-access")
	public String showWebserviceChooserDialogReworked(Component parent, String title, 
			String message, List<Webservice> webservices,
			String query, Object...params) {
		
		JList<Object> webserviceList;
		JList<Object> queryList;
		
		
		//fresh list with outputitems from selected webchainmodel
		List<String> wsQuery = WebserviceRegistry.getInstance().getQueryFromWebserviceList(webservices);
		
		WebserviceFilteredViewListModel webserviceFilteredViewListModel;
		
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory.getGlobalFactory().getResourceDomain());
		
		builder.setTitle(title);
		
		webserviceFilteredViewListModel = new WebserviceFilteredViewListModel(
				filterWebservice(webservices));
		webserviceList = new JList<Object>(webserviceFilteredViewListModel);
		webserviceList.setBorder(UIUtil.defaultContentBorder);
		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		selectionModel.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		
		
		//TODO nicer gui
		queryList = new JList<Object>(wsQuery.toArray());
		queryList.setBorder(UIUtil.defaultContentBorder);
	
		
		builder.setMessage(message, params);
		
		//Message when no Services left to be added
		if (webserviceFilteredViewListModel.getSize()==0){
			builder.addMessage("plugins.weblicht.labels.webservice.allPossibleWebservicesAdded"); //$NON-NLS-1$
		}
		builder.addMessage(webserviceList);
		builder.addMessage("plugins.weblicht.labels.webservice.WebchainOutformat"); //$NON-NLS-1$
		builder.addMessage(queryList);
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		Webservice webserviceSel = (Webservice) webserviceList.getSelectedValue();
		
		return builder.isYesValue() ? webserviceSel.getUID() : null;
	}

	public Webservice showNewWebserviceDialog(Component parent, String title, 
			String message, String uID, Object...params) {
		
		JTextField uniqueID = new JTextField(30);
		uniqueID.setText(uID);
		uniqueID.setEditable(false);
		
		JTextField name = new JTextField(30);
		JTextArea description = createTextArea();
		JTextField creator = new JTextField(30);
		JTextField contact = new JTextField(30);
		JTextField url = new JTextField(60);
		JTextField serviceID = new JTextField(30);
		JTextField webresourceFormat = new JTextField(30);
		
		
		//required fields
		name.setBorder(BorderFactory.createLineBorder(Color.red));
		creator.setBorder(BorderFactory.createLineBorder(Color.red));
		contact.setBorder(BorderFactory.createLineBorder(Color.red));
		url.setBorder(BorderFactory.createLineBorder(Color.red));
		serviceID.setBorder(BorderFactory.createLineBorder(Color.red));
		webresourceFormat.setBorder(BorderFactory.createLineBorder(Color.red));
		
		//Default Value: Warning only change if you know what your doing ;)
		webresourceFormat.setText("text/xml"); //$NON-NLS-1$
		
		
		webresourceFormat.setToolTipText(ResourceManager.getInstance().get("plugins.weblicht.dialog.hint.webserviceResource")); //$NON-NLS-1$
		
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory.getGlobalFactory().getResourceDomain());


		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage("plugins.weblicht.labels.webservice.uniqueID"); //$NON-NLS-1$
		builder.addMessage(uniqueID);
		builder.addMessage("plugins.weblicht.labels.webservice.name"); //$NON-NLS-1$
		builder.addMessage(name);
		builder.addMessage("plugins.weblicht.labels.webservice.descriptionOpt"); //$NON-NLS-1$
		builder.addMessage(getContainer(description));
		builder.addMessage("plugins.weblicht.labels.webservice.creator"); //$NON-NLS-1$
		builder.addMessage(creator);
		builder.addMessage("plugins.weblicht.labels.webservice.contact"); //$NON-NLS-1$
		builder.addMessage(contact);
		builder.addMessage("plugins.weblicht.labels.webservice.url"); //$NON-NLS-1$
		builder.addMessage(url);
		builder.addMessage("plugins.weblicht.labels.webservice.serviceID:"); //$NON-NLS-1$
		builder.addMessage(serviceID);
		builder.addMessage("plugins.weblicht.labels.webservice.webresourceFormat"); //$NON-NLS-1$
		builder.addMessage(webresourceFormat);
		builder.addMessage("plugins.weblicht.labels.webservice.requiredFieldsRed"); //$NON-NLS-1$
		
		
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);

		
		Webservice webservice = WebserviceRegistry.getInstance().createWebservice(
				uniqueID.getText(),
				name.getText(),
				description.getText(),
				contact.getText(),
				creator.getText(),
				url.getText(),
				serviceID.getText(),
				webresourceFormat.getText());
		
		return builder.isYesValue() ? webservice : null;
	}
	
	
	public Webservice showNewWebserviceReworkDialog(Component parent, String title, 
			String message, String uID, String wsname, String wsdescription,
			String wscreator, String wscontact, String wsurl, String wsserviceID,
			String wswebresourceFormat, Object...params) {
		
		JTextField uniqueID = new JTextField(30);
		uniqueID.setText(uID);
		uniqueID.setEditable(false);
		
		JTextField name = new JTextField(30);
		JTextArea description = createTextArea();
		JTextField creator = new JTextField(30);
		JTextField contact = new JTextField(30);
		JTextField url = new JTextField(60);
		JTextField serviceID = new JTextField(30);
		JTextField webresourceFormat = new JTextField(30);
		
		//required fields
		fillRequiredFields(name, wsname);
		fillRequiredFields(creator, wscreator);
		fillRequiredFields(contact, wscontact);
		fillRequiredFields(url, wsurl);
		fillRequiredFields(serviceID, wsserviceID);
		fillRequiredFields(webresourceFormat,wswebresourceFormat);
		
		if(wsdescription != null){
			description.setText(wsdescription);
		}
		
		/*
		System.out.println("unique " + uID+" " + 
				"name " + wsname+" " +
				"desc " + wsdescription+" " +
				"creat " + wscreator+" " +
				"con " + wscontact+" " +
				"url " + wsurl+" " +
				"wsres " + wswebresourceFormat+" ");
		*/


		
		
		webresourceFormat.setToolTipText(ResourceManager.getInstance().get("plugins.weblicht.dialog.hint.webserviceResource")); //$NON-NLS-1$
		
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory.getGlobalFactory().getResourceDomain());


		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage("plugins.weblicht.labels.webservice.uniqueID"); //$NON-NLS-1$
		builder.addMessage(uniqueID);
		builder.addMessage("plugins.weblicht.labels.webservice.name"); //$NON-NLS-1$
		builder.addMessage(name);
		builder.addMessage("plugins.weblicht.labels.webservice.descriptionOpt"); //$NON-NLS-1$
		builder.addMessage(getContainer(description));
		builder.addMessage("plugins.weblicht.labels.webservice.creator"); //$NON-NLS-1$
		builder.addMessage(creator);
		builder.addMessage("plugins.weblicht.labels.webservice.contact"); //$NON-NLS-1$
		builder.addMessage(contact);
		builder.addMessage("plugins.weblicht.labels.webservice.url"); //$NON-NLS-1$
		builder.addMessage(url);
		builder.addMessage("plugins.weblicht.labels.webservice.serviceID"); //$NON-NLS-1$
		builder.addMessage(serviceID);
		builder.addMessage("plugins.weblicht.labels.webservice.webresourceFormat"); //$NON-NLS-1$
		builder.addMessage(webresourceFormat);
		builder.addMessage("plugins.weblicht.labels.webservice.requiredFieldsRed"); //$NON-NLS-1$
		
		
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		

		
		if  (builder.isYesValue()){			
			if (validateRequiredWebserviceFields(name, description,
					creator, contact, url, serviceID, webresourceFormat)){
				Webservice webservice = WebserviceRegistry.getInstance().createWebservice(
						uniqueID.getText(),
						name.getText(),
						description.getText(),
						contact.getText(),
						creator.getText(),
						url.getText(),
						serviceID.getText(),
						webresourceFormat.getText());
				return builder.isYesValue() ? webservice : null;
			} else {
				//back to dialog with missing fields
				return showNewWebserviceReworkDialog(null, title, message, uID,
						name.getText(), description.getText(),
						creator.getText(), contact.getText(),
						url.getText(), serviceID.getText(),
						webresourceFormat.getText(), null, null);
			}
		}
		
		// user cancelled
		return null;	
		
	}
	
	public class WebserviceFilteredViewListModel extends AbstractListModel<Object>{
		private static final long serialVersionUID = -5945892868643517260L;
		
		protected Webchain webchain;			
		protected List<Webservice> webservices;		

		
		public WebserviceFilteredViewListModel(List<Webservice> webservice){	
			this.webservices = new ArrayList<>();
			this.webservices = webservice;
		}
		
		
		/**
		 * @see javax.swing.ListModel#getElementAt(int)
		 */
		@Override
		public Object getElementAt(int index) {
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
