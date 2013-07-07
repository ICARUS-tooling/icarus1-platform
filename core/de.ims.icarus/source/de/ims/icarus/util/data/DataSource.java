/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util.data;

import javax.swing.event.ChangeListener;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DataSource {

	Object getData();
	
	void addChangeListener(ChangeListener l);
	
	void removeChangeListener(ChangeListener l);
}
