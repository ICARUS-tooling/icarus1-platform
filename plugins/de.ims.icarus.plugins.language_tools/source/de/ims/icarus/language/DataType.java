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
package de.ims.icarus.language;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum DataType {
	
	/**
	 * Data that originated from some sort of automated generation
	 * process should be associated with this type.
	 */
	SYSTEM,

	/**
	 * Describes the gold standard, i.e. the one version
	 * of some data that is considered to define the "correct"
	 * state or annotation.
	 */
	GOLD,
	
	/**
	 * Describes data that was created by the user himself.
	 */
	USER,
}
