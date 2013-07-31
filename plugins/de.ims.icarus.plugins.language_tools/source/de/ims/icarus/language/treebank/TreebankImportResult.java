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
