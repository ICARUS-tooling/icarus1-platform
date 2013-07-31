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
package de.ims.icarus.language.tokenizer;

import java.util.Arrays;

import de.ims.icarus.util.Exceptions;


/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public final class SingleTokenizationResult implements TokenizationResult {
	
	private final Tokenizer source;
	
	private final String[] tokens;
	
	private final String input;

	/**
	 * 
	 */
	public SingleTokenizationResult(Tokenizer source, String input, String[] tokens) {
		Exceptions.testNullArgument(source, "source"); //$NON-NLS-1$
		Exceptions.testNullArgument(input, "input"); //$NON-NLS-1$
		Exceptions.testNullArgument(tokens, "tokens"); //$NON-NLS-1$
		
		this.source = source;
		this.input = input;
		this.tokens = tokens;
	}

	/**
	 * @see de.ims.icarus.language.tokenizer.TokenizationResult#getResultCount()
	 */
	@Override
	public int getResultCount() {
		return 1;
	}

	/**
	 * @see de.ims.icarus.language.tokenizer.TokenizationResult#getTokens(int)
	 */
	@Override
	public String[] getTokens(int index) {
		if(index!=0)
			throw new IndexOutOfBoundsException("Illegal index: "+index); //$NON-NLS-1$
		
		return Arrays.copyOf(tokens, tokens.length);
	}

	/**
	 * @see de.ims.icarus.language.tokenizer.TokenizationResult#getScore(int)
	 */
	@Override
	public double getScore(int index) {
		return index==0 ? 0d : 1d;
	}

	/**
	 * @see de.ims.icarus.language.tokenizer.TokenizationResult#getTokenizer()
	 */
	@Override
	public Tokenizer getTokenizer() {
		return source;
	}

	/**
	 * @see de.ims.icarus.language.tokenizer.TokenizationResult#getSource()
	 */
	@Override
	public String getSource() {
		return input;
	}

}
