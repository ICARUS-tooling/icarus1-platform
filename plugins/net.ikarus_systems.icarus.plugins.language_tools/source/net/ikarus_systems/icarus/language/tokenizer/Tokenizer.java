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

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface Tokenizer {
	
	public final static String PRECEDED_ROOT_OPTION = "precededRoot"; //$NON-NLS-1$
	
	public static final String ROOT_TOKEN = "<root>"; //$NON-NLS-1$

	TokenizationResult tokenize(String input, Options options);
}
