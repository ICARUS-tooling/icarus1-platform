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
