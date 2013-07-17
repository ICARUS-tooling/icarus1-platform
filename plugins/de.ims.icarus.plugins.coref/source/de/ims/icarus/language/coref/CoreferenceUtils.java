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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.plugins.coref.io.CONLL12Utils;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;


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

	public static ContentType getEdgeSetContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(EdgeSet.class);
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
				edgeSet.addEdge(new Edge(Span.ROOT, span));
			} else {
				Span source = heads.get(clusterId);
				if(source==null) {
					source = Span.ROOT;
				}
				
				edgeSet.addEdge(new Edge(source, span));
				
				heads.put(clusterId, span);
			}
		}
		
		return edgeSet;
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
}
