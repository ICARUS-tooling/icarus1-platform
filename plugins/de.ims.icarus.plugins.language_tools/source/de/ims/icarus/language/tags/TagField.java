/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.tags;

import java.awt.Color;


/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface TagField {

	TagSet getSet();
	
	String getName();
	
	String getDescription();
	
	Color getColor();
	
	int tagCount();
	
	Tag getTag(int index);
	
	Tag getTag(String name);
}
