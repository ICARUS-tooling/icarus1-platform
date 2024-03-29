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
package de.ims.icarus.ui.helper;

import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.java.plugin.registry.Extension;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.classes.ClassProxy;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeCollection;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class UIHelperRegistry {

	private static volatile UIHelperRegistry instance;

	public static UIHelperRegistry globalRegistry() {
		if(instance==null) {
			synchronized (UIHelperRegistry.class) {
				if(instance==null)
					instance= new UIHelperRegistry();
			}
		}

		return instance;
	}

	private Map<String, Map<String, Object>> helpers = new HashMap<>();
	private Map<String, Object> fallbackHelpers = new HashMap<>();

	public UIHelperRegistry newRegistry() {
		return new UIHelperRegistry();
	}

	/**
	 *
	 */
	private UIHelperRegistry() {
		init();
	}

	private void init() {
		// FALLBACKS

		// Register renderers
		setFallbackHelper("javax.swing.ListCellRenderer", "javax.swing.DefaultListCellRenderer"); //$NON-NLS-1$ //$NON-NLS-2$
		setFallbackHelper("java.lang.String", "javax.swing.tree.DefaultTreeCellRenderer"); //$NON-NLS-1$ //$NON-NLS-2$
		setFallbackHelper("java.lang.String", "javax.swing.table.DefaultTableCellRenderer"); //$NON-NLS-1$ //$NON-NLS-2$

		// Register editors
		setFallbackHelper("javax.swing.CellEditor", "javax.swing.DefaultCellEditor"); //$NON-NLS-1$ //$NON-NLS-2$
		setFallbackHelper("javax.swing.table.TableCellEditor", "javax.swing.DefaultCellEditor"); //$NON-NLS-1$ //$NON-NLS-2$
		setFallbackHelper("javax.swing.tree.TreeCellEditor", "javax.swing.DefaultCellEditor"); //$NON-NLS-1$ //$NON-NLS-2$

		// ACTUAL HELPERS
		registerHelper("de.ims.icarus.ui.view.AWTPresenter",  //$NON-NLS-1$
				"StringContentType",  //$NON-NLS-1$
				"de.ims.icarus.ui.view.TextPresenter"); //$NON-NLS-1$

		// TODO do we have some helpers that are not part of a plug-in?
	}

	// Prevent multiple de-serialization
	private Object readResolve() throws ObjectStreamException {
		throw new NotSerializableException(UIHelperRegistry.class.getName());
	}

	// prevent cloning
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public void setFallbackHelper(String helperClass, Object helper) {
		Exceptions.testNullArgument(helperClass, "helperClass"); //$NON-NLS-1$
		Exceptions.testNullArgument(helper, "helper"); //$NON-NLS-1$

		fallbackHelpers.put(helperClass, helper);
	}

	private <T extends Object> String getLookupKey(Class<T> clazz) {
		return clazz.getName().replace('$', '.');
	}

	/**
	 *
	 * @param helperClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> T getFallbackHelper(Class<T> helperClass) {
		Exceptions.testNullArgument(helperClass, "helperClass"); //$NON-NLS-1$

		Object helper = getFallbackHelper0(helperClass);

		if(helper!=null)
			helper = processHelper(helper, helperClass);

		// Guarantee type compatibility by discarding
		// helper objects that are not assignment compatible
		// with the desired helper class
		if(helper!=null && !helperClass.isAssignableFrom(helper.getClass()))
			helper = null;

		return (T) helper;
	}

	private Object getFallbackHelper0(Class<?> helperClass) {
		return fallbackHelpers.get(getLookupKey(helperClass));
	}

	public boolean registerHelper(String helperClass, String contentTypeId, Object helper) {
		return registerHelper(helperClass, contentTypeId, helper, false);
	}

	public boolean registerHelper(String helperClass, String contentTypeId, Object helper, boolean replace) {
		ContentType contentType = ContentTypeRegistry.getInstance().getType(contentTypeId);
		return registerHelper(helperClass, contentType, helper, replace);
	}

	public boolean registerHelper(String helperClass, ContentType contentType, Object helper) {
		return registerHelper(helperClass, contentType, helper, false);
	}

	public boolean registerHelper(String helperClass, ContentType contentType, Object helper, boolean replace) {
		Exceptions.testNullArgument(helperClass, "helperClass"); //$NON-NLS-1$
		Exceptions.testNullArgument(contentType, "contentType"); //$NON-NLS-1$
		Exceptions.testNullArgument(helper, "helper"); //$NON-NLS-1$

		Map<String, Object> map = helpers.get(helperClass);
		if(map==null) {
			map = new LinkedHashMap<>();
			helpers.put(helperClass, map);
		}

		String contentTypeId = contentType.getId();

		Object currentHelper = map.get(contentTypeId);
		if(currentHelper!=null && !replace) {
			return false;
		}

		// Wrap extension into a compact proxy
		if(helper instanceof Extension) {
			helper = new ClassProxy((Extension)helper);
		}

		map.put(contentTypeId, helper);

		return true;
	}

	private Object findHelper0(Class<?> helperClass, ContentType contentType,
			boolean includeCompatible, boolean useFallbackHelper) {
		Map<String, Object> map = helpers.get(getLookupKey(helperClass));

		// Cannot search empty map, so sadly we have to return null
		if(map==null) {
			return null;
		}

		String contentTypeId = contentType.getId();

		Object helper = map.get(contentTypeId);

		if(helper==null && includeCompatible) {
			for(Entry<String, Object> entry : map.entrySet()) {
				if(entry.getKey().equals(contentTypeId)) {
					continue;
				}
				ContentType alternateType = ContentTypeRegistry.getInstance().getType(entry.getKey());
				if(ContentTypeRegistry.isCompatible(alternateType, contentType)) {
					helper = entry.getValue();
					break;
				}
			}
		}

		// Only if allowed use fallback helper in case we
		// could not find a 'real' helper object
		if(helper==null && useFallbackHelper) {
			helper = getFallbackHelper0(helperClass);
		}

		return helper;
	}

	public <T extends Object> T findHelper(Class<T> helperClass, Object data) {
		Exceptions.testNullArgument(helperClass, "helperClass"); //$NON-NLS-1$
		Exceptions.testNullArgument(data, "data"); //$NON-NLS-1$

		T helper = null;

		// Try direct type
		try {
			ContentType contentType = ContentTypeRegistry.getInstance().getTypeForClass(data);
			helper = findHelper(helperClass, contentType);
		} catch(IllegalArgumentException e) {
			// ignore
		}

		if(helper!=null) {
			return helper;
		}

		// Try enclosing type
		try {
			ContentType contentType = ContentTypeRegistry.getInstance().getEnclosingType(data);
			helper = findHelper(helperClass, contentType);
		} catch(IllegalArgumentException e) {
			// ignore
		}

		if(helper!=null) {
			return helper;
		}

		// Try all compatible content types
		ContentTypeCollection collection = ContentTypeRegistry.getInstance().getEnclosingTypes(data);
		for(ContentType contentType : collection.getContentTypes()) {
			helper = findHelper(helperClass, contentType, true, false);
			if(helper!=null) {
				break;
			}
		}

		return helper;
	}

	/**
	 * Looks for helper implementations of the given class that are declared
	 * to handle exactly the specified type.
	 */
	public <T extends Object> T findHelper(Class<T> helperClass, ContentType contentType) {
		return findHelper(helperClass, contentType, false, false);
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T findHelper(Class<T> helperClass, ContentType contentType,
			boolean includeCompatible, boolean useFallbackHelper) {
		Exceptions.testNullArgument(helperClass, "helperClass"); //$NON-NLS-1$
		Exceptions.testNullArgument(contentType, "contentType"); //$NON-NLS-1$

		Object helper = findHelper0(helperClass, contentType,
				includeCompatible, useFallbackHelper);

		/*
		 * Postprocessing of found helper objects:
		 *
		 * 1. ClassProxy objects instantiate their wrapped helper
		 * 2. String values are interpreted as a fully qualified class name
		 *    accessible by the current class loader. The loaded class will then
		 *    be assigned as new value for 'helper'
		 * 3. Class objects are instantiated by Class.newInstance() and used
		 *    as new value for 'helper'
		 *
		 * Finally the result is checked for assignment compatibility with the
		 * helper class and discarded if this check fails.
		 */
		if(helper!=null) {
			helper = processHelper(helper, helperClass);
		}

		// Guarantee type compatibility by discarding
		// helper objects that are not assignment compatible
		// with the desired helper class
		if(helper!=null && !helperClass.isAssignableFrom(helper.getClass())) {
			helper = null;
		}

		return (T) helper;
	}

	public <T extends Object> boolean hasHelper(Class<T> helperClass, ContentType contentType) {
		return hasHelper(helperClass, contentType, false, false);
	}

	public <T extends Object> boolean hasHelper(Class<T> helperClass, ContentType contentType,
			boolean includeCompatible, boolean useFallbackHelper) {
		Exceptions.testNullArgument(helperClass, "helperClass"); //$NON-NLS-1$
		Exceptions.testNullArgument(contentType, "contentType"); //$NON-NLS-1$

		return findHelper0(helperClass, contentType,
				includeCompatible, useFallbackHelper)!=null;
	}

	public boolean hasHelper(Class<?> helperClass, Object data) {
		Exceptions.testNullArgument(helperClass, "helperClass"); //$NON-NLS-1$
		Exceptions.testNullArgument(data, "data"); //$NON-NLS-1$

		Object helper = null;

		// Try direct type
		try {
			ContentType contentType = ContentTypeRegistry.getInstance().getTypeForClass(data);
			helper = findHelper0(helperClass, contentType, false, false);
		} catch(IllegalArgumentException e) {
			// ignore
		}

		if(helper!=null) {
			return true;
		}

		// Try enclosing type
		try {
			ContentType contentType = ContentTypeRegistry.getInstance().getEnclosingType(data);
			helper = findHelper0(helperClass, contentType, false, false);
		} catch(IllegalArgumentException e) {
			// ignore
		}

		if(helper!=null) {
			return true;
		}

		// Try all compatible content types
		ContentTypeCollection collection = ContentTypeRegistry.getInstance().getEnclosingTypes(data);
		for(ContentType contentType : collection.getContentTypes()) {
			helper = findHelper0(helperClass, contentType, true, false);
			if(helper!=null) {
				break;
			}
		}

		return helper!=null;
	}

	private Object processHelper(Object helper, Class<?> helperClass) {

		if(helper instanceof ClassProxy) {
			// Let proxy create new object instance
			helper = ((ClassProxy)helper).loadObject();
		}

		if(helper instanceof String) {
			try {
				// Use current class loader to search for desired class
				helper = Class.forName((String) helper);
			} catch (ClassNotFoundException e) {
				String msg = String.format("Unable to find helper class '%s' of type '%s'",  //$NON-NLS-1$
						helper, helperClass.getName());
				LoggerFactory.log(this, Level.WARNING, msg, e);
				helper = null;
			}
		}

		if(helper instanceof Class) {
			try {
				helper = ((Class<?>)helper).newInstance();
			} catch (InstantiationException e) {
				String msg = String.format("Unable to instantiate helper class '%s' of type '%s'",  //$NON-NLS-1$
						helper, helperClass.getName());
				LoggerFactory.log(this, Level.WARNING, msg, e);
				helper = null;
			} catch (IllegalAccessException e) {
				String msg = String.format("Not allowed to access helper class '%s' of type '%s'",  //$NON-NLS-1$
						helper, helperClass.getName());
				LoggerFactory.log(this, Level.WARNING, msg, e);
				helper = null;
			}
		}

		return helper;
	}
}
