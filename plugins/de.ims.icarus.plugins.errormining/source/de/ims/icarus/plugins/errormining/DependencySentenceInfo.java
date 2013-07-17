/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class DependencySentenceInfo extends SentenceInfo {
	
	protected int sentenceHeadBegin;
	protected int sentenceHeadEnd;
	protected int sentenceHeadIndex;
	
	public DependencySentenceInfo(){
		//noop
	}

	/**
	 * @return the sentenceHeadBegin
	 */
	public int getSentenceHeadBegin() {
		return sentenceHeadBegin;
	}

	/**
	 * @return the sentenceHeadEnd
	 */
	public int getSentenceHeadEnd() {
		return sentenceHeadEnd;
	}

	/**
	 * @param sentenceHeadEnd the sentenceHeadEnd to set
	 */
	public void setSentenceHeadEnd(int sentenceHeadEnd) {
		this.sentenceHeadEnd = sentenceHeadEnd;
	}

	/**
	 * @param sentenceHeadBegin the sentenceHeadBegin to set
	 */
	public void setSentenceHeadBegin(int sentenceHeadBegin) {
		this.sentenceHeadBegin = sentenceHeadBegin;
	}

	/**
	 * @return the sentenceHeadIndex
	 */
	public int getSentenceHeadIndex() {
		return sentenceHeadIndex;
	}

	/**
	 * @param sentenceHeadIndex the sentenceHeadIndex to set
	 */
	public void setSentenceHeadIndex(int sentenceHeadIndex) {
		this.sentenceHeadIndex = sentenceHeadIndex;
	}

}
