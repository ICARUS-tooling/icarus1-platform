/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.tokenizer;

import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.id.Identity;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface Tokenizer {
	
	Identity getIdentity();

	TokenizationResult tokenize(String input, Options options);
}
