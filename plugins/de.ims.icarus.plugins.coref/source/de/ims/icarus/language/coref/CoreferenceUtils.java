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

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.ToolTipManager;

import de.ims.icarus.Core;
import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.io.Reader;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentAnnotation;
import de.ims.icarus.plugins.coref.CorefConstants;
import de.ims.icarus.plugins.coref.io.CONLL12Utils;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.util.SharedPropertyRegistry;
import de.ims.icarus.search_tools.util.ValueHandler;
import de.ims.icarus.ui.TooltipFreezer;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.CompactProperties;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.HtmlUtils;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.classes.ClassUtils;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;
import de.ims.icarus.util.strings.StringUtil;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CoreferenceUtils implements CorefConstants {

	private CoreferenceUtils() {
		// no-op
	}

	private static Map<CorefErrorType, Color> errorColors = new HashMap<>();

	private static void loadErrorColors() {
		ConfigRegistry registry = ConfigRegistry.getGlobalRegistry();
		Handle root = registry.getHandle("plugins.coref.appearance.errorColors"); //$NON-NLS-1$

		for(CorefErrorType errorType : CorefErrorType.values()) {
			if(errorType==CorefErrorType.TRUE_POSITIVE_MENTION) {
				// No need to declare a color for true positive
				// since this color is subject to the presenting
				// module's own discretions
				continue;
			}

			Handle handle = registry.getChildHandle(root, errorType.getKey());

			errorColors.put(errorType, registry.getColor(handle));
		}
	}

	static {
		if(!Core.isDebugActive()) {
			ConfigRegistry.getGlobalRegistry().addGroupListener(
					"plugins.coref.appearance.errorColors", new ConfigListener() { //$NON-NLS-1$

				@Override
				public void invoke(ConfigRegistry sender, ConfigEvent event) {
					loadErrorColors();
				}
			});

			loadErrorColors();
		}
	}

	public static Color getErrorColor(CorefErrorType errorType) {
		if(errorType==null || errorType==CorefErrorType.TRUE_POSITIVE_MENTION)
			return null;

		return errorColors.get(errorType);
	}

	public static Color getClusterMarkupColor() {
		return ConfigRegistry.getGlobalRegistry().getColor(
				"plugins.coref.appearance.text.clusterMarkup"); //$NON-NLS-1$
	}

	public static CorefComparison compare(EdgeSet edgeSet, EdgeSet goldSet, boolean filterSingletons) {
		if(edgeSet==null)
			throw new NullPointerException("Invalid edge set"); //$NON-NLS-1$

		Map<Span, CorefErrorType> errors = new HashMap<>();

		Collection<Edge> tmp = edgeSet.getEdges();
		if(filterSingletons) {
			tmp = removeSingletons(tmp);
		}
		Set<Edge> edges = new LinkedHashSet<>(tmp);

		Set<Span> spanLut = collectSpans(edges);
		Map<Span, Span> rootLut = new HashMap<>();
		Map<Span, Span> headLut = new HashMap<>();

		// Cache predicted information
		for(Edge edge : edgeSet.getEdges()) {
			Span source = edge.getSource();
			Span target = edge.getTarget();

			if(source.isROOT()) {
				continue;
			}

			headLut.put(target, source);

			Span root = rootLut.get(source);
			if(root==null) {
				root = source;
			}
			rootLut.put(target, root);
		}


		if(goldSet==null) {
			CorefComparison result = new CorefComparison();

			result.setEdgeSet(edgeSet);
			result.setEdges(edges);
			result.setSpans(spanLut);
			result.setRootMap(rootLut);

			return result;
		}

		tmp = goldSet.getEdges();
		if(filterSingletons) {
			tmp = removeSingletons(tmp);
		}
		Set<Edge> goldEdges = new LinkedHashSet<>(tmp);

		Set<Span> goldLut = collectSpans(goldEdges);
		Map<Span, Span> goldRootLut = new LinkedHashMap<>();
		TObjectIntMap<Object> goldClusterIds = new TObjectIntHashMap<>(goldLut.size());

		// Cache gold information
		for(Edge edge : goldSet.getEdges()) {
			Span source = edge.getSource();
			Span target = edge.getTarget();

			goldClusterIds.put(target, target.getClusterId());

			if(source.isROOT()) {
				continue;
			}

			Span root = goldRootLut.get(source);
			if(root==null) {
				root = source;
			}
			goldRootLut.put(target, root);
		}

		// Now process predicted spans/edges and assign error types
		for(Span span : spanLut) {
			if(!goldLut.contains(span)) {
				// False positive mention (hallucinated)
				errors.put(span, CorefErrorType.FALSE_POSITIVE_MENTION);
				continue;
			}

			Span root = rootLut.get(span);
			Span goldRoot = goldRootLut.get(span);

			if(ClassUtils.equals(root, goldRoot)) {
				// Same cluster in both predicted and gold allocation
				// (either both starting a new cluster or both being
				// part of the same cluster, as defined by their
				// respective root mention)
				// -> true positive
				continue;
			}

			if(root==null) {
				// Mention starts a new cluster in predicted allocation
				// but not in the gold
				errors.put(span, CorefErrorType.INVALID_CLUSTER_START);
				continue;
			}
//			else if(goldRoot==null) {
//				// Mention is in wrong cluster (gold mention starts
//				// a new cluster)
//				errors.put(span, CorefErrorType.FOREIGN_CLUSTER_HEAD);
//				continue;
//			}

			Span head = headLut.get(span);

			if(!goldLut.contains(head)) {
				// Head span is unknown to the gold set
				errors.put(span, CorefErrorType.HALLUCINATED_HEAD);
				continue;
			}

			int goldSourceId = goldClusterIds.get(head);
			int goldTargetId = goldClusterIds.get(span);

			if(goldSourceId==goldTargetId) {
				// Both source and target are located in the same
				// cluster respectively
				// -> true positive
				continue;
			}

//			if(ClassUtils.equals(head, goldHead)) {
//				// Same head in both predicted and gold allocation
//				// -> true positive
//				continue;
//			}

			errors.put(span, CorefErrorType.FOREIGN_CLUSTER_HEAD);
		}

		// Now collect false negatives
		goldLut.removeAll(spanLut);
		for(Span span : goldLut) {
			errors.put(span, CorefErrorType.FALSE_NEGATIVE_MENTION);
		}

//		for(Iterator<Edge> it = goldEdges.iterator(); it.hasNext();) {
//			Edge edge = it.next();
//
//			if(!goldLut.contains(edge.getSource())
//					|| !goldLut.contains(edge.getTarget())) {
//				it.remove();
//			}
//		}
		goldEdges.removeAll(edges);

		CorefComparison result = new CorefComparison();
		result.setEdgeSet(edgeSet);
		result.setGoldSet(goldSet);
		result.setSpans(spanLut);
		result.setGoldSpans(goldLut);
		result.setErrors(errors);
		result.setEdges(edges);
		result.setGoldEdges(goldEdges);
		result.setRootMap(rootLut);
		result.setGoldRootMap(goldRootLut);

		return result;
	}

	public static Set<Span> collectSpans(EdgeSet edgeSet) {
		return collectSpans(edgeSet.getEdges());
	}

	public static Set<Span> collectSpans(Collection<Edge> edges) {
		Set<Span> result = new HashSet<>();

		for(Edge edge : edges) {
			result.add(edge.getTarget());
		}

		return result;
	}

	public static final CoreferenceData emptySentence = new DefaultCoreferenceData(null, new String[0]);

	public static ContentType getCoreferenceContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(CoreferenceData.class);
	}

	public static ContentType getCoreferenceDocumentContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(DocumentData.class);
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
		return ContentTypeRegistry.getInstance().getTypeForClass(DocumentSet.class);
	}

	public static Collection<Edge> removeSingletons(Collection<Edge> edges) {
		if(edges==null)
			throw new NullPointerException("Invalid edge collection"); //$NON-NLS-1$
		if(edges.isEmpty())
			return edges;

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

	public static int getSpanLength(Span span, DocumentData document) {
		if(span.isVirtual()) {
			return 0;
		}

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

	public static String getSpanText(Span span, DocumentData document) {
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
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$
		if(span==null)
			throw new NullPointerException("Invalid span"); //$NON-NLS-1$

		Span[] spans = data.getSpans();
		if(spans==null)
			return false;

		for(Span s : spans) {
			if(s.equals(span))
				return true;
		}

		return false;
	}

	public static boolean containsSpan(DocumentData data, Span span) {
		if(data==null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$
		if(span==null)
			throw new NullPointerException("Invalid span"); //$NON-NLS-1$

		int size = data.size();
		for(int i=0; i<size; i++) {
			if(containsSpan(data.get(i), span))
				return true;
		}

		return false;
	}

	public static boolean containsSpan(CoreferenceData data, Filter filter) {
		if(data==null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$
		if(filter==null)
			throw new NullPointerException("Invalid filter"); //$NON-NLS-1$

		Span[] spans = data.getSpans();
		if(spans==null)
			return false;

		for(Span span : spans) {
			if(filter.accepts(span))
				return true;
		}

		return false;
	}

	public static boolean containsSpan(DocumentData data, Filter filter) {
		if(data==null)
			throw new NullPointerException("Invalid data"); //$NON-NLS-1$
		if(filter==null)
			throw new NullPointerException("Invalid filter"); //$NON-NLS-1$

		int size = data.size();
		for(int i=0; i<size; i++) {
			if(containsSpan(data.get(i), filter))
				return true;
		}

		return false;
	}

	public static String getDocumentHeader(DocumentData data) {
		StringBuilder sb = new StringBuilder(50);
		sb.append(CONLL12Utils.BEGIN_DOCUMENT).append(" "); //$NON-NLS-1$

		String header = data.getId();
		if(header==null) {
			header = (String) data.getProperty(DocumentData.DOCUMENT_HEADER_PROPERTY);
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

	public static final SpanSet defaultEmptySpanSet = new SpanSet();
	public static final EdgeSet defaultEmptyEdgeSet = new EdgeSet();

	public static SpanSet getSpanSet(DocumentData document, CoreferenceAllocation allocation) {
		SpanSet spanSet = allocation==null ? null : allocation.getSpanSet(document.getId());
		if(spanSet==null) {
			spanSet = document.getSpanSet();
		}
		if(spanSet==null) {
			spanSet = document.getDefaultSpanSet();
		}
		return spanSet;
	}

	public static EdgeSet getEdgeSet(DocumentData document, CoreferenceAllocation allocation) {
		EdgeSet edgeSet = allocation==null ? null : allocation.getEdgeSet(document.getId());
		if(edgeSet==null) {
			edgeSet = document.getEdgeSet();
		}
		if(edgeSet==null) {
			edgeSet = document.getDefaultEdgeSet();
		}
		return edgeSet;
	}

	public static SpanSet getGoldSpanSet(DocumentData document, CoreferenceAllocation allocation) {
		SpanSet spanSet = allocation==null ? null : allocation.getSpanSet(document.getId());

//		if(spanSet==null) {
//			spanSet = document.getDefaultSpanSet();
//		}
		return spanSet;
	}

	public static EdgeSet getGoldEdgeSet(DocumentData document, CoreferenceAllocation allocation) {
		EdgeSet edgeSet = allocation==null ? null : allocation.getEdgeSet(document.getId());

//		if(edgeSet==null) {
//			edgeSet = document.getDefaultEdgeSet();
//		}
		return edgeSet;
	}

	public static DocumentSet loadDocumentSet(Reader<DocumentData> reader,
			Location location, Options options) throws IOException, UnsupportedLocationException,
				UnsupportedFormatException {

		DocumentSet documentSet = new DocumentSet();
		loadDocumentSet(reader, location, options, documentSet);

		return documentSet;
	}

	public static void loadDocumentSet(Reader<? extends DocumentData> reader,
			Location location, Options options, DocumentSet target) throws IOException, UnsupportedLocationException,
				UnsupportedFormatException {

		options.put("documentSet", target); //$NON-NLS-1$
		reader.init(location, options);

		DocumentData document;
		while((document=reader.next())!=null) {
			// Only add new document if the reader did not use
			// the DocumentSet.newDocument(String) method
			// to create a new document.
			if(target.size()==0 || target.get(target.size()-1)!=document) {
				target.add(document);
			}
		}
	}

//	public static Set<Span> collectSpans(EdgeSet edgeSet) {
//		Set<Span> spans = new HashSet<>();
//
//		for(int i=0; i<edgeSet.size(); i++) {
//			Edge edge = edgeSet.get(i);
//
//			spans.add(edge.getSource());
//			spans.add(edge.getTarget());
//		}
//
//		return spans;
//	}

	public static void appendProperties(StringBuilder buffer, String key, CoreferenceData sentence, Span span) {
		int beginIndex = span.getBeginIndex();
		int endIndex = span.getEndIndex();

		for(int i=beginIndex; i<=endIndex; i++) {
			buffer.append(sentence.getProperty(i, key));
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

	public static String getForms(CoreferenceData sentence, Span span) {
		StringBuilder buffer = new StringBuilder();
		int beginIndex = span.getBeginIndex();
		int endIndex = span.getEndIndex();

		for(int i=beginIndex; i<=endIndex; i++) {
			buffer.append(sentence.getForm(i));
			if(i<endIndex) {
				buffer.append(' ');
			}
		}

		return buffer.toString();
	}

	public static String createTooltip(CoreferenceData sentence, List<Span> spans, List<CorefErrorType> errorTypes) {
		StringBuilder sb = new StringBuilder();

		for(int i=0; i<spans.size(); i++) {
			if(i>0) {
				sb.append("\n\n"); //$NON-NLS-1$
			}

			Span span = spans.get(i);
			span.appendTo(sb);
			sb.append(':');
			CorefErrorType errorType = errorTypes.get(i);
			if(errorType!=null && errorType!=CorefErrorType.TRUE_POSITIVE_MENTION) {
				sb.append("  ("); //$NON-NLS-1$
				sb.append(errorType.getName());
				sb.append(')');
			}
			sb.append('\n');
			int len = 0;
			int idx = span.getBeginIndex();
			sb.append('"');
			while(len<40 && idx<=span.getEndIndex()) {
				String form = sentence.getForm(idx);
				sb.append(form).append(' ');

				len += form.length();
				idx++;
			}
			if(idx<span.getEndIndex()) {
				StringUtil.trim(sb);
				sb.append(' ').append(StringUtil.TEXT_WILDCARD).append(' ');
				sb.append(sentence.getForm(span.getEndIndex()));
			}
			sb.append('"');
		}

		return sb.toString();
	}

	private static void appendProperties(StringBuilder sb, CompactProperties properties) {
		if(properties==null || properties.size()==0)
			return;

		ResourceManager rm = ResourceManager.getInstance();
		sb.append('\n').append(rm.get("plugins.coref.labels.properties")).append(':'); //$NON-NLS-1$

		Map<String, Object> map = properties.asMap();
		List<String> keys = new ArrayList<>(map.keySet());
		Collections.sort(keys);

		for(String key : keys) {
			sb.append('\n').append(key).append(": ").append(map.get(key)); //$NON-NLS-1$
		}
	}

	public static String getSpanTooltip(Span span, CoreferenceData sentence, CorefErrorType errorType) {
		if(span==null)
			return null;

		if(span.isVirtual()) {
			return null;
		}

		StringBuilder sb = new StringBuilder();

		ResourceManager rm = ResourceManager.getInstance();
		sb.append(rm.get("plugins.coref.labels.span")).append('\n'); //$NON-NLS-1$
		span.appendTo(sb);
		if(errorType!=null && errorType!=CorefErrorType.TRUE_POSITIVE_MENTION) {
			sb.append('\n');
			sb.append(rm.get("plugins.coref.labels.errorType")).append(": "); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(errorType.getName());
		}

		if(sentence!=null) {
			sb.append('\n');

			sb.append('"');
			int len = 0;
			int idx = span.getBeginIndex();
			while(len<40 && idx<=span.getEndIndex()) {
				String form = sentence.getForm(idx);
				sb.append(form).append(' ');

				len += form.length();
				idx++;
			}
			if(idx<span.getEndIndex()) {
				StringUtil.trim(sb);
				sb.append(' ').append(StringUtil.TEXT_WILDCARD).append(' ');
				sb.append(sentence.getForm(span.getEndIndex()));
			}
			sb.append('"');
		}


		appendProperties(sb, span.getProperties());

		return sb.toString();
	}

	public static String getEdgeTooltip(Edge edge, CorefErrorType errorType) {
		if(edge==null)
			return null;

		StringBuilder sb = new StringBuilder();

		ResourceManager rm = ResourceManager.getInstance();
		sb.append(rm.get("plugins.coref.labels.edge")).append('\n'); //$NON-NLS-1$
		if(errorType!=null && errorType!=CorefErrorType.TRUE_POSITIVE_MENTION) {
			sb.append(rm.get("plugins.coref.labels.errorType")).append(": "); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append(errorType.getName());
			sb.append('\n');
		}
		sb.append(rm.get("plugins.coref.labels.source")).append(": "); //$NON-NLS-1$ //$NON-NLS-2$
		edge.getSource().appendTo(sb);
		sb.append('\n');
		sb.append(rm.get("plugins.coref.labels.target")).append(": "); //$NON-NLS-1$ //$NON-NLS-2$
		edge.getTarget().appendTo(sb);

		appendProperties(sb, edge.getProperties());

		return sb.toString();

	}

//	public static Span[] getFalsePositives(Span[] spans, Span[] goldSpans) {
//		if(goldSpans==null || spans==null) {
//			return null;
//		}
//
//		Set<Span> lookup = CollectionUtils.asSet(spans);
//		for(Span span : goldSpans) {
//			lookup.remove(span);
//		}
//
//		Span[] result = new Span[lookup.size()];
//		lookup.toArray(result);
//
//		return result;
//	}

//	public static Span[] getFalseNegatives(Span[] spans, Span[] goldSpans) {
//		if(goldSpans==null || spans==null) {
//			return null;
//		}
//
//		Set<Span> lookup = CollectionUtils.asSet(goldSpans);
//		for(Span span : spans) {
//			lookup.remove(span);
//		}
//
//		Span[] result = new Span[lookup.size()];
//		lookup.toArray(result);
//
//		return result;
//	}



	private static String createErrorInfoTooltip() {
		StringBuilder sb = new StringBuilder(300);
		ResourceManager rm = ResourceManager.getInstance();

		sb.append("<html>"); //$NON-NLS-1$
		sb.append("<h3>").append(rm.get("plugins.coref.errorTypes.title")).append("</h3>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sb.append("<table>"); //$NON-NLS-1$
		sb.append("<tr><th>") //$NON-NLS-1$
			.append(rm.get("plugins.coref.errorTypes.type")).append("</th><th>") //$NON-NLS-1$ //$NON-NLS-2$
			.append(rm.get("plugins.coref.errorTypes.description")).append("</th></tr>"); //$NON-NLS-1$ //$NON-NLS-2$

		for(CorefErrorType errorType : CorefErrorType.values()) {
			if(errorType==CorefErrorType.TRUE_POSITIVE_MENTION) {
				continue;
			}

			sb.append("<tr><td>") //$NON-NLS-1$
			.append("<font color=\"") //$NON-NLS-1$
			.append(HtmlUtils.hexString(getErrorColor(errorType)))
			.append("\">") //$NON-NLS-1$
			.append(errorType.getName()) //$NON-NLS-1
			.append("</font>") //$NON-NLS-1$
			.append("</td><td>").append(errorType.getDescription()).append("</td></tr>"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		sb.append("</table>"); //$NON-NLS-1$

		return sb.toString();
	}

	public static JLabel createErrorInfoLabel() {

		final JLabel label = new JLabel(){

			private static final long serialVersionUID = -6452835926413982692L;

			/**
			 * @see javax.swing.JComponent#getToolTipText()
			 */
			@Override
			public String getToolTipText() {
				return createErrorInfoTooltip();
			}
		};
		label.addMouseListener(new TooltipFreezer());
		label.setIcon(UIUtil.getInfoIcon());
		ToolTipManager.sharedInstance().registerComponent(label);
//
//		Localizable localizable = new Localizable() {
//
//			@Override
//			public void localize() {
//				label.setToolTipText(createErrorInfoTooltip());
//			}
//		};
//
//		localizable.localize();
//		ResourceManager.getInstance().getGlobalDomain().addItem(localizable);

		return label;
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

	static {

		// Word level
		ContentType contentType = LanguageUtils.getSentenceDataContentType();
		Object level = SharedPropertyRegistry.WORD_LEVEL;
		SharedPropertyRegistry.registerHandler(FORM_KEY, ValueHandler.stringHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(TAG_KEY, ValueHandler.stringHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(PARSE_KEY, ValueHandler.stringHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(LEMMA_KEY, ValueHandler.stringHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(SENSE_KEY, ValueHandler.stringHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(ENTITY_KEY, ValueHandler.stringHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(FRAMESET_KEY, ValueHandler.stringHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(SPEAKER_KEY, ValueHandler.stringHandler, contentType, level);

		// Mention level
		contentType = CoreferenceUtils.getCoreferenceDocumentContentType();
		level = SharedPropertyRegistry.SPAN_LEVEL;
		SharedPropertyRegistry.registerHandler(MENTION_HEAD_KEY, ValueHandler.integerHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(MENTION_SIZE_KEY, ValueHandler.integerHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(BEGIN_INDEX_KEY, ValueHandler.integerHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(END_INDEX_KEY, ValueHandler.integerHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(CLUSTER_ID_KEY, ValueHandler.integerHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(NUMBER_KEY, ValueHandler.stringHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(GENDER_KEY, ValueHandler.stringHandler, contentType, level);
		SharedPropertyRegistry.registerHandler(MENTION_TYPE, ValueHandler.stringHandler, contentType, level);

		// Edge level
		SharedPropertyRegistry.registerHandler(EDGE_TYPE, ValueHandler.stringHandler, contentType, "edge"); //$NON-NLS-1$
	}

	private static final String[] defaultSpanPropertyKeys = {
		MENTION_HEAD_KEY,
		MENTION_SIZE_KEY,
		BEGIN_INDEX_KEY,
		END_INDEX_KEY,
		CLUSTER_ID_KEY,
		SIZE_KEY,
		INDEX_KEY,
		ID_KEY,

		// Values generated by HOTCoref
		"Type", //$NON-NLS-1$
		"Gender", //$NON-NLS-1$
		"Number", //$NON-NLS-1$
		"HEAD", //$NON-NLS-1$
	};

	public static String[] getDefaultSpanPropertyKeys() {
		return defaultSpanPropertyKeys.clone();
	}

	private static final String[] defaultEdgePropertyKeys = {
		EDGE_TYPE,
	};

	public static String[] getDefaultEdgePropertyKeys() {
		return defaultEdgePropertyKeys.clone();
	}

	private static final String[] defaultWordPropertyKeys = {
		FORM_KEY,
		TAG_KEY,
		SIZE_KEY,
		INDEX_KEY,
		PARSE_KEY,
		LEMMA_KEY,
		SENSE_KEY,
		SPEAKER_KEY,
		ENTITY_KEY,
		FRAMESET_KEY,
	};

	public static String[] getDefaultWordPropertyKeys() {
		return defaultWordPropertyKeys.clone();
	}

	private static final String[] defaultSentencePropertyKeys = {
		SIZE_KEY,
		INDEX_KEY,
	};

	public static String[] getDefaultSentencePropertyKeys() {
		return defaultSentencePropertyKeys.clone();
	}

	private static final String[] defaultDocumentPropertyKeys = {
		SIZE_KEY,
		ID_KEY,
		INDEX_KEY,
	};

	public static String[] getDefaultDocumentPropertyKeys() {
		return defaultDocumentPropertyKeys.clone();
	}
}
