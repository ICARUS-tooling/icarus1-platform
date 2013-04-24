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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class Webchain {
	

	protected List<WebserviceProxy> webserviceProxyList = new ArrayList<>();
	protected List<WebchainElements> webchainElementsList = new ArrayList<>();
	
	protected String name;
	protected String description;
	protected WebchainInputType inputType;
	
	

	public Webchain(){
		//no-op
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	

	//return procylist
	List<WebserviceProxy> getWebserviceProxyList() {
		return webserviceProxyList;
	}

	public int getWebserviceCount(){
		return webserviceProxyList.size();
	}
	
	public int indexOfWebservice(WebserviceProxy webserviceProxy){
		return webserviceProxyList.indexOf(webserviceProxy);
	}


	public WebserviceProxy getWebserviceAt(int index){
		return webserviceProxyList.get(index);	
	}
	
	
	public void addWebservice(String serviceID){
		webserviceProxyList.add(new WebserviceProxy(serviceID));
	}
	
	
	
	public boolean inListWebserviceProxy(Webservice webservice){
		for (int i = 0; i < getWebserviceCount(); i++){
			if (getWebserviceAt(i).get().equals(webservice)) return true;
		}
		return false;
			
	}
	
	
	
	
	public void addWebservices(List<String> serviceIDList){
		for (int i = 0; i < serviceIDList.size(); i++){
			addWebservice(serviceIDList.get(i));
		}
	}
	
	
	public List<String> getWebservices(Webchain webchain){
		List<String> serviceIDList = new ArrayList<String>();
		for (int i = 0; i < getWebserviceCount(); i++){
			serviceIDList.add(getWebserviceAt(i).get().getUID());
		}
		return serviceIDList;
	}
	
	
	public void setWebchainInputType(WebchainInputType chainInput) {
		this.inputType = chainInput;
	}
	
	public WebchainInputType getWebchainInputType(){
		return inputType;
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
	

	/**
	 * @return
	 */
	public List<Webservice> getFilteredWebservices(Webchain webchain) {
		List<Webservice> filtered = new ArrayList<>();
		List<String> idList = webchain.getWebservices(webchain);
		int webservicesCount = WebserviceRegistry.getInstance().getWebserviceCount();

		for (int i = 0; i < webservicesCount; i++) {
			Webservice webservice = WebserviceRegistry.getInstance().getWebserviceAt(i);
			if (!(idList.contains(webservice.getUID()))){
				filtered.add(webservice);				
			};

		}

		return filtered;
	}
	

	/**
	 * Input Attribute
	 * @param wit
	 */
	public void addWebchainElement(WebchainInputType wit) {		
		webchainElementsList.add((WebchainElements) wit);
	}
	
	
	/**
	 * Output Attribute
	 * @param wot
	 */
	public void addWebchainElement(WebchainOutputType wot) {		
		webchainElementsList.add((WebchainElements) wot);
	}
	
	
	/**
	 * Webservice
	 * @param serviceID
	 */
	public void addWebchainElement(String serviceID) {
		webchainElementsList.add((WebchainElements) new WebserviceProxy(serviceID));
		
	}	
	
	public int getElementsCount(){
		return webchainElementsList.size();
	}
	
	public int indexOfElement(WebchainElements webchainElements){
		return webchainElementsList.indexOf(webchainElements);
	}
	
	public WebchainElements getElementAt(int index){
		return webchainElementsList.get(index);	
	}	


}
