/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.actions;

import java.util.Map;

import javax.swing.Action;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface BindableAction extends Action {

	/**
	 * Binds this {@code Action} to the specified {@code owner}
	 * using the given collection of properties to configure it.
	 * Note that the returned {@code Action} does not have to be
	 * this very instance. There might be implementations that
	 * use a singleton class instance for performance or other
	 * reasons. In this case the signature of this method allows
	 * for proper configuration and the single shared instance
	 * can be returned (typically the object this method is called
	 * on will be discarded unless explicitly returned as bound
	 * action).
	 * 
	 * @param owner
	 * @param properties
	 * @return the fully configured and bound {@code Action}
	 */
	Action bind(Object owner, Map<String, Object> properties);
}
