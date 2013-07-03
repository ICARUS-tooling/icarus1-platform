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
import net.ikarus_systems.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum AnnotationCombinationPolicy implements Identity {
	// TODO
	NONE("none"); //$NON-NLS-1$
	
	private final String key;
	
	private AnnotationCombinationPolicy(String key) {
		this.key = key;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return getClass().getSimpleName();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getName()
	 */
	@Override
	public String getName() {
		return ResourceManager.getInstance().get(
				"core.helpers.annotationCombinationPolicy."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getDescription()
	 */
	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"core.helpers.annotationCombinationPolicy."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}

}
