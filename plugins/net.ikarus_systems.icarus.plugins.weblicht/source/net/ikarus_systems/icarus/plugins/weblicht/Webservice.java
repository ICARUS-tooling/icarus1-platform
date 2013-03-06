package net.ikarus_systems.icarus.plugins.weblicht;

import java.util.Collection;

import javax.swing.JComponent;

import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.util.Location;

public interface Webservice {
	
	boolean isEditable();
	
	void setName(String name);
	
	String getName();
	
	
	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#addListener(java.lang.String, net.ikarus_systems.icarus.ui.events.EventListener)
	 */
	void addListener(String eventName, EventListener listener);

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#removeListener(net.ikarus_systems.icarus.ui.events.EventListener)
	 */
	void removeListener(EventListener listener);

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#removeListener(net.ikarus_systems.icarus.ui.events.EventListener, java.lang.String)
	 */
	void removeListener(EventListener listener, String eventName);

	Location getLocation();

	Collection<JComponent> getProperties();

	Object getProperty(String key);

}
