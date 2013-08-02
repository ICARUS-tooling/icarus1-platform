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

import java.util.List;
import java.util.Stack;

import de.ims.icarus.config.ConfigRegistry.EntryType;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.Exceptions;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConfigBuilder implements ConfigConstants {

	private Stack<Handle> handles;
	private Object owner;
	
	private final ConfigRegistry config;

	public ConfigBuilder() {
		this(ConfigRegistry.getGlobalRegistry());
	}
	
	public ConfigBuilder(ConfigRegistry config) {
		Exceptions.testNullArgument(config, "config"); //$NON-NLS-1$
		
		handles = new Stack<Handle>();
		
		this.config = config;
		reset();
	}
	
	public ConfigRegistry getConfig() {
		return config;
	}
	
	public void reset() {
		reset(config.ROOT_HANDLE, ConfigRegistry.PUBLIC_OWNER);
	}
	
	public void reset(Handle handle) {
		reset(handle, ConfigRegistry.PUBLIC_OWNER);
	}
	
	public void reset(Object owner) {
		reset(config.ROOT_HANDLE, owner);
	}
	
	public void reset(Handle handle, Object owner) {
		if(!handles.isEmpty()) {
			handles.clear();
		}
		
		handles.push(handle);
		this.owner = owner;
	}
	
	public void backTo(int level) {
		check(level);
		
		while(handles.size()>level) {
			handles.pop();
		}
	}
	
	public void back(int offset) {
		backTo(handles.size()-offset);
	}
	
	public void back() {
		back(1);
	}
	
	public int depth() {
		return handles.size();
	}
	
	private void check(int requiredDepth) {
		if(handles.size()<requiredDepth)
			throw new IllegalBuilderOperationException(
					String.format("Cannot perform operation: current depth is %d, need %d",  //$NON-NLS-1$
							handles.size(), requiredDepth));
	}
	
	public void forward(String...names) {
		check(1);
		Handle handle;
		for (String name : names) {
			handle = config.getChildHandle(handles.peek(), name);
			if(handle==null)
				throw new IllegalArgumentException("No such child element: "+name); //$NON-NLS-1$
			if(!config.isGroup(handle))
				throw new IllegalArgumentException("Element is not a group: "+name); //$NON-NLS-1$
			
			handles.push(handle);
		}
	}
	
	public Object getValue(String name) {
		Handle child = config.getChildHandle(handles.peek(), name);
		return child==null ? null : config.getValue(child);
	}
	
	public Handle addGroup(String name) {
		return addGroup(name, null, false);
	}
	
	public Handle addGroup(String name, boolean stepInto) {
		return addGroup(name, null, stepInto);
	}
	
	public Handle addGroup(String name, String mode) {
		return addGroup(name, mode, false);
	}
	
	public Handle addGroup(String name, String mode, boolean stepInto) {
		Handle handle = config.getChildHandle(handles.peek(), name);
		if(handle==null) {
			handle = config.newGroup(owner, handles.peek(), name);
		}
		
		if(mode!=null) {
			config.setProperty(owner, handle, DISPLAY_MODE, mode);
		}
		
		if(stepInto) {
			handles.push(handle);
		}
		
		return handle;
	}
	
	public Handle addEntry(String name, EntryType type) {
		return config.newEntry(owner, handles.peek(), name, type);
	}
	
	public Handle addEntry(String name, EntryType type, Object value, Object defaultValue) {
		return config.newEntry(owner, handles.peek(), name, type, defaultValue, value);
	}
	
	public Handle addEntry(String name, EntryType type, Object value) {
		return config.newEntry(owner, handles.peek(), name, type, value, value);
	}
	
	public Handle addStringEntry(String name) {
		return addEntry(name, EntryType.STRING);
	}
	
	public Handle addStringEntry(String name, String value) {
		return addEntry(name, EntryType.STRING, value);
	}
	
	public Handle addStringEntry(String name, String value, Integer maxLength, Object pattern,
			Boolean multiline) {
		Handle handle = addStringEntry(name, value);
		
		if(maxLength!=null) {
			config.setProperty(owner, handle, MAX_LENGTH, maxLength);
		}
		
		if(pattern!=null) {
			config.setProperty(owner, handle, PATTERN, pattern);
		}
		
		if(multiline!=null) {
			config.setProperty(owner, handle, MULTILINE, multiline);
		}
		
		return handle;
	}
	
	public Handle addBooleanEntry(String name) {
		return addEntry(name, EntryType.BOOLEAN);
	}
	
	public Handle addBooleanEntry(String name, boolean value) {
		return addEntry(name, EntryType.BOOLEAN, value);
	}
	
	public Handle addIntegerEntry(String name) {
		return addEntry(name, EntryType.INTEGER);
	}
	
	public Handle addIntegerEntry(String name, int value) {
		return addEntry(name, EntryType.INTEGER, value);
	}
	
	public Handle addIntegerEntry(String name, int value, Integer minValue, 
			Integer maxValue) {
		return addIntegerEntry(name, value, minValue, maxValue, null, null);
	}
	
	public Handle addIntegerEntry(String name, int value, Integer minValue, 
			Integer maxValue, Integer precision) {
		return addIntegerEntry(name, value, minValue, maxValue, precision, null);
	}
	
	public Handle addIntegerEntry(String name, int value, Integer minValue, 
			Integer maxValue, Integer precision, Boolean exclusive) {
		Handle handle = addIntegerEntry(name, value);
		
		config.setValueFilter(owner, handle, (exclusive!=null && exclusive==true) ? 
				ConfigRegistry.rangeFilter : ConfigRegistry.rangeFilter);
		
		if(minValue!=null) {
			config.setProperty(owner, handle, MIN_VALUE, minValue);
		}
		
		if(maxValue!=null) {
			config.setProperty(owner, handle, MAX_VALUE, maxValue);
		}
		
		if(precision!=null) {
			config.setProperty(owner, handle, PRECISION, precision);
		}
		
		if(exclusive!=null) {
			config.setProperty(owner, handle, EXCLUSIVE, exclusive);
		}
		
		return handle;
	}
	
	public Handle addDoubleEntry(String name) {
		return addEntry(name, EntryType.DOUBLE);
	}
	
	public Handle addDoubleEntry(String name, double value) {
		return addEntry(name, EntryType.DOUBLE, value);
	}
	
	public Handle addDoubleEntry(String name, double value, Double minValue, 
			Double maxValue) {
		return addDoubleEntry(name, value, minValue, maxValue, null, null);
	}
	
	public Handle addDoubleEntry(String name, double value, Double minValue, 
			Double maxValue, Double precision) {
		return addDoubleEntry(name, value, minValue, maxValue, precision, null);
	}
	
	public Handle addDoubleEntry(String name, double value, Double minValue, 
			Double maxValue, Double precision, Boolean exclusive) {
		Handle handle = addDoubleEntry(name, value);
		
		config.setValueFilter(owner, handle, (exclusive!=null && exclusive==true) ? 
				ConfigRegistry.rangeFilter : ConfigRegistry.rangeFilter);
		
		if(minValue!=null) {
			config.setProperty(owner, handle, MIN_VALUE, minValue);
		}
		
		if(maxValue!=null) {
			config.setProperty(owner, handle, MAX_VALUE, maxValue);
		}
		
		if(precision!=null) {
			config.setProperty(owner, handle, PRECISION, precision);
		}
		
		if(exclusive!=null) {
			config.setProperty(owner, handle, EXCLUSIVE, exclusive);
		}
		
		return handle;
	}
	
	public Handle addColorEntry(String name) {
		return addEntry(name, EntryType.COLOR);
	}
	
	public Handle addColorEntry(String name, int value) {
		return addEntry(name, EntryType.COLOR, value);
	}
	
	public Handle addOptionsEntry(String name, int selectedIndex, Object...items) {
		Handle handle = addEntry(name, EntryType.OPTIONS, items[selectedIndex]);
		// TODO make it SimpleXML compatible
		List<?> options = CollectionUtils.asList(items);
		
		config.setProperty(owner, handle, OPTIONS, options);
		//config.setProperty(owner, handle, RENDERER, ConfigUtils.localizingListCellRenderer);
		
		return handle;
	}
	
	public Handle addListEntry(String name, EntryType itemType, Object...items) {
		Handle handle = addEntry(name, EntryType.LIST, 
				CollectionUtils.asList(items));
		setProperties(handle, ConfigConstants.ITEM_TYPE, itemType);
		
		return handle;
	}
	
	public Handle addMapEntry(String name, EntryType itemType, Object...items) {
		Handle handle = addEntry(name, EntryType.MAP, CollectionUtils.asMap(items));
		setProperties(handle, ConfigConstants.ITEM_TYPE, itemType);
		
		return handle;
	}
	
	public Handle setProperties(Handle handle, Object...args) {
		if (args != null) {
			for (int i = 0; i < args.length; i += 2) {
				if (args[i + 1] != null) {
					config.setProperty(owner, handle, 
							String.valueOf(args[i]), args[i + 1]);
				}
			}
		}
		return handle;
	}
	
	public Handle setProperties(Object...args) {
		return setProperties(handles.peek(), args);
	}
	
	public void indent(Handle handle) {
		config.setProperty(owner, handle, INDENT, true);
	}
	
	public void indent() {
		indent(handles.peek());
	}
	
	public void note(Handle handle, String noteKey) {
		config.setProperty(owner, handle, NOTE_KEY, noteKey);
	}
	
	public void note(String noteKey) {
		note(handles.peek(), noteKey);
	}
	
	public void name(Handle handle, String nameKey) {
		config.setProperty(owner, handle, NAME_KEY, nameKey);
	}
	
	public void name(String nameKey) {
		name(handles.peek(), nameKey);
	}
	
	public void desc(Handle handle, String descriptionKey) {
		config.setProperty(owner, handle, DESCRIPTION_KEY, descriptionKey);
	}
	
	public void desc(String descriptionKey) {
		desc(handles.peek(), descriptionKey);
	}
	
	public void virtual(Handle handle) {
		config.addModifier(handle, GROUP_VIRTUAL);
	}
	
	public void virtual() {
		virtual(handles.peek());
	}
	
	public void separate(Handle handle) {
		setProperties(handle, SEPARATED, true);
	}
	
	public void separate() {
		setProperties(SEPARATED, true);
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public static class IllegalBuilderOperationException extends RuntimeException {

		private static final long serialVersionUID = 2602233758549443612L;
		
		public IllegalBuilderOperationException(String message) {
			super(message);
		}
	}
}
