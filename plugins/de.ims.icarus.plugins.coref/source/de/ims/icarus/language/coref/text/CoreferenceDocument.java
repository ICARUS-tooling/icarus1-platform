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
package de.ims.icarus.language.coref.text;

import java.awt.Color;
import java.util.Map;
import java.util.Stack;

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.helper.SpanBuffer;
import de.ims.icarus.plugins.coref.view.CoreferenceStyling;
import de.ims.icarus.ui.text.BatchDocument;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.annotation.HighlightType;
import de.ims.icarus.util.cache.LRUCache;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocument extends BatchDocument {

	private static final long serialVersionUID = -5717201692774979302L;
	
	protected SpanBuffer spanBuffer;
	protected Stack<ClusterAttributes> attributeStack;
	protected StringBuilder builder;
	
	private boolean markSpans = true;
	private boolean showClusterId = true;
	private boolean showOffset = true;
	private boolean forceLinebreaks = false;
	private boolean showDocumentHeader = true;
	
	private HighlightType highlightType = HighlightType.BACKGROUND;
	
	private Filter filter;

	public static final String PARAM_CLUSTER_ID = "clusterId"; //$NON-NLS-1$

	public static final String PARAM_FILL_COLOR = "highlightColor"; //$NON-NLS-1$
	public static final String PARAM_UNDERLINE_COLOR = "highlightType"; //$NON-NLS-1$
	
	// BEGIN unused
	public static final MutableAttributeSet CONTENT = new SimpleAttributeSet();
	public static final MutableAttributeSet SUBSCRIPT = new SimpleAttributeSet();
	public static final MutableAttributeSet SUPERSCRIPT = new SimpleAttributeSet();
	public static final MutableAttributeSet HEADER = new SimpleAttributeSet();
	
	static {
		StyleConstants.setSuperscript(SUPERSCRIPT, true);
		StyleConstants.setSubscript(SUBSCRIPT, true);
		
		StyleConstants.setBold(HEADER, true);
		StyleConstants.setFontSize(HEADER, 14);
		StyleConstants.setSpaceAbove(HEADER, 5);
		StyleConstants.setSpaceBelow(HEADER, 3);
		
		CONTENT.addAttribute(ElementNameAttribute, ContentElementName);
	}
	// END unused
	
	public CoreferenceDocument() {
		// no-op
	}
	
	public void copySettings(CoreferenceDocument source) {
		markSpans = source.markSpans;
		showClusterId = source.showClusterId;
		showDocumentHeader = source.showDocumentHeader;
		showOffset = source.showOffset;
		forceLinebreaks = source.forceLinebreaks;
		filter = source.filter;
		highlightType = source.highlightType;
	}
	
	// TODO bug: no space between last and next to last token in span when nested span is filtered out!
	public void appendBatchCoreferenceData(CoreferenceData data) {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		if(data.length()==0) {
			return;
		}
		
		if(spanBuffer==null) {
			spanBuffer = new SpanBuffer();
			attributeStack = new Stack<>();
			builder = new StringBuilder();
		}
		
		spanBuffer.rebuild(data);
		
		HighlightType highlightType = getHighlightType();
		Filter filter = getFilter();
		boolean lastWasClosing = false;
		boolean lastWasImportant = false;
		
		int length = data.length();
		
		if(!batch.isEmpty() && isForceLinebreaks()) {
			appendBatchLineFeed(null);
		}
		
		
		for(int index = 0; index <length; index++) {
			
			boolean important = isMarkSpans() && spanBuffer.isImportant(index);
			boolean isLast = index==length-1;
			String token = data.getForm(index);
			
			// Add space 
			if(index>0 && (builder.length()>0 
					|| (lastWasImportant && !important) 
					|| (lastWasClosing && spanBuffer.isStart(index)))) {
				builder.append(" "); //$NON-NLS-1$
			}
			lastWasClosing = false;
			
			//
			if(builder.length()>0 && spanBuffer.isStartOrEnd(index)) {
				// Distinguish between text within a span (nested or not)
				// and plain text outside (needs to be wrapped in start- and end-tag).
				ClusterAttributes attributes = attributeStack.isEmpty() ? null : attributeStack.peek();
				AttributeSet attr = attributes==null ? null : attributes.getContentStyle();
				
				appendBatchString(builder.toString(), attr);
				
				builder.setLength(0);
			}
			
			// Mark the start of a span with the cluster id and
			// the mentions begin index
			if(isMarkSpans() && spanBuffer.isStart(index)) {	
				for(int i=0; i<spanBuffer.getSpanCount(index); i++) {
					Span span = spanBuffer.getSpan(index, i);
					if(span.getBeginIndex()!=index) {
						continue;
					}
					if(filter!=null && !filter.accepts(span)) {
						continue;
					}
					
					ClusterAttributes attributes = getClusterAttributes(span.getClusterId(), highlightType);

					// Superscript cluster id
					if(isShowClusterId()) {
						appendBatchString(String.valueOf(span.getClusterId()), attributes.getSuperscriptStyle());
					}
					
					// Opening bracket
					appendBatchString("[", attributes.getFillerStyle()); //$NON-NLS-1$

					// Subscript begin index
					if(isShowOffset()) {
						appendBatchString(String.valueOf(span.getBeginIndex()+1), attributes.getSubscriptStyle());
					}
					
					attributeStack.push(attributes);
				}
			}

			// Accumulate tokens till a significant highlight change
			// occurs so they can be stored in a single element.  
			builder.append(token);
			
			// Handle singletons and last tokens here since the regular string buffering
			// would place them after the surrounding brackets
			if(builder.length()>0 && (isLast || spanBuffer.isEnd(index))) {
				ClusterAttributes attributes = attributeStack.isEmpty() ? null : attributeStack.peek();
				AttributeSet attr = attributes==null ? null : attributes.getContentStyle();
				appendBatchString(builder.toString(), attr);
				
				builder.setLength(0);
			}
			
			if(isMarkSpans() && spanBuffer.isEnd(index)) {
				for(int i=spanBuffer.getSpanCount(index)-1; i>-1; i--) {
					Span span = spanBuffer.getSpan(index, i);
					if(span.getEndIndex()!=index) {
						continue;
					}
					if(filter!=null && !filter.accepts(span)) {
						continue;
					}
					lastWasClosing = true;
					
					ClusterAttributes attributes = attributeStack.pop();

					// Subscript begin index
					if(isShowOffset()) {
						appendBatchString(String.valueOf(span.getBeginIndex()+1), attributes.getSubscriptStyle());
					}
					
					// Closing bracket
					appendBatchString("]", attributes.getFillerStyle()); //$NON-NLS-1$

					// Superscript cluster id
					if(isShowClusterId()) {
						appendBatchString(String.valueOf(span.getClusterId()), attributes.getSuperscriptStyle());
					}
				}
			}
			
			lastWasImportant = important;
		}
	}
	
	public void appendBatchCoreferenceDocumentData(CoreferenceDocumentData data) {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		
		if(!batch.isEmpty()) {
			appendBatchLineFeed(null);
		}
		
		if(isShowDocumentHeader()) {
			appendBatchDocumentHeader(data);
		}
		
		int size = data.size();
		for(int i=0; i<size; i++) {
			appendBatchCoreferenceData(data.get(i));
		}
	}
	
	public void appendBatchDocumentHeader(CoreferenceDocumentData data) {
		appendBatchLineFeed(null);
		appendBatchString(CoreferenceUtils.getDocumentHeader(data), HEADER);
		appendBatchLineFeed(null);
	}

	public boolean isMarkSpans() {
		return markSpans;
	}

	public boolean isShowClusterId() {
		return showClusterId;
	}

	public boolean isShowOffset() {
		return showOffset;
	}

	public HighlightType getHighlightType() {
		return highlightType;
	}

	public void setMarkSpans(boolean markSpans) {
		this.markSpans = markSpans;
	}

	public void setShowClusterId(boolean showClusterIds) {
		this.showClusterId = showClusterIds;
	}

	public void setShowOffset(boolean showOffset) {
		this.showOffset = showOffset;
	}

	public void setHighlightType(HighlightType highlightType) {
		if(highlightType==null) {
			throw new IllegalArgumentException("Invalid highlight type"); //$NON-NLS-1$
		}
		
		this.highlightType = highlightType;
	}
	
	public boolean isForceLinebreaks() {
		return forceLinebreaks;
	}

	public boolean isShowDocumentHeader() {
		return showDocumentHeader;
	}

	public void setForceLinebreaks(boolean floatingText) {
		this.forceLinebreaks = floatingText;
	}

	public void setShowDocumentHeader(boolean showDocumentHeader) {
		this.showDocumentHeader = showDocumentHeader;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		if(this.filter!=null && this.filter.equals(filter)) {
			return;
		}
		
		this.filter = filter;
	}

	private static Map<String, ClusterAttributes> attributeCache = new LRUCache<>(100);
	
	protected static ClusterAttributes getClusterAttributes(int clusterId, HighlightType highlightType) {
		String key = String.valueOf(clusterId)+highlightType.toString();
		
		ClusterAttributes attr = attributeCache.get(key);
		
		if(attr==null) {
			attr = new ClusterAttributes(clusterId, highlightType);
			attributeCache.put(key, attr);
		}
		
		return attr;
	}
	
	protected static final ClusterAttributes emptyAttributes = new ClusterAttributes(-1, null);
	
	protected static class ClusterAttributes {
		final MutableAttributeSet superscript, subscript, content, filler;
		
		ClusterAttributes(int clusterId, HighlightType highlightType) {
			if(highlightType==null) {
				superscript = subscript = content = filler = null;
				return;
			}
			
			Color col = CoreferenceStyling.getClusterColor(clusterId);
			
			// Superscript style
			superscript = new SimpleAttributeSet();
			superscript.addAttribute(PARAM_CLUSTER_ID, clusterId);
			StyleConstants.setSuperscript(superscript, true);
			
			// Subscript style
			subscript = new SimpleAttributeSet();
			subscript.addAttribute(PARAM_CLUSTER_ID, clusterId);
			StyleConstants.setSubscript(subscript, true);
			
			// Content style
			content = new SimpleAttributeSet();
			content.addAttribute(PARAM_CLUSTER_ID, clusterId);
			
			// Filler style
			filler = new SimpleAttributeSet();
			filler.addAttribute(PARAM_CLUSTER_ID, clusterId);
			
			switch (highlightType) {
			case FOREGROUND:
				StyleConstants.setForeground(superscript, col);
				StyleConstants.setForeground(subscript, col);
				StyleConstants.setForeground(content, col);
				StyleConstants.setForeground(filler, col);
				break;

			case BACKGROUND:
				superscript.addAttribute(PARAM_FILL_COLOR, col);
				subscript.addAttribute(PARAM_FILL_COLOR, col);
				content.addAttribute(PARAM_FILL_COLOR, col);
				filler.addAttribute(PARAM_FILL_COLOR, col);
				break;
				
			case BOLD:
				StyleConstants.setBold(content, true);
				break;
				
			case UNDERLINED:
				content.addAttribute(PARAM_UNDERLINE_COLOR, col);
				break;
				
			case ITALIC:
				StyleConstants.setItalic(content, true);
				break;
				
			default:
				throw new IllegalArgumentException("Highlight type not supported: "+highlightType); //$NON-NLS-1$
			}
		}
		
		public AttributeSet getSuperscriptStyle() {
			return superscript;
		}
		
		public AttributeSet getSubscriptStyle() {
			return subscript;
		}
		
		public AttributeSet getContentStyle() {
			return content;
		}
		
		public AttributeSet getFillerStyle() {
			return filler;
		}
	}
	
	// TODO obsolete?
	protected class BatchDispatcher implements Runnable {
		
		protected int sizeReminder;
		protected int batchSize;
		protected int batchIndex;
		
		public BatchDispatcher() {
			sizeReminder = batch.size();
			
			batchSize = Math.max(100, sizeReminder/100);
		}
		

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}
}
