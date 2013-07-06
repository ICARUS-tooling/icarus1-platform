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

import net.ikarus_systems.icarus.util.Wrapper;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebserviceProxy implements Wrapper<Webservice>, WebchainElements {
	private final String uniqueServiceID;
	
	
	/**
	 * 
	 */
	public WebserviceProxy(String uniqueServiceID) {
		this.uniqueServiceID = uniqueServiceID;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return get().toString();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Wrapper#get()
	 */
	@Override
	public Webservice get() {
		return WebserviceRegistry.getInstance().getWebserviceFromUniqueID(uniqueServiceID);
	}
	
	public String getServiceID(){
		return uniqueServiceID;
	}
	


}
