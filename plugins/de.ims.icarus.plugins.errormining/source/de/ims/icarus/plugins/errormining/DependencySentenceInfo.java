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
	
	
	/**
	 * @return the sentenceHeadEnd
	 */
	protected int getSentenceHeadEnd() {
		return sentenceHeadEnd;
	}
	/**
	 * @param sentenceHeadEnd the sentenceHeadEnd to set
	 */
	protected void setSentenceHeadEnd(int sentenceHeadEnd) {
		this.sentenceHeadEnd = sentenceHeadEnd;
	}

}
