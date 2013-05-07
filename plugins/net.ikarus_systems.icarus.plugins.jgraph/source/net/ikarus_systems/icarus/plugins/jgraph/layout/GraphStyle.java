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

import net.ikarus_systems.icarus.util.Installable;
import net.ikarus_systems.icarus.util.Options;

import com.mxgraph.view.mxStylesheet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface GraphStyle extends Installable<GraphOwner> {

	/**
	 * Generates a {@code mxStylesheet} to be used for the given
	 * graph. It is up to the {@code GraphStyle} implementation
	 * whether all possible styles should be defined in the returned
	 * stylesheet. Typical implementations will define some general
	 * base styles and do the "decoration" when asked to fetch the
	 * style for a particular cell.
	 */
	mxStylesheet createStylesheet(GraphOwner owner, Options options);
	
	/**
	 * Generate a style string to be used for the given cell.
	 * <p>
	 * Note that the {@code GraphStyle} implementation itself is not
	 * meant to set the style on a cell directly! It is merely a
	 * source for style definitions and responsible for fetching
	 * the right style.
	 */
	String getStyle(GraphOwner owner, Object cell, Options options);
	
	/**
	 * Hook to release resources and/or to unregister listeners
	 */
	public void close();
}
