/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.core;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface IcarusFrameEvents {

	// Forwarded window events
	public static final String CLOSING = "closing"; //$NON-NLS-1$

	public static final String CLOSED = "closed"; //$NON-NLS-1$
	
	public static final String MINIMIZED = "minimized"; //$NON-NLS-1$
	
	public static final String ACTIVATED = "activated"; //$NON-NLS-1$
	
	public static final String DEACTIVATED = "deactivated"; //$NON-NLS-1$
	
	public static final String RESTORED = "restored"; //$NON-NLS-1$
	
	// Perspective events
	public static final String PERSPECTIVE_CLOSED = "perspectiveClosed"; //$NON-NLS-1$

	public static final String PERSPECTIVE_OPENED = "perspectiveOpened"; //$NON-NLS-1$
}
