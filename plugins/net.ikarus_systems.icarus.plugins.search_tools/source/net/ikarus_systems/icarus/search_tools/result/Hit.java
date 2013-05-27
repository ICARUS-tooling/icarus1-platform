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
	 * this {@code Hit} to indices in the corresponding 
	 * sentence.
	 */
	@XmlList
	@XmlElement(name="indices")
	private final int[] nodeIndices;
	
	public Hit(@XmlList @XmlElement(name="indices") int[] nodeIndices) {
		if(nodeIndices==null)
			throw new IllegalArgumentException("Invalid node indices"); //$NON-NLS-1$
			
		this.nodeIndices = nodeIndices;
	}

	public int[] getNodeIndices() {
		return nodeIndices;
	}
	
	public int getIndexCount() {
		return nodeIndices.length;
	}
	
	public int getNodeIndex(int index) {
		return nodeIndices[index];
	}
	
	@Override
	public String toString() {
		return Arrays.toString(nodeIndices);
	}
}