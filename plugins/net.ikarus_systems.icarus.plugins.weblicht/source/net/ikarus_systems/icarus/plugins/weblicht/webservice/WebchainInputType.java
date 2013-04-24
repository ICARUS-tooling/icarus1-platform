/* 
 * $Revision$ 
 * $Date$ 
 * $URL$ 
 * 
 * $LastChangedDate$  
 * $LastChangedRevision$  
 * $LastChangedBy$ 
 */
package net.ikarus_systems.icarus.plugins.weblicht.webservice;

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
	


}
