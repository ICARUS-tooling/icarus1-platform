/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util.annotation;

import javax.swing.Icon;
import javax.xml.bind.annotation.XmlEnum;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.util.id.Identity;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlEnum
public enum AnnotationDisplayMode implements Identity {

	ALL("allAnnotations", "all"), //$NON-NLS-1$ //$NON-NLS-2$
	NONE("noAnnotations", "none"), //$NON-NLS-1$ //$NON-NLS-2$
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

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return getClass().getSimpleName();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}
}
