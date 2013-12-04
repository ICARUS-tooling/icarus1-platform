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
package de.ims.icarus.resources;

import java.io.ObjectStreamException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.Exceptions;


/**
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class ResourceManager {

	private Locale locale = Locale.getDefault();
	
	private WeakHashMap<Object, Localizer> items = new WeakHashMap<Object, Localizer>();

	private Set<WeakReference<ManagedResource>> managedResources = new HashSet<>();
		
	private List<ChangeListener> listeners;
	private final ChangeEvent changeEvent = new ChangeEvent(this);
	
	private static ResourceManager instance;
	
	private static boolean notifyMissingResource = true;

	public static boolean isNotifyMissingResource() {
		return notifyMissingResource;
	}

	public static void setNotifyMissingResource(boolean notifyMissingResource) {
		ResourceManager.notifyMissingResource = notifyMissingResource;
	}

	public static ResourceManager getInstance() {
		if(instance==null) {
			synchronized (ResourceManager.class) {
				if(instance==null) {
					instance= new ResourceManager();
					instance.globalDomain.addResource("de.ims.icarus.ui.resources.ui"); //$NON-NLS-1$
				}
			}
		}
		
		return instance;
	}
	
	public static final ResourceLoader DEFAULT_RESOURCE_LOADER
			= new DefaultResourceLoader(ResourceManager.class.getClassLoader());
	
	private final ResourceDomain globalDomain = new ResourceDomain();

	private ResourceManager() {
		LoggerFactory.registerLogFile(
				"de.ims.icarus.resources", "icarus.resources"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// prevent multiple deserialization
	private Object readResolve() throws ObjectStreamException {
		return getInstance();
	}
	
	// prevent cloning
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	public ResourceDomain getGlobalDomain() {
		return globalDomain;
	}
	
	public void addChangeListener(ChangeListener listener) {
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		if(listeners==null)
			listeners = new ArrayList<>();
			
		listeners.add(listener);
	}
	
	public void removeChangeListener(ChangeListener listener) {
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		if(listeners==null)
			return;
		
		listeners.remove(listener);
	}

	public void addResource(String baseName, ResourceLoader loader) {
		globalDomain.addResource(baseName, loader);
	}

	public void addResource(String baseName) {
		globalDomain.addResource(baseName);
	}
	
	public void removeResource(String baseName) {
		globalDomain.removeResource(baseName);
	}
	
	public ManagedResource addManagedResource(String baseName, ResourceLoader loader) {
		Exceptions.testNullArgument(baseName, "baseName"); //$NON-NLS-1$
		
		if(loader==null) {
			loader = DEFAULT_RESOURCE_LOADER;
		}
		
		ManagedResource managedResource = null;
		
		synchronized (managedResources) {
			managedResource = getManagedResource(baseName);
			
			if(managedResource==null) {
				managedResource = new ManagedResource(baseName, loader);
				managedResources.add(new WeakReference<ManagedResource>(managedResource));
			}
		}
		
		return managedResource;
	}
	
	public ManagedResource addManagedResource(String baseName) {
		return addManagedResource(baseName, null);
	}
	
	public ManagedResource getManagedResource(String baseName) {
		Exceptions.testNullArgument(baseName, "baseName"); //$NON-NLS-1$

		synchronized (managedResources) {
			for(Iterator<WeakReference<ManagedResource>> i = managedResources.iterator(); i.hasNext();) {
				WeakReference<ManagedResource> ref = i.next();
				
				ManagedResource res = ref.get();
				if(res==null) {
					i.remove();
				} else if(res.getBaseName().equals(baseName)) {
					return res;
				}
			}
		}
		
		return null;
	}
	
	public ManagedResource removeManagedResource(String baseName) {

		synchronized (managedResources) {
			for(Iterator<WeakReference<ManagedResource>> i = managedResources.iterator(); i.hasNext();) {
				WeakReference<ManagedResource> ref = i.next();
				
				ManagedResource res = ref.get();
				if(res==null) {
					i.remove();
				} else if(res.getBaseName().equals(baseName)) {
					i.remove();
					return res;
				}
			}
		}
		
		return null;
	}
	
	public static ResourceLoader createResourceLoader(ClassLoader classLoader) {
		if(classLoader==null || ResourceManager.class.getClassLoader()==classLoader)
			return DEFAULT_RESOURCE_LOADER;
		else
			return new DefaultResourceLoader(classLoader);
	}

	public String getFormatted(String key, Object...args) {
		return globalDomain.getFormatted(key, args);
	}

	public String get(String key) {
		return globalDomain.get(key);
	}

	public String get(String key, Object...params) {
		return globalDomain.get(key, null, params);
	}
	
	public static String format(String text, Object...params) {
		StringBuilder result = new StringBuilder();
		String index = null;

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			if (c == '{') {
				index = ""; //$NON-NLS-1$
			} else if (index != null && c == '}') {
				int tmp = Integer.parseInt(index) - 1;

				if (tmp >= 0 && tmp < params.length) {
					result.append(params[tmp]);
				}

				index = null;
			} else if (index != null) {
				index += c;
			} else {
				result.append(c);
			}
		}

		return result.toString();
	}

	public void setLocale(Locale value) {
		Exceptions.testNullArgument(value, "locale"); //$NON-NLS-1$
		if (!locale.equals(value)) {
			locale = value;
			
			if(listeners!=null)
				for(ChangeListener listener : listeners)
					listener.stateChanged(changeEvent);
			
			refresh();
		}
	}

	public Locale getLocale() {
		return locale;
	}

	public void addLocalizableItem(Object item, Localizer localizer, boolean init) {
		Exceptions.testNullArgument(item, "item"); //$NON-NLS-1$
		Exceptions.testNullArgument(localizer, "localizer"); //$NON-NLS-1$

		items.put(item, localizer);

		if (init) {
			localizer.localize(item);
		}
	}

	public void removeLocalizableItem(Object item) {
		Exceptions.testNullArgument(item, "item"); //$NON-NLS-1$
		items.remove(item);
	}

	public Localizer getLocalizer(Object item) {
		Exceptions.testNullArgument(item, "item"); //$NON-NLS-1$
		return items.get(item);
	}

	public boolean contains(Object item) {
		Exceptions.testNullArgument(item, "item"); //$NON-NLS-1$
		return items.containsKey(item);
	}
	
	public void localize(Object item) {
		Exceptions.testNullArgument(item, "item"); //$NON-NLS-1$
		
		Localizer localizer = items.get(item);
		
		if (item instanceof Localizable) {
			((Localizable) item).localize();
		}
		
		if(localizer!=null)
			localizer.localize(item);
	}

	public void refresh() {
		Entry<Object, Localizer> entry;
		Object item;
		Localizer localizer;
		
		// clear internal state of managed resources
		for(Iterator<WeakReference<ManagedResource>> i = managedResources.iterator(); i.hasNext();) {
			WeakReference<ManagedResource> ref = i.next();
			
			if(ref.get()==null)
				i.remove();
			else
				ref.get().clear();
		}

		// notify all localizers
		for (Iterator<Entry<Object, Localizer>> i = items.entrySet().iterator(); i
				.hasNext();) {
			entry = i.next();
			item = entry.getKey();

			if (item == null)
				continue;

			localizer = entry.getValue();

			/*
			 * allow item to localize itself. this call serves merely as a hint
			 * to tell the object that localization data has changed so the
			 * object might localize components that are not covered via the
			 * regular localization workflow (e.g. invoke a repaint of a
			 * JTable).
			 */
			if (item instanceof Localizable) {
				((Localizable) item).localize();
			}

			if(localizer!=null)
				localizer.localize(item);
		}
	}
}
