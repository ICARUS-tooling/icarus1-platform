/**
 * $Id: mxEventObject.java,v 1.4 2009-12-01 15:21:30 gaudenz Exp $
 * Copyright (c) 2007, Gaudenz Alder
 */
package net.ikarus_systems.icarus.ui.events;

import java.util.Hashtable;
import java.util.Map;

/**
 * Base class for objects that dispatch named events.
 * @author Markus Gärtner
 */
public class EventObject {

	/**
	 * Holds the name of the event.
	 */
	protected String name;

	/**
	 * Holds the properties of the event.
	 */
	protected Map<String, Object> properties;

	/**
	 * Holds the consumed state of the event. Default is false.
	 */
	protected boolean consumed = false;

	/**
	 * Constructs a new event for the given name.
	 */
	public EventObject(String name) {
		this(name, (Object[]) null);
	}

	/**
	 * Constructs a new event for the given name and properties. The optional
	 * properties are specified using a sequence of keys and values, eg.
	 * {@code new mxEventObject("eventName", key1, val1, .., keyN, valN))}
	 */
	public EventObject(String name, Object... args) {
		this.name = name;
		properties = new Hashtable<String, Object>();

		if (args != null) {
			for (int i = 0; i < args.length; i += 2) {
				if (args[i + 1] != null) {
					properties.put(String.valueOf(args[i]), args[i + 1]);
				}
			}
		}
	}

	/**
	 * Returns the name of the event.
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * 
	 */
	public Object getProperty(String key) {
		return properties.get(key);
	}

	/**
	 * Returns true if the event has been consumed.
	 */
	public boolean isConsumed() {
		return consumed;
	}

	/**
	 * Consumes the event.
	 */
	public void consume() {
		consumed = true;
	}

	public static EventObject propertyEvent(String name, Object oldValue, Object newValue) {
		return new EventObject(Events.PROPERTY, "property", name,  //$NON-NLS-1$
				"oldValue", oldValue, "newValue", newValue); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
