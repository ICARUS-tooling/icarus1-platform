/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core;

import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.events.EventListener;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class FrameDelegate {
	
	abstract IcarusFrame getFrame();

	public abstract ActionManager getActionManager();

	/**
	 * Registers the given {@code listener} for events of the
	 * specified {@code eventName} or as a listener for all
	 * events in the case the {@code eventName} parameter is {@code null}
	 * @param eventName name of events to listen for or {@code null} if
	 * the listener is meant to receive all fired events
	 * @param listener the {@code EventListener} to be registered
	 */
	public abstract void addListener(String eventName, EventListener listener);

	/**
	 * Removes the given {@code EventListener} from all events
	 * it was previously registered for.
	 * @param listener the {@code EventListener} to be removed
	 */
	public abstract void removeListener(EventListener listener);

	/**
	 * Removes from the list of registered listeners all pairs
	 * matching the given combination of {@code EventListener}
	 * and {@code eventName}. If {@code eventName} is {@code null}
	 * then all occurrences of the given {@code listener} will be
	 * removed.
	 * @param listener
	 * @param eventName
	 */
	public abstract void removeListener(EventListener listener, String eventName);
}
