/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.id;

import javax.swing.Icon;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface Identity {
	
	String getId();
	
	String getName();
	
	String getDescription();
	
	Icon getIcon();
	
	Object getOwner();
}
