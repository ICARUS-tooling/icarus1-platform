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

import java.awt.Component;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Editor<T extends Object> {
	
	Component getEditorComponent();
	
	void setEditingItem(T item);
	
	T getEditingItem();
	
	void resetEdit();
	
	void applyEdit();
	
	boolean hasChanges();
	
	void close();
}
