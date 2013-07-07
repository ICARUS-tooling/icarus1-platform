/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/actions/BindableAction.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.ui.actions;

import java.util.Map;

import javax.swing.Action;

/**
 * @author Markus Gärtner
 * @version $Id: BindableAction.java 7 2013-02-27 13:18:56Z mcgaerty $
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
