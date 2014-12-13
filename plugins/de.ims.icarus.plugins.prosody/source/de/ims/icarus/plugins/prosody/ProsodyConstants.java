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

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.coref.CoreferenceDocumentData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ProsodyConstants extends LanguageConstants {

	// Plugin ID
	public static final String PROSODY_PLUGIN_ID =
			"de.ims.icarus.prosody"; //$NON-NLS-1$

	// Prosody Perspective Id
	public static final String PROSODY_PERSPECTIVE_ID =
			PROSODY_PLUGIN_ID+"@ProsodyPerspective"; //$NON-NLS-1$

	// PaIntE-Editor View Id
	public static final String PAINTE_EDITOR_VIEW_ID =
			PROSODY_PLUGIN_ID+"@PaIntEEditorView"; //$NON-NLS-1$

	// Document properties
	public static final String DOCUMENT_ID = CoreferenceDocumentData.DOCUMENT_ID_PROPERTY;
	public static final String AUDIO_FILE_KEY = "audio-file"; //$NON-NLS-1$
	public static final String AUDIO_OFFSET_KEY = "audio-offset"; //$NON-NLS-1$

	// Sentence properties
	public static final String SENTENCE_NUMBER_KEY = "sent-num"; //$NON-NLS-1$

	// Flags
	public static final int FLAG_MAPS_SYLLABLES = (1 << 2);

	// Word properties
	public static final String TONAL_PROMINENCE_KEY = "tonal_prominence"; //$NON-NLS-1$
	public static final String STRESS_KEY = "stress"; //$NON-NLS-1$

	public static final String SYLLABLE_COUNT = "syllable_count"; //$NON-NLS-1$

	public static final String BEGIN_TS_KEY = "begin_timestamp"; //$NON-NLS-1$
	public static final String END_TS_KEY = "end_timestamp"; //$NON-NLS-1$

	public static final String IS_LEX = "is_lex"; //$NON-NLS-1$
	public static final String IS_REF = "is_ref"; //$NON-NLS-1$

	// Syllable properties
	public static final String SYLLABLE_OFFSET_KEY = "syllable_offset"; //$NON-NLS-1$
	public static final String SYLLABLE_FORM_KEY = "syllable_form"; //$NON-NLS-1$
	public static final String SYLLABLE_LABEL_KEY = "syllable_label"; //$NON-NLS-1$
	public static final String SYLLABLE_TIMESTAMP_KEY = "syllable_timestamp"; //$NON-NLS-1$
	public static final String SYLLABLE_VOWEL_KEY = "syllable_vowel"; //$NON-NLS-1$
	public static final String SYLLABLE_STRESS_KEY = "syllable_stress"; //$NON-NLS-1$
	public static final String SYLLABLE_DURATION_KEY = "syllable_duration"; //$NON-NLS-1$
	public static final String VOWEL_DURATION_KEY = "vowel_duration"; //$NON-NLS-1$
	public static final String SYLLABLE_STARTPITCH_KEY = "syllable_startpitch"; //$NON-NLS-1$
	public static final String SYLLABLE_MIDPITCH_KEY = "syllable_midpitch"; //$NON-NLS-1$
	public static final String SYLLABLE_ENDPITCH_KEY = "syllable_endpitch"; //$NON-NLS-1$
	public static final String CODA_TYPE_KEY = "coda_type"; //$NON-NLS-1$
	public static final String CODA_SIZE_KEY = "coda_size"; //$NON-NLS-1$
	public static final String ONSET_TYPE_KEY = "onset_type"; //$NON-NLS-1$
	public static final String ONSET_SIZE_KEY = "onset_size"; //$NON-NLS-1$
	public static final String PHONEME_COUNT_KEY = "phoneme_count"; //$NON-NLS-1$
	public static final String PAINTE_A1_KEY = "painte_a1"; //$NON-NLS-1$
	public static final String PAINTE_A2_KEY = "painte_a2"; //$NON-NLS-1$
	public static final String PAINTE_B_KEY = "painte_b"; //$NON-NLS-1$
	public static final String PAINTE_C1_KEY = "painte_c1"; //$NON-NLS-1$
	public static final String PAINTE_C2_KEY = "painte_c2"; //$NON-NLS-1$
	public static final String PAINTE_D_KEY = "painte_d"; //$NON-NLS-1$
	public static final String PAINTE_MAX_C_KEY = "painte_max_c"; //$NON-NLS-1$

	// Accent shape constants
	public static final int ACCENT_SHAPE_RISE_VALUE = 1;
	public static final int ACCENT_SHAPE_FALL_VALUE = 2;
	public static final int ACCENT_SHAPE_RISE_FALL_VALUE = 3;

	public static final String ACCENT_SHAPE_RISE_LABEL = "Rise"; //$NON-NLS-1$
	public static final String ACCENT_SHAPE_FALL_LABEL = "Fall"; //$NON-NLS-1$
	public static final String ACCENT_SHAPE_RISE_FALL_LABEL = "Rise-Fall"; //$NON-NLS-1$
	public static final String ACCENT_SHAPE_UNACCENTED_LABEL = "Unaccented"; //$NON-NLS-1$

	// Painte channel constants
	public static final int PAINTE_CHANNEL_INSIDE_VALUE = 1;
	public static final int PAINTE_CHANNEL_ABOVE_VALUE = 2;
	public static final int PAINTE_CHANNEL_BELOW_VALUE = 3;
	public static final int PAINTE_CHANNEL_CROSSING_VALUE = 4;

	public static final String PAINTE_CHANNEL_INSIDE_LABEL = "Inside"; //$NON-NLS-1$
	public static final String PAINTE_CHANNEL_ABOVE_LABEL = "Above"; //$NON-NLS-1$
	public static final String PAINTE_CHANNEL_BELOW_LABEL = "Below"; //$NON-NLS-1$
	public static final String PAINTE_CHANNEL_CROSSING_LABEL = "Crossing"; //$NON-NLS-1$
	public static final String PAINTE_CHANNEL_UNACCENTED_LABEL = "Unaccented"; //$NON-NLS-1$
}
