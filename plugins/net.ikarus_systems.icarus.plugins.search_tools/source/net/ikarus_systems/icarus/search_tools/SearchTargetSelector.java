/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools;

import java.awt.Component;

import net.ikarus_systems.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchTargetSelector {
	
	/**
	 * Called before any other method to allow the selector to
	 * construct filters that ensure compatibility of returned
	 * target objects towards the given argument.
	 * <p>
	 * The {@code contentType} parameter is guaranteed to be {@code non-null}!
	 */
	void setAllowedContentType(ContentType contentType);
	
	Object getSelectedItem();
	
	void setSelectedItem(Object item);
	
	void clear();

	Component getSelectorComponent();
}
