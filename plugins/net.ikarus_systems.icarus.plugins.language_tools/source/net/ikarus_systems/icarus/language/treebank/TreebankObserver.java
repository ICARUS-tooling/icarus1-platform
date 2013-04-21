/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.treebank;

import net.ikarus_systems.icarus.language.SentenceData;

/**
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface TreebankObserver {
	
	void treebankDataChanged(int index, SentenceData item);
}
