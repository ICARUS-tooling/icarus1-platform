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

import de.ims.icarus.language.Grammar;

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
