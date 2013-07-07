/*
 * $Revision: 17 $
 * $Date: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/LabelProxy.java $
 *
 * $LastChangedDate: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $ 
 * $LastChangedRevision: 17 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.ui;

import javax.swing.Icon;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.Exceptions;


/**
 * @author Markus Gärtner
 * @version $Id: LabelProxy.java 17 2013-03-25 00:44:03Z mcgaerty $
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
		return ResourceManager.getInstance().get(key, key, params);
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
