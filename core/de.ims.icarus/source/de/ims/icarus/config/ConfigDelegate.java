/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.config;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.config.ConfigRegistry.Handle;


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
			throw new NullPointerException("Invalid handle"); //$NON-NLS-1$
		
		this.handle = handle;
		
		register();
	}
	
	protected ConfigDelegate(String path, ConfigRegistry registry) {
		if(path==null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$
		
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
	 * @see de.ims.icarus.config.ConfigListener#invoke(de.ims.icarus.config.ConfigRegistry, de.ims.icarus.config.ConfigEvent)
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
