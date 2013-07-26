/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.list;

import javax.swing.ListModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class ListUtils {

	private ListUtils() {
		// no-op
	}

	public static <T extends Object> int indexOf(T item, ListModel<T> model) {
		if(model==null)
			throw new IllegalArgumentException("Invalid list model"); //$NON-NLS-1$
		
		if(item==null) {
			return -1;
		}
		
		for(int i=0; i<model.getSize(); i++) {
			if(item.equals(model.getElementAt(i))) {
				return i;
			}
		}
		
		return -1;
	}
}
