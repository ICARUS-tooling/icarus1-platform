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
		int size = input.forms.length - 1;

		int[] heads = new int[size];
		String[] poss = new String[size];
		String[] forms = new String[size];
		String[] lemmas = new String[size];
		String[] features = new String[size];
		String[] relations = new String[size];
		long[] flags = new long[size];
		
		int iSource = skipRoot ? 1 : 0;
		
		for(int i=0; i<size; i++) {

			forms[i] = ensureDummy(input.forms[iSource], "<empty>"); //$NON-NLS-1$
			heads[i] = input.heads[iSource] - 1;
			lemmas[i] = ensureValid(input.lemmas[iSource]);
			features[i] = ensureValid(input.ofeats[iSource]);
			poss[i] = ensureValid(input.gpos[iSource]);
			relations[i] = ensureValid(input.labels[iSource]);
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
		int size = input.forms.length - 1;

		int[] heads = new int[size];
		String[] poss = new String[size];
		String[] forms = new String[size];
		String[] lemmas = new String[size];
		String[] features = new String[size];
		String[] relations = new String[size];
		long[] flags = new long[size];
		
		int iSource = skipRoot ? 1 : 0;
		
		for(int i=0; i<size; i++) {

			forms[i] = ensureDummy(input.forms[iSource], "<empty>"); //$NON-NLS-1$
			heads[i] = input.pheads[iSource] - 1;
			lemmas[i] = ensureValid(input.plemmas[iSource]);
			features[i] = ensureValid(input.pfeats[iSource]);
			poss[i] = ensureValid(input.ppos[iSource]);
			relations[i] = ensureValid(input.plabels[iSource]);
			flags[i] = 0;
			
			iSource = i++;
		}
		
		if(inferProjectivityFlags) {
			DependencyUtils.fillProjectivityFlags(heads, flags);
		}
		
		return new SimpleDependencyData(forms, lemmas, features, 
				poss, relations, heads, flags);
	}
	
	public static String ensureValid(String input) {
		return input==null ? "" : input; //$NON-NLS-1$
	}
	
	
	public static String ensureDummy(String input, String dummy) {
		return input==null ? dummy : input;
	}
}
