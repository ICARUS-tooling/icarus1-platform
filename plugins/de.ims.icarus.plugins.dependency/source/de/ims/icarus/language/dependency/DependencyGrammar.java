/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.dependency;

import de.ims.icarus.language.Grammar;
import de.ims.icarus.language.SentenceData;

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
	 * @see de.ims.icarus.language.Grammar#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return DependencyConstants.GRAMMAR_ID;
	}

	/**
	 * @see de.ims.icarus.language.Grammar#createEmptySentenceData()
	 */
	@Override
	public SentenceData createEmptySentenceData() {
		return DependencyUtils.createEmptySentenceData();
	}

	/**
	 * @see de.ims.icarus.language.Grammar#createExampleSentenceData()
	 */
	@Override
	public SentenceData createExampleSentenceData() {
		return DependencyUtils.createExampleSentenceData();
	}

	/**
	 * @see de.ims.icarus.language.Grammar#getBaseClass()
	 */
	@Override
	public Class<? extends SentenceData> getBaseClass() {
		return DependencyData.class;
	}

}
