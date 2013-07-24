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
public class PropertyLabelSource extends AbstractListingLabelSource {
	
	private final String key;

	public PropertyLabelSource(String key) {
		if(key==null)
			throw new IllegalArgumentException("Invalid key"); //$NON-NLS-1$
		
		this.key = key;
	}

	/**
	 * @see de.ims.icarus.plugins.coref.view.grid.labels.AbstractListingLabelSource#getSpanLabel(de.ims.icarus.language.coref.Span, de.ims.icarus.language.coref.CoreferenceData)
	 */
	@Override
	protected String getSpanLabel(EntityGridNode node, int index) {
		Span span = node.getSpan(index);
		Object value = span.getProperty(key);
		return value==null ? "-" : value.toString();  //$NON-NLS-1$
	}

}
