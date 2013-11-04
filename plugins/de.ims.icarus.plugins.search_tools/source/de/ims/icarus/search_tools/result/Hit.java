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
package de.ims.icarus.search_tools.result;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import de.ims.icarus.util.CollectionUtils;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement
public class Hit {
	
	/**
	 * Mapping from the nodes used in the matcher that created
	 * this {@code Hit} to indices in the corresponding  target graph.
	 */
	@XmlList
	@XmlElement(name="indices")
	private final int[] indices;
	
	public Hit(@XmlList @XmlElement(name="indices") int[] indices) {
		if(indices==null)
			throw new NullPointerException("Invalid indices"); //$NON-NLS-1$
			
		this.indices = indices;
	}

	public int[] getIndices() {
		return indices;
	}
	
	public int getIndexCount() {
		return indices.length;
	}
	
	public int getIndex(int index) {
		return indices[index];
	}
	
	@Override
	public String toString() {
		return Arrays.toString(indices);
	}

	@Override
	public int hashCode() {
		return CollectionUtils.hashCode(indices);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Hit) {
			Hit other = (Hit) obj;
			int i0 = 0;
			int i1 = 0;
			
			while(i0<indices.length && i1<other.indices.length) {
				if(indices[i0]==-1) {
					i0++;
				} else if(other.indices[i1]==-1) {
					i1++;
				} else if(indices[i0]!=other.indices[i1]) {
					return false;
				} else {
					i0++;
					i1++;
				}
			}
			
			return true;
		}
		return false;
	}
}