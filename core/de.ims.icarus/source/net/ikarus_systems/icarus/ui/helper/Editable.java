/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.helper;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Editable<T extends Object> {
	
	Editor<T> getEditor();
}
