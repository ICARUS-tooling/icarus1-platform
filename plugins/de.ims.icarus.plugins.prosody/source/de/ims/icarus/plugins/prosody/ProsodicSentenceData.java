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

import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.dependency.DependencyData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ProsodicSentenceData extends DependencyData, CoreferenceData, ProsodyConstants {

	float getBeginTimestamp(int index);
	float getEndTimestamp(int index);

	int getSyllableCount(int index);

	int getSyllableOffset(int index, int syllable);
	String getSyllableLabel(int index, int syllable);
	float getSyllableTimestamp(int index, int syllable);
	String getSyllableVowel(int index, int syllable);
	boolean isSyllableStressed(int index, int syllable);
	float getSyllableDuration(int index, int syllable);
	float getVowelDuration(int index, int syllable);
	float getSyllableStartPitch(int index, int syllable);
	float getSyllableMidPitch(int index, int syllable);
	float getSyllableEndPitch(int index, int syllable);
	String getCodaType(int index, int syllable);
	int getCodaSize(int index, int syllable);
	String getOnsetType(int index, int syllable);
	int getOnsetSize(int index, int syllable);
	int getPhonemeCount(int index, int syllable);
	float getPainteA1(int index, int syllable);
	float getPainteA2(int index, int syllable);
	float getPainteB(int index, int syllable);
	float getPainteC1(int index, int syllable);
	float getPainteC2(int index, int syllable);
	float getPainteD(int index, int syllable);
}
