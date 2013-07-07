/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.weblicht.webservice;

import de.ims.icarus.util.Wrapper;

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
	 * @see de.ims.icarus.util.Wrapper#get()
	 */
	@Override
	public Webservice get() {
		return WebserviceRegistry.getInstance().getWebserviceFromUniqueID(uniqueServiceID);
	}
	
	public String getServiceID(){
		return uniqueServiceID;
	}
	


}
