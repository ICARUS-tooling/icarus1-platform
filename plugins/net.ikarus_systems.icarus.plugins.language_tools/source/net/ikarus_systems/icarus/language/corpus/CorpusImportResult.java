/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.corpus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusImportResult {
	
	private List<CorpusInfo> unavailableCorpora;
	private Map<CorpusInfo, CorpusDescriptor> availabeCorpora;

	CorpusImportResult() {
		// no-op
	}

	void addUnavailable(CorpusInfo info) {
		if(unavailableCorpora==null) {
			unavailableCorpora = new ArrayList<>();
		}
		unavailableCorpora.add(info);
	}
	
	void addAvailable(CorpusInfo info, CorpusDescriptor descriptor) {
		if(availabeCorpora==null) {
			availabeCorpora = new LinkedHashMap<>();
		}
		availabeCorpora.put(info, descriptor);
	}

	/**
	 * @return the unavailableCorpora
	 */
	public List<CorpusInfo> getUnavailableCorpora() {
		return unavailableCorpora;
	}
	
	public int getUnavailableCorporaCount() {
		return unavailableCorpora==null ? 0 : unavailableCorpora.size();
	}

	/**
	 * @return the availabeCorpora
	 */
	public Map<CorpusInfo, CorpusDescriptor> getAvailabeCorpora() {
		return availabeCorpora;
	}
	
	public int getAvailableCorporaCount() {
		return availabeCorpora==null ? 0 : availabeCorpora.size();
	}
	
	public boolean isEmpty() {
		return getAvailableCorporaCount()==0 && getUnavailableCorporaCount()==0;
	}
}
