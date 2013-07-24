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
public class NGramQAttributes {
	
	protected String key;
	protected String value;
	protected boolean include;
	
	public NGramQAttributes(){
		//noop
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
		this.include = true;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	
	@Override
	public boolean equals(Object other){
	    if(!(other instanceof NGramQAttributes)) return false;
	    NGramQAttributes att = (NGramQAttributes)other;
	    return this.key.equals(att.getKey());
	}

	/**
	 * @return the include
	 */
	public boolean isInclude() {
		return include;
	}

	/**
	 * @param include the include to set
	 */
	public void setInclude(boolean include) {
		this.include = include;
	}
	
	
	//TODO needed?
//	@Override
//	public int hashCode(){
//	  return toString().hashCode();
//	}
	

}
