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
import net.ikarus_systems.icarus.util.id.Identifiable;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface Tokenizer extends Identifiable {

	TokenizationResult tokenize(String input, Options options);
}
