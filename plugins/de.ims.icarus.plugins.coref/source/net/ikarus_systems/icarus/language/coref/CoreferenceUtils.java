/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.coref;

import java.util.Comparator;

import net.ikarus_systems.icarus.plugins.coref.io.CONLL12Utils;
import net.ikarus_systems.icarus.util.Filter;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CoreferenceUtils {

	private CoreferenceUtils() {
		// no-op
	}

	public static ContentType getCoreferenceContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(CoreferenceData.class);
	}

	public static ContentType getCoreferenceDocumentContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(CoreferenceDocumentData.class);
	}

	public static ContentType getCoreferenceDocumentSetContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(CoreferenceDocumentSet.class);
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
		
		String header = (String) data.getProperty(CoreferenceDocumentData.DOCUMENT_HEADER_PROPERTY);
		if(header==null) {
			header = "<unnamed>"; //$NON-NLS-1$
		}
		sb.append(header);
		
		return sb.toString();
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
