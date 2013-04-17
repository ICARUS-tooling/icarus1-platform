/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.dependency;

import net.ikarus_systems.icarus.language.SentenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DependencyData extends SentenceData {


	String getForm(int index);

	String getPos(int index);

	String getRelation(int index);
	
	String getLemma(int index);
	
	String getFeatures(int index);
	
	int getHead(int index);
	
	void setFlag(int index, long flag);
	
	void unsetFlag(int index, long flag);
	
	boolean isFlagSet(int index, long flag);
}
