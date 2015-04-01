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
package de.ims.icarus.plugins.prosody.io;

import static de.ims.icarus.util.strings.StringUtil.intern;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ims.icarus.Core;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.io.IOUtil;
import de.ims.icarus.io.Reader;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.prosody.DefaultProsodicDocumentData;
import de.ims.icarus.plugins.prosody.DefaultProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodicDocumentData;
import de.ims.icarus.plugins.prosody.ProsodicDocumentSet;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.sampa.SampaMapper2;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.DataCreater;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FestivalDocumentReader implements Reader<ProsodicDocumentData>, DataCreater, ProsodyConstants {

	private BufferedReader reader;
	private CoreferenceDocumentSet documentSet;

	private boolean syllableOffsetsFromSampa;
	private boolean markAccentOnWords = false;
	private boolean onlyConsiderStressedSylables = false;
	private int accentExcursion = -1;

	private SampaMapper2 sampaMapper;

	private static final String DELIMITER = " "; //$NON-NLS-1$

	/*
	 * lisp_painte_a1;painte_acc_a1
	 * lisp_painte_a2;painte_acc_a2
	 * lisp_painte_b;painte_acc_b
	 * lisp_painte_c1;painte_acc_c1
	 * lisp_painte_c2;painte_acc_c2
	 * lisp_painte_d;painte_acc_d
	 * lisp_painte_name;painte_name
	 * stress;lexical_stress
	 * n.stress;next_syl_lexical_stress
	 * p.stress;prev_syl_lexical_stress
	 * lisp_last_stress;number_syl_last_stress
	 * lisp_next_stress;number_syl_next_stress
	 * lisp_utt_id;file
	 * name;syl_name
	 * syllable_start;syl_start
	 * syllable_end;syl_end
	 * syllable_duration;syl_duration
	 * syl_endpitch;syl_endpitch
	 * syl_midpitch;syl_midpitch
	 * syl_startpitch;syl_startpitch
	 * pos_in_word;position_syl_in_word
	 * position_type;position_type_syl
	 * syl_vowel;syl_vowel
	 * lisp_dur_nucleus;syl_vowel_duration
	 * syl_coda_type;coda_type
	 * syl_codasize;coda_size
	 * syl_onset_type;onset_type
	 * syl_onsetsize;onset_size
	 * R:SylStructure.parent.name;word
	 * R:SylStructure.parent.word_duration;word_duration
	 * R:SylStructure.parent.word_numsyls;number_syls_in_word
	 * R:SylStructure.parent.word_end;word_end
	 * R:SylStructure.parent.word_start;word_start
	 */

	private static final int COL_PAINTE_A1 = 0;
	private static final int COL_PAINTE_A2 = 1;
	private static final int COL_PAINTE_B = 2;
	private static final int COL_PAINTE_C1 = 3;
	private static final int COL_PAINTE_C2 = 4;
	private static final int COL_PAINTE_D = 5;
	private static final int COL_PAINTE_NAME = 6;
	private static final int COL_SYL_STRESS = 7;
	private static final int COL_SYL_NEXT_LEX_STRESS = 8;
	private static final int COL_SYL_PREV_LEX_STRESS = 9;
	private static final int COL_SYL_LAST_STRESS = 10;
	private static final int COL_SYL_NEXT_STRESS = 11;
	private static final int COL_FILE = 12;
	private static final int COL_NAME = 13;
	private static final int COL_SYL_BEGIN_TS = 14;
	private static final int COL_SYL_END_TS = 15;
	private static final int COL_SYL_DURATION = 16;
	private static final int COL_SYL_ENDPITCH = 17;
	private static final int COL_SYL_MIDPITCH = 18;
	private static final int COL_SYL_STARTPITCH = 19;
	private static final int COL_SYL_POSITION = 20;
	private static final int COL_SYL_POSITION_TYPE = 21;
	private static final int COL_SYL_VOWEL = 22;
	private static final int COL_SYL_VOWEL_DURATION = 23;
	private static final int COL_SYL_CODA_TYPE = 24;
	private static final int COL_SYL_CODA_SIZE = 25;
	private static final int COL_SYL_ONSET_TYPE = 26;
	private static final int COL_SYL_ONSET_SIZE = 27;
	private static final int COL_WORD_FORM = 28;
	private static final int COL_WORD_DURATION = 29;
	private static final int COL_WORD_SYL_COUNT = 30;
	private static final int COL_WORD_END_TS = 31;
	private static final int COL_WORD_BEGIN_TS = 32;


	/**
	 * @see de.ims.icarus.io.Reader#init(de.ims.icarus.util.location.Location, de.ims.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {
		if(location==null)
			throw new NullPointerException("Invalid location"); //$NON-NLS-1$

		documentSet = (CoreferenceDocumentSet) options.get("documentSet"); //$NON-NLS-1$

		reader = IOUtil.getReader(location.openInputStream(), IOUtil.getCharset(options));


		syllableOffsetsFromSampa = ProsodyIOUtils.DEFAULT_SYLLABLES_FROM_SAMPA;
		markAccentOnWords = ProsodyIOUtils.DEFAULT_MARK_ACCENTS;
		accentExcursion = -1;
		if(!Core.isDebugActive()) {
			syllableOffsetsFromSampa = ConfigRegistry.getGlobalRegistry().getBoolean(
					"plugins.prosody.festivalReader.syllableOffsetsFromSampa"); //$NON-NLS-1$
			markAccentOnWords = ConfigRegistry.getGlobalRegistry().getBoolean(
					"plugins.prosody.festivalReader.markAccentOnWords"); //$NON-NLS-1$
			accentExcursion = ConfigRegistry.getGlobalRegistry().getInteger(
					"plugins.prosody.festivalReader.accentExcursion"); //$NON-NLS-1$
			onlyConsiderStressedSylables = ConfigRegistry.getGlobalRegistry().getBoolean(
					"plugins.prosody.festivalReader.onlyConsiderStressedSylables"); //$NON-NLS-1$
		}
	}

	public SampaMapper2 getSampaMapper() {
		if(sampaMapper==null && syllableOffsetsFromSampa) {
			//TODO provide multiple files as options?
			sampaMapper = new SampaMapper2();
		}

		return sampaMapper;
	}

	private static boolean parseBit(String s) {
		return "1".equals(s); //$NON-NLS-1$
	}

	private DefaultProsodicSentenceData pendingSentence;

	private DefaultProsodicDocumentData newDocument(String documentId) {
		DefaultProsodicDocumentData document = new DefaultProsodicDocumentData(documentSet, documentSet.size());
		document.setId(documentId);
		document.setProperty(AUDIO_FILE_KEY, documentId+".wav"); //$NON-NLS-1$

		return document;
	}

	private void addSentenceToDoc(DefaultProsodicDocumentData document, DefaultProsodicSentenceData sentence) {
		sentence.setSentenceIndex(document.size());
		sentence.setDocument(document);

		document.add(sentence);
	}

	/**
	 * @see de.ims.icarus.io.Reader#next()
	 */
	@Override
	public ProsodicDocumentData next() throws IOException,
			UnsupportedFormatException {

		DefaultProsodicDocumentData document = null;
		String documentId = null;;

		if(pendingSentence!=null) {
			documentId = (String) pendingSentence.getProperty(DOCUMENT_ID);
			document = newDocument(documentId);
			addSentenceToDoc(document, pendingSentence);
			pendingSentence = null;
		}

		DefaultProsodicSentenceData sentence;

		while((sentence = readSentence()) !=null) {
			String declaredDocId = (String) sentence.getProperty(DOCUMENT_ID);
			if(documentId==null) {
				documentId = declaredDocId;
			} else if(!documentId.equals(declaredDocId)) {
				pendingSentence = sentence;
				break;
			}

			if(document==null) {
				document = newDocument(documentId);
			}

			addSentenceToDoc(document, sentence);
		}

		return document;
	}

	private List<String[]> lines = new ArrayList<>(100);
	private List<String> words = new ArrayList<>(100);
	private TIntList sylCounts = new TIntArrayList(100);

	protected DefaultProsodicSentenceData readSentence() throws IOException {
		String line = null;
		String wordBegin = null;

		lines.clear();
		words.clear();
		sylCounts.clear();

		boolean foundBegin = false;

		while((line = reader.readLine()) != null) {

			if(line.trim().isEmpty()) {
				if(foundBegin) {
					break;
				} else {
					continue;
				}
			}

			foundBegin = true;

			String[] items = line.split(DELIMITER);

			lines.add(items);

			if(wordBegin==null || !wordBegin.equals(items[COL_WORD_BEGIN_TS])) {
				wordBegin = items[COL_WORD_BEGIN_TS];
				words.add(items[COL_WORD_FORM]);
				sylCounts.add(parseInt(items[COL_WORD_SYL_COUNT]));

			}
		}

		if(lines.isEmpty()) {
			return null;
		}

		String[] forms = new String[words.size()];
		words.toArray(forms);

		DefaultProsodicSentenceData sentence = new DefaultProsodicSentenceData(forms);
		String documentId = null;

		int idx = 0;
		String[] items;

		for(int wordIndex = 0; wordIndex<words.size(); wordIndex++) {
			items = lines.get(idx);

			int sylCount = sylCounts.get(wordIndex);

			float[] painte_a1 = new float[sylCount];
			float[] painte_a2 = new float[sylCount];
			float[] painte_b = new float[sylCount];
			float[] painte_c1 = new float[sylCount];
			float[] painte_c2 = new float[sylCount];
			float[] painte_d = new float[sylCount];

			boolean[] syl_stress = new boolean[sylCount];
			boolean[] syl_next_lex_stress = new boolean[sylCount];
			boolean[] syl_prev_lex_stress = new boolean[sylCount];
			boolean[] syl_last_stress = new boolean[sylCount];
			boolean[] syl_next_stress = new boolean[sylCount];
			float[] syl_begin_ts = new float[sylCount];
			float[] syl_end_ts = new float[sylCount];
			float[] syl_duration = new float[sylCount];
			float[] syl_endpitch = new float[sylCount];
			float[] syl_midpitch = new float[sylCount];
			float[] syl_startpitch = new float[sylCount];

			String[] position_type = new String[sylCount];
			String[] syl_label = new String[sylCount];

			String[] syl_vowel = new String[sylCount];
			float[] syl_vowel_duration = new float[sylCount];
			String[] syl_coda_type = new String[sylCount];
			int[] syl_coda_size = new int[sylCount];
			String[] syl_onset_type = new String[sylCount];
			int[] syl_onset_size = new int[sylCount];

			for(int sylIndex = 0; sylIndex<sylCount; sylIndex++) {
				items = lines.get(idx);

				painte_a1[sylIndex] = parseFloat(items[COL_PAINTE_A1]);
				painte_a2[sylIndex] = parseFloat(items[COL_PAINTE_A2]);
				painte_b[sylIndex] = parseFloat(items[COL_PAINTE_B]);
				painte_c1[sylIndex] = parseFloat(items[COL_PAINTE_C1]);
				painte_c2[sylIndex] = parseFloat(items[COL_PAINTE_C2]);
				painte_d[sylIndex] = parseFloat(items[COL_PAINTE_D]);

				syl_stress[sylIndex] = parseBit(items[COL_SYL_STRESS]);
				syl_next_lex_stress[sylIndex] = parseBit(items[COL_SYL_NEXT_LEX_STRESS]);
				syl_prev_lex_stress[sylIndex] = parseBit(items[COL_SYL_PREV_LEX_STRESS]);
				syl_last_stress[sylIndex] = parseBit(items[COL_SYL_LAST_STRESS]);
				syl_next_stress[sylIndex] = parseBit(items[COL_SYL_NEXT_STRESS]);

				syl_begin_ts[sylIndex] = parseFloat(items[COL_SYL_BEGIN_TS]);
				syl_end_ts[sylIndex] = parseFloat(items[COL_SYL_END_TS]);
				syl_duration[sylIndex] = parseFloat(items[COL_SYL_DURATION]);
				syl_endpitch[sylIndex] = parseFloat(items[COL_SYL_ENDPITCH]);
				syl_midpitch[sylIndex] = parseFloat(items[COL_SYL_MIDPITCH]);
				syl_startpitch[sylIndex] = parseFloat(items[COL_SYL_STARTPITCH]);

				position_type[sylIndex] = intern(items[COL_SYL_POSITION_TYPE]);
				syl_label[sylIndex] = intern(items[COL_NAME]);

				syl_vowel[sylIndex] = intern(items[COL_SYL_VOWEL]);
				syl_vowel_duration[sylIndex] = parseFloat(items[COL_SYL_VOWEL_DURATION]);
				syl_coda_type[sylIndex] = intern(items[COL_SYL_CODA_TYPE]);
				syl_coda_size[sylIndex] = parseInt(items[COL_SYL_CODA_SIZE]);
				syl_onset_type[sylIndex] = intern(items[COL_SYL_ONSET_TYPE]);
				syl_onset_size[sylIndex] = parseInt(items[COL_SYL_ONSET_SIZE]);

				idx ++;
			}

			// Syllable properties
			sentence.setProperty(wordIndex, PAINTE_A1_KEY, painte_a1);
			sentence.setProperty(wordIndex, PAINTE_A2_KEY, painte_a2);
			sentence.setProperty(wordIndex, PAINTE_B_KEY, painte_b);
			sentence.setProperty(wordIndex, PAINTE_C1_KEY, painte_c1);
			sentence.setProperty(wordIndex, PAINTE_C2_KEY, painte_c2);
			sentence.setProperty(wordIndex, PAINTE_D_KEY, painte_d);

			sentence.setProperty(wordIndex, SYLLABLE_STRESS_KEY, syl_stress);
			sentence.setProperty(wordIndex, NEXT_SYL_LEXICAL_STRESS, syl_next_lex_stress);
			sentence.setProperty(wordIndex, PREV_SYL_LEXICAL_STRESS, syl_prev_lex_stress);
			sentence.setProperty(wordIndex, NUM_SYL_LAST_STRESS, syl_last_stress);
			sentence.setProperty(wordIndex, NUM_SYL_NEXT_STRESS, syl_next_stress);

			sentence.setProperty(wordIndex, SYLLABLE_TIMESTAMP_KEY, syl_begin_ts);
			sentence.setProperty(wordIndex, SYLLABLE_BEGIN_TS_KEY, syl_begin_ts);
			sentence.setProperty(wordIndex, SYLLABLE_END_TS_KEY, syl_end_ts);
			sentence.setProperty(wordIndex, SYLLABLE_DURATION_KEY, syl_duration);
			sentence.setProperty(wordIndex, SYLLABLE_ENDPITCH_KEY, syl_endpitch);
			sentence.setProperty(wordIndex, SYLLABLE_MIDPITCH_KEY, syl_midpitch);
			sentence.setProperty(wordIndex, SYLLABLE_STARTPITCH_KEY, syl_startpitch);

			sentence.setProperty(wordIndex, SYLLABLE_POSITION_TYPE, position_type);
			sentence.setProperty(wordIndex, SYLLABLE_LABEL_KEY, syl_label);

			sentence.setProperty(wordIndex, SYLLABLE_VOWEL_KEY, syl_vowel);
			sentence.setProperty(wordIndex, VOWEL_DURATION_KEY, syl_vowel_duration);
			sentence.setProperty(wordIndex, CODA_TYPE_KEY, syl_coda_type);
			sentence.setProperty(wordIndex, CODA_SIZE_KEY, syl_coda_size);
			sentence.setProperty(wordIndex, ONSET_TYPE_KEY, syl_onset_type);
			sentence.setProperty(wordIndex, ONSET_SIZE_KEY, syl_onset_size);

			// Word properties
			sentence.setProperty(wordIndex, INDEX_KEY, wordIndex);
			sentence.setProperty(wordIndex, FORM_KEY, intern(forms[wordIndex]));
			sentence.setProperty(wordIndex, SYLLABLE_COUNT, sylCount);
			sentence.setProperty(wordIndex, BEGIN_TS_KEY, parseFloat(items[COL_WORD_BEGIN_TS]));
			sentence.setProperty(wordIndex, END_TS_KEY, parseFloat(items[COL_WORD_END_TS]));

			if(documentId==null) {
				documentId = items[COL_FILE];
			}

			// Post processing
			if(syllableOffsetsFromSampa) {
				mapSampa(sentence, wordIndex);
			}

			if(markAccentOnWords) {
				markTonalProminence(sentence, wordIndex);
			}
		}

		sentence.setProperty(DOCUMENT_ID, documentId);

//		lines.clear();
//		words.clear();
//		sylCounts.clear();

		return sentence;
	}

	private static final float[] EMPTY_FLOATS = new float[0];
	private static final int[] EMPTY_INTS = new int[0];
	private static final String[] EMPTY_STRINGS = new String[0];

	protected int[] mapSampa(DefaultProsodicSentenceData sentence, int index) {
		int[] offsets = EMPTY_INTS;
		int sylCount = sentence.getSyllableCount(index);
		if(sylCount>0) {
			String[] sampa = (String[]) sentence.getProperty(index, SYLLABLE_LABEL_KEY);
			String[] labels = getSampaMapper().split(words.get(index), sampa);
			if(labels!=null) {
				offsets = new int[sylCount];
				int offset = 0;
				for(int k=0; k<sylCount; k++) {
					offsets[k] = offset;
					offset += labels[k].length();
				}
				sentence.setProperty(index, SYLLABLE_FORM_KEY, labels);
				sentence.setMapsSyllables(index, true);
			} else {
				LoggerFactory.info(ProsodyIOUtils.class,
						"Unable to map /"+Arrays.deepToString(sampa)+"/ to '"+words.get(index)+"'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}

		return offsets;
	}

	protected void markTonalProminence(DefaultProsodicSentenceData sentence, int index) {
		int sylCount = sentence.getSyllableCount(index);

		boolean hasTonalProminence = false;
		if(sylCount>0 && accentExcursion!=-1) {
			for(int i=0; i<sylCount; i++) {
				if(onlyConsiderStressedSylables && !sentence.isSyllableStressed(index, i)) {
					continue;
				}

				if(sentence.getPainteC1(index, i)>=accentExcursion
						|| sentence.getPainteC2(index, i)>=accentExcursion) {
					hasTonalProminence = true;
					break;
				}
			}
		}

		sentence.setProperty(index, TONAL_PROMINENCE_KEY, hasTonalProminence);
	}

	/**
	 * @see de.ims.icarus.io.Reader#close()
	 */
	@Override
	public void close() throws IOException {
		try {
			reader.close();
		} finally {
			reader = null;
		}
	}

	/**
	 * @see de.ims.icarus.io.Reader#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return ProsodyUtils.getProsodyDocumentContentType();
	}

	/**
	 * @see de.ims.icarus.util.data.DataCreater#create()
	 */
	@Override
	public Object create() {
		return new ProsodicDocumentSet();
	}

}
