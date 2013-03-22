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
	
	protected String name;
	protected String description;
	
	/*
	protected Map<String,List<WebserviceProxy>> chainMap;

	Map<String,List<WebserviceProxy>> getWebserviceMap() {
		return chainMap;
	}*/
	
	
	public Webchain(){
		//noop
	}

	/**
	 * @return the webserviceIDList
	 */
	List<WebserviceProxy> getWebserviceProxyList() {
		return webserviceProxyList;
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
	
	
	public void addWebservices(List<String> serviceIDList){
		for (int i = 0; i < serviceIDList.size(); i++){
			addWebservice(serviceIDList.get(i));
		}
	}
	
	
	public List<String> getWebservices(Webchain webchain){
		List<String> serviceIDList = new ArrayList<String>();
		for (int i = 0; i < getWebserviceCount(); i++){
			serviceIDList.add(getWebserviceAt(i).getServiceID());
		}
		return serviceIDList;
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}


}
