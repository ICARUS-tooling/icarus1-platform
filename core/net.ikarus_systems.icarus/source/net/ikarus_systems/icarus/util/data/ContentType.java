/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.data;

import java.util.Map;

import net.ikarus_systems.icarus.util.id.Identity;

/**
 * 
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ContentType extends Identity {
	
	/**
	 * Returns the root class or interface that objects associated
	 * with this {@code ContentType} must extend or implement.
	 * This method must not return {@code null}.
	 */
	Class<?> getContentClass();
	
	/**
	 * Returns a read-only collection of properties in the form of
	 * a key-value mapping. If this {@code ContentType} does not
	 * declare any properties it may either return an empty map or
	 * {@code null}.
	 */
	Map<String, Object> getProperties();
	
	/**
	 * Signals whether content is only allowed to be of the class
	 * returned by {@link #getContentClass()} and not of some subclass
	 * of it.
	 * <p>
	 * The property type is {@code boolean} and this property is only
	 * effective when the return value of {@link #getContentClass()}
	 * is not a class describing an interface, array or enum.
	 */
	public static final String STRICT_INHERITANCE = "strictInheritance"; //$NON-NLS-1$
}
