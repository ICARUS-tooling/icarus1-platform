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

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DerivedCorpus extends Corpus {
	
	Corpus getBase();
	
	void setBase(Corpus corpus);
}