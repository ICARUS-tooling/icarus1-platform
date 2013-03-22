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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.ikarus_systems.icarus.Core;
import net.ikarus_systems.icarus.language.corpus.Corpus;
import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.EventSource;
import net.ikarus_systems.icarus.ui.events.Events;
import net.ikarus_systems.icarus.ui.events.WeakEventSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebchainRegistry {	

	protected List<Webchain> webchainList;
	
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
	
	private static Logger logger;
	
	private static Logger getLogger() {
		if(logger==null) {
			logger = LoggerFactory.getLogger(WebchainRegistry.class);
		}
		return logger;
	}
	
	//--

	
	private WebchainRegistry() {
		//webservice = new Webservice();
		eventSource = new WeakEventSource(this);
		//webchain = new Webchain();
		webchainList = new ArrayList<Webchain>();
		
		// Attempt to load webservice list
		try {			
			loadWebchainXML();			
		} catch (Exception e) {
			getLogger().log(LoggerFactory.record(Level.SEVERE, 
					"Failed to load webservice chain list", e)); //$NON-NLS-1$
		}

		//System.out.println(webchainList.get(0).getWebserviceIDList());	
		//System.out.println(webchainList.get(1).getWebserviceIDList());	
	}
	
	private void loadWebchainXML() throws Exception {
			
			//File fXmlFile = new File(Core.getCore().getDataFolder(),"webchain.xml"); //$NON-NLS-1$
			
			
		File fXmlFile = new File(
				"D:/Eigene Dateien/smashii/workspace/Icarus/data/webchain.xml"); //$NON-NLS-1$
			if(!fXmlFile.exists() || fXmlFile.length()==0) {
				return;
			}
			
			List<WebserviceProxy> webserviceProxyList;
			
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
					
					//System.out.println("Chainname : " +	eElement.getAttribute("uname"));
					webchain.setName(eElement.getAttribute("uname"));					 //$NON-NLS-1$
					
					//Grab all unique webservice numbers from chain
					NodeList uidList = eElement.getElementsByTagName("service"); //$NON-NLS-1$
					
					webserviceProxyList = new ArrayList<>();
					for (int j = 0; j < uidList.getLength(); j++) {						
						Node uidNode = uidList.item(j);
						//System.out.println("Chainname : " +	eElement.getAttribute("uname"));
						if (uidNode.getNodeType() == Node.ELEMENT_NODE) {
							Element idElement = (Element) uidNode;
							String uniqueID = idElement.getAttribute("uid");							 //$NON-NLS-1$
							webchain.addWebservice(uniqueID);
						}							
					}	
					addNewWebchain(webchain);
				}
			}			
	}
	
	/**
	 * 
	 * @param uniquename
	 * @param list
	 */
	public void addNewWebchain(Webchain webchain){
		webchainList.add(webchain);
		int index = indexOfWebchain(webchain);
		eventSource.fireEvent(new EventObject(Events.ADDED,
				"webchain",webchain, //$NON-NLS-1$
				"index",index));//$NON-NLS-1$
	}
	
	public void deleteWebchain(Webchain webchain){
		int index = indexOfWebchain(webchain);
		if (webchainList.remove(webchain)){
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
	public void newWebchain(String name) {
		Webchain webchain = new Webchain();
		webchain.setName(name);	
		addNewWebchain(webchain);
	}
	
	/**
	 * @param name
	 * @param webservicesList
	 */
	public void newWebchain(String name, List<String> serviceIDList) {
		Webchain webchain = new Webchain();
		webchain.setName(name);	
		webchain.addWebservices(serviceIDList);
		addNewWebchain(webchain);	
		
	}

	public void setName(Webchain webchain, String name) {
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		if(name.equals(webchain.getName())) {
			return;
		}
		
		webchain.setName(name);
		int index = indexOfWebchain(webchain);
		eventSource.fireEvent(new EventObject(Events.CHANGED,
				"webchain",webchain, //$NON-NLS-1$
				"index",index));//$NON-NLS-1
	}
	
	
	/**
	 * @param webchain
	 * @param webservices
	 */
	public void setWebservices(Webchain webchain, List<Webservice> webservices) {
		
		//prepare Proxylist
		//clearAllWebservices(webchain);
		
		if (webservices==null){			
			return;
		}
		
		List<WebserviceProxy> wspList = new ArrayList<WebserviceProxy>();
		
		WebserviceProxy wsp = null;
		for(int i = 0; i < webservices.size();i++){
			//System.out.println("UID " + webservices.get(i).getUID());
			wsp = new WebserviceProxy(webservices.get(i).getUID());
			wspList.add(wsp);
		}	
		
		//compare lists if there are no changes do nothing
		if(equalsProxy(webchain.webserviceProxyList, wspList)){
			return;
		}

		//lists are different replace old proxy list
		webchain.webserviceProxyList = wspList;
		
		int index = indexOfWebchain(webchain);
		eventSource.fireEvent(new EventObject(Events.CHANGE,
				"webchain",webchain, //$NON-NLS-1$
				"index",index));//$NON-NLS-1$
	}
	
	
	//compare 2 webservice proxy lists
	private boolean equalsProxy(List<WebserviceProxy> w1, List<WebserviceProxy> w2){
		boolean equal = true;
		if (w1.size() != w2.size()){
			return false;
		} else {
			//same lsitsize we need to check proxyitems
			for (int i = 0; i < w1.size(); i++){
				if (!(w1.get(i).getServiceID().equals(w2.get(i).getServiceID()))){
					equal = false;
				};
			}
		}
		return equal;		
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
					sb.append(attribute.getAttributename()).append("=").append(attribute.getAttributevalues());
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
				getLogger().log(LoggerFactory.record(Level.SEVERE, 
						"Failed to parse existing base name index suffix: "+baseName, e)); //$NON-NLS-1$
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
	
	public static void main(String[] args) {
		
		WebchainRegistry wcl = new WebchainRegistry();
		Webchain w = WebchainRegistry.getInstance().getWebchainAt(0);
		List<String> list = WebchainRegistry.getInstance().getQueryFromWebchain(w);
		System.out.println("fertig"); //$NON-NLS-1$

	}
	
}
