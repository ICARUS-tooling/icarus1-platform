/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultWrapper<O extends Object> implements Wrapper<O> {
	
	private final O element;
	
	public DefaultWrapper(O element) {
		if(element==null)
			throw new IllegalArgumentException("invalid element"); //$NON-NLS-1$
		
		this.element = element;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Wrapper#get()
	 */
	@Override
	public O get() {
		return element;
	}

}
