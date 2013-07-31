/* 
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
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
