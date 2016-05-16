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

import java.lang.reflect.Array;

import de.ims.icarus.language.coref.DocumentData;
import de.ims.icarus.language.coref.DefaultCoreferenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultProsodicSentenceData extends DefaultCoreferenceData implements ProsodicSentenceData {

	private static final long serialVersionUID = 649080115736671895L;

	/**
	 * @param document
	 * @param forms
	 */
	public DefaultProsodicSentenceData(DocumentData document,
			String[] forms) {
		super(document, forms);
	}

	public DefaultProsodicSentenceData(String[] forms) {
		super(forms);
	}

	public DefaultProsodicSentenceData() {
		super();
	}

	@Override
	public DefaultProsodicSentenceData clone() {
		return (DefaultProsodicSentenceData)super.clone();
	}

	/**
	 * @see de.ims.icarus.language.coref.DefaultCoreferenceData#getDocument()
	 */
	@Override
	public ProsodicDocumentData getDocument() {
		return (ProsodicDocumentData) super.getDocument();
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getPos(int)
	 */
	@Override
	public String getPos(int index) {
		return (String) getProperty(index, POS_KEY);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getRelation(int)
	 */
	@Override
	public String getRelation(int index) {
		return (String) getProperty(index, DEPREL_KEY);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getLemma(int)
	 */
	@Override
	public String getLemma(int index) {
		return (String) getProperty(index, LEMMA_KEY);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getFeatures(int)
	 */
	@Override
	public String getFeatures(int index) {
		return (String) getProperty(index, FEATURES_KEY);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getHead(int)
	 */
	@Override
	public int getHead(int index) {
		Object value = getProperty(index, HEAD_KEY);
		return value==null ? DATA_UNDEFINED_VALUE : (int) value;
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#isFlagSet(int, long)
	 */
	@Override
	public boolean isFlagSet(int index, long flag) {
		return (getFlags(index) & flag) == flag;
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getFlags(int)
	 */
	@Override
	public long getFlags(int index) {
		Object value = getProperty(index, FLAGS_KEY);
		return value==null ? 0L : (long)value;
	}

	public void setFlag(int index, long mask, boolean active) {
		long flags = getFlags(index);
		if(active) {
			flags |= mask;
		} else {
			flags &= ~mask;
		}
		setFlags(index, flags);
	}

	public void setFlags(int index, long flags) {
		setProperty(index, FLAGS_KEY, flags);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getSyllableProperty(int, java.lang.String, int)
	 */
	@Override
	public Object getSyllableProperty(int index, String key, int sylIndex) {
		switch (key) {
		case INDEX_KEY:
			return sylIndex;

		case SIZE_KEY:
		case LENGTH_KEY:
			String label = getSyllableForm(index, sylIndex);
			return label==null ? 0 : label.length();

		case PAINTE_MAX_C_KEY:
			return (double)Math.max(getPainteC1(index, sylIndex), getPainteC2(index, sylIndex));

		default:
			Object array = getIndexedProperty(index, key);
			return (array==null || !array.getClass().isArray() || Array.getLength(array)==0) ?
					null : Array.get(array, sylIndex);
		}
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getBeginTimestamp(int)
	 */
	@Override
	public float getBeginTimestamp(int index) {
		Object value = getIndexedProperty(index, BEGIN_TS_KEY);
		return value==null ? DATA_UNDEFINED_VALUE : (float)value;
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getEndTimestamp(int)
	 */
	@Override
	public float getEndTimestamp(int index) {
		Object value = getIndexedProperty(index, END_TS_KEY);
		return value==null ? DATA_UNDEFINED_VALUE : (float)value;
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getSyllableCount(int)
	 */
	@Override
	public int getSyllableCount(int index) {
		//TODO change to a single consistent strategy on storing syllable count!
//		if(isMapsSyllables(index)) {
//			int[] value = (int[])getIndexedProperty(index, SYLLABLE_OFFSET_KEY);
//			return value==null ? 0 : value.length;
//		} else {
//			String[] value = (String[])getIndexedProperty(index, SYLLABLE_LABEL_KEY);
//			return value==null ? 0 : value.length;
//		}
		Object count = getProperty(index, SYLLABLE_COUNT);
		return count==null ? 0 : (int) count;
	}

	private int getSyllableIntProperty(int index, String key, int syllable) {
		int[] value = (int[])getIndexedProperty(index, key);
		return value==null ? DATA_UNDEFINED_VALUE : value[syllable];
	}

	private float getSyllableFloatProperty(int index, String key, int syllable) {
		float[] value = (float[])getIndexedProperty(index, key);
		return value==null ? DATA_UNDEFINED_VALUE : value[syllable];
	}

	private String getSyllableStringProperty(int index, String key, int syllable) {
		String[] value = (String[])getIndexedProperty(index, key);
		return value==null ? null : value[syllable];
	}

	private boolean getSyllableBooleanProperty(int index, String key, int syllable) {
		boolean[] value = (boolean[])getIndexedProperty(index, key);
		return value==null ? false : value[syllable];
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getSyllableOffset(int, int)
	 */
	@Override
	public int getSyllableOffset(int index, int syllable) {
		return getSyllableIntProperty(index, SYLLABLE_OFFSET_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getSyllableLabel(int, int)
	 */
	@Override
	public String getSyllableLabel(int index, int syllable) {
		return getSyllableStringProperty(index, SYLLABLE_LABEL_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getSyllableForm(int, int)
	 */
	@Override
	public String getSyllableForm(int index, int syllable) {
		return getSyllableStringProperty(index, SYLLABLE_FORM_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getSyllableTimestamp(int, int)
	 */
	@Override
	public float getSyllableTimestamp(int index, int syllable) {
		return getSyllableFloatProperty(index, SYLLABLE_TIMESTAMP_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getSyllableVowel(int, int)
	 */
	@Override
	public String getSyllableVowel(int index, int syllable) {
		return getSyllableStringProperty(index, SYLLABLE_VOWEL_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#isSyllableStressed(int, int)
	 */
	@Override
	public boolean isSyllableStressed(int index, int syllable) {
//		Object value = getIndexedProperty(index, SYLLABLE_STRESS_KEY);
//		if(value==null) {
//			return false;
//		}
//
//		int mask = 1<<syllable;
//
//		return ((int)value & mask) == mask;

		return getSyllableBooleanProperty(index, SYLLABLE_STRESS_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getSyllableDuration(int, int)
	 */
	@Override
	public float getSyllableDuration(int index, int syllable) {
		return getSyllableFloatProperty(index, SYLLABLE_DURATION_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getVowelDuration(int, int)
	 */
	@Override
	public float getVowelDuration(int index, int syllable) {
		return getSyllableFloatProperty(index, VOWEL_DURATION_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getSyllableStartPitch(int, int)
	 */
	@Override
	public float getSyllableStartPitch(int index, int syllable) {
		return getSyllableFloatProperty(index, SYLLABLE_STARTPITCH_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getSyllableMidPitch(int, int)
	 */
	@Override
	public float getSyllableMidPitch(int index, int syllable) {
		return getSyllableFloatProperty(index, SYLLABLE_MIDPITCH_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getSyllableEndPitch(int, int)
	 */
	@Override
	public float getSyllableEndPitch(int index, int syllable) {
		return getSyllableFloatProperty(index, SYLLABLE_ENDPITCH_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getCodaType(int, int)
	 */
	@Override
	public String getCodaType(int index, int syllable) {
		return getSyllableStringProperty(index, CODA_TYPE_KEY, syllable);
	}

	/**
	 * @return the mapsSyllables
	 */
	@Override
	public boolean isMapsSyllables(int index) {
		return isFlagSet(index, FLAG_MAPS_SYLLABLES);
	}

	/**
	 * @param mapsSyllables the mapsSyllables to set
	 */
	public void setMapsSyllables(int index, boolean mapsSyllables) {
		setFlag(index, FLAG_MAPS_SYLLABLES, mapsSyllables);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getCodaSize(int, int)
	 */
	@Override
	public int getCodaSize(int index, int syllable) {
		return getSyllableIntProperty(index, CODA_SIZE_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getOnsetType(int, int)
	 */
	@Override
	public String getOnsetType(int index, int syllable) {
		return getSyllableStringProperty(index, ONSET_TYPE_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getOnsetSize(int, int)
	 */
	@Override
	public int getOnsetSize(int index, int syllable) {
		return getSyllableIntProperty(index, ONSET_SIZE_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getPhonemeCount(int, int)
	 */
	@Override
	public int getPhonemeCount(int index, int syllable) {
		return getSyllableIntProperty(index, PHONEME_COUNT_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getPainteA1(int, int)
	 */
	@Override
	public float getPainteA1(int index, int syllable) {
		return getSyllableFloatProperty(index, PAINTE_A1_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getPainteA2(int, int)
	 */
	@Override
	public float getPainteA2(int index, int syllable) {
		return getSyllableFloatProperty(index, PAINTE_A2_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getPainteB(int, int)
	 */
	@Override
	public float getPainteB(int index, int syllable) {
		return getSyllableFloatProperty(index, PAINTE_B_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getPainteC1(int, int)
	 */
	@Override
	public float getPainteC1(int index, int syllable) {
		return getSyllableFloatProperty(index, PAINTE_C1_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getPainteC2(int, int)
	 */
	@Override
	public float getPainteC2(int index, int syllable) {
		return getSyllableFloatProperty(index, PAINTE_C2_KEY, syllable);
	}

	/**
	 * @see de.ims.icarus.plugins.prosody.ProsodicSentenceData#getPainteD(int, int)
	 */
	@Override
	public float getPainteD(int index, int syllable) {
		return getSyllableFloatProperty(index, PAINTE_D_KEY, syllable);
	}

//	public void setSyllableStressed(int index, int syllable, boolean stressed) {
//		Object value = getIndexedProperty(index, SYLLABLE_STRESS_KEY);
//		int current;
//		if(value==null) {
//			current = 0;
//		} else {
//			current = (int) value;
//		}
//
//		int mask = 1<<syllable;
//
//		if(stressed) {
//			current |= mask;
//		} else {
//			current &= ~mask;
//		}
//
//		value = current;
//
//		setProperty(index, SYLLABLE_STRESS_KEY, value);
//	}

//	private static class Key {
//		public String key;
//		public int index;
//
//		public Key() {
//			// no-op
//		}
//
//		public Key(String key, int index) {
//			this.key = key;
//			this.index = index;
//		}
//
//		public Key(Key source) {
//			key = source.key;
//			index = source.index;
//		}
//
//		@Override
//		public Key clone() {
//			return new Key(this);
//		}
//
//		/**
//		 * @see java.lang.Object#hashCode()
//		 */
//		@Override
//		public int hashCode() {
//			return key.hashCode() * (1+index);
//		}
//		/**
//		 * @see java.lang.Object#equals(java.lang.Object)
//		 */
//		@Override
//		public boolean equals(Object obj) {
//			if(obj instanceof Key) {
//				Key other = (Key) obj;
//				return index==other.index && key.equals(other.key);
//			}
//			return false;
//		}
//		/**
//		 * @see java.lang.Object#toString()
//		 */
//		@Override
//		public String toString() {
//			return "Key:"+key+"["+index+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//		}
//	}
}
