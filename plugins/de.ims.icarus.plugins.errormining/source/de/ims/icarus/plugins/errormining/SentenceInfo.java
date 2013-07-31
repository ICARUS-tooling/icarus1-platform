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
public class SentenceInfo {
	
	protected int sentenceNr;
	protected List<Integer> nucleiIndexList;
	protected int nucleiIndex;
	protected int sentenceBegin;
	protected int sentenceEnd;
	
	
	public SentenceInfo (){		
		List<Integer> nucleiIndexList = new ArrayList<>();
		this.nucleiIndexList = nucleiIndexList;
				
	}




	/**
	 * @return the sentenceBegin
	 */
	public int getSentenceBegin() {
		return sentenceBegin;
	}




	/**
	 * @param sentenceBegin the sentenceBegin to set
	 */
	public void setSentenceBegin(int sentenceBegin) {
		this.sentenceBegin = sentenceBegin;
	}




	/**
	 * @return the sentenceEnd
	 */
	public int getSentenceEnd() {
		return sentenceEnd;
	}




	/**
	 * @param sentenceEnd the sentenceEnd to set
	 */
	public void setSentenceEnd(int sentenceEnd) {
		this.sentenceEnd = sentenceEnd;
	}




//	/**
//	 * @return the wordPosInSentence
//	 */
//	public int getNucleiSentencePos() {
//		return nucleiSentencePos;
//	}
//
//	/**
//	 * @param wordPosInSentence the wordPosInSentence to set
//	 */
//	public void setNucleiSentencePos(int wordPosInSentence) {
//		this.nucleiSentencePos = wordPosInSentence;
//	}

	/**
	 * @return the sentenceNr
	 */
	public int getSentenceNr() {
		return sentenceNr;
	}
	
	/**
	 * @param sentenceNr the sentenceNr to set
	 */
	public void setSentenceNr(int sentenceNr) {
		this.sentenceNr = sentenceNr;
	}
	
	
	
	/**
	 * @return the nucleiIndex
	 */
	public int getNucleiIndex() {
		return nucleiIndex;
	}




	/**
	 * @param nucleiIndex the nucleiIndex to set
	 */
	public void setNucleiIndex(int nucleiIndex) {
		this.nucleiIndex = nucleiIndex;
	}




	//Nucleiliststuff
	public void addNucleiIndexList(int nucleiPosition){
		nucleiIndexList.add(nucleiPosition);
	}
	
	
	public int getNucleiIndexListSize(){
		return nucleiIndexList.size();
	}
	
	
	public int getNucleiIndexListAt(int i){
		return nucleiIndexList.get(i);
	}

	public int indexOfNucleiIndexList(SentenceInfo si){
		return nucleiIndexList.indexOf(si);
	}


	

}
