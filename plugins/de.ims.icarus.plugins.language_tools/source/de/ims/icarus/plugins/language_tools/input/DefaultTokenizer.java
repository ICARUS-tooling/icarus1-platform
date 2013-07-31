/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
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
