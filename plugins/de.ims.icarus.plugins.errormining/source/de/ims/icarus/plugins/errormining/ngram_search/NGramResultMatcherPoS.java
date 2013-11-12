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

import de.ims.icarus.plugins.errormining.ItemInNuclei;
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
public class NGramResultMatcherPoS implements ErrorminingMatcher, Cloneable, Comparable<NGramResultMatcherPoS> {
	

	public NGramResultMatcherPoS() {
		//noop
	}

	protected int id;
	
	Map<String, ArrayList<ItemInNuclei>> ngramsResultMap;
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
					ArrayList<ItemInNuclei> iinL = ngramsResultMap.get(mngResult.getKeyAt(tmp));
					
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
	private void buildHit(ArrayList<ItemInNuclei> iinL, int index) {
		
		//we have at least iinL size of 2 (iinL = 1 was filtered out before)
		ItemInNuclei iin = iinL.get(0);	
		String[] iiTagArray = iin.getPosTag().split(" "); //$NON-NLS-1$
		
		SentenceInfo si = iin.getSentenceInfoAt(0);
		
		List<Integer> nucleiIndexList = new ArrayList<Integer>();
		
		for (int item = 1; item < iinL.size(); item++) {
			ItemInNuclei iinCompare = iinL.get(item);
			String[] iiTagCompare = iinCompare.getPosTag().split(" "); //$NON-NLS-1$
			// System.out.println("PoSTag: "+ iin.getPosTag() +
			// " PoSCount: " + iin.getCount());			
			generateNucleiList(iiTagArray,iiTagCompare, nucleiIndexList);
		}
		
		
		for(int j = 0; j < nucleiIndexList.size(); j++){
			// start - end - nuclei
			int[] hitArray = new int[3];
			// satzanfang
			hitArray[0] = si.getSentenceBegin();
			// satzende
			hitArray[1] = si.getSentenceEnd();
			// nuclei
			hitArray[2] = si.getSentenceBegin() + nucleiIndexList.get(j);

//			 System.out.println(hitArray[0] + " "
//			 + hitArray[1] + " " + hitArray[2]);

			Hit hit = new Hit(hitArray);
			entryBuilder.addHit(hit);			
		}
			

//			for (int s = 0; s < iin.getSentenceInfoSize(); s++) {
//				SentenceInfo si = iin.getSentenceInfoAt(s);
//				int sIndex = si.getSentenceNr()-1;
////				System.out.println("InputSentence: " + index +
////						" Current " + sIndex);
//				
//				
//				// only if we have the correct sentence index check
//				// nucleis and make hits!
//				if(index == sIndex){
//					for (int nuclei = 0; nuclei < si
//							.getNucleiIndexListSize(); nuclei++) {
//
//						// start - end - nuclei
//						int[] hitArray = new int[3];
//						// satzanfang
//						hitArray[0] = si.getSentenceBegin();
//						// satzende
//						hitArray[1] = si.getSentenceEnd();
//						// nuclei
//						hitArray[2] = si.getNucleiIndexListAt(nuclei);
//
//						// System.out.println(hitArray[0] + " "
//						// + hitArray[1] + " " + hitArray[2]);
//
//						Hit hit = new Hit(hitArray);
//						entryBuilder.addHit(hit);
//					}
//				}
//			}			
//		}
		
	}


	/**
	 * @param iiTagArray
	 * @param iiTagCompare
	 * @param nucleiIndexList
	 */
	private void generateNucleiList(String[] iiTagArray, String[] iiTagCompare,
			List<Integer> nucleiIndexList) {
		
		for(int i = 0 ; i < iiTagArray.length; i++){
			//System.out.println(iiTagArray[i] + " " + iiTagCompare[i]);
			
			if(!iiTagArray[i].equals(iiTagCompare[i])){
				//System.out.println("!equal");
				if (!nucleiIndexList.contains(i)){
					nucleiIndexList.add(i);
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
	 * @param ngramsResultMap
	 */
	public void setResultNGrams(
			Map<String, ArrayList<ItemInNuclei>> ngramsResultMap) {
		this.ngramsResultMap = ngramsResultMap;		
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(NGramResultMatcherPoS other) {
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
