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


	/**
	 * Returns the number of {@code SentenceData} objects that
	 * this {@code SentenceDataList} currently contains.
	 */
	int size();

	T get(int index);
	
	ContentType getContentType();
	
	/**
	 * Adds a {@code ChangeListener} that will be notified when the number of
	 * entries in this {@code SentenceDataList} changes.
	 */
	void addChangeListener(ChangeListener listener);
	
	/**
	 * @see #addChangeListener(ChangeListener)
	 */
	void removeChangeListener(ChangeListener listener);
}
