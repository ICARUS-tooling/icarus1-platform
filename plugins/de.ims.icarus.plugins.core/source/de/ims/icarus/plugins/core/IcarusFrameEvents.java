/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
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
