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
package de.ims.icarus.plugins.prosody.search;

import de.ims.icarus.language.dependency.search.DependencyTargetTree;
import de.ims.icarus.plugins.coref.CorefConstants;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.ProsodyUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyTargetTree extends DependencyTargetTree implements ProsodyConstants, CorefConstants {

	/**
	 * @see de.ims.icarus.language.dependency.search.DependencyTargetTree#fetchHead(int)
	 */
	@Override
	protected int fetchHead(int index) {
		int head = getSource().getHead(index);
		if(head>=0 || head==DATA_HEAD_ROOT) {
			return head;
		}
		return index==0 ? DATA_HEAD_ROOT : index-1;
	}

	@Override
	protected boolean supports(Object data) {
		return data instanceof ProsodicSentenceData;
	}

	@Override
	public ProsodicSentenceData getSource() {
		return (ProsodicSentenceData) super.getSource();
	}

	// WORD METHODS

	public Object getProperty(String key) {
		return getSource().getProperty(nodePointer, key);
	}

	public int getSyllableCount() {
		return getSource().getSyllableCount(nodePointer);
	}

	public float getBeginTimestamp() {
		return getSource().getBeginTimestamp(nodePointer);
	}

	public float getEndTimestamp() {
		return getSource().getEndTimestamp(nodePointer);
	}

	public String getSpeaker() {
		return (String) getSource().getProperty(nodePointer, SPEAKER_KEY);
	}

	public String getSpeakerFeatures() {
		return (String) getSource().getProperty(nodePointer, SPEAKER_FEATURES_KEY);
	}

	public String getEntity() {
		return (String) getSource().getProperty(nodePointer, ENTITY_KEY);
	}

	public boolean hasSyllables() {
		return getSource().getSyllableCount(nodePointer)>0;
	}

	// SYLLABLE METHODS

	public Object getProperty(String key, int sylIndex) {
		return getSource().getSyllableProperty(nodePointer, key, sylIndex);
	}

	public int getSyllableOffset(int sylIndex) {
		return getSource().getSyllableOffset(nodePointer, sylIndex);
	}

	public String getSyllableLabel(int sylIndex) {
		return getSource().getSyllableLabel(nodePointer, sylIndex);
	}

	public String getSyllableVowel(int sylIndex) {
		return getSource().getSyllableVowel(nodePointer, sylIndex);
	}

	public String getCodaType(int sylIndex) {
		return getSource().getCodaType(nodePointer, sylIndex);
	}

	public String getOnsetType(int sylIndex) {
		return getSource().getOnsetType(nodePointer, sylIndex);
	}

	public float getSyllableTimestamp(int sylIndex) {
		return getSource().getSyllableTimestamp(nodePointer, sylIndex);
	}

	public float getSyllableDuration(int sylIndex) {
		return getSource().getSyllableDuration(nodePointer, sylIndex);
	}

	public float getVowelDuration(int sylIndex) {
		return getSource().getVowelDuration(nodePointer, sylIndex);
	}

	public float getSyllableStartPitch(int sylIndex) {
		return getSource().getSyllableStartPitch(nodePointer, sylIndex);
	}

	public float getSyllableMidPitch(int sylIndex) {
		return getSource().getSyllableMidPitch(nodePointer, sylIndex);
	}

	public float getSyllableEndPitch(int sylIndex) {
		return getSource().getSyllableEndPitch(nodePointer, sylIndex);
	}

	public float getPainteA1(int sylIndex) {
		return getSource().getPainteA1(nodePointer, sylIndex);
	}

	public float getPainteA2(int sylIndex) {
		return getSource().getPainteA2(nodePointer, sylIndex);
	}

	public float getPainteB(int sylIndex) {
		return getSource().getPainteB(nodePointer, sylIndex);
	}

	public float getPainteC1(int sylIndex) {
		return getSource().getPainteC1(nodePointer, sylIndex);
	}

	public float getPainteC2(int sylIndex) {
		return getSource().getPainteC2(nodePointer, sylIndex);
	}

	public float getPainteD(int sylIndex) {
		return getSource().getPainteD(nodePointer, sylIndex);
	}

	public boolean isSyllableStressed(int sylIndex) {
		return getSource().isSyllableStressed(nodePointer, sylIndex);
	}

	public int getCodaSize(int sylIndex) {
		return getSource().getCodaSize(nodePointer, sylIndex);
	}

	public int getOnsetSize(int sylIndex) {
		return getSource().getOnsetSize(nodePointer, sylIndex);
	}

	public int getPhonemeCount(int sylIndex) {
		return getSource().getPhonemeCount(nodePointer, sylIndex);
	}

	private final StringBuilder buffer = new StringBuilder(100);

	public String getSyllableProperties(String key) {
		buffer.setLength(0);

		ProsodyUtils.appendProperties(buffer, key, getSource(), nodePointer);

		return buffer.toString();
	}
}
