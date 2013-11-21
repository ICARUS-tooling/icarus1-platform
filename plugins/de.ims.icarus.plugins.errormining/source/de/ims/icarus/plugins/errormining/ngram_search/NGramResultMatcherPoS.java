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
				MappedNGramResult mr = helferList.get(i);
				buildHit(mr, index);
				
				//TODO remove
//				for(int tmp = 0; tmp < mr.getKeyListSize(); tmp++){
//					ArrayList<ItemInNuclei> iinL = ngramsResultMap.get(
//														mr.getKeyAt(tmp));
//					buildHit(iinL, index);					
//				}	
			}
		}	
		commit();
	}
	
	
	private void buildHit(MappedNGramResult mr, int index) {
	
		if(mr.containsIntIndex(index)){	
			int offset = mr.getCoverEnd() - mr.getCoverStart() + 1;
			//if(iiTagArray.length == offset){
//			System.out.println("Start:" + mr.getCoverStart() //$NON-NLS-1$
//							+ " End:" + mr.getCoverEnd() //$NON-NLS-1$
//							+ " Sentence:" + mr.getIndex() //$NON-NLS-1$
//							+ " Nucleus:" + mr.getIndex() //$NON-NLS-1$
//							);
			
			
	
			// start - end - nuclei
			int[] hitArray = new int[3];
			// satzanfang
			hitArray[0] = mr.getCoverStart();
			// satzende
			hitArray[1] = mr.getCoverEnd();
			// nuclei
			hitArray[2] = mr.getNucleusIndex();

//			 System.out.println(hitArray[0] + " "
//			 + hitArray[1] + " " + hitArray[2]);

			Hit hit = new Hit(hitArray);
			
			entryBuilder.addHit(hit);						
			//}
		}
	}
	
	/**
	 * @param iinL
	 * @param index 
	 */
	private void buildHit(ArrayList<ItemInNuclei> iinL, int index) {
				
//		//we have at least iinL size of 2 (iinL = 1 was filtered out before)
//		ItemInNuclei iin = iinL.get(0);	
//		String[] iiTagArray = iin.getPosTag().split(" "); //$NON-NLS-1$
//		
//		SentenceInfo si = iin.getSentenceInfoAt(0);		
//		List<Integer> nucleiIndexList = new ArrayList<Integer>();
//		
//		for (int item = 1; item < iinL.size(); item++) {
//			ItemInNuclei iinCompare = iinL.get(item);
//			String[] iiTagCompare = iinCompare.getPosTag().split(" "); //$NON-NLS-1$
//			// System.out.println("PoSTag: "+ iin.getPosTag() +
//			// " PoSCount: " + iin.getCount());			
//			generateNucleiList(iiTagArray, iiTagCompare, nucleiIndexList);
//		}
		
//        for(int j = 0; j < nucleiIndexList.size(); j++){
//            // start - end - nuclei
//            int[] hitArray = new int[3];
//            // satzanfang
//            hitArray[0] = si.getSentenceBegin();
//            // satzende
//            hitArray[1] = si.getSentenceEnd();
//            // nuclei
//            hitArray[2] = si.getSentenceBegin() + nucleiIndexList.get(j);
//
//             System.out.println(hitArray[0] + " "
//             + hitArray[1] + " " + hitArray[2]);
//
//            Hit hit = new Hit(hitArray);
//            entryBuilder.addHit(hit);
//        }

		
		System.out.println("-------------------------------");
		System.out.println("Length HelferList" + helferList.size());
		System.out.println("Hits for Index" + helferList.get(index).getKeyListSize());
		
		MappedNGramResult mr = helferList.get(index);
		
		for(int i = 0; i < mr.getKeyListSize(); i++){
				int offset = mr.getCoverEnd() - mr.getCoverStart() + 1;
				//if(iiTagArray.length == offset){
//						System.out.println("Start:" + mr.getCoverStart() //$NON-NLS-1$
//										+ " End:" + mr.getCoverEnd() //$NON-NLS-1$
//										+ " Sentence:" + mr.getIndex() //$NON-NLS-1$
//										);
		
				// start - end - nuclei
				int[] hitArray = new int[3];
				// satzanfang
				hitArray[0] = mr.getCoverStart();
				// satzende
				hitArray[1] = mr.getCoverEnd();
				// nuclei
				hitArray[2] =  66;

//				System.out.println(hitArray[0] + " "
//				 + hitArray[1] + " " + hitArray[2]);

				Hit hit = new Hit(hitArray);
				
				entryBuilder.addHit(hit);						
		}
		
		System.out.println("+++++++++++++++++++++++++++++++++");
		
		
//		for(int i = 0; i < iinL.size(); i++){
//			iin = iinL.get(i);	
//			iiTagArray = iin.getPosTag().split(" "); //$NON-NLS-1$
//			for (int item = 1; item < iinL.size(); item++) {
//				ItemInNuclei iinCompare = iinL.get(item);
//				String[] iiTagCompare = iinCompare.getPosTag().split(" "); //$NON-NLS-1$
//				System.out.println("PoSTag: "+ iin.getPosTag() 
//			 			+ " PoSCount: " + iin.getCount()
//			 			+ " HIT: " + index);
//				for(int s = 0; s < iin.getSentenceInfoSize(); s++){
//					System.out.println(" SatzNR: " + iin.getSentenceInfoAt(s).getSentenceNr());			
//					generateNucleiList(iiTagArray, iiTagCompare, nucleiIndexList);
//				
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
