/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.language_tools.input;

import java.util.ArrayList;
import java.util.StringTokenizer;

import de.ims.icarus.language.tokenizer.SingleTokenizationResult;
import de.ims.icarus.language.tokenizer.TokenizationResult;
import de.ims.icarus.language.tokenizer.Tokenizer;
import de.ims.icarus.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultTokenizer implements Tokenizer {

	public DefaultTokenizer() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.tokenizer.Tokenizer#tokenize(java.lang.String, de.ims.icarus.util.Options)
	 */
	@Override
	public TokenizationResult tokenize(String input, Options options) {
		if(input==null)
			throw new IllegalArgumentException("Invalid input"); //$NON-NLS-1$
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		StringTokenizer st = new StringTokenizer(input);
		ArrayList<String> forms = new ArrayList<String>();
		
		if (options.get(PRECEDED_ROOT_OPTION, false)) {
			forms.add(ROOT_TOKEN);
		}
		while(st.hasMoreTokens()) {
			forms.add(st.nextToken());
		}
		
		return new SingleTokenizationResult(this, input, forms.toArray(new String[0]));
	}

}
