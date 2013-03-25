/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.helper;

import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.CellEditor;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.util.ClassProxy;
import net.ikarus_systems.icarus.util.Exceptions;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class UIHelperRegistry {
	
	private static UIHelperRegistry instance;

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
		
		// register renderers
		setFallbackHelper(ListCellRenderer.class, "javax.swing.DefaultListCellRenderer"); //$NON-NLS-1$
		setFallbackHelper(TreeCellRenderer.class, "javax.swing.tree.DefaultTreeCellRenderer"); //$NON-NLS-1$
		setFallbackHelper(TableCellRenderer.class, "javax.swing.table.DefaultTableCellRenderer"); //$NON-NLS-1$
		
		// register editors
		setFallbackHelper(CellEditor.class, "javax.swing.DefaultCellEditor"); //$NON-NLS-1$
		setFallbackHelper(TableCellEditor.class, "javax.swing.DefaultCellEditor"); //$NON-NLS-1$
		setFallbackHelper(TreeCellEditor.class, "javax.swing.DefaultCellEditor"); //$NON-NLS-1$
		
		// ACTUAL HELPERS
		registerHelper(ListCellRenderer.class, 
				"org.java.plugin.registry.Extension",  //$NON-NLS-1$
				"net.ikarus_systems.icarus.plugins.ExtensionListCellRenderer"); //$NON-NLS-1$
		
		// TODO do we have some helpers that are not part of a plug-in?
	}
	
	// prevent multiple deserialization
	private Object readResolve() throws ObjectStreamException {
		throw new NotSerializableException(UIHelperRegistry.class.getName());
	}
	
	// prevent cloning
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	public void setFallbackHelper(Class<?> helperClass, Object helper) {
		Exceptions.testNullArgument(helperClass, "helperClass"); //$NON-NLS-1$
		Exceptions.testNullArgument(helper, "helper"); //$NON-NLS-1$
		
		fallbackHelpers.put(helperClass.getName(), helper);
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
		return fallbackHelpers.get(helperClass.getName());
	}

	public void registerHelper(Class<?> helperClass, String objectClassName, Object helper) {
		registerHelper(helperClass, objectClassName, helper, false);
	}
	
	public void registerHelper(Class<?> helperClass, String objectClassName, Object helper, boolean replace) {
		Exceptions.testNullArgument(helperClass, "helperClass"); //$NON-NLS-1$
		Exceptions.testNullArgument(objectClassName, "objectClassName"); //$NON-NLS-1$
		Exceptions.testNullArgument(helper, "helper"); //$NON-NLS-1$
		
		Map<String, Object> map = helpers.get(helperClass.getName());
		if(map==null) {
			map = new HashMap<>();
			helpers.put(helperClass.getName(), map);
		}
		
		Object currentHelper = map.get(objectClassName);
		if(currentHelper!=null && !replace)
			return;

		// Wrap extension into a compact proxy
		if(helper instanceof Extension) {
			Extension ext = (Extension) helper;
			ClassLoader loader = PluginUtil.getPluginManager().getPluginClassLoader(
					ext.getDeclaringPluginDescriptor());
			String className = ext.getParameter("class").valueAsString();  //$NON-NLS-1$
			helper = new ClassProxy(className, loader);
		}
	
		map.put(objectClassName, helper);
	}
	
	private Object findHelper0(Map<String, Object> map, Class<?> clazz, 
			boolean includeSuperTypes, boolean includeInterfaces) {
		
		// Try to fetch a helper for this specific class
		Object helper = map.get(clazz.getName());
		
		// If there is no special helper for this class
		// we have to check superclasses and interfaces
		// if we are allowed to do so
		
		// first check interfaces
		if(helper==null && (includeInterfaces || clazz.isInterface())) {
			for(Class<?> implementedInterface : clazz.getInterfaces()) {
				helper = findHelper0(map, implementedInterface, true, true);
				if(helper!=null)
					break;
			}
		}
		
		// Now check superclass
		if(helper==null && includeSuperTypes) {
			clazz = clazz.getSuperclass();
			if(clazz!=null) {
				helper = findHelper0(map, clazz, includeSuperTypes, includeInterfaces);
			}
		}
		
		return helper;
	}

	public <T extends Object> T findHelper(Class<T> helperClass, Object obj) {
		return findHelper(helperClass, obj, true, true, true);
	}
	
	/**
	 * 
	 * @param helperClass
	 * @param obj
	 * @param includeSuperTypes
	 * @param includeInterfaces
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> T findHelper(Class<T> helperClass, Object obj, 
			boolean includeSuperTypes, boolean includeInterfaces, boolean useFallbackHelper) {
		Exceptions.testNullArgument(helperClass, "helperClass"); //$NON-NLS-1$
		Exceptions.testNullArgument(obj, "obj"); //$NON-NLS-1$
			
		Map<String, Object> map = helpers.get(helperClass.getName());
		
		// Cannot search empty map, so sadly we have to return null
		if(map==null) {
			return null;
		}
		
		// Ensure our search object is a class
		if(!(obj instanceof Class)) {
			obj = obj.getClass();
		}
		
		// Delegate search to recursive search method
		Object helper = findHelper0(map, (Class<?>)obj, includeSuperTypes, includeInterfaces);

		// Only if allowed use fallback helper in case we
		// could not find a 'real' helper object
		if(helper==null && useFallbackHelper) {
			helper = getFallbackHelper0(helperClass);
		}

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
	
	public <T extends Object> boolean hasHelper(Class<T> helperClass, Object obj) {
		return hasHelper(helperClass, obj, true, true, true);
	}
	
	public <T extends Object> boolean hasHelper(Class<T> helperClass, Object obj, 
			boolean includeSuperTypes, boolean includeInterfaces, boolean useFallbackHelper) {
		Exceptions.testNullArgument(helperClass, "helperClass"); //$NON-NLS-1$
		Exceptions.testNullArgument(obj, "obj"); //$NON-NLS-1$
			
		Map<String, Object> map = helpers.get(helperClass.getName());
		
		// Cannot search empty map, so sadly we have to return null
		if(map==null) {
			return false;
		}
		
		// Ensure our search object is a class
		if(!(obj instanceof Class)) {
			obj = obj.getClass();
		}
		
		// Delegate search to recursive search method
		Object helper = findHelper0(map, (Class<?>)obj, includeSuperTypes, includeInterfaces);

		// Only if allowed use fallback helper in case we
		// could not find a 'real' helper object
		if(helper==null && useFallbackHelper) {
			helper = getFallbackHelper0(helperClass);
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
				LoggerFactory.getLogger(UIHelperRegistry.class).log(Level.WARNING, msg, e);
				helper = null;
			}
		} 
		
		if(helper instanceof Class) {
			try {
				helper = ((Class<?>)helper).newInstance();
			} catch (InstantiationException e) {
				String msg = String.format("Unable to instantiate helper class '%s' of type '%s'",  //$NON-NLS-1$
						helper, helperClass.getName());
				LoggerFactory.getLogger(UIHelperRegistry.class).log(Level.WARNING, msg, e);
				helper = null;
			} catch (IllegalAccessException e) {
				String msg = String.format("Not allowed to access helper class '%s' of type '%s'",  //$NON-NLS-1$
						helper, helperClass.getName());
				LoggerFactory.getLogger(UIHelperRegistry.class).log(Level.WARNING, msg, e);
				helper = null;
			}
		}
		
		return helper;
	}
}
