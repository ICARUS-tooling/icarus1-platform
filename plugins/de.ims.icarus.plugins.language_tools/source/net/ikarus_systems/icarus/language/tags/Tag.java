/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.tags;

import java.awt.Color;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface Tag {

	TagField getField();
	
	String getName();
	
	String getDescription();
	
	Color getColor();
}
