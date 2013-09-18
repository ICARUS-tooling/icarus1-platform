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

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.io.Reader;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentAnnotation;
import de.ims.icarus.plugins.coref.io.CONLL12Utils;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CoreferenceUtils {

	private CoreferenceUtils() {
		// no-op
	}
	
	public static final CoreferenceData emptySentence = new DefaultCoreferenceData(null, new String[0]);

	public static ContentType getCoreferenceContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(CoreferenceData.class);
	}

	public static ContentType getCoreferenceDocumentContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(CoreferenceDocumentData.class);
	}

	public static ContentType getCoreferenceDocumentAnnotationContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(CoreferenceDocumentAnnotation.class);
	}

	public static ContentType getEdgeSetContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(EdgeSet.class);
	}

	public static ContentType getSpanContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(Span.class);
	}

	public static ContentType getEdgeContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(Edge.class);
	}

	public static ContentType getCoreferenceDocumentSetContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(CoreferenceDocumentSet.class);
	}
	
	public static Collection<Edge> removeSingletons(Collection<Edge> edges) {
		if(edges==null)
			throw new IllegalArgumentException("Invalid edge collection"); //$NON-NLS-1$
		if(edges.isEmpty()) {
			return edges;
		}
			
		Set<Span> intermediates = new HashSet<>();
		for(Edge edge : edges) {
			if(!edge.getSource().isROOT()) {
				intermediates.add(edge.getSource());
			}
		}
		
		Collection<Edge> result = new LinkedHashSet<>();
		
		for(Edge edge : edges) {
			if(!edge.getSource().isROOT() || intermediates.contains(edge.getTarget())) {
				result.add(edge);
			}
		}
		
		return result;
	}
	
	public static int getSpanLength(Span span, CoreferenceDocumentData document) {
		CoreferenceData sentence = document.get(span.getSentenceIndex());
		int length = 0;
		for(int i=span.getBeginIndex(); i<=span.getEndIndex(); i++) {
			if(i>0) {
				// Whitespace!
				length++;
			}
			
			length += sentence.getForm(i).length();
		}
		
		return length;
	}
	
	public static String getSpanText(Span span, CoreferenceDocumentData document) {
		CoreferenceData sentence = document.get(span.getSentenceIndex());
		StringBuilder sb = new StringBuilder();
		for(int i=span.getBeginIndex(); i<=span.getEndIndex(); i++) {
			if(i>0) {
				sb.append(' ');
			}
			
			sb.append(sentence.getForm(i));
		}
		
		return sb.toString();
	}
	
	public static boolean containsSpan(CoreferenceData data, Span span) {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		if(span==null)
			throw new IllegalArgumentException("Invalid span"); //$NON-NLS-1$
		
		Span[] spans = data.getSpans();
		if(spans==null) {
			return false;
		}
		
		for(Span s : spans) {
			if(s.equals(span)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean containsSpan(CoreferenceDocumentData data, Span span) {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		if(span==null)
			throw new IllegalArgumentException("Invalid span"); //$NON-NLS-1$
		
		int size = data.size();
		for(int i=0; i<size; i++) {
			if(containsSpan(data.get(i), span)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean containsSpan(CoreferenceData data, Filter filter) {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		if(filter==null)
			throw new IllegalArgumentException("Invalid filter"); //$NON-NLS-1$
		
		Span[] spans = data.getSpans();
		if(spans==null) {
			return false;
		}
		
		for(Span span : spans) {
			if(filter.accepts(span)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean containsSpan(CoreferenceDocumentData data, Filter filter) {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		if(filter==null)
			throw new IllegalArgumentException("Invalid filter"); //$NON-NLS-1$
		
		int size = data.size();
		for(int i=0; i<size; i++) {
			if(containsSpan(data.get(i), filter)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static String getDocumentHeader(CoreferenceDocumentData data) {
		StringBuilder sb = new StringBuilder(50);
		sb.append(CONLL12Utils.BEGIN_DOCUMENT).append(" "); //$NON-NLS-1$
		
		String header = data.getId();
		if(header==null) {
			header = (String) data.getProperty(CoreferenceDocumentData.DOCUMENT_HEADER_PROPERTY);
		}
		if(header==null) {
			header = "<unnamed>"; //$NON-NLS-1$
		}
		sb.append(header);
		
		return sb.toString();
	}
	
	/**
	 * Creates and returns an {@code EdgeSet} by simply linking
	 * all spans in the given {@code SpanSet} based on their
	 * <i>cluster-id</i> in order of appearance.
	 */
	public static EdgeSet defaultBuildEdgeSet(SpanSet spanSet) {
		if(spanSet==null)
			throw new IllegalArgumentException("Invaldi span set"); //$NON-NLS-1$
		
		EdgeSet edgeSet = new EdgeSet();
		
		Map<Integer, Span> heads = new HashMap<>();
		
		for(Span span : spanSet.getSpans()) {
			int clusterId = span.getClusterId();
			if(clusterId==-1) {
				edgeSet.addEdge(new Edge(Span.getROOT(), span));
			} else {
				Span source = heads.get(clusterId);
				if(source==null) {
					source = Span.getROOT();
				}
				
				edgeSet.addEdge(new Edge(source, span));
				
				heads.put(clusterId, span);
			}
		}
		
		return edgeSet;
	}
	
	public static SpanSet getSpanSet(CoreferenceDocumentData document, CoreferenceAllocation allocation) {
		SpanSet spanSet = allocation==null ? null : allocation.getSpanSet(document.getId());
		if(spanSet==null) {
			spanSet = document.getSpanSet();
		}
		if(spanSet==null) {
			spanSet = document.getDefaultSpanSet();
		}
		return spanSet;
	}
	
	public static EdgeSet getEdgeSet(CoreferenceDocumentData document, CoreferenceAllocation allocation) {
		EdgeSet edgeSet = allocation==null ? null : allocation.getEdgeSet(document.getId());
		if(edgeSet==null) {
			edgeSet = document.getEdgeSet();
		}
		if(edgeSet==null) {
			edgeSet = document.getDefaultEdgeSet();
		}
		return edgeSet;
	}
	
	public static SpanSet getGoldSpanSet(CoreferenceDocumentData document, CoreferenceAllocation allocation) {
		SpanSet spanSet = allocation==null ? null : allocation.getSpanSet(document.getId());
		
		if(spanSet==null) {
			spanSet = document.getDefaultSpanSet();
		}
		return spanSet;
	}
	
	public static EdgeSet getGoldEdgeSet(CoreferenceDocumentData document, CoreferenceAllocation allocation) {
		EdgeSet edgeSet = allocation==null ? null : allocation.getEdgeSet(document.getId());
		
		if(edgeSet==null) {
			edgeSet = document.getDefaultEdgeSet();
		}
		return edgeSet;
	}
	
	public static CoreferenceDocumentSet loadDocumentSet(Reader<CoreferenceDocumentData> reader,
			Location location, Options options) throws IOException, UnsupportedLocationException, 
				UnsupportedFormatException {

		CoreferenceDocumentSet documentSet = new CoreferenceDocumentSet();
		loadDocumentSet(reader, location, options, documentSet);
		
		return documentSet;
	}
	
	public static void loadDocumentSet(Reader<CoreferenceDocumentData> reader,
			Location location, Options options, CoreferenceDocumentSet target) throws IOException, UnsupportedLocationException, 
				UnsupportedFormatException {

		options.put("documentSet", target); //$NON-NLS-1$
		reader.init(location, options);
		
		CoreferenceDocumentData document;
		while((document=reader.next())!=null) {
			// Only add new document if the reader did not use
			// the CoreferenceDocumentSet.newDocument(String) method
			// to create a new document.
			if(target.size()==0 || target.get(target.size()-1)!=document) {
				target.add(document);
			}
		}
	}
	
	public static Set<Span> collectSpans(EdgeSet edgeSet) {
		Set<Span> spans = new HashSet<>();
		
		for(int i=0; i<edgeSet.size(); i++) {
			Edge edge = edgeSet.get(i);
			
			spans.add(edge.getSource());
			spans.add(edge.getTarget());
		}
		
		return spans;
	}
	
	public static void appendProperties(StringBuilder buffer, String key, CoreferenceData sentence, Span span) {
		int beginIndex = span.getBeginIndex();
		int endIndex = span.getEndIndex();
		
		for(int i=beginIndex; i<=endIndex; i++) {
			buffer.append(sentence.getProperty(key+'_'+i));
			// TODO verify need of whitespace delimiter
			if(i<endIndex) {
				buffer.append(' ');
			}
		}
	}
	
	public static void appendForms(StringBuilder buffer, CoreferenceData sentence, Span span) {
		int beginIndex = span.getBeginIndex();
		int endIndex = span.getEndIndex();
		
		for(int i=beginIndex; i<=endIndex; i++) {
			buffer.append(sentence.getForm(i));
			if(i<endIndex) {
				buffer.append(' ');
			}
		}
	}
	
	public static final Comparator<Span> SPAN_SIZE_SORTER = new Comparator<Span>() {

		@Override
		public int compare(Span o1, Span o2) {
			return o1.getRange()-o2.getRange();
		}
		
	};
	
	public static final Comparator<Span> SPAN_SIZE_REVERSE_SORTER = new Comparator<Span>() {

		@Override
		public int compare(Span o1, Span o2) {
			return o2.getRange()-o1.getRange();
		}
		
	};
	
	private static final String[] defaultSpanPropertyKeys = {
		// TODO
	};
	
	public static String[] getDefaultSpanPropertyKeys() {
		return defaultSpanPropertyKeys.clone();
	}
	
	private static final String[] defaultEdgePropertyKeys = {
		// TODO
	};
	
	public static String[] getDefaultEdgePropertyKeys() {
		return defaultEdgePropertyKeys.clone();
	}
	
	private static final String[] defaultSentencePropertyKeys = {
		// TODO
	};
	
	public static String[] getDefaultSentencePropertyKeys() {
		return defaultSentencePropertyKeys.clone();
	}
	
	private static final String[] defaultHeadPropertyKeys = {
		// TODO
	};
	
	public static String[] getDefaultHeadPropertyKeys() {
		return defaultHeadPropertyKeys.clone();
	}
}
