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
import java.util.Comparator;
import java.util.List;

public class StatsData implements Comparable<StatsData>, Comparator<Object>{
	
	List<String> wordstring;
	String tagKey;
	int count;
	
	public StatsData(String tagKey, int count){
		this.wordstring = new ArrayList<String>();
		this.tagKey = tagKey;
		this.count = count;
	}
	
	//word list stuff		
	public int indexOfWordstring(String s){
		return wordstring.indexOf(s);
	}
	
	public int getWordstringSize(){
		return wordstring.size();
	}
	
	public String getWordstringAt(int index){
		return wordstring.get(index);
	}
	
	public void addWordstringSize(String s){
			wordstring.add(s);
	}

	/**
	 * @return the tagKey
	 */
	public String getTagKey() {
		return tagKey;
	}

	/**
	 * @param tagKey the tagKey to set
	 */
	public void setTagKey(String tagKey) {
		this.tagKey = tagKey;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof StatsData) {
			return ((StatsData)obj).tagKey.equals(tagKey);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 0;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StatsData stats) {
		return this.tagKey.compareTo(stats.getTagKey());
	}

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Object sd1, Object sd2) {
		return ((StatsData) sd1).getTagKey().compareTo(((StatsData) sd2).getTagKey());
	};
	
}