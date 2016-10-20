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
package de.ims.icarus.plugins.prosody.io;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import de.ims.icarus.Core;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.language.coref.Cluster;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.DocumentSet;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.SpanSet;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.prosody.DefaultProsodicDocumentData;
import de.ims.icarus.plugins.prosody.DefaultProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodicDocumentData;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.sampa.SampaMapper2;
import de.ims.icarus.util.CompactProperties;
import de.ims.icarus.util.strings.CharTableBuffer;
import de.ims.icarus.util.strings.CharTableBuffer.Cursor;
import de.ims.icarus.util.strings.CharTableBuffer.Row;
import de.ims.icarus.util.strings.CharTableBuffer.RowAction;
import de.ims.icarus.util.strings.CharTableBuffer.RowFilter;
import de.ims.icarus.util.strings.StringPrimitives;


/**
 *
 * Each file may contain an arbitrary number of document blocks
 *
 * Document block:
 *
 * 	#begin document <document name>
 * 	<properties>
 *
 * 	<properties>
 * 	<block section>
 *
 * 	#end document
 *
 *
 * Content of <block section>:
 * 	Tabular data with the following columns, one word per line. Blocks are separated by empty lines.
 * 	A block may represent a sentence or other kind of grouping within a document.
 *
 * Content of <properties>:
 * 	Arbitrary property declarations, one per line, in the form of
 * 	#<key> <value>
 * 	'key' may be any non-empty string apart from the two reserved tokens "begin" and "end".
 * 	The first whitespace sequence is used as delimiter between key and value, so the key itself is not allowed to contain
 * 	any whitespace characters. The value string on the other hand is not restricted in its content other than not being able
 * 	to contain any linebreak characters.
 *
 * Block level fields:
 *
 * Column 	Name					Type 			Description
 * 1 		Word number 			number			0 to block_length-1
 * 2 		Word itself 			string			This is the token as segmented/tokenized in the Treebank.
 * 3 		Part-of-Speech 			string
 * 4		Features				string[]		Morphological features
 * 5 		Head 					number			Word Number of the head in the dependency structure (0 means root)
 * 6 		DepRel					string			Dependency relation (if the word is not the root node)
 * 7 		Speaker/Author 			string			This is the speaker or author name where available. Mostly in Broadcast Conversation and Web Log data.
 * 8		Speaker-Features		string[]		Special features array to store information about speakers
 * 9 		Named Entities 			string			These columns identifies the spans representing various named entities.
 * 10 		Coreference 			string			Coreference chain information encoded in a parenthesis structure.
 * 11		Begin-Timestamp			float			Timestamp of word begin in audio file
 * 12		End-Timestamp			float			Timestamp of word end in audio file
 * 13		Syllable-SoundOffsets		int[]			Character based offsets for syllables in the word
 * 14		Syllable-Labels			string[]		Syllable labels. This array is the main reference when determining the number of syllables in a word
 * 15		Syllable-Timestamps		float[]			Timestamps of each syllable's begin in audio file
 * 16		Syllable-Vowel			string[]		Phonetic vowel description
 * 17		Syllable-Stress			int[]			List of stressed syllables in a word. Values refer to the Syllable-Labels array
 * 18		Syllable-Duration		float[]			Duration of each syllable
 * 19		Vowel-Duration			float[]			Duration of the vowel in a certain syllable.
 * 20		Syllable-Startpitch		float[]
 * 21		Syllable-Midpitch		float[]
 * 22		Syllable-Endpitch		float[]
 * 22		Coda-Type				String[]
 * 23		Coda-Size				int[]
 * 22		Onset-Type				String[]
 * 23		Onset-Size				int[]
 * 24		Phoneme-Count			int[]
 * 25:30	PaintE-Parameters		float[]			PaintE parameters (total of 6 columns) for each syllable.
 *
 *
 * All arrays use the pipe-character ('|') as delimiter
 * The underscore character ('_') signals empty values
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class DirndlIOUtils implements ProsodyConstants {

	public static boolean DEFAULT_SYLLABLES_FROM_SAMPA = false;
	public static boolean DEFAULT_MARK_ACCENTS = false;
	public static Set<String> SENTENCE_BLACKLIST = null;

	private DirndlIOUtils() {
		// no-op
	}

	private static final Object HYPHEN = "-";  //$NON-NLS-1$
	private static final String OBR = "("; //$NON-NLS-1$
	private static final String CBR = ")"; //$NON-NLS-1$
	private static final char PIPE = '|';
	private static final char SPACE = ' ';
	private static final String COMMENT_BEGIN = "#"; //$NON-NLS-1$

//	public static final int ID_COL = 0;
//	public static final int FORM_COL = 1;
//	public static final int POS_COL = 2;
//	public static final int LEMMA_COL = 3;
//	public static final int FEATURES_COL = 4;
//	public static final int HEAD_COL = 5;
//	public static final int DEPREL_COL = 6;
//	public static final int SPEAKER_COL = 7;
//	public static final int SPEAKER_FEATURES_COL = 8;
//	public static final int ENTITY_COL = 9;
//	public static final int COREF_COL = 10;
//	public static final int BEGIN_TS_COL = 11;
//	public static final int END_TS_COL = 12;
//	public static final int SYL_OFFSET_COL = 13;
//	public static final int SYL_LABEL_COL = 14;
//	public static final int SYL_TIMESTAMP_COL = 15;
//	public static final int SYL_VOWEL_COL = 16;
//	public static final int SYL_STRESS_COL = 17;
//	public static final int SYL_DURATION_COL = 18;
//	public static final int VOWEL_DURATION_COL = 19;
//	public static final int SYL_STARTPITCH_COL = 20;
//	public static final int SYL_MIDPITCH_COL = 21;
//	public static final int SYL_ENDPITCH_COL = 22;
//	public static final int CODA_TYPE_COL = 23;
//	public static final int CODA_SIZE_COL = 24;
//	public static final int ONSET_TYPE_COL = 25;
//	public static final int ONSET_SIZE_COL = 26;
//	public static final int PHONEME_COUNT_COL = 27;
//	public static final int PAINTE_A1_COL = 28;
//	public static final int PAINTE_A2_COL = 29;
//	public static final int PAINTE_B_COL = 30;
//	public static final int PAINTE_C1_COL = 31;
//	public static final int PAINTE_C2_COL = 32;
//	public static final int PAINTE_D_COL = 33;

	public static final String BEGIN_DOCUMENT = "#begin document"; //$NON-NLS-1$
	public static final String END_DOCUMENT = "#end document"; //$NON-NLS-1$

	public static final String FORMAT_VERSION = "#version"; //$NON-NLS-1$

	private static final Object US = "_"; //$NON-NLS-1$
	private static final String DELIMITER = "\\s+"; //$NON-NLS-1$
	private static final String EMPTY = ""; //$NON-NLS-1$

	public static ProsodicDocumentData readDocumentData(DocumentSet documentSet,
			CharTableBuffer buffer, ReaderControl readerControl) throws IOException {

		DefaultProsodicDocumentData result = null;

		while(buffer.next()) {
			try {
				if(result==null) {
					result = new DefaultProsodicDocumentData(documentSet, documentSet.size());
					result.setId(readerControl.getExpectedId());
					result.setProperty(INDEX_KEY, documentSet.size());
					documentSet.add(result);

					if(readDocumentProperties(result, buffer)) {
						continue;
					}
				}
				readerControl.getReader().createData(result, buffer);
			} catch(Exception e) {
				// Cannot be IOException or UnsupportedFormatException

				throw new IOException(buffer.getErrorMessage("Failed to read tabular prosodic data"), e); //$NON-NLS-1$
			}
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

	private static boolean readDocumentProperties(DefaultProsodicDocumentData document, CharTableBuffer buffer) {

		// First run: check if all rows are comments
		for(int i=0; i<buffer.getRowCount(); i++) {
			if(!buffer.getRow(i).startsWith(COMMENT_BEGIN)) {
				// Regular sentence data -> abort
				return false;
			}
		}

		// The entire block consists of comments
		for(int i=0; i<buffer.getRowCount(); i++) {
			Row row = buffer.getRow(i);
			int sepIndex = row.indexOf(SPACE);

			// Simply ignore invalid comments
			if(sepIndex==-1) {
				continue;
			}

			String key = row.subSequence(1, sepIndex).toString();
			String value = row.subSequence(sepIndex+1, row.length()).toString();

			document.setProperty(key, value);
		}

		return true;
	}

	private static SentenceReader createReader(ReaderControl readerControl) {
		SentenceReader reader = null;
		String formatVersion = readerControl.getFormatVersion();

		if(formatVersion!=null) {
			switch (formatVersion) {
			case "0.3": //$NON-NLS-1$
				reader = new SentenceReaderV03(readerControl);
				break;

			case "0.4": //$NON-NLS-1$
				reader = new SentenceReaderV04(readerControl);
				break;

			case "0.5": //$NON-NLS-1$
				reader = new SentenceReaderV05(readerControl);
				break;

			default:
				break;
			}
		}

		if(reader==null) {
			reader = new SentenceReaderV03(readerControl);
		}

		reader.init();

		return reader;
	}

//	private static DefaultProsodicSentenceData createData(final ProsodicDocumentData document,
//			final CharTableBuffer buffer, final TIntObjectMap<Cluster> clusterMap, final SampaMapper2 sampaMapper) {
//		int size = buffer.getRowCount();
//		LinkedList<Span> spanBuffer = new LinkedList<>();
//		Stack<Span> spanStack = new Stack<>();
//
//		CompactProperties properties = new CompactProperties();
//		int headerOffset = 0;
//		for(int i=0; i<size; i++) {
//			Row row = buffer.getRow(i);
//
//			if(!row.startsWith(COMMENT_BEGIN)) {
//				break;
//			}
//
//			int sepIndex = row.indexOf(SPACE);
//
//			// Only consider valid declarations
//			if(sepIndex!=-1) {
//				String key = row.subSequence(1, sepIndex).toString();
//				String value = row.subSequence(sepIndex+1, row.length()).toString();
//				properties.put(key, value);
//			}
//
//			headerOffset++;
//		}
//
//		String[] forms = new String[size-headerOffset];
//		DefaultProsodicSentenceData result = new DefaultProsodicSentenceData(document, forms);
//
//		result.setProperties(properties);
//
//		String speaker = (String) result.getProperty(SPEAKER_KEY);
//
//		for(int i=0; i<size-headerOffset; i++) {
//			Row row = buffer.getRow(i+headerOffset);
//
//			row.split(DELIMITER);
//
////			if(!String.valueOf(i).equals(cols[WORD_COL]))
////				throw new NullPointerException("Invalid start of sentence - word order out of sync: "+i); //$NON-NLS-1$
//
//			forms[i] = get(row, FORM_COL, EMPTY);
//
//			Cursor coref = row.getSplitCursor(row.getSplitCount()-1);
//			if(!coref.equals(HYPHEN)) {
//				// Build spans
//				coref.split('|');
//				for(int j=0; j<coref.getSplitCount(); j++) {
//					Cursor chunk = coref.getSplitCursor(j);
//					if(chunk.startsWith(OBR)) {
//						int i1 = chunk.length()-1;
//						if(chunk.endsWith(CBR)) {
//							i1--;
//						}
//						int clusterId = StringPrimitives.parseInt(chunk, 1, i1);
//
//						// Start of span definition
//						Span span = new Span(i, i, document.size());
//
//						if(clusterMap!=null) {
//							Cluster cluster = clusterMap.get(clusterId);
//							if (cluster==null) {
//								cluster = new Cluster(clusterId);
//								clusterMap.put(clusterId, cluster);
//							}
//							cluster.add(span);
//							span.setCluster(cluster);
//						}
//
//						spanStack.push(span);
//					}
//					if(chunk.endsWith(CBR)) {
//						int i0 = chunk.startsWith(OBR) ? 1 : 0;
//						int clusterId = StringPrimitives.parseInt(chunk, i0, chunk.length()-2);
//
//						// End of span definition
//						Span span = null;
//						for(int idx=spanStack.size()-1; idx>-1; idx--) {
//							if(spanStack.get(idx).getClusterId()==clusterId) {
//								span = spanStack.remove(idx);
//								break;
//							}
//						}
//						if(span==null)
//							throw new IllegalArgumentException("No span introduced for cluster-id: "+clusterId); //$NON-NLS-1$
//						span.setEndIndex(i);
//
//						// Ensure there can be only one span covering the exact
//						// same range of indices (we keep the first such one and
//						// discard all subsequent spans for this range
//						if(span.compareTo(spanBuffer.peekLast())!=0) {
//							spanBuffer.offerLast(span);
//						}
//					}
//
//					chunk.recycle();
//				}
//			}
//
//			coref.recycle();
//
//			// Assign properties
//			result.setProperty(i, FORM_KEY, forms[i]);
//			result.setProperty(i, POS_KEY, get(row, POS_COL, EMPTY));
//			result.setProperty(i, LEMMA_KEY, get(row, LEMMA_COL, EMPTY));
//			result.setProperty(i, FEATURES_KEY, get(row, FEATURES_COL, EMPTY));
//			result.setProperty(i, HEAD_KEY, Math.max(getInt(row, HEAD_COL, DATA_UNDEFINED_VALUE)-1, DATA_UNDEFINED_VALUE));
//			result.setProperty(i, DEPREL_KEY, get(row, DEPREL_COL, EMPTY));
//			result.setProperty(i, SPEAKER_KEY, get(row, SPEAKER_COL, EMPTY));
//			if(speaker==null) {
//				speaker = (String) result.getProperty(i, SPEAKER_KEY);
//			}
//			result.setProperty(i, SPEAKER_FEATURES_KEY, get(row, SPEAKER_FEATURES_COL, EMPTY));
//			result.setProperty(i, ENTITY_KEY, get(row, ENTITY_COL, EMPTY));
//			result.setProperty(i, BEGIN_TS_KEY, getFloat(row, BEGIN_TS_COL, DATA_UNDEFINED_VALUE));
//			result.setProperty(i, END_TS_KEY, getFloat(row, END_TS_COL, DATA_UNDEFINED_VALUE));
//			result.setProperty(i, SYLLABLE_LABEL_KEY, getStrings(row, SYL_LABEL_COL));
//			result.setProperty(i, SYLLABLE_TIMESTAMP_KEY, getFloats(row, SYL_TIMESTAMP_COL));
//			result.setProperty(i, SYLLABLE_VOWEL_KEY, getStrings(row, SYL_VOWEL_COL));
//
//			int[] offsets = getInts(row, SYL_OFFSET_COL);
//			if(offsets==EMPTY_INTS) {
//				if(sampaMapper!=null) {
//					int sylCount = result.getSyllableCount(i);
//					if(sylCount>0) {
//						String[] sampa = (String[]) result.getProperty(i, SYLLABLE_LABEL_KEY);
//						String[] labels = sampaMapper.split(forms[i], sampa);
//						if(labels!=null) {
//							offsets = new int[sylCount];
//							int offset = 0;
//							for(int k=0; k<sylCount; k++) {
//								offsets[k] = offset;
//								offset += labels[k].length();
//							}
//							result.setProperty(i, SYLLABLE_FORM_KEY, labels);
//							result.setMapsSyllables(i, true);
//						} else {
//							boolean report = true;
//							if(SENTENCE_BLACKLIST!=null) {
//								Object sentNum = result.getProperty(SENTENCE_NUMBER_KEY);
//								if(sentNum!=null && SENTENCE_BLACKLIST.contains(sentNum)) {
//									report = false;
//								}
//							}
//
//							if(report) {
//								LoggerFactory.info(DirndlIOUtils.class,
//										buffer.getErrorMessage("Unable to map /"+Arrays.deepToString(sampa)+"/ to '"+forms[i]+"'")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//							}
//						}
//					}
//				}
//			}
//			result.setProperty(i, SYLLABLE_OFFSET_KEY, offsets);
//
//			// Special handling for stressed syllables (can be either a single value or an array)
//			int[] stressedIndices = getInts(row, SYL_STRESS_COL);
//			for(int index : stressedIndices) {
//				result.setSyllableStressed(i, index, true);
//			}
//
//			result.setProperty(i, SYLLABLE_DURATION_KEY, getFloats(row, SYL_DURATION_COL));
//			result.setProperty(i, VOWEL_DURATION_KEY, getFloats(row, VOWEL_DURATION_COL));
//			result.setProperty(i, SYLLABLE_STARTPITCH_KEY, getFloats(row, SYL_STARTPITCH_COL));
//			result.setProperty(i, SYLLABLE_MIDPITCH_KEY, getFloats(row, SYL_MIDPITCH_COL));
//			result.setProperty(i, SYLLABLE_ENDPITCH_KEY, getFloats(row, SYL_ENDPITCH_COL));
//			result.setProperty(i, CODA_TYPE_KEY, getStrings(row, CODA_TYPE_COL));
//			result.setProperty(i, CODA_SIZE_KEY, getInts(row, CODA_SIZE_COL));
//			result.setProperty(i, ONSET_TYPE_KEY, getStrings(row, ONSET_TYPE_COL));
//			result.setProperty(i, ONSET_SIZE_KEY, getInts(row, ONSET_SIZE_COL));
//			result.setProperty(i, PHONEME_COUNT_KEY, getInts(row, PHONEME_COUNT_COL));
//			result.setProperty(i, PAINTE_A1_KEY, getFloats(row, PAINTE_A1_COL));
//			result.setProperty(i, PAINTE_A2_KEY, getFloats(row, PAINTE_A2_COL));
//			result.setProperty(i, PAINTE_B_KEY, getFloats(row, PAINTE_B_COL));
//			result.setProperty(i, PAINTE_C1_KEY, getFloats(row, PAINTE_C1_COL));
//			result.setProperty(i, PAINTE_C2_KEY, getFloats(row, PAINTE_C2_COL));
//			result.setProperty(i, PAINTE_D_KEY, getFloats(row, PAINTE_D_COL));
//		}
//
//		if(!spanStack.isEmpty())
//			throw new IllegalArgumentException("Coreference data contains unclosed spans"); //$NON-NLS-1$
//
//		if(speaker!=null) {
//			result.setProperty(SPEAKER_KEY, speaker);
//		}
//
//		result.setSentenceIndex(document.size());
//		document.add(result);
//
//		Span[] spans = spanBuffer.isEmpty() ? null : spanBuffer.toArray(new Span[spanBuffer.size()]);
//		if(spans!=null) {
//			CoreferenceAllocation allocation = document.getDocumentSet().getDefaultAllocation();
//			allocation.setSpans(document.getId(), result.getSentenceIndex(), spans);
//		}
//
//		return result;
//	}

	private static String get(Row row, int index, String def) {
		Cursor cursor = row.getSplitCursor(index);
		String s = EMPTY;
		if(index==-1 || cursor.equals(US) || cursor.isEmpty()) {
			s = def;
		} else {
			s = cursor.toString();
		}

		cursor.recycle();

		return s;
	}

	private static float getFloat(Row row, int index, float def) {
		Cursor cursor = row.getSplitCursor(index);
		float f = def;
		if(index!=-1 && !cursor.equals(US) && !cursor.isEmpty()) {
			f = StringPrimitives.parseFloat(cursor);
		}

		cursor.recycle();

		return f;
	}

	private static int getInt(Row row, int index, int def) {
		Cursor cursor = row.getSplitCursor(index);
		int i = def;
		if(index!=-1 && !cursor.equals(US) && !cursor.isEmpty()) {
			i = StringPrimitives.parseInt(cursor);
		}

		cursor.recycle();

		return i;
	}

	private static final float[] EMPTY_FLOATS = new float[0];
	private static final int[] EMPTY_INTS = new int[0];
	private static final String[] EMPTY_STRINGS = new String[0];

	private static float[] getFloats(Row row, int index) {
		if(index<0) {
			return EMPTY_FLOATS;
		}

		Cursor cursor = row.getSplitCursor(index);

		float[] result;
		if(index==-1 || cursor.equals(US) || cursor.isEmpty()) {
			result = EMPTY_FLOATS;
		} else {
			result = new float[cursor.split(PIPE)];
		}

		for(int i=0; i<result.length; i++) {
			Cursor part = cursor.getSplitCursor(i);
			result[i] = StringPrimitives.parseFloat(part);
			part.recycle();
		}

		cursor.recycle();

		return result;
	}

	private static int[] getInts(Row row, int index) {
		if(index<0) {
			return EMPTY_INTS;
		}

		Cursor cursor = row.getSplitCursor(index);

		int[] result;
		if(index==-1 || cursor.equals(US) || cursor.isEmpty()) {
			result = EMPTY_INTS;
		} else {
			result = new int[cursor.split(PIPE)];
		}

		for(int i=0; i<result.length; i++) {
			Cursor part = cursor.getSplitCursor(i);
			result[i] = StringPrimitives.parseInt(part);
			part.recycle();
		}

		cursor.recycle();

		return result;
	}

	private static String[] getStrings(Row row, int index) {
		if(index<0) {
			return EMPTY_STRINGS;
		}

		Cursor cursor = row.getSplitCursor(index);

		String[] result;
		if(index==-1 || cursor.equals(US) || cursor.isEmpty()) {
			result = EMPTY_STRINGS;
		} else {
			result = new String[cursor.split(PIPE)];
		}

		for(int i=0; i<result.length; i++) {
			Cursor part = cursor.getSplitCursor(i);
			result[i] = part.toString();
			part.recycle();
		}

		cursor.recycle();

		return result;
	}

	public static class ReaderControl implements RowFilter {
		private String expectedId;
		private String formatVersion;
		private boolean syllableOffsetsFromSampa;
		private boolean markAccentOnWords = false;
		private boolean onlyConsiderStressedSylables = false;
		private int accentExcursion = -1;

		private SentenceReader reader;
		private SampaMapper2 sampaMapper;

		public ReaderControl() {

			syllableOffsetsFromSampa = DEFAULT_SYLLABLES_FROM_SAMPA;
			markAccentOnWords = DEFAULT_MARK_ACCENTS;
			accentExcursion = -1;
			if(!Core.isDebugActive()) {
				syllableOffsetsFromSampa = ConfigRegistry.getGlobalRegistry().getBoolean(
						"plugins.prosody.prosodyReader.syllableOffsetsFromSampa"); //$NON-NLS-1$
				markAccentOnWords = ConfigRegistry.getGlobalRegistry().getBoolean(
						"plugins.prosody.prosodyReader.markAccentOnWords"); //$NON-NLS-1$
				accentExcursion = ConfigRegistry.getGlobalRegistry().getInteger(
						"plugins.prosody.prosodyReader.accentExcursion"); //$NON-NLS-1$
				onlyConsiderStressedSylables = ConfigRegistry.getGlobalRegistry().getBoolean(
						"plugins.prosody.prosodyReader.onlyConsiderStressedSylables"); //$NON-NLS-1$
			}
		}

		public SampaMapper2 getSampaMapper() {
			if(sampaMapper==null && syllableOffsetsFromSampa) {
				//TODO provide multiple files as options?
				sampaMapper = new SampaMapper2();
			}

			return sampaMapper;
		}

		public SentenceReader getReader() {
			if(reader==null) {
				reader = createDefaultReader(this);
			}

			return reader;
		}

		public boolean isSyllableOffsetsFromSampa() {
			return syllableOffsetsFromSampa;
		}

		public boolean isMarkAccentOnWords() {
			return markAccentOnWords;
		}

		public boolean isOnlyConsiderStressedSylables() {
			return onlyConsiderStressedSylables;
		}

		public int getAccentExcursion() {
			return accentExcursion;
		}

		public static SentenceReader createDefaultReader(ReaderControl readerControl) {
			return new SentenceReaderV03(readerControl);
		}

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

		public String getFormatVersion() {
			return formatVersion;
		}

		/**
		 * @see de.ims.icarus.util.strings.CharTableBuffer.RowFilter#getRowAction(de.ims.icarus.util.strings.CharTableBuffer.Row)
		 */
		@Override
		public RowAction getRowAction(Row row) {
			if(row.isEmpty()) {
				return expectedId==null ? RowAction.IGNORE : RowAction.END_OF_TABLE;
			} if(row.startsWith(FORMAT_VERSION)) {
				if(formatVersion!=null)
					throw new IllegalStateException("Duplicate format version declaration: "+row); //$NON-NLS-1$
				formatVersion = row.subSequence(FORMAT_VERSION.length(), row.length()).toString().trim();
				reader = createReader(this);
			} if(row.startsWith(BEGIN_DOCUMENT)) {
				if(expectedId!=null)
					throw new IllegalStateException("Unexpected begin of document: "+row); //$NON-NLS-1$
				expectedId = row.subSequence(BEGIN_DOCUMENT.length(), row.length()).toString().trim();
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

	private abstract static class SentenceReader {

		protected final LinkedList<Span> spanBuffer = new LinkedList<>();
		protected final Stack<Span> spanStack = new Stack<>();

		protected ProsodicDocumentData document;
		protected CharTableBuffer buffer;
		protected DefaultProsodicSentenceData result;
		protected String[] forms;
		protected TIntObjectMap<Cluster> clusterMap = new TIntObjectHashMap<>();

		protected int ID_COL = 0;
		protected int FORM_COL = 1;
		protected int COREF_COL = 10;

		protected final ReaderControl readerControl;

		protected SentenceReader(ReaderControl readerControl) {
			if (readerControl == null)
				throw new NullPointerException("Invalid readerControl"); //$NON-NLS-1$

			this.readerControl = readerControl;
		}

		public void init() {
			initColumns();
		}

		protected abstract void initColumns();

		protected void clearInternals() {
			spanBuffer.clear();
			spanStack.clear();
		}

		protected int readProperties(CompactProperties properties) {
			int headerOffset = 0;
			for(int i=0; i<buffer.getRowCount(); i++) {
				Row row = buffer.getRow(i);

				if(!row.startsWith(COMMENT_BEGIN)) {
					break;
				}

				int sepIndex = row.indexOf(SPACE);

				// Only consider valid declarations
				if(sepIndex!=-1) {
					String key = row.subSequence(1, sepIndex).toString();
					String value = row.subSequence(sepIndex+1, row.length()).toString();
					properties.put(key, value);
				}

				headerOffset++;
			}

			return headerOffset;
		}

		protected void readAdditionalColumns(int i, Row row) {
			// for subclasses
		}

		protected void readCoref(int i, Row row) {
			Cursor coref = row.getSplitCursor(COREF_COL);
			if(!coref.equals(HYPHEN) && !coref.equals(US)) {
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
		}

		protected void readLine(int i, Row row) {

//			if(!String.valueOf(i).equals(cols[WORD_COL]))
//				throw new NullPointerException("Invalid start of sentence - word order out of sync: "+i); //$NON-NLS-1$

			forms[i] = get(row, FORM_COL, EMPTY);

			readCoref(i, row);

			readAdditionalColumns(i, row);
		}

		public DefaultProsodicSentenceData createData(final ProsodicDocumentData document, final CharTableBuffer buffer) {
			int size = buffer.getRowCount();

			this.document = document;
			this.buffer = buffer;

			clearInternals();

			// Read in properties
			CompactProperties properties = new CompactProperties();
			int headerOffset = readProperties(properties);
			forms = new String[size-headerOffset];
			result = new DefaultProsodicSentenceData(document, forms);

			result.setProperties(properties);
			result.setProperty(INDEX_KEY, document.size());

			String speaker = (String) result.getProperty(SPEAKER_KEY);

			for(int i=0; i<size-headerOffset; i++) {
				Row row = buffer.getRow(i+headerOffset);

				row.split(DELIMITER);

				readLine(i, row);

				if(speaker==null) {
					speaker = (String) result.getProperty(i, SPEAKER_KEY);
				}
			}

			if(!spanStack.isEmpty())
				throw new IllegalArgumentException("Coreference data contains unclosed spans"); //$NON-NLS-1$

			if(speaker!=null) {
				result.setProperty(SPEAKER_KEY, speaker);
			}

			result.setIndex(document.size());
			document.add(result);

			Span[] spans = spanBuffer.isEmpty() ? null : spanBuffer.toArray(new Span[spanBuffer.size()]);
			if(spans!=null) {
				CoreferenceAllocation allocation = document.getDocumentSet().getDefaultAllocation();
				allocation.setSpans(document.getId(), result.getIndex(), spans);
			}

			clearInternals();

			return result;
		}
	}

	private static class SentenceReaderV03 extends SentenceReader {

		protected int POS_COL = 2;
		protected int LEMMA_COL = 3;
		protected int FEATURES_COL = 4;
		protected int HEAD_COL = 5;
		protected int DEPREL_COL = 6;
		protected int SPEAKER_COL = 7;
		protected int SPEAKER_FEATURES_COL = 8;
		protected int ENTITY_COL = 9;
		protected int BEGIN_TS_COL = 11;
		protected int END_TS_COL = 12;
		protected int SYL_OFFSET_COL = 13;
		protected int SYL_LABEL_COL = 14;
		protected int SYL_TIMESTAMP_COL = 15;
		protected int SYL_VOWEL_COL = 16;
		protected int SYL_STRESS_COL = 17;
		protected int SYL_DURATION_COL = 18;
		protected int VOWEL_DURATION_COL = 19;
		protected int SYL_STARTPITCH_COL = 20;
		protected int SYL_MIDPITCH_COL = 21;
		protected int SYL_ENDPITCH_COL = 22;
		protected int CODA_TYPE_COL = 23;
		protected int CODA_SIZE_COL = 24;
		protected int ONSET_TYPE_COL = 25;
		protected int ONSET_SIZE_COL = 26;
		protected int PHONEME_COUNT_COL = 27;
		protected int PAINTE_A1_COL = 28;
		protected int PAINTE_A2_COL = 29;
		protected int PAINTE_B_COL = 30;
		protected int PAINTE_C1_COL = 31;
		protected int PAINTE_C2_COL = 32;
		protected int PAINTE_D_COL = 33;

		public SentenceReaderV03(ReaderControl readerControl) {
			super(readerControl);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.io.DirndlIOUtils.SentenceReader#initColumns()
		 */
		@Override
		protected void initColumns() {
			/*
			 * VERSION 0.3
			 */
			ID_COL = 0;
			FORM_COL = 1;
			POS_COL = 2;
			LEMMA_COL = 3;
			FEATURES_COL = 4;
			HEAD_COL = 5;
			DEPREL_COL = 6;
			SPEAKER_COL = 7;
			SPEAKER_FEATURES_COL = 8;
			ENTITY_COL = 9;
			COREF_COL = 10;
			BEGIN_TS_COL = 11;
			END_TS_COL = 12;
			SYL_OFFSET_COL = 13;
			SYL_LABEL_COL = 14;
			SYL_TIMESTAMP_COL = 15;
			SYL_VOWEL_COL = 16;
			SYL_STRESS_COL = 17;
			SYL_DURATION_COL = 18;
			VOWEL_DURATION_COL = 19;
			SYL_STARTPITCH_COL = 20;
			SYL_MIDPITCH_COL = 21;
			SYL_ENDPITCH_COL = 22;
			CODA_TYPE_COL = 23;
			CODA_SIZE_COL = 24;
			ONSET_TYPE_COL = 25;
			ONSET_SIZE_COL = 26;
			PHONEME_COUNT_COL = 27;
			PAINTE_A1_COL = 28;
			PAINTE_A2_COL = 29;
			PAINTE_B_COL = 30;
			PAINTE_C1_COL = 31;
			PAINTE_C2_COL = 32;
			PAINTE_D_COL = 33;
		}

		protected int[] mapSampa(int index) {
			int[] offsets = EMPTY_INTS;
			int sylCount = result.getSyllableCount(index);
			if(sylCount>0) {
				String[] sampa = (String[]) result.getProperty(index, SYLLABLE_LABEL_KEY);
				String[] labels = readerControl.getSampaMapper().split(forms[index], sampa);
				if(labels!=null) {
					offsets = new int[sylCount];
					int offset = 0;
					for(int k=0; k<sylCount; k++) {
						offsets[k] = offset;
						offset += labels[k].length();
					}
					result.setProperty(index, SYLLABLE_FORM_KEY, labels);
					result.setMapsSyllables(index, true);
				} else {
					boolean report = true;
					if(SENTENCE_BLACKLIST!=null) {
						Object sentNum = result.getProperty(SENTENCE_NUMBER_KEY);
						if(sentNum!=null && SENTENCE_BLACKLIST.contains(sentNum)) {
							report = false;
						}
					}

					if(report) {
						LoggerFactory.info(DirndlIOUtils.class,
								buffer.getErrorMessage("Unable to map /"+Arrays.deepToString(sampa)+"/ to '"+forms[index]+"'")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
			}

			return offsets;
		}

		protected void markTonalProminence(int index) {
			int sylCount = result.getSyllableCount(index);

			int accentExcursion = readerControl.getAccentExcursion();
			boolean onlyConsiderStressedSylables = readerControl.isOnlyConsiderStressedSylables();

			boolean hasTonalProminence = false;
			if(sylCount>0 && accentExcursion!=-1) {
				for(int i=0; i<sylCount; i++) {
					if(onlyConsiderStressedSylables && !result.isSyllableStressed(index, i)) {
						continue;
					}

					if(result.getPainteC1(index, i)>=accentExcursion
							|| result.getPainteC2(index, i)>=accentExcursion) {
						hasTonalProminence = true;
						break;
					}
				}
			}

			result.setProperty(index, TONAL_PROMINENCE_KEY, hasTonalProminence);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.io.DirndlIOUtils.SentenceReader#readAdditionalColumns(int, de.ims.icarus.util.strings.CharTableBuffer.Row)
		 */
		@Override
		protected void readAdditionalColumns(int i, Row row) {
			super.readAdditionalColumns(i, row);


			// Assign properties
			result.setProperty(i, INDEX_KEY, i);
			result.setProperty(i, FORM_KEY, forms[i]);
			result.setProperty(i, POS_KEY, get(row, POS_COL, EMPTY));
			result.setProperty(i, LEMMA_KEY, get(row, LEMMA_COL, EMPTY));
			result.setProperty(i, FEATURES_KEY, get(row, FEATURES_COL, EMPTY));
			result.setProperty(i, HEAD_KEY, Math.max(getInt(row, HEAD_COL, DATA_UNDEFINED_VALUE)-1, DATA_UNDEFINED_VALUE));
			result.setProperty(i, DEPREL_KEY, get(row, DEPREL_COL, EMPTY));
			result.setProperty(i, SPEAKER_KEY, get(row, SPEAKER_COL, EMPTY));
			result.setProperty(i, SPEAKER_FEATURES_KEY, get(row, SPEAKER_FEATURES_COL, EMPTY));
			result.setProperty(i, ENTITY_KEY, get(row, ENTITY_COL, EMPTY));
			result.setProperty(i, BEGIN_TS_KEY, getFloat(row, BEGIN_TS_COL, DATA_UNDEFINED_VALUE));
			result.setProperty(i, END_TS_KEY, getFloat(row, END_TS_COL, DATA_UNDEFINED_VALUE));
			result.setProperty(i, SYLLABLE_LABEL_KEY, getStrings(row, SYL_LABEL_COL));
			result.setProperty(i, SYLLABLE_TIMESTAMP_KEY, getFloats(row, SYL_TIMESTAMP_COL));
			result.setProperty(i, SYLLABLE_VOWEL_KEY, getStrings(row, SYL_VOWEL_COL));

			String[] labels = (String[]) result.getProperty(i, SYLLABLE_LABEL_KEY);
			int sylCount = labels==null ? 0 : labels.length;
			result.setProperty(i, SYLLABLE_COUNT, sylCount);

			int[] offsets = getInts(row, SYL_OFFSET_COL);
			if(offsets==EMPTY_INTS) {
				if(readerControl.getSampaMapper()!=null) {
					offsets = mapSampa(i);
				}
			}
			result.setProperty(i, SYLLABLE_OFFSET_KEY, offsets);

			// Special handling for stressed syllables (can be either a single value or an array)
			int[] stressedIndices = getInts(row, SYL_STRESS_COL);
			boolean[] stress = new boolean[sylCount];
			boolean wordStressed = false;
			for(int index : stressedIndices) {
//				result.setSyllableStressed(i, index, true);
				//FIXME stupid workaround
				stress[index] = true;
				wordStressed = true;
			}
			result.setProperty(i, SYLLABLE_STRESS_KEY, stress);
			result.setProperty(i, STRESS_KEY, wordStressed);

			result.setProperty(i, SYLLABLE_DURATION_KEY, getFloats(row, SYL_DURATION_COL));
			result.setProperty(i, VOWEL_DURATION_KEY, getFloats(row, VOWEL_DURATION_COL));
			result.setProperty(i, SYLLABLE_STARTPITCH_KEY, getFloats(row, SYL_STARTPITCH_COL));
			result.setProperty(i, SYLLABLE_MIDPITCH_KEY, getFloats(row, SYL_MIDPITCH_COL));
			result.setProperty(i, SYLLABLE_ENDPITCH_KEY, getFloats(row, SYL_ENDPITCH_COL));
			result.setProperty(i, CODA_TYPE_KEY, getStrings(row, CODA_TYPE_COL));
			result.setProperty(i, CODA_SIZE_KEY, getInts(row, CODA_SIZE_COL));
			result.setProperty(i, ONSET_TYPE_KEY, getStrings(row, ONSET_TYPE_COL));
			result.setProperty(i, ONSET_SIZE_KEY, getInts(row, ONSET_SIZE_COL));
			result.setProperty(i, PHONEME_COUNT_KEY, getInts(row, PHONEME_COUNT_COL));
			result.setProperty(i, PAINTE_A1_KEY, getFloats(row, PAINTE_A1_COL));
			result.setProperty(i, PAINTE_A2_KEY, getFloats(row, PAINTE_A2_COL));
			result.setProperty(i, PAINTE_B_KEY, getFloats(row, PAINTE_B_COL));
			result.setProperty(i, PAINTE_C1_KEY, getFloats(row, PAINTE_C1_COL));
			result.setProperty(i, PAINTE_C2_KEY, getFloats(row, PAINTE_C2_COL));
			result.setProperty(i, PAINTE_D_KEY, getFloats(row, PAINTE_D_COL));

			if(readerControl.isMarkAccentOnWords()) {
				markTonalProminence(i);
			}
		}
	}

	private static class SentenceReaderV04 extends SentenceReaderV03 {

		protected int IS_LEX_COL = 11;
		protected int IS_REF_COL = 12;

		public SentenceReaderV04(ReaderControl readerControl) {
			super(readerControl);
		}

		@Override
		protected void initColumns() {
			/*
			 * VERSION 0.4
			 */
			ID_COL = 0;
			FORM_COL = 1;
			POS_COL = 2;
			LEMMA_COL = 3;
			FEATURES_COL = 4;
			HEAD_COL = 5;
			DEPREL_COL = 6;
			SPEAKER_COL = 7;
			SPEAKER_FEATURES_COL = 8;
			ENTITY_COL = 9;
			COREF_COL = 10;
			IS_LEX_COL = 11;
			IS_REF_COL = 12;
			BEGIN_TS_COL = 13;
			END_TS_COL = 14;
			SYL_OFFSET_COL = 15;
			SYL_LABEL_COL = 16;
			SYL_TIMESTAMP_COL = 17;
			SYL_VOWEL_COL = 18;
			SYL_STRESS_COL = 19;
			SYL_DURATION_COL = 20;
			VOWEL_DURATION_COL = 21;
			SYL_STARTPITCH_COL = 22;
			SYL_MIDPITCH_COL = 23;
			SYL_ENDPITCH_COL = 24;
			CODA_TYPE_COL = 25;
			CODA_SIZE_COL = 26;
			ONSET_TYPE_COL = 27;
			ONSET_SIZE_COL = 28;
			PHONEME_COUNT_COL = 29;
			PAINTE_A1_COL = 30;
			PAINTE_A2_COL = 31;
			PAINTE_B_COL = 32;
			PAINTE_C1_COL = 33;
			PAINTE_C2_COL = 34;
			PAINTE_D_COL = 35;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.io.DirndlIOUtils.SentenceReaderV03#readAdditionalColumns(int, de.ims.icarus.util.strings.CharTableBuffer.Row)
		 */
		@Override
		protected void readAdditionalColumns(int i, Row row) {
			super.readAdditionalColumns(i, row);

			result.setProperty(i, IS_LEX, get(row, IS_LEX_COL, EMPTY));
			result.setProperty(i, IS_REF, get(row, IS_REF_COL, EMPTY));
		}
	}

	private static class SentenceReaderV05 extends SentenceReaderV04 {

		protected int TOBI_LABEL_COL = 25;
		protected int TOBI_TONE_COL = 27;

		public SentenceReaderV05(ReaderControl readerControl) {
			super(readerControl);
		}

		@Override
		protected void initColumns() {
			super.initColumns();

			/*
			 * VERSION 0.5
			 */
			TOBI_LABEL_COL = 25;
			TOBI_TONE_COL = 27;

			// Reset the captured slots!!!
			CODA_TYPE_COL = -1;
			ONSET_TYPE_COL = -1;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.io.DirndlIOUtils.SentenceReaderV03#readAdditionalColumns(int, de.ims.icarus.util.strings.CharTableBuffer.Row)
		 */
		@Override
		protected void readAdditionalColumns(int i, Row row) {
			super.readAdditionalColumns(i, row);

			result.setProperty(i, TOBI_LABEL, getStrings(row, TOBI_LABEL_COL));
			result.setProperty(i, TOBI_TONE, getStrings(row, TOBI_TONE_COL));
		}
	}
}
