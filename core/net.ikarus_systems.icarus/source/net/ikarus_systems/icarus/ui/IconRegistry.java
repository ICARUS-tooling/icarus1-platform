/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui;

import java.io.ObjectStreamException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.util.Exceptions;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class IconRegistry {

	private final Map<String, Icon> icons = new HashMap<>();
	
	private final List<Entry<ClassLoader, String>> loaders = new ArrayList<>();
	
	private final IconRegistry parent;
	
	private static IconRegistry globalRegistry;

	public static IconRegistry getGlobalRegistry() {
		if(globalRegistry==null) {
			synchronized (IconRegistry.class) {
				if(globalRegistry==null) {
					globalRegistry= new IconRegistry(null);
					globalRegistry.addSearchPath(IconRegistry.class.getClassLoader(), 
							"net/ikarus_systems/icarus/ui/icons/"); //$NON-NLS-1$
				}
			}
		}
		
		return globalRegistry;
	}
	
	public static IconRegistry newRegistry(IconRegistry parent) {
		// TODO maybe force globalRegistry to be fallback parent?
		return new IconRegistry(parent);
	}

	private IconRegistry(IconRegistry parent) {
		this.parent = parent;
	}
	
	// prevent multiple deserialization
	private Object readResolve() throws ObjectStreamException {
		return getGlobalRegistry();
	}
	
	// prevent cloning
	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private Icon loadIcon(String name) {
		Icon icon = null;
		
		synchronized (loaders) {
			for(Entry<ClassLoader, String> entry : loaders) {
				try {
					String prefix = entry.getValue();
					ClassLoader loader = entry.getKey();
					
					// apply prefix
					String path = prefix+name;
					
					// try to locate resource
					URL location = loader.getResource(path);
					
					// create new icon
					if(location!=null) {
						icon = new ImageIcon(location);
					}
					
					if(icon!=null) {
						break;
					}
					
				} catch(Exception e) {
					LoggerFactory.log(this, Level.SEVERE, "Error while loading icon: "+name, e); //$NON-NLS-1$
				}
			}
		}
		
		// Save icon for future calls or delegate search
		// to parent which will save loaded icons in its own
		// map so that calls to lookupIcon(String) will search
		// the correct maps
		if(icon!=null) {
			icons.put(name, icon);
		} else if(parent!=null) {
			icon = parent.loadIcon(name);
		}
		
		return icon;
	}
	
	public void addSearchPath(String prefix) {
		addSearchPath(null, prefix);
	}
	
	public void addSearchPath(ClassLoader loader, String prefix) {
		if(loader==null && prefix==null)
			throw new IllegalArgumentException("Either loader or prefix has to be defined!"); //$NON-NLS-1$
		
		if(loader==null)
			loader = getClass().getClassLoader();
		
		if(prefix==null)
			prefix = ""; //$NON-NLS-1$
		
		synchronized (loaders) {
			
			// check for duplicates
			for(int i=0; i<loaders.size(); i++) {
				Entry<ClassLoader, String> entry = loaders.get(i);
				if(entry.getKey().equals(loader) && entry.getValue().equals(prefix)) {
					return;
				}
			}
			
			// not present yet -> add new entry
			Entry<ClassLoader, String> entry = 
					new AbstractMap.SimpleEntry<ClassLoader, String>(loader, prefix);
			loaders.add(entry);
		}
	}
	
	public void removeSearchPath(ClassLoader loader, String prefix) {
		Exceptions.testNullArgument(loader, "loader"); //$NON-NLS-1$
		if(prefix==null)
			prefix = ""; //$NON-NLS-1$

		synchronized (loaders) {
			for(int i=0; i<loaders.size(); i++) {
				Entry<ClassLoader, String> entry = loaders.get(i);
				if(entry.getKey().equals(loader) && entry.getValue().equals(prefix)) {
					loaders.remove(i);
					break;
				}
			}
		}
	}
	
	private Icon lookupIcon(String name) {
		Icon icon = icons.get(name);
		
		if(icon==null && parent!=null)
			icon = parent.lookupIcon(name);
		
		return icon;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public Icon getIcon(String name) {
		Exceptions.testNullArgument(name, "name"); //$NON-NLS-1$
		
		Icon icon = lookupIcon(name);
		
		if(icon==null) {
			icon = loadIcon(name);
		}
		
		return icon;
	}
}
