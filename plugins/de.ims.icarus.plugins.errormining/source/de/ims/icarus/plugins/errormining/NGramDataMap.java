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
package de.ims.icarus.plugins.errormining;

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
