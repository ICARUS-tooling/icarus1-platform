/**
 * $Id$
 * Copyright (c) 2007, Gaudenz Alder
 */
package de.ims.icarus.ui.events;

import java.util.Hashtable;
import java.util.Map;

import de.ims.icarus.util.collections.CollectionUtils;

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
		return CollectionUtils.getMapProxy(properties);
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(name);
		if(consumed) {
			sb.append(" (consumed)"); //$NON-NLS-1$
		}

		sb.append("["); //$NON-NLS-1$
		for(Map.Entry<String, Object> entry : properties.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		sb.append("]"); //$NON-NLS-1$

		return sb.toString();
	}

	public static EventObject propertyEvent(String name, Object oldValue, Object newValue) {
		return new EventObject(Events.PROPERTY, "property", name,  //$NON-NLS-1$
				"oldValue", oldValue, "newValue", newValue); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
