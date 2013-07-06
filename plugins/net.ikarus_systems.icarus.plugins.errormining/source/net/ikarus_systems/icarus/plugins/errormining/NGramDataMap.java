/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.errormining;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramDataMap {
	
	protected Map<String,ArrayList<ItemInNuclei>> nGramCache;
	
	public NGramDataMap(){
		nGramCache = new LinkedHashMap<String,ArrayList<ItemInNuclei>>();
	}

	/**
	 * @param tag
	 * @return
	 */
	public boolean containsKey(String tag) {
		return nGramCache.containsKey(tag);
	}

	/**
	 * @param tag
	 * @return
	 */
	public ArrayList<ItemInNuclei> get(String tag) {
		return nGramCache.get(tag);
	}

	/**
	 * @param currentWord
	 * @param items
	 */
	public void put(String currentWord, ArrayList<ItemInNuclei> items) {
		nGramCache.put(currentWord, items);
		
	}

	/**
	 * @param outputNGram
	 */
	public void putAll(Map<String, ArrayList<ItemInNuclei>> outputNGram) {
		nGramCache.putAll(outputNGram);
		
	}


}
