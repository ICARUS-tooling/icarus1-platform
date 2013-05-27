/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.annotation;

import javax.swing.Icon;

import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.IconRegistry;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum AnnotationDisplayMode {

	ALL("allAnnotations", "all"), //$NON-NLS-1$ //$NON-NLS-2$
	NONE("noAnnotation", "none"), //$NON-NLS-1$ //$NON-NLS-2$
	FIRST_ONLY("firstAnnotation", "first"), //$NON-NLS-1$ //$NON-NLS-2$
	LAST_ONLY("lastAnnotation", "last"), //$NON-NLS-1$ //$NON-NLS-2$
	SELECTED("selectedAnnotation", "selected"); //$NON-NLS-1$ //$NON-NLS-2$
	
	private final String key, token;
	
	private AnnotationDisplayMode(String key, String token) {
		this.key = key;
		this.token = token;
	}
	
	public String getToken() {
		return token;
	}
	
	public AnnotationDisplayMode parseMode(String s) {
		for(AnnotationDisplayMode mode : values()) {
			if(mode.token.equals(s)) {
				return mode;
			}
		}
		
		throw new IllegalArgumentException("Unknown token: "+s); //$NON-NLS-1$
	}
	
	public String getName() {
		return ResourceManager.getInstance().get(
				"core.helpers.annotationDisplayMode."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"core.helpers.annotationDisplayMode."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public Icon getIcon() {
		return IconRegistry.getGlobalRegistry().getIcon(token+".gif"); //$NON-NLS-1$
	}
}
