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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.language.coref.io;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.set.hash.THashSet;
import gnu.trove.strategy.IdentityHashingStrategy;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.coref.Cluster;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.DocumentData;
import de.ims.icarus.language.coref.DocumentSet;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.SpanSet;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.xml.ContentHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SlateAllocationReader2 implements AllocationReader {

	public static final String KEY_OVERRIDE_DOCUMENT_ID = "overrideDocumentId"; //$NON-NLS-1$
	public static final String KEY_SPAN_FILTER_KEY = "spanFilterKey"; //$NON-NLS-1$
	public static final String KEY_SPAN_FILTER_INCLUDE_PATTERN = "spanFilterIncludePattern"; //$NON-NLS-1$
	public static final String KEY_SPAN_FILTER_EXCLUDE_PATTERN = "spanFilterExcludePattern"; //$NON-NLS-1$
	public static final String KEY_EDGE_FILTER_KEY = "edgeFilterKey"; //$NON-NLS-1$
	public static final String KEY_EDGE_FILTER_INCLUDE_PATTERN = "edgeFilterIncludePattern"; //$NON-NLS-1$
	public static final String KEY_EDGE_FILTER_EXCLUDE_PATTERN = "edgeFilterExcludePattern"; //$NON-NLS-1$

	public static final String PROPERTY_SLATE_ID = "slate_id";
	public static final String PROPERTY_SLATE_ANNOTATOR = "slate_annotator";
	public static final String PROPERTY_SLATE_TAG = "slate_tag";

	private SlateXmlHandler slateXmlHandler;
	private Location location;
	private Options options;

	/**
	 * @see de.ims.icarus.language.coref.io.AllocationReader#init(de.ims.icarus.util.location.Location, de.ims.icarus.util.Options, de.ims.icarus.language.coref.DocumentSet)
	 */
	@Override
	public void init(Location location, Options options, DocumentSet documentSet)
			throws Exception {
		this.location = location;
		this.options = options;
		this.slateXmlHandler = new SlateXmlHandler(documentSet, options);
	}

	/**
	 * @see de.ims.icarus.language.coref.io.AllocationReader#readAllocation(de.ims.icarus.language.coref.CoreferenceAllocation)
	 */
	@Override
	public void readAllocation(CoreferenceAllocation target) throws Exception {

		slateXmlHandler.setAllocation(target);

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();

		//TODO do we need the try-with-resource statement?
		try(Reader reader = IOUtil.getReader(location.openInputStream(), IOUtil.getCharset(options))) {
			InputSource source = new InputSource(reader);

			parser.parse(source, slateXmlHandler);
		}
	}

	private static final Comparator<Span> COMP = new Comparator<Span>() {

		@Override
		public int compare(Span s1, Span s2) {
			if(s1.getSentenceIndex()!=s2.getSentenceIndex()) {
				return s1.getSentenceIndex()-s2.getSentenceIndex();
			}
			if(s1.getBeginIndex()!=s2.getBeginIndex()) {
				return s1.getBeginIndex()-s2.getBeginIndex();
			}

			return s1.getRange() - s2.getRange();
		}
	};

	public static class SlateXmlHandler extends ContentHandler {

		private final DocumentSet documentSet;
		private final Options options;
		private CoreferenceAllocation allocation;

		private final Matcher edgeIncludeFilter, edgeExcludeFilter;
		private final String edgeFilterKey;
		private final boolean filterEdges;

		private final Matcher spanIncludeFilter, spanExcludeFilter;
		private final String spanFilterKey;
		private final boolean filterSpans;

		public SlateXmlHandler(DocumentSet documentSet, Options options) {
			if (documentSet == null)
				throw new NullPointerException("Invalid documentSet"); //$NON-NLS-1$
			if (options == null)
				throw new NullPointerException("Invalid options"); //$NON-NLS-1$

			this.documentSet = documentSet;
			this.options = options;

			if((spanFilterKey = options.getString(KEY_SPAN_FILTER_KEY))!=null) {
				spanIncludeFilter = toMatcher(options.getString(KEY_SPAN_FILTER_INCLUDE_PATTERN));
				spanExcludeFilter = toMatcher(options.getString(KEY_SPAN_FILTER_EXCLUDE_PATTERN));

				filterSpans = spanFilterKey!=null && (spanIncludeFilter!=null || spanExcludeFilter!=null);
			} else {
				filterSpans = false;
				spanIncludeFilter = spanExcludeFilter = null;
			}

			if((edgeFilterKey = options.getString(KEY_EDGE_FILTER_KEY))!=null) {
				edgeIncludeFilter = toMatcher(options.getString(KEY_EDGE_FILTER_INCLUDE_PATTERN));
				edgeExcludeFilter = toMatcher(options.getString(KEY_EDGE_FILTER_EXCLUDE_PATTERN));

				filterEdges = edgeFilterKey!=null && (edgeIncludeFilter!=null || edgeExcludeFilter!=null);
			} else {
				filterEdges = false;
				edgeIncludeFilter = edgeExcludeFilter = null;
			}
		}

		private static Matcher toMatcher(String s) {
			if(s==null || s.isEmpty()) {
				return null;
			}

			return Pattern.compile(s).matcher(""); //$NON-NLS-1$
		}


		/**
		 * attributes: id, name
		 * <br>
		 * content: -
		 */
		public static final String TAG_DOCUMENT = "document"; //$NON-NLS-1$

		/**
		 * attributes: -
		 * <br>
		 * content: ignore
		 */
		public static final String TAG_TEXT = "text"; //$NON-NLS-1$

		/**
		 * attributes: -
		 * <br>
		 * content: attach as global property
		 */
		public static final String TAG_COMMENT = "comment"; //$NON-NLS-1$

		/**
		 * attributes: id, annotator, start, end, tag_name
		 * <br>
		 * content: -
		 */
		public static final String TAG_SEGMENT = "segment"; //$NON-NLS-1$

		/**
		 * attributes: -
		 * <br>
		 * content: use as hint for span coverage
		 */
		public static final String TAG_CONTENT = "content"; //$NON-NLS-1$

		/**
		 * attributes: key
		 * <br>
		 * content: -
		 */
		public static final String TAG_ATTRIBUTE = "attribute"; //$NON-NLS-1$

		/**
		 * attributes: -
		 * <br>
		 * content: use as mapped content for surrounding key-value pair
		 */
		public static final String TAG_VALUE = "value"; //$NON-NLS-1$

		/**
		 * attributes: id, annotator, tag_name
		 * <br>
		 * content: -
		 */
		public static final String TAG_LINK = "link"; //$NON-NLS-1$

		/**
		 * attributes: type, id
		 * <br>
		 * content: -
		 */
		public static final String TAG_SOURCE = "source"; //$NON-NLS-1$

		/**
		 * attributes: type, id
		 * <br>
		 * content: -
		 */
		public static final String TAG_DESTINATION = "destination"; //$NON-NLS-1$

		// Allocation
		private SpanSet spanSet = new SpanSet();
		private EdgeSet edgeSet = new EdgeSet();
		private int allocationId;
		private String documentId;

		// Spans & Edges
		private Span currentSpan;
		private Edge currentEdge;
		private String propertyKey;

		private TIntObjectMap<Span> spanLookup = new TIntObjectHashMap<>(130);
		private TIntObjectMap<Edge> edgeLookup = new TIntObjectHashMap<>(130);

		private List<Span> spans = new ArrayList<>(100);
		private List<Edge> edges = new ArrayList<>(100);

		private DocumentData document;

		private Set<Span> unresolvableSpans = new TCustomHashSet<>(IdentityHashingStrategy.INSTANCE);

		/**
		 * Maps character offsets to token anchors using the begin and end indices of the first
		 * and last characters in the token. So every token gets mapped twice.
		 */
		private TIntObjectMap<Anchor> beginAnchors = new TIntObjectHashMap<>(500);
		private TIntObjectMap<Anchor> endAnchors = new TIntObjectHashMap<>(500);

		protected CoreferenceAllocation getAllocation() {
			return allocation;
		}

		protected void setAllocation(CoreferenceAllocation allocation) {
			this.allocation = allocation;
		}

		private Span lookupSpan(int id) {
			return spanLookup.get(id);
		}

		private void registerSpan(int id, Span span) {
			if(spanLookup.containsKey(id))
				throw new IllegalArgumentException("Duplicate span/segment id: "+id); //$NON-NLS-1$

			spanLookup.put(id, span);
			spans.add(span);
		}

		private void registerEdge(int id, Edge edge) {
			if(edgeLookup.containsKey(id))
				throw new IllegalArgumentException("Duplicate edge/link id: "+id); //$NON-NLS-1$

			edgeLookup.put(id, edge);
			edges.add(edge);
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			clearText();

			// Per default we always want to collect character content
			setIgnoreCharacters(false);

			switch (qName) {
			case TAG_DOCUMENT: {
				if(documentId==null) {
					documentId = stringValue(attributes, "name"); //$NON-NLS-1$
				}
				allocationId = intValue(attributes, "id"); //$NON-NLS-1$
			} break;

//			case TAG_TEXT: {
//				// Ignore the excessive wall of text. We will use other means of mapping
//				setIgnoreCharacters(true);
//			} break;

			case TAG_SEGMENT: {
				int id = intValue(attributes, "id"); //$NON-NLS-1$

				currentSpan = new Span();
				currentSpan.setProperty(PROPERTY_SLATE_ID, Integer.valueOf(id));
				currentSpan.setProperty(PROPERTY_SLATE_ANNOTATOR, intValue(attributes, "annotator")); //$NON-NLS-1$
				currentSpan.setProperty(PROPERTY_SLATE_TAG, stringValue(attributes, "tag_name")); //$NON-NLS-1$

				// Temporary range assignment
				currentSpan.setBeginIndex(intValue(attributes, "start")); //$NON-NLS-1$
				currentSpan.setEndIndex(intValue(attributes, "end")); //$NON-NLS-1$
			} break;

			case TAG_ATTRIBUTE: {
				propertyKey = stringValue(attributes, "key"); //$NON-NLS-1$
			} break;

			case TAG_LINK: {
				int id = intValue(attributes, "id"); //$NON-NLS-1$

				currentEdge  = new Edge();
				currentEdge.setProperty(PROPERTY_SLATE_ID, Integer.valueOf(id));
				currentEdge.setProperty(PROPERTY_SLATE_ANNOTATOR, intValue(attributes, "annotator")); //$NON-NLS-1$
				currentEdge.setProperty(PROPERTY_SLATE_TAG, stringValue(attributes, "tag_name")); //$NON-NLS-1$
			} break;

			case TAG_SOURCE: {
				// Ignore the type attribute
				Span span = lookupSpan(intValue(attributes, "id")); //$NON-NLS-1$
				if(span!=null) {
					currentEdge.setTarget(span);
				}
			} break;

			case TAG_DESTINATION: {
				// Ignore the type attribute
				Span span = lookupSpan(intValue(attributes, "id")); //$NON-NLS-1$
				if(span!=null) {
					currentEdge.setSource(span);
				}
			} break;

			default:
				break;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			switch (qName) {

			case TAG_DOCUMENT: {
				resolve();
			} break;

			case TAG_TEXT: {
				createAnchors(getText());
			} break;

			case TAG_SEGMENT: {
				if(!isUndesiredSpan(currentSpan)) {
					int id = ((Integer)currentSpan.getProperty(PROPERTY_SLATE_ID)).intValue();
					registerSpan(id, currentSpan);
				}

				currentSpan = null;
			} break;

			case TAG_LINK: {

				if(currentEdge.getSource()!=null && currentEdge.getTarget()!=null) {
					int id = ((Integer)currentEdge.getProperty(PROPERTY_SLATE_ID)).intValue();
					registerEdge(id, currentEdge);
				}
				currentEdge = null;
			} break;

			case TAG_VALUE: {
				String value = getText();

				if(currentSpan!=null) {
					currentSpan.setProperty(propertyKey, value);
				} else if(currentEdge!=null) {
					currentEdge.setProperty(propertyKey, value);
				}
			} break;

			case TAG_ATTRIBUTE: {
				propertyKey = null;
			} break;

			case TAG_CONTENT: {
				currentSpan.setId(getText());
			} break;

			default:
				break;
			}
		}

		private void createAnchors(String text) {

			document = null;

			if(documentSet.size()==1) {
				document = documentSet.get(0);
			} else if(documentId!=null) {
				document = documentSet.getDocument(documentId);
			}

			if(document==null)
				throw new IllegalStateException("Unable to map slate allocation data to a valid document in corpus"); //$NON-NLS-1$

			if(options.getBoolean(KEY_OVERRIDE_DOCUMENT_ID, false)) {
				documentId = document.getId();
			}

			int cursor = 0;

			for(int sentenceIndex = 0; sentenceIndex<document.size(); sentenceIndex++) {
				SentenceData sentence = document.get(sentenceIndex);

				// Skip the speaker attribution on begin of sentences (kinda optional thing)
//				int sentenceBegin = StringUtil.indexOf(text, ':', cursor, Math.min(len, cursor+20));
//				if(sentenceBegin!=-1) {
//					cursor = sentenceBegin+1;
//				}
				//FIXME find better way of cleaning the speaker attributes, since not every sentence has it and we might delete real content

				for(int tokenIndex = 0; tokenIndex<sentence.length(); tokenIndex++) {
					String token = sentence.getForm(tokenIndex);

					int anchorBegin = text.indexOf(token, cursor);

					if(anchorBegin==-1)
						throw new IllegalStateException(String.format(
								"Unable to map token %d of sentence %d (\"%s\") to text anchors - offset in text: %d", //$NON-NLS-1$
								tokenIndex, sentenceIndex, token, cursor));

					Anchor anchor = new Anchor(sentenceIndex, tokenIndex);

					beginAnchors.put(anchorBegin, anchor);

					cursor = anchorBegin+token.length();

					endAnchors.put(cursor, anchor);
				}
			}
		}

		private void resolve() {

			allocation.setSpanSet(documentId, spanSet);
			allocation.setEdgeSet(documentId, edgeSet);
			allocation.setProperty("id", Integer.valueOf(allocationId)); //$NON-NLS-1$

			Collections.sort(spans, COMP);

			List<Span> spansInSentence = new ArrayList<>();
			int sentenceIndex = 0;

			for(Span span : spans) {
				int anchor0 = span.getBeginIndex();
				int anchor1 = span.getEndIndex();

				String content = span.getId();
				span.setId(null);

				Anchor beginAnchor = beginAnchors.get(anchor0);
				if(beginAnchor==null)
					throw new IllegalStateException(String.format(
							"Failed to resolve span \"%s\" (%d-%d) to existing tokens in document - misplaced begin anchor", //$NON-NLS-1$
							content, anchor0, anchor1));

				Anchor endAnchor = endAnchors.get(anchor1);
				if(endAnchor==null)
					throw new IllegalStateException(String.format(
							"Failed to resolve span \"%s\" (%d-%d) to existing tokens in document - misplaced end anchor", //$NON-NLS-1$
							content, anchor0, anchor1));

				//TODO alternative way of handling this kind of spans? (maybe silently ignore them?
				if(beginAnchor.sentenceIndex!=endAnchor.sentenceIndex) {
//					throw new IllegalStateException(String.format(
//							"Span is crossing sentence border [%d-%d]: %s", //$NON-NLS-1$
//							beginAnchor.sentenceIndex+1, endAnchor.sentenceIndex+1, content));

					LoggerFactory.warning(this, String.format("Span is crossing sentence border [%d-%d]: %s", //$NON-NLS-1$
							beginAnchor.sentenceIndex+1, endAnchor.sentenceIndex+1, content));

					unresolvableSpans.add(span);

					continue;
				}

				int newSentenceIndex = beginAnchor.sentenceIndex;

				if(newSentenceIndex!=sentenceIndex) {
					commitSpans(spansInSentence, sentenceIndex);
					sentenceIndex = newSentenceIndex;
				}

				span.setSentenceIndex(sentenceIndex);
				span.setBeginIndex(beginAnchor.tokenIndex);
				span.setEndIndex(endAnchor.tokenIndex);

				spansInSentence.add(span);
			}

			commitSpans(spansInSentence, sentenceIndex);

			// Assume every span is a potential root candidate until used as a target for linking

			Set<Span> roots = new TCustomHashSet<>(IdentityHashingStrategy.INSTANCE, spans);

			Set<Edge> undesiredEdges = new TCustomHashSet<>(IdentityHashingStrategy.INSTANCE);

			for(Edge edge : edges) {

				if(isUndesiredEdge(edge)) {
					undesiredEdges.add(edge);
				} else {
					// Remove edge target from root candidates
					roots.remove(edge.getTarget());
				}
			}

			System.out.printf("Reader: %d spans, %d edges, %d undesired edges, %d unresolvable spans, %d root candidates\n",
					spans.size(), edges.size(), undesiredEdges.size(), unresolvableSpans.size(), roots.size());

			// Link all unconnected nodes to root node
			roots.removeAll(unresolvableSpans);
			for(Span root : roots) {
				edges.add(new Edge(Span.getROOT(), root));
			}

			if(!edges.isEmpty()) {
				if(undesiredEdges.isEmpty()) {
					edgeSet.addEdges(edges);
				} else {
					for(Edge edge : edges) {
						if(undesiredEdges.contains(edge)) {
							continue;
						}

						edgeSet.addEdge(edge);
					}
				}

				//FIXME
//				allocateClusters(edgeSet);
			}
		}

		private boolean isUndesiredSpan(Span span) {

			if(filterSpans) {
				Object value = span.getProperty(spanFilterKey);
				if(value!=null) {
					String s = String.valueOf(value);
					if(spanIncludeFilter!=null) {
						spanIncludeFilter.reset(s);
						if(spanIncludeFilter.find()) {
							return false;
						}
					}
					if(spanExcludeFilter!=null) {
						spanExcludeFilter.reset(s);
						if(spanExcludeFilter.find()) {
							return true;
						}
					}
				}
			}
			return false;
		}

		private boolean isUndesiredEdge(Edge edge) {
			if(unresolvableSpans.contains(edge.getSource())
					|| unresolvableSpans.contains(edge.getTarget())) {
				return true;
			}

			if(filterEdges) {
				Object value = edge.getProperty(edgeFilterKey);
				if(value!=null) {
					String s = String.valueOf(value);
					if(edgeIncludeFilter!=null) {
						edgeIncludeFilter.reset(s);
						if(edgeIncludeFilter.find()) {
							return false;
						}
					}
					if(edgeExcludeFilter!=null) {
						edgeExcludeFilter.reset(s);
						if(edgeExcludeFilter.find()) {
							return true;
						}
					}
				}
			}
			return false;
		}

		/**
		 * Save spans in buffer to SpanSet, using provided sentenceIndex
		 */
		private void commitSpans(List<Span> spans, int sentenceIndex) {
			if(!spans.isEmpty() && sentenceIndex!=-1) {
//				System.out.println("Commiting "+spans.size()+" spans for sentence "+sentenceIndex);
				Span[] sa = new Span[spans.size()];
				spans.toArray(sa);
				spans.clear();
				spanSet.setSpans(sentenceIndex, sa);
			}
		}

		private void allocateClusters(EdgeSet edgeSet) {
			Queue<Edge> pending = new LinkedList<>(edgeSet.getEdges());
			TIntObjectMap<Cluster> clusterMap = new TIntObjectHashMap<>();
			Map<Span, Cluster> heads = new TCustomHashMap<>(IdentityHashingStrategy.INSTANCE);
			Set<Edge> postponed = new THashSet<>();
			while(!pending.isEmpty()) {
				Edge edge = pending.poll();
				if(edge.getSource().isROOT()) {
					int clusterId = clusterMap.size();
					Cluster cluster = new Cluster(clusterId, edge.getTarget());
					clusterMap.put(clusterId, cluster);
					edge.getTarget().setCluster(cluster);
					heads.put(edge.getTarget(), cluster);
				} else if(heads.containsKey(edge.getSource())) {
					Cluster cluster = heads.get(edge.getSource());
					cluster.addSpan(edge.getTarget(), edge);
					edge.getTarget().setCluster(cluster);
					heads.put(edge.getTarget(), cluster);
				} else if(!postponed.contains(edge)) {
					pending.offer(edge);
					postponed.add(edge);
				} else {
					throw new IllegalArgumentException(errMsg("Unable to to assign node to cluster " //$NON-NLS-1$
							+ "- source of edge has no connection to ROOT: "+edge+" ")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}

		private String errMsg(String s) {
			return "Error in document '"+documentId+"': "+s; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private static class Anchor {
		public final int sentenceIndex;
		public final int tokenIndex;

		public Anchor(int sentenceIndex, int tokenIndex) {
			this.sentenceIndex = sentenceIndex;
			this.tokenIndex = tokenIndex;
		}
	}
}
