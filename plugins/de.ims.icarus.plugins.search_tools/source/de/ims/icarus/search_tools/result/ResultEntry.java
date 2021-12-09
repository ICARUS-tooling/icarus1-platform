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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlList;
import jakarta.xml.bind.annotation.XmlRootElement;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="entry")
public class ResultEntry {
	
	@XmlAttribute(name="index")
	private final int index;

	@XmlElement(name="hits")
	@XmlList
	private final Hit[] hits;
	
	public ResultEntry(int corpusIndex,
			@XmlElement(name="hits") @XmlList Hit[] hits) {
		if(hits==null)
			throw new NullPointerException("Invalid hits"); //$NON-NLS-1$
		
		this.hits = hits;
		this.index = corpusIndex;
	}
	
	@Override
	public String toString() {
		return String.format("Entry: index=%d hits=%s", index, Arrays.toString(hits)); //$NON-NLS-1$
	}
	
	public Hit[] getHits() {
		return hits;
	}
	
	public int getIndex() {
		return index;
	}

	public Hit getHit(int index) {
		return hits[index];
	}

	public int getHitCount() {
		return hits.length;
	}

	@Override
	public int hashCode() {
		return (index+1) * (hits.length+1);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ResultEntry) {
			ResultEntry other = (ResultEntry) obj;
			return other.index==index && Arrays.equals(hits, other.hits);
		}
		return false;
	}
}