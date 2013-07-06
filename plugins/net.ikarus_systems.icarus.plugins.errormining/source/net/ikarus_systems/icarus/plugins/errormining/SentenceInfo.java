/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.errormining;

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
