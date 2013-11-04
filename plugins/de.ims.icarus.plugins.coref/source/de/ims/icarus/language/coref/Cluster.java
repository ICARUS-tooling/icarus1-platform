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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Cluster extends CorefListMember<Span> implements Comparable<Cluster> {
	
	protected int id;
	
	protected Map<Span, Edge> edges;
	
	public Cluster(int id, Span...spans) {
		this.id = id;
		
		if(spans!=null) {
			for(Span span : spans) {
				addSpan(span);
			}
		}
	}
	
	public int getId() {
		return id;
	}

	public void addSpan(Span span) {
		if(span==null)
			throw new NullPointerException("Invalid span"); //$NON-NLS-1$
		
		if(items==null) {
			items = new ArrayList<>();
		}
		if(edges==null) {
			edges = new HashMap<>();
		}
		
		Span last = items.isEmpty() ? null : items.get(items.size()-1);
		if(last!=null) {
			edges.put(span, new Edge(last, span));
		}
		items.add(span);
	}

	public void addSpan(Span span, Edge edge) {
		if(span==null)
			throw new NullPointerException("Invalid span"); //$NON-NLS-1$
		if(edge==null)
			throw new NullPointerException("Invalid edge"); //$NON-NLS-1$
		
		if(items==null) {
			items = new ArrayList<>();
		}
		if(edges==null) {
			edges = new HashMap<>();
		}

		items.add(span);
		edges.put(span, edge);
	}
	
	public void addEdge(Edge edge) {
		if(edge==null)
			throw new NullPointerException("Invalid egde"); //$NON-NLS-1$
		if(items==null || !items.contains(edge.getTarget()))
			throw new IllegalArgumentException("Unknown target for edge: "+edge); //$NON-NLS-1$
		
		if(edges==null) {
			edges = new HashMap<>();
		}
		
		edges.put(edge.getTarget(), edge);
	}
	
	/**
	 * Returns the incoming edge for the given span within this cluster
	 */
	public Edge getEdge(Span span) {
		return edges==null ? null : edges.get(span);
	}
	
	public Span[] getSpans() {
		Span[] result = new Span[0];
		return items==null ? result : items.toArray(result);
	}
	
	public void setEdges(EdgeSet edgeSet) {
		if(items==null || items.isEmpty())
			throw new IllegalStateException("Cannot add edges without registered spans"); //$NON-NLS-1$
		
		if(edges==null) {
			edges = new HashMap<>();
		}
		
		Set<Span> lookup = new HashSet<>(items);
		for(int i=0; i<edgeSet.size(); i++) {
			Edge edge = edgeSet.get(i);
			if(lookup.contains(edge.getTarget())) {
				edges.put(edge.getTarget(), edge);
			}
		}
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getSpanContentType();
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Cluster other) {
		return id - other.id;
	}
}
