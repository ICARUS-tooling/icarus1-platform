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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.Stack;

import javax.swing.Icon;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.SpanCache;
import de.ims.icarus.language.coref.SpanSet;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentAnnotationManager;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentHighlighting;
import de.ims.icarus.language.coref.helper.SpanBuffer;
import de.ims.icarus.plugins.coref.view.CoreferenceStyling;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.text.BatchDocument;
import de.ims.icarus.util.ClassUtils;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.StringUtil;
import de.ims.icarus.util.annotation.AnnotatedData;
import de.ims.icarus.util.annotation.Annotation;
import de.ims.icarus.util.annotation.AnnotationManager;
import de.ims.icarus.util.annotation.HighlightType;
import de.ims.icarus.util.cache.LRUCache;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocument extends BatchDocument {

	private static final long serialVersionUID = -5717201692774979302L;
	
	protected SpanBuffer spanBuffer;
	//protected SpanBuffer goldBuffer;
	protected Stack<ClusterAttributes> attributeStack;
	protected Stack<Color> highlightStack;
	protected Stack<Span> spanStack;
	protected StringBuilder builder;
	
	private boolean markSpans = true;
	private boolean showClusterId = true;
	private boolean showOffset = true;
	private boolean forceLinebreaks = true;
	private boolean showDocumentHeader = true;
	private boolean showSentenceIndex = true;

	private boolean filterSingletons = true;
	private boolean filterNonHighlighted = false;
	
	private HighlightType highlightType = HighlightType.BACKGROUND;
	
	private Filter filter;
	private Filter markupFilter;
	
	private AnnotationManager annotationManager;
	
	private SpanCache cache;
	
	private PropertyChangeSupport propertyChangeSupport;
	
	private DisplayMode displayMode = DisplayMode.DEFAULT;

	public static final String PARAM_SPAN = "span"; //$NON-NLS-1$

	public static final String PARAM_HIGHLIGHT_COLOR = "highlightColor"; //$NON-NLS-1$
	public static final String PARAM_FILL_COLOR = "fillColor"; //$NON-NLS-1$
	public static final String PARAM_UNDERLINE_COLOR = "underlineColor"; //$NON-NLS-1$

	public static final String PARAM_HIGHLIGHT_TYPE = "highlightType"; //$NON-NLS-1$
	public static final int HIGHLIGHT_TYPE_BEGIN = (1 << 0);
	public static final int HIGHLIGHT_TYPE_END = (1 << 1);
	
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
	
	public static boolean isHighlightBegin(int highlight) {
		return (highlight & HIGHLIGHT_TYPE_BEGIN)==HIGHLIGHT_TYPE_BEGIN;
	}

	public static boolean isHighlightEnd(int highlight) {
		return (highlight & HIGHLIGHT_TYPE_END)==HIGHLIGHT_TYPE_END;
	}
	
	public enum DisplayMode implements Identity {
		DEFAULT("default"), //$NON-NLS-1$
		GOLD("gold"), //$NON-NLS-1$
		FALSE_POSITIVES("falsePositives"), //$NON-NLS-1$
		FALSE_NEGATIVES("falseNegatives"), //$NON-NLS-1$
		;
		
		private final String key;
		
		private DisplayMode(String key) {
			this.key = key;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return getName();
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return ResourceManager.getInstance().get(
					"plugins.coref.documentDisplayType."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			return ResourceManager.getInstance().get(
					"plugins.coref.documentDisplayType."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return null;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return getClass();
		}
		
	}
	
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
		filterSingletons = source.filterSingletons;
		filterNonHighlighted = source.filterNonHighlighted;
		showSentenceIndex = source.showSentenceIndex;
		markupFilter = source.markupFilter;
		displayMode = source.displayMode;
	}
	
	protected PropertyChangeSupport getPropertyChangeSupport() {
		if(propertyChangeSupport==null) {
			propertyChangeSupport = new PropertyChangeSupport(this);
		}
		
		return propertyChangeSupport;
	}
	
	/**
	 * @param listener
	 * @see de.ims.icarus.util.PropertyChangeSource#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		getPropertyChangeSupport().addPropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see de.ims.icarus.util.PropertyChangeSource#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		getPropertyChangeSupport().addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * @param listener
	 * @see de.ims.icarus.util.PropertyChangeSource#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		getPropertyChangeSupport().removePropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see de.ims.icarus.util.PropertyChangeSource#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		getPropertyChangeSupport().removePropertyChangeListener(propertyName, listener);
	}

	// TODO bug: no space between last and next to last token in span when nested span is filtered out!
	@SuppressWarnings("incomplete-switch")
	public void appendBatchCoreferenceData(CoreferenceData data, int sentenceIndex, 
			Span[] spanSet, Span[] goldSet) {
		if(data==null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$
		if(data.length()==0) {
			return;
		}

		CoreferenceDocumentAnnotationManager annotationManager = 
				(CoreferenceDocumentAnnotationManager) getAnnotationManager();
		/*Annotation annotation = data instanceof AnnotatedData ? 
				((AnnotatedData)data).getAnnotation() : null; 
		if(annotationManager!=null) {
			annotationManager.setAnnotation(annotation);
		}*/
		
		Span[] spans = spanSet;
		
		switch (displayMode) {
		case GOLD:
			spans = goldSet;
			break;

		case FALSE_NEGATIVES:
			spans = CoreferenceUtils.getFalseNegatives(spanSet, goldSet);
			break;

		case FALSE_POSITIVES:
			spans = CoreferenceUtils.getFalsePositives(spanSet, goldSet);
			break;
		}
		
		if(spanBuffer==null) {
			spanBuffer = new SpanBuffer();
			attributeStack = new Stack<>();
			highlightStack = new Stack<>();
			spanStack = new Stack<>();
			builder = new StringBuilder();
		}
		
		if(spans==null) {
			spanBuffer.clear();
			// Ensure a non-null spans array!
			spans = new Span[0];
		} else {
			spanBuffer.rebuild(spans);
		}
		
		/*if(goldSpans==null) {
			goldBuffer.clear();
		} else {
			goldBuffer.rebuild(goldSpans);
		}*/
		
//		Color markupColor = CoreferenceUtils.getClusterMarkupColor();
		HighlightType highlightType = getHighlightType();
		Filter filter = getFilter();
		//boolean lastWasClosing = false;
		//boolean lastWasImportant = false;
		
		int length = data.length();
		
		if(!batch.isEmpty() && isForceLinebreaks()) {
			appendBatchLineFeed(null);
		}
		
		if(isShowSentenceIndex()) {
			String indexString = StringUtil.formatDecimal(sentenceIndex+1)+": "; //$NON-NLS-1$
			appendBatchString(indexString, null);
		}
		
		for(int index = 0; index<length; index++) {			
			//boolean important = isMarkSpans() && spanBuffer.isImportant(index);
			boolean isLast = index==length-1;
			String token = data.getForm(index);
			
			// Add space 
			//if(index>0 && (builder.length()>0 
			//		|| (lastWasImportant /*&& !important*/) // TODO 
			//		|| (lastWasClosing && spanBuffer.isStart(index)))) {
			//	builder.append(" "); //$NON-NLS-1$
			//}
			//lastWasClosing = false;
			
			// Border between two spans or regular text an the beginning of a new span
			if(builder.length()>0 && spanBuffer.isStartOrEnd(index)) {
				// Distinguish between text within a span (nested or not)
				// and plain text outside (needs to be wrapped in start- and end-tag).
				ClusterAttributes attributes = attributeStack.isEmpty() ? null : attributeStack.peek();
				AttributeSet attr = attributes==null ? null : attributes.getContentStyle();
				Span span = spanStack.isEmpty() ? null : spanStack.peek();
				Color col = highlightStack.isEmpty() ? null : highlightStack.peek();
				attr = createHighlightedAttr(attr, span, col, 0);
				appendBatchString(builder.toString(), attr);
				
				builder.setLength(0);
			}
			
			// Mark the start of a span with the cluster id and
			// the mentions begin index
			if(isMarkSpans() && spanBuffer.isStart(index)) {	
				for(int i=0; i<spanBuffer.getSpanCount(index); i++) {
					Span span = spanBuffer.getSpan(index, i);
					if(span==null || span.getBeginIndex()!=index) {
						continue;
					}
					if(filter!=null && !filter.accepts(span)) {
						continue;
					}
					
					int clusterIndex = getCache().getIndex(span);
					Color highlightColor = null;
					long highlight = annotationManager.getHighlight(clusterIndex);
					if(CoreferenceDocumentHighlighting.getInstance().isHighlighted(highlight)) {
						highlightColor =  CoreferenceDocumentHighlighting.getInstance().getGroupColor(highlight);
						if(highlightColor==null) {
							highlightColor = CoreferenceDocumentHighlighting.getInstance().getHighlightColor(highlight);
						}
					}
					
//					if(highlightColor==null && markupFilter!=null && markupFilter.accepts(span)) {
//						highlightColor = markupColor;
//					}
					
					if(filterNonHighlighted && highlightColor==null) {
						continue;
					}
					
					// Do not filter out highlighted singletons!
					if(highlightColor==null && filterSingletons && getCache().isSingleton(span)) {
						continue;
					}
										
					ClusterAttributes attributes = getClusterAttributes(span.getClusterId(), highlightType);
					AttributeSet attr;
					
					// Superscript cluster id
					if(isShowClusterId()) {
						attr = createHighlightedAttr(attributes.getSuperscriptStyle(), span, highlightColor, HIGHLIGHT_TYPE_BEGIN);
						appendBatchString(String.valueOf(span.getClusterId()), attr);
					}
					
					// Opening bracket
					attr = createHighlightedAttr(attributes.getFillerStyle(), span, highlightColor, 
							isShowClusterId() ? 0 : HIGHLIGHT_TYPE_BEGIN);
					appendBatchString("[", attr); //$NON-NLS-1$

					// Subscript begin index
					if(isShowOffset()) {
						attr = createHighlightedAttr(attributes.getSubscriptStyle(), span, highlightColor, 0);
						appendBatchString(String.valueOf(span.getBeginIndex()+1), attr);
					}
					
					attributeStack.push(attributes);
//					System.out.printf("attr::push %s %s\n",attributeStack, span);
					spanStack.push(span);
					if(highlightColor!=null) {
						highlightStack .push(highlightColor);
					}
				}
			}

			// Accumulate tokens till a significant highlight change
			// occurs so they can be stored in a single element.  
			builder.append(token);
			if(index<length-1) {
				builder.append(' ');
			}
			
			// Handle singletons and last tokens here since the regular string buffering
			// would place them after the surrounding brackets
			if(builder.length()>0 && (isLast || spanBuffer.isEnd(index))) {
				ClusterAttributes attributes = attributeStack.isEmpty() ? null : attributeStack.peek();
				AttributeSet attr = attributes==null ? null : attributes.getContentStyle();
				Span span = spanStack.isEmpty() ? null : spanStack.peek();
				Color col = highlightStack.isEmpty() ? null : highlightStack.peek();
				attr = createHighlightedAttr(attr, span, col, 0);
				appendBatchString(builder.toString(), attr);
				
				builder.setLength(0);
			}
			
			if(isMarkSpans() && spanBuffer.isEnd(index)) {
				for(int i=0; i<spanBuffer.getSpanCount(index); i++) {
					Span span = spanBuffer.getSpan(index, i);
					if(span==null || span.getEndIndex()!=index) {
						continue;
					}
					
					if(filter!=null && !filter.accepts(span)) {
						continue;
					}
					//lastWasClosing = true;
					
					int clusterIndex = getCache().getIndex(span);
					Color highlightColor = null;
					long highlight = annotationManager.getHighlight(clusterIndex);
					if(CoreferenceDocumentHighlighting.getInstance().isHighlighted(highlight)) {
						highlightColor =  highlightStack.pop();
					}
					
//					if(highlightColor==null && markupFilter!=null && markupFilter.accepts(span)) {
//						highlightColor = markupColor;
//					}

					if(filterNonHighlighted && highlightColor==null) {
						continue;
					}
					
					// Do not filter out highlighted singletons!
					if(highlightColor==null && filterSingletons && getCache().isSingleton(span)) {
						continue;
					}

					ClusterAttributes attributes = attributeStack.pop();
//					System.out.printf("attr::pop %s %s\n",attributeStack, span);
					span = spanStack.pop();
					AttributeSet attr;
					
					// Remove trailing whitespace characters
					StringUtil.trim(builder);

					// Subscript end index
					if(isShowOffset()) {
						attr = createHighlightedAttr(attributes.getSubscriptStyle(), span, highlightColor, 0);
						appendBatchString(String.valueOf(span.getEndIndex()+1), attr);
					}
					
					// Closing bracket
					attr = createHighlightedAttr(attributes.getFillerStyle(), span, highlightColor, 
							isShowClusterId() ? 0 : HIGHLIGHT_TYPE_END);
					appendBatchString("]", attr); //$NON-NLS-1$

					// Superscript cluster id
					if(isShowClusterId()) {
						attr = createHighlightedAttr(attributes.getSuperscriptStyle(), span, highlightColor, HIGHLIGHT_TYPE_END);
						appendBatchString(String.valueOf(span.getClusterId()), attr);
					}
					
					builder.append(' ');
				}
			}
			
			//lastWasImportant = important;
		}
	}
	
	public void appendBatchCoreferenceDocumentData(CoreferenceDocumentData data, 
			CoreferenceAllocation allocation, CoreferenceAllocation goldAllocation) {
		if(data==null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$
		
		if(!batch.isEmpty()) {
			appendBatchLineFeed(null);
		}
		
		if(isShowDocumentHeader()) {
			appendBatchDocumentHeader(data);
		}
		
		SpanSet spanSet = CoreferenceUtils.getSpanSet(data, allocation);
		SpanSet goldSet = CoreferenceUtils.getGoldSpanSet(data, goldAllocation);
		
		cacheSpans(spanSet, goldSet);

		CoreferenceDocumentAnnotationManager annotationManager = 
				(CoreferenceDocumentAnnotationManager) getAnnotationManager();
		Annotation annotation = data instanceof AnnotatedData ? 
				((AnnotatedData)data).getAnnotation() : null; 
		if(annotationManager!=null) {
			annotationManager.setAnnotation(annotation);
		}
		
		int size = data.size();
		for(int i=0; i<size; i++) {
			Span[] spans = spanSet.getSpans(i);
			Span[] goldSpans = goldSet==null ? null : goldSet.getSpans(i);
			appendBatchCoreferenceData(data.get(i), i, spans, goldSpans);
		}
	}
	
	public void appendBatchDocumentHeader(CoreferenceDocumentData data) {
		appendBatchLineFeed(null);
		appendBatchString(CoreferenceUtils.getDocumentHeader(data), HEADER);
		appendBatchLineFeed(null);
	}
	
	protected SpanCache getCache() {
		if(cache==null) {
			cache = new SpanCache();
		}
		return cache;
	}
	
	public void cacheSpans(SpanSet spans, SpanSet goldSpans) {
		getCache().clear();
		if(displayMode==CoreferenceDocument.DisplayMode.GOLD
				|| displayMode==CoreferenceDocument.DisplayMode.FALSE_NEGATIVES) {
			getCache().cacheSpans(goldSpans);
		} else {
			getCache().cacheSpans(spans);
		}
	}

	public AnnotationManager getAnnotationManager() {
		return annotationManager;
	}

	public void setAnnotationManager(AnnotationManager annotationManager) {
		this.annotationManager = annotationManager;
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
			throw new NullPointerException("Invalid highlight type"); //$NON-NLS-1$
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

	public boolean isFilterSingletons() {
		return filterSingletons;
	}

	public void setFilterSingletons(boolean filterSingletons) {
		this.filterSingletons = filterSingletons;
	}

	public boolean isFilterNonHighlighted() {
		return filterNonHighlighted;
	}

	public void setFilterNonHighlighted(boolean filterNonHighlighted) {
		this.filterNonHighlighted = filterNonHighlighted;
	}

	public boolean isShowSentenceIndex() {
		return showSentenceIndex;
	}

	public void setShowSentenceIndex(boolean showSentenceIndex) {
		this.showSentenceIndex = showSentenceIndex;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter filter) {
		if(ClassUtils.equals(this.filter, filter)) {
			return;
		}
		
		Filter oldValue = this.filter;
		this.filter = filter;
		
		getPropertyChangeSupport().firePropertyChange("filter", oldValue, filter); //$NON-NLS-1$
	}

	public Filter getMarkupFilter() {
		return markupFilter;
	}

	public void setMarkupFilter(Filter markupFilter) {
		if(ClassUtils.equals(this.markupFilter, markupFilter)) {
			return;
		}
		
		Filter oldValue = this.markupFilter;
		this.markupFilter = markupFilter;

		getPropertyChangeSupport().firePropertyChange("markupFilter", oldValue, markupFilter); //$NON-NLS-1$
	}
	
//	protected static MutableAttributeSet cloneAttributes(AttributeSet attr) {
//		return new SimpleAttributeSet(attr);
//	}
	
	/**
	 * @return the displayMode
	 */
	public DisplayMode getDisplayMode() {
		return displayMode;
	}

	/**
	 * @param displayMode the displayMode to set
	 */
	public void setDisplayMode(DisplayMode displayMode) {
		if(displayMode==null)
			throw new NullPointerException("Invalid display mode"); //$NON-NLS-1$
		if(this.displayMode==displayMode) {
			return;
		}
		
		DisplayMode oldValue = this.displayMode;
		this.displayMode = displayMode;
		
		getPropertyChangeSupport().firePropertyChange("displayMode", oldValue, displayMode); //$NON-NLS-1$
	}

	protected static MutableAttributeSet cloneAttributes(AttributeSet attr, Span span) {
		SimpleAttributeSet a = new SimpleAttributeSet(attr);
		
		if(span!=null) {
			a.addAttribute(PARAM_SPAN, span);
		}
		
		return a;
	}
	
	protected AttributeSet createHighlightedAttr(AttributeSet attr, Span span, Color col, int type) {
//		System.out.printf("attr=%s span=%s col=%s type=%d\n",attr,span,col,type);
		
		if(col==null && !highlightStack.isEmpty()) {
			col = highlightStack.peek();
			type = 0;
		}
		
		MutableAttributeSet a = null;
		
		if(col!=null || span!=null) {
			a = new SimpleAttributeSet(attr);
			
			if(span!=null) {
				a.addAttribute(PARAM_SPAN, span);
			}
		}
		
		if(a!=null && a!=attr) {
			Integer v = (Integer) attr.getAttribute(PARAM_HIGHLIGHT_TYPE);
			if(v==null) {
				v = 0;
			}
			v |= type;
			a.addAttribute(PARAM_HIGHLIGHT_TYPE, v);
		}
		
		return a;
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
		MutableAttributeSet superscript, subscript, content, filler;
		
		final int clusterId;
		final HighlightType highlightType;
		
		ClusterAttributes(ClusterAttributes source) {
			subscript = source.subscript;
			superscript = source.superscript;
			content = source.content;
			filler = source.filler;
			clusterId = source.clusterId;
			highlightType = source.highlightType;
		}
		
		ClusterAttributes(int clusterId, HighlightType highlightType) {
			this.clusterId = clusterId;
			this.highlightType = highlightType;
			
			if(highlightType==null) {
				superscript = subscript = content = filler = null;
				return;
			}
			
			Color col = CoreferenceStyling.getClusterColor(clusterId);
			
			// Superscript style
			superscript = new SimpleAttributeSet();
			//superscript.addAttribute(PARAM_CLUSTER_ID, clusterId);
			StyleConstants.setSuperscript(superscript, true);
			
			// Subscript style
			subscript = new SimpleAttributeSet();
			//subscript.addAttribute(PARAM_CLUSTER_ID, clusterId);
			StyleConstants.setSubscript(subscript, true);
			
			// Content style
			content = new SimpleAttributeSet();
			//content.addAttribute(PARAM_CLUSTER_ID, clusterId);
			
			// Filler style
			filler = new SimpleAttributeSet();
			//filler.addAttribute(PARAM_CLUSTER_ID, clusterId);
			
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
		
		@Override
		public ClusterAttributes clone() {
			return new ClusterAttributes(this);
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return (clusterId + 32) * highlightType.hashCode(); 
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ClusterAttributes) {
				ClusterAttributes other = (ClusterAttributes) obj;
				return clusterId==other.clusterId && highlightType==other.highlightType;
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "{CA:"+clusterId+":"+highlightType+"}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
}
