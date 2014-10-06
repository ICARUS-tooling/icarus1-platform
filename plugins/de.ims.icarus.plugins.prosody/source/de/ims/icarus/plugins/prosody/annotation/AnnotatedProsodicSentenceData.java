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
package de.ims.icarus.plugins.prosody.annotation;

import de.ims.icarus.language.Grammar;
import de.ims.icarus.language.annotation.AnnotatedSentenceData;
import de.ims.icarus.language.coref.CorefProperties;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.plugins.prosody.ProsodicDocumentData;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.util.annotation.Annotation;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotatedProsodicSentenceData implements ProsodicSentenceData, AnnotatedSentenceData {

	private static final long serialVersionUID = -8819152695167807937L;

	private final ProsodicSentenceData source;
	private Annotation annotation;

	public AnnotatedProsodicSentenceData(ProsodicSentenceData source, Annotation annotation) {
		if(source==null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$

		this.source = source;
		this.annotation = annotation;
	}

	public AnnotatedProsodicSentenceData(ProsodicSentenceData source) {
		this(source, null);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return source.toString();
	}

	@Override
	public AnnotatedProsodicSentenceData clone() {
		return new AnnotatedProsodicSentenceData(source, annotation);
	}

	/**
	 * @see de.ims.icarus.language.dependency.DependencyData#getForm(int)
	 */
	@Override
	public String getForm(int index) {
		return source.getForm(index);
	}

	/**
	 * @see de.ims.icarus.language.annotation.AnnotatedSentenceData#getAnnotation()
	 */
	@Override
	public ProsodicAnnotation getAnnotation() {
		return (ProsodicAnnotation) annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}

	@Override
	public ProsodicDocumentData getDocument() {
		return source.getDocument();
	}

	@Override
	public Object getSyllableProperty(int index, String key, int sylIndex) {
		return source.getSyllableProperty(index, key, sylIndex);
	}

	@Override
	public String getText() {
		return source.getText();
	}

	@Override
	public float getBeginTimestamp(int index) {
		return source.getBeginTimestamp(index);
	}

	@Override
	public float getEndTimestamp(int index) {
		return source.getEndTimestamp(index);
	}

	@Override
	public int getSyllableCount(int index) {
		return source.getSyllableCount(index);
	}

	@Override
	public boolean isMapsSyllables() {
		return source.isMapsSyllables();
	}

	@Override
	public int getSyllableOffset(int index, int syllable) {
		return source.getSyllableOffset(index, syllable);
	}

	@Override
	public String getPos(int index) {
		return source.getPos(index);
	}

	@Override
	public String getRelation(int index) {
		return source.getRelation(index);
	}

	@Override
	public String getSyllableLabel(int index, int syllable) {
		return source.getSyllableLabel(index, syllable);
	}

	@Override
	public String getLemma(int index) {
		return source.getLemma(index);
	}

	@Override
	public int getIndex() {
		return source.getIndex();
	}

	@Override
	public float getSyllableTimestamp(int index, int syllable) {
		return source.getSyllableTimestamp(index, syllable);
	}

	@Override
	public String getFeatures(int index) {
		return source.getFeatures(index);
	}

	@Override
	public int getHead(int index) {
		return source.getHead(index);
	}

	@Override
	public String getSyllableVowel(int index, int syllable) {
		return source.getSyllableVowel(index, syllable);
	}

	@Override
	public boolean isFlagSet(int index, long flag) {
		return source.isFlagSet(index, flag);
	}

	@Override
	public boolean isSyllableStressed(int index, int syllable) {
		return source.isSyllableStressed(index, syllable);
	}

	@Override
	public float getSyllableDuration(int index, int syllable) {
		return source.getSyllableDuration(index, syllable);
	}

	@Override
	public float getVowelDuration(int index, int syllable) {
		return source.getVowelDuration(index, syllable);
	}

	@Override
	public Object getProperty(String key) {
		return source.getProperty(key);
	}

	@Override
	public float getSyllableStartPitch(int index, int syllable) {
		return source.getSyllableStartPitch(index, syllable);
	}

	@Override
	public long getFlags(int index) {
		return source.getFlags(index);
	}

	@Override
	public float getSyllableMidPitch(int index, int syllable) {
		return source.getSyllableMidPitch(index, syllable);
	}

	@Override
	public Object getProperty(int index, String key) {
		return source.getProperty(index, key);
	}

	@Override
	public float getSyllableEndPitch(int index, int syllable) {
		return source.getSyllableEndPitch(index, syllable);
	}

	@Override
	public boolean isEmpty() {
		return source.isEmpty();
	}

	@Override
	public String getCodaType(int index, int syllable) {
		return source.getCodaType(index, syllable);
	}

	@Override
	public int getCodaSize(int index, int syllable) {
		return source.getCodaSize(index, syllable);
	}

	@Override
	public Span[] getSpans() {
		return source.getSpans();
	}

	@Override
	public String getOnsetType(int index, int syllable) {
		return source.getOnsetType(index, syllable);
	}

	@Override
	public int getOnsetSize(int index, int syllable) {
		return source.getOnsetSize(index, syllable);
	}

	@Override
	public int getPhonemeCount(int index, int syllable) {
		return source.getPhonemeCount(index, syllable);
	}

	@Override
	public CorefProperties getProperties() {
		return source.getProperties();
	}

	@Override
	public float getPainteA1(int index, int syllable) {
		return source.getPainteA1(index, syllable);
	}

	@Override
	public float getPainteA2(int index, int syllable) {
		return source.getPainteA2(index, syllable);
	}

	@Override
	public int length() {
		return source.length();
	}

	@Override
	public float getPainteB(int index, int syllable) {
		return source.getPainteB(index, syllable);
	}

	@Override
	public float getPainteC1(int index, int syllable) {
		return source.getPainteC1(index, syllable);
	}

	@Override
	public float getPainteC2(int index, int syllable) {
		return source.getPainteC2(index, syllable);
	}

	@Override
	public Grammar getSourceGrammar() {
		return source.getSourceGrammar();
	}

	@Override
	public float getPainteD(int index, int syllable) {
		return source.getPainteD(index, syllable);
	}
}
