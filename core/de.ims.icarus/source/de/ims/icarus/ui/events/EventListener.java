/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/events/EventListener.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.ui.events;

/**
 * @author Markus Gärtner 
 * @version $Id: EventListener.java 7 2013-02-27 13:18:56Z mcgaerty $
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
