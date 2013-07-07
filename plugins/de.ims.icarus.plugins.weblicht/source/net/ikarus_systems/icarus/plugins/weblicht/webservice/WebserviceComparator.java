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

import java.util.Comparator;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebserviceComparator implements Comparator<Object> {
	
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Object w1, Object w2) {
		return ((Webservice) w1).getName().compareTo(((Webservice) w2).getName());
	}

}
