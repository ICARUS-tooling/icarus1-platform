/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.grid.labels;

import de.ims.icarus.language.coref.Span;
import de.ims.icarus.plugins.coref.view.grid.EntityGridNode;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface GridLabelBuilder {

	/**
	 * Create a textual representation of the {@link Span} at
	 * index {@code spanIndex} of the given {@link EntityGridNode}.
	 */
	public String getLabel(EntityGridNode node, int spanIndex);
}
