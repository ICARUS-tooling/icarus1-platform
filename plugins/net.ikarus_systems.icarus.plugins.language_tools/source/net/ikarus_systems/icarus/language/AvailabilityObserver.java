/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language;


/**
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface AvailabilityObserver {
	
	void dataAvailable(int index, SentenceData item);
}
