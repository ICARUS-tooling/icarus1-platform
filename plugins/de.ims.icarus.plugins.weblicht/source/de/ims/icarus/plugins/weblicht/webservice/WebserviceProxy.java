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
