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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.io;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchXmlConstants {

	public static final String TAG_SEARCH = "search"; //$NON-NLS-1$
	public static final String TAG_RESULT = "result"; //$NON-NLS-1$
	public static final String TAG_GROUP = "group"; //$NON-NLS-1$
	public static final String TAG_LABEL = "label"; //$NON-NLS-1$
	public static final String TAG_ENTRY = "entry"; //$NON-NLS-1$
	public static final String TAG_HIT = "hit"; //$NON-NLS-1$
	public static final String TAG_PARAMETER = "parameter"; //$NON-NLS-1$
	public static final String TAG_QUERY = "query"; //$NON-NLS-1$
	public static final String TAG_TARGET = "target"; //$NON-NLS-1$

	public static final String ATTR_FACTORY = "factory"; //$NON-NLS-1$
	public static final String ATTR_KEY = "key"; //$NON-NLS-1$
	public static final String ATTR_INDICES = "indices"; //$NON-NLS-1$
	public static final String ATTR_INDEX = "index"; //$NON-NLS-1$
	public static final String ATTR_TYPE = "type"; //$NON-NLS-1$
	public static final String ATTR_DIMENSION = "dimension"; //$NON-NLS-1$
}
