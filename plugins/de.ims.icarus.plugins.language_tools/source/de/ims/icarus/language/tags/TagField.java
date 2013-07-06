/*
 * $Revision: 11 $
 * $Date: 2013-03-06 14:36:15 +0100 (Mi, 06 Mrz 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.language_tools/source/net/ikarus_systems/icarus/language/tags/TagField.java $
 *
 * $LastChangedDate: 2013-03-06 14:36:15 +0100 (Mi, 06 Mrz 2013) $ 
 * $LastChangedRevision: 11 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.tags;

import java.awt.Color;


/**
 * @author Markus Gärtner 
 * @version $Id: TagField.java 11 2013-03-06 13:36:15Z mcgaerty $
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
