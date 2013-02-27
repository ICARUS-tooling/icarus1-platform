/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools;

import javax.swing.JComponent;

import net.ikarus_systems.icarus.language.Grammar;
import net.ikarus_systems.icarus.plugins.core.Perspective;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class LanguageToolsPerspective extends Perspective {
	
	protected Grammar currentGrammar;

	protected LanguageToolsPerspective() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.Perspective#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.Perspective#isClosable()
	 */
	@Override
	public boolean isClosable() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.Perspective#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
