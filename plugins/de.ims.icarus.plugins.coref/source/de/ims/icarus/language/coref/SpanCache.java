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
package de.ims.icarus.language.coref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.ims.icarus.util.Counter;

public class SpanCache {
	private Counter counter = new Counter();
//	private TObjectIntMap<Span> indexMap = new TObjectIntHashMap<>();

	public boolean isSingleton(Span span) {
		return counter.getCount(span.getClusterId())==1;
	}

	public void clear() {
		counter.clear();
//		indexMap.clear();
	}

//	public int getIndex(Span span) {
//		int index = indexMap.get(span);
//		if(index==-1)
//			throw new IllegalArgumentException("Unknown span: "+span); //$NON-NLS-1$
//
//		return index;
//	}

	private void cacheSpan(Span span) {
		if(span!=null && !span.isROOT()) {

//			if(indexMap.containsKey(span))
//				throw new IllegalStateException("Span already cached: "+span); //$NON-NLS-1$

			counter.increment(span.getClusterId());
//			indexMap.put(span, indexMap.size());
		}
	}

	public void cacheSpans(Collection<Span> spans) {
		if(spans==null || spans.isEmpty()) {
			return;
		}

		for(Span span : spans) {
			cacheSpan(span);
		}
	}

	public void cacheEdges(EdgeSet edgeSet) {
		if(edgeSet==null) {
			return;
		}

		cacheEdges(edgeSet.getEdges());
	}

	public void cacheEdges(Collection<Edge> edges) {
		if(edges==null || edges.isEmpty()) {
			return;
		}

		List<Span> spans = new ArrayList<>(edges.size());
		for(Edge edge : edges) {
			spans.add(edge.getTarget());
		}

		// Preserve natural order of spans!
		Collections.sort(spans);

		cacheSpans(spans);
	}
}