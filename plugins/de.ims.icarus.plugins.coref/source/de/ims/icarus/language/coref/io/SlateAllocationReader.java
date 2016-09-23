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
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.IdentityHashingStrategy;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.language.SentenceData;
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
import de.ims.icarus.util.strings.StringUtil;
import de.ims.icarus.xml.ContentHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SlateAllocationReader implements AllocationReader {

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

		protected final DocumentSet documentSet;
		protected final Options options;
		protected CoreferenceAllocation allocation;

		public SlateXmlHandler(DocumentSet documentSet, Options options) {
			if (documentSet == null)
				throw new NullPointerException("Invalid documentSet"); //$NON-NLS-1$
			if (options == null)
				throw new NullPointerException("Invalid options"); //$NON-NLS-1$

			this.documentSet = documentSet;
			this.options = options;
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

		private Set<Span> roots = new TCustomHashSet<>(IdentityHashingStrategy.INSTANCE);

		private String documentContent;

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

			// Assume every span is a potential root candidate until used as a target for linking
			roots.add(span);
		}

		private void registerEdge(int id, Edge edge) {
			if(edgeLookup.containsKey(id))
				throw new IllegalArgumentException("Duplicate edge/link id: "+id); //$NON-NLS-1$

			edgeLookup.put(id, edge);
			edges.add(edge);

			// Remove edge target from root candidates
			roots.remove(edge.getTarget());
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			clearText();

			// Per default we always want to collect character content
			setIgnoreCharacters(false);

			switch (qName) {
			case TAG_DOCUMENT: {
				documentId = stringValue(attributes, "name"); //$NON-NLS-1$
				allocationId = intValue(attributes, "id"); //$NON-NLS-1$
			} break;

//			case TAG_TEXT: {
//				// Ignore the excessive wall of text. We will use other means of mapping
//				setIgnoreCharacters(true);
//			} break;

			case TAG_SEGMENT: {
				int id = intValue(attributes, "id"); //$NON-NLS-1$

				currentSpan = new Span();
				currentSpan.setProperty("id", Integer.valueOf(id)); //$NON-NLS-1$
				currentSpan.setProperty("annotator", intValue(attributes, "annotator")); //$NON-NLS-1$ //$NON-NLS-2$
				currentSpan.setProperty("tag", stringValue(attributes, "tag_name")); //$NON-NLS-1$ //$NON-NLS-2$

				// Temporary range assignment
				currentSpan.setBeginIndex(intValue(attributes, "start")); //$NON-NLS-1$
				currentSpan.setEndIndex(intValue(attributes, "end")); //$NON-NLS-1$

				registerSpan(id, currentSpan);
			} break;

			case TAG_ATTRIBUTE: {
				propertyKey = stringValue(attributes, "key"); //$NON-NLS-1$
			} break;

			case TAG_LINK: {
				int id = intValue(attributes, "id"); //$NON-NLS-1$

				currentEdge  = new Edge();
				currentEdge.setProperty("id", Integer.valueOf(id)); //$NON-NLS-1$
				currentEdge.setProperty("annotator", intValue(attributes, "annotator")); //$NON-NLS-1$ //$NON-NLS-2$
				currentEdge.setProperty("tag", stringValue(attributes, "tag_name")); //$NON-NLS-1$ //$NON-NLS-2$

				registerEdge(id, currentEdge);
			} break;

			case TAG_SOURCE: {
				// Ignore the type attribute
				currentEdge.setSource(lookupSpan(intValue(attributes, "id"))); //$NON-NLS-1$
			} break;

			case TAG_DESTINATION: {
				// Ignore the type attribute
				currentEdge.setTarget(lookupSpan(intValue(attributes, "id"))); //$NON-NLS-1$
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
				documentContent = getText();
			} break;

			case TAG_SEGMENT: {
				currentSpan = null;
			} break;

			case TAG_LINK: {
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

		private DocumentData document;

		private List<Span> currentSentenceSpans = new ArrayList<>();
		private Set<Span> unresolvableSpans = new TCustomHashSet<>(IdentityHashingStrategy.INSTANCE);

		private void resolve() {

			document = null;

			if(documentSet.size()==1) {
				document = documentSet.get(0);
			} else if(documentId!=null) {
				document = documentSet.getDocument(documentId);
			}

			if(document==null)
				throw new IllegalStateException("Unable to map slate allocation data to a valid document in corpus"); //$NON-NLS-1$

			allocation.setSpanSet(documentId, spanSet);
			allocation.setEdgeSet(documentId, edgeSet);
			allocation.setProperty("id", Integer.valueOf(allocationId)); //$NON-NLS-1$

			Collections.sort(spans, COMP);
//			Collections.sort(edges);

//			System.out.println("spans:  "+spans); //$NON-NLS-1$

			MappingState state = new MappingState();

			int tokenIndex = 0;
			int sentenceIndex  = 0;

			for(Span span : spans) {
				state.reset(span);
				state.tokenBeginIndex = tokenIndex;
				state.sentenceBeginIndex = sentenceIndex;

				if(findBegin(state)) {
					tokenIndex = state.tokenBeginIndex;
					sentenceIndex = state.sentenceBeginIndex;

					if(findEnd(state)) {

						boolean valid = true;

						// We can only accept spans that reside within a single sentence
						if(state.sentenceBeginIndex!=state.sentenceEndIndex) {
							LoggerFactory.info(this, String.format("Span is crossing sentence border [%d-%d]: %s", //$NON-NLS-1$
									state.sentenceBeginIndex+1, state.sentenceEndIndex+1, state.content));
							valid = false;
						}

						if(valid) {

//							System.out.println("raw span data: "+span); //$NON-NLS-1$

							// Copy over the "real" span data
							span.setSentenceIndex(state.sentenceBeginIndex);
							span.setBeginIndex(state.tokenBeginIndex);
							span.setEndIndex(state.tokenEndIndex);

//							System.out.println("converted span data: "+span); //$NON-NLS-1$

							currentSentenceSpans.add(span);
						} else {
							unresolvableSpans.add(span);
						}
					}
				}
			}

			// Finally commit remaining spans in buffer
			commitSpans(state.sentenceBeginIndex);

			for(Span root : roots) {
				edges.add(new Edge(Span.getROOT(), root));
			}

			if(!edges.isEmpty()) {
				if(unresolvableSpans.isEmpty()) {
					edgeSet.addEdges(edges);
				} else {
					for(Edge edge : edges) {
						if(unresolvableSpans.contains(edge.getSource())
								|| unresolvableSpans.contains(edge.getTarget())) {
							continue;
						}

						edgeSet.addEdge(edge);
					}
				}
			}
		}

		private boolean findBegin(MappingState state) {
			return scan(state, true) > 0;
		}

		private boolean findEnd(MappingState state) {
			// Reset end index since we're going to move from the current one forward
			state.sentenceEndIndex = state.sentenceBeginIndex;
			return scan(state, false) > 0;
		}

		private int scan(MappingState state, boolean findBegin) {
			int len = state.content.length();
			int cursor = state.contentCursor;
			int tokenIndex = state.tokenBeginIndex;
			int sentenceIndex = state.sentenceBeginIndex;

			SentenceData sentence = null;
			String token = null;

			int matchedTokens = 0;

			// Special case for finding the end: we already matched a first token, so start from next one
			if(!findBegin) {
				matchedTokens++;
				tokenIndex++;
			}

			while(cursor<len) {
				if(sentence==null) {
					if(sentenceIndex<document.size()) {
						sentence = document.get(sentenceIndex);
					} else {
						break;
					}
				}

				if(token==null) {
					if(tokenIndex<sentence.length()) {
						// Within current sentence
						token = sentence.getForm(tokenIndex);
					} else {
						// Step forward to next sentence
						sentence = null;
						sentenceIndex++;
						tokenIndex = 0;

						//TODO needs review
						commitSpans(state.sentenceBeginIndex);
						continue;
					}
				}

				boolean tokenMatched = StringUtil.startsWith(state.content, token, cursor);

				if(tokenMatched) {
					matchedTokens++;
					cursor += token.length();

					// Ignore whitespaces within the span's content string
					while(cursor<len && Character.isWhitespace(state.content.charAt(cursor))) {
						cursor++;
					}

					state.contentCursor = cursor;

					if(findBegin) {
						state.tokenBeginIndex = tokenIndex;
					} else {
						state.tokenEndIndex = tokenIndex;
					}
				}

				if(findBegin && matchedTokens>0) {
					break;
				}

				if(tokenMatched || findBegin) {
					token = null;
					tokenIndex++;
					continue;
				} else {
					break;
				}
			}

			return matchedTokens;
		}

		/**
		 * Save spans in buffer to SpanSet, using provided sentenceIndex
		 */
		private void commitSpans(int sentenceIndex) {
			if(!currentSentenceSpans.isEmpty()) {
				Span[] spans = new Span[currentSentenceSpans.size()];
				currentSentenceSpans.toArray(spans);
				spanSet.setSpans(sentenceIndex, spans);
			}
		}
	}

	private static class MappingState {
		public String content;
		public int anchorBegin = -1;
		public int anchorEnd = -1;
		public int sentenceBeginIndex = -1;
		public int tokenBeginIndex = -1;
		public int sentenceEndIndex = -1;
		public int tokenEndIndex = -1;
		public int contentCursor = -1;
		public int documentCursor = 0;

		void reset(Span span) {
			content = span.getId();
			span.setId(null);

			anchorBegin = span.getBeginIndex();
			anchorEnd = span.getEndIndex();

			sentenceBeginIndex = -1;
			tokenBeginIndex = -1;
			sentenceEndIndex = -1;
			tokenEndIndex = -1;
			contentCursor = 0;
		}
	}
}
