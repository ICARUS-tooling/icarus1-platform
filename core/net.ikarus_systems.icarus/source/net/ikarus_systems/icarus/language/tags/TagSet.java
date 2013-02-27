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

import net.ikarus_systems.icarus.language.Grammar;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface TagSet {
	
	Grammar getGrammar();
	
	String getId();
	
	String getName();
	
	String getDescription();
	
	int fieldCount();
	
	TagField getField(int index);
	
	TagField getField(String name);
}
