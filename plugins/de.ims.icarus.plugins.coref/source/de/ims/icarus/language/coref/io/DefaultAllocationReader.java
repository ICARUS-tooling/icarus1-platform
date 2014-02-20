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
package de.ims.icarus.language.coref.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.language.coref.Cluster;
import de.ims.icarus.language.coref.CorefMember;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.SpanSet;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.location.Location;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultAllocationReader implements AllocationReader {

	public static final String BEGIN_DOCUMENT = "#begin document"; //$NON-NLS-1$
	public static final String END_DOCUMENT = "#end document"; //$NON-NLS-1$

	public static final String BEGIN_NODES = "#begin nodes"; //$NON-NLS-1$
	public static final String END_NODES = "#end nodes"; //$NON-NLS-1$

	public static final String BEGIN_EDGES = "#begin edges"; //$NON-NLS-1$
	public static final String END_EDGES = "#end edges"; //$NON-NLS-1$

	public static final String COMMENT_PREFIX = "#"; //$NON-NLS-1$

	private BufferedReader reader;
	private CoreferenceDocumentSet documentSet;
	private int lineCount;

	private String documentId;

	public DefaultAllocationReader() {
		// no-op
	}

	@Override
	public void init(Location location,
			Options options, CoreferenceDocumentSet documentSet) throws Exception {

		reader = IOUtil.getReader(location.openInputStream(), IOUtil.getCharset(options));
		lineCount = 0;
		this.documentSet = documentSet;
	}

	private String readLine() throws IOException {
		String line = reader.readLine();
		if(line!=null) {
			lineCount++;
		}
		return line;
	}

	@Override
	public void readAllocation(CoreferenceAllocation allocation) throws Exception {
		Set<String> ids = new HashSet<>();
		boolean checkIds = false;
		if(documentSet!=null) {
			ids.addAll(documentSet.getDocumentIds());
			checkIds = true;
		}

		main : while(true) {

			if(Thread.currentThread().isInterrupted())
				throw new InterruptedException();

			String line = skipEmptyLines();
			if(line==null) {
				break main;
			}

			if(!line.startsWith(BEGIN_DOCUMENT))
				throw new NullPointerException("Invalid '"+BEGIN_DOCUMENT+"' declaration: "+line); //$NON-NLS-1$ //$NON-NLS-2$

			int startLine = lineCount;
			documentId = line.substring(BEGIN_DOCUMENT.length()).trim();

			if(checkIds && !ids.remove(documentId))
				throw new IllegalArgumentException(String.format(
						"Unknown document id '%s' at line %s ", lineCount, documentId)); //$NON-NLS-1$

			CoreferenceDocumentData document = documentSet==null ?
					null : documentSet.getDocument(documentId);

			// Read in properties
			while((line=readLine())!=null) {
				if(BEGIN_NODES.equals(line)) {
					break;
				} else if(line.startsWith(COMMENT_PREFIX)) {
					readProperty(line, allocation);
				} else
					throw new IllegalArgumentException(errMsg(String.format(
							"Invalid property statement '%s' at line %d", line, lineCount))); //$NON-NLS-1$
			}

			// Read nodes
			SpanSet spanSet = readNodes(document);

			// Read edges
			EdgeSet edgeSet = readEdges(spanSet);

			allocation.setSpanSet(documentId, spanSet);
			allocation.setEdgeSet(documentId, edgeSet);

			// Check for closing declaration
			if(!END_DOCUMENT.equals(skipEmptyLines()))
				throw new IllegalArgumentException(errMsg(String.format(
						"Missing '%s' statement to close '%s' at line %d", //$NON-NLS-1$
						END_DOCUMENT, BEGIN_DOCUMENT, startLine)));
		}

		if(checkIds && !ids.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Missing allocations for documents:\n"); //$NON-NLS-1$
			sb.append(CollectionUtils.toString(ids));

			throw new IllegalArgumentException(sb.toString());
		}
	}

	private static void readProperty(String s, CorefMember member) {
		int sep = s.indexOf(' ');
		member.setProperty(s.substring(1, sep), s.substring(sep+1));
	}

	private String skipEmptyLines() throws IOException {
		String line = null;

		while((line = readLine())!=null) {
			if(!line.isEmpty()) {
				break;
			}
		}

		return line;
	}

	private SpanSet readNodes(CoreferenceDocumentData document) throws IOException {
		String line = null;
		boolean closed = false;

		SpanSet spanSet = new SpanSet();
		List<Span> buffer = new ArrayList<>();
		int sentenceId = -1;
		int beginLine = lineCount;

		while((line = readLine()) != null) {
			if(END_NODES.equals(line)) {
				closed = true;
				break;
			} else if(BEGIN_NODES.equals(line)) {
				beginLine = lineCount;
			} else if(line.startsWith(COMMENT_PREFIX)) {
				readProperty(line, spanSet);
			} else {

				Span span = Span.parse(line);
				if(span.isROOT()) {
					continue;
				}

				if(spanSet.contains(span))
					throw new IllegalArgumentException(
							errMsg("Duplicate span declaration: "+span)); //$NON-NLS-1$

				if(span.getSentenceIndex()!=sentenceId && !buffer.isEmpty()) {
					Span[] spans = new Span[buffer.size()];
					buffer.toArray(spans);
					spanSet.setSpans(sentenceId, spans);
					buffer.clear();
				}

				buffer.add(span);
				sentenceId = span.getSentenceIndex();

				if(document!=null) {
					CoreferenceData sentence = document.get(sentenceId);
					if(span.getBeginIndex()<0 || span.getEndIndex()>=sentence.length())
						throw new IllegalArgumentException(errMsg("Span range out of bounds: "+span)); //$NON-NLS-1$
				}
			}
		}

		if(!buffer.isEmpty()) {
			Span[] spans = new Span[buffer.size()];
			buffer.toArray(spans);
			spanSet.setSpans(sentenceId, spans);
			buffer.clear();
		}

		if(!closed)
			throw new IllegalArgumentException(errMsg(String.format(
					"Missing '%s' statement to close '%s' at line %d", //$NON-NLS-1$
					END_NODES, BEGIN_NODES, beginLine)));

		return spanSet;
	}

	private EdgeSet readEdges(SpanSet spanSet) throws IOException {
		String line = null;
		boolean closed = false;

		EdgeSet edgeSet = new EdgeSet();
		int beginLine = lineCount;
		Set<Edge> lookup = new HashSet<>();

		while((line = readLine()) != null) {
			if(END_EDGES.equals(line)) {
				closed = true;
				break;
			} else if(BEGIN_EDGES.equals(line)) {
				beginLine = lineCount;
			} else if(line.startsWith(COMMENT_PREFIX)) {
				readProperty(line, edgeSet);
			} else {
				Edge edge = Edge.parse(line, spanSet);

				if(lookup.contains(edge))
					throw new IllegalArgumentException(errMsg("Duplicate edge delcaration: "+edge)); //$NON-NLS-1$
				lookup.add(edge);

				edgeSet.addEdge(edge);
			}
		}

		if(!closed)
			throw new IllegalArgumentException(errMsg(String.format(
					"Missing '%s' statement to close '%s' at line %d", //$NON-NLS-1$
					END_EDGES, BEGIN_EDGES, beginLine)));

		allocateClusters(edgeSet);

		return edgeSet;
	}

	private void allocateClusters(EdgeSet edgeSet) {
		Queue<Edge> pending = new LinkedList<>(edgeSet.getEdges());
		Map<Integer, Cluster> clusterMap = new HashMap<>();
		Map<Span, Cluster> heads = new HashMap<>();
		Set<Edge> postproned = new HashSet<>();
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
			} else if(!postproned.contains(edge)) {
				pending.offer(edge);
				postproned.add(edge);
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
