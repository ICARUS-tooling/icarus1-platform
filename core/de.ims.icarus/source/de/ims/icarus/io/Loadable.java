/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.io;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Loadable {

	boolean isLoaded();
	
	void load() throws Exception;
}
