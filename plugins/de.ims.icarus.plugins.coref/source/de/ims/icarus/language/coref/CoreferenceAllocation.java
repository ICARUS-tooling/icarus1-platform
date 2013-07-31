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

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.util.id.DuplicateIdentifierException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceAllocation extends CorefMember {
	
	private Map<String, SpanSet> spanMap;
	
	private Map<String, EdgeSet> edgeMap;

	public CoreferenceAllocation() {
		// no-op
	}
	
	public int size() {
		return spanMap==null ? 0 : spanMap.size(); 
	}
	
	public void free() {
		spanMap = null;
		edgeMap = null;
		properties = null;
	}

	public SpanSet getSpanSet(String documentId) {
		return spanMap==null ? null : spanMap.get(documentId);
	}
	
	public EdgeSet getEdgeSet(String documentId) {
		return edgeMap==null ? null : edgeMap.get(documentId);
	}
	
	public void setSpans(String documentId, int sentenceIndex, Span[] spans) {
		if(spanMap==null) {
			spanMap = new HashMap<>();
		}
		
		SpanSet spanSet = spanMap.get(documentId);
		if(spanSet==null) {
			spanSet = new SpanSet();
			spanMap.put(documentId, spanSet);
		}
		
		spanSet.setSpans(sentenceIndex, spans);
	}
	
	public void setSpanSet(String documentId, SpanSet spanSet) {
		if(spanMap==null) {
			spanMap = new HashMap<>();
		}
		
		if(spanMap.containsKey(documentId))
			throw new DuplicateIdentifierException("Span set already defined for document: "+documentId); //$NON-NLS-1$
		
		spanMap.put(documentId, spanSet);
	}
	
	public void setEdgeSet(String documentId, EdgeSet edgeSet) {
		if(edgeMap==null) {
			edgeMap = new HashMap<>();
		}
		
		if(edgeMap.containsKey(documentId))
			throw new DuplicateIdentifierException("Edge set already defined for document: "+documentId); //$NON-NLS-1$
		
		edgeMap.put(documentId, edgeSet);
	}
}
