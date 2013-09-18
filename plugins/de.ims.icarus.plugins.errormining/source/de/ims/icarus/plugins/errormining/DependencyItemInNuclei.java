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
import java.util.List;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class DependencyItemInNuclei {
	
	protected String posTag;
	//protected ArrayList<String> posTagList;

	protected List<DependencySentenceInfo> sl;
	protected int count;
	//protected int nucleiCount;
	
	public DependencyItemInNuclei(){
		List<DependencySentenceInfo> sl = new ArrayList<>();
		ArrayList<String> posTagList = new ArrayList<>();
		this.sl = sl;
		count = 1; //innitialize with 1 occurences +1 when found
		count = 1;
	}
	
	
	public DependencyItemInNuclei(int count, String Tag){
		List<DependencySentenceInfo> sl = new ArrayList<>();
		ArrayList<String> posTagList = new ArrayList<>();
		this.sl = sl;
		this.count = count;
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
	 * @return the posTag
	 */
	public String getPosTag() {
		return posTag;
	}
	
	/**
	 * @param posTag the posTag to set
	 */
	public void setPosTag(String posTag) {
		this.posTag = posTag;
//		if (posTagList == null){
//			posTagList = new ArrayList<>();
//		}
//		posTagList.add(posTag);
	}
	
	/**
	 * listworkerstuff below
	 * @param i 
	 * @return
	 */
	
	public void addNewDependencySentenceInfoUniGrams(int sentenceNR, int positionNR, int headIndex){
		DependencySentenceInfo sentenceInfo = new DependencySentenceInfo();
		sentenceInfo.setSentenceNr(sentenceNR);
		sentenceInfo.addNucleiIndexList(positionNR);
		sentenceInfo.setNucleiIndex(positionNR);
		//sentenceInfo.setNucleiSentencePos(positionNR);
//		sentenceInfo.setSentenceBegin(positionNR);
//		sentenceInfo.setSentenceEnd(positionNR);
		
//		//TODO Workaround needed?
		
		if(positionNR > headIndex){
			sentenceInfo.setSentenceBegin(headIndex);
		} else {
			sentenceInfo.setSentenceBegin(positionNR);
		}
		

		if(positionNR < headIndex){
			sentenceInfo.setSentenceEnd(headIndex);
		} else {
			sentenceInfo.setSentenceEnd(positionNR);
		}
		
		
		sentenceInfo.setSentenceHeadIndex(headIndex);
		sl.add(sentenceInfo);
	}
	
	
	public void addNewDependencySentenceInfo(int sentenceNR, int nucleiPos, int sentenceBegin, int sentenceEnd){
		DependencySentenceInfo sentenceInfo = new DependencySentenceInfo();
		sentenceInfo.setSentenceNr(sentenceNR);
		sentenceInfo.addNucleiIndexList(nucleiPos);
		//sentenceInfo.setNucleiSentencePos(nucleiPos);
		sentenceInfo.setSentenceBegin(sentenceBegin);
		sentenceInfo.setSentenceEnd(sentenceEnd);
		sl.add(sentenceInfo);
	}
	
	
	public void addNewNucleiToSentenceInfoLeft(DependencySentenceInfo si, DependencySentenceInfo sitemp){
		DependencySentenceInfo sentenceInfo = new DependencySentenceInfo();		
		sentenceInfo.setSentenceNr(si.getSentenceNr());
		sentenceInfo.setNucleiIndex(si.getNucleiIndex());
		sentenceInfo.setSentenceHeadIndex(si.getSentenceHeadIndex());
		
		for(int i = 0; i < sitemp.getNucleiIndexListSize();  i++){
			sentenceInfo.addNucleiIndexList(sitemp.getNucleiIndexListAt(i));			
		}
		for(int i = 0; i < si.getNucleiIndexListSize();  i++){
			sentenceInfo.addNucleiIndexList(si.getNucleiIndexListAt(i));			
		}
		sentenceInfo.setSentenceBegin(si.getSentenceBegin()-1); //going left
		sentenceInfo.setSentenceEnd(si.getSentenceEnd());
		sl.add(sentenceInfo);
	}
	
	
	public void addNewNucleiToSentenceInfoRight(DependencySentenceInfo si, DependencySentenceInfo sitemp){
		DependencySentenceInfo sentenceInfo = new DependencySentenceInfo();		
		sentenceInfo.setSentenceNr(si.getSentenceNr());
		sentenceInfo.setNucleiIndex(si.getNucleiIndex());
		sentenceInfo.setSentenceHeadIndex(si.getSentenceHeadIndex());
		
		for(int i = 0; i < si.getNucleiIndexListSize();  i++){
			sentenceInfo.addNucleiIndexList(si.getNucleiIndexListAt(i));			
		}
		for(int i = 0; i < sitemp.getNucleiIndexListSize();  i++){
			sentenceInfo.addNucleiIndexList(sitemp.getNucleiIndexListAt(i));			
		}
		sentenceInfo.setSentenceBegin(si.getSentenceBegin());
		sentenceInfo.setSentenceEnd(si.getSentenceEnd()+1); //going right
		sl.add(sentenceInfo);
	}
	
	
	public void addNewSentenceInfoLeft(DependencySentenceInfo si){		
		DependencySentenceInfo sentenceInfo = new DependencySentenceInfo();
		sentenceInfo.setNucleiIndex(si.getNucleiIndex());
		sentenceInfo.setSentenceNr(si.getSentenceNr());
		sentenceInfo.setSentenceHeadIndex(si.getSentenceHeadIndex());
		
		for(int i = 0; i < si.getNucleiIndexListSize();  i++){
			sentenceInfo.addNucleiIndexList(si.getNucleiIndexListAt(i));			
		}
		//sentenceInfo.setNucleiSentencePos(si.getNucleiSentencePos());
		sentenceInfo.setSentenceBegin(si.getSentenceBegin()-1); //going left
		sentenceInfo.setSentenceEnd(si.getSentenceEnd());
		sl.add(sentenceInfo);
	}
	
	public void addNewSentenceInfoRigth(DependencySentenceInfo si){
		DependencySentenceInfo sentenceInfo = new DependencySentenceInfo();
		sentenceInfo.setNucleiIndex(si.getNucleiIndex());
		sentenceInfo.setSentenceNr(si.getSentenceNr());
		sentenceInfo.setSentenceHeadIndex(si.getSentenceHeadIndex());
		
		for(int i = 0; i < si.getNucleiIndexListSize();  i++){
			sentenceInfo.addNucleiIndexList(si.getNucleiIndexListAt(i));			
		}
		//sentenceInfo.setNucleiSentencePos(si.getNucleiSentencePos());
		sentenceInfo.setSentenceBegin(si.getSentenceBegin());
		sentenceInfo.setSentenceEnd(si.getSentenceEnd()+1); //going right
		sl.add(sentenceInfo);
	}
	
	
	/**
	 * listworkerstuff below
	 * @return
	 */
		
	
	public int getSentenceInfoSize(){
		return sl.size();
	}
	
	
	public DependencySentenceInfo getSentenceInfoAt(int i){
		return sl.get(i);
	}

	public int indexOfSentenceInfo(DependencySentenceInfo si){
		return sl.indexOf(si);
	}
	
	
//	/**
//	 * @return the posTagList
//	 */
//	public int getposTagListSize(){
//		return posTagList.size();
//	}	
//	
//	public String getposTagListAt(int i){
//		return posTagList.get(i);
//	}
//
//	public int indexOfposTagList(String si){
//		return posTagList.indexOf(si);
//	}
//	
//	
//	public String posTagListToString(){
//		return posTagList.toString();
//	}
	

}
