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

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import net.ikarus_systems.icarus.Core;
import net.ikarus_systems.icarus.util.Exceptions;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConfigRegistry implements ConfigConstants {
	
	public final Handle ROOT_HANDLE = new Handle("root"); //$NON-NLS-1$
	public static final String PUBLIC_OWNER = ConfigRegistry.class.getName();
	
	private final ConfigGroup root;
	
	private String delimiter = "."; //$NON-NLS-1$
	private String escapedDelimiter = Pattern.quote(delimiter);
	
	/**
	 * Registered listeners to be notified on changes.
	 */
	private List<Object> listeners;
	
	private Map<Handle, List<ConfigListener>> groupListeners;
	
	/**
	 * Fast lookup for items by their handle values.
	 */
	private Map<Handle, ConfigItem> handles;
	
	/**
	 * Mapping of storages to all the ConfigItem instances
	 * that use them.
	 */
	private Map<ConfigStorage, Set<ConfigItem>> storages;
	
	/**
	 * Counter used for the generation of handle identifiers
	 * that are unique within a single registry instance.
	 */
	private AtomicInteger itemCounter = new AtomicInteger();
	
	private boolean eventsEnabled = true;
	
	private static ConfigRegistry globalRegistry;
	
	public static ConfigRegistry getGlobalRegistry() {
		if(globalRegistry==null) {
			synchronized (ConfigRegistry.class) {
				if(globalRegistry==null) {
					File configFile = new File(Core.getCore().getDataFolder(), 
							"config.xml"); //$NON-NLS-1$
					JAXBConfigStorage storage = new JAXBConfigStorage(
							configFile, IMMEDIATE_SAVING);
					globalRegistry = new ConfigRegistry(storage);
				}
			}
		}
		
		return globalRegistry;
	}

	public static ConfigRegistry newRegistry(ConfigStorage rootStorage) {
		return new ConfigRegistry(rootStorage);
	}
	
	private ConfigRegistry(ConfigStorage rootStorage) {
		Exceptions.testNullArgument(rootStorage, "rootStorage"); //$NON-NLS-1$
		
		rootStorage.setRegistry(this);
		
		root = new ConfigGroup(PUBLIC_OWNER, ROOT_HANDLE, "root", null); //$NON-NLS-1$
		root.path = root.name;
		handles = new Hashtable<>();
		handles.put(root.handle, root);
		
		storages = new Hashtable<>();
		mapStorage(rootStorage, root);
		root.storage = rootStorage;
	}
	
	/**
	 * @return the delemiter
	 */
	public String getDelimiter() {
		return delimiter;
	}

	/**
	 * @param delemiter the delimiter to set
	 */
	public void setDelimiter(String delimiter) {
		Exceptions.testNullArgument(delimiter, "delimiter"); //$NON-NLS-1$
		if(!this.delimiter.equals(delimiter)) {
			this.delimiter = delimiter;
			this.escapedDelimiter = Pattern.quote(delimiter);
		}
	}

	private void mapStorage(ConfigStorage storage, ConfigItem item) {
		Set<ConfigItem> set = storages.get(storage);
		if(set==null) {
			set = new HashSet<>();
			storages.put(storage, set);
		}
		set.add(item);
	}
	
	private void unmapStorage(ConfigStorage storage, ConfigItem item) {
		Set<ConfigItem> set = storages.get(storage);
		if(set!=null) {
			set.remove(item);
			if(set.isEmpty())
				storages.put(storage, null);
		}
	}
	
	private Handle createHandle() {
		return new Handle(String.format("item_%04d", itemCounter.incrementAndGet())); //$NON-NLS-1$
	}
	
	private boolean checkOwner(Object owner, ConfigItem item) {
		// TODO throw exception?
		return item.owner==owner;
	}
	
	private boolean equals(Object a, Object b) {
		if(a instanceof Collection || b instanceof Collection) {
			return false;
		}
		return a==null ? b==null : a.equals(b);
	}
	
	private boolean checkClass(ConfigEntry entry, Object value) {
		return value==null ? true : entry.type.getEntryClass().isAssignableFrom(value.getClass());
	}
	
	private boolean filterValue(ConfigEntry entry, Object value) {
		return entry.filter==null || entry.filter.isLegalValue(
				this, entry.handle, value);
	}
	
	private boolean assignValue(Object owner, ConfigEntry entry, Object value) {
		if(checkOwner(owner, entry) && !equals(entry.value, value)
				&& checkClass(entry, value) && filterValue(entry, value)) {
			entry.value = value;
			
			// we want only values different from the default to be stored
			value = entry.defaultValue==null || !entry.defaultValue.equals(value) ?
					value : null;
			
			getStorage(entry).setValue(entry.path, value);
			
			return true;
		}
		return false;
	}
	
	public void setValues(Map<Handle, Object> batch) {
		setValues(PUBLIC_OWNER, batch);
	}
	
	public void setValues(Object owner, Map<Handle, Object> batch) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(batch, "batch"); //$NON-NLS-1$
		
		List<ConfigEntry> changedEntries = new Vector<>();
		ConfigItem item;
		Object oldValue;
		for(Entry<Handle, Object> entry : batch.entrySet()) {
			item = getItem(entry.getKey()); 
			if(item instanceof ConfigEntry) {
				oldValue = ((ConfigEntry)item).value;			
				if(assignValue(owner, (ConfigEntry) item, entry.getValue())) {
					changedEntries.add((ConfigEntry) item);	
					fireEvent(new ConfigEvent(ConfigEvent.VALUE_CHANGE, 
							"handle", item.handle, "oldValue", oldValue)); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		
		if(changedEntries.isEmpty())
			return;
		
		Set<ConfigGroup> changedGroups = new HashSet<>(changedEntries.size());
		for(ConfigEntry entry : changedEntries)
			changedGroups.add(entry.parent);
		
		ConfigGroup[] groups = changedGroups.toArray(
				new ConfigGroup[changedGroups.size()]);
		
		Arrays.sort(groups, levelComparator);
		
		for(ConfigGroup group : groups) {
			//System.out.println("notifying: "+group.path);
			notifyGroupListeners(group);
		}
	}
	
	private static Comparator<ConfigItem> levelComparator = new Comparator<ConfigItem>() {

		@Override
		public int compare(ConfigItem o1, ConfigItem o2) {
			return o1.depth==o2.depth ? 0 : o1.depth>o2.depth ? -1 : 1;
		}
	};
	
	public void addGroupListener(String path, ConfigListener listener) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		addGroupListener(getHandle(path), listener);
	}
	
	public void addGroupListener(Handle handle, ConfigListener listener) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		if(!(getItem(handle) instanceof ConfigGroup))
			throw new IllegalArgumentException("Handle must point to a config group!"); //$NON-NLS-1$
		
		if(groupListeners==null) {
			groupListeners = new HashMap<>();
		}
		
		List<ConfigListener> list = groupListeners.get(handle);
		if(list==null) {
			list = new LinkedList<>();
			groupListeners.put(handle, list);
		}
		
		list.add(listener);
	}
	
	public void removeGroupListener(String path, ConfigListener listener) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		removeGroupListener(getHandle(path), listener);
	}
	
	public void removeGroupListener(Handle handle, ConfigListener listener) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		if(groupListeners==null)
			return;
		
		List<ConfigListener> list = groupListeners.get(handle);
		if(list!=null)
			list.remove(listener);
	}
	
	public void addListener(ConfigListener listener) {
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		addListener(null, listener);
	}
	
	public void addListener(String event, ConfigListener listener) {
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		if(listeners==null)
			listeners = new ArrayList<>();
		
		listeners.add(event);
		listeners.add(listener);
	}
	
	/**
	 * @return the eventsEnabled
	 */
	public boolean isEventsEnabled() {
		return eventsEnabled;
	}

	/**
	 * @param eventsEnabled the eventsEnabled to set
	 */
	public void setEventsEnabled(boolean eventsEnabled) {
		this.eventsEnabled = eventsEnabled;
	}

	public void removeListener(ConfigListener listener) {
		removeListener(null, listener);
	}
	
	public void removeListener(String event, ConfigListener listener) {
		if (listeners != null) {
			for (int i = listeners.size() - 2; i > -1; i -= 2) {
				if (listeners.get(i + 1) == listener
						&& (event == null || String.valueOf(
								listeners.get(i)).equals(event))) {
					listeners.remove(i + 1);
					listeners.remove(i);
				}
			}
		}
	}
	
	public boolean hasUnsavedChanges() {		
		for(ConfigStorage storage : storages.keySet())
			if(storage.hasUnsavedChanges())
				return true;
		
		return false;
	}
	
	public void save() {
		updateStorage(root, getStorage(root));
		for(ConfigStorage storage : storages.keySet()) {
			storage.commit();
		}
	}
	
	public void saveNow() {
		updateStorage(root, getStorage(root));
		for(ConfigStorage storage : storages.keySet()) {
			storage.commitNow();
		}
	}
	
	public void loadNow() {
		for(ConfigStorage storage : storages.keySet()) {
			storage.updateNow();
		}
		updateConfigTree(null);
	}
	
	private void updateStorage(ConfigItem item, ConfigStorage storage) {
		if(item.storage!=null && item.storage!=storage)
			storage = item.storage;
		
		//System.out.printf("updating storage: item=%s storage=%s\n", item, storage);
		
		if(item instanceof ConfigEntry) {
			// save the value to storage only if non-null and
			// different from the default value
			ConfigEntry entry = (ConfigEntry)item;
			if(entry.value!=null && !equals(entry.defaultValue, entry.value))
				storage.setValue(entry.path, entry.value);
		} else if(item instanceof ConfigGroup) {
			// proceed with child elements
			ConfigGroup group = (ConfigGroup) item;
			if(group.itemCount()>0) {
				for(ConfigItem child : group.items)
					updateStorage(child, storage);
			}
		}
	}
	
	public void updateConfigTree(Handle handle) {
		if(handle==null) {
			handle = ROOT_HANDLE;
		}
		
		ConfigItem item = getItem(handle);
		updateFromStorage(item, getStorage(item), false, true);
	}

	public void storageUpdated(ConfigStorage storage) {
		Exceptions.testNullArgument(storage, "storage"); //$NON-NLS-1$

		Set<ConfigItem> set = storages.get(storage);
		if(set!=null && !set.isEmpty()) {
			for(ConfigItem item : set)
				updateFromStorage(item, storage, false, false);
		}
	}
	
	private boolean updateFromStorage(ConfigItem item, ConfigStorage storage, 
			boolean groupCall, boolean completeTree) {
		if(item.storage!=null && item.storage!=storage) {
			if(completeTree)
				storage = item.storage;
			else
				return false;
		}
		
		if(item instanceof ConfigEntry) {
			ConfigEntry entry = (ConfigEntry)item;
			Object value = storage.getValue(entry.path);
			
			if(value!=null && !equals(entry.value, value) && 
					checkClass(entry, value) && filterValue(entry, value)) {
				Object oldValue = ((ConfigEntry)item).value;
				entry.value = value;
				
				fireEvent(new ConfigEvent(ConfigEvent.VALUE_CHANGE, 
						"handle", item.handle, "oldValue", oldValue)); //$NON-NLS-1$ //$NON-NLS-2$
				
				if(!groupCall)
					notifyGroupListeners(((ConfigEntry)item).parent);
				
				return true;
			}				
		} else if(item instanceof ConfigGroup) {
			ConfigGroup group = (ConfigGroup) item;
			boolean updated = false;
			if(group.itemCount()>0) {
				for(ConfigItem child : group.items)
					updated = updateFromStorage(child, storage, 
							true, completeTree) || updated;
				
				if(updated)
					notifyGroupListeners(group);
			}
		}
		
		return false;
	}

	private void fireEvent(ConfigEvent event) {
		if (eventsEnabled && listeners != null && !listeners.isEmpty()) {
			
			String listen;
			for (int i = 0; i < listeners.size(); i += 2) {
				listen = (String) listeners.get(i);

				if (listen == null || listen.equals(event.getName())) {
					((ConfigListener) listeners.get(i + 1)).invoke(this, event);
				}
			}
		}
	}
	
	private void notifyGroupListeners(ConfigGroup group) {
		if(groupListeners!=null) {
			List<ConfigListener> list = groupListeners.get(group.handle);
			if(list!=null && !list.isEmpty()) {
				ConfigEvent event = new ConfigEvent(ConfigEvent.VALUE_CHANGE, 
						"handle", group.handle); //$NON-NLS-1$
				for(ConfigListener listener : list)
					listener.invoke(this, event);
			}
		}
		
		if(group.parent!=null)
			notifyGroupListeners(group.parent);
	}
	
	private ConfigItem getItem(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		return handles==null ? null : handles.get(handle);
	}
	
	private ConfigItem getItemByPath(String path) {
		String[] tokens = path.split(escapedDelimiter);
		
		int index = 0;
		if(root.path.equals(tokens[0]))
			index++;
		
		//System.out.println(Arrays.toString(tokens));
		
		ConfigGroup group = root;
		ConfigItem item = null;
		while(group!=null && index<tokens.length) {
			item = group.getItem(tokens[index++]);
			if(item instanceof ConfigGroup)
				group = (ConfigGroup) item;
			else 
				break;
		}
		
		return index==tokens.length ? item : null;
	}
	
	private void refreshPath(ConfigItem item) {
		if(item.parent!=null) {
			item.path = item.parent.path+delimiter+item.name;
			
			if(item instanceof ConfigGroup && ((ConfigGroup)item).items!=null) {
				for(ConfigItem subItem : ((ConfigGroup)item).items) {
					refreshPath(subItem);
				}
			}
		}
	}
	
	private ConfigStorage getStorage(ConfigItem item) {
		ConfigStorage storage = null;
		while(item!=null && (storage = item.storage)==null) {
			item = item.parent;
		}
		
		return storage;
	}
	
	private void checkName(String name) {
		if(name.contains(".")) //$NON-NLS-1$
			throw new IllegalArgumentException("Name must not contain '.': "+name); //$NON-NLS-1$
	}
	
	/*
	 * PUBLIC CREATION METHODS
	 */
	
	/**
	 * Creates a new entry with the given parameters and adds it as a child of the
	 * specified group.<p>
	 * The <code>defaultValue</code> and <code>value</code> arguments are optional
	 * and can be null. Returns the handle to be used for modification access
	 * on the new entry.
	 * 
	 * @param owner
	 * @param parentHandle
	 * @param name
	 * @param type
	 * @param defaultValue
	 * @param value
	 * @return The handle for the new entry.
	 */
	public Handle newEntry(Object owner, Handle parentHandle, String name, EntryType type, 
			Object defaultValue, Object value) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(parentHandle, "parentHandle"); //$NON-NLS-1$
		Exceptions.testNullArgument(name, "name"); //$NON-NLS-1$
		Exceptions.testNullArgument(type, "type"); //$NON-NLS-1$
		
		ConfigGroup group = (ConfigGroup) getItem(parentHandle);
		
		return newEntry0(owner, group, name, type, defaultValue, value);
	}
	
	public Handle newEntry(Handle parentHandle, String name, EntryType type, 
			Object defaultValue, Object value) {
		return newEntry(PUBLIC_OWNER, parentHandle, name, type, defaultValue, value);
	}
	
	public Handle newEntry(Object owner, Handle parentHandle, String name, EntryType type) {
		return newEntry(owner, parentHandle, name, type, null, null);
	}
	
	public Handle newEntry(Handle parentHandle, String name, EntryType type) {
		return newEntry(PUBLIC_OWNER, parentHandle, name, type, null, null);
	}

	public Handle newEntry(Object owner, String parentPath, String name, EntryType type, 
			Object defaultValue, Object value) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(parentPath, "parentPath"); //$NON-NLS-1$
		Exceptions.testNullArgument(name, "name"); //$NON-NLS-1$
		Exceptions.testNullArgument(type, "type"); //$NON-NLS-1$
		
		ConfigGroup group = (ConfigGroup) getItemByPath(parentPath);
		
		return newEntry0(owner, group, name, type, defaultValue, value);
	}
	public Handle newEntry(String parentPath, String name, EntryType type, 
			Object defaultValue, Object value) {
		return newEntry(PUBLIC_OWNER, parentPath, name, type, defaultValue, value);
	}

	public Handle newEntry(Object owner, String parentPath, String name, EntryType type) {
		return newEntry(owner, parentPath, name, type, null, null);
	}

	public Handle newEntry(String parentPath, String name, EntryType type) {
		return newEntry(PUBLIC_OWNER, parentPath, name, type, null, null);
	}
	
	private Handle newEntry0(Object owner, ConfigGroup group, String name, EntryType type, 
			Object defaultValue, Object value) {
		checkName(name);
		
		Handle handle = createHandle();
		
		ConfigEntry entry = new ConfigEntry(owner, handle, name, type, group);
		entry.defaultValue = defaultValue;
		entry.value = value;
		
		group.addItem(entry);
		handles.put(handle, entry);
		
		refreshPath(entry);
		
		ConfigStorage storage = getStorage(entry);
		Object storedValue = storage==null ? null : storage.getValue(entry.path);
		if(storedValue!=null) {
			entry.value = storedValue;
		}
		
		fireEvent(new ConfigEvent(ConfigEvent.ITEM_ADDED, "handle", handle)); //$NON-NLS-1$
		
		// we want only values different from the default to be stored
		/*value = entry.defaultValue==null || !entry.defaultValue.equals(value) ?
				value : null;
		
		getStorage(entry).setValue(entry.path, value);*/
		
		return handle;
	}
	
	public Handle newGroup(Object owner, Handle parentHandle, String name) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(parentHandle, "parentHandle"); //$NON-NLS-1$
		Exceptions.testNullArgument(name, "name"); //$NON-NLS-1$
		
		ConfigGroup parent = (ConfigGroup) getItem(parentHandle);
		
		return newGroup0(owner, parent, name);
	}
	
	public Handle newGroup(Handle parentHandle, String name) {
		return newGroup(PUBLIC_OWNER, parentHandle, name);
	}
	
	public Handle newGroup(Object owner, String parentPath, String name) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(parentPath, "parentPath"); //$NON-NLS-1$
		Exceptions.testNullArgument(name, "name"); //$NON-NLS-1$
		
		ConfigGroup parent = (ConfigGroup) getItemByPath(parentPath);
		
		return newGroup0(owner, parent, name);
	}
	
	public Handle newGroup(String parentPath, String name) {
		return newGroup(PUBLIC_OWNER, parentPath, name);
	}
	
	private Handle newGroup0(Object owner, ConfigGroup parent, String name) {
		checkName(name);
		
		Handle handle = createHandle();
		
		ConfigGroup group = new ConfigGroup(owner, handle, name, parent);

		parent.addItem(group);
		handles.put(handle, group);
		
		refreshPath(group);
		
		fireEvent(new ConfigEvent(ConfigEvent.ITEM_ADDED, "handle", handle)); //$NON-NLS-1$
		
		return handle;
	}
	
	// TODO add methods to move and delete items
	
	/*
	 * PUBLIC NOTIFICATIONS
	 */
	
	public void valueChanged(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		ConfigItem item = getItem(handle);
		if(item!=null && item instanceof ConfigEntry) {
			
			fireEvent(new ConfigEvent(ConfigEvent.VALUE_CHANGE, 
					"handle", handle)); //$NON-NLS-1$
			
			notifyGroupListeners(((ConfigEntry)item).parent);
		}
	}
	
	public void valueChanged(String path) {
		valueChanged(getHandle(path));
	}
	
	/*
	 * OWNER ONLY WRITE METHODS
	 */
	
	// BEGIN TYPE
	
	public boolean setType(Object owner, Handle handle, EntryType type) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		Exceptions.testNullArgument(type, "type"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		if(item!=null && item instanceof ConfigEntry)
			return setType0(owner, (ConfigEntry)item, type);

		return false;
	}
	
	public boolean setType(Handle handle, EntryType type) {
		return setType(PUBLIC_OWNER, handle, type);
	}
	
	public boolean setType(Object owner, String path, EntryType type) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		Exceptions.testNullArgument(type, "type"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		if(item!=null && item instanceof ConfigEntry)
			return setType0(owner, (ConfigEntry)item, type);

		return false;
	}
	
	public boolean setType(String path, EntryType type) {
		return setType(PUBLIC_OWNER, path, type);
	}
	
	private boolean setType0(Object owner, ConfigEntry entry, EntryType type) {
		if(checkOwner(owner, entry) && entry.type!=type) {
			EntryType oldType = entry.type;
			entry.type = type;
			
			fireEvent(new ConfigEvent(ConfigEvent.ITEM_MODIFIED, 
					"handle", entry.handle, "field", "type", "oldValue", oldType)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return true;
		}
		return false;
	}
	
	// END TYPE
	
	
	// BEGIN VALUE
	
	public boolean setValue(Object owner, Handle handle, Object value) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		Exceptions.testNullArgument(value, "value"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		if(item!=null && item instanceof ConfigEntry)
			return setValue0(owner, (ConfigEntry)item, value);

		return false;
	}
	
	public boolean setValue(Handle handle, Object value) {
		return setValue(PUBLIC_OWNER, handle, value);
	}
	
	public boolean setValue(Object owner, String path, Object value) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		Exceptions.testNullArgument(value, "value"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		if(item!=null && item instanceof ConfigEntry)
			return setValue0(owner, (ConfigEntry)item, value);

		return false;
	}
	
	public boolean setValue(String path, Object value) {
		return setValue(PUBLIC_OWNER, path, value);
	}
	
	private boolean setValue0(Object owner, ConfigEntry entry, Object value) {
		Object oldValue = entry.value;
		
		if(assignValue(owner, entry, value)) {			
			fireEvent(new ConfigEvent(ConfigEvent.VALUE_CHANGE, 
					"handle", entry.handle, "oldValue", oldValue)); //$NON-NLS-1$ //$NON-NLS-2$
			
			notifyGroupListeners(entry.parent);
			return true;
		}
		
		return false;
	}
	
	// END VALUE
	
	
	// BEGIN VALUEFILTER
	
	public boolean setValueFilter(Object owner, Handle handle, ValueFilter filter) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		Exceptions.testNullArgument(filter, "filter"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		if(item!=null && item instanceof ConfigEntry)
			return setValueFilter0(owner, (ConfigEntry)item, filter);

		return false;
	}
	
	public boolean setValueFilter(Handle handle, ValueFilter filter) {
		return setValue(PUBLIC_OWNER, handle, filter);
	}
	
	public boolean setValueFilter(Object owner, String path, ValueFilter filter) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		Exceptions.testNullArgument(filter, "filter"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		if(item!=null && item instanceof ConfigEntry)
			return setValueFilter0(owner, (ConfigEntry)item, filter);

		return false;
	}
	
	public boolean setValueFilter(String path, ValueFilter filter) {
		return setValue(PUBLIC_OWNER, path, filter);
	}
	
	private boolean setValueFilter0(Object owner, ConfigEntry entry, ValueFilter filter) {
		if(checkOwner(owner, entry) && entry.filter!=filter) {
			Object oldValue = entry.filter;
			entry.filter = filter;
			
			fireEvent(new ConfigEvent(ConfigEvent.ITEM_MODIFIED, 
					"handle", entry.handle, "field", "filter", "oldValue", oldValue)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return true;
		}
		return false;
	}
	
	// END VALUEFILTER
	
	
	// BEGIN DEFAULTVALUE
	
	public boolean setDefaultValue(Object owner, Handle handle, Object defaultValue) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		Exceptions.testNullArgument(defaultValue, "defaultValue"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		if(item!=null && item instanceof ConfigEntry)
			return setDefaultValue0(owner, (ConfigEntry)item, defaultValue);

		return false;
	}
	
	public boolean setDefaultValue(Handle handle, Object defaultValue) {
		return setValue(PUBLIC_OWNER, handle, defaultValue);
	}
	
	public boolean setDefaultValue(Object owner, String path, Object defaultValue) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		Exceptions.testNullArgument(defaultValue, "defaultValue"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		if(item!=null && item instanceof ConfigEntry)	
			return setDefaultValue0(owner, (ConfigEntry)item, defaultValue);

		return false;
	}
	
	public boolean setDefaultValue(String path, Object defaultValue) {
		return setValue(PUBLIC_OWNER, path, defaultValue);
	}
	
	private boolean setDefaultValue0(Object owner, ConfigEntry entry, Object defaultValue) {
		if(checkOwner(owner, entry) && !equals(entry.defaultValue, defaultValue)
				&& checkClass(entry, defaultValue)) {
			Object oldValue = entry.defaultValue;
			entry.defaultValue = defaultValue;
			
			fireEvent(new ConfigEvent(ConfigEvent.VALUE_CHANGE, 
					"handle", entry.handle, "oldValue", oldValue)); //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}
		return false;
	}
	
	// END DEFAULTVALUE
	
	
	// BEGIN MODIFIER
	
	public boolean setModifier(Object owner, Handle handle, int modifier) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		return item==null ? false : setModifier0(owner, item, modifier);
	}
	
	public boolean setModifier(Handle handle, int modifier) {
		return setModifier(PUBLIC_OWNER, handle, modifier);
	}
	
	public boolean setModifier(Object owner, String path, int modifier) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		return item==null ? false : setModifier0(owner, item, modifier);
	}
	
	public boolean setModifier(String path, int modifier) {
		return setValue(PUBLIC_OWNER, path, modifier);
	}
	
	private boolean setModifier0(Object owner, ConfigItem item, int modifier) {
		if(checkOwner(owner, item) && item.modifier!=modifier) {
			int oldValue = item.modifier;
			item.modifier = modifier;
			
			fireEvent(new ConfigEvent(ConfigEvent.ITEM_MODIFIED, 
					"handle", item.handle, "field", "modifier", "oldValue", oldValue)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return true;
		}
		return false;
	}
	
	public boolean addModifier(Object owner, Handle handle, int modifier) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		return item==null ? false : addModifier0(owner, item, modifier);		
	}
	
	public boolean addModifier(Handle handle, int modifier) {
		return addModifier(PUBLIC_OWNER, handle, modifier);
	}
	
	public boolean addModifier(Object owner, String path, int modifier) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		return item==null ? false : addModifier0(owner, item, modifier);		
	}
	
	public boolean addModifier(String path, int modifier) {
		return addModifier(PUBLIC_OWNER, path, modifier);
	}
	
	private boolean addModifier0(Object owner, ConfigItem item, int modifier) {
		if(checkOwner(owner, item) && (item.modifier&modifier)!=modifier) {
			int oldValue = item.modifier;
			item.modifier |= modifier;
			
			fireEvent(new ConfigEvent(ConfigEvent.ITEM_MODIFIED, 
					"handle", item.handle, "field", "modifier", "oldValue", oldValue)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return true;
		}
		return false;
	} 
	
	// END MODIFIER
	
	
	// BEGIN PROPERTIES
	
	public boolean setProperty(Object owner, Handle handle, String key, Object value) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		Exceptions.testNullArgument(key, "key"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		return item==null ? false : setProperty0(owner, item, key, value);		
	}
	
	public boolean setProperty(Handle handle, String key, Object value) {
		return setProperty(PUBLIC_OWNER, handle, key, value);
	}
	
	public boolean setProperty(Object owner, String path, String key, Object value) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		Exceptions.testNullArgument(key, "key"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		return item==null ? false : setProperty0(owner, item, key, value);		
	}
	
	public boolean setProperty(String path, String key, Object value) {
		return setProperty(PUBLIC_OWNER, path, key, value);
	}
	
	private boolean setProperty0(Object owner, ConfigItem item, 
			String key, Object value) {
		if(checkOwner(owner, item) && item.setProperty(key, value)) {
			fireEvent(new ConfigEvent(ConfigEvent.ITEM_MODIFIED, 
					"handle", item.handle, "field", "properties", "key", key)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			
			return true;
		}
		return false;
	}
	
	// END PROPERTIES
	
	
	// BEGIN STORAGE
	
	public boolean setStorage(Object owner, Handle handle, ConfigStorage storage) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		return item==null ? false : setStorage0(owner, item, storage);
	}
	
	public boolean setStorage(Handle handle, ConfigStorage storage) {
		return setStorage(PUBLIC_OWNER, handle, storage);
	}
	
	public boolean setStorage(Object owner, String path, ConfigStorage storage) {
		Exceptions.testNullArgument(owner, "owner"); //$NON-NLS-1$
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		return item==null ? false : setStorage0(owner, item, storage);
	}
	
	public boolean setStorage(String path, ConfigStorage storage) {
		return setStorage(PUBLIC_OWNER, path, storage);
	}

	private boolean setStorage0(Object owner, ConfigItem item, ConfigStorage storage) {
		if(item==root)
			throw new ConfigException("Not allowed to change the root storage", root.path); //$NON-NLS-1$
		
		if(checkOwner(owner, item) && item.storage!=storage) {
			item.storage = storage;
			if(storage==null)
				unmapStorage(storage, item);
			else
				mapStorage(storage, item);
			
			return true;
		}
		return false;
	}
	
	// END STORAGE 
	
	/*
	 * PUBLIC READ ONLY ACCESS METHODS
	 */
	
	public boolean pathExists(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		return getItemByPath(path)!=null;
	}
	
	public boolean isEntry(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$

		return getItemByPath(path) instanceof ConfigEntry;
	}
	
	public boolean isEntry(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$

		return getItem(handle) instanceof ConfigEntry;
	}
	
	public boolean isGroup(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		return getItemByPath(path) instanceof ConfigGroup;
	}
	
	public boolean isGroup(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		return getItem(handle) instanceof ConfigGroup;
	}
	
	public String getName(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		return item==null ? null : item.name;
	}
	
	public String getName(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		return item==null ? null : item.name;
	}
	
	public Handle getHandle(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		return item==null ? null : item.handle;
	}
	
	public String getPath(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		return item==null ? null : item.path;
	}
	
	public EntryType getType(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		if(item!=null && item instanceof ConfigEntry)
			return ((ConfigEntry)item).type;
		
		return null;
	}
	
	public EntryType getType(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		if(item!=null && item instanceof ConfigEntry)
			return ((ConfigEntry)item).type;
		
		return null;
	}
	
	public int getModifier(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		return item==null ? 0 : item.modifier;
	}
	
	public int getModifier(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		return item==null ? 0 : item.modifier;
	}
	
	public Class<?> getEntryClass(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		EntryType type = getType(path);
		return type==null ? null : type.getEntryClass();
	}
	
	public Class<?> getEntryClass(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		EntryType type = getType(handle);
		return type==null ? null : type.getEntryClass();
	}
	
	public Handle getParentHandle(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		return (item==null || item.parent==null) ? null : item.parent.handle; 
	}
	
	public Handle getParentHandle(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		return (item==null || item.parent==null) ? null : item.parent.handle; 
	}
	
	public String getParentPath(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		return (item==null || item.parent==null) ? null : item.parent.path; 
	}
	
	public String getParentPath(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		return (item==null || item.parent==null) ? null : item.parent.path; 
	}
	
	public Object getValue(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		Object value = null;
		if(item!=null && item instanceof ConfigEntry)
			value = ((ConfigEntry)item).value;

		return value;
	}
	
	@SuppressWarnings("unchecked")
	public <O extends Object> O getValue(Handle handle, Class<O> clazz) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		Object value = null;
		if(item!=null && item instanceof ConfigEntry)
			value = ((ConfigEntry)item).value;

		return (O) (value);
	}
	
	@SuppressWarnings("unchecked")
	public <O extends Object> O getValue(String path, Class<O> clazz) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		Object value = null;
		if(item!=null && item instanceof ConfigEntry)
			value = ((ConfigEntry)item).value;

		return (O) (value);
	}
	
	@SuppressWarnings("unchecked")
	public <O extends Object> O getValue(Handle handle, O defaultValue) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		Object value = null;
		if(item!=null && item instanceof ConfigEntry) {
			value = ((ConfigEntry)item).value;
		}
		
		if(value==null) {
			value = defaultValue;
		}

		return (O) (value);
	}
	
	@SuppressWarnings("unchecked")
	public <O extends Object> O getValue(String path, O defaultValue) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		Object value = null;
		if(item!=null && item instanceof ConfigEntry) {
			value = ((ConfigEntry)item).value;
		}
		
		if(value==null) {
			value = defaultValue;
		}

		return (O) (value);
	}
	
	public Object getValue(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		Object value = null;
		if(item!=null && item instanceof ConfigEntry)
			value = ((ConfigEntry)item).value;

		return value;
	}
	
	// GETTER for typical types
	
	public int getInteger(Handle handle) {
		return ((Number)getValue(handle)).intValue();
	}
	
	public int getInteger(String path) {
		return ((Number)getValue(path)).intValue();
	}
	
	public Color getColor(Handle handle) {
		return new Color(getInteger(handle));
	}
	
	public Color getColor(String path) {
		return new Color(getInteger(path));
	}
	
	public String getString(Handle handle) {
		return (String)getValue(handle);
	}
	
	public String getString(String path) {
		return (String)getValue(path);
	}
	
	public boolean getBoolean(Handle handle) {
		return (Boolean)getValue(handle);
	}
	
	public boolean getBoolean(String path) {
		return (Boolean)getValue(path);
	}
	
	public double getDouble(Handle handle) {
		return ((Number)getValue(handle)).doubleValue();
	}
	
	public double getDouble(String path) {
		return ((Number)getValue(path)).doubleValue();
	}
	
	public float getFloat(Handle handle) {
		return ((Number)getValue(handle)).floatValue();
	}
	
	public float getFloat(String path) {
		return ((Number)getValue(path)).floatValue();
	}
	
	public short getShort(Handle handle) {
		return ((Number)getValue(handle)).shortValue();
	}
	
	public short getShort(String path) {
		return ((Number)getValue(path)).shortValue();
	}
	
	public long getLong(Handle handle) {
		return ((Number)getValue(handle)).longValue();
	}
	
	public long getLong(String path) {
		return ((Number)getValue(path)).longValue();
	}
	
	public byte getByte(Handle handle) {
		return ((Number)getValue(handle)).byteValue();
	}
	
	public byte getByte(String path) {
		return ((Number)getValue(path)).byteValue();
	}
	
	public char getCharacter(Handle handle) {
		return (Character)getValue(handle);
	}
	
	public char getCharacter(String path) {
		return (Character)getValue(path);
	}
	
	public List<?> getList(Handle handle) {
		return (List<?>)getValue(handle);
	}
	
	public List<?> getList(String path) {
		return (List<?>)getValue(path);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, ?> getMap(Handle handle) {
		return (Map<String, ?>)getValue(handle);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, ?> getMap(String path) {
		return (Map<String, ?>)getValue(path);
	}
	
	@SuppressWarnings("unchecked")
	public Object getMapEntry(String path, String key) {
		Map<String, ?> map = (Map<String, ?>)getValue(path);
		return map==null ? null : map.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public Object getMapEntry(Handle handle, String key) {
		Map<String, ?> map = (Map<String, ?>)getValue(handle);
		return map==null ? null : map.get(key);
	}
	
	public ValueFilter getValueFilter(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		if(item!=null && item instanceof ConfigEntry)
			return ((ConfigEntry)item).filter;
		
		return null;
	}
	
	public ValueFilter getValueFilter(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		if(item!=null && item instanceof ConfigEntry)
			return ((ConfigEntry)item).filter;
		
		return null;
	}
	
	public Object getDefaultValue(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		if(item!=null && item instanceof ConfigEntry)
			return ((ConfigEntry)item).defaultValue;
		
		return null;
	}
	
	public Object getDefaultValue(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		if(item!=null && item instanceof ConfigEntry)
			return ((ConfigEntry)item).defaultValue;
		
		return null;
	}
	
	public int getItemCount(String path) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		if(item!=null && item instanceof ConfigGroup)
			return ((ConfigGroup)item).itemCount();
		
		return 0;
	}
	
	public int getItemCount(Handle handle) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		if(item!=null && item instanceof ConfigGroup)
			return ((ConfigGroup)item).itemCount();
		
		return 0;
	}
	
	public Object getProperty(String path, String key) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		Exceptions.testNullArgument(key, "key"); //$NON-NLS-1$

		ConfigItem item = getItemByPath(path);
		
		return item==null ? null : item.getProperty(key);
	}
	
	public Object getProperty(Handle handle, String key) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		Exceptions.testNullArgument(key, "key"); //$NON-NLS-1$

		ConfigItem item = getItem(handle);
		
		return item==null ? null : item.getProperty(key);
	}
	
	public Handle getChildHandle(String path, int index) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		if(item!=null && item instanceof ConfigGroup) {
			item = ((ConfigGroup)item).getItem(index);
			return item==null ? null : item.handle;
		}
		
		return null;
	}
	
	public Handle getChildHandle(Handle handle, int index) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		if(item!=null && item instanceof ConfigGroup) {
			item = ((ConfigGroup)item).getItem(index);
			return item==null ? null : item.handle;
		}
		
		return null;
	}
	
	public Handle getChildHandle(String path, String name) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		Exceptions.testNullArgument(name, "name"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		if(item!=null && item instanceof ConfigGroup) {
			item = ((ConfigGroup)item).getItem(name);
			return item==null ? null : item.handle;
		}
		
		return null;
	}
	
	public Handle getChildHandle(Handle handle, String name) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		Exceptions.testNullArgument(name, "name"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		if(item!=null && item instanceof ConfigGroup) {
			item = ((ConfigGroup)item).getItem(name);
			return item==null ? null : item.handle;
		}
		
		return null;
	}
	
	public String getChildPath(String path, int index) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		if(item!=null && item instanceof ConfigGroup) {
			item = ((ConfigGroup)item).getItem(index);
			return item==null ? null : item.path;
		}
		
		return null;
	}
	
	public String getChildPath(Handle handle, int index) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		if(item!=null && item instanceof ConfigGroup) {
			item = ((ConfigGroup)item).getItem(index);
			return item==null ? null : item.path;
		}
		
		return null;
	}
	
	public String getChildPath(String path, String name) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		Exceptions.testNullArgument(name, "name"); //$NON-NLS-1$
		
		ConfigItem item = getItemByPath(path);
		if(item!=null && item instanceof ConfigGroup) {
			item = ((ConfigGroup)item).getItem(name);
			return item==null ? null : item.path;
		}
		
		return null;
	}
	
	public String getChildPath(Handle handle, String name) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		Exceptions.testNullArgument(name, "name"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		if(item!=null && item instanceof ConfigGroup) {
			item = ((ConfigGroup)item).getItem(name);
			return item==null ? null : item.path;
		}
		
		return null;
	}
	
	public Handle getChildHandle(Handle handle, String...names) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		Exceptions.testNullArgument(names, "names"); //$NON-NLS-1$
		
		ConfigItem item = getChildItem0(getItem(handle), names);
		return item==null ? null : item.handle;
	}
	
	public Handle getChildHandle(String path, String...names) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		Exceptions.testNullArgument(names, "names"); //$NON-NLS-1$
		
		ConfigItem item = getChildItem0(getItemByPath(path), names);
		return item==null ? null : item.handle;
	}
	
	public String getChildPath(Handle handle, String...names) {
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		Exceptions.testNullArgument(names, "names"); //$NON-NLS-1$
		
		ConfigItem item = getChildItem0(getItem(handle), names);
		return item==null ? null : item.path;
	}
	
	public String getChildPath(String path, String...names) {
		Exceptions.testNullArgument(path, "path"); //$NON-NLS-1$
		Exceptions.testNullArgument(names, "names"); //$NON-NLS-1$
		
		ConfigItem item = getChildItem0(getItemByPath(path), names);
		return item==null ? null : item.path;
	}
	
	private ConfigItem getChildItem0(ConfigItem item, String...names) {
		int index = 0;
		while(index<names.length && item!=null && item instanceof ConfigGroup) {
			item = ((ConfigGroup)item).getItem(names[index]);
			index++;
		}
		
		return index==names.length ? item : null;
		
	}
	
	public boolean isHidden(String path) {
		return (getModifier(path)&ENTRY_HIDDEN)==ENTRY_HIDDEN;
	}
	
	public boolean isHidden(Handle handle) {
		return (getModifier(handle)&ENTRY_HIDDEN)==ENTRY_HIDDEN;
	}
	
	public boolean isModifiable(String path) {
		return (getModifier(path)&ENTRY_MODIFIABLE)==ENTRY_MODIFIABLE;
	}
	
	public boolean isModifiable(Handle handle) {
		return (getModifier(handle)&ENTRY_MODIFIABLE)==ENTRY_MODIFIABLE;
	}
	
	public boolean isLocked(String path) {
		return (getModifier(path)&ENTRY_LOCKED)==ENTRY_LOCKED;
	}
	
	public boolean isLocked(Handle handle) {
		return (getModifier(handle)&ENTRY_LOCKED)==ENTRY_LOCKED;
	}
	
	public boolean isVirtual(String path) {
		return (getModifier(path)&GROUP_VIRTUAL)==GROUP_VIRTUAL;
	}
	
	public boolean isVirtual(Handle handle) {
		return (getModifier(handle)&GROUP_VIRTUAL)==GROUP_VIRTUAL;
	}
	
	public boolean isParentOf(Handle parentHandle, Handle handle) {
		Exceptions.testNullArgument(parentHandle, "parentHandle"); //$NON-NLS-1$
		Exceptions.testNullArgument(handle, "handle"); //$NON-NLS-1$
		
		ConfigItem item = getItem(handle);
		ConfigItem parentItem = getItem(parentHandle);
		
		return item.path.startsWith(parentItem.path);
	}
	
	public class Handle {
		private final String id;
		
		private Handle(String id) {
			this.id = id;
		}
		
		@Override
		public String toString() {
			return id;
		}
		
		@Override
		public boolean equals(Object o) {
			return this==o;
		}
		
		public ConfigRegistry getSource() {
			return ConfigRegistry.this;
		}
	}
	
	public interface ValueFilter {
		boolean isLegalValue(ConfigRegistry registry, Handle handle, Object value);
	}
	
	/**
	 * A simple <code>ValueFilter</code> that uses the following 
	 * property fields of the <code>ConfigEntry</code> specified by
	 * the handle argument:<p>
	 * <ul>
	 * <li><code>minValue</code> as the minimal allowed value , inclusive.
	 * <li><code>maxValue</code> as the maximal allowed value , inclusive.
	 * </ul>
	 */
	public static ValueFilter rangeFilter = new ValueFilter() {
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean isLegalValue(ConfigRegistry registry, Handle handle,
				Object value) {
			
			Comparable<Object> minValue = (Comparable<Object>) registry.getProperty(handle, "minValue"); //$NON-NLS-1$
			Comparable<Object> maxValue = (Comparable<Object>) registry.getProperty(handle, "maxValue"); //$NON-NLS-1$
			
			if(minValue!=null && maxValue!=null && value instanceof Comparable<?>)
				return minValue.compareTo(value)<=0
						&& maxValue.compareTo(value)>=0;
			return false;
		}
	};
	
	/**
	 * An <code>ValueFilter</code> that works the same as <code>rangeFilter</code>
	 * except that its bounding values are exclusive.
	 */
	public static ValueFilter exclusiveRangeFilter = new ValueFilter() {
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean isLegalValue(ConfigRegistry registry, Handle handle,
				Object value) {
			
			Comparable<Object> minValue = (Comparable<Object>) registry.getProperty(handle, "minValue"); //$NON-NLS-1$
			Comparable<Object> maxValue = (Comparable<Object>) registry.getProperty(handle, "maxValue"); //$NON-NLS-1$
			
			if(minValue!=null && maxValue!=null && value instanceof Comparable<?>)
				return minValue.compareTo(value)<0
						&& maxValue.compareTo(value)>0;
						
			return false;
		}
	};
	
	/**
	 * A filter for strings that matches the new value against 
	 * the pattern string in the <code>pattern</code> property
	 * field of the <code>ConfigEntry</code> specified by
	 * the handle argument.
	 */
	public static ValueFilter patternFilter = new ValueFilter() {
		@Override
		public boolean isLegalValue(ConfigRegistry registry, Handle handle,
				Object value) {
			
			Object pattern = registry.getProperty(handle, "pattern"); //$NON-NLS-1$
			if(pattern!=null && value instanceof String) {
				if(pattern instanceof String)
					return ((String)value).matches((String)pattern);
				else if(pattern instanceof Pattern)
					return ((Pattern)pattern).matcher((String)value).matches();
			}
			
			return false;
		}
		
	};
	
	/**
	 * Legal type constants for config entries.
	 * Every item holds the base class of allowed
	 * values for the config entry it is assigned to.
	 * This information is used when attempts to alter
	 * the value of a certain entry are made. Such attempts
	 * immediatly fail in case the class of the given object
	 * to be used as new value is not compatible with
	 * the one described in the corresponding type field of
	 * the entry. Config-guis should use this information
	 * to decide the type of components to be displayed
	 * in order to enable user side modification of the config
	 * entry.
	 * 
	 * @author Markus Gärtner
	 *
	 */
	public enum EntryType {
		INTEGER(Integer.class),
		FLOAT(Float.class),
		DOUBLE(Double.class),
		LONG(Long.class),
		BOOLEAN(Boolean.class),
		STRING(String.class),
		CUSTOM(Object.class),
		FILE(String.class),
		OPTIONS(Object.class),
		COLOR(Integer.class),
		MAP(Map.class),
		LIST(List.class);
		
		private Class<?> entryClass;
		
		private EntryType(Class<?> entryClass) {
			this.entryClass = entryClass;
		}
		
		public Class<?> getEntryClass() {
			return entryClass;
		}
	}
	
	private static class ConfigItem {
		protected ConfigGroup parent;
		protected String name;
		protected Handle handle;
		protected Object owner;
		protected int modifier;		
		protected String path;
		protected Map<String, Object> properties;
		protected ConfigStorage storage;
		protected int depth;
		
		ConfigItem(Object owner, String name, Handle handle, ConfigGroup parent) {
			this.owner = owner;
			this.name = name;
			this.handle = handle;
			this.parent = parent;
			this.depth = parent==null ? 0 : parent.depth+1;
		}
		
		boolean setProperty(String key, Object value) {
			boolean created = false;
			if(properties==null) {
				properties = new HashMap<>();
				created = true;
			}
			
			return properties.put(key, value)!=null || created;
		}
		
		Object getProperty(String key) {
			return properties==null ? null : properties.get(key);
		}
	}
	
	private static class ConfigEntry extends ConfigItem {
		private EntryType type;
		private Object defaultValue;
		private Object value;
		private ValueFilter filter;
		
		private ConfigEntry(Object owner, Handle handle, String name, EntryType type, ConfigGroup group) {
			super(owner, name, handle, group);
			this.type = type;
		}
		
		@Override
		public String toString() {
			return String.format("entry '%s' (path=%s, type=%s)", name, path, type); //$NON-NLS-1$
		}
	}
	
	private static class ConfigGroup extends ConfigItem {
		private List<ConfigItem> items;
		
		ConfigGroup(Object owner, Handle handle, String name, ConfigGroup parent) {
			super(owner, name, handle, parent);
		}
		
		void addItem(ConfigItem item) {
			if(items==null)
				items = new Vector<>();
			
			for(ConfigItem i : items)
				if(i.name.equals(item.name))
					throw new ConfigException("Duplicate item name: "+i.name, path); //$NON-NLS-1$
			
			items.add(item);
		}
		
		ConfigItem getItem(int index) {
			return items==null ? null : items.get(index);
		}
		
		ConfigItem getItem(String name) {
			if(items!=null) {
				for(ConfigItem item : items)
					if(item.name.equals(name))
						return item;
			}
			return null;
		}
		
		int itemCount() {
			return items==null ? 0 : items.size();
		}
		
		@Override
		public String toString() {
			return String.format("group '%s' (path=%s, itemCount=%s)", name, path, itemCount()); //$NON-NLS-1$
		}
	}
}
