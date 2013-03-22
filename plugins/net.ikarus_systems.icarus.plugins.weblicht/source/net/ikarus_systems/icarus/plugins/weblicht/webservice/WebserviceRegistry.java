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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.EventSource;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.ui.events.WeakEventSource;
import net.ikarus_systems.icarus.util.id.ExtensionIdentity;
import net.ikarus_systems.icarus.util.id.StaticIdentity;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebserviceRegistry {
	
	//protected LinkedHashMap<String, Webservice> webserviceHashMap;
	protected List<Webservice> webserviceList;
	//protected List<String> uniqueIDList;
	//protected Webservice webservice;
	
	//--
	private EventSource eventSource;
	private static WebserviceRegistry instance;
	
	public static WebserviceRegistry getInstance() {
		if(instance==null) {
			synchronized (WebserviceRegistry.class) {
				if(instance==null) {
					instance = new WebserviceRegistry();
				}
			}
		}
		return instance;
	}
	
	private static Logger logger;
	
	private static Logger getLogger() {
		if(logger==null) {
			logger = LoggerFactory.getLogger(WebserviceRegistry.class);
		}
		return logger;
	}
	
	private WebserviceRegistry() {
		//webserviceHashMap = new LinkedHashMap<String,Webservice>();
		
		eventSource = new WeakEventSource(this);
		webserviceList = new ArrayList<Webservice>();

		try {
			loadWebserviceXML();
		} catch (Exception e) {
			getLogger().log(LoggerFactory.record(Level.SEVERE, 
					"Failed to load webservices", e)); //$NON-NLS-1$
		}
		//System.out.println(wHMap.keySet());
		//System.out.println(wHMap.get("1").getInput().keySet());
	}
	
	
	private void loadWebserviceExtensions(){
		PluginDescriptor descriptor=PluginUtil.getPluginRegistry().getPluginDescriptor("net.ikarus_systems.icarus.weblicht"); //$NON-NLS-1$
		ExtensionPoint extensionPoint=descriptor.getExtensionPoint("Webservice"); //$NON-NLS-1$
		
		Collection<Extension> extensions=extensionPoint.getConnectedExtensions();
		Webservice webservice;
		for(Extension extension : extensions){
			
			webservice = new Webservice();
			webservice.setIdentity(new ExtensionIdentity(extension));
			webservice.setContact(getTextFromExtension(extension, "Contact"));					 //$NON-NLS-1$
			webservice.setCreator(getTextFromExtension(extension, "Creator")); //$NON-NLS-1$
			webservice.setURL(getTextFromExtension(extension, "URL")); //$NON-NLS-1$
			webservice.setServiceID(getTextFromExtension(extension, "ID")); //$NON-NLS-1$
			//TODO in/out
			
			webserviceList.add(webservice);
			//TODO existing ID
			
		}
	}
	
	private void loadWebserviceXML() throws Exception {
			
			//File fXmlFile = new File(Core.getCore().getDataFolder(),"weblicht.xml"); //$NON-NLS-1$
			
			File fXmlFile = new File(
					"D:/Eigene Dateien/smashii/workspace/Icarus/data/weblicht.xml"); //$NON-NLS-1$
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();

			//System.out.println("Root element :"	+ doc.getDocumentElement().getNodeName());

			NodeList servicesList = doc.getElementsByTagName("Service"); //$NON-NLS-1$
			for (int i = 0; i < servicesList.getLength(); i++) {
				Node sNode = servicesList.item(i);
				Webservice webservice;
				//System.out.println("\nCurrent Element :" + sNode.getNodeName());
							

				if (sNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) sNode;
					NamedNodeMap inputMap = getAttributesFromElement(eElement, "Input"); //$NON-NLS-1$
					NamedNodeMap outputMap = getAttributesFromElement(eElement, "Output"); //$NON-NLS-1$

					//Debug Stuff
					/*
					System.out.println("UID : " +	eElement.getAttribute("uid"));
					System.out.println("Name: " +
					  eElement.getElementsByTagName("Name").item(0).getTextContent());
					System.out.println("Desc: " + 
					  eElement.getElementsByTagName("Description").item(0).getTextContent());
					System.out.println("Creator : " +
					 eElement.getElementsByTagName("Creator").item(0).getTextContent());
					System.out.println("Contact : " + 
					  eElement.getElementsByTagName("Contact").item(0).getTextContent());
					System.out.println("URL : " +
					  eElement.getElementsByTagName("URL").item(0).getTextContent());
					 
					for (int j = 0; j < inputMap.getLength(); j++) {
						System.out.println("Key: "
								+ inputMap.item(j).getNodeName() + " | Value: "
								+ inputMap.item(j).getNodeValue());
					}
					for (int j = 0; j < outputMap.getLength(); j++) {
						System.out.println("Key: "
								+ outputMap.item(j).getNodeName()
								+ " | Value: "
								+ outputMap.item(j).getNodeValue());
					}*/
					
					webservice = new Webservice();
					StaticIdentity identity = new StaticIdentity(eElement.getAttribute("uid"), webservice); //$NON-NLS-1$
					
					identity.setName(getTextFromElement(eElement, "Name")); //$NON-NLS-1$
					identity.setDescription(getTextFromElement(eElement, "Description")); //$NON-NLS-1$
					webservice.setIdentity(identity);
					webservice.setContact(getTextFromElement(eElement, "Contact"));					 //$NON-NLS-1$
					webservice.setCreator(getTextFromElement(eElement, "Creator")); //$NON-NLS-1$
					webservice.setURL(getTextFromElement(eElement, "URL")); //$NON-NLS-1$
					webservice.setServiceID(getTextFromElement(eElement, "ID")); //$NON-NLS-1$
					
					List<WebserviceIOAttributes> inputList = new ArrayList<WebserviceIOAttributes>();
					WebserviceIOAttributes input = null;
					for (int j = 0; j < inputMap.getLength(); j++) {	
						input = new WebserviceIOAttributes();
						input.setAttributename(inputMap.item(j).getNodeName());
						input.setAttributevalues(inputMap.item(j).getNodeValue());
						inputList.add(input);
					}

					webservice.setInput(inputList);
					
				
					List<WebserviceIOAttributes> outList = new ArrayList<WebserviceIOAttributes>();
					WebserviceIOAttributes output = null;
					for (int j = 0; j < outputMap.getLength(); j++) {	
						output = new WebserviceIOAttributes();
						output.setAttributename(outputMap.item(j).getNodeName());
						output.setAttributevalues(outputMap.item(j).getNodeValue());
						outList.add(output);
					}
					webservice.setOutput(outList);
					
					//webserviceHashMap.put(eElement.getAttribute("uid"), webservice); //$NON-NLS-1$
					//webserviceList.add(webservice);
					addNewWebservice(webservice);
				}
			}	
	}
	
	private String getTextFromExtension(Extension extension, String s){
		return extension.getParameter(s).valueAsString();	
	}
	
	/**
	 * e is XML Element, s is the Name of our Node Element
	 * @param e 
	 * @param s
	 * @return xml node text content
	 */
	private String getTextFromElement(Element e, String s){
		return e.getElementsByTagName(s).item(0).getTextContent();		
	}
	
	/**
	 * e is XML Element, s is the Name of our Node Element
	 * @param e 
	 * @param s
	 * @return xml node attributes
	 */
	private NamedNodeMap getAttributesFromElement(Element e, String s){
		return e.getElementsByTagName(s).item(0).getAttributes();		
	}
	
	public Webservice getWebserviceFromUniqueID(String uniqueID){		
		Webservice webservice = null;
		for (int i = 0; i < webserviceList.size();i++){
			if (webserviceList.get(i).getUID().equals(uniqueID)){
				webservice = webserviceList.get(i);
			}
		}
		return webservice;
		//return webserviceHashMap.get(serviceid);
	}
	
	public String getNameFromUniqueID(String uniqueID){
		String serviceName = "No service name specified"; //$NON-NLS-1$
		for (int i = 0; i < webserviceList.size();i++){
			Webservice webservice = webserviceList.get(i);
			if (webservice.getUID().equals(uniqueID)){
				serviceName = webservice.getName();
			}
		}
		//return webserviceHashMap.get(serviceid).getName();		
		return serviceName;
	}
	
	
	public void addNewWebservice(Webservice webservice){
		webserviceList.add(webservice);
		int index = indexOfWebservice(webservice);
		eventSource.fireEvent(new EventObject(Events.ADDED,
				"webservice",webservice, //$NON-NLS-1$
				"index",index));//$NON-NLS-1$
	}
	
	public void deleteWebservice(Webservice webservice){
		int index = indexOfWebservice(webservice);
		if (webserviceList.remove(webservice)){
			eventSource.fireEvent(new EventObject(Events.REMOVED,
					"webservice",webservice, //$NON-NLS-1$
					"index",index)); //$NON-NLS-1$
		}
	}
	
	public Webservice createWebservice(String uid, String name, String desc,
			String contact, String creator, String url, String serviceID){
		
		Webservice webservice = new Webservice();
		StaticIdentity identity = new StaticIdentity(uid, webservice);
		identity.setName(name);
		identity.setDescription(desc);
		webservice.setIdentity(identity);
		webservice.setContact(contact);
		webservice.setCreator(creator);
		webservice.setURL(url);
		webservice.setServiceID(serviceID);

		return webservice;
	}
	
	
	
	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#addListener(java.lang.String, net.ikarus_systems.icarus.ui.events.EventListener)
	 */
	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#removeListener(net.ikarus_systems.icarus.ui.events.EventListener)
	 */
	public void removeListener(EventListener listener) {
		eventSource.removeListener(listener);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#removeListener(net.ikarus_systems.icarus.ui.events.EventListener, java.lang.String)
	 */
	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}	


	/**
	 * @param webchain
	 */
	public void webserviceChanged(Webservice webservice) {
		eventSource.fireEvent(new EventObject(Events.CHANGED, 
				"webservice", webservice)); //$NON-NLS-1$		
	}	
	
	public int indexOfWebservice(Webservice webservice){
		return webserviceList.indexOf(webservice);
	}

	
	public int getWebserviceCount(){
		return webserviceList.size();
	}
	
	public Webservice getWebserviceAt(int index){
		return webserviceList.get(index);
	}
	
	public List<Webservice> getWebserviceList(){
		return webserviceList;
	}
	
	
	public boolean isValidUniqueID(String uniqueID){
		Boolean unique = true;
		
		for(int i = 0; i < webserviceList.size(); i++){
			if (webserviceList.get(i).getUID().equals(uniqueID)){
				return unique = false;
			}
		}
		return unique;		
	}
	
	
	public String createUniqueID(String uniqueID){
		
		if (uniqueID.equals("")){ //$NON-NLS-1$
			uniqueID = createUniqueID(UUID.randomUUID().toString());
		}
		if (isValidUniqueID(uniqueID)){
			return uniqueID;
		}
		return createUniqueID(UUID.randomUUID().toString());		
	}
	
	
	public List<WebserviceIOAttributes> getWebserviceInput(Webservice webservice){
		return webservice.getInput();
	}
	
	public List<WebserviceIOAttributes> getWebserviceOutput(Webservice webservice){
		return webservice.getOutput();
	}
	
	/**
	 * @param webservice
	 * @param newName
	 */
	public void setName(Webservice webservice, String name) {
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		if(name.equals(webservice.getName())) {
			return;
		}
		StaticIdentity identity = (StaticIdentity) webservice.getIdentity();
		identity.setName(name);
		int index = indexOfWebservice(webservice);
		eventSource.fireEvent(new EventObject(Events.CHANGED,
				"webservice",webservice, //$NON-NLS-1$
				"index",index));//$NON-NLS-1$
		
	}

	/**
	 * could be empty
	 * @param webservice
	 * @param newDesc
	 */
	public void setDescription(Webservice webservice, String description) {
		if(description.equals(webservice.getDescription())) {
			return;
		}
		StaticIdentity identity = (StaticIdentity) webservice.getIdentity();
		identity.setDescription(description);
		
	}

	/**
	 * @param webservice
	 * @param newCreator
	 */
	public void setCreator(Webservice webservice, String creator) {
		if(creator==null)
			throw new IllegalArgumentException("Invalid creator"); //$NON-NLS-1$
		if(creator.equals(webservice.getCreator())) {
			return;
		}
		webservice.setCreator(creator);
		
	}

	/**
	 * @param webservice
	 * @param newContact
	 */
	public void setContact(Webservice webservice, String contact) {
		if(contact==null)
			throw new IllegalArgumentException("Invalid contact"); //$NON-NLS-1$
		if(contact.equals(webservice.getContact())) {
			return;
		}
		webservice.setContact(contact);
		
	}

	/**
	 * @param webservice
	 * @param newURL
	 */
	public void setURL(Webservice webservice, String url) {
		if(url==null)
			throw new IllegalArgumentException("Invalid URL"); //$NON-NLS-1$
		if(url.equals(webservice.getURL())) {
			return;
		}
		webservice.setURL(url);
		
	}

	/**
	 * @param webservice
	 * @param newServiceID
	 */
	public void setServiceID(Webservice webservice, String serviceID) {
		if(serviceID==null)
			throw new IllegalArgumentException("Invalid serviceID"); //$NON-NLS-1$
		if(serviceID.equals(webservice.getServiceID())) {
			return;
		}
		webservice.setServiceID(serviceID);
		
	}

	/**
	 * @param webservice
	 * @param newInput
	 */
	public void setInputAttributes(Webservice webservice,
			List<WebserviceIOAttributes> newInput) {
		
		if (newInput==null){			
			return;
		}
		
		if(equalsIOList(webservice.getInput(), newInput)){
			return;
		}
		
		//lists are different replace old proxy list
		webservice.input = newInput;		
	}
	
	/**
	 * @param webservice
	 * @param newInput
	 */
	public void setOutputAttributes(Webservice webservice,
			List<WebserviceIOAttributes> newOutput) {
		
		if (newOutput==null){			
			return;
		}
		
		if(equalsIOList(webservice.getOutput(), newOutput)){
			return;
		}
		
		//lists are different replace old proxy list
		webservice.output = newOutput;		
	}

	/**
	 * @param input
	 * @param input2
	 * @return
	 */
	private boolean equalsIOList(List<WebserviceIOAttributes> i1,
			List<WebserviceIOAttributes> i2) {
		boolean equal = true;
		if (i1.size() != i2.size()){
			return false;
		} else {
			//same listsize we need to check ioattributes
			for (int i = 0; i < i1.size(); i++){
				if (!((i1.get(i).getAttributename().equals(i2.get(i).getAttributename())
					&& (i1.get(i).getAttributevalues().equals(i2.get(i).getAttributevalues()))))){
					equal = false;
				};
			}
		}
		return equal;
	}

	public static void main(String[] args) {	
		WebserviceRegistry wl = new WebserviceRegistry();
		//System.out.println(wl.webserviceHashMap.size());		
		
	}



}
