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

/**
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface TokenizationResult {
	
	int getResultCount();
	
	String getSource();
	
	String[] getTokens(int index);
	
	double getScore(int index);
	
	Tokenizer getTokenizer();
}