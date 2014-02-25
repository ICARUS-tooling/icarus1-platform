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

import java.io.IOException;
import java.util.LinkedList;
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
import de.ims.icarus.util.collections.IntHashMap;
import de.ims.icarus.util.strings.CharTableBuffer;
import de.ims.icarus.util.strings.CharTableBuffer.Cursor;
import de.ims.icarus.util.strings.CharTableBuffer.Row;
import de.ims.icarus.util.strings.CharTableBuffer.RowAction;
import de.ims.icarus.util.strings.CharTableBuffer.RowFilter;
import de.ims.icarus.util.strings.StringPrimitives;
import de.ims.icarus.util.strings.StringUtil;


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

	private static final String US = "_"; //$NON-NLS-1$
	private static final String DELIMITER = "\\s+"; //$NON-NLS-1$
	private static final String EMPTY = ""; //$NON-NLS-1$

	public static DefaultCoreferenceData readData(CoreferenceDocumentData document, CharTableBuffer buffer) {
		if(buffer.isEmpty())
			throw new IllegalArgumentException("No rows to read in buffer"); //$NON-NLS-1$

		return createData(document, buffer, null);
	}

	public static CoreferenceDocumentData readDocumentData(CoreferenceDocumentSet documentSet,
			CharTableBuffer buffer, BlockHandler blockHandler) throws IOException {

		CoreferenceDocumentData result = null;
		IntHashMap<Cluster> clusterMap = new IntHashMap<>();

		while(buffer.next()) {
			if(result==null) {
				result = documentSet.newDocument(blockHandler.getExpectedId());
			}
			createData(result, buffer, clusterMap);
		}

		if(result!=null) {
			SpanSet spanSet = result.getDefaultSpanSet();
			if(spanSet!=null) {
				CoreferenceAllocation allocation = result.getDocumentSet().getDefaultAllocation();
				EdgeSet edgeSet = CoreferenceUtils.defaultBuildEdgeSet(spanSet);
				allocation.setEdgeSet(result.getId(), edgeSet);
			}
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
			CharTableBuffer buffer, IntHashMap<Cluster> clusterMap) {
		int size = buffer.getRowCount();
		String[] forms = new String[size];
		LinkedList<Span> spanBuffer = new LinkedList<>();
		Stack<Span> spanStack = new Stack<>();

		String documentId = null;
		String partId = null;

		CorefProperties properties = new CorefProperties();
		Row row;

		// TODO evaluate need to expand storage to cover more than just form and spans
		for(int i=0; i<size; i++) {
			row = buffer.getRow(i);
			row.split(DELIMITER);

//			if(!String.valueOf(i).equals(cols[WORD_COL]))
//				throw new NullPointerException("Invalid start of sentence - word order out of sync: "+i); //$NON-NLS-1$

			forms[i] = get(row, FORM_COL, EMPTY);

			if(documentId==null) {
				documentId = get(row, DOC_COL, null);
			}
			if(partId==null) {
				partId = get(row, PART_COL, null);
			}

			Cursor coref = row.getSplitCursor(row.getSplitCount()-1);
			if(!HYPHEN.equals(coref)) {
				// Build spans
				coref.split('|');
				for(int j=0; j<coref.getSplitCount(); j++) {
					Cursor chunk = coref.getSplitCursor(j);
					if(chunk.startsWith(OBR)) {
						int i1 = chunk.length()-1;
						if(chunk.endsWith(CBR)) {
							i1--;
						}
						int clusterId = StringPrimitives.parseInt(chunk, 1, i1);

						// Start of span definition
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
						int i0 = chunk.startsWith(OBR) ? 1 : 0;
						int clusterId = StringPrimitives.parseInt(chunk, i0, chunk.length()-2);

						// End of span definition
						Span span = null;
						for(int idx=spanStack.size()-1; idx>-1; idx--) {
							if(spanStack.get(idx).getClusterId()==clusterId) {
								span = spanStack.remove(idx);
								break;
							}
						}
						if(span==null)
							throw new IllegalArgumentException("No span introduced for cluster-id: "+clusterId); //$NON-NLS-1$
						span.setEndIndex(i);

						// Ensure there can be only one span covering the exact
						// same range of indices (we keep the first such one and
						// discard all subsequent spans for this range
						if(span.compareTo(spanBuffer.peekLast())!=0) {
							spanBuffer.offerLast(span);
						}
					}

					chunk.recycle();
				}
			}

			coref.recycle();

			// Assign properties
			properties.put(StringUtil.intern(FORM_KEY+'_'+i), get(row, FORM_COL, EMPTY));
			properties.put(StringUtil.intern(TAG_KEY+'_'+i), get(row, TAG_COL, EMPTY));
			properties.put(StringUtil.intern(PARSE_KEY+'_'+i), get(row, PARSE_COL, EMPTY));
			properties.put(StringUtil.intern(LEMMA_KEY+'_'+i), get(row, LEMMA_COL, EMPTY));
			properties.put(StringUtil.intern(FRAMESET_KEY+'_'+i), get(row, FRAMESET_COL, EMPTY));
			properties.put(StringUtil.intern(SENSE_KEY+'_'+i), get(row, SENSE_COL, EMPTY));
			properties.put(StringUtil.intern(SPEAKER_KEY+'_'+i), get(row, SPEAKER_COL, EMPTY));
			properties.put(StringUtil.intern(ENTITY_KEY+'_'+i), get(row, ENTITY_COL, EMPTY));
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

	private static String get(Row row, int index, String def) {
		Cursor cursor = row.getSplitCursor(index);
		String s = EMPTY;
		if(US.equals(cursor) || cursor.isEmpty()) {
			s = def;
		} else {
			s = cursor.toString();
		}

		cursor.recycle();

		return s;
	}

	public static class BlockHandler implements RowFilter {
		private String expectedId;

		/**
		 * @return the expectedId
		 */
		public String getExpectedId() {
			if(expectedId==null)
				throw new IllegalStateException("No document begin defined"); //$NON-NLS-1$

			return expectedId;
		}

		public boolean isDocumentActive() {
			return expectedId!=null;
		}

		/**
		 * @see de.ims.icarus.util.strings.CharTableBuffer.RowFilter#getRowAction(de.ims.icarus.util.strings.CharTableBuffer.Row)
		 */
		@Override
		public RowAction getRowAction(Row row) {
			if(row.isEmpty()) {
				return expectedId==null ? RowAction.IGNORE : RowAction.END_OF_TABLE;
			} if(row.startsWith(BEGIN_DOCUMENT)) {
				if(expectedId!=null)
					throw new IllegalStateException("Unexpected begin of document: "+row); //$NON-NLS-1$
				expectedId = row.subSequence(BEGIN_DOCUMENT.length(), row.length()-1).toString().trim();
				return RowAction.IGNORE;
			} else if(row.startsWith(END_DOCUMENT)) {
				if(expectedId==null)
					throw new IllegalStateException("Missing begin of document: "+row); //$NON-NLS-1$
//				if(!row.regionMatches(END_DOCUMENT.length(), expectedId, 0, expectedId.length()))
//					throw new IllegalStateException("Unexpected end of document: "+row);

				expectedId = null;
				return RowAction.END_OF_TABLE;
			} else {
				return RowAction.VALID;
			}
		}
	}
}
