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


/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class MappedNGramResult {
	
	protected int index;
	protected String key;
	
	/**
	 * @param index
	 * @param key
	 */
	public MappedNGramResult(int index, String key) {
		this.index = index;
		this.key = key;
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
	
	
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	
	
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	
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
