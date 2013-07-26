/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.weblicht;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import de.ims.icarus.plugins.weblicht.webservice.Webchain;
import de.ims.icarus.plugins.weblicht.webservice.WebchainElements;
import de.ims.icarus.plugins.weblicht.webservice.WebchainInputType;
import de.ims.icarus.plugins.weblicht.webservice.WebchainOutputType;
import de.ims.icarus.plugins.weblicht.webservice.WebchainRegistry;
import de.ims.icarus.plugins.weblicht.webservice.Webservice;
import de.ims.icarus.plugins.weblicht.webservice.WebserviceIOAttributes;
import de.ims.icarus.plugins.weblicht.webservice.WebserviceProxy;
import de.ims.icarus.plugins.weblicht.webservice.WebserviceRegistry;
import de.ims.icarus.resources.ResourceDomain;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.dialog.BasicDialogBuilder;
import de.ims.icarus.ui.dialog.DialogFactory;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebserviceDialogs {
	
	private static WebserviceDialogs webserviceDialogFactory;
	
	private DocumentListener requiredFieldsListener = new RequiredFieldsListener();	

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
		textArea.setToolTipText(UIUtil.toSwingTooltip(null));
		
		return textArea;
	}
	
	private void fillRequiredFields(JTextField creator, String input){
		creator.getDocument().addDocumentListener(requiredFieldsListener);
		creator.getDocument().putProperty("field", creator); //$NON-NLS-1$
		if (input == null || input.equals("")) { //$NON-NLS-1$
			creator.setBorder(BorderFactory.createLineBorder(Color.red));
		} else {
			creator.setText(input);
		}
	}
	
	/**
	 * filter return all webservices which are not already in list
	 * @param webservices
	 * @return
	 */
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
	
	/**
	 * filter return all webservices with key / value
	 * @param webservices
	 * @return
	 */
	private List<Webservice> filterWebserviceByKeyPair(List<Webservice> webservices,
			String key,String value){
		List<Webservice> filtered = new ArrayList<>();
		int webservicesCount = WebserviceRegistry.getInstance().getWebserviceCount();

		for (int i = 0; i < webservicesCount; i++) {
			Webservice webservice = WebserviceRegistry.getInstance().getWebserviceAt(i);
			if (!(webservices.contains(WebserviceRegistry.getInstance().getWebserviceAt(i)))){
				
				List<WebserviceIOAttributes> wio = webservice.getInput();
				for(int j = 0; j < wio.size(); j++){
					WebserviceIOAttributes wa = wio.get(j);
					if (wa.getAttributename().equals(key) &&
							wa.getAttributevalues().equals(value)){
						filtered.add(webservice);
					};
				}
			}
		}
		return filtered;
	}
	
	/**
	 * filter webservices take care of io attributes
	 * @param webservices
	 * @param query
	 * @return
	 */
	private List<Webservice> filterWebservicePlusIOAttributesUnfullfilled(List<Webservice> webservices,
			List<String> query){
		List<Webservice> filtered = new ArrayList<>();
		int webservicesCount = WebserviceRegistry.getInstance().getWebserviceCount();
		//System.out.println("query" + query.size() + "count " + webservicesCount);
		for (int i = 0; i < webservicesCount; i++) {
			Webservice webservice = WebserviceRegistry.getInstance().getWebserviceAt(i);
			if (!(webservices.contains(WebserviceRegistry.getInstance().getWebserviceAt(i)))){
				//if (allAttributesFit(webservice, query)) filtered.add(webservice);
				if(!isWebserviceInputInCurrentOutputAttributeList(webservice,query)){
					filtered.add(webservice);
				}
			};
		}
		return filtered;
	}
	
	
	/**
	 * filter webservices take care of io attributes
	 * @param webservices
	 * @param query
	 * @return
	 */
	private List<Webservice> filterWebservicePlusIOAttributes(List<Webservice> webservices,
			List<String> query){
		List<Webservice> filtered = new ArrayList<>();
		int webservicesCount = WebserviceRegistry.getInstance().getWebserviceCount();
		//System.out.println("query " + query.size() + " count " + webservicesCount);
		for (int i = 0; i < webservicesCount; i++) {
			Webservice webservice = WebserviceRegistry.getInstance().getWebserviceAt(i);
			if (!(webservices.contains(WebserviceRegistry.getInstance().getWebserviceAt(i)))){
				//if (allAttributesFit(webservice, query)) filtered.add(webservice);
				if(isWebserviceInputInCurrentOutputAttributeList(webservice,query)){
					filtered.add(webservice);
				}
			};
		}
		return filtered;
	}
	
	
	private boolean isWebserviceInputInCurrentOutputAttributeList(Webservice webservice,
			List<String> attributes){
		
		List<WebserviceIOAttributes> wio = webservice.getInput();
		boolean complete = false;
		
		for(int i = 0; i < wio.size(); i++){
			WebserviceIOAttributes wa = wio.get(i);
			complete = false;
			if (attributes.contains(wa.getAttributename()
					+"="+wa.getAttributevalues())){ //$NON-NLS-1$
				complete = true;
			}
			
			if (attributes.contains(wa.getAttributename())){
				complete = true;
			};
			
			// if one attribute is not in attribute list we can direct stop searching
			if (!complete) return false;
			
			/*
			System.out.println(wio.get(i).getAttributename()+"="
								+wio.get(i).getAttributevalues() + " "
								+complete);
			*/
		}
	
		return complete;
	}
	
	/*
	private boolean isWebserviceOutputConflictingCurrentOutput(Webservice webservice,
			List<String> attributes){
		
		List<WebserviceIOAttributes> wio = webservice.getOutput();
		boolean complete = false;
		for(int i = 0; i < wio.size(); i++){
			WebserviceIOAttributes wa = wio.get(i);
			if (attributes.contains(wa.getAttributename())){
				complete = true;
			};
			if (attributes.contains(wa.getAttributename()+"="+wa.getAttributevalues())){
				complete = true;
			};
		}
	
		return complete;
	}
	*/
	
	
	private boolean validateRequiredWebchainFields(JTextField name) {		
		if (name.getText().equals("")){ //$NON-NLS-1$
			DialogFactory.getGlobalFactory().showError(null,
					"plugins.weblicht.weblichtWebchainView.dialogs.noEmptyName.title",  //$NON-NLS-1$
					"plugins.weblicht.weblichtWebchainView.dialogs.noEmptyName.message"); //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
	
	/**
	 * 
	 * @param name
	 * @param description
	 * @param creator
	 * @param contact
	 * @param url
	 * @param serviceID
	 * @param webresourceFormat
	 * @return
	 */
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
	
	
	/**
	 * Little helper to grab only the Webservices from a given Webchain Webelement List
	 * e.g. building the requirements for new webservices (in/out) we only need webservices
	 * and dont care about what the outputservice does.
	 * @param list
	 * @return
	 */
	private List<Webservice> extractWebservicesFromElements(List<WebchainElements> list){
		List<Webservice> serviceList = new ArrayList<>();
		for (int i = 0; i < list.size();i++){
			if (list.get(i) instanceof WebserviceProxy){
				WebserviceProxy wsp = (WebserviceProxy)list.get(i);
				serviceList.add(wsp.get());
			}
		}
		return serviceList;
	}
	
	
	/**
	 * 
	 * @param bGroup
	 * @return
	 */
	private String getNameFromSelectedButton(ButtonGroup bGroup){
		  Enumeration<AbstractButton> allRadioButton = bGroup.getElements();  
		   
		  while(allRadioButton.hasMoreElements()) {  
		   JRadioButton temp=(JRadioButton)allRadioButton.nextElement();  
		   if(temp.isSelected()) {
		    return temp.getName();
		   } 
		  }
		  return null;		   
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
		
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory
																.getGlobalFactory()
																.getResourceDomain());
		
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
			String message, List<WebchainElements> chainElements,
			String query, Object...params) {
		
		JList<Object> webserviceList;
		JList<Object> webserviceListUnfiltered;
		JList<Object> queryList;

		//No webservices added so far, disblay webservice which dont have any inputtypes
		if(query == null){
			//wsQuery
		}		
		//fresh list with outputitems from selected webchainmodel
		List<String> wsQuery = WebserviceRegistry.getInstance()
								.getQueryFromWebserviceList(
										extractWebservicesFromElements(chainElements));
		
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory.getGlobalFactory().getResourceDomain());
		
		builder.setTitle(title);
		
		//prepare filtered list
		WebserviceFilteredViewListModel webserviceFilteredViewListModel;
		if (chainElements.size()>1){
			webserviceFilteredViewListModel = new WebserviceFilteredViewListModel(
					filterWebservicePlusIOAttributes(extractWebservicesFromElements(chainElements), wsQuery),true);
		} else {
			// only show webservices where input type is text/plain
			webserviceFilteredViewListModel = new WebserviceFilteredViewListModel(
					filterWebserviceByKeyPair(extractWebservicesFromElements(chainElements),
									"type", //$NON-NLS-1$
									"text/plain"),true);//$NON-NLS-1$
		}
		webserviceList = new JList<Object>(webserviceFilteredViewListModel);
		webserviceList.setBorder(UIUtil.defaultContentBorder);
		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		selectionModel.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
		
		//prepare unfiltered list
		WebserviceFilteredViewListModel webserviceFilteredRemainingListModel;
		webserviceFilteredRemainingListModel = new WebserviceFilteredViewListModel(
				filterWebservicePlusIOAttributesUnfullfilled(extractWebservicesFromElements(chainElements),
						wsQuery),false);

		webserviceListUnfiltered = new JList<Object>(webserviceFilteredRemainingListModel);
		webserviceListUnfiltered.setBorder(UIUtil.defaultContentBorder);

		
		//TODO nicer gui
		queryList = new JList<Object>(wsQuery.toArray());
		queryList.setBorder(UIUtil.defaultContentBorder);
	
		
		builder.setMessage(message, params);
		
		//Message when no Services left to be added
		if (webserviceFilteredViewListModel.getSize()==0){
			builder.addMessage("plugins.weblicht.labels.webservice.allPossibleWebservicesAdded"); //$NON-NLS-1$
		}
		builder.addMessage(webserviceList);
		
		builder.addMessage("plugins.weblicht.labels.webservice.notAddablewebservices"); //$NON-NLS-1$
		webserviceListUnfiltered.setEnabled(false);
		builder.addMessage(webserviceListUnfiltered);
		builder.addMessage("plugins.weblicht.labels.webservice.WebchainOutformat"); //$NON-NLS-1$
		builder.addMessage(queryList);
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		Webservice webserviceSel = (Webservice) webserviceList.getSelectedValue();
		
		return builder.isYesValue() ? webserviceSel.getUID() : null;
	}
	
	
	
	/**
	 * Using Fields From WebchainEditor
	 * @param parent
	 * @param title
	 * @param message
	 * @param chainelement
	 * @param inputPanel
	 * @param inputArea
	 * @param params
	 */
	public void editIOPanel(Component parent, String title, 
			String message, WebchainElements chainelement,
			JPanel inputPanel,JTextArea inputArea, Object...params) {		

		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory.getGlobalFactory()
																.getResourceDomain());
		if(chainelement instanceof WebchainInputType){
			builder.addMessage("plugins.weblicht.labels.webservice.WebchainInput"); //$NON-NLS-1$
		}
		
		if(chainelement instanceof WebchainOutputType){
			builder.addMessage("plugins.weblicht.labels.webservice.WebchainOutput"); //$NON-NLS-1$
		}
		
		builder.setTitle(title);
		builder.addMessage(inputPanel);
		builder.addMessage(inputArea);
		//System.out.println(inputPanel.getComponentCount());
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		
		
		if(chainelement instanceof WebchainInputType){
			WebchainInputType wi = new WebchainInputType();
			wi.setInputTypeValue(inputArea.getText());
			
		}
		
		if(chainelement instanceof WebchainOutputType){
			builder.addMessage("plugins.weblicht.labels.webservice.WebchainOutput"); //$NON-NLS-1$
		}

		
		//return builder.isYesValue() ? webserviceSel.getUID() : null;
	}

	/**
	 * @param object
	 * @param string
	 * @param string2
	 * @param chainelement
	 * @param object2
	 * @return 
	 */
	public WebchainOutputType showAddOutputElementDialog(Component parent, String title, 
			String message, WebchainElements chainelement, Object...params) {
		
		JRadioButton webserviceStaticInput;
		JRadioButton webserviceLocationInput;
		JRadioButton webserviceDynamicInput;
		ButtonGroup  webserviceInputGroup;
		JTextArea	 webserviceInputArea;
				
		JTextField ioValueField = new JTextField(30);

		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory.getGlobalFactory()
																.getResourceDomain());		

		//input Buttons
		webserviceStaticInput = new JRadioButton();
		webserviceStaticInput.setName("static"); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceStaticInput, "static", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceStaticInput);
		//webserviceStaticInput.addActionListener(handler);	
		
		webserviceLocationInput = new JRadioButton();
		webserviceLocationInput.setName("location"); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceLocationInput, "location", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceLocationInput);
		//webserviceLocationInput.addActionListener(handler);	
		
		webserviceDynamicInput = new JRadioButton();
		webserviceDynamicInput.setName("dynamic"); //$NON-NLS-1$
		resourceDomain.prepareComponent(webserviceDynamicInput, "dynamic", null); //$NON-NLS-1$
		resourceDomain.addComponent(webserviceDynamicInput);
		//webserviceDynamicInput.addActionListener(handler);
		
		
		webserviceInputGroup = new ButtonGroup();
		webserviceInputArea = new JTextArea();
		webserviceInputArea.setBorder(UIUtil.defaultContentBorder);
		
		webserviceInputGroup.add(webserviceStaticInput);
		webserviceInputGroup.add(webserviceLocationInput);
		webserviceInputGroup.add(webserviceDynamicInput);

		
		builder.setTitle(title);
		builder.addMessage("plugins.weblicht.labels.webservice.WebchainOutput"); //$NON-NLS-1$
	
		
		
		builder.addMessage(webserviceStaticInput);
		builder.addMessage(webserviceLocationInput);
		builder.addMessage(webserviceDynamicInput);
		builder.addMessage(ioValueField);
		

		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
		
		WebchainOutputType wo = new WebchainOutputType();
		
		if (webserviceDynamicInput.isSelected()){
			wo.setOutputType("dynamic");	 //$NON-NLS-1$
		}
		if (webserviceLocationInput.isSelected()){
			wo.setOutputType("location"); //$NON-NLS-1$
		}
		if (webserviceStaticInput.isSelected()){
			wo.setOutputType("static"); //$NON-NLS-1$
		}
		//System.out.println(wo.getOutputType());
		
		return builder.isYesValue() ? wo : null;
		
	}
	


	public Webchain showNewWebchain(Component parent, String title, 
			String message, Object...params) {
		
		final JTextField name = new JTextField(30);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				name.requestFocus();
			}
		});

		name.getDocument().addDocumentListener(requiredFieldsListener);
		name.getDocument().putProperty("field", name); //$NON-NLS-1$
		name.setBorder(BorderFactory.createLineBorder(Color.red));
		
		//required fields
		name.setBorder(BorderFactory.createLineBorder(Color.red));		
		
		BasicDialogBuilder builder = new BasicDialogBuilder(DialogFactory
											.getGlobalFactory().getResourceDomain());

		builder.setTitle(title);
		builder.setMessage(message, params);
		builder.addMessage("plugins.weblicht.labels.webchain.name"); //$NON-NLS-1$
		builder.addMessage(name);
		builder.addMessage("plugins.weblicht.labels.webservice.requiredFieldsRed"); //$NON-NLS-1$
		
		ButtonGroup webserviceInputGroup = new ButtonGroup();
		
		JRadioButton webserviceStaticInput = new JRadioButton(ResourceManager.getInstance()
													.get("static")); //$NON-NLS-1$
		webserviceStaticInput.setName("static"); //$NON-NLS-1$
		//webserviceStaticInput.addActionListener(handler);
		
		JRadioButton webserviceLocationInput = new JRadioButton(ResourceManager.getInstance()
				.get("location")); //$NON-NLS-1$
		webserviceLocationInput.setName("location"); //$NON-NLS-1$
		//webserviceLocationInput.addActionListener(handler);
		
		JRadioButton webserviceDynamicInput = new JRadioButton(ResourceManager.getInstance()
				.get("dynamic")); //$NON-NLS-1$
		webserviceDynamicInput.setName("dynamic"); //$NON-NLS-1$
		//webserviceDynamicInput.addActionListener(handler);
		
		//default value is dynamic
		webserviceDynamicInput.setSelected(true);
		
		webserviceInputGroup.add(webserviceStaticInput);
		webserviceInputGroup.add(webserviceLocationInput);
		webserviceInputGroup.add(webserviceDynamicInput);
		builder.addMessage(webserviceStaticInput);
		builder.addMessage(webserviceLocationInput);
		builder.addMessage(webserviceDynamicInput);
		builder.addMessage("plugins.weblicht.labels.webservice.inputValueEditLater"); //$NON-NLS-1$
		
		builder.setPlainType();
		builder.setOptions("ok", "cancel"); //$NON-NLS-1$ //$NON-NLS-2$
		
		builder.showDialog(parent);
				
		if (builder.isYesValue()) {			
			if (validateRequiredWebchainFields(name)){
				//System.out.println("Input " + getNameFromSelectedButton(webserviceInputGroup));
				WebchainInputType inputType = new WebchainInputType();				
				inputType.setInputType(getNameFromSelectedButton(webserviceInputGroup));
				Webchain webchain = WebchainRegistry.getInstance().createWebchain(
						name.getText(),
						inputType);
				return builder.isYesValue() ? webchain : null;
			} else {
				//back to dialog with missing fields
				return showNewWebchain(null, title, message,
						name.getText());
				
			}
		}
		
		// user cancelled
		return null;
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
		
		webresourceFormat.setToolTipText(UIUtil.toSwingTooltip(
				ResourceManager.getInstance().get("plugins.weblicht.dialog.hint.webserviceResource"))); //$NON-NLS-1$
		
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
		boolean inverted;

		
		public WebserviceFilteredViewListModel(List<Webservice> webservice, boolean inverted){	
			this.webservices = new ArrayList<>();
			this.webservices = webservice;
			this.inverted = inverted;
		}
		
		
		private String buildListViewString(int index){
			StringBuilder sb = new StringBuilder();
			if (inverted){
				sb.append(ResourceManager.getInstance().get(
						"plugins.weblicht.webchainEditor.serviceName")) //$NON-NLS-1$
					.append(" ") //$NON-NLS-1$
					.append(webservices.get(index).getName())
					.append(" ") //$NON-NLS-1$
					.append(ResourceManager.getInstance().get(
							"plugins.weblicht.webchainEditor.outFormat")) //$NON-NLS-1$
					.append(" ") //$NON-NLS-1$
					.append(webservices.get(index).getOutputText());
			} else {
				sb.append(ResourceManager.getInstance().get(
						"plugins.weblicht.webchainEditor.serviceName")) //$NON-NLS-1$
					.append(" ") //$NON-NLS-1$
					.append(webservices.get(index).getName())
					.append(" ") //$NON-NLS-1$
					.append(ResourceManager.getInstance().get(
							"plugins.weblicht.webchainEditor.inFormat")) //$NON-NLS-1$
					.append(" ") //$NON-NLS-1$
					.append(webservices.get(index).getInputText());
			}
			return sb.toString();
			
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
	
	
	
	public class RequiredFieldsListener implements DocumentListener{

		/**
		 * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
		 */
		@Override
		public void changedUpdate(DocumentEvent e) {
			 textFieldValueChanged(e);
			
		}

		/**
		 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
		 */
		@Override
		public void insertUpdate(DocumentEvent e) {
			textFieldValueChanged(e);
			
		}

		/**
		 * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
		 */
		@Override
		public void removeUpdate(DocumentEvent e) {
			textFieldValueChanged(e);			
		}
		
		
		private void textFieldValueChanged (DocumentEvent e) {
			Document source = e.getDocument();
			
			JTextField textField = (JTextField) e.getDocument().getProperty("field"); //$NON-NLS-1$
	
			
			if (source.getLength() != 0) {
				textField.setBorder(UIUtil.defaultContentBorder);
			} else {
				textField.setBorder(BorderFactory.createLineBorder(Color.red));
			}
		
		}
		
	}
}
	

