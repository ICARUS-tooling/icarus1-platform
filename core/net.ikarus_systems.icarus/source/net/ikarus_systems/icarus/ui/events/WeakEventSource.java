/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.events;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import net.ikarus_systems.icarus.util.Exceptions;

/**
 * Extension of the basic {@code EventSource} class to allow
 * for storing of weak references to the listeners being
 * registered. This implementation does {@code not} keep
 * strong references to registered listeners so that listeners
 * which are not strongly referenced from their origin can be
 * gc-ed at any time.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class WeakEventSource extends EventSource {
	
	protected transient AtomicInteger deadListenerCount = new AtomicInteger();

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
	public void addListener(String eventName, EventListener listener) {
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		if (eventListeners == null) {
			eventListeners = new LinkedList<>();
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
	public void removeListener(EventListener listener, String eventName) {
		if (eventListeners != null) {
			synchronized (this) {
				for (Iterator<Object> i = eventListeners.iterator(); i.hasNext(); ) {
					Entry entry = (Entry) i.next();
					if(entry.ref.get()==null || (entry.ref.get()==listener
							&& (eventName==null || String.valueOf(
									entry.eventName).equals(eventName)))) {
						entry.delete();
						i.remove();
					}
				}
				deadListenerCount.set(0);
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
	public void fireEvent(EventObject event, Object sender) {
		if (eventListeners != null && !eventListeners.isEmpty()
				&& isEventsEnabled()) {
			
			// ensure a valid non-null source!
			if (sender == null)
				sender = getEventSource();
			if (sender == null)
				sender = this;

			for (Iterator<Object> i = eventListeners.iterator(); i.hasNext(); ) {
				Entry entry = (Entry) i.next();
				EventListener listener = entry.ref.get();
				if(listener==null) {
					deadListenerCount.incrementAndGet();
				} else if(entry.eventName==null || entry.eventName.equals(event.getName())) {
					listener.invoke(sender, event);
				}
			}
		}
		
		if(deadListenerCount.get()>=5) {
			synchronized (this) {
				int removed = 0;
				for(Iterator<Object> i = eventListeners.iterator(); i.hasNext();) {
					Entry entry = (Entry) i.next();
					if(entry.ref.get()==null) {
						entry.delete();
						i.remove();
						removed++;
					}
				}
				deadListenerCount.addAndGet(removed);
			}
		}
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
			ref = null;
		}
	}
}