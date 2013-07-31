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
package de.ims.icarus.util;

/**
 * Type safe definitions for the order possible bewteen two objects.  
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum Order {
	
	/**
	 * Indicates that for two objects {@code A} and {@code B} and order
	 * is defined such that {@code A} is placed <i>before</i> {@code B}.
	 */
	BEFORE("before"), //$NON-NLS-1$

	/**
	 * Indicates that for two objects {@code A} and {@code B} and order
	 * is defined such that {@code B} is placed <i>before</i> {@code A}.
	 */
	AFTER("after"), //$NON-NLS-1$
	
	/**
	 * No particular order exists between the two objects.
	 */
	UNDEFINED("undefined"); //$NON-NLS-1$
	
	private final String token;
	
	private Order(String token) {
		this.token = token;
	}
	
	public String getToken() {
		return token;
	}

	public static Order parseOrder(String s) {
		if(s==null || s.isEmpty())
			throw new IllegalArgumentException("Invalid string"); //$NON-NLS-1$
		
		for(Order order : values()) {
			if(order.name().toLowerCase().startsWith(s)) {
				return order;
			}
		}
		
		throw new IllegalArgumentException("Unknown order string: "+s); //$NON-NLS-1$
	}
}
