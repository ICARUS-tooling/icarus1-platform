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
package de.ims.icarus.plugins.matetools.conll;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.dependency.DependencySentenceData;
import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.language.dependency.SimpleDependencyData;
import de.ims.icarus.util.strings.StringUtil;
import is2.data.SentenceData09;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class MatetoolsCONLLUtils {

	//DEBUG
	private static final boolean intern = true;

	private MatetoolsCONLLUtils() {
		// no-op
	}

	public static DependencySentenceData readGold(SentenceData09 input, int corpusIndex, boolean skipRoot, boolean inferProjectivityFlags) {
		int size = input.forms.length;
		if(skipRoot) {
			size--;
		}

		short[] heads = new short[size];
		String[] poss = new String[size];
		String[] forms = new String[size];
		String[] lemmas = new String[size];
		String[] features = new String[size];
		String[] relations = new String[size];
		long[] flags = new long[size];

		int iSource = skipRoot ? 1 : 0;
		String def = ""; //$NON-NLS-1$

		for(int i=0; i<size; i++) {

			forms[i] = get(input.forms, iSource, "<empty>"); //$NON-NLS-1$
			heads[i] = (short) get(input.heads, iSource);
			lemmas[i] = get(input.lemmas, iSource, def);
			features[i] = get(input.ofeats, iSource, def);
			poss[i] = get(input.gpos, iSource, def);
			relations[i] = get(input.labels, iSource, def);
			flags[i] = 0;

			iSource++;
		}

		if(inferProjectivityFlags) {
			DependencyUtils.fillProjectivityFlags(heads, flags);
		}

		int index = -1;
		if(input.id!=null && input.id.length>1) {
			String id = input.id[1];
			int idx = id==null ? -1 : id.indexOf('_');
			if(idx!=-1) {
				index = Integer.parseInt(id.substring(0, idx))-1;
			}
		}
		if(index==-1) {
			index = corpusIndex;
		}

		return new SimpleDependencyData(index, forms, lemmas, features,
				poss, relations, heads, flags);
	}

	public static DependencySentenceData readPredicted(SentenceData09 input, int corpusIndex, boolean skipRoot, boolean inferProjectivityFlags) {
		int size = input.forms.length;
		if(skipRoot) {
			size--;
		}

		short[] heads = new short[size];
		String[] poss = new String[size];
		String[] forms = new String[size];
		String[] lemmas = new String[size];
		String[] features = new String[size];
		String[] relations = new String[size];
		long[] flags = new long[size];

		int iSource = skipRoot ? 1 : 0;

		String def = ""; //$NON-NLS-1$

		for(int i=0; i<size; i++) {

			forms[i] = get(input.forms, iSource, "<empty>"); //$NON-NLS-1$
			heads[i] = (short) get(input.pheads, iSource);
			lemmas[i] = get(input.plemmas, iSource, def);
			features[i] = get(input.pfeats, iSource, def);
			poss[i] = get(input.ppos, iSource, def);
			relations[i] = get(input.plabels, iSource, def);
			flags[i] = 0;

			iSource++;
		}

		if(inferProjectivityFlags) {
			DependencyUtils.fillProjectivityFlags(heads, flags);
		}

		int index = -1;
		if(input.id!=null && input.id.length>1) {
			String id = input.id[1];
			int idx = id==null ? -1 : id.indexOf('_');
			if(idx!=-1) {
				index = Integer.parseInt(id.substring(0, idx))-1;
			}
		}
		if(index==-1) {
			index = corpusIndex;
		}

		return new SimpleDependencyData(index, forms, lemmas, features,
				poss, relations, heads, flags);
	}

	private static int get(int[] vals, int index) {
		return vals==null ? LanguageConstants.DATA_UNDEFINED_VALUE : vals[index]-1;
	}

	private static String get(String[] vals, int index, String def) {
		String v = vals==null ? def : vals[index];
		if(v==null) {
			v = def;
		} else if(intern) {
			v = StringUtil.intern(v);
		}
		return v;
	}

	public static String ensureValid(String input) {
		return input==null ? "" : input; //$NON-NLS-1$
	}


	public static String ensureDummy(String input, String dummy) {
		return input==null ? dummy : input;
	}
}
