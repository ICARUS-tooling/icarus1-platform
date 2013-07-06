/*
 * $Revision: 23 $
 * $Date: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/events/WeakEventSource.java $
 *
 * $LastChangedDate: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $ 
 * $LastChangedRevision: 23 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.ui.events;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.ims.icarus.util.Exceptions;


/**
 * Extension of the basic {@code EventSource} class to allow
 * for storing of weak references to the listeners being
 * registered. This implementation does {@code not} keep
 * strong references to registered listeners so that listeners
 * which are not strongly referenced from their origin can be
 * gc-ed at any time.
 * 
 * @author Markus Gärtner
 * @version $Id: WeakEventSource.java 23 2013-04-17 12:39:04Z mcgaerty $
 *
 */
public class WeakEventSource extends EventSource {

	/**
	 * Constructs a new event source using this as the source object.
	 */
	public WeakEventSource() {
		super(null);
	}

	/**
	 * Constructs a new event source for the given source object.
	 */
	public WeakEventSource(Object source) {
		super(source);
	}

	/**
	 * Registers the given {@code listener} for events of the
	 * specified {@code eventName} or as a listener for all
	 * events in the case the {@code eventName} parameter is {@code null}
	 * @param eventName name of events to listen for or {@code null} if
	 * the listener is meant to receive all fired events
	 * @param listener the {@code EventListener} to be registered
	 */
	@Override
	public void addListener(String eventName, EventListener listener) {
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		if (eventListeners == null) {
			eventListeners = new ArrayList<>();
		}

		eventListeners.add(new Entry(listener, eventName));
	}

	/**
	 * Removes from the list of registered listeners all pairs
	 * matching the given combination of {@code EventListener}
	 * and {@code eventName}. If {@code eventName} is {@code null}
	 * then all occurrences of the given {@code listener} will be
	 * removed.
	 * @param listener
	 * @param eventName
	 */
	@Override
	public void removeListener(EventListener listener, String eventName) {
		if (eventListeners != null) {
			for (int i=eventListeners.size()-1; i>-1; i--) {
				Entry entry = (Entry)eventListeners.get(i);
				if(entry==null) {
					continue;
				}
				if(entry.ref==null || entry.ref.get()==null || (entry.ref.get()==listener
						&& (eventName==null || String.valueOf(
								entry.eventName).equals(eventName)))) {
					entry.delete();
					eventListeners.set(i, null);
				}
			}
		}
	}

	/**
	 * Dispatches the given {@code event} to all registered {@code EventListener}s
	 * that listen to the name of this {@code EventObject} or that are registered
	 * as {@code 'catch all'}-listeners
	 * @param event
	 * @param sender
	 */
	@Override
	public void fireEvent(EventObject event, Object sender) {
		if (eventListeners != null && !eventListeners.isEmpty()
				&& isEventsEnabled()) {
			
			// Ensure a valid non-null source!
			if (sender == null)
				sender = getEventSource();
			if (sender == null)
				sender = this;
			
			for (int i = 0; i<eventListeners.size(); i++) {
				Entry entry = (Entry) eventListeners.get(i);
				if(entry==null || entry.ref==null) {
					deadListenerCount++;
					continue;
				}
				EventListener listener = entry.ref.get();
				if(listener==null) {
					deadListenerCount++;
				} else if(entry.eventName==null || entry.eventName.equals(event.getName())) {
					listener.invoke(sender, event);
				}
			}
		}
		
		if(deadListenerCount>=deadListenerTreshold) {
			clearEventListeners();
		}
	}
	
	@Override
	protected void clearEventListeners() {
		for(int i=eventListeners.size()-1; i>-1; i--) {
			if(eventListeners.get(i)==null) {
				eventListeners.remove(i);
			}
		}
		deadListenerCount = 0;
	}

	private class Entry {
		private WeakReference<EventListener> ref;
		private String eventName;
		
		private Entry(EventListener listener, String eventName) {
			this.ref = new WeakReference<EventListener>(listener);
			this.eventName = eventName;
		}
		
		private void delete() {
			ref.clear();
		}
	}
}