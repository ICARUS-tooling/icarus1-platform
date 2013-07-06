/*
 * $Revision: 11 $
 * $Date: 2013-03-06 14:36:15 +0100 (Mi, 06 Mrz 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.language_tools/source/net/ikarus_systems/icarus/language/tokenizer/TokenizationResult.java $
 *
 * $LastChangedDate: 2013-03-06 14:36:15 +0100 (Mi, 06 Mrz 2013) $ 
 * $LastChangedRevision: 11 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.tokenizer;

/**
 * 
 * @author Markus Gärtner 
 * @version $Id: TokenizationResult.java 11 2013-03-06 13:36:15Z mcgaerty $
 *
 */
public interface TokenizationResult {
	
	int getResultCount();
	
	String getSource();
	
	String[] getTokens(int index);
	
	double getScore(int index);
	
	Tokenizer getTokenizer();
}