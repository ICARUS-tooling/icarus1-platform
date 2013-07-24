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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SpanSet extends CorefListMember<Span> {
	
	protected Map<Integer, Span[]> blockMap;
	protected Map<String, Span> spanMap;

	public SpanSet() {
		// no-op
	}
	
	public Span[] getSpans(int sentenceIndex) {
		return blockMap==null ? null : blockMap.get(sentenceIndex);
	}
	
	public void setSpans(int sentenceIndex, Span[] spans) {
		if(blockMap==null) {
			blockMap = new HashMap<>();
		}
		if(spanMap==null) {
			spanMap = new LinkedHashMap<>();
		}
		if(items==null) {
			items = new ArrayList<>();
		}
		
		blockMap.put(sentenceIndex, spans);
		for(Span span : spans) {
			spanMap.put(Span.asString(span), span);
			items.add(span);
		}
	}
	
	public Span getSpan(String id) {
		if(Span.ROOT_ID.equals(id)) {
			return Span.getROOT();
		} else {
			return spanMap==null ? null : spanMap.get(id);
		}
	}
	
	public Collection<Span> getSpans() {
		Collection<Span> result = items;
		if(result==null) {
			result = Collections.emptyList();
		}
		return result;
	}
	
	@Override
	public SpanSet clone() {
		SpanSet clone = new SpanSet();
		clone.setProperties(cloneProperties());
		if(blockMap!=null) {
			clone.blockMap = new HashMap<>(blockMap);
		}
		if(spanMap!=null) {
			clone.spanMap = new HashMap<>(spanMap);
		}
		
		return clone;
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getSpanContentType();
	}
}
