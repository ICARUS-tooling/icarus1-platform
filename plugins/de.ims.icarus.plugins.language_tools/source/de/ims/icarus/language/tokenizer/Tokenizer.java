/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.tokenizer;

import de.ims.icarus.util.Options;

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
