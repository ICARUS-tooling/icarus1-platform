/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.config;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class ConfigDelegate implements ConfigListener {
	
	private final Handle handle;
	
	private List<ChangeListener> listeners;

	protected ConfigDelegate(Handle handle) {
		if(handle==null)
			throw new IllegalArgumentException("Invalid handle"); //$NON-NLS-1$
		
		this.handle = handle;
		
		register();
	}
	
	protected ConfigDelegate(String path, ConfigRegistry registry) {
		if(path==null)
			throw new IllegalArgumentException("Invalid path"); //$NON-NLS-1$
		
		if(registry==null) {
			registry = ConfigRegistry.getGlobalRegistry();
		}
		
		this.handle = registry.getHandle(path);
		
		register();
	}
	
	protected void register() {
		ConfigRegistry registry = handle.getSource();
		
		registry.addGroupListener(handle, this);
	}
	
	public Handle getHandle() {
		return handle;
	}
	
	public void addChangeListener(ChangeListener listener) {
		if(listeners==null) {
			listeners = new ArrayList<>();
		}
		
		listeners.add(listener);
	}
	
	public void removeChangeListener(ChangeListener listener) {
		if(listeners==null) {
			return;
		}
		
		listeners.remove(listener);
	}
	
	public void notifyListeners() {
		if(listeners==null) {
			return;
		}
		
		ChangeEvent evt = new ChangeEvent(this);
		Object[] listeners = this.listeners.toArray();
		for(Object listener : listeners) {
			((ChangeListener)listener).stateChanged(evt);
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.config.ConfigListener#invoke(net.ikarus_systems.icarus.config.ConfigRegistry, net.ikarus_systems.icarus.config.ConfigEvent)
	 */
	@Override
	public void invoke(ConfigRegistry sender, ConfigEvent event) {
		reload();
		notifyListeners();
	}
	
	/**
	 * Called by {@link #invoke(ConfigRegistry, ConfigEvent)} before listeners
	 * are being notified to enable subclasses to process configuration data
	 * before publishing it. In addition it can serve as access point for 
	 * external entities to force a refresh of internal data without further
	 * notifications being passed to listeners.
	 */
	public void reload() {
		// no-op
	}
	
	public abstract Object getValue(String name);
	
	@SuppressWarnings("unchecked")
	public <T extends Object> T getValue(String name, T defaultValue) {
		Object value = getValue(name);
		return value==null ? defaultValue : (T)value;
	}
	
	public String getString(String name) {
		Object value = getValue(name);
		return value==null ? "" : (String)value; //$NON-NLS-1$
	}
	
	public int getInteger(String name) {
		Object value = getValue(name);
		return value==null ? 0 : (int)value;
	}
	
	public boolean getBoolean(String name) {
		Object value = getValue(name);
		return value==null ? false : (boolean)value;
	}
	
	public float getFloat(String name) {
		Object value = getValue(name);
		return value==null ? 0f : (float)value;
	}
	
	public double getDouble(String name) {
		Object value = getValue(name);
		return value==null ? 0d : (double)value;
	}
	
	public long getLong(String name) {
		Object value = getValue(name);
		return value==null ? 0L : (long)value;
	}
}
