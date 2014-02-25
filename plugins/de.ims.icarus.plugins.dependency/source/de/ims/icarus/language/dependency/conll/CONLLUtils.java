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
package de.ims.icarus.language.dependency.conll;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.language.dependency.SimpleDependencyData;
import de.ims.icarus.util.strings.CharTableBuffer;
import de.ims.icarus.util.strings.CharTableBuffer.Cursor;
import de.ims.icarus.util.strings.CharTableBuffer.Row;
import de.ims.icarus.util.strings.StringPrimitives;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CONLLUtils {


	// column	content
	// 1	id
	// 2	form
	// 3	lemma
	// 4	cpos-tag
	// 5	pos-tog
	// 6	feats
	// 7	head
	// 8	deprel
	public static final int ID06 = 0;
	public static final int FORM06 = 1;
	public static final int LEMMA06 = 2;
	public static final int CPOS06 = 3;
	public static final int POS06 = 4;
	public static final int FEAT06 = 5;
	public static final int HEAD06 = 6;
	public static final int DEPREL06 = 7;

	public static final int ID09 = 0;
	public static final int FORM09 = 1;
	public static final int LEMMA09 = 2;
	public static final int PLEMMA09 = 3;
	public static final int POS09 = 4;
	public static final int PPOS09 = 5;
	public static final int FEAT09 = 6;
	public static final int PFEAT09 = 7;
	public static final int HEAD09 = 8;
	public static final int PHEAD09 = 9;
	public static final int DEPREL09 = 10;
	public static final int PDEPREL09 = 11;
	public static final int FILLPRED09 = 12;
	public static final int PRED09 = 13;

	// APREDs from index 14 on

	// Number of columns of interest (skip APREDs)
	private final static int COL_LIMIT09 = 12;

	private final static int COL_LIMIT06 = 8;

	private static final String US = "_"; //$NON-NLS-1$
	private static final String DELIMITER = "\\s+"; //$NON-NLS-1$
	private static final String EMPTY = ""; //$NON-NLS-1$

	public static DependencyData readGold09(CharTableBuffer buffer, int corpusIndex) {
		if(buffer.isEmpty())
			throw new IllegalArgumentException("No rows to read in buffer"); //$NON-NLS-1$

		int size = buffer.getRowCount();

		short[] heads = new short[size];
		String[] poss = new String[size];
		String[] forms = new String[size];
		String[] lemmas = new String[size];
		String[] features = new String[size];
		String[] relations = new String[size];
		long[] flags = new long[size];

		int index = -1;

		if(index==-1) {
			index = corpusIndex;
		}

		Row row;
		boolean checkIdForIndex = true;

		for(int i=0; i<size; i++) {

			row = buffer.getRow(i);
			if(row.split(DELIMITER, COL_LIMIT09)<COL_LIMIT09)
				throw new IllegalArgumentException("Insufficient columns"); //$NON-NLS-1$

			forms[i] = get(row, FORM09, "<empty>"); //$NON-NLS-1$
			heads[i] = (short) get(row, HEAD09);
			lemmas[i] = get(row, LEMMA09, EMPTY);
			features[i] = get(row, FEAT09, EMPTY);
			poss[i] = get(row, POS09, EMPTY);
			relations[i] = get(row, DEPREL09, EMPTY);
			flags[i] = 0;

			if(index==-1 && checkIdForIndex) {
				Cursor cursor = row.getSplitCursor(ID09);
				int offset = cursor.indexOf('_');
				if(offset>-1 && offset<cursor.length()) {
					index = StringPrimitives.parseInt(cursor, offset+1, -1);
				} else {
					checkIdForIndex = false;
				}
				cursor.recycle();
			}
		}

		DependencyUtils.fillProjectivityFlags(heads, flags);

		return new SimpleDependencyData(index, forms, lemmas, features, poss, relations, heads, flags);
	}

	public static DependencyData readPredicted09(CharTableBuffer buffer, int corpusIndex) {
		if(buffer.isEmpty())
			throw new IllegalArgumentException("No rows to read in buffer"); //$NON-NLS-1$

		int size = buffer.getRowCount();

		short[] heads = new short[size];
		String[] poss = new String[size];
		String[] forms = new String[size];
		String[] lemmas = new String[size];
		String[] features = new String[size];
		String[] relations = new String[size];
		long[] flags = new long[size];

		int index = -1;

		if(index==-1) {
			index = corpusIndex;
		}

		Row row;
		boolean checkIdForIndex = true;

		for(int i=0; i<size; i++) {

			row = buffer.getRow(i);
			if(row.split(DELIMITER, COL_LIMIT09)<COL_LIMIT09)
				throw new IllegalArgumentException("Insufficient columns"); //$NON-NLS-1$

			forms[i] = get(row, FORM09, "<empty>"); //$NON-NLS-1$
			heads[i] = (short) get(row, PHEAD09);
			lemmas[i] = get(row, PLEMMA09, EMPTY);
			features[i] = get(row, PFEAT09, EMPTY);
			poss[i] = get(row, PPOS09, EMPTY);
			relations[i] = get(row, PDEPREL09, EMPTY);
			flags[i] = 0;

			if(index==-1 && checkIdForIndex) {
				Cursor cursor = row.getSplitCursor(ID09);
				int offset = cursor.indexOf('_');
				if(offset>-1 && offset<cursor.length()) {
					index = StringPrimitives.parseInt(cursor, offset+1, -1);
				} else {
					checkIdForIndex = false;
				}
				cursor.recycle();
			}
		}

		DependencyUtils.fillProjectivityFlags(heads, flags);

		return new SimpleDependencyData(index, forms, lemmas, features, poss, relations, heads, flags);
	}

	public static DependencyData read06(CharTableBuffer buffer, int corpusIndex) {
		if(buffer.isEmpty())
			throw new IllegalArgumentException("No rows to read in buffer"); //$NON-NLS-1$

		int size = buffer.getRowCount();

		short[] heads = new short[size];
		String[] poss = new String[size];
		String[] forms = new String[size];
		String[] lemmas = new String[size];
		String[] features = new String[size];
		String[] relations = new String[size];
		long[] flags = new long[size];

		int index = -1;

		if(index==-1) {
			index = corpusIndex;
		}

		Row row;
		boolean checkIdForIndex = true;

		for(int i=0; i<size; i++) {

			row = buffer.getRow(i);
			if(row.split(DELIMITER, COL_LIMIT06)<COL_LIMIT06)
				throw new IllegalArgumentException("Insufficient columns"); //$NON-NLS-1$

			forms[i] = get(row, FORM06, "<empty>"); //$NON-NLS-1$
			heads[i] = (short) get(row, HEAD06);
			lemmas[i] = get(row, LEMMA06, EMPTY);
			features[i] = get(row, FEAT06, EMPTY);
			poss[i] = get(row, POS06, EMPTY);
			relations[i] = get(row, DEPREL06, EMPTY);
			flags[i] = 0;

			if(index==-1 && checkIdForIndex) {
				Cursor cursor = row.getSplitCursor(ID06);
				int offset = cursor.indexOf('_');
				if(offset>-1 && offset<cursor.length()) {
					index = StringPrimitives.parseInt(cursor, offset+1, -1);
				} else {
					checkIdForIndex = false;
				}
				cursor.recycle();
			}
		}

		DependencyUtils.fillProjectivityFlags(heads, flags);

		return new SimpleDependencyData(index, forms, lemmas, features, poss, relations, heads, flags);
	}

	private static int get(Row row, int index) {
		Cursor cursor = row.getSplitCursor(index);

		int value = LanguageConstants.DATA_UNDEFINED_VALUE;

		if(!US.equals(cursor) && !cursor.isEmpty()) {
			value = StringPrimitives.parseInt(cursor)-1;
		}

		cursor.recycle();

		return value;
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
}
