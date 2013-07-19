/*
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
import java.util.List;

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.language.coref.CorefMember;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.SpanSet;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.location.Location;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AllocationReader {

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

	public AllocationReader() {
		// no-op
	}
	
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

	public CoreferenceAllocation readAllocation() throws Exception {
		
		CoreferenceAllocation allocation = new CoreferenceAllocation();
		
		readAllocation(allocation);
		
		return allocation;
	}
	
	private void readAllocation(CoreferenceAllocation allocation) throws IOException {
		main : while(true) {
			String line = skipEmptyLines();
			if(line==null) {
				break main;
			}
			
			if(!line.startsWith(BEGIN_DOCUMENT))
				throw new IllegalArgumentException("Invalid '"+BEGIN_DOCUMENT+"' declaration: "+line); //$NON-NLS-1$ //$NON-NLS-2$
			
			int startLine = lineCount;
			String documentId = line.substring(BEGIN_DOCUMENT.length()).trim();
			
			if(documentSet!=null && documentSet.getDocument(documentId)==null)
				throw new IllegalArgumentException(String.format(
						"Unknown document id '%s' at line %d ", lineCount,documentId)); //$NON-NLS-1$
			
			// Read in properties
			while((line=readLine())!=null) {
				if(BEGIN_NODES.equals(line)) {
					break;
				} else if(line.startsWith(COMMENT_PREFIX)) {
					readProperty(line, allocation);
				} else
					throw new IllegalArgumentException(String.format(
							"Invalid property statement '%s' at line %d", line, lineCount)); //$NON-NLS-1$
			}
			
			// Read nodes
			SpanSet spanSet = readNodes();
			
			// Read edges
			EdgeSet edgeSet = readEdges(spanSet);
			
			allocation.setSpanSet(documentId, spanSet);
			allocation.setEdgeSet(documentId, edgeSet);
			
			// Check for closing declaration
			
			if(!END_DOCUMENT.equals(skipEmptyLines()))
				throw new IllegalArgumentException(String.format(
						"Missing '%s' statement to close '%s' at line %d", //$NON-NLS-1$
						END_DOCUMENT, BEGIN_DOCUMENT, startLine));
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
	
	private SpanSet readNodes() throws IOException {
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
				
				if(span.getSentenceIndex()!=sentenceId && !buffer.isEmpty()) {
					Span[] spans = new Span[buffer.size()];
					buffer.toArray(spans);
					spanSet.setSpans(sentenceId, spans);
					buffer.clear();
				}
				
				buffer.add(span);
				sentenceId = span.getSentenceIndex();
			}
		}
		
		if(!buffer.isEmpty()) {
			Span[] spans = new Span[buffer.size()];
			buffer.toArray(spans);
			spanSet.setSpans(sentenceId, spans);
			buffer.clear();
		}
		
		if(!closed)
			throw new IllegalArgumentException(String.format(
					"Missing '%s' statement to close '%s' at line %d", //$NON-NLS-1$
					END_NODES, BEGIN_NODES, beginLine));
		
		return spanSet;
	}
	
	private EdgeSet readEdges(SpanSet spanSet) throws IOException {
		String line = null;
		boolean closed = false;
		
		EdgeSet edgeSet = new EdgeSet();
		int beginLine = lineCount;
		
		while((line = readLine()) != null) {
			if(END_EDGES.equals(line)) {
				closed = true;
				break;
			} else if(BEGIN_EDGES.equals(line)) {
				beginLine = lineCount;
			} else if(line.startsWith(COMMENT_PREFIX)) {
				readProperty(line, edgeSet);
			} else {
				edgeSet.addEdge(Edge.parse(line, spanSet));
			}
		}
		
		if(!closed)
			throw new IllegalArgumentException(String.format(
					"Missing '%s' statement to close '%s' at line %d", //$NON-NLS-1$
					END_EDGES, BEGIN_EDGES, beginLine));
		
		return edgeSet;
	}
}