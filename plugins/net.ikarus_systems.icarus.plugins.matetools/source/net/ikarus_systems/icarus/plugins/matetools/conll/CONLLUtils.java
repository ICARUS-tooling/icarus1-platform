/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.matetools.conll;

import is2.data.SentenceData09;
import net.ikarus_systems.icarus.language.LanguageUtils;
import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.language.dependency.SimpleDependencyData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CONLLUtils {

	private CONLLUtils() {
		// no-op
	}

	public static DependencyData readGold(SentenceData09 input, boolean skipRoot, boolean inferProjectivityFlags) {
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
		
		return new SimpleDependencyData(forms, lemmas, features, 
				poss, relations, heads, flags);
	}

	public static DependencyData readPredicted(SentenceData09 input, boolean skipRoot, boolean inferProjectivityFlags) {
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
		
		return new SimpleDependencyData(forms, lemmas, features, 
				poss, relations, heads, flags);
	}
	
	private static int get(int[] vals, int index) {
		return vals==null ? LanguageUtils.DATA_UNDEFINED_VALUE : vals[index]-1;
	}
	
	private static String get(String[] vals, int index, String def) {
		String v = vals==null ? def : vals[index];
		return v==null ? def : v;
	}
	
	public static String ensureValid(String input) {
		return input==null ? "" : input; //$NON-NLS-1$
	}
	
	
	public static String ensureDummy(String input, String dummy) {
		return input==null ? dummy : input;
	}
}
