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
package de.ims.icarus.plugins.coref.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

import de.ims.icarus.language.coref.Cluster;
import de.ims.icarus.language.coref.CorefProperties;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.DefaultCoreferenceData;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.SpanSet;


/**
 * 
 * CONLL 2012 shared task data format:
 * <p>
 * Column 	Type 	Description
 * 1 	Document ID 	This is a variation on the document filename
 * 2 	Part number 	Some files are divided into multiple parts numbered as 000, 001, 002, ... etc.
 * 3 	Word number 	
 * 4 	Word itself 	This is the token as segmented/tokenized in the Treebank. Initially the *_skel file contain the placeholder [WORD] which gets replaced by the actual token from the Treebank which is part of the OntoNotes release.
 * 5 	Part-of-Speech 	
 * 6 	Parse bit 	This is the bracketed structure broken before the first open parenthesis in the parse, and the word/part-of-speech leaf replaced with a *. The full parse can be created by substituting the asterix with the "([pos] [word])" string (or leaf) and concatenating the items in the rows of that column.
 * 7 	Predicate lemma 	The predicate lemma is mentioned for the rows for which we have semantic role information. All other rows are marked with a "-"
 * 8 	Predicate Frameset ID 	This is the PropBank frameset ID of the predicate in Column 7.
 * 9 	Word sense 	This is the word sense of the word in Column 3.
 * 10 	Speaker/Author 	This is the speaker or author name where available. Mostly in Broadcast Conversation and Web Log data.
 * 11 	Named Entities 	These columns identifies the spans representing various named entities.
 * 12:N 	Predicate Arguments 	There is one column each of predicate argument structure information for the predicate mentioned in Column 7.
 * N 	Coreference 	Coreference chain information encoded in a parenthesis structure.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CONLL12Utils {

	private CONLL12Utils() {
		// no-op
	}

	private static final String HYPHEN = "-";  //$NON-NLS-1$
	private static final String OBR = "("; //$NON-NLS-1$
	private static final String CBR = ")"; //$NON-NLS-1$

	public static final int DOC_COL = 0;
	public static final int PART_COL = 1;
	public static final int WORD_COL = 2;
	public static final int FORM_COL = 3;
	public static final int TAG_COL = 4;
	public static final int PARSE_COL = 5;
	public static final int LEMMA_COL = 6;
	public static final int FRAMESET_COL = 7;
	public static final int SENSE_COL = 8;
	public static final int SPEAKER_COL = 9;
	public static final int ENTITY_COL = 10;
	
	public static final String FORM_KEY = "form"; //$NON-NLS-1$
	public static final String TAG_KEY = "tag"; //$NON-NLS-1$
	public static final String PARSE_KEY = "parse"; //$NON-NLS-1$
	public static final String LEMMA_KEY = "lemma"; //$NON-NLS-1$
	public static final String FRAMESET_KEY = "frameset"; //$NON-NLS-1$
	public static final String SENSE_KEY = "sense"; //$NON-NLS-1$
	public static final String SPEAKER_KEY = "speaker"; //$NON-NLS-1$
	public static final String ENTITY_KEY = "entity"; //$NON-NLS-1$

	public static final Pattern WS = Pattern.compile("\\s+"); //$NON-NLS-1$
	public static final Pattern BLANK = Pattern.compile("^(_|-)$"); //$NON-NLS-1$
	public static final Pattern BAR = Pattern.compile("\\|"); //$NON-NLS-1$
	public static final Pattern NUM = Pattern.compile("(\\d+)"); //$NON-NLS-1$

	public static final String BEGIN_DOCUMENT = "#begin document"; //$NON-NLS-1$
	public static final String END_DOCUMENT = "#end document"; //$NON-NLS-1$
	
	public static DefaultCoreferenceData readData(CoreferenceDocumentData document, BufferedReader reader) throws IOException {
		DefaultCoreferenceData result = null;
		List<String> lines = new ArrayList<>();
		String line;
		while((line = reader.readLine()) != null) {
			if(line.isEmpty()) {
				result = createData(document, lines, null);
				break;
			}
			
			lines.add(line);
		}
		
		return result;
	}
	
	public static CoreferenceDocumentData readDocumentData(CoreferenceDocumentSet documentSet, BufferedReader reader) throws IOException {
		String header = reader.readLine();
		if(header==null) {
			return null;
		}
		
		if(!header.startsWith(BEGIN_DOCUMENT))
			throw new IllegalArgumentException("Illegal '#begin document' definition: "+header); //$NON-NLS-1$
		
		header = header.substring(BEGIN_DOCUMENT.length()).trim();
		
		CoreferenceDocumentData result = documentSet.newDocument(header);
		Map<Integer, Cluster> clusterMap = new HashMap<>();
		List<String> lines = new ArrayList<>();
		boolean closed = false;
		String line;
		while((line = reader.readLine()) != null) {
			if(line.isEmpty()) {
				createData(result, lines, clusterMap);
				lines.clear();
			} else {
				lines.add(line);
			}
			
			if(line.startsWith(END_DOCUMENT)) {
				closed = true;
				break;
			}
		}
		
		if(!closed)
			throw new IllegalArgumentException("Missing '"+END_DOCUMENT+"' statement"); //$NON-NLS-1$ //$NON-NLS-2$
		
		SpanSet spanSet = result.getDefaultSpanSet();
		if(spanSet!=null) {
			CoreferenceAllocation allocation = result.getDocumentSet().getDefaultAllocation();
			EdgeSet edgeSet = CoreferenceUtils.defaultBuildEdgeSet(spanSet);
			allocation.setEdgeSet(result.getId(), edgeSet);
		}
		
		return result;
	}
	
	/*
	 * CONLL 2012 shared task data format:
	 * <p>
	 * Column 	Type 	Description
	 * 1 	Document ID 	This is a variation on the document filename
	 * 2 	Part number 	Some files are divided into multiple parts numbered as 000, 001, 002, ... etc.
	 * 3 	Word number 	
	 * 4 	Word itself 	This is the token as segmented/tokenized in the Treebank. Initially the *_skel file contain the placeholder [WORD] which gets replaced by the actual token from the Treebank which is part of the OntoNotes release.
	 * 5 	Part-of-Speech 	
	 * 6 	Parse bit 	This is the bracketed structure broken before the first open parenthesis in the parse, and the word/part-of-speech leaf replaced with a *. The full parse can be created by substituting the asterix with the "([pos] [word])" string (or leaf) and concatenating the items in the rows of that column.
	 * 7 	Predicate lemma 	The predicate lemma is mentioned for the rows for which we have semantic role information. All other rows are marked with a "-"
	 * 8 	Predicate Frameset ID 	This is the PropBank frameset ID of the predicate in Column 7.
	 * 9 	Word sense 	This is the word sense of the word in Column 3.
	 * 10 	Speaker/Author 	This is the speaker or author name where available. Mostly in Broadcast Conversation and Web Log data.
	 * 11 	Named Entities 	These columns identifies the spans representing various named entities.
	 * 12:N 	Predicate Arguments 	There is one column each of predicate argument structure information for the predicate mentioned in Column 7.
	 * N 	Coreference 	Coreference chain information encoded in a parenthesis structure.
	 * 
	 */
	private static DefaultCoreferenceData createData(CoreferenceDocumentData document, 
			List<String> lines, Map<Integer, Cluster> clusterMap) {
		int size = lines.size();
		String[] forms = new String[size];
		LinkedList<Span> spanBuffer = new LinkedList<>();
		Stack<Span> spanStack = new Stack<>();
		
		String documentId = null;
		String partId = null;
		
		CorefProperties properties = new CorefProperties();
		
		// TODO evaluate need to expand storage to cover more than just form and spans
		for(int i=0; i<size; i++) {
			String[] cols = WS.split(lines.get(i));
			
			if(!String.valueOf(i).equals(cols[WORD_COL]))
				throw new IllegalArgumentException("Invalid start of sentence - word order out of sync: "+i); //$NON-NLS-1$
			
			forms[i] = cols[FORM_COL];
			
			if(documentId==null) {
				documentId = cols[DOC_COL];
			}
			if(partId==null) {
				partId = cols[PART_COL];
			}
			
			String coref = cols[cols.length-1];
			if(!HYPHEN.equals(coref)) {
				// Build spans
				String[] chunks = BAR.split(coref);
				for(String chunk : chunks) {
					if(chunk.startsWith(OBR)) {
						// Start of span definition
						int clusterId = Integer.parseInt(chunk.endsWith(CBR) ? 
								chunk.substring(1, chunk.length()-1) : chunk.substring(1));
						Span span = new Span(i, i, document.size());
						
						if(clusterMap!=null) {
							Cluster cluster = clusterMap.get(clusterId);
							if (cluster==null) {
								cluster = new Cluster(clusterId);
								clusterMap.put(clusterId, cluster);
							}
							cluster.add(span);
							span.setCluster(cluster);
						}
						
						spanStack.push(span);
					}
					if(chunk.endsWith(CBR)) {
						// End of span definition
						Span span = spanStack.pop();
						span.setEndIndex(i);
						
						// Ensure there can be only one span covering the exact
						// same range of indices (we keep the first such one and
						// discard all subsequent spans for this range 
						if(span.compareTo(spanBuffer.peekLast())!=0) {
							spanBuffer.offerLast(span);
						}
					}
				}
			}
			
			// Assign properties
			properties.put(FORM_KEY+'_'+i, cols[FORM_COL]);
			properties.put(TAG_KEY+'_'+i, cols[TAG_COL]);
			properties.put(PARSE_KEY+'_'+i, cols[PARSE_COL]);
			properties.put(LEMMA_KEY+'_'+i, cols[LEMMA_COL]);
			properties.put(FRAMESET_KEY+'_'+i, cols[FRAMESET_COL]);
			properties.put(SENSE_KEY+'_'+i, cols[SENSE_COL]);
			properties.put(SPEAKER_KEY+'_'+i, cols[SPEAKER_COL]);
			properties.put(ENTITY_KEY+'_'+i, cols[ENTITY_COL]);
		}
		
		if(!spanStack.isEmpty())
			throw new IllegalArgumentException("Coreference data contains unclosed spans"); //$NON-NLS-1$
		
		DefaultCoreferenceData result = new DefaultCoreferenceData(document, forms);
		result.setProperties(properties);
		result.setSentenceIndex(document.size());
		document.add(result);
		result.setProperty(CoreferenceData.DOCUMENT_ID_PROPERTY, documentId);
		result.setProperty(CoreferenceData.PART_ID_PROPERTY, partId);

		Span[] spans = spanBuffer.isEmpty() ? null : spanBuffer.toArray(new Span[spanBuffer.size()]);
		if(spans!=null) {
			CoreferenceAllocation allocation = document.getDocumentSet().getDefaultAllocation();
			allocation.setSpans(document.getId(), result.getSentenceIndex(), spans);
		}
		
		return result;
	}
}
