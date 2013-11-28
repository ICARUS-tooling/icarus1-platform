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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.errormining.ngram_tools;

import de.ims.icarus.plugins.errormining.DependencyItemInNuclei;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyNucleusCache {
	
	protected String key;
	protected int headIndex;
	protected DependencyItemInNuclei diin;
	public DependencyNucleusCache(){
			//noop
	}


	/**
	 * @return the headIndex
	 */
	public int getHeadIndex() {
		return headIndex;
	}


	/**
	 * @param headIndex the headIndex to set
	 */
	public void setHeadIndex(int headIndex) {
		this.headIndex = headIndex;
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
	/**
	 * @return the diin
	 */
	public DependencyItemInNuclei getDiin() {
		return diin;
	}
	/**
	 * @param diin the diin to set
	 */
	public void setDiin(DependencyItemInNuclei diin) {
		this.diin = diin;
	}

}
