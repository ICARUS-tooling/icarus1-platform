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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EdgeSet extends CorefMember {
	
	private List<Edge> edges;

	public EdgeSet() {
		// no-op
	}
	
	public int size() {
		return edges==null ? 0 : edges.size();
	}
	
	public Edge getEdgeAt(int index) {
		return edges==null ? null : edges.get(index);
	}
	
	public void addEdge(Edge edge) {
		if(edges==null) {
			edges = new ArrayList<>();
		}
		
		edges.add(edge);
	}
	
	/**
	 * Returns a read-only view of all the edges in this {@code EdgeSet}
	 */
	public List<Edge> getEdges() {
		return Collections.unmodifiableList(edges);
	}
	
	@Override
	public EdgeSet clone() {
		EdgeSet clone = new EdgeSet();
		clone.setProperties(cloneProperties());
		if(edges!=null) {
			clone.edges = new ArrayList<>(edges);
		}
		
		return clone;
	}
}
