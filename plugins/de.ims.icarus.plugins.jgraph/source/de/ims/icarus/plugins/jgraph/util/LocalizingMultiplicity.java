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
