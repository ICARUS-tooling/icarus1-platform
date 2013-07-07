/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph.layout;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface CellMerger {

	/**
	 * Merges the content of cell {@code cell} into cell {@code parent}
	 */
	void merge(GraphOwner owner, Object parent, Object cell);
}
