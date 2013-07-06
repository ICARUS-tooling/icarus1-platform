/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.result;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;


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
			throw new IllegalArgumentException("Invalid indices"); //$NON-NLS-1$
			
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
}