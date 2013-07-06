/*
 * $Revision: 41 $
 * $Date: 2013-05-21 00:46:47 +0200 (Di, 21 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.language_tools/source/net/ikarus_systems/icarus/language/tokenizer/Tokenizer.java $
 *
 * $LastChangedDate: 2013-05-21 00:46:47 +0200 (Di, 21 Mai 2013) $ 
 * $LastChangedRevision: 41 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.tokenizer;

import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner 
 * @version $Id: Tokenizer.java 41 2013-05-20 22:46:47Z mcgaerty $
 *
 */
public interface Tokenizer {
	
	public final static String PRECEDED_ROOT_OPTION = "precededRoot"; //$NON-NLS-1$
	
	public static final String ROOT_TOKEN = "<root>"; //$NON-NLS-1$

	TokenizationResult tokenize(String input, Options options);
}
