/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.events;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface EventListener {

	/**
	 * Signals the occurrence 
	 * 
	 * @param sender source of the event, typically an instance of
	 * {@link EventSource}
	 * @param the event to be dispatched 
	 */
	void invoke(Object sender, EventObject event);
}
