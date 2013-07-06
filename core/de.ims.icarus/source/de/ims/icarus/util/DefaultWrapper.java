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
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultWrapper<O extends Object> implements Wrapper<O> {
	
	private final O element;
	
	private final boolean forwardEquals;
	private final boolean forwardHashCode;
	

	public DefaultWrapper(O element) {
		this(element, false, false);
	}
	
	public DefaultWrapper(O element, boolean forwardEquals, boolean forwardHashCode) {
		if(element==null)
			throw new IllegalArgumentException("invalid element"); //$NON-NLS-1$
		
		this.element = element;
		this.forwardEquals = forwardEquals;
		this.forwardHashCode = forwardHashCode;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if(forwardHashCode) {
			return element.hashCode();
		}
		return super.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(forwardEquals) {
			// Unwrap the target if it is a wrapper
			if(obj instanceof Wrapper) {
				obj = ((Wrapper<?>)obj).get();
			}
			return element.equals(get());
		}
		return super.equals(obj);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return element.toString();
	}

	/**
	 * @see de.ims.icarus.util.Wrapper#get()
	 */
	@Override
	public O get() {
		return element;
	}
}
