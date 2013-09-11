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
public class ItemInNuclei {

	protected String posTag;
	protected List<SentenceInfo> sl;
	protected int count;
	//protected int nucleiCount;
	
	public ItemInNuclei(){
		List<SentenceInfo> sl = new ArrayList<>();
		this.sl = sl;
		count = 1; //innitialize with 1 occurences +1 when found
		count = 1;
	}
	
	
	public ItemInNuclei(int count, String Tag){
		List<SentenceInfo> sl = new ArrayList<>();
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
	}
	
	/**
	 * listworkerstuff below
	 * @return
	 */
	
	public void addNewSentenceInfoUniGrams(int sentenceNR, int positionNR){
		SentenceInfo sentenceInfo = new SentenceInfo();
		sentenceInfo.setSentenceNr(sentenceNR);
		sentenceInfo.addNucleiIndexList(positionNR);
		sentenceInfo.setNucleiIndex(positionNR);
		//sentenceInfo.setNucleiSentencePos(positionNR);
		sentenceInfo.setSentenceBegin(positionNR);
		sentenceInfo.setSentenceEnd(positionNR);
		sl.add(sentenceInfo);
	}
	
	
	public void addNewSentenceInfo(int sentenceNR, int nucleiPos, int sentenceBegin, int sentenceEnd){
		SentenceInfo sentenceInfo = new SentenceInfo();
		sentenceInfo.setSentenceNr(sentenceNR);
		sentenceInfo.addNucleiIndexList(nucleiPos);
		//sentenceInfo.setNucleiSentencePos(nucleiPos);
		sentenceInfo.setSentenceBegin(sentenceBegin);
		sentenceInfo.setSentenceEnd(sentenceEnd);
		sl.add(sentenceInfo);
	}
	
	
	public void addNewNucleiToSentenceInfoLeft(SentenceInfo si, SentenceInfo sitemp){
		SentenceInfo sentenceInfo = new SentenceInfo();		
		sentenceInfo.setSentenceNr(si.getSentenceNr());
		sentenceInfo.setNucleiIndex(si.getNucleiIndex());
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
	
	
	public void addNewNucleiToSentenceInfoRight(SentenceInfo si, SentenceInfo sitemp){
		SentenceInfo sentenceInfo = new SentenceInfo();		
		sentenceInfo.setSentenceNr(si.getSentenceNr());
		sentenceInfo.setNucleiIndex(si.getNucleiIndex());
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
	
	
	public void addNewSentenceInfoLeft(SentenceInfo si){		
		SentenceInfo sentenceInfo = new SentenceInfo();
		//nucleiCount = sentenceInfo.getNucleiIndexListSize();
		sentenceInfo.setNucleiIndex(si.getNucleiIndex());
		sentenceInfo.setSentenceNr(si.getSentenceNr());
		for(int i = 0; i < si.getNucleiIndexListSize();  i++){
			sentenceInfo.addNucleiIndexList(si.getNucleiIndexListAt(i));			
		}
		//sentenceInfo.setNucleiSentencePos(si.getNucleiSentencePos());
		sentenceInfo.setSentenceBegin(si.getSentenceBegin()-1); //going left
		sentenceInfo.setSentenceEnd(si.getSentenceEnd());
		sl.add(sentenceInfo);
	}
	
	public void addNewSentenceInfoRigth(SentenceInfo si){
		SentenceInfo sentenceInfo = new SentenceInfo();
		//nucleiCount = sentenceInfo.getNucleiIndexListSize();
		sentenceInfo.setNucleiIndex(si.getNucleiIndex());
		sentenceInfo.setSentenceNr(si.getSentenceNr());
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
	
	
	public SentenceInfo getSentenceInfoAt(int i){
		return sl.get(i);
	}

	public int indexOfSentenceInfo(SentenceInfo si){
		return sl.indexOf(si);
	}
	
	


}
