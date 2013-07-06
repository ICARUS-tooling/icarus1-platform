/*
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
