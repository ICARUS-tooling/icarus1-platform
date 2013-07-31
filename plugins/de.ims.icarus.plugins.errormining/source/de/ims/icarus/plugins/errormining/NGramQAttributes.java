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
