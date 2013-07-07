/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language;

/**
 * Listener interface to handle changes in {@code MutableSentenceData}
 * objects.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface SentenceDataListener {

	/**
	 * Notifies the listener of a change in an instance of
	 * {@code MutableSentenceData}
	 * @param event the encapsulated change
	 */
	void dataChanged(SentenceDataEvent event);
}
