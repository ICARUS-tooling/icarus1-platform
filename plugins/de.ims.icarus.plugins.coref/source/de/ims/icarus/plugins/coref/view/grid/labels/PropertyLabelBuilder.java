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
public class PropertyLabelBuilder implements GridLabelBuilder {
	
	private final String key;

	public PropertyLabelBuilder(String key) {
		if(key==null)
			throw new IllegalArgumentException("Invalid key"); //$NON-NLS-1$
		
		this.key = key;
	}

	/**
	 * @see de.ims.icarus.plugins.coref.view.grid.labels.GridLabelBuilder#getLabel(de.ims.icarus.plugins.coref.view.grid.EntityGridNode, int)
	 */
	@Override
	public String getLabel(EntityGridNode node, int spanIndex) {
		Span span = node.getSpan(spanIndex);
		Object value = span.getProperty(key);
		return value==null ? "-" : value.toString();  //$NON-NLS-1$
	}

}
