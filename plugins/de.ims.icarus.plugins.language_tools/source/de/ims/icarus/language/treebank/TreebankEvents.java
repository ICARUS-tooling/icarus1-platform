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
package de.ims.icarus.language.treebank;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface TreebankEvents {

	public static final String ADDED = "treebank:added"; //$NON-NLS-1$
	public static final String REMOVED = "treebank:removed"; //$NON-NLS-1$
	public static final String UPDATED = "treebank:updated"; //$NON-NLS-1$
	public static final String LOADING = "treebank:loading"; //$NON-NLS-1$
	public static final String LOADED = "treebank:loaded"; //$NON-NLS-1$
	public static final String FREEING = "treebank:freeing"; //$NON-NLS-1$
	public static final String FREED = "treebank:freed"; //$NON-NLS-1$
	public static final String CHANGED = "treebank:changed"; //$NON-NLS-1$
	public static final String LOCATION = "treebank:location"; //$NON-NLS-1$
	public static final String EDITABLE = "treebank:editable"; //$NON-NLS-1$
	public static final String METADATA = "treebank:metadata"; //$NON-NLS-1$
}
