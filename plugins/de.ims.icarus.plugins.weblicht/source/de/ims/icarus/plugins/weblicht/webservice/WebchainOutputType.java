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
public class WebchainOutputType implements WebchainElements{
	
	protected String outputType;
	protected String outputTypeValue;
	protected boolean isOutputUsed;
	
	public WebchainOutputType(){		
	}
	
	public WebchainOutputType(String outputType, String outputTypeValue, boolean isOutputUsed){
		this.outputType = outputType;
		this.outputTypeValue = outputTypeValue;
		this.isOutputUsed = isOutputUsed;
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

	/**
	 * @return the isOutputUsed
	 */
	public boolean isOutputUsed() {
		return isOutputUsed;
	}
	
	public String getOutputUsed() {
		if (isOutputUsed) return "enabled"; //$NON-NLS-1$
		return "disabled"; //$NON-NLS-1$
	}

	/**
	 * @param isOutputUsed the isOutputUsed to set
	 */
	public void setOutputUsed(boolean isOutputUsed) {
		this.isOutputUsed = isOutputUsed;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
	    if(!(other instanceof WebchainOutputType)){
	    	return false;
	    }
	    
	    //check fields
	    WebchainOutputType wo = (WebchainOutputType)other;
	    
	    if(!(this.outputType == wo.getOutputType())){
	    	return false;
	    }
	    if(!(this.outputTypeValue == wo.getOutputTypeValue())){
	    	return false;
	    }
	    if(!(this.isOutputUsed == wo.isOutputUsed())){
	    	return false;
	    }
	    
	    return true;
	}


}
