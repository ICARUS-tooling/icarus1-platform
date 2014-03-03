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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.language.model.standard.structure;

import java.util.Arrays;

import de.ims.icarus.language.model.api.Edge;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.util.collections.LongHashMap;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class GraphStruct {

	@Link
	private LongHashMap<Object> incoming;
	@Link
	private LongHashMap<Object> outgoing;

	private LongHashMap<Object> getMap(boolean incoming) {
		if(incoming) {
			if(this.incoming==null) {
				this.incoming = new LongHashMap<>();
			}
			return this.incoming;
		} else {
			if(this.outgoing==null) {
				this.outgoing = new LongHashMap<>();
			}
			return this.outgoing;
		}
	}

	public int getEdgeCount(boolean incoming, Markable markable) {
		LongHashMap<Object> map = getMap(incoming);
		Object edges = map.get(markable.getId());
		if(edges ==null) {
			return 0;
		} else if(edges instanceof Edge) {
			return 1;
		} else {
			return ((Edge[])edges).length;
		}
	}

	public Edge getEdgeAt(boolean incoming, Markable markable, int index) {
		LongHashMap<Object> map = getMap(incoming);
		Object edges = map.get(markable.getId());
		if(edges ==null) {
			throw new IndexOutOfBoundsException();
		} else if(edges instanceof Edge) {
			if(index!=0)
				throw new IndexOutOfBoundsException();
			return (Edge) edges;
		} else {
			return ((Edge[])edges)[index];
		}
	}

	public void clear() {
		if(incoming!=null) {
			incoming.clear();
		}
		if(outgoing!=null) {
			outgoing.clear();
		}
	}

	public void removeEdge(boolean incoming, Markable markable, Edge edge) {
		LongHashMap<Object> map = getMap(incoming);
		Object edges = map.get(markable.getId());
		if(edges==null) {
			throw new IllegalArgumentException("No edges defined for markable: "+markable); //$NON-NLS-1$
		} else if(edges instanceof Edge) {
			if(edges!=edge)
				throw new IllegalArgumentException("Unrecognized edge: "+edge); //$NON-NLS-1$
			map.remove(markable.getId());
		} else {
			Object replacement = remove((Edge[]) edges, edge);
			if(replacement==null)
				throw new IllegalArgumentException("Unrecognized edge: "+edge); //$NON-NLS-1$

			map.put(markable.getId(), replacement);
		}
	}

	public void removeEdge(Edge edge) {
		removeEdge(true, edge.getTarget(), edge);
		removeEdge(false, edge.getSource(), edge);

		if(!edge.isDirected()) {
			removeEdge(false, edge.getTarget(), edge);
			removeEdge(true, edge.getSource(), edge);
		}
	}

	// Removes a single edge from an array of edges
	private Object remove(Edge[] edges, Edge edge) {

		int index = -1;

		for(int i=0; i<edges.length; i++) {
			if(edges[i]==edge) {
				index = i;
				break;
			}
		}

		if(index==-1) {
			return null;
		} else if(edges.length==2) {
			return index==0 ? edges[1] : edges[0];
		} else {
			Edge[] result = new Edge[edges.length-1];

			int idx = 0;

			for(int i=0; i<edges.length; i++) {
				if(i!=index) {
					result[idx++] = edges[i];
				}
			}

			return result;
		}
	}

	public void addEdge(boolean incoming, Markable markable, Edge edge) {
		LongHashMap<Object> map = getMap(incoming);
		Object edges = map.get(markable.getId());
		if(edges==null) {
			edges = edge;
		} else if(edges instanceof Edge) {
			edges = new Edge[] {(Edge) edges, edge};
		} else {
			Edge[] old = (Edge[]) edges;
			Edge[] tmp = Arrays.copyOf(old, old.length+1);
			tmp[old.length] = edge;
			edges = tmp;
		}

		map.put(markable.getId(), edges);
	}

	public void addEdge(Edge edge) {
		addEdge(true, edge.getTarget(), edge);
		addEdge(false, edge.getSource(), edge);

		if(!edge.isDirected()) {
			addEdge(false, edge.getTarget(), edge);
			addEdge(true, edge.getSource(), edge);
		}
	}
}
