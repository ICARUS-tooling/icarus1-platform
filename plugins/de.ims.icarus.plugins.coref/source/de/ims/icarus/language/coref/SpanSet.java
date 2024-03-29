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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class SpanSet extends CorefListMember<Span> {

	@Link
	protected TIntObjectMap<Span[]> blockMap;
	@Link
	protected Map<String, Span> spanMap;

	public SpanSet() {
		// no-op
	}

	public void finish() {
		Collections.sort(items);
	}

	public Span[] getSpans(int sentenceIndex) {
		return blockMap==null ? null : blockMap.get(sentenceIndex);
	}

	private void addSpan(Span span) {
		if(spanMap==null) {
			spanMap = new LinkedHashMap<>();
		}
		if(items==null) {
			items = new ArrayList<>();
		}

		span.setIndex(items.size());
		items.add(span);
		spanMap.put(Span.asString(span), span);
	}

	public void setSpans(int sentenceIndex, Span[] spans) {
		if(blockMap==null) {
			blockMap = new TIntObjectHashMap<>();
		}

		// Ensure sorted spans array!
		Arrays.sort(spans);

		blockMap.put(sentenceIndex, spans);
		for(Span span : spans) {
			addSpan(span);
		}
	}

	public boolean contains(String id) {
		return spanMap!=null && spanMap.containsKey(id);
	}

	public void registerSpan(Span span) {
		addSpan(span);
	}

	public boolean contains(Span span) {
		return spanMap!=null && spanMap.containsKey(Span.asString(span));
	}

	public Span getSpan(String id) {
		if(id.equals(Span.ROOT_ID)) {
			return Span.getROOT();
		} else {
			return spanMap==null ? null : spanMap.get(id);
		}
	}

	public Collection<Span> getSpans() {
		Collection<Span> result = items;
		if(result==null) {
			result = Collections.emptyList();
		} else {
			result = CollectionUtils.getCollectionProxy(result);
		}
		return result;
	}

//	@Override
//	public SpanSet clone() {
//		SpanSet clone = new SpanSet();
//		clone.setProperties(cloneProperties());
//		if(blockMap!=null) {
//			clone.blockMap = new HashMap<>(blockMap);
//		}
//		if(spanMap!=null) {
//			clone.spanMap = new HashMap<>(spanMap);
//		}
//
//		return clone;
//	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getSpanContentType();
	}
}
