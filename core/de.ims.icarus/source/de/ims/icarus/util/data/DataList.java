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
public interface DataList<T extends Object> extends DataContainer {


	int size();

	T get(int index);
	
	ContentType getContentType();
	
	/**
	 * Adds a {@code ChangeListener} that will be notified when the number of
	 * entries in this {@code DataList} changes.
	 */
	void addChangeListener(ChangeListener listener);
	
	/**
	 * @see #addChangeListener(ChangeListener)
	 */
	void removeChangeListener(ChangeListener listener);
}
