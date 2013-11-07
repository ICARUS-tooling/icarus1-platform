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
package de.ims.icarus.plugins.coref.view.graph;

import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.plugins.jgraph.cells.GraphEdge;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorefEdgeData extends CorefCellData<Edge> implements GraphEdge {

	private static final long serialVersionUID = 440883995135005413L;
	
	protected CorefEdgeData() {
		// no-op
	}
	
	public CorefEdgeData(Edge edge) {
		super(edge);
	}
	
	public CorefEdgeData(Edge edge, int edgeType) {
		super(edge, edgeType);
	}

	public CorefEdgeData(Edge data, int type, long highlight) {
		super(data, type, highlight);
	}

	public Edge getEdge() {
		return data;
	}

	public void setEdge(Edge edge) {
		setData(edge);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CorefEdgeData) {
			CorefEdgeData other = (CorefEdgeData) obj;
			return data.equals(other.getEdge());
		}
		return false;
	}

	@Override
	public CorefEdgeData clone() {
		return new CorefEdgeData(getEdge());
	}

	@Override
	protected String createLabel() {
		return data.toString();
	}

	@Override
	public String toString() {
		//return getLabel();
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return data.hashCode();
	}
}
