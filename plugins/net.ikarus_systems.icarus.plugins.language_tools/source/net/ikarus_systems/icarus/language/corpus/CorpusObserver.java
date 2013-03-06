/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.corpus;

import net.ikarus_systems.icarus.language.SentenceData;

/**
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface CorpusObserver {
	
	void corpusDataChanged(int index, SentenceData item);
}
