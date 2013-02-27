/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface IcarusFrameEvents {

	public static final String CLOSING = "closing"; //$NON-NLS-1$

	public static final String CLOSED = "closed"; //$NON-NLS-1$
	
	public static final String MINIMIZED = "minimized"; //$NON-NLS-1$
	
	public static final String ACTIVATED = "activated"; //$NON-NLS-1$
	
	public static final String DEACTIVATED = "deactivated"; //$NON-NLS-1$
	
	public static final String RESTORED = "restored"; //$NON-NLS-1$
}
