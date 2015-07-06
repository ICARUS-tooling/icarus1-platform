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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus.Core;
import de.ims.icarus.io.IOUtil;
import de.ims.icarus.io.Reader;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.prosody.DefaultProsodicDocumentData;
import de.ims.icarus.plugins.prosody.DefaultProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodicDocumentData;
import de.ims.icarus.plugins.prosody.ProsodicDocumentSet;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.io.UniSylIOUtils.AnnotationLevel;
import de.ims.icarus.plugins.prosody.io.UniSylIOUtils.Column;
import de.ims.icarus.plugins.prosody.io.UniSylIOUtils.ColumnType;
import de.ims.icarus.plugins.prosody.io.UniSylIOUtils.Delimiter;
import de.ims.icarus.plugins.prosody.io.UniSylIOUtils.UniSylConfig;
import de.ims.icarus.plugins.prosody.sampa.SampaMapper2;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.DataCreater;
import de.ims.icarus.util.location.DefaultFileLocation;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;
import de.ims.icarus.util.strings.CharLineBuffer;
import de.ims.icarus.util.strings.Splitable;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class UniSylDocumentReader implements Reader<ProsodicDocumentData>, DataCreater, ProsodyConstants {

	public static void main(String[] args) throws Exception {

		Core.debugInit();

		String path = "D:\\Workspaces\\Default\\Icarus\\data\\prosody\\all-feats-utf8-header.txt"; //$NON-NLS-1$
		Location location = new DefaultFileLocation(Paths.get(path));

		CoreferenceDocumentSet documentSet = new CoreferenceDocumentSet();
		Options options = new Options();
		options.put("documentSet", documentSet); //$NON-NLS-1$

		try(UniSylDocumentReader reader = new UniSylDocumentReader()) {
			CoreferenceUtils.loadDocumentSet(reader, location, options, documentSet);
		}

		System.out.println(documentSet.size());
	}

	private CharLineBuffer buffer;
	private CoreferenceDocumentSet documentSet;

	private UniSylConfig config;

	private boolean needsSylCollector;

	// Utility stuff used during reading
	private Object[] colPayload;
	private AnnotationLevel currentLevel;
	private DefaultProsodicDocumentData document;
	private DefaultProsodicSentenceData sentence;
	private String separator;
	private int wordIndex;

	private SampaMapper2 sampaMapper;
	private Matcher emptyContentMatcher;

	public UniSylDocumentReader() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.io.Reader#init(de.ims.icarus.util.location.Location, de.ims.icarus.util.Options)
	 */
	@Override
	public void init(Location location, Options options) throws IOException,
			UnsupportedLocationException {
		if(location==null)
			throw new NullPointerException("Invalid location"); //$NON-NLS-1$

		documentSet = (CoreferenceDocumentSet) options.get("documentSet"); //$NON-NLS-1$

		buffer = new CharLineBuffer();
		buffer.startReading(IOUtil.getReader(location.openInputStream(), IOUtil.getCharset(options)));

		config = UniSylIOUtils.readConfig(buffer);

		separator = UniSylIOUtils.resolveConstant(config.separator);
		colPayload = new Object[config.columns.length];

		needsSylCollector = config.lineLevel==AnnotationLevel.SYLLABLE;

		for(Column column : config.columns) {
			if(column.ignore()) {
				continue;
			}

			if(needsSylCollector && column.level==AnnotationLevel.SYLLABLE) {
				colPayload[column.index] = new ArrayList<String>();
			}
		}

		if(config.syllableOffsetsFromSampa) {
			if(config.localSampaRulesFile!=null) {
				Path rulesFile = Core.getCore().getDataFolder().resolve(config.localSampaRulesFile);
				if(!Files.exists(rulesFile, LinkOption.NOFOLLOW_LINKS)) {
					LoggerFactory.warning(this, "Missing SAMPA rules file in /data folder: "+config.localSampaRulesFile); //$NON-NLS-1$
				} else {
					sampaMapper = new SampaMapper2(rulesFile.toAbsolutePath().toUri().toURL());
				}
			}


			if(sampaMapper==null){
				sampaMapper = new SampaMapper2();
			}
		}

		if(config.emptyContent!=null) {
			emptyContentMatcher = Pattern.compile(config.emptyContent).matcher(""); //$NON-NLS-1$
		}
	}

	/**
	 * @see de.ims.icarus.io.Reader#close()
	 */
	@Override
	public void close() throws IOException {
		try {
			buffer.close();
		} finally {
			buffer = null;
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

	/**
	 * @see de.ims.icarus.io.Reader#next()
	 */
	@Override
	public ProsodicDocumentData next() throws IOException,
			UnsupportedFormatException {
		if(buffer.isEndOfStream()) {
			return null;
		}

		wordIndex = -1;

		// Buffer is at the correct starting line already

		while(!buffer.isEndOfStream()) {
//			System.out.println("LINE"+(buffer.getLineNumber()+1)+": "+buffer); //$NON-NLS-1$ //$NON-NLS-2$

			if(config.skipEmptyLines && buffer.isEmpty()) {
				buffer.next();
				continue;
			}

			buffer.trim();

			if(buffer.getLineNumber()==59) {
				System.out.println(buffer);
			}

			boolean isHashLine = buffer.startsWith("#"); //$NON-NLS-1$
			if(!isHashLine || !tryReadProperty()) {

				Arrays.fill(delimiterResults, 0);
//				Arrays.fill(lineActions, 0);

				int currentLine = buffer.getLineNumber();
				boolean isDataLine = !tryRawDelimiters() && !isHashLine;

				if(buffer.getLineNumber()>currentLine) {
					isDataLine = true;
					buffer.trim();
				}

				if(isDataLine) {
					int colCount = buffer.split(separator);
					if(colCount!=config.columns.length)
						throw new IllegalStateException("Insufficient number of data columns encountered at line "+(buffer.getLineNumber()+1) //$NON-NLS-1$
								+" (expected "+config.columns.length+" - got "+colCount+"): "+buffer); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

					tryColDelimiters();
				}

				scanDelimiterResults(false);

				if(isDataLine) {
					pushColumnData();
				}
			}

			// Next line
			buffer.next();
		}

		Arrays.fill(delimiterResults, 0);
		scanDelimiterResults(true);

		// Always return null since we read the entire data set in one go!
		return null;
	}

	private boolean tryReadProperty() {
		if(currentLevel==null) {
			return false;
		}

		int sepIdx = buffer.indexOf('=');

		if(sepIdx==-1) {
			return false;
		}

		String key = buffer.substring(1, sepIdx);
		String value = buffer.substring(sepIdx+1);

		assignProperty(currentLevel, key, value);

		return true;
	}

	private static final AnnotationLevel[] levels = AnnotationLevel.values();
//	static {
//		CollectionUtils.reverse(levels, 0, levels.length);
//		System.out.println(Arrays.deepToString(levels));
//	}
	private final int[] delimiterResults = new int[levels.length];

//	private final int[] lineActions = new int[levels.length];

	/**
	 * Returns {@code true} in case a delimiter requested the end or start of a member
	 */
	private boolean tryRawDelimiters() {

		boolean changeDetected = false;

		for(int i=0; i<levels.length; i++) {
			Delimiter delimiter = config.rawDelimiters.get(levels[i]);
			if(delimiter==null) {
				continue;
			}
			delimiterResults[i] |= delimiter.checkLine(buffer);

			changeDetected |= UniSylIOUtils.isBeginElement(delimiterResults[i])
					|| UniSylIOUtils.isEndElement(delimiterResults[i]);
		}

		return changeDetected;
	}

	private void tryColDelimiters() {

		for(int i=0; i<levels.length; i++) {
			Delimiter delimiter = config.colDelimiters.get(levels[i]);
			if(delimiter==null) {
				continue;
			}
			delimiterResults[i] |= delimiter.checkLine(buffer);
		}
	}

	private boolean isEmptyContent(CharSequence s) {
		if(emptyContentMatcher==null) {
			return false;
		} else {
			emptyContentMatcher.reset(s);
			boolean result = emptyContentMatcher.matches();
			emptyContentMatcher.reset();
			return result;
		}
	}

	private void scanDelimiterResults(boolean forceEnd) {

//		// End bottom-up
//		for(int i=levels.length-1; i>=0; i--) {
//			if(UniSylIOUtils.isEndElement(delimiterResults[i])) {
//				endCurrent(levels[i]);
//			}
//		}
//
//		// Start top-down
//		for(int i=0; i<levels.length; i++) {
//			if(UniSylIOUtils.isBeginElement(delimiterResults[i])) {
//				startNew(levels[i]);
//			}
//		}

		// END STUFF

		boolean endDoc = forceEnd || UniSylIOUtils.isEndElement(delimiterResults[AnnotationLevel.DOCUMENT.ordinal()]);
		boolean endSent = forceEnd || endDoc || UniSylIOUtils.isEndElement(delimiterResults[AnnotationLevel.SENTENCE.ordinal()]);
		boolean endWord = forceEnd || endSent || UniSylIOUtils.isEndElement(delimiterResults[AnnotationLevel.WORD.ordinal()]);

		if(endWord && sentence!=null) {
			if(needsSylCollector) {
				int sylCount = 0;
				for(Column column : config.columns) {
					if(column.level!=AnnotationLevel.SYLLABLE) {
						continue;
					}

					@SuppressWarnings("unchecked")
					List<String> list = (List<String>) colPayload[column.index];
					sylCount = list.size();
					Object buffer = column.type.createSylBuffer(sylCount);
					for(int i=0; i<sylCount; i++) {
						try {
							column.type.parseAndSet(buffer, i, list.get(i));
						} catch(NumberFormatException e) {
							throw new IllegalStateException("Invalid content in column "+column, e); //$NON-NLS-1$
						}
					}
					list.clear();
					sentence.setProperty(wordIndex, column.property, buffer);
				}

				sentence.setProperty(wordIndex, SYLLABLE_COUNT, sylCount);
			}

			currentLevel = AnnotationLevel.SENTENCE;
		}

		if(endSent && sentence!=null) {
			finalizeSentence();

			sentence.setDocument(document);
			document.add(sentence);
			sentence = null;

			currentLevel = AnnotationLevel.DOCUMENT;
		}

		if(endDoc && document!=null) {
			finalizeDocument();

			documentSet.add(document);
			document = null;

			currentLevel = AnnotationLevel.DOCUMENT_SET;
		}

		// START STUFF

		boolean startWord = UniSylIOUtils.isBeginElement(delimiterResults[AnnotationLevel.WORD.ordinal()]);
		boolean startSent = startWord || UniSylIOUtils.isBeginElement(delimiterResults[AnnotationLevel.SENTENCE.ordinal()]);
		boolean startDoc = startSent || UniSylIOUtils.isBeginElement(delimiterResults[AnnotationLevel.DOCUMENT.ordinal()]);


		if(startDoc && document==null) {
			document = new DefaultProsodicDocumentData(documentSet, documentSet.size());
			currentLevel = AnnotationLevel.DOCUMENT;
		}

		if(startSent && sentence==null) {
			sentence = new DefaultProsodicSentenceData();
			currentLevel = AnnotationLevel.SENTENCE;

			wordIndex = -1;
		}

		if(startWord && sentence!=null) {
			wordIndex++;
			currentLevel = AnnotationLevel.WORD;
		}
	}

	private void finalizeDocument() {
		if(document.getId()==null) {
			Object id = document.getProperty(DOCUMENT_ID);
			if(id==null) {
				id = "doc_"+document.getDocumentIndex(); //$NON-NLS-1$
			}
			document.setId(String.valueOf(id));
		}

		if(config.createCorefStructure) {
			createCorefStructure();
		}
	}

	private void finalizeSentence() {
		// Create 'forms' array
		String[] forms = new String[wordIndex+1];
		for(int i=0; i<forms.length; i++) {
			Object form = sentence.getProperty(i, FORM_KEY);
			if(form==null) {
				form = ""; //$NON-NLS-1$
			}
			forms[i] = String.valueOf(form);

			// Check for sampa mapping
			int[] offsets = (int[]) sentence.getProperty(i, SYLLABLE_OFFSET_KEY);
			if((offsets==null || offsets.length==0) && config.syllableOffsetsFromSampa) {
				offsets = mapSampa(i, forms[i]);
				sentence.setProperty(i, SYLLABLE_OFFSET_KEY, offsets);
			}

			// Check for word accent markers
			if(config.markAccentOnWords) {
				markTonalProminence(i);
			}

			if(config.adjustDependencyHeads) {
				int head = sentence.getHead(i);
				sentence.setProperty(i, HEAD_KEY, Math.max(head-1, DATA_UNDEFINED_VALUE));
			}
		}
		sentence.setForms(forms);
	}

	private void assignProperty(AnnotationLevel level, String key, Object value) {
		if(level==null) {
			return;
		}

		switch (level) {
		case DOCUMENT_SET:
			documentSet.setProperty(key, value);
			break;
		case DOCUMENT:
			document.setProperty(key, value);
			break;
		case SENTENCE:
			sentence.setProperty(key, value);
			break;
		case WORD:
			sentence.setProperty(wordIndex, key, value);
			break;

		default:
			throw new IllegalStateException("Cannot assign property to level: "+currentLevel); //$NON-NLS-1$
		}
	}

	private void pushColumnData() {
		for(Column column : config.columns) {
			if(column.ignore()) {
				continue;
			}

			ColumnType type = column.type;
			Splitable cursor = buffer.getSplitCursor(column.index);

			if(column.isAggregator()) {

				Object array;

				if(isEmptyContent(cursor)) {
					array = type.emptySylBuffer();
				} else {
					int sylCount = cursor.split(column.separator);
					array = type.createSylBuffer(sylCount);
					for(int i=0; i<sylCount; i++) {
						Splitable part = cursor.getSplitCursor(i);
						type.parseAndSet(array, i, part);
						part.recycle();
					}
					sentence.setProperty(wordIndex, SYLLABLE_COUNT, sylCount);
				}

				sentence.setProperty(wordIndex, column.property, array);
			} else {

				switch (column.level) {
				case SYLLABLE: {
					@SuppressWarnings("unchecked")
					List<String> list = (List<String>)colPayload[column.index];
					list.add(cursor.toString());
				} break;

				default:
					Object value = isEmptyContent(cursor) ? type.emptyValue() : type.parse(cursor);
					assignProperty(column.level, column.property, value);
					break;
				}
			}

			cursor.recycle();
		}
	}

	private static int[] EMPTY_INTS = {};

	private int[] mapSampa(int index, String word) {
		int[] offsets = EMPTY_INTS;
		int sylCount = sentence.getSyllableCount(index);
		if(sylCount>0) {
			String[] sampa = (String[]) sentence.getProperty(index, SYLLABLE_LABEL_KEY);
			String[] labels = sampaMapper.split(word, sampa);
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
						"Unable to map /"+Arrays.deepToString(sampa)+"/ to '"+word+"'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}

		return offsets;
	}

	private void markTonalProminence(int index) {
		int sylCount = sentence.getSyllableCount(index);

		boolean hasTonalProminence = false;
		if(sylCount>0 && config.accentExcursion!=-1) {
			for(int i=0; i<sylCount; i++) {
				if(config.onlyConsiderStressedSylables && !sentence.isSyllableStressed(index, i)) {
					continue;
				}

				if(sentence.getPainteC1(index, i)>=config.accentExcursion
						|| sentence.getPainteC2(index, i)>=config.accentExcursion) {
					hasTonalProminence = true;
					break;
				}
			}
		}

		sentence.setProperty(index, TONAL_PROMINENCE_KEY, hasTonalProminence);
	}

	private void createCorefStructure() {

	}
}
