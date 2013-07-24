/*
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
		return Collections.unmodifiableList(items);
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
