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
public abstract class AbstractListingLabelSource implements GridLabelSource {

	protected AbstractListingLabelSource() {
		// no-op
	}
	
	protected abstract String getSpanLabel(EntityGridNode node, int index);

	/**
	 * @see de.ims.icarus.plugins.coref.view.grid.labels.GridLabelSource#getLabel(de.ims.icarus.language.coref.Span[], de.ims.icarus.language.coref.CoreferenceData)
	 */
	@Override
	public String getLabel(EntityGridNode node) {
		StringBuilder sb = new StringBuilder();
		
		sb.append('[');
		for(int i=0; i<node.getSpanCount(); i++) {
			if(i>0) {
				sb.append(',');
			}
			sb.append(getSpanLabel(node, i));
		}
		sb.append(']');
		
		return sb.toString();
	}

}
