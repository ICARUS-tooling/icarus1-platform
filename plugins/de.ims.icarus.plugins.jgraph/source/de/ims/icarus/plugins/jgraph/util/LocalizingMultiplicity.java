/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.jgraph.util;

import java.util.Collection;


import com.mxgraph.view.mxMultiplicity;

import de.ims.icarus.resources.Localizable;
import de.ims.icarus.resources.ResourceManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LocalizingMultiplicity extends mxMultiplicity implements Localizable {
	
	protected String countErrorKey;
	protected String typeErrorKey;

	/**
	 * @param source
	 * @param type
	 * @param attr
	 * @param value
	 * @param min
	 * @param max
	 * @param validNeighbors
	 * @param countErrorKey
	 * @param typeErrorKey
	 * @param validNeighborsAllowed
	 */
	public LocalizingMultiplicity(boolean source, String type, String attr,
			String value, int min, String max,
			Collection<String> validNeighbors, String countErrorKey,
			String typeErrorKey, boolean validNeighborsAllowed) {
		super(source, type, attr, value, min, max, validNeighbors, null,
				null, validNeighborsAllowed);
		
		this.countErrorKey = countErrorKey;
		this.typeErrorKey = typeErrorKey;
		
		localize();
	}

	/**
	 * @see de.ims.icarus.resources.Localizable#localize()
	 */
	@Override
	public void localize() {
		countError = ResourceManager.getInstance().get(countErrorKey);
		typeError = ResourceManager.getInstance().get(typeErrorKey);
	}	
}
