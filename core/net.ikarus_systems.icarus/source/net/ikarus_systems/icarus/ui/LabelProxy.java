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

import javax.swing.Icon;

import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.util.Exceptions;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LabelProxy {

	private final String key;
	private final Object icon;
	private final Object[] params;

	public LabelProxy(String key, String iconName) {
		this(key, iconName, (Object[])null);
	}
	
	public LabelProxy(String key, Object icon, Object...params) {
		Exceptions.testNullArgument(key, "key"); //$NON-NLS-1$
		
		this.key = key;
		this.icon = icon;
		this.params = params;
	}
	
	@Override
	public String toString() {
		return ResourceManager.getInstance().get(key, params, key);
	}

	public Icon getIcon() {
		if(icon instanceof String) {
			return IconRegistry.getGlobalRegistry().getIcon((String)icon);
		}
		
		if(icon instanceof Icon) {
			return (Icon) icon;
		}
		
		return null;
	}
	
	public static String limit(String s) {
		return s.length()>25 ? s.substring(0, 25) : s;
	}
}