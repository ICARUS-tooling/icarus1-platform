/* 
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.weblicht.webservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ims.icarus.Core;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.EventSource;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.ui.events.WeakEventSource;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebchainRegistry {	

	protected List<Webchain> webchainList;
	protected WebchainInputType defaultInputType;
	
	private boolean hasChanges;
	
	//protected Webservice webservice;
	
	private EventSource eventSource;
	private static WebchainRegistry instance;
	
	public static WebchainRegistry getInstance() {
		if(instance==null) {
			synchronized (WebchainRegistry.class) {
				if(instance==null) {
					instance = new WebchainRegistry();
				}
			}
		}
		return instance;
	}
	
	//--

	
	private WebchainRegistry() {
		//webservice = new Webservice();
		eventSource = new WeakEventSource(this);
		//webchain = new Webchain();
		webchainList = new ArrayList<Webchain>();
		
		//set defaultInputType
		defaultInputType = new WebchainInputType();
		defaultInputType.setInputType("dynamic"); //$NON-NLS-1$
		defaultInputType.setInputTypeValue(""); //$NON-NLS-1$
		
		// Attempt to load webservice list
		try {			
			loadWebchainXML();			
		} catch (Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to load webservice chain list", e); //$NON-NLS-1$
		}

		//System.out.println(webchainList.get(0).getWebserviceIDList());	
		//System.out.println(webchainList.get(1).getWebserviceIDList());	
	}
	
	/**
	 * 
	 * @param document
	 * @param eName
	 * @param eData
	 * @return
	 */
	private Element generateChainElement(Document document, String eName, String eData){
		Element em = document.createElement(eName);
		em.setAttribute("uid", eData); //$NON-NLS-1$
		return em;		
	}
	
	
	private Element generateChainInputElement(Document document, String eName, String type, String value){
		Element em = document.createElement(eName);
		em.setAttribute("type", type); //$NON-NLS-1$
		em.setTextContent(value);
		return em;		
	}
	
	private Element generateChainOutputElement(Document document, String eName, String type, String value, String used){
		Element em = document.createElement(eName);
		em.setAttribute("type", type); //$NON-NLS-1$
		em.setAttribute("value", value); //$NON-NLS-1$
		em.setAttribute("outputused", used); //$NON-NLS-1$
		return em;		
	}
	
	
	public void saveWebchains() throws Exception{

		String root = "Webchains"; //$NON-NLS-1$
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		Element rootElement = document.createElement(root);

		document.appendChild(rootElement);
		
		for (int i = 0; i < webchainList.size(); i++){
			Webchain webchain = getWebchainAt(i);
	
			Element chain = document.createElement("Chain"); //$NON-NLS-1$
			chain.setAttribute("chainname", webchain.getName()); //$NON-NLS-1$

			for (int j = 0; j < webchain.getElementsCount(); j++){
				if (webchain.getElementAt(j) instanceof WebserviceProxy){
					WebserviceProxy service = (WebserviceProxy) webchain.getElementAt(j);
					chain.appendChild(
							generateChainElement(document, "chainelement", //$NON-NLS-1$
													service.get().getUID()));
					
				}
				if (webchain.getElementAt(j) instanceof WebchainInputType){
					WebchainInputType wi = (WebchainInputType) webchain.getElementAt(j);
					chain.appendChild(
							generateChainInputElement(document, "input", //$NON-NLS-1$
													wi.getInputType(),
													wi.getInputTypeValue())
							);
					
				}
				if (webchain.getElementAt(j) instanceof WebchainOutputType){
					WebchainOutputType wo = (WebchainOutputType) webchain.getElementAt(j);
					String used = new Boolean(wo.isOutputUsed()).toString();
					chain.appendChild(
							generateChainOutputElement(document, "chainelement", //$NON-NLS-1$
													wo.getOutputType(),
													wo.getOutputTypeValue(),
													used)
							);
					
				}
			}
				
			
			rootElement.appendChild(chain);
		}
		
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        //format output
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", //$NON-NLS-1$
        								"2"); //$NON-NLS-1$
        DOMSource source = new DOMSource(document);
        StreamResult result =  new StreamResult(new StringWriter());
        transformer.transform(source, result);
        

        saveXMLToFile(result);
        hasChanges=false;
	}
	
	
	private void saveXMLToFile (StreamResult result) throws Exception{
        //writing to file
        FileOutputStream fop = null;
        
        //debug
        //File file = new File("D:/Eigene Dateien/smashii/workspace/Icarus/data/webchain_out.xml"); //$NON-NLS-1$
        //File file = new File("D:/Eigene Dateien/smashii/workspace/Icarus/data/webchain.xml"); //$NON-NLS-1$
        File  file = new File(Core.getCore().getDataFolder(), "webchain.xml"); //$NON-NLS-1$
        fop = new FileOutputStream(file);

        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }

        // get the content in bytes
        String xmlString = result.getWriter().toString();
        // System.out.println(xmlString);
        byte[] contentInBytes = xmlString.getBytes();

        fop.write(contentInBytes);
        fop.flush();
        fop.close();
	}

	
	private void loadWebchainXML() throws Exception {
			
			File fXmlFile = new File(Core.getCore().getDataFolder(),"webchain.xml"); //$NON-NLS-1$
					
			//File fXmlFile = new File("D:/Eigene Dateien/smashii/workspace/Icarus/data/webchain.xml"); //$NON-NLS-1$
			if(!fXmlFile.exists() || fXmlFile.length()==0) {
				return;
			}
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();
			
			Webchain webchain;

			//System.out.println("Root element :"	+ doc.getDocumentElement().getNodeName());

			NodeList servicesList = doc.getElementsByTagName("Chain"); //$NON-NLS-1$
			for (int i = 0; i < servicesList.getLength(); i++) {
				
				Node sNode = servicesList.item(i);
				
				//System.out.println("\nCurrent Element :" + sNode.getNodeName());							

				if (sNode.getNodeType() == Node.ELEMENT_NODE) {
					webchain = new Webchain();
					
					Element eElement = (Element) sNode;
					
					//System.out.println("Chainname : " +	eElement.getAttribute("chainname"));
					webchain.setName(eElement.getAttribute("chainname"));	 //$NON-NLS-1$

					
					/*
					System.out.println("Type: "
							+ getNodeFromElement(eElement, "input", "type")
							+ " Value: "
							+ getTextFromElement(eElement, "input"));
					*/
					
					WebchainInputType inputType = new WebchainInputType();
					inputType.setInputType(getNodeFromElement(eElement, "input", "type")); //$NON-NLS-1$ //$NON-NLS-2$
					
					if (inputType.getInputType().equals("static")){ //$NON-NLS-1$
						inputType.setInputTypeValue(getTextFromElement(eElement, "input")); //$NON-NLS-1$
					}
					
					if (inputType.getInputType().equals("location")){ //$NON-NLS-1$
						inputType.setInputTypeValue(getTextFromElement(eElement, "input")); //$NON-NLS-1$
					}
					
					if (inputType.getInputType().equals("dynamic")){ //$NON-NLS-1$
						inputType.setInputTypeValue(""); //$NON-NLS-1$
					}
					
					webchain.setWebchainInputType(inputType);
					
					//addinput
					webchain.addWebchainElement(inputType);
					
					//Grab all chainelements from chain
					NodeList uidList = eElement.getElementsByTagName("chainelement"); //$NON-NLS-1$
					
					for (int j = 0; j < uidList.getLength(); j++) {						
						Node uidNode = uidList.item(j);
						//System.out.println("Chainname : " +	eElement.getAttribute("uname"));
						if (uidNode.getNodeType() == Node.ELEMENT_NODE) {
							Element idElement = (Element) uidNode;
							String uniqueID = idElement.getAttribute("uid"); //$NON-NLS-1$
							
							//we found an output element
							if (uniqueID.equals("")){ //$NON-NLS-1$								
								webchain.addWebchainElement(
										new WebchainOutputType(
												idElement.getAttribute("type"), //$NON-NLS-1$
												idElement.getAttribute("value"), //$NON-NLS-1$
												getBoolean(idElement.getAttribute("outputused")))); //$NON-NLS-1$
												
							}
							
							//webservice
							else {
								webchain.addWebchainElement(uniqueID);
								webchain.addWebservice(uniqueID);
							}
						}
					}					
										
					addNewWebchain(webchain);
					
					
					// reset change flag after loading chains - otherwise we will
					// get unsaved changes dialog even after loading chains from file
					hasChanges = false;
				}
			}			
	}
	
	

	/**
	 * @param attribute
	 * @return
	 */
	private boolean getBoolean(String attribute) {
		if (attribute.equals("true")) return true; //$NON-NLS-1$
		return false;
	}

	/**
	 * e is XML Element, s is the Name of our Node Element
	 * 
	 * @param e
	 * @param s
	 * @return xml node text content
	 */
	private String getTextFromElement(Element e, String s) {
		return e.getElementsByTagName(s).item(0).getTextContent();
	}
	
	/**
	 * 
	 * @param e
	 * @param s
	 * @return
	 */
	private String getNodeFromElement(Element e, String s, String item) {
		return e.getElementsByTagName(s).item(0).getAttributes().getNamedItem(item).getNodeValue();
	}
	
	/**
	 * @return the hasChanges
	 */
	public boolean isHasChanges() {
		return hasChanges;
	}
	
	
	
	/**
	 * 
	 * @param uniquename
	 * @param list
	 */
	public void addNewWebchain(Webchain webchain){
		webchainList.add(webchain);
		int index = indexOfWebchain(webchain);
		hasChanges=true;
		eventSource.fireEvent(new EventObject(Events.ADDED,
				"webchain",webchain, //$NON-NLS-1$
				"index",index));//$NON-NLS-1$
	}
	
	public void deleteWebchain(Webchain webchain){
		int index = indexOfWebchain(webchain);
		if (webchainList.remove(webchain)){
			hasChanges=true;
			eventSource.fireEvent(new EventObject(Events.REMOVED,
					"webchain",webchain, //$NON-NLS-1$
					"index",index)); //$NON-NLS-1$
		}
	}
	
	public int indexOfWebchain(Webchain webchain){
		return webchainList.indexOf(webchain);
	}

	
	public int getWebchainCount(){
		return webchainList.size();
	}
	
	public Webchain getWebchainAt(int index){
		return webchainList.get(index);
	}

	
	
	
	/**
	 * @param name
	 */
	public Webchain createWebchain(String name, WebchainInputType inputType) {
		Webchain webchain = new Webchain();
		webchain.setName(name);	
		webchain.setWebchainInputType(inputType);
		//addinput to chainelements
		webchain.addWebchainElement(inputType);
		return webchain;
		//addNewWebchain(webchain);
	}
	
	
	/**
	 * 
	 * @param name
	 * @param oldWebchain
	 */
	public void cloneWebchain(String name, Webchain old) {
		Webchain webchain = new Webchain();
		webchain.setName(name);
		
		for(int i = 0; i < old.getElementsCount();i++){
			if(old.getElementAt(i) instanceof WebserviceProxy){
				WebserviceProxy wp = (WebserviceProxy) old.getElementAt(i);
				webchain.addWebchainElement(wp.get().getUID());
				webchain.addWebservice(wp.get().getUID());
			}			
			if(old.getElementAt(i) instanceof WebchainInputType){
				webchain.addWebchainElement((WebchainInputType) old.getElementAt(i));
				WebchainInputType wi = new WebchainInputType();
				wi.setInputType(old.getWebchainInputType().getInputType());
				wi.setInputTypeValue(old.getWebchainInputType().getInputTypeValue());
				webchain.setWebchainInputType(wi);
			}
			if(old.getElementAt(i) instanceof WebchainOutputType){
				webchain.addWebchainElement((WebchainOutputType) old.getElementAt(i));
			}
		}
		addNewWebchain(webchain);	
	}
	
	
	protected void hasChange(Webchain webchain){
		int index = indexOfWebchain(webchain);
		hasChanges=true;
		eventSource.fireEvent(new EventObject(Events.CHANGED,
				"webchain",webchain //$NON-NLS-1$
				,"index",index));//NON-NLS-1$ //$NON-NLS-1$
		
	}
	
	public void setName(Webchain webchain, String name) {
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		if(name.equals(webchain.getName())) {
			return;
		}
		
		webchain.setName(name);
		hasChange(webchain);
	}
	
	
	/**
	 * @param webchain
	 * @param webchainElements
	 */
	public void setWebchainElements(Webchain webchain, List<WebchainElements> webchainElements) {

		//prepare Proxylist
		//clearAllWebservices(webchain);
		
		if (webchainElements==null){			
			return;
		}
		
		
		boolean refreshAll = webchain.getElementsCount() != webchainElements.size();
		System.out.println(refreshAll);
		//lists are different replace old webchainelement list
		webchain.setNewChainlist(webchainElements);

		//fire event to refresh webchaintree with the latest data
		if (refreshAll){
			int index = indexOfWebchain(webchain);
			hasChanges=true;
			//TODO correct Event
			eventSource.fireEvent(new EventObject(Events.CHANGE,
					"webchain",webchain //$NON-NLS-1$
					,"index",index));//NON-NLS-1$ //$NON-NLS-1$
		} else {
			hasChange(webchain);
		}
	}
	
	
	
	
	/**
	 * @param webchain 
	 * @param nameFromSelectedButton
	 * @param text
	 */
	public void setWebserviceInput(Webchain webchain, String type, String value) {
		if (type==null){			
			return;
		}
		WebchainInputType chainInput = new WebchainInputType();
		chainInput.setInputType(type);
		chainInput.setInputTypeValue(value);
		webchain.setWebchainInputType(chainInput);
		hasChange(webchain);
	}

	//compare 2 webservice element lists
	public boolean equalElements(List<WebchainElements> w1, List<WebchainElements> w2){
		
		boolean equal = true;
		if (w1.size() != w2.size()){
			return false;
		} else {
			//same listsize we need to check items
			for (int i = 0; i < w1.size(); i++){
				
				//TODO Check equal = replace with return?! - should also work that way
				if (w1.get(i) instanceof WebserviceProxy
						&& w2.get(i) instanceof WebserviceProxy) {
					
					if (!(((WebserviceProxy) w1.get(i)).getServiceID()
							.equals(((WebserviceProxy) w2.get(i)).getServiceID()))){
						equal = false;
					}
					//System.out.println("proxy" + equal);
				}
				
				//Check Input
				if (w1.get(i) instanceof WebchainInputType
						&& w2.get(i) instanceof WebchainInputType) {
					equal = compareWebchainInputType(
								(WebchainInputType) w1.get(i), 
								(WebchainInputType) w2.get(i));
					//System.out.println("wio" + equal);
				}
				
				// Check Output
				if (w1.get(i) instanceof WebchainOutputType
						&& w2.get(i) instanceof WebchainOutputType) {

					equal = compareWebchainOutputType(
							(WebchainOutputType) w1.get(i),
							(WebchainOutputType) w2.get(i));
					// System.out.println("wot" + equal);
				}
				
				if(!equal){
					//System.out.println("Changes " + w1.get(i) + w2.get(i));
					return equal;
				}
				
			}
		}
		return equal;		
	}
	
	/**
	 * Compare 2 Webchain Input Types
	 * @param t1
	 * @param t2
	 * @return
	 */
	public boolean compareWebchainInputType(WebchainInputType t1, WebchainInputType t2){
		if (t1.equals(t2)){
			return true;			
		}
		return false;
		
	}
	
	
	/**
	 * Compare 2 Webchain Output Types
	 * @param t1
	 * @param t2
	 * @return
	 */
	public boolean compareWebchainOutputType(WebchainOutputType t1, WebchainOutputType t2){
		//same type, value and activatestatus
		if (t1.equals(t2)){
			return true;			
		}
		return false;
		
	}


	//check if webservice occurs in any chain before delete possible
	public boolean webserviceUsed(Webservice webservice){
		for(int i = 0; i < getWebchainCount(); i++){
			
			for (int j = 0; j < getWebchainAt(i).getWebserviceCount();j++){
				Webservice w = (Webservice) getWebchainAt(i).getWebserviceAt(j).get();
				if (w.equals(webservice)) return true;
			}
		}
		return false;
	}
	
	//return first chain webservice is used in
	public Webchain webserviceFirstOccurence(Webservice webservice){
		for(int i = 0; i < getWebchainCount(); i++){
			for (int j = 0; j < getWebchainAt(i).getWebserviceCount();j++){
				Webservice w = (Webservice) getWebchainAt(i).getWebserviceAt(j).get();
				if (w.equals(webservice)) return getWebchainAt(i);
			}
		}
		return null;
	}
	
	
	// return webservice chain query
	public List<String> getQueryFromWebchain(Webchain webchain){
		List<String> query = new ArrayList<>();	
		String type = null;		
		//collect webservices
		for (int i = 0; i < webchain.getWebserviceCount(); i++){
			Webservice webservice = webchain.getWebserviceAt(i).get();
			
			//collect output attributes
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < webservice.getOutputAttributesSize(); j++){
				WebserviceIOAttributes attribute = webservice.getOutputAttributesAt(j);	
				
				if (attribute.getAttributename().equals("type")){ //$NON-NLS-1$
					type = //attribute.getAttributename() +"="+
							attribute.getAttributevalues();
					//System.out.println("Last Webchain Type " + type);
				} else {

				if (attribute.getAttributevalues().equals("")){ //$NON-NLS-1$
					sb.append(attribute.getAttributename());
				} else {
					sb.append(attribute.getAttributename()).append("=") //$NON-NLS-1$
						.append(attribute.getAttributevalues()); 
				}				
				
				//only add if type not in list
				if (!query.contains(sb.toString())){
					query.add(sb.toString());
				}
				sb.delete(0, sb.length());
				}
			} 
		}
		
		//add type from last item
		query.add(type);
		
		/*
		for (int i = 0; i< query.size(); i++){
			System.out.println(query.get(i));
		}
		*/
		
		//System.out.println(query.get(query.size()-1));
		return query;
	}
	
	
	
	
	public boolean hasChainOutput(Webchain webchain){
		for(int i = 0; i < webchain.getElementsCount(); i++){
			if (webchain.getElementAt(i) instanceof WebchainOutputType){
				return true;
			}			
		}
		return false;
	}
	

	
	

	//##############################
	private static Pattern indexPattern;

	public String getUniqueName(String baseName) {
		Set<String> usedNames = new HashSet<>(webchainList.size());
		for(int i = 0; i< webchainList.size(); i++) {
			usedNames.add(webchainList.get(i).getName());
		}
	
		String name = baseName;
		int count = 2;
		
		if(indexPattern==null) {
			indexPattern = Pattern.compile("\\((\\d+)\\)$"); //$NON-NLS-1$
		}
		
		Matcher matcher = indexPattern.matcher(baseName);
		if(matcher.find()) {
			int currentCount = 0;
			try {
				currentCount = Integer.parseInt(matcher.group(1));
			} catch(NumberFormatException e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to parse existing base name index suffix: "+baseName, e); //$NON-NLS-1$
			}
			
			count = Math.max(count, currentCount+1);
			baseName = baseName.substring(0, baseName.length()-matcher.group().length()).trim();
		}
		
		if(usedNames.contains(name)) {
			while(usedNames.contains((name = baseName+" ("+count+")"))) { //$NON-NLS-1$ //$NON-NLS-2$
				count++;
			}
		}
		
		return name;
	}

	/**
	 * @see de.ims.icarus.ui.events.EventSource#addListener(java.lang.String, de.ims.icarus.ui.events.EventListener)
	 */
	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * @see de.ims.icarus.ui.events.EventSource#removeListener(de.ims.icarus.ui.events.EventListener)
	 */
	public void removeListener(EventListener listener) {
		eventSource.removeListener(listener);
	}

	/**
	 * @see de.ims.icarus.ui.events.EventSource#removeListener(de.ims.icarus.ui.events.EventListener, java.lang.String)
	 */
	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}
	
	
	
	
	public static void main(String[] args) {
		
		WebchainRegistry wcl = new WebchainRegistry();
		//List<String> list = WebchainRegistry.getInstance().getQueryFromWebchain(w);
		try {
			//wcl.saveWebchains();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(wcl.getWebchainAt(0).getElementsCount());;
		System.out.println("fertig"); //$NON-NLS-1$

	}
	
}
