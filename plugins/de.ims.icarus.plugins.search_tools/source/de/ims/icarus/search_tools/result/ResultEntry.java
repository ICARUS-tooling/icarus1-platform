/*
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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;


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
			throw new IllegalArgumentException("Invalid hits"); //$NON-NLS-1$
		
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