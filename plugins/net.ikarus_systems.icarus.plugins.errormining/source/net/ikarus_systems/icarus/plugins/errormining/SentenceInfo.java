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
	protected List<Integer> nucleiSentencePosition;
	protected int sentenceBegin;
	protected int sentenceEnd;
	
	
	public SentenceInfo (){
		List<Integer> nucleiSentencePosition = new ArrayList<>();
		this.nucleiSentencePosition = nucleiSentencePosition;
				
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
	
	
	
	//Nucleiliststuff
	public void addNucleiSentencePosition(int nucleiPosition){
		nucleiSentencePosition.add(nucleiPosition);
	}
	
	
	public int getNucleiSentencePositionSize(){
		return nucleiSentencePosition.size();
	}
	
	
	public int getNucleiSentencePositionAt(int i){
		return nucleiSentencePosition.get(i);
	}

	public int indexOfNucleiSentencePosition(SentenceInfo si){
		return nucleiSentencePosition.indexOf(si);
	}


	

}
