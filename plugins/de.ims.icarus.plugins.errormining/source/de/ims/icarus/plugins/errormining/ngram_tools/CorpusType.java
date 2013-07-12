/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.errormining.ngram_tools;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class CorpusType{
	Integer sentence;
	String key;
	
	public CorpusType(){
		
	}
	
	public CorpusType(Integer sentence, String key){
		this.sentence = sentence;
		this.key = key;
		
	}
	
	/**
	 * @return the sentence
	 */
	public Integer getSentence() {
		return sentence;
	}
	/**
	 * @param sentence the sentence to set
	 */
	public void setSentence(Integer sentence) {
		this.sentence = sentence;
	}
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
}
