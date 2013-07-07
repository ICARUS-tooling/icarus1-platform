/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.treebank;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankImportResult {
	
	private List<TreebankInfo> unavailableTreebanks;
	private Map<TreebankInfo, TreebankDescriptor> availabeTreebank;

	TreebankImportResult() {
		// no-op
	}

	void addUnavailable(TreebankInfo info) {
		if(unavailableTreebanks==null) {
			unavailableTreebanks = new ArrayList<>();
		}
		unavailableTreebanks.add(info);
	}
	
	void addAvailable(TreebankInfo info, TreebankDescriptor descriptor) {
		if(availabeTreebank==null) {
			availabeTreebank = new LinkedHashMap<>();
		}
		availabeTreebank.put(info, descriptor);
	}

	/**
	 * @return the unavailableTreebanks
	 */
	public List<TreebankInfo> getUnavailableTreebanks() {
		return unavailableTreebanks;
	}
	
	public int getUnavailableTreebankCount() {
		return unavailableTreebanks==null ? 0 : unavailableTreebanks.size();
	}

	/**
	 * @return the availabeTreebank
	 */
	public Map<TreebankInfo, TreebankDescriptor> getAvailabeTreebanks() {
		return availabeTreebank;
	}
	
	public int getAvailableTreebankCount() {
		return availabeTreebank==null ? 0 : availabeTreebank.size();
	}
	
	public boolean isEmpty() {
		return getAvailableTreebankCount()==0 && getUnavailableTreebankCount()==0;
	}
}
