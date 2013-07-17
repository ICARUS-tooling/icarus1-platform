/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.tab;

import java.awt.Component;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface TabController {

	boolean closeTab(Component comp);
	
	boolean closeChildren(Component comp);
}
