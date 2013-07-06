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
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface AvailabilityObserver {
	
	void dataAvailable(int index, SentenceData item);
}
