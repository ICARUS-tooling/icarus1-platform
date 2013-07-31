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
package de.ims.icarus.resources;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface ResourceConstants {

	/**
	 * The default key used to store localization
	 * data in an object's properties for the {@code text}
	 * field.
	 */
	public static final String DEFAULT_TEXT_KEY = "_localizationKey_text"; //$NON-NLS-1$
	
	/**
	 * The default key used to store localization
	 * data in an object's properties for the {@code description}
	 * field. This data is optional and most localization
	 * facilities do not report an error in the case it is
	 * missing for a certain object.
	 */
	public static final String DEFAULT_DESCRIPTION_KEY = "_localizationKey_desc"; //$NON-NLS-1$
}
