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
package de.ims.icarus.language.model;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum ContainerType {
	
	/**
	 * The container holds a single {@code Markable}.
	 */
	SINGLETON,
	
	/**
	 * The container holds a non-continuous collection
	 * of {@code Markable}s. The elements may appear in
	 * any order.
	 */
	UNORDERED_SET,

	/**
	 * The container holds a non-continuous but ordered
	 * collection of {@code Markable}s. 
	 */
	ORDERED_SET,
	
	/**
	 * The container holds an ordered and continuous list
	 * of {@code Markable}s.
	 */
	SPAN,
}