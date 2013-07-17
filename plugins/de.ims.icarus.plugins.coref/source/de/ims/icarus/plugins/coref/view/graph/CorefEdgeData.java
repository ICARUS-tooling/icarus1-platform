/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.graph;

import java.io.Serializable;

import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.plugins.jgraph.cells.GraphEdge;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorefEdgeData implements Serializable, GraphEdge {

	private static final long serialVersionUID = 440883995135005413L;

	public static final int FALSE_PREDICTED_EDGE = 1;
	public static final int MISSING_GOLD_EDGE = 2; 

	protected Edge edge;
	protected String label;
	
	protected int edgeType = 0;
	
	protected CorefEdgeData() {
		// no-op
	}
	
	public CorefEdgeData(Edge edge) {
		setEdge(edge);
	}
	
	public CorefEdgeData(Edge edge, int edgeType) {
		setEdge(edge);
		setEdgeType(edgeType);
	}

	public Edge getEdge() {
		return edge;
	}

	public void setEdge(Edge edge) {
		if(edge==null)
			throw new IllegalArgumentException("Invalid edge"); //$NON-NLS-1$
		
		this.edge = edge;
		label = null;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CorefEdgeData) {
			CorefEdgeData other = (CorefEdgeData) obj;
			return edge.equals(other.getEdge());
		}
		return false;
	}

	@Override
	public CorefEdgeData clone() {
		return new CorefEdgeData(getEdge());
	}
	
	public String getLabel() {
		if(label==null) {
			label = edge.toString();
		}
		return label;
	}

	@Override
	public String toString() {
		//return getLabel();
		return ""; //$NON-NLS-1$
	}

	public int getEdgeType() {
		return edgeType;
	}

	public void setEdgeType(int edgeType) {
		this.edgeType = edgeType;
	}

	public boolean isFalsePredictedEdge() {
		return edgeType==FALSE_PREDICTED_EDGE;
	}
	
	public boolean isMissingGoldEdge() {
		return edgeType==MISSING_GOLD_EDGE;
	}
}
