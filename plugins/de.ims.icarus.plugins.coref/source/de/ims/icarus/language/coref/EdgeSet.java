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
package de.ims.icarus.language.coref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EdgeSet extends CorefListMember<Edge> {
	
	public EdgeSet() {
		// no-op
	}
	
	public void addEdge(Edge edge) {
		if(items==null) {
			items = new ArrayList<>();
		}
		
		items.add(edge);
	}
	
	/**
	 * Returns a read-only view of all the edges in this {@code EdgeSet}
	 */
	public List<Edge> getEdges() {
		List<Edge> result = items;
		if(result==null) {
			result = Collections.emptyList();
		} else {
			result = CollectionUtils.getListProxy(result);
		}
		return result;
	}
	
	@Override
	public EdgeSet clone() {
		EdgeSet clone = new EdgeSet();
		clone.setProperties(cloneProperties());
		if(items!=null) {
			clone.items = new ArrayList<>(items);
		}
		
		return clone;
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getEdgeContentType();
	}
}
