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
package de.ims.icarus.plugins.errormining.ngram_search;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.plugins.errormining.SentenceInfo;


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class MappedNGramResult {
	
	protected int index;
	protected List<String> keyList;
	protected int coverStart;
	protected int coverEnd;
	
	/**
	 * @param index
	 * @param key
	 */
	public MappedNGramResult(int index, String key, SentenceInfo si) {
		if(keyList == null){
			keyList = new ArrayList<String>();
		}
		
		addKey(key);
		
		this.index = index;
		this.coverStart = si.getSentenceBegin();
		this.coverEnd = si.getSentenceEnd();
	}





	public String getKeyAt(int index) {
		return keyList.get(index);
	}


	public int getKeyListSize() {
		return keyList.size();
	}
	
	public void addKey(String key) {
		keyList.add(key);
	}
	
	public int indexOfKex(String key) {
		return keyList.indexOf(key);
	}




	/**
	 * @return the coverStart
	 */
	public int getCoverStart() {
		return coverStart;
	}




	/**
	 * @param coverStart the coverStart to set
	 */
	public void setCoverStart(int coverStart) {
		this.coverStart = coverStart;
	}




	/**
	 * @return the coverEnd
	 */
	public int getCoverEnd() {
		return coverEnd;
	}




	/**
	 * @param coverEnd the coverEnd to set
	 */
	public void setCoverEnd(int coverEnd) {
		this.coverEnd = coverEnd;
	}




	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
	
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MappedNGramResult) {
			return getIndex()==((MappedNGramResult)obj).getIndex();
		}		
		return false;
	}


	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	
	
//	/**
//	 * @return the key
//	 */
//	public String getKey() {
//		return key;
//	}
//	
//	
//	/**
//	 * @param key the key to set
//	 */
//	public void setKey(String key) {
//		this.key = key;
//	}
	
	
	/**
	 * containsIntEndex check if we have the input corpusindex
	 * in our resulting list, return the index if found
	 * 
	 * @param index
	 * @return
	 */
	public boolean containsIntIndex(int index) {
		return getIndex()==index;
	}
	

}
