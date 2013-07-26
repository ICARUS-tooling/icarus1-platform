/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package de.ims.icarus.plugins.weblicht.webservice;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class WebchainInputType implements WebchainElements {
	
	protected String inputType;
	protected String inputTypeValue;
	
	public WebchainInputType(){
		
	}

	/**
	 * @return the inputType
	 */
	public String getInputType() {
		return inputType;
	}

	/**
	 * @param inputType the inputType to set
	 */
	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	/**
	 * @return the inputTypeValue
	 */
	public String getInputTypeValue() {
		return inputTypeValue;
	}

	/**
	 * @param inputTypeValue the inputTypeValue to set
	 */
	public void setInputTypeValue(String inputTypeValue) {
		this.inputTypeValue = inputTypeValue;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
	    if(!(other instanceof WebchainInputType)){
	    	return false;
	    }
	    
	    //check fields
	    WebchainInputType wi = (WebchainInputType)other;
	    
	    if(!(this.inputType == wi.getInputType())){
	    	return false;
	    }	
	    if(!(this.inputTypeValue == wi.getInputTypeValue())){
	    	return false;
	    }	
	    return true;
	}
}
