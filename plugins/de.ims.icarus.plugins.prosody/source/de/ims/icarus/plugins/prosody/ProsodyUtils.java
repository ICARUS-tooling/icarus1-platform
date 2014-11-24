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
package de.ims.icarus.plugins.prosody;

import java.awt.Cursor;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotation;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyUtils implements ProsodyConstants {

//	private static Cursor speakerCursor;

	public static Cursor getSpeakerCursor() {
//		if(speakerCursor==null) {
//			URL url = ProsodyUtils.class.getResource("speaker.png"); //$NON-NLS-1$
//			ImageIcon source = new ImageIcon(url);
//
//			Dimension size = Toolkit.getDefaultToolkit().getBestCursorSize(0, 0);
//
//			BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
//			Graphics2D graphics = image.createGraphics();
//			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//			graphics.setColor(new Color(0, true));
//			graphics.fillRect(0, 0, size.width, size.height);
//			graphics.drawImage(source.getImage(), 0, 0, null);
//
//			speakerCursor = Toolkit.getDefaultToolkit().createCustomCursor(
//					image, new Point(0, 0), "speaker"); //$NON-NLS-1$
//		}
//		return speakerCursor;

		return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	}

	public static ContentType getProsodyDocumentContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(ProsodicDocumentData.class);
	}

	public static ContentType getProsodySentenceContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(ProsodicSentenceData.class);
	}

	public static ContentType getProsodyAnnotationType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(ProsodicAnnotation.class);
	}

	public static boolean isNumberToken(String word) {
		return word!=null && !word.isEmpty() && Character.isDigit(word.charAt(0));
	}

	private static final String[] defaultWordPropertyKeys = {
		FORM_KEY,
		POS_KEY,
		LEMMA_KEY,
		BEGIN_TS_KEY,
		DEPREL_KEY,
		END_TS_KEY,
		ENTITY_KEY,
		FEATURES_KEY,
		HEAD_KEY,
		SYLLABLE_COUNT,
		ACCENT_KEY,
		STRESS_KEY,
		PHONEME_COUNT_KEY,
		SPEAKER_KEY,
		SPEAKER_FEATURES_KEY,
		IS_LEX,
		IS_REF,
	};

	public static String[] getDefaultWordPropertyKeys() {
		return defaultWordPropertyKeys.clone();
	}

	private static final String[] defaultSyllablePropertyKeys = {
		SYLLABLE_DURATION_KEY,
		SYLLABLE_ENDPITCH_KEY,
		SYLLABLE_LABEL_KEY,
		SYLLABLE_FORM_KEY,
		SYLLABLE_MIDPITCH_KEY,
		SYLLABLE_OFFSET_KEY,
		SYLLABLE_STARTPITCH_KEY,
		SYLLABLE_STRESS_KEY,
		SYLLABLE_TIMESTAMP_KEY,
		SYLLABLE_VOWEL_KEY,
		VOWEL_DURATION_KEY,
		INDEX_KEY,
		CODA_SIZE_KEY,
		CODA_TYPE_KEY,
		ONSET_SIZE_KEY,
		ONSET_TYPE_KEY,
		PAINTE_A1_KEY,
		PAINTE_A2_KEY,
		PAINTE_B_KEY,
		PAINTE_C1_KEY,
		PAINTE_C2_KEY,
		PAINTE_D_KEY,
	};

	public static String[] getDefaultSyllablePropertyKeys() {
		return defaultSyllablePropertyKeys.clone();
	}

	private static final String[] defaultNumericalSyllablePropertyKeys = {
		SYLLABLE_DURATION_KEY,
		SYLLABLE_ENDPITCH_KEY,
		SYLLABLE_MIDPITCH_KEY,
		SYLLABLE_OFFSET_KEY,
		SYLLABLE_STARTPITCH_KEY,
		SYLLABLE_TIMESTAMP_KEY,
		VOWEL_DURATION_KEY,
		INDEX_KEY,
		CODA_SIZE_KEY,
		ONSET_SIZE_KEY,
		PAINTE_A1_KEY,
		PAINTE_A2_KEY,
		PAINTE_B_KEY,
		PAINTE_C1_KEY,
		PAINTE_C2_KEY,
		PAINTE_D_KEY,
	};

	public static String[] getDefaultNumericalSyllablePropertyKeys() {
		return defaultNumericalSyllablePropertyKeys.clone();
	}

	private static final String[] defaultSentencePropertyKeys = {
		SENTENCE_NUMBER_KEY,
		INDEX_KEY,
	};

	public static String[] getDefaultSentencePropertyKeys() {
		return defaultSentencePropertyKeys.clone();
	}

	private static final String[] defaultDocumentPropertyKeys = {
		DOCUMENT_ID,
		AUDIO_FILE_KEY,
		AUDIO_OFFSET_KEY,
		INDEX_KEY,
	};

	public static String[] getDefaultDocumentPropertyKeys() {
		return defaultDocumentPropertyKeys.clone();
	}

	private static final char defaultDelimiter = ' ';

	public static void appendProperties(StringBuilder buffer, String key, ProsodicSentenceData sentence, int wordIndex) {

		for(int i=0; i<=sentence.getSyllableCount(wordIndex); i++) {
			if(i>0) {
				buffer.append(defaultDelimiter);
			}
			buffer.append(sentence.getSyllableProperty(wordIndex, key, i));
		}
	}

	public static String getAccentShapeLabel(int value) {
		switch (value) {
		case ACCENT_SHAPE_RISE_VALUE:
			return ACCENT_SHAPE_RISE_LABEL;
		case ACCENT_SHAPE_FALL_VALUE:
			return ACCENT_SHAPE_FALL_LABEL;
		case ACCENT_SHAPE_RISE_FALL_VALUE:
			return ACCENT_SHAPE_RISE_FALL_LABEL;

		default:
			return LanguageConstants.DATA_UNDEFINED_LABEL;
		}
	}

	public static int parseAccentShapeLabel(String label) {
		if(ACCENT_SHAPE_RISE_LABEL.equals(label)) {
			return ACCENT_SHAPE_RISE_VALUE;
		} else if(ACCENT_SHAPE_FALL_LABEL.equals(label)) {
			return ACCENT_SHAPE_FALL_VALUE;
		} else if(ACCENT_SHAPE_RISE_FALL_LABEL.equals(label)) {
			return ACCENT_SHAPE_RISE_FALL_VALUE;
		} else {
			return LanguageConstants.DATA_UNDEFINED_VALUE;
		}
	}

	public static String getPaIntEChannelLabel(int value) {
		switch (value) {
		case PAINTE_CHANNEL_ABOVE_VALUE:
			return PAINTE_CHANNEL_ABOVE_LABEL;
		case PAINTE_CHANNEL_BELOW_VALUE:
			return PAINTE_CHANNEL_BELOW_LABEL;
		case PAINTE_CHANNEL_INSIDE_VALUE:
			return PAINTE_CHANNEL_INSIDE_LABEL;
		case PAINTE_CHANNEL_CROSSING_VALUE:
			return PAINTE_CHANNEL_CROSSING_LABEL;

		default:
			return LanguageConstants.DATA_UNDEFINED_LABEL;
		}
	}

	public static int parsePaIntEChannelLabel(String label) {
		if(PAINTE_CHANNEL_ABOVE_LABEL.equals(label)) {
			return PAINTE_CHANNEL_ABOVE_VALUE;
		} else if(PAINTE_CHANNEL_BELOW_LABEL.equals(label)) {
			return PAINTE_CHANNEL_BELOW_VALUE;
		} else if(PAINTE_CHANNEL_INSIDE_LABEL.equals(label)) {
			return PAINTE_CHANNEL_INSIDE_VALUE;
		} else if(PAINTE_CHANNEL_CROSSING_LABEL.equals(label)) {
			return PAINTE_CHANNEL_CROSSING_VALUE;
		} else {
			return LanguageConstants.DATA_UNDEFINED_VALUE;
		}
	}
}
