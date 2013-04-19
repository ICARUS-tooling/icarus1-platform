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
public class WebchainOutputType implements WebchainElements{
	
	protected String outputType;
	protected String outputTypeValue;
	
	public WebchainOutputType(String outputType, String outputTypeValue){
		this.outputType = outputType;
		this.outputTypeValue = outputTypeValue;
	}

	/**
	 * @return the outputType
	 */
	public String getOutputType() {
		return outputType;
	}

	/**
	 * @param outputType the outputType to set
	 */
	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	/**
	 * @return the outputTypeValue
	 */
	public String getOutputTypeValue() {
		return outputTypeValue;
	}

	/**
	 * @param outputTypeValue the outputTypeValue to set
	 */
	public void setOutputTypeValue(String outputTypeValue) {
		this.outputTypeValue = outputTypeValue;
	}


}
