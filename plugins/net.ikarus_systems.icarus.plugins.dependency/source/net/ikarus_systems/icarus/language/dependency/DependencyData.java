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
	
	/**
	 * Tests whether a given flag is set on the current {@code SentenceData}
	 * object. The exact meaning of {@code flag} values is implementation group
	 * specific.
	 */
	boolean isFlagSet(int index, long flag);
	
	long getFlags(int index);
}
