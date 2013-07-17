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
