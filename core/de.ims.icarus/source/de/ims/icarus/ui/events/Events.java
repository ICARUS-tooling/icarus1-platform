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
package de.ims.icarus.ui.events;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Events {

	public static final String PROPERTY = "property"; //$NON-NLS-1$
	public static final String ADD = "adding"; //$NON-NLS-1$
	public static final String ADDED = "added"; //$NON-NLS-1$
	public static final String REMOVE = "removing"; //$NON-NLS-1$
	public static final String REMOVED = "removed"; //$NON-NLS-1$
	public static final String INSERT = "inserting"; //$NON-NLS-1$
	public static final String INSERTED = "inserted"; //$NON-NLS-1$
	public static final String MOVE = "moving"; //$NON-NLS-1$
	public static final String MOVED = "moved"; //$NON-NLS-1$
	public static final String CLEAN = "cleaning"; //$NON-NLS-1$
	public static final String CLEANED = "cleaned"; //$NON-NLS-1$
	public static final String DELETE = "deleting"; //$NON-NLS-1$
	public static final String DELETED = "deleted"; //$NON-NLS-1$
	public static final String CHANGE = "changing"; //$NON-NLS-1$
	public static final String CHANGED = "changed"; //$NON-NLS-1$

	public static final String SELECTION_CHANGED = "selectionChanged"; //$NON-NLS-1$
}
