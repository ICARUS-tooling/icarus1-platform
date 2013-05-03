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

import net.ikarus_systems.icarus.util.Options;

import com.mxgraph.view.mxStylesheet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface GraphStyle {

	mxStylesheet createStylesheet(GraphOwner owner, Options options);
	
	String getStyle(GraphOwner owner, Object cell, Options options);
}
