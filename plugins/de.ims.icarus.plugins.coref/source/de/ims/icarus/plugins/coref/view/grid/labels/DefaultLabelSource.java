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

import de.ims.icarus.plugins.coref.view.grid.EntityGridNode;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultLabelSource implements GridLabelSource {

	public DefaultLabelSource() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.coref.view.grid.labels.GridLabelSource#getLabel(de.ims.icarus.language.coref.Span, de.ims.icarus.language.coref.CoreferenceData)
	 */
	@Override
	public String getLabel(EntityGridNode node) {
		return "["+node.getSpanCount()+"]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
