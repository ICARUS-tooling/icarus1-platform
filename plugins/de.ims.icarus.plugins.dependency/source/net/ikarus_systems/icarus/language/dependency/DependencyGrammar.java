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

import net.ikarus_systems.icarus.language.Grammar;
import net.ikarus_systems.icarus.language.SentenceData;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyGrammar implements Grammar {

	public DependencyGrammar() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.language.Grammar#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return DependencyConstants.GRAMMAR_ID;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.Grammar#createEmptySentenceData()
	 */
	@Override
	public SentenceData createEmptySentenceData() {
		return DependencyUtils.createEmptySentenceData();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.Grammar#createExampleSentenceData()
	 */
	@Override
	public SentenceData createExampleSentenceData() {
		return DependencyUtils.createExampleSentenceData();
	}

	/**
	 * @see net.ikarus_systems.icarus.language.Grammar#getBaseClass()
	 */
	@Override
	public Class<? extends SentenceData> getBaseClass() {
		return DependencyData.class;
	}

}
