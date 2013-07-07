/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph.util;

import java.util.Collection;

import net.ikarus_systems.icarus.resources.Localizable;
import net.ikarus_systems.icarus.resources.ResourceManager;

import com.mxgraph.view.mxMultiplicity;

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
	 * @see net.ikarus_systems.icarus.resources.Localizable#localize()
	 */
	@Override
	public void localize() {
		countError = ResourceManager.getInstance().get(countErrorKey);
		typeError = ResourceManager.getInstance().get(typeErrorKey);
	}	
}
