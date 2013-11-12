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
import java.util.Map;

import de.ims.icarus.plugins.errormining.DependencyItemInNuclei;
import de.ims.icarus.plugins.errormining.DependencySentenceInfo;
import de.ims.icarus.plugins.errormining.SentenceInfo;
import de.ims.icarus.search_tools.result.EntryBuilder;
import de.ims.icarus.search_tools.result.Hit;
import de.ims.icarus.search_tools.standard.GroupCache;
import de.ims.icarus.util.CorruptedStateException;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramResultMatcherDependency  implements ErrorMiningMatcherDependency, Cloneable, Comparable<NGramResultMatcherDependency> {


	public NGramResultMatcherDependency() {
		//noop
	}
	
	
protected int id;
	
	Map<String, ArrayList<DependencyItemInNuclei>> ngramsResultMap;
	protected List<MappedNGramResult> helferList;
	
	protected GroupCache cache;
	protected EntryBuilder entryBuilder;
	protected int allocation = -1;
	
	public void matches(int index) {	
		
		// for every entry in  list we check if current buffer index 
		// equals; note all indices saved within the list are results and have
		// to show up in the resulting list!
		
		for(int i = 0; i < helferList.size(); i++){
			if(helferList.get(i).containsIntIndex(index)){
				MappedNGramResult mngResult = helferList.get(i);
				for(int tmp = 0; tmp < mngResult.getKeyListSize(); tmp++){
					ArrayList<DependencyItemInNuclei> iinL = ngramsResultMap.get(
																	mngResult.getKeyAt(tmp));
					
					buildHit(iinL, index);					
				}	

				commit();
			
			}
		}			
	}
	
	
	/**
	 * @param iinL
	 * @param index
	 */
	private void buildHit(ArrayList<DependencyItemInNuclei> iinL, int index) {
		for (int item = 0; item < iinL.size(); item++) {
			DependencyItemInNuclei iin = iinL.get(item);
			// System.out.println("PoSTag: "+ iin.getPosTag() +
			// " PoSCount: " + iin.getCount());

			for (int s = 0; s < iin.getSentenceInfoSize(); s++) {
				DependencySentenceInfo si = iin.getSentenceInfoAt(s);
				int sIndex = si.getSentenceNr()-1;
//				System.out.println("InputSentence: " + index +
//						" Current " + sIndex);
				
				
				// only if we have the correct sentence index check
				// nucleis and make hits!
				if(index == sIndex){
					for (int nuclei = 0; nuclei < si
							.getNucleiIndexListSize(); nuclei++) {

						// start - end - nuclei
						int[] hitArray = new int[4];
						// satzanfang
						hitArray[0] = si.getSentenceBegin();
						// satzende
						hitArray[1] = si.getSentenceEnd();
						// targetnode
						hitArray[2] = si.getNucleiIndex();
						// sourcenode
						hitArray[3] = si.getSentenceHeadIndex();
						
						System.out.println(si.getNucleiIndex() + " Target");
						System.out.println(si.getSentenceHeadIndex()+ " Source");

						// System.out.println(hitArray[0] + " "
						// + hitArray[1] + " " + hitArray[2]);

						Hit hit = new Hit(hitArray);
						entryBuilder.addHit(hit);
					}
				}
			}
		}
		
	}


	protected void commit() {
		cache.commit(entryBuilder.toEntry());
	}
	
	
	protected void cacheHits() {
		entryBuilder.commitAllocation();
	}
	
	
	public GroupCache getCache() {
		return cache;
	}

	
	public EntryBuilder getEntryBuilder() {
		return entryBuilder;
	}
	
	
	public void setCache(GroupCache cache) {
		if(cache==null)
			throw new NullPointerException("Invalid cache"); //$NON-NLS-1$
		
		this.cache = cache;
		
//		if(next!=null) {
//			next.setCache(cache);
//		}
//		if(alternate!=null) {
//			alternate.setCache(cache);
//		}
//		if(exclusions!=null) {
//			for(NGramResultMatcher matcher : exclusions) {
//				matcher.setCache(cache);
//			}
//		}
//		if(options!=null) {
//			for(NGramResultMatcher option : options) {
//				option.setCache(cache);
//			}
//		}
	}
	
	
	public void setEntryBuilder(EntryBuilder entryBuilder) {
		if(entryBuilder==null)
			throw new NullPointerException("Invalid entry-builder"); //$NON-NLS-1$
		
		this.entryBuilder = entryBuilder;
//		if(next!=null) {
//			next.setEntryBuilder(entryBuilder);
//		}
//		if(alternate!=null) {
//			alternate.setEntryBuilder(entryBuilder);
//		}
//		if(exclusions!=null) {
//			for(NGramResultMatcher matcher : exclusions) {
//				matcher.setEntryBuilder(entryBuilder);
//			}
//		}
//		if(options!=null) {
//			for(NGramResultMatcher option : options) {
//				option.setEntryBuilder(entryBuilder);
//			}
//		}
	}
	
	/**
	 * @param helferList
	 */
	public void setSentenceList(List<MappedNGramResult> helferList) {
		this.helferList = helferList;		
	}


	/**
	 * @see de.ims.icarus.plugins.errormining.ngram_search.ErrorminingMatcher#setResultNGramsDependency(java.util.Map)
	 */
	@Override
	public void setResultNGramsDependency(
			Map<String, ArrayList<DependencyItemInNuclei>> ngramsResultMap) {
		this.ngramsResultMap = ngramsResultMap;	
		
	}


	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(NGramResultMatcherDependency other) {
		return id-other.id;
	}
	
	public NGramResultMatcherPoS clone() {
		NGramResultMatcherPoS clone = null;
		
		try {
			clone = (NGramResultMatcherPoS) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new CorruptedStateException("Cannot clone cloneable super type: "+getClass(), e); //$NON-NLS-1$
		}
		
		return clone;
	}


}
